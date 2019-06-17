#include "gov/nist/ucef/hla/base/FederateBase.h"

#include <thread>
#include <chrono>
#include <iostream>

#include "gov/nist/ucef/hla/base/FederateAmbassador.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/SOMParser.h"
#include "gov/nist/ucef/hla/types.h"
#include "gov/nist/ucef/hla/base/FederateConfiguration.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"

using namespace rti1516e;
using namespace std;
using namespace base;
using namespace base::util;

namespace base
{

	FederateBase::FederateBase() : rtiAmbassadorWrapper( new RTIAmbassadorWrapper() ),
	                               federateAmbassador( make_shared<FederateAmbassador>(this) ),
	                               ucefConfig( make_shared<FederateConfiguration>() ),
	                               lifecycleState( LIFE_CYCLE_UNKNOWN )
	{

	}

	FederateBase::~FederateBase()
	{

	}

	void FederateBase::runFederate()
	{
		// Prepare this federate for execution
		federateSetup();

		// The main execution loop of the federate
		federateExecute();

		// Teardown of the federate
		federateTeardown();
	}

	shared_ptr<FederateConfiguration> FederateBase::getFederateConfiguration()
	{
		return ucefConfig;
	}

	LifecycleState FederateBase::getLifecycleState()
	{
		return this->lifecycleState;
	}

	double FederateBase::getTime()
	{
		return federateAmbassador->getFederateTime();
	}

	void FederateBase::incomingObjectRegistration( long objectInstanceHash, long objectClassHash )
	{
		lock_guard<mutex> lock( threadSafeLock );
		Logger& logger = Logger::getInstance();

		shared_ptr<ObjectClass> objectClass = getObjectClassByClassHandle( objectClassHash );
		if( objectClass )
		{
			shared_ptr<HLAObject> hlaObject = make_shared<HLAObject>( objectClass->name, objectInstanceHash );
			logger.log( "Discovered new object named " + hlaObject->getClassName(), LevelInfo );
			objectDataStoreByInstance[objectInstanceHash] = objectClass;
			receivedObjectRegistration( const_pointer_cast<const HLAObject>(hlaObject),
			                            federateAmbassador->getFederateTime() );

		}
		else
		{
			logger.log( "Discovered an unknown object with class id " +
			            to_string(objectClassHash), LevelWarn );
		}
	}

	void FederateBase::incomingAttributeReflection( long objectInstanceHash,
	                                                map<AttributeHandle, VariableLengthData> const& attributeValues )
	{
		lock_guard<mutex> lock( threadSafeLock );
		Logger& logger = Logger::getInstance();
		shared_ptr<ObjectClass> objectClass = getObjectClassByInstanceHandle( objectInstanceHash );
		if( objectClass )
		{
			logger.log( "Received attribute update for " + objectClass->name, LevelInfo );
			shared_ptr<HLAObject> hlaObject = make_shared<HLAObject>( objectClass->name , objectInstanceHash );

			ObjectClassHandle classHandle = rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( !classHandle.isValid() )
			{
				logger.log( "No valid class handle found for the received attribute update of " +
				            objectClass->name, LevelWarn);
				return;
			}

			for( auto& incomingAttributeValue : attributeValues )
			{
				string attName = rtiAmbassadorWrapper->getAttributeName( classHandle, incomingAttributeValue.first );
				if( attName == "" )
				{
					logger.log( "No valid attribute name found for the received attribute with id : " +
				                to_string(incomingAttributeValue.first.hash()), LevelWarn);
					continue;
				}

				size_t size = incomingAttributeValue.second.size();
				const void* data = incomingAttributeValue.second.data();
				shared_ptr<void> arr(new char[size](), [](char *p) { delete [] p; });
				memcpy( arr.get(), data, size );
				hlaObject->setValue( attName, arr, size );
			}
			receivedAttributeReflection( const_pointer_cast<const HLAObject>(hlaObject),
			                             federateAmbassador->getFederateTime() );
		}
		else
		{
			logger.log( string("Received attribute update of an unknown object."), LevelWarn );
		}
	}

	void FederateBase::incomingInteraction( long interactionHash,
	                                        const ParameterHandleValueMap& parameterValues )
	{
		lock_guard<mutex> lock( threadSafeLock );
		Logger& logger = Logger::getInstance();
		shared_ptr<InteractionClass> interactionClass = getInteractionClass( interactionHash );
		logger.log( "Received interaction update for " + interactionClass->name, LevelInfo );
		if( interactionClass )
		{
			shared_ptr<HLAInteraction> hlaInteraction = make_shared<HLAInteraction>( interactionClass->name );
			populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
			receivedInteraction( const_pointer_cast<const HLAInteraction>(hlaInteraction),
			                     federateAmbassador->getFederateTime() );
		}
		else
		{
			logger.log( "Received an unknown interaction with interaction id " +
			             to_string(interactionHash), LevelWarn );
		}
	}

	void FederateBase::incomingObjectDeletion( long objectInstanceHash )
	{
		lock_guard<mutex> lock( threadSafeLock );
		Logger& logger = Logger::getInstance();

		shared_ptr<ObjectClass> objectClass = getObjectClassByInstanceHandle( objectInstanceHash );
		logger.log( "Received object removed notification for HLAObject with id :" +
		             to_string(objectInstanceHash), LevelInfo );

		bool success = deleteIncomingInstanceHandle( objectInstanceHash );
		if( success )
		{
			logger.log( "HLAObject with id :" + to_string( objectInstanceHash ) +
			            " successfully removed from the incoming map.", LevelInfo );
			shared_ptr<HLAObject> hlaObject = make_shared<HLAObject>( objectClass->name, objectInstanceHash );
			receivedObjectDeletion( const_pointer_cast<const HLAObject>(hlaObject) );
		}
		else
			logger.log( "HLAObject with id :" + to_string(objectInstanceHash) +
			            " could not find for deletion.", LevelWarn );
	}

	shared_ptr<InteractionClass> FederateBase::getInteractionClass( long hash )
	{
		if( interactionDataStoreByHash.find( hash ) != interactionDataStoreByHash.end() )
		{
			return interactionDataStoreByHash[hash];
		}
		return nullptr;
	}

	void FederateBase::federateSetup()
	{
		lifecycleState = INITIALIZING;

		// initialise rti ambassador
		connectToRti();

		// create federation
		createFederation();
		// join the federation
		joinFederation();
		// enables time management policy for this federate
		enableTimePolicy();

		// inform RTI about the data we are going publish and subscribe
		publishAndSubscribe();

		// lifecycle hook
		tickForCallBacks();
		beforeReadyToPopulate();

		// now we are ready to populate the federation
		synchronize( READY_TO_POPULATE );

		// before federate run hook
		tickForCallBacks();
		beforeReadyToRun();

		// now we are ready to run this federate
		synchronize( READY_TO_RUN );

		// just before the first update hook
		tickForCallBacks();
		beforeFirstStep();
	}

	void FederateBase::federateExecute()
	{
		while( true )
		{
			if( !execute() )
				break;
		}
	}

	bool FederateBase::execute()
	{
		lifecycleState = RUNNING;

		bool continueFedEx = step( federateAmbassador->getFederateTime() );
		if( continueFedEx )
			advanceTime();
		return continueFedEx;
	}

	void FederateBase::federateTeardown()
	{
		lifecycleState = CLEANING_UP;

		disableTimePolicy();

		// before resigning the federation hook
		tickForCallBacks();
		beforeReadyToResign();

		// now we are ready to resign from this federation
		if( ucefConfig->getSyncBeforeResign() )
			synchronize( READY_TO_RESIGN );

		// before exit hook for cleanup
		tickForCallBacks();

		lifecycleState = EXPIRED;

		beforeExit();

		// resign from this federation
		resignAndDestroy();
	}

	void FederateBase::advanceTime()
	{
		Logger& logger = Logger::getInstance();

		double requestedTime = federateAmbassador->getFederateTime() + ucefConfig->getTimeStep();
		try
		{
			rtiAmbassadorWrapper->timeAdvanceRequest( requestedTime );
		}
		catch( UCEFException& )
		{
			throw;
		}

		// wait for the rti grant the requested time advancement
		logger.log( "Request a time advance to " + to_string(requestedTime), LevelInfo );
		while( federateAmbassador->getFederateTime() < requestedTime )
		{
			logger.log( "Waiting for the logical time of this federate to advance to " +
			            to_string( requestedTime ), LevelDebug );

			tickForCallBacks();
		}
		logger.log( "The logical time of this federate advanced to " + to_string( requestedTime ), LevelInfo );
	}

	void FederateBase::registerSyncPoint( string& synchPoint )
	{
		Logger& logger = Logger::getInstance();

		// announce synch point to the federation
		try
		{
			rtiAmbassadorWrapper->registerFederationSynchronizationPoint( synchPoint );
		}
		catch( UCEFException& )
		{
			throw;
		}

		logger.log( "Waiting for the announcement of synchronization Point " + synchPoint, LevelInfo );
		while( !federateAmbassador->isAnnounced(synchPoint) )
		{
			logger.log( "Waiting for the announcement of synchronization Point " + synchPoint, LevelDebug);
			tickForCallBacks();
		}

		logger.log("Successfully announced the synchronization Point " + synchPoint, LevelInfo);
	}

	void FederateBase::achieveSynchronization( string& synchPoint )
	{
		try
		{
			rtiAmbassadorWrapper->synchronizationPointAchieved( synchPoint );
		}
		catch( UCEFException& )
		{
			throw;
		}
	}

	bool FederateBase::isAchieved( string& synchPoint )
	{
		bool achieved = federateAmbassador->isAchieved( synchPoint );
		return achieved;
	}

	void FederateBase::populateInteraction( const string& interactionClassName,
	                                        shared_ptr<HLAInteraction>& hlaInteraction,
	                                        const ParameterHandleValueMap& parameterValues )
	{
		Logger& logger = Logger::getInstance();
		InteractionClassHandle interactionHandle =
			rtiAmbassadorWrapper->getInteractionHandle( interactionClassName );

		if( !interactionHandle.isValid() )
		{
			logger.log( "No valid interaction handle found for the received interaction of " +
			            interactionClassName, LevelWarn );
			return;
		}

		for( auto& incomingParameterValue : parameterValues )
		{
			string paramName =
				rtiAmbassadorWrapper->getParameterName( interactionHandle, incomingParameterValue.first );
			if( paramName == "" )
			{
				logger.log( "No valid parameter name found for the received parameter with id : " +
				            to_string(incomingParameterValue.first.hash()), LevelWarn);
				continue;
			}

			size_t size = incomingParameterValue.second.size();
			const void* data = incomingParameterValue.second.data();
			shared_ptr<void> arr(new char[size](), [](char *p) { delete[] p; });
			memcpy( arr.get(), data, size );
			hlaInteraction->setValue( paramName, arr, size );
		}
	}

	//----------------------------------------------------------
	//                    Business Logic
	//----------------------------------------------------------

	void FederateBase::connectToRti()
	{
		try
		{
			rtiAmbassadorWrapper->connect( federateAmbassador, ucefConfig->isImmediate() );
			Logger::getInstance().log( ucefConfig->getFederateName() + " connected to RTI.", LevelInfo );
		}
		catch( UCEFException& )
		{
			throw;
		}
	}

	void FederateBase::createFederation()
	{
		if( !ucefConfig->isPermittedToCreateFederation() )
		{
			Logger::getInstance().log( " Do not have permission to create " + ucefConfig->getFederationName(), LevelInfo );
			return;
		}

		try
		{
			rtiAmbassadorWrapper->createFederation( ucefConfig->getFederationName(),  ucefConfig->getFomPaths() );
			Logger::getInstance().log( "Federation : " + ucefConfig->getFederationName() + " created.", LevelInfo );
		}
		catch( UCEFException& )
		{
			throw;
		}
	}

	void FederateBase::joinFederation()
	{
		bool hasJoined = false;
		int attemptCount = 0;
		int retryInterval = ucefConfig->getRetryInterval();
		while( !hasJoined )
		{
			try
			{
				Logger::getInstance().log( "Trying to join : " + ucefConfig->getFederationName(), LevelInfo );

				rtiAmbassadorWrapper->joinFederation( ucefConfig->getFederateName(),
													  ucefConfig->getFederateType(),
													  ucefConfig->getFederationName() );

				Logger::getInstance().log( ucefConfig->getFederateName() + " joined the federation " +
										   ucefConfig->getFederationName() + ".", LevelInfo );
				hasJoined = true;
			}
			catch( UCEFException& )
			{
				attemptCount++;
				Logger::getInstance().log( "Failed to join : " + ucefConfig->getFederationName(), LevelWarn );
				Logger::getInstance().log( "Retrying in : " + to_string(retryInterval) + " seconds.", LevelWarn );
				this_thread::sleep_for( chrono::seconds(retryInterval) );

				if( attemptCount >= ucefConfig->getMaxJoinAttempts() )
				{
					Logger::getInstance().log( "Tried " + to_string(attemptCount) + " and could not connect.", LevelWarn );
					Logger::getInstance().log( "Failing permanently.", LevelError );
					throw;
				}
			}
		}
	}

	void FederateBase::enableTimePolicy()
	{
		Logger& logger = Logger::getInstance();

		if( ucefConfig->isTimeRegulated() )
		{
			logger.log( string("Inform time policy - regulated to RTI."), LevelInfo );

			try
			{
				rtiAmbassadorWrapper->enableTimeRegulation( ucefConfig->getLookAhead() );
			}
			catch( UCEFException& )
			{
				throw;
			}

			while( !federateAmbassador->isTimeRegulated() )
			{
				tickForCallBacks();
			}
			logger.log( string("RTI acknowledged time policy - regulated"), LevelInfo );
		}

		if( ucefConfig->isTimeConstrained() )
		{
			logger.log( string("Inform time policy - constrain to RTI."), LevelInfo );

			try
			{
				rtiAmbassadorWrapper->enableTimeConstrained();
			}
			catch( UCEFException& )
			{
				throw;
			}

			while( !federateAmbassador->isTimeConstrained() )
			{
				tickForCallBacks();
			}
			logger.log( string("RTI acknowledged time policy - constrain"), LevelInfo );
		}
	}

	void FederateBase::disableTimePolicy()
	{
		Logger& logger = Logger::getInstance();

		if( ucefConfig->isTimeRegulated() )
		{
			logger.log( string("Disable time policy - regulated"), LevelInfo );

			try
			{
				rtiAmbassadorWrapper->disableTimeRegulation();
				federateAmbassador->setTimeRegulatedFlag( false );
			}
			catch( UCEFException& )
			{
				throw;
			}
		}

		if( ucefConfig->isTimeConstrained() )
		{
			logger.log( string("Disable time policy - constrained"), LevelInfo );

			try
			{
				rtiAmbassadorWrapper->disableTimeConstrained();
				federateAmbassador->setTimeConstrainedFlag( false );
			}
			catch( UCEFException& )
			{
				throw;
			}
		}
	}

	void FederateBase::publishAndSubscribe()
	{
		Logger& logger = Logger::getInstance();
		vector<string> somPaths = ucefConfig->getSomPaths();
		// note: currently SOM parser can only accommodate a single SOM file
		if( somPaths.size() )
		{
			// parse the SOM file and build up the HLA object classes
			vector<shared_ptr<ObjectClass>> objectClasses = SOMParser::getObjectClasses( somPaths[0] );
			logger.log( string("Inform RTI about publishing and subscribing classes"), LevelInfo );

			publishObjectClassAttributes( objectClasses );
			subscribeObjectClassAttributes (objectClasses );
			storeObjectClassData( objectClasses );

			// parse the SOM file and build up the HLA object classes
			vector<shared_ptr<InteractionClass>> interactionClasses =
				SOMParser::getInteractionClasses( somPaths[0] );
			logger.log(string("Inform RTI about publishing and subscribing interactions"), LevelInfo);

			publishInteractionClasses( interactionClasses );
			subscribeInteractionClasses( interactionClasses );
			storeInteractionClassData( interactionClasses );
		}
	}

	void FederateBase::synchronize( SynchPoint point )
	{
		Logger& logger = Logger::getInstance();
		string synchPointStr = ConversionHelper::SynchPointToString( point );
		registerSyncPoint( synchPointStr );

		// immediately achieve the announced synch point
		achieveSynchronization( synchPointStr );

		logger.log( "Waiting till the federation achieve synchronization " + ConversionHelper::SynchPointToString(point), LevelInfo );
		while( !this->isAchieved(synchPointStr) )
		{
			logger.log( "Waiting till the federation achieve synchronization " +
						ConversionHelper::SynchPointToString(point), LevelDebug );
			tickForCallBacks();
		}

		logger.log( "Federation achieved synchronization Point " +
					ConversionHelper::SynchPointToString(point), LevelInfo );
	}

	void FederateBase::resignAndDestroy()
	{
		Logger& logger = Logger::getInstance();
		//----------------------------------------------------------
		//            delete object instance handles
		//----------------------------------------------------------
		logger.log( string("Federate ") + ucefConfig->getFederateName()  + " resigning from federation " +
					ucefConfig->getFederationName(), LevelInfo );
		try
		{
			rtiAmbassadorWrapper->resign();
		}
		catch( UCEFException& )
		{
			throw;
		}
		logger.log( string("Federate ") + ucefConfig->getFederateName() + " resigned from federation " +
					ucefConfig->getFederationName(), LevelInfo );
	}

	shared_ptr<ObjectClass> FederateBase::getObjectClassByClassHandle( long hash )
	{
		if( objectDataStoreByHash.find( hash ) != objectDataStoreByHash.end() )
		{
			return objectDataStoreByHash[hash];
		}
		return nullptr;
	}

	shared_ptr<ObjectClass> FederateBase::getObjectClassByInstanceHandle( long hash )
	{
		if( objectDataStoreByInstance.find( hash ) != objectDataStoreByInstance.end() )
		{
			return objectDataStoreByInstance[hash];
		}
		return nullptr;
	}

	bool FederateBase::deleteIncomingInstanceHandle( long hash )
	{
		size_t deletedCount = objectDataStoreByInstance.erase( hash );
		bool success = deletedCount ? true : false;
		return success;
	}

	void FederateBase::storeObjectClassData( vector<shared_ptr<ObjectClass>>& objectClasses )
	{
		//----------------------------------------------------------
		//            Store object class and attribute handles
		//----------------------------------------------------------

		// try to update object classes with correct class and attribute rti handles
		for( auto& objectClass : objectClasses )
		{
			// store ObjectClass in m_objectCacheStoreByName for later use
			ucefConfig->cacheObjectClass( objectClass );

			ObjectClassHandle classHandle = rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( classHandle.isValid() )
			{
				// store the ObjectClass in m_objectCacheStoreByHash for later use
				objectDataStoreByHash.insert( make_pair( classHandle.hash(), objectClass ) );
			}
		}
	}

	void FederateBase::storeInteractionClassData( vector<shared_ptr<InteractionClass>>& interactionClasses )
	{
		//----------------------------------------------------------
		//     Store interaction class and parameter handles
		//----------------------------------------------------------

		// try to update interaction classes with correct interaction and parameter rti handles
		for( auto& interactionClass : interactionClasses )
		{
			// store interaction class in m_objectCacheStoreByName for later use
			ucefConfig->cacheInteractionClass( interactionClass );

			// now store the ObjectClass in m_objectCacheStoreByHash for later use
			InteractionClassHandle interactionHandle =
			                     rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( interactionHandle.isValid() )
			{
				interactionDataStoreByHash.insert(
				                make_pair( interactionHandle.hash(), interactionClass ) );
			}
		}
	}

	void FederateBase::publishObjectClassAttributes( vector<shared_ptr<ObjectClass>>& objectClasses )
	{
		//----------------------------------------------------------
		// Inform RTI about the classes and attributes that
		// are published by this federate
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();
		for( auto objectClass : objectClasses )
		{
			ObjectClassHandle classHandle = rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( !classHandle.isValid() )
			{
				continue;
			}
			// attributes we are going to publish
			AttributeHandleSet pubAttributes;

			ObjectAttributes &objectAtributes = objectClass->objectAttributes;
			for( auto& attributePair : objectAtributes )
			{
				shared_ptr<ObjectAttribute> attribute = attributePair.second;
				AttributeHandle attHandle = rtiAmbassadorWrapper->getAttributeHandle( classHandle, attribute->name );
				if( !attHandle.isValid() )
				{
					continue;
				}
				if( attribute->publish )
				{
					logger.log( "Federate publishes an attribute named " + attribute->name + " in " +
					            objectClass->name , LevelInfo );
					pubAttributes.insert( attHandle );
				}
			}
			rtiAmbassadorWrapper->publishObjectClassAttributes( classHandle,
			                                                    pubAttributes );
		}
	}

	void FederateBase::subscribeObjectClassAttributes( vector<shared_ptr<ObjectClass>>& objectClasses )
	{
		//----------------------------------------------------------
		// Inform RTI about the classes and attributes that
		// are subscribed by this federate
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();
		for( auto objectClass : objectClasses )
		{
			ObjectClassHandle classHandle = rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( !classHandle.isValid() )
			{
				logger.log( "Received an invalid handle for " + objectClass->name  + ", something went wrong.",
				             LevelWarn );
				continue;
			}
			// attributes we are going to subscribe
			AttributeHandleSet subAttributes;

			ObjectAttributes &objectAtributes = objectClass->objectAttributes;
			for( auto& attributePair : objectAtributes )
			{
				shared_ptr<ObjectAttribute> attribute = attributePair.second;
				AttributeHandle attHandle = rtiAmbassadorWrapper->getAttributeHandle( classHandle, attribute->name );
				if( !attHandle.isValid() )
				{
					logger.log( "Received an invalid attribute handle for " + attribute->name +
					            " in " + objectClass->name  + ", something went wrong.", LevelWarn );
					continue;
				}
				if( attribute->subscribe )
				{
					logger.log( "Federate subscribed to an attribute named " + attribute->name + " in " +
					            objectClass->name, LevelInfo );
					subAttributes.insert( attHandle );
				}
			}

			rtiAmbassadorWrapper->subscribeObjectClassAttributes( classHandle,
			                                                      subAttributes );

		}
	}

	void FederateBase::publishInteractionClasses(vector<shared_ptr<InteractionClass>>& interactionClasses)
	{
		//--------------------------------------------
		// Inform RTI about interactions that are
		// published by this federate
		//--------------------------------------------

		Logger& logger = Logger::getInstance();
		for( auto& interactionClass : interactionClasses )
		{
			InteractionClassHandle interactionHandle =
				rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( interactionClass->publish )
			{
				logger.log( "Federate publishes interaction class " + interactionClass->name, LevelInfo);
				rtiAmbassadorWrapper->publishInteractionClass( interactionHandle );
			}
		}
	}

	void FederateBase::subscribeInteractionClasses(vector<shared_ptr<InteractionClass>>& interactionClasses)
	{
		//----------------------------------------------------------
		// Inform RTI about interactions that are subscribed
		// by this federate
		//----------------------------------------------------------

		Logger& logger = Logger::getInstance();
		for( auto& interactionClass : interactionClasses )
		{
			InteractionClassHandle interactionHandle =
				rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( interactionClass->subscribe )
			{
				logger.log( "Federate subscribed to Interaction class " + interactionClass->name, LevelInfo);
				rtiAmbassadorWrapper->subscribeInteractionClasses( interactionHandle );
			}
		}
	}

	void FederateBase::tickForCallBacks()
	{
		if( ucefConfig->isImmediate() )
		{
			this_thread::sleep_for( chrono::microseconds( 10 ) );
		}
		else
		{
			rtiAmbassadorWrapper->evokeMultipleCallbacks( 0.1, 1.0 );
		}
	}
}

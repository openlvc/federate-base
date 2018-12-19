#include "FederateBase.h"

#include <thread>
#include <chrono>
#include <iostream>

#include "gov/nist/ucef/hla/FederateAmbassador.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/SOMParser.h"
#include "gov/nist/ucef/util/types.h"
#include "gov/nist/ucef/util/FederateConfiguration.h"
#include "gov/nist/ucef/util/UCEFException.h"

#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"
#include "RTI/Typedefs.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	FederateBase::FederateBase() : m_rtiAmbassadorWrapper( new RTIAmbassadorWrapper() ),
	                               m_federateAmbassador( make_shared<FederateAmbassador>(this) ),
	                               m_ucefConfig( make_shared<FederateConfiguration>() )
	{

	}

	FederateBase::~FederateBase()
	{

	}

	void FederateBase::runFederate()
	{
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
		beforeReadyToPopulate();

		// now we are ready to populate the federation
		synchronize( READY_TO_POPULATE );

		// before federate run hook
		beforeReadyToRun();
		// now we are ready to run this federate
		synchronize( READY_TO_RUN );

		// just before the first update hook
		beforeFirstStep();

		while( true )
		{
			if( step( m_federateAmbassador->getFederateTime() ) == false )
				break;
			advanceTime();
		}

		disableTimePolicy();

		// before resigning the federation hook
		beforeReadyToResign();

		// now we are ready to resign from this federation
		synchronize( READY_TO_RESIGN );

		// before exit hook for cleanup
		beforeExit();

		// resign from this federation
		resignAndDestroy();
	}

	std::shared_ptr<util::FederateConfiguration> FederateBase::getFederateConfiguration()
	{
		return m_ucefConfig;
	}

	void FederateBase::connectToRti()
	{
		try
		{
			m_rtiAmbassadorWrapper->connect( m_federateAmbassador, m_ucefConfig->isImmediate() );
			Logger::getInstance().log( m_ucefConfig->getFederateName() + " connected to RTI.", LevelInfo );
		} 
		catch( UCEFException& )
		{
			throw;
		}
	}

	void FederateBase::createFederation()
	{
		try
		{
			m_rtiAmbassadorWrapper->createFederation( m_ucefConfig->getFederationName(),  m_ucefConfig->getFomPaths() );
			Logger::getInstance().log( "Federation " + m_ucefConfig->getFederationName() + " created.", LevelInfo );
		}
		catch( UCEFException& )
		{
			throw;
		}
	}

	void FederateBase::joinFederation()
	{
		try
		{
			m_rtiAmbassadorWrapper->joinFederation( m_ucefConfig->getFederateName(),
			                                        m_ucefConfig->getFederateType(),
			                                        m_ucefConfig->getFederationName());
			Logger::getInstance().log( m_ucefConfig->getFederateName() + " joined the federation " +
			                           m_ucefConfig->getFederationName() + ".", LevelInfo );
		}
		catch( UCEFException& )
		{
			throw;
		}
	}

	void FederateBase::enableTimePolicy()
	{
		Logger& logger = Logger::getInstance();

		if( m_ucefConfig->isTimeRegulated() )
		{
			logger.log( string("Inform time policy - regulated to RTI."), LevelInfo );

			try
			{
				m_rtiAmbassadorWrapper->enableTimeRegulation( m_ucefConfig->getLookAhead() );
			}
			catch( UCEFException& )
			{
				throw;
			}

			while( !m_federateAmbassador->isTimeRegulated() )
			{
				tickForCallBacks();
			}
			logger.log( string("RTI acknowledged time policy - regulated"), LevelInfo );
		}

		if( m_ucefConfig->isTimeConstrained() )
		{
			logger.log( string("Inform time policy - constrain to RTI."), LevelInfo );

			try
			{
				m_rtiAmbassadorWrapper->enableTimeConstrained();
			}
			catch( UCEFException& )
			{
				throw;
			}

			while( !m_federateAmbassador->isTimeConstrained() )
			{
				tickForCallBacks();
			}
			logger.log( string("RTI acknowledged time policy - constrain"), LevelInfo );
		}
	}

	void FederateBase::disableTimePolicy()
	{
		Logger& logger = Logger::getInstance();

		if( m_ucefConfig->isTimeRegulated() )
		{
			logger.log( string("Disable time policy - regulated"), LevelInfo );

			try
			{
				m_rtiAmbassadorWrapper->disableTimeRegulation();
				m_federateAmbassador->setTimeRegulatedFlag( false );
			}
			catch( UCEFException& )
			{
				throw;
			}
		}

		if( m_ucefConfig->isTimeConstrained() )
		{
			logger.log( string("Disable time policy - constrained"), LevelInfo );

			try
			{
				m_rtiAmbassadorWrapper->disableTimeConstrained();
				m_federateAmbassador->setTimeConstrainedFlag( false );
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
		std::vector<std::string> somPaths = m_ucefConfig->getSomPaths();
		// note: currently SOM parser can only accommodate a single SOM file
		if( somPaths.size() )
		{
			// parse the SOM file and build up the HLA object classes
			vector<shared_ptr<ObjectClass>> objectClasses = SOMParser::getObjectClasses( somPaths[0] );
			logger.log(string("Inform RTI about publishing and subscribing classes"), LevelInfo);

			publishObjectClassAttributes(objectClasses);
			subscribeObjectClassAttributes(objectClasses);
			storeObjectClassData(objectClasses);

			// parse the SOM file and build up the HLA object classes
			vector<shared_ptr<InteractionClass>> interactionClasses =
				SOMParser::getInteractionClasses( somPaths[0] );
			logger.log(string("Inform RTI about publishing and subscribing interactions"), LevelInfo);

			publishInteractionClasses(interactionClasses);
			subscribeInteractionClasses(interactionClasses);
			storeInteractionClassData(interactionClasses);
		}
	}

	void FederateBase::synchronize( SynchPoint point )
	{
		Logger& logger = Logger::getInstance();
		string synchPointStr = ConversionHelper::SynchPointToString( point );
		// announce synch point
		try
		{
			m_rtiAmbassadorWrapper->registerFederationSynchronizationPoint( synchPointStr );
		}
		catch( UCEFException& )
		{
			throw;
		}

		while( !m_federateAmbassador->isAnnounced(synchPointStr) )
		{
			logger.log( "Waiting for the announcement of synchronization Point " +
			            ConversionHelper::SynchPointToString(point), LevelInfo );
			tickForCallBacks();
		}

		logger.log( "Successfully announced the synchronization Point " +
		            ConversionHelper::SynchPointToString(point), LevelInfo );

		// immedietly acheive the announced synch point
		try
		{
			m_rtiAmbassadorWrapper->synchronizationPointAchieved( synchPointStr );
		}
		catch( UCEFException& )
		{
			throw;
		}

		while( !m_federateAmbassador->isAchieved(synchPointStr) )
		{
			logger.log( "Waiting for the federation to synchronise to " +
			            ConversionHelper::SynchPointToString(point), LevelInfo );
			tickForCallBacks();
		}

		logger.log( "Federation achieved synchronization Point " +
		            ConversionHelper::SynchPointToString(point), LevelInfo );
	}

	void FederateBase::advanceTime()
	{
		Logger& logger = Logger::getInstance();

		double requestedTime = m_federateAmbassador->getFederateTime() + m_ucefConfig->getTimeStep();
		try
		{
			m_rtiAmbassadorWrapper->timeAdvanceRequest( requestedTime );
		}
		catch( UCEFException& )
		{
			throw;
		}

		// wait for the rti grant the requested time advancement
		while( m_federateAmbassador->getFederateTime() < requestedTime )
		{
			logger.log( "Waiting for the logical time of this federate to advance to " +
			            to_string( requestedTime ), LevelInfo );

			tickForCallBacks();
		}
		logger.log( "The logical time of this federate advanced to " + to_string( requestedTime ), LevelInfo );
	}

	void FederateBase::incomingObjectRegistration( long objectInstanceHash, long objectClassHash )
	{
		lock_guard<mutex> lock( m_threadSafeLock );
		Logger& logger = Logger::getInstance();

		shared_ptr<ObjectClass> objectClass = getObjectClassByClassHandle( objectClassHash );
		if( objectClass )
		{
			shared_ptr<HLAObject> hlaObject = make_shared<HLAObject>( objectClass->name, objectInstanceHash );
			logger.log( "Discovered new object named " + hlaObject->getClassName(), LevelCritical );
			m_objectDataStoreByInstance[objectInstanceHash] = objectClass;
			receivedObjectRegistration( const_pointer_cast<const HLAObject>(hlaObject),
			                            m_federateAmbassador->getFederateTime() );

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
		lock_guard<mutex> lock( m_threadSafeLock );
		Logger& logger = Logger::getInstance();
		shared_ptr<ObjectClass> objectClass = getObjectClassByInstanceHandle( objectInstanceHash );
		if( objectClass )
		{
			logger.log( "Received attribute update for " + objectClass->name, LevelCritical );
			shared_ptr<HLAObject> hlaObject = make_shared<HLAObject>( objectClass->name , objectInstanceHash );

			ObjectClassHandle classHandle = m_rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( !classHandle.isValid() )
			{
				logger.log( "No valid class handle found for the received attribute update of " +
				            objectClass->name, LevelCritical );
				return;
			}

			for( auto& incomingAttributeValue : attributeValues )
			{
				string attName = m_rtiAmbassadorWrapper->getAttributeName( classHandle, incomingAttributeValue.first );
				if( attName == "" )
				{
					logger.log( "No valid attribute name found for the received attribute with id : " +
				                to_string(incomingAttributeValue.first.hash()), LevelCritical );
					continue;
				}

				size_t size = incomingAttributeValue.second.size();
				const void* data = incomingAttributeValue.second.data();
				shared_ptr<void> arr(new char[size](), [](char *p) { delete [] p; });
				memcpy( arr.get(), data, size );
				hlaObject->setValue( attName, arr, size );
			}
			receivedAttributeReflection( const_pointer_cast<const HLAObject>(hlaObject),
			                             m_federateAmbassador->getFederateTime() );
		}
		else
		{
			logger.log( string("Received attribute update of an unknown object."), LevelWarn );
		}
	}

	void FederateBase::incomingInteraction( long interactionHash,
	                                        const ParameterHandleValueMap& parameterValues )
	{
		lock_guard<mutex> lock( m_threadSafeLock );
		Logger& logger = Logger::getInstance();
		shared_ptr<InteractionClass> interactionClass = getInteractionClass( interactionHash );
		logger.log( "Received interaction update for " + interactionClass->name, LevelCritical );
		if( interactionClass )
		{
			shared_ptr<HLAInteraction> hlaInteraction = make_shared<HLAInteraction>( interactionClass->name );
			InteractionClassHandle interactionHandle =
				m_rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( !interactionHandle.isValid() )
			{
				logger.log( "No valid interaction handle found for the received interaction of " +
				            interactionClass->name, LevelCritical );
				return;
			}

			for( auto& incomingParameterValue : parameterValues )
			{
				string paramName =
					m_rtiAmbassadorWrapper->getParameterName( interactionHandle, incomingParameterValue.first );
				if( paramName == "" )
				{
					logger.log( "No valid parameter name found for the received parameter with id : " +
				                to_string(incomingParameterValue.first.hash()), LevelCritical );
					continue;
				}

				size_t size = incomingParameterValue.second.size();
				const void* data = incomingParameterValue.second.data();
				shared_ptr<void> arr( new char[size](), [](char *p) { delete [] p; } );
				memcpy( arr.get(), data, size );
				hlaInteraction->setValue( paramName, arr, size );
			}
			receivedInteraction( const_pointer_cast<const HLAInteraction>(hlaInteraction),
			                     m_federateAmbassador->getFederateTime() );
		}
		else
		{
			logger.log( "Received an unknown interation with interaction id " +
			             to_string(interactionHash), LevelWarn );
		}
	}

	void FederateBase::incomingObjectDeletion( long objectInstanceHash )
	{
		lock_guard<mutex> lock( m_threadSafeLock );
		Logger& logger = Logger::getInstance();

		shared_ptr<ObjectClass> objectClass = getObjectClassByInstanceHandle( objectInstanceHash );
		logger.log( "Received object removed notification for HLAObject with id :" +
		             to_string(objectInstanceHash), LevelInfo );

		bool success = deleteIncomingInstanceHandle( objectInstanceHash );
		if( success )
		{
			logger.log( "HLAObject with id :" + to_string( objectInstanceHash ) +
			            " successsfully removed from the incoming map.", LevelInfo );
			shared_ptr<HLAObject> hlaObject = make_shared<HLAObject>( objectClass->name, objectInstanceHash );
			receivedObjectDeletion( const_pointer_cast<const HLAObject>(hlaObject) );
		}
		else
			logger.log( "HLAObject with id :" + to_string(objectInstanceHash) +
			            " could not find for deletion.", LevelWarn );
	}
	
	shared_ptr<ObjectClass> FederateBase::getObjectClassByClassHandle( long hash )
	{
		if( m_objectDataStoreByHash.find( hash ) != m_objectDataStoreByHash.end() )
		{
			return m_objectDataStoreByHash[hash];
		}
		return nullptr;
	}

	shared_ptr<ObjectClass> FederateBase::getObjectClassByInstanceHandle( long hash )
	{
		if( m_objectDataStoreByInstance.find( hash ) != m_objectDataStoreByInstance.end() )
		{
			return m_objectDataStoreByInstance[hash];
		}
		return nullptr;
	}

	bool FederateBase::deleteIncomingInstanceHandle( long hash )
	{
		size_t deletedCount = m_objectDataStoreByInstance.erase( hash );
		bool success = deletedCount ? true : false;
		return success;
	}

	shared_ptr<InteractionClass> FederateBase::getInteractionClass( long hash )
	{
		if( m_interactionDataStoreByHash.find( hash ) != m_interactionDataStoreByHash.end() )
		{
			return m_interactionDataStoreByHash[hash];
		}
		return nullptr;
	}

	void FederateBase::resignAndDestroy()
	{
		Logger& logger = Logger::getInstance();
		//----------------------------------------------------------
		//            delete object instance handles
		//----------------------------------------------------------
		logger.log( string("Federate ") + m_ucefConfig->getFederateName()  + " resigning from federation " +
		            m_ucefConfig->getFederationName(), LevelInfo );
		try
		{
			m_rtiAmbassadorWrapper->resign();
		}
		catch( UCEFException& )
		{
			throw;
		}
		logger.log( string("Federate ") + m_ucefConfig->getFederateName() + " resigned from federation " +
		            m_ucefConfig->getFederationName(), LevelInfo );
	}

	//----------------------------------------------------------
	//                    Business Logic
	//----------------------------------------------------------
	void FederateBase::storeObjectClassData( vector<shared_ptr<ObjectClass>>& objectClasses )
	{
		//----------------------------------------------------------
		//            Store object class and attribute handles
		//----------------------------------------------------------

		// try to update object classes with correct class and attribute rti handles
		for( auto& objectClass : objectClasses )
		{
			// store ObjectClass in m_objectCacheStoreByName for later use
			m_ucefConfig->cacheObjectClass( objectClass );

			ObjectClassHandle classHandle = m_rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( classHandle.isValid() )
			{
				// store the ObjectClass in m_objectCacheStoreByHash for later use
				m_objectDataStoreByHash.insert( make_pair( classHandle.hash(), objectClass ) );
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
			ObjectClassHandle classHandle = m_rtiAmbassadorWrapper->getClassHandle( objectClass->name );
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
				AttributeHandle attHandle = m_rtiAmbassadorWrapper->getAttributeHandle( classHandle, attribute->name );
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
			m_rtiAmbassadorWrapper->publishObjectClassAttributes( classHandle,
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
			ObjectClassHandle classHandle = m_rtiAmbassadorWrapper->getClassHandle( objectClass->name );
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
				AttributeHandle attHandle = m_rtiAmbassadorWrapper->getAttributeHandle( classHandle, attribute->name );
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

			m_rtiAmbassadorWrapper->subscribeObjectClassAttributes( classHandle,
			                                                        subAttributes );

		}
	}

	void FederateBase::storeInteractionClassData( vector<shared_ptr<InteractionClass>>& interactionClasses )
	{
		//----------------------------------------------------------
		//     Store interaction class and parametere handles
		//----------------------------------------------------------

		// try to update interaction classes with correct interaction and parameter rti handles
		for( auto& interactionClass : interactionClasses )
		{
			// store interaction class in m_objectCacheStoreByName for later use
			m_ucefConfig->cacheInteractionClass( interactionClass );

			// now store the ObjectClass in m_objectCacheStoreByHash for later use
			InteractionClassHandle interactionHandle =
			                     m_rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( interactionHandle.isValid() )
			{
				m_interactionDataStoreByHash.insert(
				                make_pair( interactionHandle.hash(), interactionClass ) );
			}
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
				m_rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( interactionClass->publish )
			{
				logger.log( "Federate publishes interaction class " + interactionClass->name, LevelInfo);
				m_rtiAmbassadorWrapper->publishInteractionClass( interactionHandle );
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
				m_rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( interactionClass->subscribe )
			{
				logger.log( "Federate subscribed to Interaction class " + interactionClass->name, LevelInfo);
				m_rtiAmbassadorWrapper->subscribeInteractionClasses( interactionHandle );
			}
		}
	}

	void FederateBase::tickForCallBacks()
	{
		if( m_ucefConfig->isImmediate() )
		{
			this_thread::sleep_for( chrono::microseconds( 10 ) );
		}
		else
		{
			m_rtiAmbassadorWrapper->evokeMultipleCallbacks( 0.1, 1.0 );
		}
	}
}

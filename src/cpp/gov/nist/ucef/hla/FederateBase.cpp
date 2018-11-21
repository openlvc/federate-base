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

		// before creating the federation hook
		beforeFederationCreate();
		// create federation
		createFederation();

		// before joining the federation hook
		beforeFederateJoin();
		// join the federation
		joinFederation();

		// enables time management policy for this federate
		enableTimePolicy();
		string s;
		cin >> s;
		// now we are ready to populate the federation
		synchronize( PointReadyToPopulate );
		// inform RTI about the data we are going publish and subscribe
		publishAndSubscribe();

		// before federate run hook
		beforeReadyToRun();
		// now we are ready to run this federate
		synchronize( PointReadyToRun );
		// just before the first update
		afterReadyToRun();

		while( true )
		{
			if( step( m_federateAmbassador->getFederateTime() ) == false )
				break;
			advanceLogicalTime();
		}

		// before resigning the federation hook
		beforeReadyToResign();
		// now we are ready to resign from this federation
		synchronize( PointReadyToResign );
		// resign from this federation
		resignAndDestroy();
		// after death hook for cleanup
		afterDeath();
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
				m_rtiAmbassadorWrapper->enableTimeRegulated( m_ucefConfig->getLookAhead() );
			}
			catch( UCEFException& )
			{
				throw;
			}

			while( !m_federateAmbassador->isRegulated() )
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

			while( !m_federateAmbassador->isConstrained() )
			{
				tickForCallBacks();
			}
			logger.log( string("RTI acknowledged time policy - constrain"), LevelInfo );
		}
	}

	void FederateBase::publishAndSubscribe()
	{
		Logger& logger = Logger::getInstance();
		// parse the SOM file and build up the HLA object classes
		vector<shared_ptr<ObjectClass>> objectClasses =
			SOMParser::getObjectClasses( ConversionHelper::ws2s(m_ucefConfig->getSomPath()) );

		logger.log( string("Inform RTI about publishing and subscribing classes"), LevelInfo );
		storeObjectClassData( objectClasses );
		pubSubAttributes();

		// parse the SOM file and build up the HLA object classes
		vector<shared_ptr<InteractionClass>> interactionClasses =
			SOMParser::getInteractionClasses( ConversionHelper::ws2s(m_ucefConfig->getSomPath()) );

		logger.log( string("Inform RTI about publishing and subscribing interactions"), LevelInfo );
		storeInteractionClassData( interactionClasses );
		pubSubInteractions();

	}

	void FederateBase::synchronize( SynchPoint point )
	{
		Logger& logger = Logger::getInstance();
		wstring synchPointStr = ConversionHelper::SynchPointToWstring( point );
		// announce synch point
		try
		{
			m_rtiAmbassadorWrapper->announceSynchronizationPoint( synchPointStr );
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
			m_rtiAmbassadorWrapper->achieveSynchronizationPoint( synchPointStr );
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

	void FederateBase::advanceLogicalTime()
	{
		Logger& logger = Logger::getInstance();

		double requestedTime = m_federateAmbassador->getFederateTime() + m_ucefConfig->getTimeStep();
		try
		{
			m_rtiAmbassadorWrapper->advanceLogicalTime( requestedTime );
		}
		catch( UCEFException& )
		{
			throw;
		}

		// wait for the rti grant the requested time advancement
		while( !m_federateAmbassador->isTimeAdvanced() )
		{
			logger.log( "Waiting for the logical time of this federate to advance to " +
			            to_string( requestedTime ), LevelInfo );

			tickForCallBacks();
		}
		logger.log( "The logical time of this federate advanced to " + to_string( requestedTime ), LevelInfo );
		m_federateAmbassador->resetTimeAdvanced();
	}

	void FederateBase::incomingObjectRegistration( long objectInstanceHash, long objectClassHash )
	{
		lock_guard<mutex> lock( m_threadSafeLock );
		Logger& logger = Logger::getInstance();

		shared_ptr<ObjectClass> objectClass = getObjectClassByClassHandle( objectClassHash );
		if( objectClass )
		{
			shared_ptr<HLAObject> hlaObject =
				make_shared<HLAObject>( ConversionHelper::ws2s( objectClass->name ), objectInstanceHash );
			logger.log( "Discovered new object named " + hlaObject->getClassName(), LevelCritical );
			m_objectDataStoreByInstance[objectInstanceHash] = objectClass;
			receiveObjectRegistration( const_pointer_cast<const HLAObject>(hlaObject),
			                           m_federateAmbassador->getFederateTime());

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
			logger.log( "Received attribute update for " + ConversionHelper::ws2s(objectClass->name), LevelCritical );
			shared_ptr<HLAObject> hlaObject =
				make_shared<HLAObject>( ConversionHelper::ws2s( objectClass->name ), objectInstanceHash );

			ObjectClassHandle classHandle = m_rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( !classHandle.isValid() )
			{
				logger.log( "No valid class handle found for the received attribute update of " +
				            ConversionHelper::ws2s(objectClass->name), LevelCritical );
				return;
			}

			for( auto& incomingAttributeValue : attributeValues )
			{
				ObjectAttributes& attributes = objectClass->objectAttributes;
				wstring attName = m_rtiAmbassadorWrapper->getAttributeName(classHandle, incomingAttributeValue.first);
				if( attName == L"" )
				{
					logger.log( "No valid attribute name found for the received attribute with id : " +
				                to_string(incomingAttributeValue.first.hash()), LevelCritical );
					continue;
				}

				size_t size = incomingAttributeValue.second.size();
				const void* data = incomingAttributeValue.second.data();
				shared_ptr<void> arr(new char[size](), [](char *p) { delete [] p; });
				memcpy_s(arr.get(), size, data, size);
				hlaObject->setAttributeValue( ConversionHelper::ws2s(attName), arr, size );
			}
			receiveAttributeReflection( const_pointer_cast<const HLAObject>(hlaObject),
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
		logger.log( "Received interaction update for " +
		            ConversionHelper::ws2s(interactionClass->name), LevelCritical );
		if( interactionClass )
		{
			shared_ptr<HLAInteraction> hlaInteraction =
				make_shared<HLAInteraction>( ConversionHelper::ws2s( interactionClass->name ) );
			InteractionClassHandle interactionHandle =
				m_rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( !interactionHandle.isValid() )
			{
				logger.log( "No valid interaction handle found for the received interaction of " +
				            ConversionHelper::ws2s(interactionClass->name), LevelCritical );
				return;
			}

			for( auto& incomingParameterValue : parameterValues )
			{
				InteractionParameters& parameters = interactionClass->parameters;
				wstring paramName =
					m_rtiAmbassadorWrapper->getParameterName(interactionHandle, incomingParameterValue.first);
				if( paramName == L"" )
				{
					logger.log( "No valid parameter name found for the received parameter with id : " +
				                to_string(incomingParameterValue.first.hash()), LevelCritical );
					continue;
				}

				size_t size = incomingParameterValue.second.size();
				const void* data = incomingParameterValue.second.data();
				shared_ptr<void> arr(new char[size](), [](char *p) { delete [] p; });
				memcpy_s(arr.get(), size, data, size);
				hlaInteraction->setParameterValue( ConversionHelper::ws2s(paramName), arr, size );
			}
			receiveInteraction(const_pointer_cast<const HLAInteraction>(hlaInteraction),
			                   m_federateAmbassador->getFederateTime());
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

		size_t deletedCount = deleteIncomingInstanceHandle( objectInstanceHash );
		if( deletedCount )
		{
			logger.log( "HLAObject with id :" + to_string( objectInstanceHash ) +
			            " successsfully removed from the incoming map.", LevelInfo );
			shared_ptr<HLAObject> hlaObject =
				make_shared<HLAObject>( ConversionHelper::ws2s( objectClass->name ), objectInstanceHash );
			receiveObjectDeletion( const_pointer_cast<const HLAObject>(hlaObject) );
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

	shared_ptr<ObjectClass> FederateBase::getObjectClassByName( string name )
	{
		if( m_objectDataStoreByName.find( name ) != m_objectDataStoreByName.end() )
		{
			return m_objectDataStoreByName[name];
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

	size_t FederateBase::deleteIncomingInstanceHandle( long hash )
	{
		return m_objectDataStoreByInstance.erase( hash );
	}

	std::shared_ptr<util::InteractionClass> FederateBase::getInteractionClass( long hash )
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
		Logger& logger = Logger::getInstance();

		//----------------------------------------------------------
		//            Store object class and attribute handles
		//----------------------------------------------------------

		// try to update object classes with correct class and attribute rti handles
		for( auto& objectClass : objectClasses )
		{
			// store the ObjectClass in m_objectCacheStoreByName for later use
			m_objectDataStoreByName.insert( make_pair(ConversionHelper::ws2s(objectClass->name), objectClass) );

			ObjectClassHandle classHandle = m_rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( classHandle.isValid() )
			{
				// store the ObjectClass in m_objectCacheStoreByHash for later use
				m_objectDataStoreByHash.insert( make_pair( classHandle.hash(), objectClass ) );
			}
		}
	}

	void FederateBase::pubSubAttributes()
	{
		//----------------------------------------------------------
		// Inform RTI about the classes and attributes that
		// are published and subscribed by this federate
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();
		for( auto classPair : m_objectDataStoreByName )
		{
			shared_ptr<ObjectClass> objectClass = classPair.second;

			ObjectClassHandle classHandle = m_rtiAmbassadorWrapper->getClassHandle(objectClass->name);
			if( !classHandle.isValid() )
			{
				continue;
			}
			// attributes we are going to publish
			AttributeHandleSet pubAttributes;
			// attributes we are going to subscribe
			AttributeHandleSet subAttributes;

			ObjectAttributes &objectAtributes = objectClass->objectAttributes;
			for( auto& attributePair : objectAtributes )
			{
				shared_ptr<ObjectAttribute> attribute = attributePair.second;
				AttributeHandle attHandle = m_rtiAmbassadorWrapper->getAttributeHandle(classHandle, attribute->name);
				if( !attHandle.isValid() )
				{
					continue;
				}
				if( attribute->publish )
				{
					logger.log( "Federate publishes attribute " + ConversionHelper::ws2s(attribute->name) +
					            " in " + ConversionHelper::ws2s(objectClass->name), LevelInfo );
					pubAttributes.insert(attHandle);
				}
				if( attribute->subscribe )
				{
					logger.log( "Federate subscribed to attribute " + ConversionHelper::ws2s(attribute->name) +
					            " in " + ConversionHelper::ws2s(objectClass->name), LevelInfo );
					subAttributes.insert(attHandle);
				}
			}

			if( pubAttributes.size() || subAttributes.size())
			{
				m_rtiAmbassadorWrapper->publishSubscribeObjectClassAttributes( classHandle,
				                                                               pubAttributes,
				                                                               subAttributes );
			}
		}
	}

	void FederateBase::storeInteractionClassData( vector<shared_ptr<InteractionClass>>& interactionClasses )
	{
		//----------------------------------------------------------
		//     Store interaction class and parametere handles
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();

		// try to update interaction classes with correct interaction and parameter rti handles
		for( auto& interactionClass : interactionClasses )
		{
			// now store the interactionClass in m_interactionCacheStoreByName for later use
			m_interactionDataStoreByName.insert(
			                  make_pair(ConversionHelper::ws2s(interactionClass->name), interactionClass) );
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

	void FederateBase::pubSubInteractions()
	{
		//----------------------------------------------------------
		// Inform RTI about interaction that are published
		// and subscribed by this federate
		//----------------------------------------------------------

		Logger& logger = Logger::getInstance();
		for( auto& interactionPair : m_interactionDataStoreByName )
		{
			shared_ptr<InteractionClass> interactionClass = interactionPair.second;
			InteractionClassHandle interactionHandle =
				m_rtiAmbassadorWrapper->getInteractionHandle(interactionClass->name);
			if( interactionClass->publish )
			{
				logger.log( "Federate publishes interaction class " +
				            ConversionHelper::ws2s(interactionClass->name), LevelInfo);
			}
			if( interactionClass->subscribe )
			{
				logger.log( "Federate subscribed to Interaction class " +
				            ConversionHelper::ws2s(interactionClass->name), LevelInfo);
			}

			m_rtiAmbassadorWrapper->publishSubscribeInteractionClass( interactionHandle,
			                                                          interactionClass->publish,
			                                                          interactionClass->subscribe);
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
			m_rtiAmbassadorWrapper->tickForCallBacks( 0.1, 1.0 );
		}
	}
}
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
		try
		{
			cacheHandles( objectClasses );
			pubSubAttributes();
		}
		catch( UCEFException& )
		{
			throw;
		}

		// parse the SOM file and build up the HLA object classes
		vector<shared_ptr<InteractionClass>> interactionClasses =
			SOMParser::getInteractionClasses( ConversionHelper::ws2s(m_ucefConfig->getSomPath()) );

		logger.log( string("Inform RTI about publishing and subscribing interactions"), LevelInfo );
		try
		{
			cacheHandles( interactionClasses );
			pubSubInteractions();
		}
		catch( UCEFException& )
		{
			throw;
		}
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

	void FederateBase::receiveObjectRegistration( shared_ptr<HLAObject>& hlaObject, double federateTime )
	{
		lock_guard<mutex> lock( m_threadSafeLock );

		Logger& logger = Logger::getInstance();
		logger.log( "Discovered new object named " + hlaObject->getClassName(), LevelCritical );
		m_incomingStore[hlaObject->getInstanceHandle()->hash()] = hlaObject;
	}

	void FederateBase::receiveAttributeReflection( shared_ptr<const HLAObject>& hlaObject, double federateTime )
	{
		Logger& logger = Logger::getInstance();
		logger.log( "Received attribute update for " + hlaObject->getClassName()
		            + to_string(hlaObject->getAttributeValueAsDouble("Flavor")), LevelCritical );
		logger.log( "Received attribute update for " + hlaObject->getClassName()
		            + to_string(hlaObject->getAttributeValueAsDouble("NumberCups")), LevelCritical );
	}

	void FederateBase::receiveInteraction( shared_ptr<const HLAInteraction>& hlaInteraction,
	                                       double federateTime )
	{

	}

	void FederateBase::objectDelete( std::shared_ptr<HLAObject>& hlaObject )
	{
		lock_guard<mutex> lock( m_threadSafeLock );

		Logger& logger = Logger::getInstance();
		logger.log( "Received object removed notification for HLAObject with id :" +
		             to_string(hlaObject->getInstanceHandle()->hash()), LevelInfo );

		size_t deletedCount =  m_incomingStore.erase( hlaObject->getInstanceHandle()->hash() );

		if( deletedCount )
			logger.log( "HLAObject with id :" + to_string(hlaObject->getInstanceHandle()->hash()) +
			            " successsfully removed from the incoming map.", LevelInfo );
		else
			logger.log( "HLAObject with id :" + to_string(hlaObject->getInstanceHandle()->hash()) +
			            " could not found for deletion.", LevelInfo );
	}
	
	shared_ptr<ObjectClass> FederateBase::getObjectClass( long hash )
	{
		if( m_objectCacheStoreByHash.find( hash ) != m_objectCacheStoreByHash.end() )
		{
			return m_objectCacheStoreByHash[hash];
		}
		return nullptr;
	}

	shared_ptr<ObjectClass> FederateBase::getObjectClass( string name )
	{
		if( m_objectCacheStoreByName.find( name ) != m_objectCacheStoreByName.end() )
		{
			return m_objectCacheStoreByName[name];
		}
		return nullptr;
	}

	shared_ptr<HLAObject> FederateBase::findIncomingObject( long hash )
	{
		lock_guard<mutex> lock( m_threadSafeLock );

		if( m_incomingStore.find( hash ) != m_incomingStore.end() )
		{
			return m_incomingStore[hash];
		}
		return nullptr;
	}

	std::shared_ptr<util::InteractionClass> FederateBase::getInteractionClass( long hash )
	{
		if( m_interactionCacheStoreByHash.find( hash ) != m_interactionCacheStoreByHash.end() )
		{
			return m_interactionCacheStoreByHash[hash];
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
	void FederateBase::cacheHandles( vector<shared_ptr<ObjectClass>>& objectClasses )
	{
		Logger& logger = Logger::getInstance();

		//----------------------------------------------------------
		//            Store object class and attribute handles
		//----------------------------------------------------------

		// try to update object classes with correct class and attribute rti handles
		for( auto& objectClass : objectClasses )
		{
			ObjectClassHandle classHandle = m_rtiAmbassadorWrapper->getClassHandle( objectClass->name );
			if( classHandle.isValid() )
			{
				objectClass->classHandle = make_shared<ObjectClassHandle>( classHandle );
			}
			else
			{
				logger.log( "An invalid class handle returned for " + ConversionHelper::ws2s(objectClass->name),
				            LevelWarn );
			}

			ObjectAttributes& attributes = objectClass->objectAttributes;
			for( auto& attribute : attributes )
			{
				AttributeHandle attributeHandle =
					m_rtiAmbassadorWrapper->getAttributeHandle( classHandle, attribute.second->name );
				if( attributeHandle.isValid() )
				{
					attribute.second->handle = make_shared<AttributeHandle>( attributeHandle );
				}
				else
				{
					logger.log( "An invalid attribute handle returned for " +
					            ConversionHelper::ws2s(objectClass->name) + "." +
					            ConversionHelper::ws2s(attribute.second->name), LevelWarn );
				}
			}

			// now store the ObjectClass in m_objectCacheStoreByName for later use
			m_objectCacheStoreByName.insert( make_pair(ConversionHelper::ws2s(objectClass->name), objectClass) );
			// now store the ObjectClass in m_objectCacheStoreByHash for later use
			m_objectCacheStoreByHash.insert( make_pair(objectClass->classHandle->hash(), objectClass) );
		}
	}

	void FederateBase::pubSubAttributes()
	{
		//----------------------------------------------------------
		// Inform RTI about the classes and attributes that
		// are published and subscribed by this federate
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();
		for( auto classPair : m_objectCacheStoreByName )
		{
			shared_ptr<ObjectClass> objectClass = classPair.second;

			ObjectClassHandle& classHandle = *objectClass->classHandle;
			// attributes we are going to publish
			AttributeHandleSet pubAttributes;
			// attributes we are going to subscribe
			AttributeHandleSet subAttributes;

			ObjectAttributes &objectAtributes = objectClass->objectAttributes;
			for( auto& attributePair : objectAtributes )
			{
				shared_ptr<ObjectAttribute> attribute = attributePair.second;
				if( attribute->publish )
				{
					logger.log( "Federate publishes attribute " + ConversionHelper::ws2s(attribute->name) +
					            " in " + ConversionHelper::ws2s(objectClass->name), LevelInfo );
					pubAttributes.insert(*attribute->handle);
				}
				if( attribute->subscribe )
				{
					logger.log( "Federate subscribed to attribute " + ConversionHelper::ws2s(attribute->name) +
					            " in " + ConversionHelper::ws2s(objectClass->name), LevelInfo );
					subAttributes.insert(*attribute->handle);
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

	void FederateBase::cacheHandles( vector<shared_ptr<InteractionClass>>& interactionClasses )
	{
		//----------------------------------------------------------
		//     Store interaction class and parametere handles
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();

		// try to update interaction classes with correct interaction and parameter rti handles
		for( auto& interactionClass : interactionClasses )
		{
			InteractionClassHandle interactionHandle =
			                     m_rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );
			if( interactionHandle.isValid() )
			{
				interactionClass->interactionHandle = make_shared<InteractionClassHandle>( interactionHandle );
			}
			else
			{
				logger.log( "An invalid interaction handle returned for " +
				            ConversionHelper::ws2s(interactionClass->name), LevelWarn );
			}

			InteractionParameters& params = interactionClass->parameters;
			for( auto& param : params )
			{
				ParameterHandle paramHandle =
					m_rtiAmbassadorWrapper->getParameterHandle( interactionHandle, param.second->name );
				if( paramHandle.isValid() )
				{
					param.second->handle = make_shared<ParameterHandle>( paramHandle );
				}
				else
				{
					logger.log( "An invalid parameter handle returned for " +
					            ConversionHelper::ws2s(interactionClass->name) + "." +
					            ConversionHelper::ws2s(param.second->name), LevelWarn );
				}
			}
			// now store the interactionClass in m_interactionCacheStoreByName for later use
			m_interactionCacheStoreByName.insert(
			                  make_pair(ConversionHelper::ws2s(interactionClass->name), interactionClass) );
			// now store the ObjectClass in m_objectCacheStoreByHash for later use
			m_interactionCacheStoreByHash.insert(
			                  make_pair(interactionClass->interactionHandle->hash(), interactionClass) );
		}
	}

	void FederateBase::pubSubInteractions()
	{
		//----------------------------------------------------------
		// Inform RTI about interaction that are published
		// and subscribed by this federate
		//----------------------------------------------------------

		Logger& logger = Logger::getInstance();
		for( auto& interactionPair : m_interactionCacheStoreByName )
		{
			shared_ptr<InteractionClass> interactionClass = interactionPair.second;

			InteractionClassHandle& interactionHandle = *interactionClass->interactionHandle;
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

			m_rtiAmbassadorWrapper->publishSubscribeInteractionClassParams( interactionHandle,
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
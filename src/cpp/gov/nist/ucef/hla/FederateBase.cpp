#include "FederateBase.h"

#include <thread>
#include <chrono>

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
	                               m_federateAmbassador( make_shared<FederateAmbassador>() ),
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
			if( step() == false )
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
		wstring synchPointStr = ConversionHelper::s2ws( ConversionHelper::SynchPointToString(point) );
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

	void FederateBase::updateObject( std::shared_ptr<HLAObject>& object )
	{
		Logger::getInstance().log( "updating object " + object->getClassName(), LevelInfo );
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
				objectClass->classHandle.reset( new ObjectClassHandle(classHandle) );
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
					attribute.second->handle.reset( new AttributeHandle(attributeHandle) );
				}
				else
				{
					logger.log( "An invalid attribute handle returned for " +
					            ConversionHelper::ws2s(objectClass->name) + "." +
					            ConversionHelper::ws2s(attribute.second->name), LevelWarn );
				}
			}

			// now store the ObjectClass in objectClassMap for later use
			m_objectClassMap.insert( make_pair(ConversionHelper::ws2s(objectClass->name), objectClass) );
		}
	}

	void FederateBase::pubSubAttributes()
	{
		//----------------------------------------------------------
		// Inform RTI about the classes and attributes that
		// are published and subscribed by this federate
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();
		for( auto classPair : m_objectClassMap )
		{
			shared_ptr<ObjectClass> objectClass = classPair.second;

			ObjectClassHandle classHandle = *objectClass->classHandle;
			// attributes we are going to publish
			AttributeHandleSet pubAttributes;
			// attributes we are going to subscribe
			AttributeHandleSet subAttributes;

			ObjectAttributes &objectAtributes = objectClass->objectAttributes;
			for( auto attributePair : objectAtributes )
			{
				shared_ptr<ObjectAttribute> attribute = attributePair.second;
				if( attribute->publish )
				{
					logger.log( ConversionHelper::ws2s(attribute->name) + " added for publishing.", LevelInfo );
					pubAttributes.insert(*attribute->handle);
				}
				else if( attribute->subscribe )
				{
					logger.log( ConversionHelper::ws2s(attribute->name) + " added for subscribing.", LevelInfo );
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
		//TO-DO
	}

	void FederateBase::pubSubInteractions()
	{
		//----------------------------------------------------------
		// Inform RTI about interaction that are published
		// and subscribed by this federate
		//----------------------------------------------------------
		//TO-DO
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
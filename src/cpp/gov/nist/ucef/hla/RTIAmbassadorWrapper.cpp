#include "RTIAmbassadorWrapper.h"

#include <thread>
#include <chrono>

#include "FederateAmbassador.h"

#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/SOMParser.h"
#include "gov/nist/ucef/util/FederateConfiguration.h"

#include "RTI/Handle.h"
#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	RTIAmbassadorWrapper::RTIAmbassadorWrapper() : m_ucefConfig( new FederateConfiguration() ),
	                                               m_federateAmbassador( nullptr ),
	                                               m_rtiAmbassador( nullptr )
	{

	}

	RTIAmbassadorWrapper::~RTIAmbassadorWrapper()
	{

	}

	void RTIAmbassadorWrapper::createRtiAmbassador()
	{
		//----------------------------------------
		//            Create Federate Ambassador
		//-----------------------------------------
		m_federateAmbassador = make_shared<FederateAmbassador>();

		//----------------------------------------
		//            Create RTI Ambassador
		//-----------------------------------------
		RTIambassador* tmpAmbassador = RTIambassadorFactory().createRTIambassador().release();
		m_rtiAmbassador.reset(tmpAmbassador);

		//----------------------------------------
		//            Connect to the RTI
		//-----------------------------------------
		Logger& logger = Logger::getInstance();
		logger.log( ConversionHelper::ws2s(m_ucefConfig->getFederateName()) + " trying to connect to RTI.", LevelInfo );
		try
		{
			CallbackModel callBackModel = m_ucefConfig->isImmediate() ? HLA_IMMEDIATE : HLA_EVOKED;
			m_rtiAmbassador->connect (*m_federateAmbassador, callBackModel );
			logger.log( ConversionHelper::ws2s(m_ucefConfig->getFederateName()) +
			            " Successfully connected to the RTI.", LevelInfo );
		}
		catch( ConnectionFailed& connectionFailed )
		{
			logger.log( "Connection failed " + ConversionHelper::ws2s(connectionFailed.what()), LevelError );
		}
		catch( InvalidLocalSettingsDesignator& settings )
		{
			logger.log( "Connection failed, InvalidLocalSettingsDesignator: " +
			            ConversionHelper::ws2s(settings.what()), LevelError );
		}
		catch( UnsupportedCallbackModel& callbackModel )
		{
			logger.log( "Connection failed, UnsupportedCallbackModel: " +
			            ConversionHelper::ws2s(callbackModel.what()), LevelError );
		}
		catch( AlreadyConnected& connected )
		{
			logger.log( "Connection failed, AlreadyConnected: " +
			            ConversionHelper::ws2s(connected.what()), LevelError );
		}
		catch( RTIinternalError& error )
		{
			logger.log( "Connection failed, Generic Error: " +
			            ConversionHelper::ws2s(error.what()), LevelError );
		}
	}

	void RTIAmbassadorWrapper::createFederation()
	{
		Logger& logger = Logger::getInstance();
		try
		{
			m_rtiAmbassador->createFederationExecution( m_ucefConfig->getFederationName(),
			                                            m_ucefConfig->getFomPaths() );
			logger.log( string("Federation Created."), LevelInfo );
		}
		catch( FederationExecutionAlreadyExists& )
		{
			logger.log( string("Federation creation failed, federation already exist."), LevelWarn );
		}
		catch( Exception& e )
		{
			logger.log( "Generic Error: " + ConversionHelper::ws2s(e.what()),  LevelError );
		}
	}

	void RTIAmbassadorWrapper::joinFederation()
	{
		Logger& logger = Logger::getInstance();
		try
		{
			m_rtiAmbassador->joinFederationExecution( m_ucefConfig->getFederateName(),
			                                          m_ucefConfig->getFederateType(),
			                                          m_ucefConfig->getFederationName() );

			logger.log( ConversionHelper::ws2s(m_ucefConfig->getFederateName()) + " joined the federation " +
			            ConversionHelper::ws2s(m_ucefConfig->getFederationName()) + ".", LevelInfo );
		}
		catch( Exception& e )
		{
			logger.log( "Could not join the federation : " + ConversionHelper::ws2s(m_ucefConfig->getFederationName()) +
			            "Error: " + ConversionHelper::ws2s(e.what()), LevelError );
		}
	}

	void RTIAmbassadorWrapper::synchronize( SynchPoint point )
	{
		announceSynchronizationPoint( point );
		achieveSynchronizationPoint( point );
	}

	inline void RTIAmbassadorWrapper::enableTimePolicy()
	{
		if( m_ucefConfig->isTimeRegulated() )
		{
			enableTimeRegulated();
		}

		if( m_ucefConfig->isTimeConstrained() )
		{
			enableTimeConstrained();
		}
	}

	void RTIAmbassadorWrapper::publishAndSubscribe()
	{
		initialiseHandles();
		Logger& logger = Logger::getInstance();
		logger.log( string("Inform RTI about publishing and subscribing attributes") , LevelInfo );
		publishSubscribeObjectClassAttributes();
		logger.log( string("Inform RTI about publishing  and subscribing interactions") , LevelInfo );
		publishSubscribeInteractionClasses();
	}

	void RTIAmbassadorWrapper::announceSynchronizationPoint( SynchPoint point )
	{
		Logger& logger = Logger::getInstance();
		VariableLengthData tag( (void*)"", 1 );
		wstring synchPointStr = ConversionHelper::s2ws( ConversionHelper::SynchPointToString(point) );
		m_rtiAmbassador->registerFederationSynchronizationPoint( synchPointStr, tag );

		// wait for the rti to acknowledge the synch point
		while( true )
		{
			tick();

			if( m_federateAmbassador->isAnnounced(synchPointStr) )
			{
				logger.log( "Successfully announced the synchronization Point " +
				            ConversionHelper::SynchPointToString(point), LevelInfo );
				break;
			}
			else
			{
				logger.log( "Waiting for the announcement of synchronization Point " +
				            ConversionHelper::SynchPointToString(point), LevelInfo );
			}
		}
	}

	void RTIAmbassadorWrapper::achieveSynchronizationPoint( SynchPoint point )
	{
		Logger& logger = Logger::getInstance();
		wstring synchPointStr = ConversionHelper::s2ws( ConversionHelper::SynchPointToString(point) );
		// achieve the given synchronization point
		m_rtiAmbassador->synchronizationPointAchieved( synchPointStr );
		logger.log( "Federate achieved synchronization Point " +
		            ConversionHelper::SynchPointToString(point), LevelInfo );

		// wait for the federation to achieve the given synchronization point
		while( true )
		{
			tick();

			if( m_federateAmbassador->isAchieved(synchPointStr) )
			{
				logger.log( "Federation achieved synchronization Point  " +
				            ConversionHelper::SynchPointToString(point), LevelInfo );
				break;
			}
			else
			{
				logger.log( "Waiting for the federation to synchronise to " +
				            ConversionHelper::SynchPointToString(point), LevelInfo );
			}
		}
	}

	void RTIAmbassadorWrapper::enableTimeRegulated()
	{
		HLAfloat64Interval lookAheadInterval( m_ucefConfig->getLookAhead() );

		Logger& logger = Logger::getInstance();

		logger.log( string("Inform time policy - regulated to RTI."), LevelInfo ) ;
		m_rtiAmbassador->enableTimeRegulation( lookAheadInterval );

		// inform time policy to RTI
		while( true )
		{
			tick();
			
			if( m_ucefConfig->isTimeRegulated() )
			{
				logger.log( string("RTI acknowledged time policy - regulated of this federate."), LevelInfo );
				break;
			}
		}
	}

	void RTIAmbassadorWrapper::enableTimeConstrained()
	{
		Logger& logger = Logger::getInstance();

		logger.log( string("Inform time policy - constrain to RTI."), LevelInfo ) ;
		m_rtiAmbassador->enableTimeConstrained();

		// inform time policy to RTI
		while( true )
		{
			tick();

			if( m_ucefConfig->isTimeConstrained() )
			{
				logger.log( string("RTI acknowledged time policy - constrain of this federate."), LevelInfo );
				break;
			}
		}
	}

	void RTIAmbassadorWrapper::initialiseHandles()
	{
		//----------------------------------------------------------
		//            Store object class handlers
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();

		// parse the SOM file and build up the HLA object classes
		vector<shared_ptr<ObjectClass>> objectClasses = SOMParser::getObjectClasses( m_ucefConfig->getSomPath() );

		// try to update object classes with correct class and attribute rti handlers
		for( auto& objectClass : objectClasses )
		{			
			rti1516e::ObjectClassHandle classHandle =
					m_rtiAmbassador->getObjectClassHandle( objectClass->name );
			if( classHandle.isValid() )
			{
				objectClass->handle.reset(new rti1516e::ObjectClassHandle(classHandle));
			}
			else
			{
				logger.log( "An invalid class handler returned for " + ConversionHelper::ws2s(objectClass->name),
				            LevelWarn );
			}

			ObjectAttributes& attributes = objectClass->objectAttributes;
			for( auto& attribute : attributes )
			{
				rti1516e::AttributeHandle attributeHandle =
						m_rtiAmbassador->getAttributeHandle( classHandle, attribute.second->name );
				if( attributeHandle.isValid() )
				{
					attribute.second->handle.reset(new rti1516e::AttributeHandle(attributeHandle));
				}
				else
				{
					logger.log( "An invalid attribute handler returned for " +
					            ConversionHelper::ws2s(objectClass->name) + "." +
					            ConversionHelper::ws2s(attribute.second->name), LevelWarn );
				}
			}

			// now store the ObjectClass in objectClassMap for later use
			objectClassMap.insert( make_pair(ConversionHelper::ws2s(objectClass->name), objectClass) );
		}

		//----------------------------------------------------------
		//            Store interaction class handlers
		//----------------------------------------------------------
		vector<shared_ptr<InteractionClass>> interactionClasses =
			SOMParser::getInteractionClasses( m_ucefConfig->getSomPath() );
		for( const auto& interactionClass : interactionClasses )
		{
			InteractionClassHandle interactionHandle =
					m_rtiAmbassador->getInteractionClassHandle( interactionClass->name );

			list<std::shared_ptr<InteractionParameter>> &parameters = interactionClass->parameters;
			for( const shared_ptr<InteractionParameter> &parameter : parameters )
			{
				ParameterHandle parameterHandle =
						m_rtiAmbassador->getParameterHandle( interactionHandle, parameter->name );
			}
		}
	}

	void RTIAmbassadorWrapper::publishSubscribeObjectClassAttributes()
	{
		Logger& logger = Logger::getInstance();
		for( auto classPair : objectClassMap )
		{
			shared_ptr<ObjectClass> objectClass = classPair.second;

			ObjectClassHandle classHandle = *objectClass->handle;
			// attributes we are going to publish
			AttributeHandleSet pubAttributes;
			// attributes we are going to subscribe
			AttributeHandleSet subAttributes;

			ObjectAttributes &objectAtributes = objectClass->objectAttributes;
			for( auto attributePair : objectAtributes )
			{
				shared_ptr<ObjectAttribute> attribute = attributePair.second;
				if( attribute->sharingState == StatePublish )
				{
					logger.log( ConversionHelper::ws2s(attribute->name) + " added for publishing.", LevelInfo );
					pubAttributes.insert(*attribute->handle);
				}
				else if( attribute->sharingState == StateSubscribe)
				{
					logger.log( ConversionHelper::ws2s(attribute->name) + " added for subscribing.", LevelInfo );
					subAttributes.insert(*attribute->handle);
				}
				else if( attribute->sharingState == StatePubSub )
				{
					logger.log( ConversionHelper::ws2s(attribute->name) +
					            " added for both publishing and subscribing.", LevelInfo );
					pubAttributes.insert(*attribute->handle);
					subAttributes.insert(*attribute->handle);
				}
			}

			if( pubAttributes.size() )
			{
				m_rtiAmbassador->publishObjectClassAttributes( classHandle, pubAttributes );
			}
			if( subAttributes.size() )
			{
				m_rtiAmbassador->subscribeObjectClassAttributes( classHandle, subAttributes );
			}
		}
	}

	void RTIAmbassadorWrapper::publishSubscribeInteractionClasses()
	{
		// interactions we are going to send out
		//InteractionClassHandle interactionClassHandle;
		//m_rtiAmbassador->publishInteractionClass( interactionClassHandle );
	}

	void RTIAmbassadorWrapper::tick()
	{
		if( m_ucefConfig->isImmediate() )
		{
			this_thread::sleep_for( chrono::microseconds( 10 ) );
		}
		else
		{
			m_rtiAmbassador->evokeMultipleCallbacks( 0.1, 1.0 );
		}
	}
}
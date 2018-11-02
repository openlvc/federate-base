#include "FederateBase.h"

#include <thread>
#include <chrono>

#include "FederateAmbassador.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/SOMParser.h"
#include "gov/nist/ucef/util/FederateConfiguration.h"
#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	FederateBase::FederateBase() : m_ucefConfig( new FederateConfiguration() ),
	                               m_federateAmbassador( nullptr ),
	                               m_rtiAmbassador( nullptr )
	{

	}

	FederateBase::~FederateBase()
	{

	}

	void FederateBase::runFederate()
	{
		// initialise rti ambassador
		createRtiAmbassador();

		// before create federation hook
		beforeFederationCreate();
		// create federation
		createFederation();

		// before federation join hook
		beforeFederationJoin();
		// federation join
		joinFederation();

		// cache object, attribute, interaction, and parameter handles
		initialiseHandles();

		// enables time management policy for this federate
		enableTimePolicy();

		// now we are ready to run the federate
		synchronize( PointReadyToRun );
	}

	void FederateBase::createRtiAmbassador()
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
		Logger &logger = Logger::getInstance();
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

	void FederateBase::createFederation()
	{
		Logger &logger = Logger::getInstance();
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

	void FederateBase::joinFederation()
	{
		Logger &logger = Logger::getInstance();
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

	void FederateBase::initialiseHandles()
	{
		//----------------------------------------------------------
		//            Store object class handlers
		//----------------------------------------------------------
		Logger &logger = Logger::getInstance();

		// parse the SOM file and build up the HLA object classes
		vector<shared_ptr<ObjectClass>> objectClasses = SOMParser::getObjectClasses( m_ucefConfig->getSomPath() );

		// try to update object classes with correct class and attribute rti handlers
		for( auto& objectClass : objectClasses )
		{			
			ObjectClassHandle classHandle =
					m_rtiAmbassador->getObjectClassHandle( objectClass->name );
			if( classHandle.isValid() )
			{
				objectClass->handle = classHandle;
			}
			else
			{
				logger.log( "An invalid class handler returned for " + ConversionHelper::ws2s(objectClass->name),
				            LevelWarn );
			}

			ObjectAttributes& attributes = objectClass->objectAttributes;
			for( auto& attribute : attributes )
			{
				AttributeHandle attributeHandle =
						m_rtiAmbassador->getAttributeHandle( classHandle, attribute.second->name );
				if( attributeHandle.isValid() )
				{
					attribute.second->handle = attributeHandle;
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

	void FederateBase::synchronize( SynchPoint point )
	{
		announceSynchronizationPoint( point );
		achieveSynchronizationPoint( point );
	}

	inline void FederateBase::enableTimePolicy()
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

	//----------------------------------------------------------
	//             Instance Methods
	//----------------------------------------------------------
	void FederateBase::announceSynchronizationPoint( SynchPoint point )
	{
		Logger &logger = Logger::getInstance();
		VariableLengthData tag( (void*)"", 1 );
		wstring synchPointStr = ConversionHelper::s2ws( ConversionHelper::SynchPointToString(point) );
		m_rtiAmbassador->registerFederationSynchronizationPoint( synchPointStr, tag );
		while( true )
		{
			if( m_federateAmbassador->isAnnouncedSynchPoint(synchPointStr) )
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

			if( m_ucefConfig->isImmediate() )
			{
				std::this_thread::sleep_for( std::chrono::microseconds(10) );
			}
			else
			{
				m_rtiAmbassador->evokeMultipleCallbacks( 0.1, 1.0 );
			}
		}
	}

	void FederateBase::achieveSynchronizationPoint( SynchPoint point )
	{
		Logger &logger = Logger::getInstance();
		wstring synchPointStr = ConversionHelper::s2ws( ConversionHelper::SynchPointToString(point) );
		// achieve the given synchronization point
		m_rtiAmbassador->synchronizationPointAchieved( synchPointStr );
		logger.log( "Federate achieved synchronization Point " +
		            ConversionHelper::SynchPointToString(point), LevelInfo );

		// wait for the federation to achieve the given synchronization point
		while( true )
		{
			if( m_federateAmbassador->isAchievedSynchPoint(synchPointStr) )
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

			if( m_ucefConfig->isImmediate() )
			{
				std::this_thread::sleep_for( std::chrono::microseconds(10) );
			}
			else
			{
				m_rtiAmbassador->evokeMultipleCallbacks( 0.1, 1.0 );
			}
		}
	}

	void FederateBase::enableTimeRegulated()
	{
		HLAfloat64Interval lookAheadInterval( m_ucefConfig->getLookAhead() );

		Logger &logger = Logger::getInstance();

		logger.log( string("Requesting to enable time regulation for this federate."), LevelInfo ) ;
		m_rtiAmbassador->enableTimeRegulation( lookAheadInterval );

		// wait till LRC enable time regulation for this federate
		while( true )
		{
			if( m_ucefConfig->isTimeRegulated() )
			{
				break;
			}

			if( m_ucefConfig->isImmediate() )
			{
				std::this_thread::sleep_for( std::chrono::microseconds( 10 ) );
			}
			else
			{
				m_rtiAmbassador->evokeMultipleCallbacks( 0.1, 1.0 );
			}
		}
		logger.log( string("Time regulation enabled."), LevelInfo ) ;
	}

	void FederateBase::enableTimeConstrained()
	{
		Logger &logger = Logger::getInstance();

		logger.log( string("Requesting to enable time constraining for this federate."), LevelInfo ) ;
		m_rtiAmbassador->enableTimeConstrained();

		// wait till LRC enable time constrain for this federate
		while( true )
		{
			if( m_ucefConfig->isTimeConstrained() )
			{
				break;
			}

			if( m_ucefConfig->isImmediate() )
			{
				std::this_thread::sleep_for( std::chrono::microseconds( 10 ) );
			}
			else
			{
				m_rtiAmbassador->evokeMultipleCallbacks( 0.1, 1.0 );
			}
		}
		logger.log( string("Time constrain enabled."), LevelInfo ) ;
	}
}
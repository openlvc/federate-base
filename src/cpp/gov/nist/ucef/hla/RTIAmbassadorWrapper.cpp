#include "RTIAmbassadorWrapper.h"

#include <thread>
#include <chrono>

#include "gov/nist/ucef/hla/FederateAmbassador.h"
#include "gov/nist/ucef/util/FederateConfiguration.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/UCEFException.h"

#include "RTI/Handle.h"
#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"
#include "RTI/time/HLAfloat64Interval.h"
#include "RTI/time/HLAfloat64Time.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	RTIAmbassadorWrapper::RTIAmbassadorWrapper()
	{
		RTIambassador* tmpAmbassador = RTIambassadorFactory().createRTIambassador().release();
		m_rtiAmbassador.reset(tmpAmbassador);
	}

	RTIAmbassadorWrapper::~RTIAmbassadorWrapper()
	{

	}

	void RTIAmbassadorWrapper::connect( shared_ptr<FederateAmbassador>& federateAmbassador,
	                                    const shared_ptr<FederateConfiguration>& config )
	{

		//----------------------------------------
		//            Connect to the RTI
		//-----------------------------------------
		try
		{
			CallbackModel callBackModel = config->isImmediate() ? HLA_IMMEDIATE : HLA_EVOKED;
			m_rtiAmbassador->connect( *federateAmbassador, callBackModel );
		}
		catch( AlreadyConnected& )
		{
			Logger::getInstance().log( config->getFederateName() + " Already connected to the federation " +
			                           config->getFederationName(), LevelWarn );
		}
		catch( Exception& ex )
		{
			throw UCEFException( "Failed to connect to " + config->getFederationName() );
		}
	}

	void RTIAmbassadorWrapper::createFederation( const shared_ptr<FederateConfiguration>& config )
	{
		Logger& logger = Logger::getInstance();
		try
		{
			m_rtiAmbassador->createFederationExecution( ConversionHelper::s2ws(config->getFederationName()),
			                                            config->getFomPaths() );

		}
		catch( FederationExecutionAlreadyExists& )
		{
			logger.log( string("Federation creation failed, federation "
			            + config->getFederationName() + " already exist."), LevelWarn );
		}
		catch( Exception& e )
		{
			throw UCEFException( "Failed to create federation " + config->getFederationName() );
		}
	}

	void RTIAmbassadorWrapper::joinFederation( const shared_ptr<FederateConfiguration>& config )
	{
		try
		{
			m_rtiAmbassador->joinFederationExecution( ConversionHelper::s2ws(config->getFederateName()),
			                                          ConversionHelper::s2ws(config->getFederateType()),
			                                          ConversionHelper::s2ws(config->getFederationName()) );
		}
		catch( Exception& e )
		{
			throw UCEFException( "Could not join the federation : " + config->getFederationName() );
		}
	}

	void RTIAmbassadorWrapper::enableTimeRegulated( const shared_ptr<FederateConfiguration>& config )
	{
		HLAfloat64Interval lookAheadInterval( config->getLookAhead() );
		try
		{
			m_rtiAmbassador->enableTimeRegulation( lookAheadInterval );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::enableTimeConstrained( const shared_ptr<FederateConfiguration>& config )
	{
		try
		{
			m_rtiAmbassador->enableTimeConstrained();
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::synchronize( SynchPoint point )
	{
		announceSynchronizationPoint( point );
		achieveSynchronizationPoint( point );
	}

	void RTIAmbassadorWrapper::publishAndSubscribe()
	{
		initialiseClassHandles();
		Logger& logger = Logger::getInstance();
		logger.log( string("Inform RTI about publishing and subscribing attributes") , LevelInfo );
		publishSubscribeObjectClassAttributes();
		logger.log( string("Inform RTI about publishing  and subscribing interactions") , LevelInfo );
		publishSubscribeInteractionClasses();
		initialiseInstanceHandles();
	}

	inline void RTIAmbassadorWrapper::resign()
	{
		Logger& logger = Logger::getInstance();
		//----------------------------------------------------------
		//            delete object instance handles
		//----------------------------------------------------------
		logger.log( string("Federate ") + ConversionHelper::ws2s(m_ucefConfig->getFederateName())
		            + " resigning from federation " + ConversionHelper::ws2s(m_ucefConfig->getFederationName()),
		            LevelInfo );
		for( auto& classPair : objectClassMap )
		{
			shared_ptr<ObjectClass> objectClass = classPair.second;
			ObjectInstanceHandle instanceHandle = *objectClass->instanceHandle;
			if( objectClass->hasAttrToPubOrSub )
			{
				VariableLengthData tag( (void*)"", 1 );
				m_rtiAmbassador->deleteObjectInstance( instanceHandle, tag );
			}
		}
		m_rtiAmbassador->resignFederationExecution( NO_ACTION );
		logger.log( string("Federate ") + ConversionHelper::ws2s(m_ucefConfig->getFederateName())
		            + " resigned from federation " + ConversionHelper::ws2s(m_ucefConfig->getFederationName()),
		            LevelInfo );
	}

	inline void RTIAmbassadorWrapper::advanceLogicalTime()
	{
		Logger& logger = Logger::getInstance();

		double requestedTime = m_federateAmbassador->getFederateTime() + m_ucefConfig->getTimeStep();
		unique_ptr<HLAfloat64Time> newTime( new HLAfloat64Time(requestedTime) );
		try
		{
			m_rtiAmbassador->timeAdvanceRequest( *newTime );

			// wait for the rti grant the requested time advancement
			while( true )
			{
				tick();

				if( m_federateAmbassador->isTimeAdvanced() )
				{
					logger.log( "The logical time of this federate advanced to " +
					            to_string( requestedTime ), LevelInfo );
					m_federateAmbassador->resetTimeAdvanced();
					break;
				}
				else
				{
					logger.log( "Waiting for the logical time of this federate to advance to " +
					            to_string( requestedTime ), LevelInfo );
				}
			}
		}
		catch( Exception& e )
		{
			logger.log( "Generic Error: " + ConversionHelper::ws2s(e.what()),  LevelError );
		}
	}

	void RTIAmbassadorWrapper::tickForCallBacks( double min, double max )
	{
		m_rtiAmbassador->evokeMultipleCallbacks( min, max );
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
				logger.log( "Federation achieved synchronization Point " +
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



	void RTIAmbassadorWrapper::initialiseClassHandles()
	{
		//----------------------------------------------------------
		//            Store object class handles
		//----------------------------------------------------------
		Logger& logger = Logger::getInstance();

		// parse the SOM file and build up the HLA object classes
		vector<shared_ptr<ObjectClass>> objectClasses = SOMParser::getObjectClasses( m_ucefConfig->getSomPath() );

		// try to update object classes with correct class and attribute rti handles
		for( auto& objectClass : objectClasses )
		{
			ObjectClassHandle classHandle = m_rtiAmbassador->getObjectClassHandle( objectClass->name );
			if( classHandle.isValid() )
			{
				objectClass->classHandle.reset( new ObjectClassHandle(classHandle) );
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
					attribute.second->handle.reset( new AttributeHandle(attributeHandle) );
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
		//            Store interaction class handles
		//----------------------------------------------------------
		vector<shared_ptr<InteractionClass>> interactionClasses =
			SOMParser::getInteractionClasses( m_ucefConfig->getSomPath() );
		for( const auto& interactionClass : interactionClasses )
		{
			InteractionClassHandle interactionHandle =
					m_rtiAmbassador->getInteractionClassHandle( interactionClass->name );

			InteractionParameters &parameters = interactionClass->parameters;
			for( auto& parameter : parameters )
			{
				ParameterHandle parameterHandle =
						m_rtiAmbassador->getParameterHandle( interactionHandle, parameter.second->name );
			}
		}
	}

	inline void RTIAmbassadorWrapper::initialiseInstanceHandles()
	{
		//----------------------------------------------------------
		//            Store object instance handles
		//----------------------------------------------------------
		for( auto& classPair : objectClassMap )
		{
			shared_ptr<ObjectClass> objectClass = classPair.second;
			ObjectClassHandle classHandle = *objectClass->classHandle;
			ObjectInstanceHandle instanceHandle = m_rtiAmbassador->registerObjectInstance(classHandle);
			if( instanceHandle.isValid() && objectClass->hasAttrToPubOrSub )
			{
				objectClass->instanceHandle.reset( new ObjectInstanceHandle(instanceHandle) );
			}
		}
	}

	void RTIAmbassadorWrapper::publishSubscribeObjectClassAttributes()
	{
		Logger& logger = Logger::getInstance();
		for( auto classPair : objectClassMap )
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
				objectClass->hasAttrToPubOrSub = true;
				m_rtiAmbassador->publishObjectClassAttributes( classHandle, pubAttributes );
			}
			if( subAttributes.size() )
			{
				objectClass->hasAttrToPubOrSub = true;
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
}
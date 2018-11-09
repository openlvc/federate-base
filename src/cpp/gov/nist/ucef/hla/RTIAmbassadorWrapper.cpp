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
#include "RTI/Typedefs.h"

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
		catch( Exception& )
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
		catch( Exception& )
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
		catch( Exception& )
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

	ObjectClassHandle RTIAmbassadorWrapper::getClassHandle( wstring& name )
	{
		try
		{
			return m_rtiAmbassador->getObjectClassHandle( name );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	AttributeHandle RTIAmbassadorWrapper::getAttributeHandle( ObjectClassHandle& classHandle, wstring& name )
	{
		try
		{
			return m_rtiAmbassador->getAttributeHandle( classHandle, name );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::publishSubscribeObjectClassAttributes( ObjectClassHandle & classHandle,
	                                                                  set<AttributeHandle>& pubAttributes,
	                                                                  set<AttributeHandle>& subAttributes )
	{
		if( pubAttributes.size() )
		{
			try
			{
				m_rtiAmbassador->publishObjectClassAttributes( classHandle, pubAttributes );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
		if( subAttributes.size() )
		{
			try
			{
				m_rtiAmbassador->subscribeObjectClassAttributes( classHandle, subAttributes );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
	}

	std::shared_ptr<HLAObject> RTIAmbassadorWrapper::registerObject( const string& className,
	                                                                 ObjectClassHandle& classHandle)
	{
		shared_ptr<HLAObject> hlaObject = nullptr;
		try
		{
			ObjectInstanceHandle instanceHandle = m_rtiAmbassador->registerObjectInstance( classHandle );
			shared_ptr<ObjectInstanceHandle> inst;
			inst.reset( new ObjectInstanceHandle(instanceHandle) );
			hlaObject = make_shared<HLAObject>( className, inst );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
		return hlaObject;
	}

	InteractionClassHandle RTIAmbassadorWrapper::getInteractionHandle( wstring& name )
	{
		try
		{
			return m_rtiAmbassador->getInteractionClassHandle( name );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	ParameterHandle RTIAmbassadorWrapper::getParameterHandle( InteractionClassHandle& interactionHandle,
	                                                          wstring& name )
	{
		try
		{
			return m_rtiAmbassador->getParameterHandle( interactionHandle, name );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::publishSubscribeInteractionClass( InteractionClassHandle& interactionHandle,
	                                                             bool toPublish,
	                                                             bool toSubscribe )
	{
		if( toPublish )
		{
			try
			{
				m_rtiAmbassador->publishInteractionClass( interactionHandle );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
		if( toSubscribe )
		{
			try
			{
				m_rtiAmbassador->subscribeInteractionClass( interactionHandle );
			}
			catch( Exception& e )
			{
				throw UCEFException( ConversionHelper::ws2s(e.what()) );
			}
		}
	}

	void RTIAmbassadorWrapper::announceSynchronizationPoint( wstring& synchPoint )
	{
		try
		{
			VariableLengthData tag( (void*)"", 1 );
			m_rtiAmbassador->registerFederationSynchronizationPoint( synchPoint, tag );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::achieveSynchronizationPoint( wstring& synchPoint )
	{
		try
		{
			m_rtiAmbassador->synchronizationPointAchieved( synchPoint );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::advanceLogicalTime( double requestedTime )
	{
		unique_ptr<HLAfloat64Time> newTime( new HLAfloat64Time(requestedTime) );
		try
		{
			m_rtiAmbassador->timeAdvanceRequest( *newTime );
		}
		catch( InTimeAdvancingState& )
		{
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::deleteObjectInstances( std::shared_ptr<HLAObject>& hlaObject )
	{
		try
		{
			VariableLengthData tag( (void*)"", 1 );
			m_rtiAmbassador->deleteObjectInstance( *hlaObject->getInstanceHandle(), tag );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::resign()
	{
		try
		{
			m_rtiAmbassador->resignFederationExecution( NO_ACTION );
		}
		catch( Exception& e )
		{
			throw UCEFException( ConversionHelper::ws2s(e.what()) );
		}
	}

	void RTIAmbassadorWrapper::tickForCallBacks( double min, double max )
	{
		m_rtiAmbassador->evokeMultipleCallbacks( min, max );
	}
}
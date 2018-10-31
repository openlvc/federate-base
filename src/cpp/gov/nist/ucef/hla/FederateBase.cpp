#include "FederateBase.h"

#include "FederateAmbassador.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/SOMParser.h"
#include "gov/nist/ucef/util/UCEFConfig.h"
#include "RTI/RTIambassador.h"
#include "RTI/RTIambassadorFactory.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	FederateBase::FederateBase( wstring& federateName ) : m_ucefConfig(new UCEFConfig()),
	                                                      m_federateAmbassador(nullptr),
	                                                      m_rtiAmbassador(nullptr)

	{
		
		initialiseRti();
		initialiseFederation();
		initialiseHandles();
	}

	FederateBase::~FederateBase()
	{

	}

	void FederateBase::initialiseRti()
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
			m_rtiAmbassador->connect (*m_federateAmbassador, HLA_IMMEDIATE );
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

	void FederateBase::initialiseFederation()
	{
		//----------------------------------------
		//            Create Federation
		//-----------------------------------------
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

		//----------------------------------------
		//            Join Federation
		//-----------------------------------------

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
		string name = "RestaurantSOMmodule.xml";
		string path = "restaurant/";

		//----------------------------------------------------------
		//            Store object class handlers
		//----------------------------------------------------------
		vector<shared_ptr<ObjectClass>> objectClasses = SOMParser::getObjectClasses( path, name );

		for( const shared_ptr<ObjectClass> &objectClass : objectClasses )
		{
			ObjectClassHandle classHandle =
					m_rtiAmbassador->getObjectClassHandle( objectClass->name );

			list<std::shared_ptr<ObjectAttribute>> &attributes = objectClass->attributes;
			for( const shared_ptr<ObjectAttribute> &attribute : attributes )
			{
				AttributeHandle attributeHandle =
						m_rtiAmbassador->getAttributeHandle( classHandle, attribute->name );
			}
		}

		//----------------------------------------------------------
		//            Store interaction class handlers
		//----------------------------------------------------------
		vector<shared_ptr<InteractionClass>> interactionClasses = SOMParser::getInteractionClasses( name, path );
		for( const shared_ptr<InteractionClass> &interactionClass : interactionClasses )
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
}


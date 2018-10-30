#include "FederateBase.h"

#include <iostream>

#include "RTI/RTIambassadorFactory.h"

#include "gov/nist/ucef/util/SOMParser.h"

using namespace rti1516e;
using namespace std;
using namespace ucef::util;

namespace ucef
{

	FederateBase::FederateBase( wstring& federateName ) : m_federateName(federateName),
	                                                      m_federateAmbassador(),
	                                                      m_rtiAmbassador(nullptr)

	{
		initialiseRti();
		initialiseFederation();
	}

	FederateBase::~FederateBase()
	{

	}

	void FederateBase::initialiseRti()
	{
		//----------------------------------------
		//            Create RTI Ambassador
		//-----------------------------------------
		RTIambassador* tmpAmbassador = RTIambassadorFactory().createRTIambassador().release();
		m_rtiAmbassador.reset(tmpAmbassador);

		//----------------------------------------
		//            Connect to the RTI
		//-----------------------------------------
		wcout << m_federateName << " trying to connect to RTI." << endl;
		try
		{
			m_rtiAmbassador->connect(m_federateAmbassador, HLA_IMMEDIATE);
			wcout << m_federateName << " Successfully connected to the RTI" << endl;
		}
		catch( ConnectionFailed& connectionFailed )
		{
			wcout << L"Connection failed " << connectionFailed.what() << endl;
		}
		catch( InvalidLocalSettingsDesignator& settings )
		{
			wcout << L"Connection failed, InvalidLocalSettingsDesignator: "
			      << settings.what() << endl;
		}
		catch( UnsupportedCallbackModel& callbackModel )
		{
			wcout << L"Connection failed, UnsupportedCallbackModel: "
			      << callbackModel.what() << endl;
		}
		catch( AlreadyConnected& connected )
		{
			wcout << L"Connection failed, AlreadyConnected: "
			      << connected.what() << endl;
		}
		catch( RTIinternalError& error )
		{
			wcout << L"Connection failed, Generic Error: "
			      << error.what() << endl;
		}
	}

	void FederateBase::initialiseFederation()
	{
		//----------------------------------------
		//            Create Federation
		//-----------------------------------------
		try
		{
			vector<wstring> foms;
			foms.push_back( L"restaurant/RestaurantFood.xml" );
			foms.push_back( L"restaurant/RestaurantDrinks.xml" );
			foms.push_back( L"restaurant/RestaurantProcesses.xml" );

			m_rtiAmbassador->createFederationExecution( L"ExampleFederation", foms );
			wcout << L"Federation Created." << endl;
		}
		catch( FederationExecutionAlreadyExists& exists )
		{
			wcout << L"Federation creation failed, already created" << endl;
		}
		catch( Exception& e )
		{
			wcout << L"Generic Error: " << e.what() << endl;
		}

		//----------------------------------------
		//            Join Federation
		//-----------------------------------------
		m_rtiAmbassador->joinFederationExecution( m_federateName,
		                                          L"Example Federate",
		                                          L"ExampleFederation" );
		wcout << L"Joined Federation as " << m_federateName << endl;
	}

	void FederateBase::initialiseHandles()
	{
		string name = "name";
		string path = "path";

		//----------------------------------------------------------
		//            Store object class handlers
		//----------------------------------------------------------
		vector<shared_ptr<ObjectClass>> objectClasses = SOMParser::getObjectClasses( name, path );

		for( const shared_ptr<ObjectClass> &objectClass : objectClasses )
		{
			wcout << L"Checking class " << objectClass->name << endl;
			ObjectClassHandle classHandle =
					m_rtiAmbassador->getObjectClassHandle( objectClass->name );

			list<std::shared_ptr<ObjectAttribute>> &attributes = objectClass->attributes;
			for( const shared_ptr<ObjectAttribute> &attribute : attributes )
			{
				AttributeHandle attributeHandle =
						m_rtiAmbassador->getAttributeHandle( classHandle, attribute->name );
				wcout << L"Checking attribute " << attribute->name << " in " << objectClass->name << endl;
			}
		}

		//----------------------------------------------------------
		//            Store interaction class handlers
		//----------------------------------------------------------
		vector<shared_ptr<InteractionClass>> interactionClasses = SOMParser::getInteractionClasses( name, path );
		for( const shared_ptr<InteractionClass> &interactionClass : interactionClasses )
		{
			wcout << L"Checking class " << interactionClass->name << endl;
			InteractionClassHandle interactionHandle =
					m_rtiAmbassador->getInteractionClassHandle( interactionClass->name );

			list<std::shared_ptr<InteractionParameter>> &parameters = interactionClass->parameters;
			for( const shared_ptr<InteractionParameter> &parameter : parameters )
			{
				ParameterHandle parameterHandle =
						m_rtiAmbassador->getParameterHandle( interactionHandle, parameter->name );
				wcout << L"Checking attribute " << parameter->name << " in " << interactionClass->name << endl;
			}
		}
	}
}


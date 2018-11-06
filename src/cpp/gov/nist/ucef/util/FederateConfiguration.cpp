#include "FederateConfiguration.h"
using namespace std;

namespace ucef
{
	namespace util
	{
		FederateConfiguration::FederateConfiguration() : m_federateName( "Federate" + to_string(rand()) ),
		                                                 m_federateType( "FederateType" + to_string(rand()) ),
		                                                 m_immediateCallBacks( true ),
		                                                 m_timeRegulated( true ),
		                                                 m_timeConstrained( true ),
		                                                 m_lookAhead( 1.0 ),
		                                                 m_timeStep( 1.0 )
		{

		}

		string FederateConfiguration::getFederationName()
		{
			return "ExampleFederation";
		}

		string FederateConfiguration::getFederateName()
		{
			return m_federateName;
		}

		string FederateConfiguration::getFederateType()
		{
			return m_federateType;
		}

		vector<wstring> FederateConfiguration::getFomPaths()
		{
			vector<wstring> foms;
			foms.push_back( L"restaurant/RestaurantFood.xml" );
			foms.push_back( L"restaurant/RestaurantDrinks.xml" );
			foms.push_back( L"restaurant/RestaurantProcesses.xml" );
			return foms;
		}

		wstring FederateConfiguration::getSomPath()
		{
			return L"restaurant/RestaurantDrinks.xml";
		}

		float FederateConfiguration::getLookAhead()
		{
			return m_lookAhead;
		}

		float FederateConfiguration::getTimeStep()
		{
			return m_lookAhead;
		}

		bool FederateConfiguration::isImmediate()
		{
			return m_immediateCallBacks;
		}

		bool FederateConfiguration::isTimeRegulated()
		{
			return m_timeRegulated;
		}

		bool FederateConfiguration::isTimeConstrained()
		{
			return m_timeConstrained;
		}
	}
}

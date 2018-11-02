#include "FederateConfiguration.h"
using namespace std;

namespace ucef
{
	namespace util
	{
		FederateConfiguration::FederateConfiguration() : m_federateName( L"Federate" + to_wstring(rand()) ),
		                                                 m_federateType( L"FederateType" + to_wstring(rand()) ),
		                                                 m_immediateCallBacks( true ),
		                                                 m_timeRegulated( true ),
		                                                 m_timeConstrained( true ),
		                                                 m_lookAhead( 1.0 )
		{

		}

		wstring FederateConfiguration::getFederationName()
		{
			return L"ExampleFederation";
		}

		std::wstring FederateConfiguration::getFederateName()
		{
			return m_federateName;
		}

		std::wstring FederateConfiguration::getFederateType()
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

		string FederateConfiguration::getSomPath()
		{
			return "restaurant/RestaurantFood.xml";
		}

		float FederateConfiguration::getLookAhead()
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

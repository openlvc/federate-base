#include "FederateConfiguration.h"

#include <ctime>

using namespace std;

namespace ucef
{
	namespace util
	{
		FederateConfiguration::FederateConfiguration() : m_federateName( "Federate" + to_string(rand()) ),
		                                                 m_federateType( "FederateType" + to_string(rand()) ),
		                                                 m_lookAhead( 1.0 ),
		                                                 m_timeStep( 1.0 ),
		                                                 m_immediateCallBacks( true ),
		                                                 m_timeRegulated( true ),
		                                                 m_timeConstrained( true )
		{

			m_federateName = ( "Federate" + to_string(std::time(0)));
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

		vector<string> FederateConfiguration::getFomPaths()
		{
			vector<string> foms;
			foms.push_back( "restaurant/RestaurantFood.xml" );
			foms.push_back( "restaurant/RestaurantDrinks.xml" );
			foms.push_back( "restaurant/RestaurantProcesses.xml" );
			return foms;
		}

		string FederateConfiguration::getSomPath()
		{
			return "restaurant/RestaurantProcesses.xml";
		}

		float FederateConfiguration::getLookAhead()
		{
			return m_lookAhead;
		}

		float FederateConfiguration::getTimeStep()
		{
			return m_timeStep;
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

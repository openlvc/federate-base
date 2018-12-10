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

		void FederateConfiguration::cacheObjectClass( shared_ptr<ObjectClass>& objectClass )
		{
			// store the ObjectClass in m_objectCacheStoreByName for later use
			m_objectDataStoreByName.insert( make_pair(objectClass->name, objectClass) );
		}

		vector<string> FederateConfiguration::getClassNamesPublished()
		{
			vector<string> publishClassNames;
			for( auto& kv : m_objectDataStoreByName )
			{
				if( kv.second->publish )
				{
					publishClassNames.push_back( kv.first );
				}
			}
			return publishClassNames;
		}

		vector<string> FederateConfiguration::getClassNamesSubscribed()
		{
			vector<string> subscribedClassNames;
			for( auto& kv : m_objectDataStoreByName )
			{
				if( kv.second->subscribe )
				{
					subscribedClassNames.push_back( kv.first );
				}
			}
			return subscribedClassNames;
		}

		void FederateConfiguration::cacheInteractionClass(std::shared_ptr<InteractionClass>& interactionClass)
		{
			// now store the interactionClass in m_interactionCacheStoreByName for later use
			m_interactionDataStoreByName.insert( make_pair(interactionClass->name, interactionClass) );
		}

		vector<string> FederateConfiguration::getInteractionNamesSubscribed()
		{
			vector<string> publishInteractionNames;
			for( auto& kv : m_interactionDataStoreByName )
			{
				if( kv.second->publish )
				{
					publishInteractionNames.push_back( kv.first );
				}
			}
			return publishInteractionNames;
		}

		vector<string> FederateConfiguration::getInteractionNamesPublished()
		{
			vector<string> subscribedInteractionNames;
			for( auto& kv : m_interactionDataStoreByName )
			{
				if( kv.second->publish )
				{
					subscribedInteractionNames.push_back( kv.first );
				}
			}
			return subscribedInteractionNames;
		}

		vector<string> FederateConfiguration::getAttributeNamesPublished( const string& className )
		{
			vector<string> attributeNamesPublish;
			ObjectDataStoreByName::iterator it = m_objectDataStoreByName.find( className );
			if( it != m_objectDataStoreByName.end() )
			{
				ObjectAttributes& attributes = it->second->objectAttributes;
				for( auto& kv : attributes )
				{
					if( kv.second->publish )
					{
						attributeNamesPublish.push_back( kv.first );
					}
				}
			}
			return attributeNamesPublish;
		}

		vector<string> FederateConfiguration::getAttributeNamesSubscribed( const string& className )
		{
			vector<string> attributeNamesSubscribe;
			ObjectDataStoreByName::iterator it = m_objectDataStoreByName.find( className );
			if( it != m_objectDataStoreByName.end() )
			{
				ObjectAttributes& attributes = it->second->objectAttributes;
				for( auto& kv : attributes )
				{
					if( kv.second->subscribe )
					{
						attributeNamesSubscribe.push_back( kv.first );
					}
				}
			}
			return attributeNamesSubscribe;
		}

		vector<string> FederateConfiguration::getParameterNames( const string& interactionName )
		{
			vector<string> parameterNames;
			InteractionDataStoreByName::iterator it = m_interactionDataStoreByName.find( interactionName );
			if( it != m_interactionDataStoreByName.end() )
			{
				InteractionParameters& parameters = it->second->parameters;
				for( auto& kv : parameters )
				{
					parameterNames.push_back( kv.first );
				}
			}
			return parameterNames;
		}
	}
}
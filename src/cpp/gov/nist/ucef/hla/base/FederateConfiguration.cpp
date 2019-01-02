#include "FederateConfiguration.h"

#include <ctime>

using namespace std;

namespace base
{

	FederateConfiguration::FederateConfiguration() : m_federationName( "ExampleFederation" ),
	                                                 m_federateName( "Federate" + to_string(rand()) ),
	                                                 m_federateType( "FederateType" + to_string(rand()) ),
	                                                 m_lookAhead( 1.0 ),
	                                                 m_stepSize( 1.0 ),
	                                                 m_immediateCallBacks( true ),
	                                                 m_timeRegulated( true ),
	                                                 m_timeConstrained( true )
	{
	}

	string FederateConfiguration::getFederationName()
	{
		return m_federationName;
	}

	void FederateConfiguration::setFederationName( const string &federationName )
	{
		m_federationName = federationName;
	}

	string FederateConfiguration::getFederateName()
	{
		return m_federateName;
	}

	void FederateConfiguration::setFederateName( const string &federateName )
	{
		m_federateName = federateName;
	}

	string FederateConfiguration::getFederateType()
	{
		return m_federateType;
	}

	vector<string> FederateConfiguration::getFomPaths()
	{
		return m_foms;
	}

	void FederateConfiguration::addFomPath( const string &path )
	{
		m_foms.push_back(path);
	}

	void FederateConfiguration::clearFomPaths()
	{
		m_foms.clear();
	}

	vector<string> FederateConfiguration::getSomPaths()
	{
		// this is to support multiple SOM usage without breaking the interface
		vector<string> soms;
		soms.push_back( m_som );
		return soms;
	}

	void FederateConfiguration::addSomPath( const string &path )
	{
		m_som = path;
	}

	float FederateConfiguration::getLookAhead()
	{
		return m_lookAhead;
	}

	void FederateConfiguration::setLookAhead( float lookahead )
	{
		m_lookAhead = lookahead;
	}

	float FederateConfiguration::getTimeStep()
	{
		return m_stepSize;
	}

	void FederateConfiguration::setTimeStep( float stepSize)
	{
		m_stepSize = stepSize;
	}

	bool FederateConfiguration::isImmediate()
	{
		return m_immediateCallBacks;
	}

	void FederateConfiguration::setImmediate( bool callbackMode )
	{
		m_immediateCallBacks = callbackMode;
	}

	bool FederateConfiguration::isTimeRegulated()
	{
		return m_timeRegulated;
	}

	void FederateConfiguration::setTimeRegulated( bool timeRegulated )
	{
		m_timeRegulated = timeRegulated;
	}

	bool FederateConfiguration::isTimeConstrained()
	{
		return m_timeConstrained;
	}

	void FederateConfiguration::setTimeConstrained( bool timeConstrained )
	{
		m_timeConstrained = timeConstrained;
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

	void FederateConfiguration::cacheInteractionClass( std::shared_ptr<InteractionClass>& interactionClass )
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

#include "FederateConfiguration.h"

#include <ctime>

using namespace std;

namespace base
{

	FederateConfiguration::FederateConfiguration() : federationName( "ExampleFederation" ),
	                                                 federateName( "Federate" + to_string(rand()) ),
	                                                 federateType( "FederateType" + to_string(rand()) ),
	                                                 lookAhead( 1.0 ),
	                                                 stepSize( 1.0 ),
	                                                 immediateCallBacks( true ),
	                                                 timeRegulated( true ),
	                                                 timeConstrained( true ),
	                                                 permitToCreateFederation( false ),
	                                                 maxJoinAttempts( 1 ),
	                                                 synchBeforeResign( false )
	{
	}

	string FederateConfiguration::getFederationName()
	{
		return federationName;
	}

	void FederateConfiguration::setFederationName( const string &federationName )
	{
		this->federationName = federationName;
	}

	string FederateConfiguration::getFederateName()
	{
		return this->federateName;
	}

	void FederateConfiguration::setFederateName( const string &federateName )
	{
		this->federateName = federateName;
	}

	string FederateConfiguration::getFederateType()
	{
		return this->federateType;
	}

	void FederateConfiguration::setFederateType( string type )
	{
		this->federateType = type;
	}

	vector<string> FederateConfiguration::getFomPaths()
	{
		return this->foms;
	}

	void FederateConfiguration::addFomPath( const string &path )
	{
		this->foms.push_back(path);
	}

	void FederateConfiguration::clearFomPaths()
	{
		this->foms.clear();
	}

	vector<string> FederateConfiguration::getSomPaths()
	{
		// this is to support multiple SOM usage without breaking the interface
		vector<string> soms;
		soms.push_back( this->som );
		return soms;
	}

	void FederateConfiguration::addSomPath( const string &path )
	{
		this->som = path;
	}

	float FederateConfiguration::getLookAhead()
	{
		return this->lookAhead;
	}

	void FederateConfiguration::setLookAhead( float lookahead )
	{
		this->lookAhead = lookahead;
	}

	float FederateConfiguration::getTimeStep()
	{
		return this->stepSize;
	}

	void FederateConfiguration::setTimeStepSize( float stepSize )
	{
		this->stepSize = stepSize;
	}

	bool FederateConfiguration::isImmediate()
	{
		return this->immediateCallBacks;
	}

	void FederateConfiguration::setImmediate( bool callbackMode )
	{
		this->immediateCallBacks = callbackMode;
	}

	bool FederateConfiguration::isTimeRegulated()
	{
		return this->timeRegulated;
	}

	void FederateConfiguration::setTimeRegulated( bool timeRegulated )
	{
		this->timeRegulated = timeRegulated;
	}

	bool FederateConfiguration::isTimeConstrained()
	{
		return this->timeConstrained;
	}

	void FederateConfiguration::setTimeConstrained( bool timeConstrained )
	{
		this->timeConstrained = timeConstrained;
	}

	bool FederateConfiguration::isPermittedToCreateFederation()
	{
		return permitToCreateFederation;
	}

	void FederateConfiguration::setPermisionToCreateFederation( bool permission )
	{
		this->permitToCreateFederation = permission;
	}

	int FederateConfiguration::getMaxJoinAttempts()
	{
		return maxJoinAttempts;
	}

	void FederateConfiguration::setMaxJoinAttempts( int jointAttempts )
	{
		this->maxJoinAttempts = jointAttempts;
	}

	bool FederateConfiguration::syncBeforeResign()
	{
		return this->synchBeforeResign;
	}

	void FederateConfiguration::syncBeforeResign( bool synch )
	{
		this->synchBeforeResign = synch;
	}

	void FederateConfiguration::cacheObjectClass( shared_ptr<ObjectClass>& objectClass )
	{
		// store the ObjectClass in m_objectCacheStoreByName for later use
		this->objectDataStoreByName.insert( make_pair(objectClass->name, objectClass) );
	}

	vector<string> FederateConfiguration::getClassNamesPublished()
	{
		vector<string> publishClassNames;
		for( auto& kv : this->objectDataStoreByName )
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
		for( auto& kv : this->objectDataStoreByName )
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
		this->interactionDataStoreByName.insert( make_pair(interactionClass->name, interactionClass) );
	}

	vector<string> FederateConfiguration::getInteractionNamesSubscribed()
	{
		vector<string> publishInteractionNames;
		for( auto& kv : this->interactionDataStoreByName )
		{
			if( kv.second->subscribe )
			{
				publishInteractionNames.push_back( kv.first );
			}
		}
		return publishInteractionNames;
	}

	vector<string> FederateConfiguration::getInteractionNamesPublished()
	{
		vector<string> subscribedInteractionNames;
		for( auto& kv : this->interactionDataStoreByName )
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
		ObjectDataStoreByName::iterator it = this->objectDataStoreByName.find( className );
		if( it != this->objectDataStoreByName.end() )
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
		ObjectDataStoreByName::iterator it = this->objectDataStoreByName.find( className );
		if( it != this->objectDataStoreByName.end() )
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
		InteractionDataStoreByName::iterator it = this->interactionDataStoreByName.find( interactionName );
		if( it != this->interactionDataStoreByName.end() )
		{
			InteractionParameters& parameters = it->second->parameters;
			for( auto& kv : parameters )
			{
				parameterNames.push_back( kv.first );
			}
		}
		return parameterNames;
	}
	DataType FederateConfiguration::getDataType( const string& className, const string& memberName )
	{
		DataType dataType = DATATYPEUNKNOWN;

		ObjectDataStoreByName::iterator objIt = this->objectDataStoreByName.find( className );
		if( objIt != this->objectDataStoreByName.end() ) // check object classes
		{
			ObjectAttributes& attributes = objIt->second->objectAttributes;
			ObjectAttributes::iterator attIt = attributes.find( memberName );
			if( attIt != attributes.end() ) // found an attribute with given class name and member name
			{
				dataType = attIt->second->type;
			}
		}
		else // check parameter classes
		{
			InteractionDataStoreByName::iterator intIt = this->interactionDataStoreByName.find( className );
			if( intIt != this->interactionDataStoreByName.end() )
			{
				InteractionParameters& parameters = intIt->second->parameters;
				InteractionParameters::iterator paramIt = parameters.find( memberName );
				if( paramIt != parameters.end()) // found a parameter with the given class name and member name
				{
					dataType = paramIt->second->type;
				}
			}
		}
		return dataType;
	}
}

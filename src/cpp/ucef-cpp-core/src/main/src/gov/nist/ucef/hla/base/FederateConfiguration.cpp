#include "gov/nist/ucef/hla/base/FederateConfiguration.h"

#include <algorithm>
#include <ctime>
#include <fstream>
#include <iostream>
#include <sstream>

#include "gov/nist/ucef/util/JsonParser.h"
#include "gov/nist/ucef/util/Logger.h"

#include <rapidjson/document.h>
#include <rapidjson/istreamwrapper.h>

using namespace base::util;
using namespace rapidjson;
using namespace std;

namespace base
{
	// General fedconfig parameter keys
	string FederateConfiguration::KEY_LOG_LEVEL               = "logLevel";
	string FederateConfiguration::KEY_FEDERATE_NAME           = "federateName";
	string FederateConfiguration::KEY_FEDERATE_TYPE           = "federateType";
	string FederateConfiguration::KEY_FEDERATION_EXEC_NAME    = "federationExecName";
	string FederateConfiguration::KEY_CAN_CREATE_FEDERATION   = "canCreateFederation";
	string FederateConfiguration::KEY_STEP_SIZE               = "stepSize";
	string FederateConfiguration::KEY_MAX_JOIN_ATTEMPTS       = "maxJoinAttempts";
	string FederateConfiguration::KEY_JOIN_RETRY_INTERVAL_SEC = "joinRetryIntervalSec";
	string FederateConfiguration::KEY_SYNC_BEFORE_RESIGN      = "syncBeforeResign";
	string FederateConfiguration::KEY_CALLBACKS_ARE_IMMEDIATE = "callbacksAreImmediate";
	string FederateConfiguration::KEY_LOOK_AHEAD              = "lookAhead";
	string FederateConfiguration::KEY_TIME_REGULATED          = "timeRegulated";
	string FederateConfiguration::KEY_TIME_CONSTRAINED        = "timeConstrained";
	string FederateConfiguration::KEY_BASE_FOM_PATHS          = "baseFomPaths";
	string FederateConfiguration::KEY_JOIN_FOM_PATHS          = "joinFomPaths";
	string FederateConfiguration::KEY_SOM_PATHS               = "somPaths";

	FederateConfiguration::FederateConfiguration() : federationName( "BaseFederation" ),
	                                                 federateName( "Federate" + to_string(rand()) ),
	                                                 federateType( "FederateType" + to_string(rand()) ),
	                                                 lookAhead( 1.0 ),
	                                                 stepSize( 1.0 ),
	                                                 immediateCallBacks( true ),
	                                                 timeRegulated( true ),
	                                                 timeConstrained( true ),
	                                                 permitToCreateFederation( false ),
													 retryInterval( 1 ),
	                                                 maxJoinAttempts( 1 ),
	                                                 synchBeforeResign( false )
	{
	}

	FederateConfiguration::~FederateConfiguration()
	{

	}

	void FederateConfiguration::fromJsonFile( const string& configPath )
	{
		Logger::getInstance().log( "Federate config path is set to : " + configPath, LevelInfo );

		string configStr = JsonParser::getJsonString( configPath );

		//---------------------------------
		// Start loading config values
		//---------------------------------
		Logger::getInstance().log( "Reading federate configuration", LevelInfo );

		// Set log level
		bool hasKey = JsonParser::hasKey( configStr, KEY_LOG_LEVEL );
		if( hasKey )
		{
			string logLevelStr = JsonParser::getValueAsString( configStr, KEY_LOG_LEVEL );
			LogLevel logLevel = ConversionHelper::toLogLevel( logLevelStr );
			Logger::getInstance().setLogLevel( logLevel );
		}
		else
		{
			string errorMsg = "Config key " + KEY_LOG_LEVEL + " could not be found.";
			errorMsg += " Using Info as the default loge level.";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Federation name
		hasKey = JsonParser::hasKey( configStr, KEY_FEDERATION_EXEC_NAME );
		if( hasKey )
		{
			string fedExecStr = JsonParser::getValueAsString( configStr, KEY_FEDERATION_EXEC_NAME );
			setFederationName( fedExecStr );

			string msg = "Using " + getFederationName() + " as the federation name.";
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_FEDERATION_EXEC_NAME + " could not be found.";
			errorMsg += " Using " + getFederationName() + " as the default federation name.";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Federate name
		hasKey = JsonParser::hasKey( configStr, KEY_FEDERATE_NAME );
		if( hasKey )
		{
			string fedNameStr = JsonParser::getValueAsString( configStr, KEY_FEDERATE_NAME );
			setFederateName( fedNameStr );

			string msg = "Using " + getFederateName() + " as the federate name.";
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_FEDERATE_NAME + " could not be found.";
			errorMsg += " Using " + getFederateName() + " as the default federate name.";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Federate type
		hasKey = JsonParser::hasKey( configStr, KEY_FEDERATE_TYPE );
		if( hasKey )
		{
			string fedType = JsonParser::getValueAsString( configStr, KEY_FEDERATE_TYPE );
			setFederateType( fedType );

			string msg = "Using " + getFederateType() + " as the federate type.";
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_FEDERATE_TYPE + " could not be found.";
			errorMsg += " Using " + getFederateType() + " as the default federate type.";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Create permission
		hasKey = JsonParser::hasKey( configStr, KEY_CAN_CREATE_FEDERATION );
		if( hasKey )
		{
			bool canCreate = JsonParser::getValueAsBool( configStr, KEY_CAN_CREATE_FEDERATION );
			setPermisionToCreateFederation( canCreate );

			string msg = "Setting federation creation permission to : ";
			msg+= isPermittedToCreateFederation() ? "True" : "False";
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_CAN_CREATE_FEDERATION + " could not be found.";
			errorMsg += "Setting federation creation permission to : " + isPermittedToCreateFederation();
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Step size
		hasKey = JsonParser::hasKey( configStr, KEY_STEP_SIZE );
		if( hasKey )
		{
			float stepSizeVal = JsonParser::getValueAsFloat( configStr, KEY_STEP_SIZE );
			if( stepSizeVal > 0.0f )
				setTimeStep( stepSizeVal );
			else
				setTimeStep( 1.0f );
			string msg = "Setting time step size to : " + to_string( getTimeStep() );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_STEP_SIZE + " could not be found.";
			errorMsg += "Setting time step size to : " + to_string( getTimeStep() );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Joint attempts
		hasKey = JsonParser::hasKey( configStr, KEY_MAX_JOIN_ATTEMPTS );
		if( hasKey )
		{
			float jointAttempts = JsonParser::getValueAsInt( configStr, KEY_MAX_JOIN_ATTEMPTS );
			if( jointAttempts > 0 )
				setMaxJoinAttempts( jointAttempts );
			else
				setMaxJoinAttempts( 1 );
			string msg = "Setting maximum join attempts to : " + to_string( getMaxJoinAttempts() );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_MAX_JOIN_ATTEMPTS + " could not be found.";
			errorMsg += " Setting maximum join attempts to : " + to_string( getMaxJoinAttempts() );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Retry interval
		hasKey = JsonParser::hasKey( configStr, KEY_JOIN_RETRY_INTERVAL_SEC );
		if( hasKey )
		{
			float retryInterval = JsonParser::getValueAsInt( configStr, KEY_JOIN_RETRY_INTERVAL_SEC );
			if( retryInterval > 0 )
				setRetryInterval( retryInterval );
			else
				setRetryInterval( 1 );
			string msg = "Setting retry interval to : " + to_string( getRetryInterval() );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_JOIN_RETRY_INTERVAL_SEC + " could not be found.";
			errorMsg += " Setting retry interval to : " + to_string( getRetryInterval() );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Synch before resign
		hasKey = JsonParser::hasKey( configStr, KEY_SYNC_BEFORE_RESIGN );
		if( hasKey )
		{
			bool syncBeforeResign =  JsonParser::getValueAsBool( configStr, KEY_SYNC_BEFORE_RESIGN );
			setSyncBeforeResign( syncBeforeResign );

			string msg = string( "Setting synch before resign to : " ) + ( getSyncBeforeResign() ? "True" : "False" );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_SYNC_BEFORE_RESIGN + " could not be found.";
			errorMsg += string( " Setting synch before resign to : " ) + ( getSyncBeforeResign() ? "True" : "False" );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Immediate callback
		hasKey = JsonParser::hasKey( configStr, KEY_CALLBACKS_ARE_IMMEDIATE );
		if( hasKey )
		{
			bool immediateCallback =  JsonParser::getValueAsBool( configStr, KEY_CALLBACKS_ARE_IMMEDIATE );
			setImmediate( immediateCallback );

			string msg = string( "Setting immediate callbacks to : " ) + ( isImmediate() ? "True" : "False" );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_CALLBACKS_ARE_IMMEDIATE + " could not be found.";
			errorMsg += string( " Setting immediate callbacks to : " ) + ( isImmediate() ? "True" : "False" );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Look ahead
		hasKey = JsonParser::hasKey( configStr, KEY_LOOK_AHEAD );
		if( hasKey )
		{
			float lookAhead =  JsonParser::getValueAsFloat( configStr, KEY_LOOK_AHEAD );
			setLookAhead( lookAhead );

			string msg = "Setting look ahead to : " + to_string( getLookAhead() );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_LOOK_AHEAD + " could not be found.";
			errorMsg += " Setting look ahead to : " + to_string( getLookAhead() );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Time Regulated
		hasKey = JsonParser::hasKey( configStr, KEY_TIME_REGULATED );
		if( hasKey )
		{
			bool timeRegulated =  JsonParser::getValueAsBool( configStr, KEY_TIME_REGULATED );
			setTimeRegulated( timeRegulated );

			string msg = string( "Setting time regulated to : " ) + ( isTimeRegulated() ? "True" : "False" );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_TIME_REGULATED + " could not be found.";
			errorMsg += string( " Setting time regulated to : " ) + ( isTimeRegulated() ? "True" : "False" );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Time Constrained
		hasKey = JsonParser::hasKey( configStr, KEY_TIME_CONSTRAINED );
		if( hasKey )
		{
			bool timeConstrained =  JsonParser::getValueAsBool( configStr, KEY_TIME_CONSTRAINED );
			setTimeConstrained( timeConstrained );

			string msg = string( "Setting time constrained to : " ) + ( isTimeConstrained() ? "True" : "False" );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_TIME_CONSTRAINED + " could not be found.";
			errorMsg += string( " Setting time constrained to : " ) + ( isTimeConstrained() ? "True" : "False" );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Base FOM path
		hasKey = JsonParser::hasKey( configStr, KEY_BASE_FOM_PATHS );
		if( hasKey )
		{
			auto fomPaths =  JsonParser::getValueAsStrList( configStr, KEY_BASE_FOM_PATHS );

			string msg = string( "Using base FOM path : " );
			for( string fomPath : fomPaths)
			{
				addBaseFomPath( fomPath );
				msg +=  "\n" + fomPath;
			}
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_BASE_FOM_PATHS + " could not be found.";
			errorMsg += " Base FOM path is not configured.";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// Join FOM path
		hasKey = JsonParser::hasKey( configStr, KEY_JOIN_FOM_PATHS );
		if( hasKey )
		{
			auto fomPaths =  JsonParser::getValueAsStrList( configStr, KEY_JOIN_FOM_PATHS );

			string msg = string( "Using join FOM path : " );
			for( string fomPath : fomPaths)
			{
				addJoinFomPath( fomPath );
				msg += "\n" + fomPath;
			}
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_JOIN_FOM_PATHS + " could not be found.";
			errorMsg += " Joining SOM path is not configured.";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// SOM path
		hasKey = JsonParser::hasKey( configStr, KEY_SOM_PATHS );
		if( hasKey )
		{
			auto somPaths =  JsonParser::getValueAsStrList( configStr, KEY_SOM_PATHS );

			string msg = string( "Using SOM path : " );
			for( string somPath : somPaths)
			{
				addSomPath( somPath );
				msg += "\n" + somPath;
			}
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_SOM_PATHS + " could not be found.";
			errorMsg += " SOM path is not configured.";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		Logger::getInstance().log( "Reading Federate configuration completed", LevelInfo );
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

	vector<string> FederateConfiguration::getBaseFomPaths()
	{
		return this->baseFoms;
	}

	void FederateConfiguration::addBaseFomPath( const string &path )
	{
		this->baseFoms.push_back(path);
	}

	void FederateConfiguration::clearBaseFomPaths()
	{
		this->baseFoms.clear();
	}

	vector<string> FederateConfiguration::getJoinFomPaths()
	{
		return this->joinFoms;
	}

	void FederateConfiguration::addJoinFomPath( const string &path )
	{
		this->joinFoms.push_back(path);
	}

	void FederateConfiguration::clearJoinFomPaths()
	{
		this->joinFoms.clear();
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

	void FederateConfiguration::setTimeStep( float stepSize )
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

	int FederateConfiguration::getRetryInterval()
	{
		return retryInterval;
	}

	void FederateConfiguration::setRetryInterval( int retryInterval )
	{
		this->retryInterval = retryInterval;
	}

	int FederateConfiguration::getMaxJoinAttempts()
	{
		return maxJoinAttempts;
	}

	void FederateConfiguration::setMaxJoinAttempts( int jointAttempts )
	{
		this->maxJoinAttempts = jointAttempts;
	}

	bool FederateConfiguration::getSyncBeforeResign()
	{
		return this->synchBeforeResign;
	}

	void FederateConfiguration::setSyncBeforeResign( bool synch )
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

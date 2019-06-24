#include "gov/nist/ucef/hla/base/FederateConfiguration.h"

#include <algorithm>
#include <ctime>
#include <fstream>
#include <iostream>
#include <sstream>

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
	string FederateConfiguration::KEY_FOM_PATH                = "fomPath";
	string FederateConfiguration::KEY_SOM_PATH                = "somPath";

	// OMNeT++ specific fedconfig parameter keys
	string FederateConfiguration::KEY_OMNET_INTERACTIONS      = "omnetInteractions";
	string FederateConfiguration::KEY_NET_INT_NAME            = "networkInteractionName";
	// This key represents the host to inject the network msg
	string FederateConfiguration::KEY_SRC_HOST                = "sourceHost";

	// Params in network interaction designated to the OMNeT federate
	// This key represents the name of the class wrapped by this interaction
	string FederateConfiguration::KEY_ORG_CLASS               = "wrappedClassName";
	// This key represents the payload of the wrapped class
	string FederateConfiguration::KEY_NET_DATA                = "data";

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

	list<string> FederateConfiguration::getValuesAsList( const string& configPath, const string& key )
	{
		list<string> jsonArrayValues;

		Logger::getInstance().log( "Looking for key : " + key + " in " + configPath, LevelInfo );
		ifstream ifs( configPath );
		if ( !ifs.is_open() )
		{
			Logger::getInstance().log( "Could not open the config file for reading,"
									   "Returning an empty list", LevelWarn );
			return jsonArrayValues;
		}

		IStreamWrapper isw { ifs };

		Document doc {};
		doc.ParseStream( isw );
		if( doc.HasParseError() )
		{
			stringstream ss;
			ss << "Error  : " << doc.GetParseError()  << '\n'
			   << "Offset : " << doc.GetErrorOffset() << '\n';
			Logger::getInstance().log( ss.str(), LevelError );
			return jsonArrayValues;
		}

		Value::ConstMemberIterator it = doc.FindMember( key.c_str() );
		if( it != doc.MemberEnd() )
		{
			auto values = it->value.GetArray();
			for( Value::ConstValueIterator itr = values.Begin(); itr != values.End(); ++itr)
			{
				string tmpValue = (*itr).GetString();
				jsonArrayValues.push_back( tmpValue );
			}
		}
		else
		{
			string errorMsg = "Config key " + key + " could not be found.";
			errorMsg += " Returning an empty list";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		if( jsonArrayValues.size() )
		{
			string logMsg = "Config key " + key + " found. Returning values : \n";

			for(string val : jsonArrayValues)
			{
				logMsg += val + '\n';
			}
			Logger::getInstance().log( logMsg, LevelInfo );
		}

		return jsonArrayValues;
	}

	string FederateConfiguration::getValueAsString( const string& configPath, const string& key )
	{
		string jsonValue = "";

		Logger::getInstance().log( "Looking for key : " + key + " in " + configPath, LevelInfo );
		ifstream ifs( configPath );
		if ( !ifs.is_open() )
		{
			Logger::getInstance().log( "Could not open the config file for reading,"
									   "Returning an empty list", LevelWarn );
			return jsonValue;
		}

		IStreamWrapper isw { ifs };

		Document doc {};
		doc.ParseStream( isw );
		if( doc.HasParseError() )
		{
			stringstream ss;
			ss << "Error  : " << doc.GetParseError()  << '\n'
			   << "Offset : " << doc.GetErrorOffset() << '\n';
			Logger::getInstance().log( ss.str(), LevelError );
			return jsonValue;
		}

		Value::ConstMemberIterator it = doc.FindMember( key.c_str() );
		if( it != doc.MemberEnd() )
		{
			jsonValue = it->value.GetString();

			string logMsg = "Config key " + key + " found. Returning : " + jsonValue;
			Logger::getInstance().log( logMsg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + key + " could not be found.";
			errorMsg += " Returning an empty value";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		return jsonValue;
	}

	void FederateConfiguration::loadFromJson( const string &configPath )
	{
		Logger::getInstance().log( "Federate config path is set to : " + configPath, LevelInfo );

		ifstream ifs( configPath );
		if ( !ifs.is_open() )
		{
			Logger::getInstance().log( "Could not open the config file for reading,"
			                           "trying to run with the default configuration values", LevelError );
		}

		IStreamWrapper isw { ifs };

		Document doc {};
		doc.ParseStream( isw );
		if( doc.HasParseError() )
		{
			stringstream ss;
			ss << "Error  : " << doc.GetParseError()  << '\n'
			<< "Offset : " << doc.GetErrorOffset() << '\n';
			Logger::getInstance().log( ss.str(), LevelError );

		}

		//---------------------------------
		// Start loading config values
		//---------------------------------
		Logger::getInstance().log( "Reading federate configuration", LevelInfo );

		// Set log level
		Value::ConstMemberIterator it = doc.FindMember( KEY_LOG_LEVEL.c_str() );
		if( it != doc.MemberEnd() )
		{
			string tmpLogLevel = it->value.GetString();
			transform( tmpLogLevel.begin(), tmpLogLevel.end(), tmpLogLevel.begin(), ::tolower );
			LogLevel logLevel = ConversionHelper::toLogLevel( tmpLogLevel );
			Logger::getInstance().setLogLevel( logLevel );
		}

		// Federation name
		it = doc.FindMember( KEY_FEDERATION_EXEC_NAME.c_str() );
		if( it != doc.MemberEnd() )
		{
			setFederationName( it->value.GetString() );

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
		it = doc.FindMember( KEY_FEDERATE_NAME.c_str() );
		if( it != doc.MemberEnd() )
		{
			setFederateName( it->value.GetString() );

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
		it = doc.FindMember( KEY_FEDERATE_TYPE.c_str() );
		if( it != doc.MemberEnd() )
		{
			setFederateType( it->value.GetString() );

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
		it = doc.FindMember( KEY_CAN_CREATE_FEDERATION.c_str() );
		if( it != doc.MemberEnd() )
		{
			setPermisionToCreateFederation( it->value.GetBool() );

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
		it = doc.FindMember( KEY_STEP_SIZE.c_str() );
		if( it != doc.MemberEnd() )
		{
			setTimeStep( it->value.GetFloat() );

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
		it = doc.FindMember( KEY_MAX_JOIN_ATTEMPTS.c_str() );
		if( it != doc.MemberEnd() )
		{
			setMaxJoinAttempts( it->value.GetInt() );

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
		it = doc.FindMember( KEY_JOIN_RETRY_INTERVAL_SEC.c_str() );
		if( it != doc.MemberEnd() )
		{
			setRetryInterval( it->value.GetInt() );

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
		it = doc.FindMember( KEY_SYNC_BEFORE_RESIGN.c_str() );
		if( it != doc.MemberEnd() )
		{
			setSyncBeforeResign( it->value.GetBool() );

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
		it = doc.FindMember( KEY_CALLBACKS_ARE_IMMEDIATE.c_str() );
		if( it != doc.MemberEnd() )
		{
			setImmediate( it->value.GetBool() );

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
		it = doc.FindMember( KEY_LOOK_AHEAD.c_str() );
		if( it != doc.MemberEnd() )
		{
			setLookAhead( it->value.GetFloat() );

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
		it = doc.FindMember( KEY_TIME_REGULATED.c_str() );
		if( it != doc.MemberEnd() )
		{
			setTimeRegulated( it->value.GetBool() );

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
		it = doc.FindMember( KEY_TIME_CONSTRAINED.c_str() );
		if( it != doc.MemberEnd() )
		{
			setTimeConstrained( it->value.GetBool() );

			string msg = string( "Setting time constrained to : " ) + ( isTimeConstrained() ? "True" : "False" );
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_TIME_CONSTRAINED + " could not be found.";
			errorMsg += string( " Setting time constrained to : " ) + ( isTimeConstrained() ? "True" : "False" );
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// FOM path
		it = doc.FindMember( KEY_FOM_PATH.c_str() );
		if( it != doc.MemberEnd() )
		{
			string fomPaths = it->value.GetString();
			stringstream ss( fomPaths );
			string item;
			while( std::getline(ss, item, ',') )
			{
				addFomPath(item);
			}

			string msg = string( "Using FOM path : " ) + fomPaths;
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_FOM_PATH + " could not be found.";
			errorMsg += " Running without a FOM path.";
			Logger::getInstance().log( errorMsg, LevelWarn );
		}

		// SOM path
		it = doc.FindMember( KEY_SOM_PATH.c_str() );
		if( it != doc.MemberEnd() )
		{
			string somPaths = it->value.GetString();
			stringstream ss( somPaths );
			string item;
			while( std::getline(ss, item, ',') )
			{
				addSomPath(item);
			}

			string msg = string( "Using SOM path : " ) + somPaths;
			Logger::getInstance().log( msg, LevelInfo );
		}
		else
		{
			string errorMsg = "Config key " + KEY_SOM_PATH + " could not be found.";
			errorMsg += " Running without a SOM path.";
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

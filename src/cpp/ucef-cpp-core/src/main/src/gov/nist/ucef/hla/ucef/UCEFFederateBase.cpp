#include "gov/nist/ucef/hla/ucef/UCEFFederateBase.h"

#include <algorithm>
#include <cstring>
#include <rapidjson/document.h>
#include <rapidjson/stringbuffer.h>
#include <rapidjson/prettywriter.h>

#include "gov/nist/ucef/hla/base/FederateAmbassador.h"
#include "gov/nist/ucef/util/Logger.h"

using namespace base::util;
using namespace rapidjson;
using namespace rti1516e;
using namespace std;

namespace base
{
	namespace ucef
	{

		UCEFFederateBase::UCEFFederateBase() : simEndReceived( false )
		{

		}

		UCEFFederateBase::~UCEFFederateBase()
		{

		}

		void UCEFFederateBase::initConfigFromJson( std::string configFilePath )
		{
			this->configFilePath = configFilePath;
			ucefConfig->loadFromJson( configFilePath );
		}

		void UCEFFederateBase::sendInteraction( shared_ptr<HLAInteraction>& hlaInteraction )
		{
			Logger& logger = Logger::getInstance();

			string intClassName = hlaInteraction->getInteractionClassName();
			list<string> omnetInteractions =
					ucefConfig->getValuesAsList( configFilePath, FederateConfiguration::KEY_OMNET_INTERACTIONS );

			// If not an Omnet routed interaction send to RTO
			if( find(omnetInteractions.begin(), omnetInteractions.end(), intClassName) == omnetInteractions.end() )
			{
				rtiAmbassadorWrapper->sendInteraction( hlaInteraction );
			}
			else
			{
				// Here we need to build a network interaction before sending it out
				string netInteractionName =
						ucefConfig->getValueAsString( configFilePath, FederateConfiguration::KEY_NET_INT_NAME );
				string srcOmnetHost =
						ucefConfig->getValueAsString( configFilePath, FederateConfiguration::KEY_SRC_OMNET_HOST );
				string dstOmnetHost =
						ucefConfig->getValueAsString( configFilePath, FederateConfiguration::KEY_DST_OMNET_HOST );

				string jsonStr = getJsonString( hlaInteraction );

				string logMsg = "Parameters of interaction class " + intClassName + " converted to \n" + jsonStr;
				logger.log(logMsg, LevelDebug );

				shared_ptr<HLAInteraction> netInteraction = make_shared<HLAInteraction>( netInteractionName );

				netInteraction->setValue
					( FederateConfiguration::KEY_ORG_CLASS, hlaInteraction->getInteractionClassName() );
				netInteraction->setValue( FederateConfiguration::KEY_SRC_OMNET_HOST, srcOmnetHost );
				netInteraction->setValue( FederateConfiguration::KEY_DST_OMNET_HOST, dstOmnetHost );
				netInteraction->setValue( FederateConfiguration::KEY_NET_DATA, jsonStr );
				rtiAmbassadorWrapper->sendInteraction( netInteraction );
			}

		}

		void UCEFFederateBase::incomingInteraction( long interactionHash,
		                                            const ParameterHandleValueMap& parameterValues )
		{
			lock_guard<mutex> lock( threadSafeLock );
			Logger& logger = Logger::getInstance();
			shared_ptr<InteractionClass> interactionClass = getInteractionClass( interactionHash );
			logger.log( "Received interaction update for " + interactionClass->name, LevelInfo );
			if( interactionClass )
			{
				shared_ptr<HLAInteraction> hlaInteraction;
				if( interactionClass->name == SimEnd::INTERACTION_NAME )
				{
					ucefConfig->setSyncBeforeResign( true );
					// create correct interaction based on the class name
					hlaInteraction = make_shared<SimEnd>( interactionClass->name );
					// populate interaction with received data
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					// call the right hook so users can do whatever they want to do with this interaction
					receivedSimEnd( dynamic_pointer_cast<SimEnd>(hlaInteraction),
					                federateAmbassador->getFederateTime() );
					// this execure receivedSimEnd call at least once before ending the sim
					simEndReceived = true;
				}
				else if( interactionClass->name == SimPause::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimPause>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					receivedSimPaused( dynamic_pointer_cast<SimPause>(hlaInteraction),
					                   federateAmbassador->getFederateTime() );
				}
				else if( interactionClass->name == SimResume::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimResume>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					receivedSimResumed( dynamic_pointer_cast<SimResume>(hlaInteraction),
					                    federateAmbassador->getFederateTime() );
				}
				else if( interactionClass->name == SimStart::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimStart>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					receivedSimStart( dynamic_pointer_cast<SimStart>(hlaInteraction),
					                  federateAmbassador->getFederateTime() );
				}
				else
				{
					hlaInteraction = make_shared<HLAInteraction>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					receivedInteraction( const_pointer_cast<const HLAInteraction>(hlaInteraction),
					                     federateAmbassador->getFederateTime() );
				}
			}
			else
			{
				logger.log( "Received an unknown interation with interaction id " +
				            to_string(interactionHash), LevelWarn );
			}
		}

		void UCEFFederateBase::federateExecute()
		{
			while( !simEndReceived )
			{
				if( !execute() )
					break;
			}
		}

		string UCEFFederateBase::getJsonString( shared_ptr<HLAInteraction>& hlaInteraction )
		{
			// convert interaction param values to a JSON string
			Document d;
			d.SetObject();
			Document::AllocatorType& allocator = d.GetAllocator();

			Value obj( kObjectType );

			// Get parameters of this interaction
			string intClassName = hlaInteraction->getInteractionClassName();
			vector<string> params = ucefConfig->getParameterNames( intClassName );
			for( auto& param : params )
			{
				// Figure out the data type of the parameter
				DataType dataType = ucefConfig->getDataType( intClassName, param );

				// Now add param values to JSON object
				if( dataType == DATATYPESTRING )
				{
					string parmVal = hlaInteraction->getAsString( param );
					Value val;
					val.SetString( parmVal.c_str(), static_cast<SizeType>(parmVal.length()), allocator );
					Value key( param.c_str(), allocator );
					obj.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPEBOOLEAN )
				{
					bool parmVal = hlaInteraction->getAsBool( param );
					Value val;
					val.SetBool( parmVal );
					Value key( param.c_str(), allocator );
					obj.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPESHORT )
				{
					short parmVal = hlaInteraction->getAsShort( param );
					Value val;
					val.SetInt( (int)parmVal );
					Value key( param.c_str(), allocator );
					obj.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPEINT  )
				{
					int parmVal = hlaInteraction->getAsInt( param );
					Value val;
					val.SetInt( parmVal );
					Value key( param.c_str(), allocator );
					obj.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPELONG )
				{
					long parmVal = hlaInteraction->getAsLong( param );
					Value val;
					val.SetInt64( parmVal );
					Value key( param.c_str(), allocator );
					obj.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPEFLOAT )
				{
					float parmVal = hlaInteraction->getAsFloat( param );
					Value val;
					val.SetFloat(parmVal );
					Value key( param.c_str(), allocator );
					obj.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPEDOUBLE )
				{
					double parmVal = hlaInteraction->getAsDouble( param );
					Value val;
					val.SetDouble( parmVal );
					Value key( param.c_str(), allocator );
					obj.AddMember( key, val, allocator );
				}
			}

			// Convert JSON document to a string
			rapidjson::StringBuffer strbuf;
			rapidjson::PrettyWriter<rapidjson::StringBuffer> writer( strbuf );
			d.Accept( writer );
			return string( strbuf.GetString() );
		}
	}
}

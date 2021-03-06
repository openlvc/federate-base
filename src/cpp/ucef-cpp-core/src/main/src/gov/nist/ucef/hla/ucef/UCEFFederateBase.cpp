#include "gov/nist/ucef/hla/ucef/UCEFFederateBase.h"

#include <algorithm>
#include <cstring>

#include <rapidjson/document.h>
#include <rapidjson/prettywriter.h>
#include <rapidjson/stringbuffer.h>

#include "gov/nist/ucef/hla/base/FederateAmbassador.h"
#include "gov/nist/ucef/util/JsonParser.h"
#include "gov/nist/ucef/util/Logger.h"

using namespace base::util;
using namespace rapidjson;
using namespace rti1516e;
using namespace std;

namespace base
{
	namespace ucef
	{
		// OMNeT++ specific fedconfig parameter keys
		string UCEFFederateBase::KEY_OMNET_CONFIG            = "omnet";
		string UCEFFederateBase::KEY_OMNET_INTERACTIONS      = "interactions";
		string UCEFFederateBase::KEY_NET_INT_NAME            = "networkInteractionName";
		// This key represents the host to inject the network msg
		string UCEFFederateBase::KEY_SRC_HOST                = "sourceHost";

		// Params in network interaction designated to the OMNeT federate
		// This key represents the name of the original class wrapped by network interaction
		string UCEFFederateBase::KEY_ORG_CLASS               = "wrappedClassName";
		// This key represents the payload of the wrapped class
		string UCEFFederateBase::KEY_NET_DATA                = "data";

		UCEFFederateBase::UCEFFederateBase() : netInteractionName( "HLAinteractionRoot.NetworkInteraction" ),
		                                       simEndReceived( false )

		{

		}

		UCEFFederateBase::~UCEFFederateBase()
		{

		}

		void UCEFFederateBase::configureFromJSON( const string& configFilePath )
		{
			Logger& logger = Logger::getInstance();

			FederateBase::configureFromJSON( configFilePath );

			// For Omnet specifics first get the JSON string from the given file
			string configString = JsonParser::getJsonString( configFilePath );

			if( JsonParser::hasKey( configString, KEY_OMNET_CONFIG ) )
			{
				// get the omnet part of the configuration as a string
				configString = JsonParser::getJsonObjectAsString( configString, KEY_OMNET_CONFIG );
				bool hasKey = JsonParser::hasKey( configString, KEY_NET_INT_NAME );
				if( hasKey )
				{
					string tmpIntName = JsonParser::getValueAsString( configString, KEY_NET_INT_NAME );
					netInteractionName = tmpIntName;

					string msg = "Key " + KEY_NET_INT_NAME + " found. Using " + netInteractionName;
					msg += " as network interaction to communicate with OMNeT++ federate ";
					logger.log( msg, LevelInfo );
				}
				else
				{
					string msg = "Key " + KEY_NET_INT_NAME + " not found. Using " + netInteractionName;
					msg += " as network interaction to communicate with OMNeT++ federate ";
					logger.log( msg, LevelInfo );
				}

				hasKey = JsonParser::hasKey( configString, KEY_SRC_HOST );
				if( hasKey )
				{
					srcHost = JsonParser::getValueAsString( configString, KEY_SRC_HOST );

					string msg = "Key " + KEY_SRC_HOST + " found. Using " + srcHost;
					msg += " as the name of the corresponding node in OMNeT++ simulation ";
					logger.log( msg, LevelInfo );
				}

				hasKey = JsonParser::hasKey( configString, KEY_OMNET_INTERACTIONS );
				if( hasKey )
				{
					string msg = "Key " + KEY_OMNET_INTERACTIONS + " found. Following ";
					msg += "interactions will be routed via OMNeT++ simulation.";
					logger.log( msg, LevelInfo );

					omnetInteractions = JsonParser::getValueAsStrList( configString, KEY_OMNET_INTERACTIONS );
					for( const string& omnetInteraction : omnetInteractions )
					{
						logger.log( omnetInteraction + "\n", LevelInfo );
						omnetInteractionsInRegex.push_back( ConversionHelper::toRegex(omnetInteraction) );
					}
				}
				else
				{
					logger.log( "No interactions are specified for OMNeT routing", LevelInfo );
				}

			}
			else
			{
				logger.log( "Configured to run without OMNeT++", LevelDebug );
			}
		}

		void UCEFFederateBase::sendInteraction( shared_ptr<HLAInteraction>& hlaInteraction )
		{
			Logger& logger = Logger::getInstance();

			string intClassName = hlaInteraction->getInteractionClassName();

			// If not an OMNeT routed interaction send to RTI
			if( isNetworkInteraction( intClassName ) )
			{
				string logMsg = "Converting interaction class " + intClassName + " to a network interaction";
				logger.log( logMsg, LevelInfo );

				// Here we need to build a network interaction before sending it out
				shared_ptr<HLAInteraction> netInteraction = make_shared<HLAInteraction>( netInteractionName );

				// convert the parameters of this interactions to a JSON string
				string jsonStr = hlaToJsonString( hlaInteraction );
				logger.log( "Parameters of this interaction got converted to \n" + jsonStr, LevelDebug);

				netInteraction->setValue( KEY_ORG_CLASS, hlaInteraction->getInteractionClassName() );
				netInteraction->setValue( KEY_SRC_HOST, srcHost );
				netInteraction->setValue( KEY_NET_DATA, jsonStr );

				logger.log( "Sending network interaction to RTI", LevelInfo );

				rtiAmbassadorWrapper->sendInteraction( netInteraction );
			}
			else
			{
				logger.log( "Sending interaction class " + intClassName + " to RTI", LevelInfo );
				rtiAmbassadorWrapper->sendInteraction( hlaInteraction );
			}

		}

		void UCEFFederateBase::incomingInteraction( long interactionHash,
		                                            const ParameterHandleValueMap& parameterValues )
		{
			lock_guard<mutex> lock( threadSafeLock );
			Logger& logger = Logger::getInstance();
			shared_ptr<InteractionClass> interactionClass = getInteractionClass( interactionHash );

			if( interactionClass )
			{
				// If the received interaction is a sim interaction call the
				// simulation control methods accordingly.
				if( isSimulationControlInteraction(interactionClass->name) )
				{
					processSimControlInteraction( interactionClass, parameterValues );

					string logMsg = "Federate " + ucefConfig->getFederateName();
					logMsg = logMsg + " received a sim interaction " + interactionClass->name;
					logger.log( logMsg, LevelDebug );
				}
				else
				{
					shared_ptr<HLAInteraction> hlaInteraction = make_shared<HLAInteraction>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					// determine whether we should receive this interaction or not
					if( shouldReceiveInteraction(hlaInteraction) )
					{
						receivedInteraction( hlaInteraction, federateAmbassador->getFederateTime() );
					}
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

		bool UCEFFederateBase::isSimulationControlInteraction( const string& interactionName )
		{
			bool simInteraction = false;

			if( interactionName == SimEnd::INTERACTION_NAME ||
			    interactionName == SimPause::INTERACTION_NAME ||
			    interactionName == SimResume::INTERACTION_NAME ||
			    interactionName == SimStart::INTERACTION_NAME )
			{
				simInteraction = true;
			}
			return simInteraction;
		}

		bool UCEFFederateBase::shouldReceiveInteraction( shared_ptr<HLAInteraction>& hlaInteraction )
		{
			Logger& logger = Logger::getInstance();

			bool shouldReceive = true;
			string interactionName = hlaInteraction->getInteractionClassName();

			if( hlaInteraction->isPresent("federateFilter") )
			{
				string fedFilter = hlaInteraction->getAsString( "federateFilter" );
				string federateName = getFederateConfiguration()->getFederateName();
				list<string> dstFeds = ConversionHelper::tokenize( fedFilter, ',' );
				// If the interaction has a federateFilter param test interaction is
				// designated to me.
				if( ConversionHelper::isMatch(federateName, dstFeds) )
				{
					string logMsg = "Federate " + ucefConfig->getFederateName();
					logMsg = logMsg + " received interaction " + interactionName + " designated to ";
					logMsg = logMsg +  "me . I am going to forward it to the user.";
					logger.log( logMsg, LevelDebug );
				}
				else
				{
					string logMsg = "Federate " + ucefConfig->getFederateName();
					logMsg = logMsg + " received an interaction " + interactionName;
					logMsg = logMsg +  ". Going to ignore it as it is not designated to me.";
					logger.log( logMsg, LevelDebug );

					shouldReceive = false;
				}
			}
			else
			{
				string logMsg = "Federate " + ucefConfig->getFederateName();
				logMsg = logMsg + " received interaction " + interactionName;
				logMsg += " without a designated federate. I am going to forward it to the user.";
				logger.log( logMsg, LevelDebug );
			}

			return shouldReceive;
		}


		bool UCEFFederateBase::isNetworkInteraction( const string& className )
		{
			return ConversionHelper::isMatch( className, omnetInteractionsInRegex );
		}

		void UCEFFederateBase::processSimControlInteraction( shared_ptr<InteractionClass>& interactionClass,
				                                             const ParameterHandleValueMap& parameterValues)
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
		}

		string UCEFFederateBase::hlaToJsonString( shared_ptr<HLAInteraction>& hlaInteraction )
		{
			// convert interaction param values to a JSON string
			Document d;
			d.SetObject();
			Document::AllocatorType& allocator = d.GetAllocator();

			// Get parameters of this interaction
			string intClassName = hlaInteraction->getInteractionClassName();
			vector<string> params = ucefConfig->getParameterNames( intClassName );
			for( auto& param : params )
			{
				if( !hlaInteraction->isPresent(param) ) continue;

				// Figure out the data type of the parameter
				DataType dataType = ucefConfig->getDataType( intClassName, param );

				// Now add param values to JSON object
				if( dataType == DATATYPESTRING )
				{
					string parmVal = hlaInteraction->getAsString( param );
					Value val( parmVal.c_str(), allocator );
					Value key( param.c_str(), allocator );
					d.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPEBOOLEAN )
				{
					bool parmVal = hlaInteraction->getAsBool( param );
					Value val( parmVal );
					Value key( param.c_str(), allocator );
					d.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPESHORT )
				{
					short parmVal = hlaInteraction->getAsShort( param );
					Value val( (int)parmVal );
					Value key( param.c_str(), allocator );
					d.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPEINT  )
				{
					int parmVal = hlaInteraction->getAsInt( param );
					Value val( parmVal );
					Value key( param.c_str(), allocator );
					d.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPELONG )
				{
					long parmVal = hlaInteraction->getAsLong( param );
					Value val;
					val.SetInt64( parmVal );
					Value key( param.c_str(), allocator );
					d.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPEFLOAT )
				{
					float parmVal = hlaInteraction->getAsFloat( param );
					Value val( parmVal );
					Value key( param.c_str(), allocator );
					d.AddMember( key, val, allocator );
				}
				else if( dataType == DATATYPEDOUBLE )
				{
					double parmVal = hlaInteraction->getAsDouble( param );
					Value val( parmVal );
					Value key( param.c_str(), allocator );
					d.AddMember( key, val, allocator );
				}
			}

			// Convert JSON document to a string
			StringBuffer strbuf;
			PrettyWriter<StringBuffer> writer( strbuf );
			d.Accept( writer );
			return string( strbuf.GetString() );
		}
	}
}

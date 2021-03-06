//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see http://www.gnu.org/licenses/.
// 

#include "base/ucef/omnet/OmnetFederate.h"

#include <algorithm>

#include "base/ucef/omnet/util/MessageCodec.h"
#include "gov/nist/ucef/util/Logger.h"
#include "gov/nist/ucef/util/JsonParser.h"
#include "gov/nist/ucef/hla/base/FederateConfiguration.h"

using namespace base::ucef;
using namespace base::ucef::omnet;
using namespace base::ucef::omnet::util;
using namespace base::util;
using namespace omnetpp;
using namespace std;

NoOpFederate* OmnetFederate::thisFedarate = 0;

string OmnetFederate::KEY_OMNET_CONFIG     = "config";
string OmnetFederate::KEY_OMNET_INT_CONFIG = "interactions";
string OmnetFederate::KEY_OMNET_DST_CONFIG = "destinations";
string OmnetFederate::KEY_OMNET_SRC_HOSTS  = "sourceHosts";

OmnetFederate::OmnetFederate() : fedConfigFile( ".//resources//config//fedConfig.json" ),
                                 simConfigFile( ".//resources//config//omnetSimConfig.json" ),
                                 shouldContinue( true )
{
    thisFedarate = dynamic_cast<NoOpFederate*>( this );
}

OmnetFederate::~OmnetFederate()
{

}

NoOpFederate* OmnetFederate::getFederatePtr()
{
    return thisFedarate;
}

void OmnetFederate::initialize()
{
    Logger& logger = Logger::getInstance();

    initializeFederate();

    // Retrieve Federate properties
    federateName = getFederateConfiguration()->getFederateName();
    stepSize = getFederateConfiguration()->getTimeStep();

    // Retrieve network interaction destination properties
    routingConfigString = JsonParser::getJsonString( simConfigFile );
    bool hasRouterConfig = JsonParser::hasKey( routingConfigString, OmnetFederate::KEY_OMNET_CONFIG );
    if( hasRouterConfig )
    {
        logger.log("Following routing config found \n" + routingConfigString, LevelDebug);
    }
    else
    {
        string msg = OmnetFederate::KEY_OMNET_CONFIG + " value cannot be found in router config file " + simConfigFile;
        msg += "Running without any interaction routing information";
        logger.log(msg, LevelWarn);
    }

    // Schedule a timer message so we can run the OMNeT simulation continuously
    timerMessage = new cMessage( "timer" );
    scheduleAt( simTime(), timerMessage );
}

void OmnetFederate::finish()
{
    cancelAndDelete( timerMessage );
    shouldContinue = false;
    tearDownFederate();
}

void OmnetFederate::handleMessage( cMessage *cMsg )
{
    Logger& logger = Logger::getInstance();
    if( cMsg->isSelfMessage() )
    {
        execute(); // tick the federate
        scheduleAt( simTime() + stepSize, timerMessage );
        return;
    }
    else
    {
       // check message got the wrapped class name so we can re-construct the correct interaction
       if( cMsg->hasPar(UCEFFederateBase::KEY_ORG_CLASS.c_str()) )
       {
           string hlaClassName = cMsg->par( UCEFFederateBase::KEY_ORG_CLASS.c_str() ).stringValue();
           shared_ptr<HLAInteraction> interaction = make_shared<HLAInteraction>( hlaClassName );
           MessageCodec::packValues( interaction, cMsg );

           string logMsg = "Interaction " + hlaClassName + " created successfully.";
           logger.log( logMsg, LevelDebug );

           interactionsToRti.push_back( interaction );

           cancelAndDelete( cMsg );
       }
       else
       {
           string logMsg = "Received message doesn't have the parameter " +
                            UCEFFederateBase::KEY_ORG_CLASS + ". Hence, I cannot create a valid interaction." ;
           logger.log( logMsg, LevelError );
       }
    }
}

bool OmnetFederate::step( double federateTime )
{
    processToOmnet();
    processToHla();
    return shouldContinue;
}

void OmnetFederate::receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
                                         double federateTime )
{
    Logger& logger = Logger::getInstance();

    string interactionClassname = hlaInt->getInteractionClassName();
    if( interactionClassname.find(netInteractionName) != string::npos )
    {
        string msg = "Received an network interaction designated to me. I am going to send this to OMNeT simulation.";
        logger.log( msg, LevelDebug );

        unique_lock<mutex> lock( toOmnetLock );
        interactionsToOmnet.push_back( hlaInt );
        lock.unlock();
    }
    else
    {
        string msg = "Received an unknown interaction to me (" +  hlaInt->getInteractionClassName()  + ") going to ignore it.";
        logger.log( msg, LevelWarn );
    }
}

void OmnetFederate::initializeFederate()
{
    configureFromJSON( fedConfigFile );
    federateSetup();
}

void OmnetFederate::tearDownFederate()
{
    federateTeardown();
}

void OmnetFederate::processToHla()
{
    Logger& logger = Logger::getInstance();

    for( auto& interaction : interactionsToRti )
    {
        string logMsg = "Sending interaction " +  interaction->getInteractionClassName() + " to the RTI now";
        logger.log( logMsg, LevelDebug );

        rtiAmbassadorWrapper->sendInteraction( interaction );
    }
    interactionsToRti.clear();
}

void OmnetFederate::processToOmnet()
{
    Logger& logger = Logger::getInstance();

    unique_lock<mutex> lock( toOmnetLock );
    auto cpyInteractionsToOmnet = interactionsToOmnet;
    interactionsToOmnet.clear();
    lock.unlock();

    for( auto interaction : cpyInteractionsToOmnet )
    {

        if( !interaction->isPresent(UCEFFederateBase::KEY_SRC_HOST.c_str()) )
        {
            string msg = "Cannot find the source host in received interaction " + interaction->getInteractionClassName() + ". I am going to ignore it.";
            logger.log( msg, LevelDebug );
        }

        string srcHost = interaction->getAsString( UCEFFederateBase::KEY_SRC_HOST.c_str() );

        cModule* hostNode = getParentModule()->getSubmodule( srcHost.c_str() );

        bool routingInfoFound = false;
        if( hostNode )
        {
            cMessage* outMsg = new cMessage();
            MessageCodec::packValues( outMsg, interaction );

            // Pack an extra param to indicate that this is from OMNeT federate
            cMsgPar& msgPar = outMsg->addPar( "isOmnet" );
            msgPar.setBoolValue( true );

            int configElementCount = JsonParser::getArrayElementCount( routingConfigString, KEY_OMNET_CONFIG );

            for( int i = 0; i < configElementCount; i++ )
            {
                // First match the host name
                string routeSegment = JsonParser::getJsonObjectAsString( routingConfigString, KEY_OMNET_CONFIG, i );
                auto sourceList = JsonParser::getValueAsStrList( routeSegment, KEY_OMNET_SRC_HOSTS );
                if( ConversionHelper::isMatch(srcHost, sourceList) )
                {
                    // Then match the interaction
                    string orginteraction = interaction->getAsString( UCEFFederateBase::KEY_ORG_CLASS.c_str() );

                    auto intList = JsonParser::getValueAsStrList( routeSegment, KEY_OMNET_INT_CONFIG );

                    if( ConversionHelper::isMatch(orginteraction, intList) )
                    {
                        // If here that means we found a matching routing segment, now we need to stuff that info
                        string routingInfo = JsonParser::getJsonObjectAsString( routeSegment, KEY_OMNET_DST_CONFIG );

                        if( routingInfo != "")
                        {
                            routingInfoFound = true;
                            auto routingInfoMap = JsonParser::getValuesAsKeyValMapList( routeSegment, KEY_OMNET_DST_CONFIG );
                            for( auto designationInfo : routingInfoMap )
                            {
                                if( designationInfo.size() )
                                {
                                    cMessage* dupMSg = outMsg->dup();
                                    for( auto &kv : designationInfo )
                                    {
                                        cMsgPar& msgPar = dupMSg->addPar( kv.first.c_str() );
                                        msgPar.setStringValue( kv.second.c_str() );
                                    }
                                    sendDirect( dupMSg, hostNode, "out" );

                                    string logMsg = "Sending a message(id :" + to_string( dupMSg->getId() ) + ") representing received interaction ";
                                    logMsg += interaction->getInteractionClassName() + " directly to the source host " + srcHost;
                                    logger.log( logMsg, LevelDebug );
                                }
                            }
                        }
                        break;
                    }
                }

            }

            if( !routingInfoFound )
            {
                string msg = "I couldn't find routing info for the message directed to " + srcHost +". I am not going to send the message.";
                logger.log( msg, LevelWarn );
            }
            delete outMsg; // Must delete this msg as we are sending dup of this msg out
        }
        else
        {
            string msg = "OMNeT federate cannot find the source host " + srcHost +". I am not going to send the message";
            logger.log( msg, LevelWarn );
        }
    }
}



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
#include "gov/nist/ucef/hla/base/FederateConfiguration.h"

using namespace base::ucef;
using namespace base::ucef::omnet;
using namespace base::ucef::omnet::util;
using namespace base::util;
using namespace omnetpp;
using namespace std;

NoOpFederate* OmnetFederate::thisFedarate = 0;

OmnetFederate::OmnetFederate() : fedConfigFile( ".//resources//config//fedConfig.json" ),
                                 networkInteractionName( "HLAinteractionRoot.NetworkInteraction" ),
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

    federateName = getFederateConfiguration()->getFederateName();
    hostName = getFederateConfiguration()->getValueAsString( fedConfigFile, FederateConfiguration::KEY_OMNET_HOST );
    string tmpValue = getFederateConfiguration()->getValueAsString( fedConfigFile, FederateConfiguration::KEY_NET_INT_NAME );
    if( tmpValue != "" )
        networkInteractionName = tmpValue;

    logger.log( "Host name of this module set to " + hostName , LevelInfo );
    logger.log( "Network interaction name set to " + networkInteractionName , LevelInfo );
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
        scheduleAt( simTime() + 1, timerMessage );
        return;
    }

    // check message got the destination host param
    if( cMsg->hasPar(FederateConfiguration::KEY_DST_OMNET_HOST.c_str()) )
    {
       string dstOmnetHost = cMsg->par( FederateConfiguration::KEY_DST_OMNET_HOST.c_str() ).stringValue();

       // Received packet should be designated to this host
       if( dstOmnetHost.compare(hostName) == 0 )
       {
           string logMsg = "Received a packet designated to " +  hostName + ". Trying to create an interaction for it." ;
           logger.log( logMsg, LevelInfo );

           // check message got the wrapped class name so we can re-construct the correct interaction
           if( cMsg->hasPar(FederateConfiguration::KEY_ORG_CLASS.c_str()) )
           {
               string hlaClassName = cMsg->par( FederateConfiguration::KEY_ORG_CLASS.c_str() ).stringValue();
               shared_ptr<HLAInteraction> interaction = make_shared<HLAInteraction>( hlaClassName );
               MessageCodec::packValues( interaction, cMsg );

               string logMsg = "Interaction " + hlaClassName + "Created successfully.";
               logger.log( logMsg, LevelInfo );

               unique_lock<mutex> lock( toHlaLock );
               interactionsToRti.push_back( interaction );
               lock.unlock();

               cancelAndDelete( cMsg );
           }
           else
           {
               string logMsg = "Received packet doesn't have the parameter " +
                               FederateConfiguration::KEY_ORG_CLASS + ". Hence, I(" + hostName +") cannot create a valid interaction." ;
               logger.log( logMsg, LevelError );
           }

       }
       else
       {
           send( cMsg, "out" );
           string msg = "Received a packet designated to " +  dstOmnetHost + ". I(" + hostName +") am going to simply forward it." ;
           logger.log( msg, LevelInfo );
       }
    }
    else
    {
       string msg = "Received packet doesn't contain parameter " +  FederateConfiguration::KEY_DST_OMNET_HOST;
       msg += ". I(" + hostName +")  am going to simply forward it" ;
       logger.log( msg, LevelDebug );
       send( cMsg, "out" );
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
    if( interactionClassname.find(networkInteractionName) != string::npos )
    {
        string designatedOmnetFederate = hlaInt->getAsString( FederateConfiguration::KEY_SRC_OMNET_HOST );

        if( designatedOmnetFederate.compare(hostName) == 0 )
        {
            string msg = "Received an network interaction designated to me (" + designatedOmnetFederate  + "). I am going to send this to OMNeT simulation.";
            logger.log( msg, LevelInfo );

            unique_lock<mutex> lock( toOmnetLock );
            interactionsToOmnet.push_back( hlaInt );
            lock.unlock();
        }
        else
        {
            string msg = "Received an network interaction designated to " + designatedOmnetFederate + " OMNeT federate. ";
            msg += hostName  + " going to ignore it.";
            logger.log( msg, LevelInfo );
        }
    }
    else
    {
        string msg = "Received an unknown interaction to me (" + hostName  + ") going to ignore it.";
        logger.log( msg, LevelWarn );
    }
}

void OmnetFederate::initializeFederate()
{
    initConfigFromJson( fedConfigFile );
    federateSetup();
}

void OmnetFederate::tearDownFederate()
{
    federateTeardown();
}

void OmnetFederate::processToHla()
{
    Logger& logger = Logger::getInstance();

    unique_lock<mutex> lock( toHlaLock );
    auto cpyInteractionsToRti = interactionsToRti;
    interactionsToRti.clear();

    lock.unlock();

    for( auto& interaction : cpyInteractionsToRti )
    {
        string logMsg = "Sending interaction " +  interaction->getInteractionClassName() + " to the RTI now";
        logger.log( logMsg, LevelInfo );

        rtiAmbassadorWrapper->sendInteraction( interaction );
    }
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
        cMessage* outMsg = new cMessage();
        MessageCodec::packValues( outMsg, interaction );

        send( outMsg, "out" );

        string logMsg = "Sending a packet to OMNeT simulation for the received interaction " +  interaction->getInteractionClassName();
        logger.log( logMsg, LevelInfo );
    }
}



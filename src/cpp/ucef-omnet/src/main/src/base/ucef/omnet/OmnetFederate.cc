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

string OmnetFederate::KEY_DST_OMNET_HOST = "dstOmnetHost";
string OmnetFederate::KEY_OMNET_HOST = "hostName";
string OmnetFederate::KEY_ORG_CLASS = "wrappedClassName";
string OmnetFederate::KEY_SRC_OMNET_HOST = "srcOmnetHost";

NoOpFederate* OmnetFederate::thisFedarate = 0;

OmnetFederate::OmnetFederate() : fedConfigFile( "\\resources\\fedConfig.json" ),
                                 networkInteractionName( "NetworkInteraction" )
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
    initializeFederate();
    auto fedConfig = thisFedarate->getFederateConfiguration();

    federateName = fedConfig->getFederateName();
    selfMessageStepSize = fedConfig->getTimeStep();
    hostName = fedConfig->getValueAsString( fedConfigFile, KEY_OMNET_HOST );

    // Schedule a self directed message
    selfMessage = new cMessage("timer");
    scheduleAt( simTime() + selfMessageStepSize, selfMessage );
}

void OmnetFederate::finish()
{
    tearDownFederate();
}

void OmnetFederate::handleMessage( cMessage *cMsg )
{
    Logger& logger = Logger::getInstance();

    if( cMsg->isSelfMessage() )
    {
        unique_lock<mutex> lock( toOmnetLock );
        auto cpyInteractionsToOmnet = interactionsToOmnet;
        interactionsToOmnet.clear();
        lock.unlock();

        for( auto interaction : cpyInteractionsToOmnet )
        {
            cMessage* outMsg = new cPacket();
            MessageCodec::packValues( outMsg, interaction );
            send( cMsg, "out" );

            string logMsg = "Sending a packet to OMNeT simulation for the received interaction " +  interaction->getInteractionClassName();
            logger.log( logMsg, LevelDebug );
        }

        scheduleAt( simTime() + selfMessageStepSize, selfMessage );
    }
    else
    {
       if( cMsg->hasPar(KEY_DST_OMNET_HOST.c_str()) )
       {
           string dstOmnetHost = cMsg->par( KEY_DST_OMNET_HOST.c_str() ).stringValue();

           // Received packet should be designated to this host
           if( dstOmnetHost.compare(hostName) == 0 )
           {
               string logMsg = "Received a packet designated to " +  hostName + ". Trying to create an interaction for it." ;
               logger.log( logMsg, LevelDebug );

               if( cMsg->hasPar(KEY_ORG_CLASS.c_str()) )
               {
                   string hlaClassName = cMsg->par( KEY_ORG_CLASS.c_str() ).stringValue();
                   shared_ptr<HLAInteraction> interaction = make_shared<HLAInteraction>( hlaClassName );
                   MessageCodec::packValues( interaction, cMsg );

                   unique_lock<mutex> lock( toHlaLock );
                   interactionsToRti.push_back( interaction );
                   lock.unlock();

                   cancelAndDelete( cMsg );
               }
               else
               {
                   string logMsg = "Received packet doesn't have the parameter " +  KEY_ORG_CLASS + ". Hence, I cannot create a valid interaction." ;
                   logger.log( logMsg, LevelError );
               }

           }
           else
           {
               string msg = "Received a packet designated to " +  dstOmnetHost + ". I am going to ignore it" ;
               logger.log( msg, LevelDebug );
           }
       }
       else
       {
           string msg = "Received packet doesn't contain parameter " +  KEY_DST_OMNET_HOST + ". I am going to ignore it" ;
           logger.log( msg, LevelDebug );
       }
    }
}

bool OmnetFederate::step( double federateTime )
{
    unique_lock<mutex> lock( toHlaLock );

    auto cpyInteractionsToRti = interactionsToRti;
    interactionsToRti.clear();

    lock.unlock();

    for( auto& interaction : cpyInteractionsToRti )
    {
        rtiAmbassadorWrapper->sendInteraction( interaction );
    }

    return true;
}
void OmnetFederate::receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
                                         double federateTime )
{
    Logger& logger = Logger::getInstance();

    string interactionClassname = hlaInt->getInteractionClassName();
    if( interactionClassname.find(networkInteractionName) != string::npos )
    {
        string designatedOmnetFederate = hlaInt->getAsString( KEY_SRC_OMNET_HOST );

        if( designatedOmnetFederate.compare(federateName) == 0 )
        {
            unique_lock<mutex> lock( toOmnetLock );
            interactionsToOmnet.push_back( hlaInt );
            lock.unlock();
        }
        else
        {
            string msg = "Received an network interaction designated to " + designatedOmnetFederate + " OMNeT federate. I am going to ignore it";
            logger.log( msg, LevelInfo );
        }
    }
    else
    {
        string msg = "Received an unknown interaction " + interactionClassname + ". I am going to ignore it";
        logger.log( msg, LevelWarn );
    }
}

void OmnetFederate::initializeFederate()
{
    shared_ptr<base::FederateConfiguration> federateConfig = getFederateConfiguration();
    federateConfig->loadFromJson( fedConfigFile );
    federateSetup();
}

void OmnetFederate::tearDownFederate()
{
    cancelAndDelete( selfMessage );

    federateTeardown();
}


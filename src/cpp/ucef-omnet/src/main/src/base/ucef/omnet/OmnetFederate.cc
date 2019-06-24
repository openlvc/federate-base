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
    string tmpValue = getFederateConfiguration()->getValueAsString( fedConfigFile, FederateConfiguration::KEY_NET_INT_NAME );
    if( tmpValue != "" )
        networkInteractionName = tmpValue;

    logger.log( "Network interaction name set to " + networkInteractionName , LevelDebug );
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
        logger.log( logMsg, LevelDebug );

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

        string logMsg = "Sending a packet to OMNeT simulation for the received interaction " +  interaction->getInteractionClassName();
        logger.log( logMsg, LevelDebug );

        send( outMsg, "out" );
    }
}



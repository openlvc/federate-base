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

#include "OmnetFederateModule.h"

#include "base/ucef/omnet/util/MessageCodec.h"

using namespace base;
using namespace base::ucef;
using namespace base::ucef::omnet;
using namespace base::ucef::omnet::util;
using namespace omnetpp;
using namespace std;

Define_Module(OmnetFederateModule);

const static string RESPONSE_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.Response";

OmnetFederateModule::OmnetFederateModule()
{

}

OmnetFederateModule::~OmnetFederateModule()
{
    cancelAndDelete(timerMessage);
}

//----------------------------------------------------------
//                IOmnetFederate Methods
//----------------------------------------------------------

void OmnetFederateModule::initModule()
{
    setFedConfigPath( ".//config/fedConfig.json" );

    timerMessage = new cMessage("timer");
    scheduleAt( simTime(), timerMessage );
}

void OmnetFederateModule::handleCMessage( cMessage *msg )
{
    if( msg->isSelfMessage() )
    {
        auto tmpRemoteChallenges = remoteChallenges;
        remoteChallenges.clear();
        // Generate a response for each remote challenge
        for( auto challenge : tmpRemoteChallenges )
        {
            cMessage* msg = new cMessage();
            msg = MessageCodec::packValues(msg, challenge );
            send( msg, "out" );
        }
        scheduleAt( simTime() + getFederateConfiguration()->getTimeStep() * 2, timerMessage );
    }
    else
    {
        std::shared_ptr<base::HLAInteraction> interaction = MessageCodec::toInteraction( msg, RESPONSE_INTERACTION );
        challengeResponses.emplace_back( interaction );
        cancelAndDelete( msg );
    }
}

void OmnetFederateModule::tearDownModule()
{
    cancelAndDelete( timerMessage );
}

//----------------------------------------------------------
//                Federate Methods
//----------------------------------------------------------
void OmnetFederateModule::beforeReadyToPopulate()
{
    std::cout << "Before ready to populate." << std::endl;
    pressEnterToContinue();
}

void OmnetFederateModule::beforeReadyToRun()
{
    std::cout << "Before ready to run." << std::endl;
    pressEnterToContinue();
}

void OmnetFederateModule::beforeFirstStep()
{
    std::cout << "Before first step." << std::endl;
    pressEnterToContinue();
}

void OmnetFederateModule::beforeReadyToResign()
{
    std::cout << "Before ready to resign." << std::endl;
    pressEnterToContinue();
}

void OmnetFederateModule::beforeExit()
{
    std::cout << "Before exit." << std::endl;
    pressEnterToContinue();
}

void OmnetFederateModule::receivedHlaInteraction( shared_ptr<const HLAInteraction> hlaInt,
                                                  double federateTime )
{
    cout << "---------------------------------------------------------" << endl;
    cout << "Received interaction with," << endl;
    cout << "--------------------------------------------------------" << endl;
    cout << "\tChallenge id : " + hlaInt->getAsString( "challengeId" ) << endl;
    cout << "\tString as    : " + hlaInt->getAsString( "stringValue" ) << endl;
    cout << "\tIndex as     : " + to_string(hlaInt->getAsInt( "beginIndex" )) << endl << endl;
    cout << "Adding to the queue for processing" << endl;
    cout << "--------------------------------------------------------" << endl;

    remoteChallenges.emplace_back( hlaInt );
}

bool OmnetFederateModule::step( double federateTime )
{
    if( !challengeResponses.size() ) return true;


    list<shared_ptr<base::HLAInteraction>> tmpReplies = challengeResponses;
    challengeResponses.clear();
    // Generate a response for each remote challenge
    for( auto reply : tmpReplies )
    {
        string ans = reply->getAsString( "stringValue" );
        string id = reply->getAsString( "challengeId" );

        reply->clear();

        reply->setValue( "substring", ans  );
        reply->setValue( "challengeId", id  );

        rtiAmbassadorWrapper->sendInteraction( reply );

        cout << "\n-----------------------------------------------------" << endl;
        cout << "Sending Name : " +  reply->getInteractionClassName() << endl;
        cout << "Sending respond : " +  reply->getAsString( "challengeId" ) << endl;
        cout << "Answer respond : " +  reply->getAsString( "substring" ) << endl;
        cout << "-----------------------------------------------------" << endl;
    }
    return true;
}

void OmnetFederateModule::pressEnterToContinue()
{
    do
    {
        cout << '\n' << "Press ENTER to continue...";
    } while (cin.get() != '\n');
}

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

using namespace base;
using namespace base::ucef;
using namespace base::ucef::omnet;
using namespace omnetpp;
using namespace std;

Define_Module(OmnetFederateModule);

const static string RESPONSE_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.Response";

OmnetFederateModule::OmnetFederateModule() : canProcess(false)
{

}

OmnetFederateModule::~OmnetFederateModule()
{
    cancelAndDelete(timerMessage);
}

bool OmnetFederateModule::step( double federateTime )
{
    if( !canProcess ) return true;

    list<Challenge> tmpRemoteChallenges = remoteChallenges;
    remoteChallenges.clear();
    // Generate a response for each remote challenge
    for( auto challenge : tmpRemoteChallenges )
    {
        Response response = solveChallenge( challenge );
        shared_ptr<HLAInteraction> responseInteraction = make_shared<HLAInteraction>( RESPONSE_INTERACTION );
        responseInteraction->setValue( "challengeId", response.challengeId );
        responseInteraction->setValue( "substring", response.resultString );
        rtiAmbassadorWrapper->sendInteraction( responseInteraction );
        cout << "Sending respond : " + challenge.id << endl;
    }
    canProcess = false;
    return true;
}

void OmnetFederateModule::receivedAttributeReflection( shared_ptr<const HLAObject> hlaObject,
                                                       double federateTime )
{
    Challenge challenge;
    challenge.id = hlaObject->getAsString( "challengeId" );
    challenge.textValue = hlaObject->getAsString( "stringValue" );
    challenge.beginIndex = hlaObject->getAsInt( "beginIndex" );

    remoteChallenges.emplace_back( challenge );

    cout << "Received object challenge id : " + challenge.id << endl;
    cout << "Received string is           : " + challenge.textValue << endl;
    cout << "Received index is            : " + to_string( challenge.beginIndex ) << endl;
    cout << "---------------------------------------------------------------------------------" << endl;

}

void OmnetFederateModule::receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
                                               double federateTime )
{
    Challenge challenge;
    challenge.id = hlaInt->getAsString( "challengeId" );
    challenge.textValue = hlaInt->getAsString( "stringValue" );
    challenge.beginIndex = hlaInt->getAsInt( "beginIndex" );

    remoteChallenges.emplace_back( challenge );

    cout << "Received interaction challenge id : " + challenge.id << endl;
    cout << "Received string is                : " + challenge.textValue << endl;
    cout << "Received index is                 : " + to_string( challenge.beginIndex ) << endl;
    cout << "---------------------------------------------------------------------------------" << endl;

}

void OmnetFederateModule::initModule()
{
    setFedConfigPath(".", "fedConfig.json");
    timerMessage = new cMessage("timer");
    scheduleAt(simTime(), timerMessage);
}

void OmnetFederateModule::handleNetMessage( omnetpp::cMessage *msg )
{
    if( msg->isSelfMessage() )
    {
         cMessage *testMessage = new cMessage( "testMessage" );
         send( testMessage, "out" );
         scheduleAt(simTime() + getFederateConfiguration()->getTimeStep() * 2, timerMessage);
    }
    else
    {
        canProcess = true;
        cancelAndDelete(msg);
    }

}

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


Response OmnetFederateModule::solveChallenge( Challenge &receievedChallenge )
{
    string receivedString = receievedChallenge.textValue;
    int beginIndex = receievedChallenge.beginIndex;
    string resultStr = receivedString.substr( beginIndex );

    Response response;
    response.challengeId = receievedChallenge.id;
    response.resultString = resultStr;
    return response;
}

void OmnetFederateModule::pressEnterToContinue()
{
    do
    {
        cout << '\n' << "Press ENTER to continue...";
    } while (cin.get() != '\n');
}

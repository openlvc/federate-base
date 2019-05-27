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

#include "OmnetFederate.h"

using namespace base::ucef;
using namespace base::ucef::omnet;
using namespace std;

NoOpFederate* OmnetFederate::thisFedarate = 0;

OmnetFederate::OmnetFederate() : filePath("."), fileName(".")
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

void OmnetFederate::initModule()
{
    cout << "OMnet++ module initializing call ";
}

void OmnetFederate::handleNetMessage( omnetpp::cMessage *msg )
{
    // send the message out
    cout << "Received message :" << msg->getName();
    delete(msg);
}

void OmnetFederate::tearDownModule()
{
    cout << "OMnet++ module tear down call ";
}

void OmnetFederate::initialize()
{
    initModule();
    initialiseFederate(".");
}

void OmnetFederate::handleMessage( omnetpp::cMessage *msg )
{
    handleNetMessage( msg );
}

void OmnetFederate::finish()
{
    tearDownFederate();
    tearDownModule();
}

void OmnetFederate::initialiseFederate( string configFilePath )
{
    shared_ptr<base::FederateConfiguration> federateConfig = getFederateConfiguration();
    federateConfig->setFederationName( string("ChallengeResponse") );
    federateConfig->setFederateName( string("OmnetResponseFederate") );
    federateConfig->setFederateType( string("OmnetResponseFederate") );
    federateConfig->setLookAhead( 0.2f );
    federateConfig->setTimeStepSize( 1.0f );
    federateConfig->addFomPath( string("ChallengeResponse/fom/ChallengeResponse.xml") );
    federateConfig->addSomPath( string("ChallengeResponse/som/Response.xml") );
    federateConfig->setPermisionToCreateFederation( true );
    federateSetup();
}

void OmnetFederate::tearDownFederate()
{
    federateTeardown();
}

void OmnetFederate::setFedConfigPath( string fedConfigFilePath, string fedConfigFileName )
{
    filePath = fedConfigFilePath;
    fileName = fedConfigFileName;
}

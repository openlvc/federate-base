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

OmnetFederate::OmnetFederate() : fedConfigFile(".")
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

void OmnetFederate::handleCMessage( omnetpp::cMessage *msg )
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
    initializeFederate();
}

void OmnetFederate::handleMessage( omnetpp::cMessage *msg )
{
    handleCMessage( msg );
}

void OmnetFederate::finish()
{
    tearDownFederate();
    tearDownModule();
}

void OmnetFederate::initializeFederate()
{
    shared_ptr<base::FederateConfiguration> federateConfig = getFederateConfiguration();
    federateConfig->loadFromJson( fedConfigFile );
    federateSetup();
}

void OmnetFederate::tearDownFederate()
{
    federateTeardown();
}

void OmnetFederate::setFedConfigPath( const string &fedConfigFilePath )
{
    fedConfigFile = fedConfigFilePath;
}

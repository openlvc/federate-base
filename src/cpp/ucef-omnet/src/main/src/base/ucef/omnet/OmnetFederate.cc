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

#include "gov/nist/ucef/util/Logger.h"

using namespace base::ucef;
using namespace base::ucef::omnet;
using namespace base::util;
using namespace omnetpp;
using namespace std;


NoOpFederate* OmnetFederate::thisFedarate = 0;

string OmnetFederate::KEY_HLA_MSG_FILTER = "hlaIncoming";

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

void OmnetFederate::initialize()
{
    initModule();
    initializeFederate();
}

void OmnetFederate::finish()
{
    tearDownModule();
    tearDownFederate();
}

void OmnetFederate::handleMessage( cMessage *msg )
{
    handleCMessage( msg );
}

void OmnetFederate::setFedConfigPath( const string &fedConfigFilePath )
{
    fedConfigFile = fedConfigFilePath;
}

void OmnetFederate::receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
                                         double federateTime )
{
    auto it = std::find( hlaMsgFilter.begin(), hlaMsgFilter.end(), hlaInt->getInteractionClassName() );
    if( it != hlaMsgFilter.end() )
    {
        receivedHlaInteraction( hlaInt, federateTime );
    }
    else
    {
        Logger::getInstance().log(" Received :" + hlaInt->getInteractionClassName() + " and ignoring it", LevelInfo );
    }
}

void OmnetFederate::initializeFederate()
{
    shared_ptr<base::FederateConfiguration> federateConfig = getFederateConfiguration();

    federateConfig->loadFromJson( fedConfigFile );

    hlaMsgFilter = federateConfig->getValueAsString( fedConfigFile, KEY_HLA_MSG_FILTER );

    federateSetup();
}

void OmnetFederate::tearDownFederate()
{
    federateTeardown();
}


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

#include "SimpleResponseModule.h"

using namespace std;

Define_Module(SimpleResponseModule);

void SimpleResponseModule::initialize() {}

void SimpleResponseModule::handleMessage( cMessage *msg )
{
   solveChallenge( msg );
   send( msg, "out");
}

void SimpleResponseModule::solveChallenge( cMessage *msg )
{

    string challengeStr = msg->par("stringValue").stringValue();
    long beginIndex = msg->par("beginIndex").longValue();
    string resultStr = challengeStr.substr( beginIndex );
    msg->par("stringValue").setStringValue( resultStr.c_str() );
}

void SimpleResponseModule::finish() {}



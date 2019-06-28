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

#include "SimpleModule.h"

using namespace std;

Define_Module(SimpleModule);

void SimpleModule::initialize()
{

}

void SimpleModule::handleMessage( cMessage *msg )
{
    if( msg->hasPar("host") )
    {
        string dstHost = msg->par("host").stringValue();
        if( getName() == dstHost )
        {
            cModule* omnetNode = getParentModule()->getSubmodule( "OMNeTFed" );
            sendDirect(msg, omnetNode, "omnet");
        }
    }
    else if( gate("out")->isConnected() )
    {
        cMessage* cMsg = new cMessage(msg->getName());
        send( cMsg, "out");
        delete msg;
    }
}

void SimpleModule::finish() {}



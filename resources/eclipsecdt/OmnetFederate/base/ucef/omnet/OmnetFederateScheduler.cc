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

#include "OmnetFederateScheduler.h"

#include "OmnetFederate.h"

using namespace base::ucef::omnet;
using namespace omnetpp;
using namespace std;

Register_Class(OmnetFederateScheduler);

OmnetFederateScheduler::OmnetFederateScheduler() : cSequentialScheduler()
{
    // TODO Auto-generated constructor stub
}

OmnetFederateScheduler::~OmnetFederateScheduler()
{
    // TODO Auto-generated destructor stub
}

cEvent* OmnetFederateScheduler::takeNextEvent()
{
    cEvent *event = sim->getFES()->peekFirst();

    if( !event )
    {
        OmnetFederate::getFederatePtr()->execute();
        return nullptr;
    }

    // First try to see if an event is scheduled compared to federate time
    double timeDifference = event->getArrivalTime().dbl() - OmnetFederate::getFederatePtr()->getTime();

    while( timeDifference > 0 )
    {
        OmnetFederate::getFederatePtr()->execute();
        timeDifference = event->getArrivalTime().dbl() - OmnetFederate::getFederatePtr()->getTime();
    }
    event = cSequentialScheduler::takeNextEvent();
    return event;
}

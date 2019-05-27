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

#ifndef IOMNETFEDERATE_H_
#define IOMNETFEDERATE_H_

#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"

#include <omnetpp.h>

namespace base
{
    namespace ucef
    {
        namespace omnet
        {
            class IOmnetFederate : public omnetpp::cSimpleModule, public base::ucef::NoOpFederate
            {
            public:

                /*
                 * Get called when initialising this cSimpleModule
                 */
                virtual void initModule() = 0;

                /*
                 * Get called when a message received from omnet network simulator
                 *
                 * @param msg represent events, messages, jobs or other entity received
                 */
                virtual void handleNetMessage( omnetpp::cMessage *msg ) = 0;

                /*
                 * Get called when exiting from this cSimpleModule
                 */
                virtual void tearDownModule() = 0;

            };
        }
    }
}

#endif /* OMNETFEDERATE_H_ */

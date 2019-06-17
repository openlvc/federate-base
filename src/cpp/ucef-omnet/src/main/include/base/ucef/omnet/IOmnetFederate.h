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

                //----------------------------------------------------------
                //   cSimpleModule methods to be implement in user code
                //----------------------------------------------------------

                /*
                 * Get called when initializing this cSimpleModule
                 */
                virtual void initModule() = 0;

                /*
                 * Get called when a message received from Omnet++ network simulator
                 *
                 * @param msg represent events, messages, jobs or other entity received
                 */
                virtual void handleCMessage( omnetpp::cMessage *msg ) = 0;

                /*
                 * Get called when exiting from this cSimpleModule
                 */
                virtual void tearDownModule() = 0;

                //----------------------------------------------------------
                //   Federate methods to be implemented in user code
                //----------------------------------------------------------

                /**
                 * Get called just before announcing and achieving the 'READY_TO_POPULATE'
                 * synchronization point
                 */
                virtual void beforeReadyToPopulate() override {}

                /**
                 * Get called just before announcing and achieving the 'READY_TO_RUN'
                 * synchronization point
                 */
                virtual void beforeReadyToRun() override {}

                /**
                 * Get called just before entering the main update loop
                 */
                virtual void beforeFirstStep() override {}

                /**
                 * Get called just before announcing and achieving the 'READY_TO_RESIGN'
                 * synchronization point
                 */
                virtual void beforeReadyToResign() override {}

                /**
                 * Get called just before resigning from the federation
                 */
                virtual void beforeExit() override {}

                /**
                 * This method is called inside the main update loop.
                 *
                 * @param federateTime the current logical time of this federate
                 */
                virtual bool step( double federateTime ) override { return true; }

                /**
                 * Get called whenever Omnet++ receives an hla interaction relevant to this federate.
                 * <p/>
                 *
                 * HLA interactions can be filtered by specifying 'hlaIncoming' key and its values in array format in
                 * federate config file.
                 *
                 * E.g. "hlaIncoming" : [ "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.ChallengeInteraction",
                 *                        "HLAobjectRoot.ParentObject.ChallengeObject" ]
                 *
                 * @param hlaInteraction Stores the received parameter updates relevant to the interaction class represented
                 *                       by {@link HLAInteraction#getClassName()}. Use {@link HLAInteraction#getAs***}
                 *                       methods to get the values of the received parameter updates. Since no type checking
                 *                       is carried out it is important to use the right methods to obtain the correct values.
                 * @param federateTime the current logical time of the federate
                 */
                virtual void receivedHlaInteraction( std::shared_ptr<const base::HLAInteraction> hlaInt, double federateTime ) = 0;

            };
        }
    }
}

#endif /* OMNETFEDERATE_H_ */

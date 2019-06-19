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

#ifndef OMNETFEDERATE_H_
#define OMNETFEDERATE_H_

#include <omnetpp.h>
#include <list>

#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"

namespace base
{
    namespace ucef
    {
        namespace omnet
        {

            class OmnetFederate : public omnetpp::cSimpleModule, public base::ucef::NoOpFederate
            {
            public:

                //----------------------------------------------------------
                //                     Static methods
                //----------------------------------------------------------

                static base::ucef::NoOpFederate* getFederatePtr();


                //----------------------------------------------------------
                //                     Constructors
                //----------------------------------------------------------
                OmnetFederate();
                virtual ~OmnetFederate();

                //----------------------------------------------------------
                //                     cSimpleModule methods
                //----------------------------------------------------------
                virtual void initialize() override;
                virtual void finish() override;

                //----------------------------------------------------------
                //                     No-Op federate methods
                //----------------------------------------------------------

                /**
                 * Step function of this federate
                 */
                virtual bool step( double federateTime ) override;

                /**
                 * Get called whenever RTI receives a new object interaction
                 */
                virtual void receivedInteraction( std::shared_ptr<const base::HLAInteraction> hlaInt,
                                                  double federateTime ) override;

            protected:
                //----------------------------------------------------------
                //                     cSimpleModule methods
                //----------------------------------------------------------
                virtual void handleMessage( omnetpp::cMessage *msg ) override;

            private:
                void initializeFederate();
                void tearDownFederate();
                void processToHla();
                void processToOmnet();

            private:
                bool shouldContinue;
                std::mutex toOmnetLock;
                std::mutex toHlaLock;
                std::list<std::shared_ptr<const base::HLAInteraction>> interactionsToOmnet;
                std::list<std::shared_ptr<base::HLAInteraction>> interactionsToRti;

                std::string federateName;
                std::string hostName;
                std::string fedConfigFile;
                std::string networkInteractionName;

                static base::ucef::NoOpFederate* thisFedarate;
            };
        }
    }
}

#endif /* OMNETFEDERATE_H_ */

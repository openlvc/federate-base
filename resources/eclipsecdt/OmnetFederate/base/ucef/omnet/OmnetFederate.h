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

#include "IOmnetFederate.h"

#include <omnetpp.h>

namespace base
{
    namespace ucef
    {
        namespace omnet
        {
            class OmnetFederate : public IOmnetFederate
            {
            public:
                //----------------------------------------------------------
                //                     Static methods
                //----------------------------------------------------------

                static base::ucef::NoOpFederate* getFederatePtr();

                //----------------------------------------------------------
                //                IOmnetFederate interface implementation
                //----------------------------------------------------------

                /*
                 * Get called when initialising this cSimpleModule
                 */
                virtual void initModule() override;

                /*
                 * Get called when a message received from omnet network simulator
                 */
                virtual void handleNetMessage( omnetpp::cMessage *msg ) override;

                /*
                 * Get called when exiting from this cSimpleModule
                 */
                virtual void tearDownModule() override;

                //----------------------------------------------------------
                //                     Constructors
                //----------------------------------------------------------
                OmnetFederate();
                virtual ~OmnetFederate();


                //----------------------------------------------------------
                //                     Member Methods
                //----------------------------------------------------------
                virtual void initialize() override;
                virtual void finish() override;

                /*
                 * Sets the file path and name of the federate configuration
                 *
                 * <b>NOTE:</b> Path to the federate configuration must be set
                 * when overriding {@link OmnetFederate#initModule}.
                 *
                 * @param fedConfigFilePath path to the federate configuration
                 * @param fedConfigFileName file name of the federate configuration
                 */
                void setFedConfigPath( std::string fedConfigFilePath, std::string fedConfigFileName );

            protected:
                virtual void handleMessage( omnetpp::cMessage *msg ) override;

            private:
                void initialiseFederate( std::string configFilePath );
                void tearDownFederate();
                std::string filePath;
                std::string fileName;
                static base::ucef::NoOpFederate* thisFedarate;
            };
        }
    }
}

#endif /* OMNETFEDERATE_H_ */

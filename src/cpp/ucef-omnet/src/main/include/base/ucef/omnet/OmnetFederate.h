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
                static std::string KEY_HLA_MSG_FILTER;
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

                virtual void receivedInteraction( std::shared_ptr<const HLAInteraction> hlaInt,
                                                  double federateTime ) override;

                //----------------------------------------------------------
                //                    Member methods
                //----------------------------------------------------------
                /*
                 * Sets the file path of the federate configuration
                 *
                 * <b>NOTE:</b> Path to the federate configuration must be configured
                 * inside when initializing modules in {@link OmnetFederate#initModule}.
                 *
                 * @param fedConfigFilePath path to the federate configuration
                 */
                void setFedConfigPath( const std::string &fedConfigFilePath );

            protected:
                //----------------------------------------------------------
                //                     cSimpleModule methods
                //----------------------------------------------------------
                virtual void handleMessage( omnetpp::cMessage *msg ) override;

            private:
                void initializeFederate();
                void tearDownFederate();

            private:
                std::string fedConfigFile;
                static base::ucef::NoOpFederate* thisFedarate;
                std::list<std::string> hlaMsgFilter;
            };
        }
    }
}

#endif /* OMNETFEDERATE_H_ */

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

#ifndef BASE_UCEF_UTIL_MESSAGECODEC_H_
#define BASE_UCEF_UTIL_MESSAGECODEC_H_

#include <memory>
#include <omnetpp.h>


#include "gov/nist/ucef/hla/base/HLAInteraction.h"

namespace base
{
    namespace ucef
    {
        namespace omnet
        {
            namespace util
            {
                class MessageCodec
                {

                public:
                    MessageCodec() = default;
                    virtual ~MessageCodec() = default;

                    /*
                     * Adds interaction parameters to the given cMessage
                     *
                     * @param cMsg message to pack interaction parameters
                     * @param hlaInt interaction to extract parameters from
                     */
                    static void packValues( omnetpp::cMessage* cMsgTo, std::shared_ptr<const base::HLAInteraction> hlaIntFrom );
                    static void packValues( std::shared_ptr<base::HLAInteraction> hlaIntTo, omnetpp::cMessage* cMsgFrom );

                private:
                    // Packs interaction attributes into cMessage
                    static void packValueTypes( omnetpp::cMessage* cMsgTo, std::shared_ptr<const base::HLAInteraction> hlaIntFrom );
                    static void packString( omnetpp::cMessage* cMsgTo, const std::string &key, const std::string &value  );
                    static void packInteger( omnetpp::cMessage* cMsgTo, const std::string &key, long value  );
                    static void packFloat( omnetpp::cMessage* cMsgTo, const std::string &key, double value  );
                    static void packBoolean( omnetpp::cMessage* cMsgTo, const std::string &key, bool value  );
                };
            }
        }
    }
}

#endif /* BASE_UCEF_UTIL_MESSAGECODEC_H_ */

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

#include <omnetpp.h>
#include <memory>

#include "gov/nist/ucef/hla/base/HLAInteraction.h"
#include "base/ucef/omnet/OmnetFederate.h"

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

                    static inline omnetpp::cMessage* toCmessage( std::shared_ptr<const base::HLAInteraction> hlaInt )
                    {
                        std::string interactionName = hlaInt->getInteractionClassName();
                        omnetpp::cMessage* cMsg = new omnetpp::cMessage( hlaInt->getInteractionClassName().c_str() );
                        packValue( hlaInt, cMsg );
                        return cMsg;
                    }

                    // how do you go from cMessage to interaction ???
                    static inline std::shared_ptr<base::HLAInteraction> toInteraction( omnetpp::cMessage* cMsg, const std::string &interactionName )
                    {
                        NoOpFederate* federate = OmnetFederate::getFederatePtr();

                        std::shared_ptr<base::HLAInteraction> interaction = std::make_shared<base::HLAInteraction>( interactionName );

                        for( omnetpp::cArray::Iterator it(cMsg->getParList()); !it.end(); ++it )
                        {
                            omnetpp::cObject *parObject = *it;
                            if( parObject )
                            {
                                omnetpp::cMsgPar *msgPar = dynamic_cast<omnetpp::cMsgPar*>( parObject );
                                if( msgPar )
                                {
                                    std::string paramName = std::string( msgPar->getName()) ;
                                    DataType dataType = federate->getFederateConfiguration()->getDataType( cMsg->getName(), paramName );

                                    if( dataType == DATATYPESTRING )
                                        packValue<std::string>( interaction, paramName, msgPar->stringValue() );
                                    else if( dataType == DATATYPESHORT )
                                        packValue<short>( interaction, paramName, msgPar->longValue() );
                                    else if( dataType == DATATYPEINT )
                                        packValue<int>( interaction, paramName, msgPar->longValue() );
                                    else if( dataType == DATATYPELONG )
                                        packValue<long>( interaction, paramName, msgPar->longValue() );
                                    else if( dataType == DATATYPEFLOAT )
                                        packValue<float>( interaction, paramName, msgPar->doubleValue() );
                                    else if( dataType == DATATYPEDOUBLE  )
                                        packValue<double>( interaction, paramName, msgPar->doubleValue() );
                                    else if( dataType == DATATYPEBOOLEAN)
                                        packValue<bool>( interaction, paramName, msgPar->boolValue() );
                                }
                            }
                        }

                       return interaction;
                    }
                private:

                    // Packs cMessage params into interaction. But this is not logical????
                    template <class T>
                    static void packValue( std::shared_ptr<base::HLAInteraction> &hlaInt, const std::string &key, const T &value  )
                    {
                        hlaInt->setValue(key, value);
                    }

                    // Packs interaction attributes into cMessage
                    static void packValue( std::shared_ptr<const base::HLAInteraction> hlaInt, omnetpp::cMessage* cMsg )
                    {
                        NoOpFederate* federate = OmnetFederate::getFederatePtr();

                        std::vector<std::string> params = hlaInt->getParameterNames();
                        for( auto &paramName : params )
                        {
                            DataType dataType = federate->getFederateConfiguration()->getDataType( hlaInt->getInteractionClassName(), paramName );

                            if( dataType == DATATYPESTRING )
                                packString( cMsg, paramName, hlaInt->getAsString(paramName) );
                            else if( dataType == DATATYPESHORT )
                                packInteger( cMsg, paramName, hlaInt->getAsShort(paramName) );
                            else if( dataType == DATATYPEINT )
                                packInteger( cMsg, paramName, hlaInt->getAsInt(paramName) );
                            else if( dataType == DATATYPELONG )
                                packInteger( cMsg, paramName, hlaInt->getAsLong(paramName) );
                            else if( dataType == DATATYPEFLOAT )
                                packFloat( cMsg, paramName, hlaInt->getAsFloat(paramName) );
                            else if( dataType == DATATYPEDOUBLE  )
                                packFloat( cMsg, paramName, hlaInt->getAsDouble(paramName) );
                            else if( dataType == DATATYPEBOOLEAN)
                                packBoolean( cMsg, paramName, hlaInt->getAsBool(paramName) );
                        }

                    }

                    static void packString( omnetpp::cMessage* cMsg, const std::string &key, const std::string &value  )
                    {
                        omnetpp::cMsgPar& msgPar = cMsg->addPar( key.c_str() );
                        msgPar.setStringValue( value.c_str() );
                    }

                    static void packInteger( omnetpp::cMessage* cMsg, const std::string &key, long value  )
                    {
                        omnetpp::cMsgPar& msgPar = cMsg->addPar( key.c_str() );
                        msgPar.setLongValue( value );
                    }

                    static void packFloat( omnetpp::cMessage* cMsg, const std::string &key, double value  )
                    {
                        omnetpp::cMsgPar& msgPar = cMsg->addPar( key.c_str() );
                        msgPar.setDoubleValue( value );
                    }

                    static void packBoolean( omnetpp::cMessage* cMsg, const std::string &key, bool value  )
                    {
                        omnetpp::cMsgPar& msgPar = cMsg->addPar( key.c_str() );
                        msgPar.setBoolValue( value );
                    }
                };
            }
        }
    }
}

#endif /* BASE_UCEF_UTIL_MESSAGECODEC_H_ */

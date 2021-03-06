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

#include <omnetpp.h>
#include <memory>

#include "base/ucef/omnet/util/MessageCodec.h"
#include "base/ucef/omnet/OmnetFederate.h"
#include "gov/nist/ucef/util/JsonParser.h"

using namespace base;
using namespace base::util;
using namespace omnetpp;
using namespace std;

namespace base
{
    namespace ucef
    {
        namespace omnet
        {
            namespace util
            {

                void MessageCodec::packValues( cMessage* cMsgTo, shared_ptr<const HLAInteraction> hlaIntFrom  )
                {
                    string interactionName = hlaIntFrom->getInteractionClassName();
                    cMsgTo->setName( interactionName.c_str() );
                    packValueTypes( cMsgTo, hlaIntFrom );
                }

                // Find interaction parameters in cMessage and pack them into a interaction
                void MessageCodec::packValues( shared_ptr<HLAInteraction> hlaIntTo, cMessage* cMsgFrom   )
                {
                   bool hasData = cMsgFrom->hasPar( UCEFFederateBase::KEY_NET_DATA.c_str() );
                   if( hasData )
                   {
                       NoOpFederate* federate = OmnetFederate::getFederatePtr();
                       auto fedConfig = federate->getFederateConfiguration();

                       // Payload is a Json string
                       string payLoad = cMsgFrom->par( UCEFFederateBase::KEY_NET_DATA.c_str() ).stringValue();

                       string interactionClassName = hlaIntTo->getInteractionClassName();

                       // Get parameters of this interaction
                       vector<string> params = fedConfig->getParameterNames( interactionClassName );
                       for( auto& param : params )
                       {
                           // Check whether we have this interaction param in msg payload
                           bool present = JsonParser::hasKey( payLoad, param );
                           if( !present ) continue;
                           // Figure out the data type of the parameter
                           DataType dataType = fedConfig->getDataType( interactionClassName, param );

                           // Now extract the parameter value from JSON string
                           if( dataType == DATATYPESTRING )
                           {
                              string value = JsonParser::getValueAsString( payLoad, param  );
                              hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPESHORT )
                           {
                               short value = (short)JsonParser::getValueAsInt( payLoad, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPEINT )
                           {
                               int value = JsonParser::getValueAsInt( payLoad, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPELONG )
                           {
                               long value = JsonParser::getValueAsLong( payLoad, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPEFLOAT )
                           {
                               float value = JsonParser::getValueAsFloat( payLoad, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPEDOUBLE  )
                           {
                               double value = JsonParser::getValueAsDouble( payLoad, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPEBOOLEAN)
                           {
                               bool value = JsonParser::getValueAsBool( payLoad, param );
                               hlaIntTo->setValue( param, value );
                           }
                       }
                   }
                }

                // Packs interaction parameters into a cMessage
                void MessageCodec::packValueTypes( cMessage* cMsgTo, shared_ptr<const HLAInteraction> hlaIntFrom )
                {
                    NoOpFederate* federate = OmnetFederate::getFederatePtr();

                    vector<string> params = hlaIntFrom->getParameterNames();
                    for( auto &paramName : params )
                    {
                        DataType dataType = federate->getFederateConfiguration()->getDataType( hlaIntFrom->getInteractionClassName(), paramName );

                        if( dataType == DATATYPESTRING )
                            packString( cMsgTo, paramName, hlaIntFrom->getAsString(paramName) );
                        else if( dataType == DATATYPESHORT )
                            packInteger( cMsgTo, paramName, hlaIntFrom->getAsShort(paramName) );
                        else if( dataType == DATATYPEINT )
                            packInteger( cMsgTo, paramName, hlaIntFrom->getAsInt(paramName) );
                        else if( dataType == DATATYPELONG )
                            packInteger( cMsgTo, paramName, hlaIntFrom->getAsLong(paramName) );
                        else if( dataType == DATATYPEFLOAT )
                            packFloat( cMsgTo, paramName, hlaIntFrom->getAsFloat(paramName) );
                        else if( dataType == DATATYPEDOUBLE  )
                            packFloat( cMsgTo, paramName, hlaIntFrom->getAsDouble(paramName) );
                        else if( dataType == DATATYPEBOOLEAN)
                            packBoolean( cMsgTo, paramName, hlaIntFrom->getAsBool(paramName) );
                    }

                }

                void MessageCodec::packString( cMessage* cMsgTo, const string &key, const string &value  )
                {
                    cMsgPar& msgPar = cMsgTo->addPar( key.c_str() );
                    msgPar.setStringValue( value.c_str() );
                }

                void MessageCodec::packInteger( cMessage* cMsgTo, const string &key, long value  )
                {
                    cMsgPar& msgPar = cMsgTo->addPar( key.c_str() );
                    msgPar.setLongValue( value );
                }

                void MessageCodec::packFloat( cMessage* cMsgTo, const string &key, double value  )
                {
                    cMsgPar& msgPar = cMsgTo->addPar( key.c_str() );
                    msgPar.setDoubleValue( value );
                }

                void MessageCodec::packBoolean( cMessage* cMsgTo, const string &key, bool value  )
                {
                    cMsgPar& msgPar = cMsgTo->addPar( key.c_str() );
                    msgPar.setBoolValue( value );
                }
            }
        }
    }
}

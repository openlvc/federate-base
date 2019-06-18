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
                    packValue( cMsgTo, hlaIntFrom );
                }

                void MessageCodec::packValues( shared_ptr<HLAInteraction> hlaIntTo, cMessage* cMsgFrom   )
                {
                   bool hasData = cMsgFrom->hasPar("data");
                   if( hasData )
                   {
                       NoOpFederate* federate = OmnetFederate::getFederatePtr();
                       auto fedConfig = federate->getFederateConfiguration();
                       JsonParser& parser = JsonParser::getInstance();

                       string data = cMsgFrom->par("data").stringValue();

                       string interactionClassName = hlaIntTo->getInteractionClassName();
                       vector<string> params = fedConfig->getParameterNames( interactionClassName );
                       for( auto& param : params )
                       {
                           DataType dataType = fedConfig->getDataType( interactionClassName, param );
                           if( dataType == DATATYPESTRING )
                           {
                              string value = parser.getValueAsString( data, param  );
                              hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPESHORT )
                           {
                               short value = (short)parser.getValueAsInt( data, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPEINT )
                           {
                               int value = parser.getValueAsInt( data, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPELONG )
                           {
                               long value = parser.getValueAsLong( data, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPEFLOAT )
                           {
                               float value = parser.getValueAsFloat( data, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPEDOUBLE  )
                           {
                               double value = parser.getValueAsDouble( data, param );
                               hlaIntTo->setValue( param, value );
                           }
                           else if( dataType == DATATYPEBOOLEAN)
                           {
                               bool value = parser.getValueAsBool( data, param );
                               hlaIntTo->setValue( param, value );
                           }

                       }
                   }
                }

                // Packs interaction attributes into cMessage
                void MessageCodec::packValue( cMessage* cMsgTo, shared_ptr<const HLAInteraction> hlaIntFrom )
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

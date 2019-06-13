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

#ifndef OMNETFEDERATESCHEDULER_H_
#define OMNETFEDERATESCHEDULER_H_

#include <omnetpp.h>

namespace base
{
    namespace ucef
    {
        namespace omnet
        {
            class OmnetFederateScheduler : public omnetpp::cSequentialScheduler
            {
            public:
                OmnetFederateScheduler();
                virtual ~OmnetFederateScheduler();
                virtual omnetpp::cEvent *takeNextEvent();
            };
        }
    }
}
#endif /* OMNETFEDERATESCHEDULER_H_ */

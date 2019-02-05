/*
 *  This software is contributed as a public service by
 *  The National Institute of Standards and Technology(NIST)
 *  and is not subject to U.S.Copyright.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files(the "Software"), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify,
 *  merge, publish, distribute, sublicense, and / or sell copies of the
 *  Software, and to permit persons to whom the Software is furnished
 *  to do so, subject to the following conditions :
 *
 *               The above NIST contribution notice and this permission
 *               and disclaimer notice shall be included in all copies
 *               or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.THE AUTHOR
 *  OR COPYRIGHT HOLDERS SHALL NOT HAVE ANY OBLIGATION TO PROVIDE
 *  MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
#pragma once

#include "gov/nist/ucef/hla/ucef/UCEFFederateBase.h"
namespace base
{
	namespace ucef
	{
		class UCEFNullFederate : public UCEFFederateBase
		{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			UCEFNullFederate() = default;
			virtual ~UCEFNullFederate() = default;

		protected:
			//----------------------------------------------------------
			//          Empty Lifecycle hooks
			//----------------------------------------------------------
			void beforeReadyToPopulate() override {}
			void beforeReadyToRun() override {}
			void beforeFirstStep() override {}
			void beforeReadyToResign() override {}
			virtual void beforeExit() override {}
			virtual bool step( double federateTime ) override { return true; }

			//----------------------------------------------------------
			//         Empty Callback Methods
			//----------------------------------------------------------

			virtual void receivedObjectRegistration( std::shared_ptr<const HLAObject> hlaObject,
			                                         double federateTime ) override {}

			virtual void receivedAttributeReflection( std::shared_ptr<const HLAObject> hlaObject,
			                                          double federateTime ) override {}

			virtual void receivedInteraction( std::shared_ptr<const HLAInteraction> hlaInt,
			                                  double federateTime ) override {}

			virtual void receivedObjectDeletion( std::shared_ptr<const HLAObject> hlaObject ) override {}

			virtual void receivedSimStart( std::shared_ptr<const SimStart> hlaInt,
			                               double federateTime ) override {}

			virtual void receivedSimEnd( std::shared_ptr<const SimEnd> hlaInt,
			                             double federateTime ) override {}

			virtual void receivedSimPaused( std::shared_ptr<const SimPause> hlaInt,
			                                double federateTime ) override {}

			virtual void receivedSimResumed( std::shared_ptr<const SimResume> hlaInt,
			                                 double federateTime ) override {}
		};
	}
}
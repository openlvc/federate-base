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
		class NoOpFederate : public UCEFFederateBase
		{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			NoOpFederate() = default;
			virtual ~NoOpFederate() = default;

		protected:
			//----------------------------------------------------------
			//          Empty Lifecycle hooks
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

			//----------------------------------------------------------
			//         Empty Callback Methods
			//----------------------------------------------------------

			/**
			 * Get called whenever RTI discovers a new object instance
			 *
			 * @param hlaObject a simplified representation of the discovered object.
			 *                  At this satge, the discovered object will only have a valid fully qualified
			 *                  name and a unique identifier (@link HLAInteraction#getInstanceId()).
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedObjectRegistration( std::shared_ptr<const HLAObject> hlaObject,
			                                         double federateTime ) override {}

			/**
			 * Get called whenever RTI receives a object class update
			 *
			 * @param hlaObject Stores the received attribute updates relavant to the object class represented by
			 *                  {@link HLAOBject#getClassName()}. Use {@link HLAOBject#getAs***} methods
			 *                  to get the values of the received attribute updates. Since no type checking
			 *                  is carried out it is important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedAttributeReflection( std::shared_ptr<const HLAObject> hlaObject,
			                                          double federateTime ) override {}

			/**
			 * Get called whenever RTI receives a new object interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the interaction class represented
			 *                       by {@link HLAInteraction#getClassName()}. Use {@link HLAInteraction#getAs***}
			 *                       methods to get the values of the received parameter updates. Since no type checking
			 *                       is carried out it is important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedInteraction( std::shared_ptr<const HLAInteraction> hlaInt,
			                                  double federateTime ) override {}

			/**
			 * Get called whenever RTI receives a object deletion call
			 *
			 * @param hlaObject HLAObject the object that has been deleted from the federation. This object contains a
			 *                  valid fully qualified object class name and a unique instance identifier.
			 */
			virtual void receivedObjectDeletion( std::shared_ptr<const HLAObject> hlaObject ) override {}

			/**
			 * Get called whenever RTI receives ucef specific simulation start interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the SimStart interaction.
			 *                       Use {@link HLAInteraction#getAs***} methods to get the values of the
			 *                       received parameter updates. Since no type checking is carried out it is
			 *                       important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimStart( std::shared_ptr<const SimStart> hlaInteraction,
			                               double federateTime ) override {}

			/**
			 * Get called whenever RTI receives ucef specific simulation end interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the SimEnd interaction.
			 *                       Use {@link HLAInteraction#getAs***} methods to get the values of the
			 *                       received parameter updates. Since no type checking is carried out it is
			 *                       important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimEnd( std::shared_ptr<const SimEnd> hlaInteraction,
			                             double federateTime ) override {}

			/**
			 * Get called whenever RTI receives ucef specific simulation pause interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the SimPause interaction.
			 *                       Use {@link HLAInteraction#getAs***} methods to get the values of the
			 *                       received parameter updates. Since no type checking is carried out it is
			 *                       important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimPaused( std::shared_ptr<const SimPause> hlaInteraction,
			                                double federateTime ) override {}

			/**
			 * Get called whenever RTI receives ucef specific simulation resume interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the SimResume interaction.
			 *                       Use {@link HLAInteraction#getAs***} methods to get the values of the
			 *                       received parameter updates. Since no type checking is carried out it is
			 *                       important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimResumed( std::shared_ptr<const SimResume> hlaInteraction,
			                                 double federateTime ) override {}
		};
	}
}
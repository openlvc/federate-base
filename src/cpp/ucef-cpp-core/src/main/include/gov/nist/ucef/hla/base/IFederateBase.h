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

#include <memory>
#include <mutex>

#include "gov/nist/ucef/hla/base/FederateConfiguration.h"
#include "gov/nist/ucef/hla/types.h"
#include "gov/nist/ucef/hla/base/HLAObject.h"
#include "RTIAmbassadorWrapper.h"

namespace base
{
	/**
	 * The {@link IFederateBase} provides a simplified interface to create a functional federate that
	 * can use HLA for distributed simulation.
	 * </p>
	 * There are three main aspects to IFederateBase. First, lifecycle hooks, provide the ability to inject
	 * custom code at the initialisation, execution and resignation of the federate. Secondly,
	 * the callback methods, provide the ability to communicate incoming object and interaction data to users
	 * as they arrive. Finally, helper methods, given for users to query the SOM data which may useful in
	 * object class and interaction class creation and update.
	 */
	class IFederateBase
	{
		public:
			//----------------------------------------------------------
			//                      CONSTRUCTORS
			//----------------------------------------------------------
			IFederateBase() = default;
			virtual ~IFederateBase() {};

		protected:
			//----------------------------------------------------------
			//                     Callback Methods
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
			                                         double federateTime ) = 0;
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
			                                          double federateTime ) = 0;

			/**
			 * Get called whenever RTI receives a new object interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the interaction class represented
			 *                       by {@link HLAInteraction#getClassName()}. Use {@link HLAInteraction#getAs***}
			 *                       methods to get the values of the received parameter updates. Since no type checking
			 *                       is carried out it is important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedInteraction( std::shared_ptr<const HLAInteraction> hlaInteraction,
			                                  double federateTime ) = 0;

			/**
			 * Get called whenever RTI receives a object deletion call
			 *
			 * @param hlaObject HLAObject the object that has been deleted from the federation. This object contains a
			 *                  valid fully qualified object class name and a unique instance identifier.
			 */
			virtual void receivedObjectDeletion( std::shared_ptr<const HLAObject> hlaObject ) = 0;

			//----------------------------------------------------------
			//            Lifecycle hooks
			//----------------------------------------------------------

			/**
			 * Get called just before announcing and achieving the 'READY_TO_POPULATE'
			 * synchronization point
			 */
			virtual void beforeReadyToPopulate() = 0;

			/**
			 * Get called just before announcing and achieving the 'READY_TO_RUN'
			 * synchronization point
			 */
			virtual void beforeReadyToRun() = 0;

			/**
			 * Get called just before entering the main update loop
			 */
			virtual void beforeFirstStep() = 0;

			/**
			 * Get called just before announcing and achieving the 'READY_TO_RESIGN'
			 * synchronization point
			 */
			virtual void beforeReadyToResign() = 0;

			/**
			 * Get called just before resigning from the federation
			 */
			virtual void beforeExit() = 0;

			/**
			 * This method is called inside the main update loop.
			 *
			 * @param federateTime the current logical time of this federate
			 */
			virtual bool step( double federateTime ) = 0;

		public:
			/**
			 * Starts the execution of the federate.
			 * <p/>
			 * This method must be called after creating an instance of
			 * {@link IFederateBase} to start the execution of federate life-cycle.
			 */
			virtual void runFederate() = 0;
			//----------------------------------------------------------
			//            Helper methods
			//----------------------------------------------------------

			/**
			 * Allows to access federate's configuration parameters.
			 *
			 * @return FederateConfiguration that allows to access configuration of this federate.
			 */
			virtual std::shared_ptr<base::FederateConfiguration> getFederateConfiguration() = 0;
	};
}

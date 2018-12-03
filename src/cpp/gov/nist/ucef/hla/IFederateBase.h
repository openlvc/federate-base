#pragma once

#include <memory>
#include <mutex>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"
#include "gov/nist/ucef/hla/HLAObject.h"
#include "RTIAmbassadorWrapper.h"

namespace ucef
{
	/**
	 * The {@link IFederateBase} provides a simplified interface to create a functional federate that
	 * can use HLA for distributed simulation.
	 * </p>
	 * There are three main aspects to IFederateBase. First, lifecycle hooks, provide the ability to inject
	 * custom code at the initialisation, execution and resignation of the federate. Secondly,
	 * the callback methods, provide the ability to communicate incoming object and interaction data to users
	 * as they arrive. Finaly, helper methods, given for users to query the SOM data which may useful in
	 * object class and interaction class creation and update.
	 */
	class UCEF_API IFederateBase
	{
		public:
			//----------------------------------------------------------
			//                      CONSTRUCTORS
			//----------------------------------------------------------
			IFederateBase() = default;
			virtual ~IFederateBase() {};

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
			 *                  is carried out it is important to use the right getters to obtain the right values.
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
			 *                       is carried out it is important to use the right getters to obtain the right values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedInteraction( std::shared_ptr<const HLAInteraction> hlaInteraction,
			                                 double federateTime ) = 0;

			/**
			 * Get called whenever RTI receives a object deletion call
			 *
			 * @param hlaObject HLAObject the object that got deleted from the federation. This object contains a
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

			/**
			 * Starts the execution of the federate.
			 * <p/>
			 * This method must be called after creating an instance of
			 * {@link IFederateBase} to start the life-cycle execution.
			 */
			virtual void runFederate() = 0;

			//----------------------------------------------------------
			//                 SOM data
			//----------------------------------------------------------

			/**
			 * Returns the fully qualified names of the object classes that
			 * are published by this federate.
			 *
			 * @return the names of the publishing object classes
			 */
			virtual std::vector<std::string> getClassNamesPublish() = 0;

			/**
			 * Returns the fully qualified names of the object classes that
			 * are subscribed by this federate.
			 *
			 * @return the names of the subscribed object classes
			 */
			virtual std::vector<std::string> getClassNamesSubscribe() = 0;

			/**
			 * Returns the fully qualified names of the interaction classes that
			 * are published by this federate.
			 *
			 * @return the names of the publishing interaction classes
			 */
			virtual std::vector<std::string> getInteractionNamesPublish() = 0;

			/**
			 * Returns the fully qualified names of the interaction classes that
			 * are subscribed by this federate.
			 *
			 * @return the names of the subscribed interaction classes
			 */
			virtual std::vector<std::string> getInteractionNamesSubscribe() = 0;

			/**
			 * Returns the names of the publishing attributes of the given object class
			 *
			 * @param className the name of the class
			 * @return publishing attributes of the given object class
			 */
			virtual std::vector<std::string> getAttributeNamesPublish( const std::string& className ) = 0;

			/**
			 * Returns the names of the subscribed attributes of the given object class
			 *
			 * @param className the name of the class
			 * @return subscribed attributes of the given object class
			 */
			virtual std::vector<std::string> getAttributeNamesSubscribe( const std::string& className ) = 0;

			/**
			 * Returns the names of the parameters of the given interaction class
			 *
			 * @param interactionName the name of the interaction class
			 */
			virtual std::vector<std::string> getParameterNames( const std::string& interactionName ) = 0;
	};
}


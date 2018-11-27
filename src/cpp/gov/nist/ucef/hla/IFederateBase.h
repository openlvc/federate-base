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
	 * There are three main aspects to IFederateBase. First, lifecycle hooks provide the ability to inject
	 * custom code at the initialisation, execution and resignation phases of the federate. Secondly,
	 * the callback methods provide the ability to communicate incoming object and interaction data to users
	 * as they arrive. Finaly, helper methods are given to the users to query the SOM data which will be
	 * useful in object class and interaction class creation and update.
	 */
	class IFederateBase
	{
		public:
			//----------------------------------------------------------
			//                      CONSTRUCTORS
			//----------------------------------------------------------
			public:
				virtual ~IFederateBase() {};

			//----------------------------------------------------------
			//                     Callback Methods
			//----------------------------------------------------------

			/**
			 * Called by {@link IFederateBase} whenever RTI discovers a new object instance
			 *
			 * @param hlaObject a simplified representation of the discovered object.
			 *                  The discovered object will only have a valid fully qualified
			 *                  name and a unique identifier (@link HLAInteraction#getInstanceId()).
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receiveObjectRegistration( std::shared_ptr<const HLAObject> hlaObject,
			                                        double federateTime ) = 0;
			/**
			 * Called by {@link IFederateBase} whenever RTI receives a new attribute update
			 * of a discovered instance
			 *
			 * @param hlaObject Stores the received attribute data. Use {@link HLAOBject#getAs***} methods
			 *                  to get the updated values of the stored attributes. Since no type checking
			 *                  is carried out it is important to use the right getters to get valid data.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receiveAttributeReflection( std::shared_ptr<const HLAObject> hlaObject,
			                                         double federateTime ) = 0;

			/**
			 * Called by {@link IFederateBase} whenever RTI receives a new object interaction
			 *
			 * @param hlaInteraction Stores the received parameter data. Use {@link HLAInteraction#getAs***}
			 *                       methods to get the updated values of the stored parameters.
			 *                       Since no type checking is carried out it is important to use the right
			 *                       getters to get valid data.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receiveInteraction( std::shared_ptr<const HLAInteraction> hlaInteraction,
			                                 double federateTime ) = 0;

			/**
			 * Called by {@link IFederateBase} whenever RTI receives a object deletion call
			 *
			 * @param hlaObject HLAObject that got deleted from the federation. This object contains a
			 *                  valid object class name and a identifier (@link HLAInteraction#getInstanceId())
			 */
			virtual void receiveObjectDeletion( std::shared_ptr<const HLAObject> hlaObject ) = 0;

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
			 * {@link IFederateBase}.
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
			 * Returns the names of the publishing attributes that are in the given object class
			 *
			 * @param className the name of the class
			 * @return publishing attributes that are in the given object class
			 */
			virtual std::vector<std::string> getAttributeNamesPublish( const std::string& className ) = 0;

			/**
			 * Returns the names of the subscribed attributes that are in the given object class
			 *
			 * @param className the name of the class
			 * @return subscribed attributes that are in the given object class
			 */
			virtual std::vector<std::string> getAttributeNamesSubscribe( const std::string& className ) = 0;

			/**
			 * Returns the names of the parameters that are in the given interaction class
			 *
			 * @param interactionName the name of the interaction class
			 */
			virtual std::vector<std::string> getParameterNames( const std::string& interactionName ) = 0;
	};
}


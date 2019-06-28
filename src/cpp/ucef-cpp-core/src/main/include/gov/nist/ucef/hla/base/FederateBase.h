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
#include <vector>

#include "gov/nist/ucef/hla/base/FederateConfiguration.h"
#include "gov/nist/ucef/hla/base/HLAInteraction.h"
#include "gov/nist/ucef/hla/base/IFederateBase.h"
#include "RTIAmbassadorWrapper.h"

namespace base
{
	class FederateAmbassador;
	/**
	 * The {@link FederateBase} implements some of the {@link IFederateBase} methods to provide
	 * a simplified interface in order to easily create a functional federate that can participate in 
	 * distributed simulation.
	 */
	class FederateBase : public IFederateBase
	{
		public:

			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			FederateBase();
			virtual ~FederateBase();
			FederateBase( const FederateBase& ) = delete;
			FederateBase& operator=(const FederateBase&) = delete;

			//----------------------------------------------------------
			//       IFederateBase interface implementation
			//----------------------------------------------------------
			/**
			 * Starts the execution of the federate.
			 * <p/>
			 * This method must be called after creating an instance of
			 * {@link IFederateBase} to start the execution of federate life-cycle.
			 */
			virtual void runFederate() final;

			/**
			 * Allows to access federate's configuration parameters.
			 *
			 * @return FederateConfiguration that allows to access configuration of this federate.
			 */
			virtual std::shared_ptr<base::FederateConfiguration> getFederateConfiguration() final;

			/**
			 * Determine the current lifecycle state of this federate.
			 *
			 * <b>NOTE:</b> The lifecycle state is managed by the federate itself
			 * and cannot be manually altered.
			 * <p/>
			 * This principally provides a mechanism for federate implementations to differentiate between
			 * the main three cases:
			 * <ol>
			 * <li> {@link LifecycleState#INITIALIZING} during {@link #beforeReadyToPopulate()},
			 *      {@link #beforeReadyToRun()} or {@link #beforeFirstStep()}</li>
			 * <li> {@link LifecycleState#RUNNING} during {@link #step(double)}</li>
			 * <li> {@link LifecycleState#CLEANING_UP} during {@link #beforeReadyToResign()} or
			 *      {@link #beforeExit()}</li>
			 * <li> {@link LifecycleState#EXPIRED} during {@link #beforeExit()} </li>
			 * <li> {@link LifecycleState#LIFE_CYCLE_UNKNOWN} in all other cases
			 * </ol>
			 *
			 * This allows handling of incoming interactions and attribute reflections to be tailored to
			 * the current lifecycle state of the federate.
			 *
			 * @return the current lifecycle state of this federate.
			 *
			 * See {@link LifecycleState} for all possible states.
			 */
			LifecycleState getLifecycleState();

			/**
			 * Returns federate's logical time
			 */
			double getTime();

			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------

			/**
			 * Convenience wrapper to configure a federate from a JSON.
			 * <p/>
			 *
			 * Refer to {@link FederateConfiguration#fromJsonFile(const std::string&)}
	         * for standard configuration JSON keys and data types.
	         *
			 * @param configFilePath path to the federate config json
			 */
			virtual void configureFromJSON( const std::string& configFilePath );

			/**
			 * Called by {@link FederateAmbassador} whenever RTI discovers a new object instance
			 *
			 * @param objectInstanceHash the unique hash of the discovered object instance
			 * @param objectClassHash the unique hash of the discovered object class
			 */
			void incomingObjectRegistration( long objectInstanceHash,
			                                 long objectClassHash );

			/**
			 * Called by {@link FederateAmbassador} whenever RTI receives object instance update
			 * of a discovered object instance
			 *
			 * @param objectInstanceHash the unique hash of the received object instance
			 * @param attributeValues a map that contains attributes and values of the
			 *                        received object instance
			 *
			 * @see #incomingObjectRegistration( long, long )
			 */
			void incomingAttributeReflection
			       ( long objectInstanceHash,
			         const std::map<rti1516e::AttributeHandle, rti1516e::VariableLengthData>& attributeValues );

			/**
			 * Called by {@link FederateAmbassador} whenever RTI receives a new interaction
			 *
			 * @param interactionHash the unique hash of the received object interaction
			 * @param parameterValues a map that holds parameters and values of the
			 *                        received interaction
			 */
			virtual void incomingInteraction
			                    ( long interactionHash,
			                      const std::map<rti1516e::ParameterHandle, rti1516e::VariableLengthData>& parameterValues );

			/**
			 * Called by {@link FederateAmbassador} whenever RTI receives a object deletion call
			 *
			 * @param objectInstanceHash the unique hash of the deleted object instance
			 */
			void incomingObjectDeletion( long objectInstanceHash );

			/**
			 * The main execution of the federate
			 */
			bool execute();

			/**
			 * Determine if this federate is a "late joiner"
			 *
			 * @return true if the federate is a late joiner, false otherwise
			 */
			bool isLateJointer();

		protected:
			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			/**
			 * Returns the interaction class instance mapped to the given hash identifier
			 * of a HLA object interaction class
			 *
			 * @param hash hash identifier of a HLA object interaction class
			 */
			std::shared_ptr<base::InteractionClass> getInteractionClass(long hash);

			/**
			 * Prepare this federate for execution
			 */
			virtual void federateSetup();

			/**
			 * The main execution loop of the federate
			 */
			virtual void federateExecute();

			/**
			 * Teardown this federate
			 */
			virtual void federateTeardown();

			/**
			 * Request permission to advance federate time
			 */
			void advanceTime();

			/**
			 * Announces a synchronization point to the federation
			 *
			 * @param synchPoint the name of the synchronization point to be announced
			 */
			void registerSyncPoint( std::string& synchPoint );

			/**
			 * Achieve the specified synchronization point
			 *
			 * @param synchPoint the name of the synchronization point to be achieved
			 */
			void achieveSynchronization( std::string& synchPoint );

			/**
			 * Checks if the given synchronization point has been achieved by the federation
			 *
			 * @param label synchronization point identifier
			 * @return true if the synchronization point has been achieved by the federation,
			 *         false otherwise
			 */
			bool isAchieved( std::string& synchPoint );

			/**
			 * Populates a given interaction with received parameter values
			 *
			 * @param interactionClassName the name of the interaction class
			 * @param hlaInteraction HLA interaction instance that needs to be
			 *                       populated
			 * @param parameterValues a map that holds parameters and values of the
			 *                        received interaction
			 */
			void populateInteraction( const std::string& interactionClassName,
			                          std::shared_ptr<HLAInteraction>& hlaInteraction,
			                          const std::map<rti1516e::ParameterHandle,
			                          rti1516e::VariableLengthData>& parameterValues );

		private:
			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			void connectToRti();
			void createFederation();
			void joinFederation();
			void enableTimePolicy();
			void disableTimePolicy();
			void publishAndSubscribe();
			void synchronize( base::SynchPoint point );
			void resignAndDestroy();

			/**
			 * Returns the ObjectClass mapped to the given hash identifier of a HLA object
			 * <p/>
			 * If an ObjectClass cannot be found for the given identifier a nullptr is returned.
			 *
			 * @param hash hash identifier of a HLA object class
			 */
			std::shared_ptr<base::ObjectClass> getObjectClassByClassHandle( long hash );

			/**
			 * Returns the ObjectClass mapped to the given hash identifier of a HLA object instance 
			 * <p/>
			 * If an ObjectClass cannot be found for the given identifier a nullptr is returned.
			 *
			 * @param hash hash identifier of a HLA object instance
			 */
			std::shared_ptr<base::ObjectClass> getObjectClassByInstanceHandle( long hash );

			/**
			 * Updates incoming data store
			 *
			 * @param hash hash identifier of a HLA object class
			 */
			bool deleteIncomingInstanceHandle( long hash );

			/**
			 * Cache ObjectClasses for faster access
			 *
			 * @param objectClasses ObjectClasses as in SOM file
			 */
			inline void storeObjectClassData( std::vector<std::shared_ptr<base::ObjectClass>>& objectClasses );

			/**
			 * Cache InteractionClasses for faster access
			 *
			 * @param interactionClasses InteractionClasses as in SOM file
			 */
			inline void storeInteractionClassData( std::vector<std::shared_ptr<base::InteractionClass>>& interactionClasses);

			/**
			 * Register publishing attributes with RTI
			 *
			 * @param objectClasses ObjectClasses as in SOM file
			 */
			inline void publishObjectClassAttributes( std::vector<std::shared_ptr<base::ObjectClass>>& objectClasses );

			/**
			 * Register subscribing attributes with RTI
			 *
			 * @param objectClasses ObjectClasses as in SOM file
			 */
			inline void subscribeObjectClassAttributes
			                     ( std::vector<std::shared_ptr<base::ObjectClass>>& objectClasses );

			/**
			 * Register publishing interactions with RTI
			 *
			 * @param interactionClasses InteractionClasses as in SOM file
			 */
			inline void publishInteractionClasses
			                     ( std::vector<std::shared_ptr<base::InteractionClass>>& interactionClasses );

			/**
			 * Register subscribing interactions with RTI
			 *
			 * @param interactionClasses InteractionClasses as in SOM file
			 */
			inline void subscribeInteractionClasses
			                     ( std::vector<std::shared_ptr<base::InteractionClass>>& interactionClasses );

			/**
			 * Ticks RTI for callbacks
			 * <p/>
			 * If HLA_IMMEDIATE callback mechanism is used the main thread will be blocked
			 * for a fixed time; while explicit ticking is used in HLA_EVOKED mode.
			 */
			inline void tickForCallBacks();

		protected:
			std::unique_ptr<RTIAmbassadorWrapper> rtiAmbassadorWrapper;
			std::shared_ptr<FederateAmbassador> federateAmbassador;
			std::shared_ptr<base::FederateConfiguration> ucefConfig;
			std::mutex threadSafeLock;

		private:
			base::ObjectDataStoreByHash objectDataStoreByHash;
			base::InteractionDataStoreByHash interactionDataStoreByHash;
			base::ObjectDataStoreByInstance objectDataStoreByInstance;
			LifecycleState lifecycleState;
			std::set<SynchPoint> syncPointTimeouts;

	};
}

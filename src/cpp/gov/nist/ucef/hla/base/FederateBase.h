#pragma once

#include <memory>
#include <mutex>
#include <vector>

#include "gov/nist/ucef/hla/base/FederateConfiguration.h"
#include "gov/nist/ucef/hla/base/HLAObject.h"
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
	class UCEF_API FederateBase : public IFederateBase
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
			virtual void runFederate() final;
			virtual std::shared_ptr<base::FederateConfiguration> getFederateConfiguration() final;

			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
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
			 * @param parameterValues a map that contains parameters and values of the
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

		protected:
			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			/**
			 * Returns an Interactionclass mapped to the given hash identifier
			 * of a HLA object interaction class
			 *
			 * @param hash hash identifier of a HLA object interaction class
			 */
			std::shared_ptr<base::InteractionClass> getInteractionClass(long hash);

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
			void advanceTime();
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
			 * for a fixed time; while explicit tikcing is used in HLA_EVOKED mode.
			 *
			 */
			inline void tickForCallBacks();

		protected:
			std::unique_ptr<RTIAmbassadorWrapper> m_rtiAmbassadorWrapper;
			std::shared_ptr<FederateAmbassador> m_federateAmbassador;
			std::mutex m_threadSafeLock;

		private:
			base::ObjectDataStoreByHash m_objectDataStoreByHash;
			base::InteractionDataStoreByHash m_interactionDataStoreByHash;
			base::ObjectDataStoreByInstance m_objectDataStoreByInstance;
			std::shared_ptr<base::FederateConfiguration> m_ucefConfig;
	};
}
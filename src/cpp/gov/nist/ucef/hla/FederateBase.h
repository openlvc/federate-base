#pragma once


#include <memory>
#include <mutex>
#include <vector>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"
#include "gov/nist/ucef/hla/HLAObject.h"
#include "gov/nist/ucef/hla/IFederateBase.h"
#include "RTIAmbassadorWrapper.h"

namespace ucef
{

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
			virtual std::vector<std::string> getClassNamesPublish() override;
			virtual std::vector<std::string> getClassNamesSubscribe() override;
			virtual std::vector<std::string> getInteractionNamesSubscribe() override;
			virtual std::vector<std::string> getInteractionNamesPublish() override;

			virtual std::vector<std::string> getAttributeNamesPublish( const std::string& className ) override;
			virtual std::vector<std::string> getAttributeNamesSubscribe( const std::string& className ) override;
			virtual std::vector<std::string> getParameterNames( const std::string& interactionName ) override;

			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			/**
			 * Called by {@link FederateAmbassador} whenever RTI discovers a new object instance
			 *
			 * @param objectInstanceHash a unique hash of the discovered object instance
			 * @param objectClassHash the unique hash of the discovered object class
			 */
			void incomingObjectRegistration( long objectInstanceHash,
			                                 long objectClassHash );

			/**
			 * Called by {@link FederateAmbassador} whenever RTI receives object instance update
			 * of a discovered instance
			 *
			 * @param objectInstanceHash a unique hash of the discovered object instance
			 * @param attributeValues a map that contains attributes and the their values
			 */
			void incomingAttributeReflection
			       ( long objectInstanceHash,
			         const std::map<rti1516e::AttributeHandle, rti1516e::VariableLengthData>& attributeValues );

			/**
			 * Called by {@link FederateAmbassador} whenever RTI receives a new interaction
			 *
			 * @param interactionHash a unique hash of the received object interaction
			 * @param parameterValues a map that contains parameters and the their values
			 */
			void incomingInteraction
			       ( long interactionHash,
			         const std::map<rti1516e::ParameterHandle, rti1516e::VariableLengthData>& parameterValues );

			/**
			 * Called by {@link FederateAmbassador} whenever RTI receives a object deletion call
			 *
			 * @param objectInstanceHash a unique hash of the deleted object instance
			 */
			void incomingObjectDeletion( long objectInstanceHash );

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
			void synchronize( util::SynchPoint point );
			void advanceTime();
			void resignAndDestroy();

			/**
			 * Returns a object class that matches the provided hash identifier of a object class
			 *
			 * @param hash hash identifier of a object class
			 */
			std::shared_ptr<util::ObjectClass> getObjectClassByClassHandle( long hash );

			/**
			 * Returns a object class that matches the provided hash identifier of a object instance
			 *
			 * @param hash hash identifier of a object class
			 */
			std::shared_ptr<util::ObjectClass> getObjectClassByInstanceHandle( long hash );

			/**
			 * Deletes an entry in object data store (by instance) that matches the provided hash
			 * identifier of a object instance
			 *
			 * @param hash hash identifier of a object class
			 */
			bool deleteIncomingInstanceHandle( long hash );

			/**
			 * Returns a interaction class that matches the provided a hash identifier
			 *
			 * @param hash hash identifier of a object class
			 */
			std::shared_ptr<util::InteractionClass> getInteractionClass( long hash );

			/**
			 * Cache information about the object classes in SOM file
			 *
			 * @param objectClasses object classes in the SOM
			 */
			inline void storeObjectClassData( std::vector<std::shared_ptr<util::ObjectClass>>& objectClasses );

			/**
			 * Cache information about the interaction classes in SOM file
			 *
			 * @param intClasses interaction classes in the SOM
			 */
			inline void storeInteractionClassData( std::vector<std::shared_ptr<util::InteractionClass>>& intClasses );

			/**
			 * Register publishing attributes with RTI
			 *
			 * @param objectClasses object classes in the SOM
			 */
			inline void publishObjectClassAttributes( std::vector<std::shared_ptr<util::ObjectClass>>& objectClasses );

			/**
			 * Register subscribing attributes with RTI
			 *
			 * @param objectClasses object classes in the SOM
			 */
			inline void subscribeObjectClassAttributes
			                     ( std::vector<std::shared_ptr<util::ObjectClass>>& objectClasses );

			/**
			 * Register publishing interactions with RTI
			 *
			 * @param intClasses interaction classes in the SOM
			 */
			inline void publishInteractionClasses
			                     ( std::vector<std::shared_ptr<util::InteractionClass>>& interactionClass );

			/**
			 * Register subscribing interactions with RTI
			 *
			 * @param intClasses interaction classes in the SOM
			 */
			inline void subscribeInteractionClasses
			                     ( std::vector<std::shared_ptr<util::InteractionClass>>& interactionClass );

			/**
			 * Ticks RTI for callbacks
			 * <p/>
			 * If HLA_IMMEDIATE callback mechanism is used the main thread will be blocked
			 * for 10 milliseconds while explicit tikcing is used in HLA_EVOKED mode.
			 *
			 */
			inline void tickForCallBacks();

		protected:
			std::unique_ptr<RTIAmbassadorWrapper> m_rtiAmbassadorWrapper;

		private:
			util::ObjectDataStoreByName m_objectDataStoreByName;
			util::InteractionDataStoreByName m_interactionDataStoreByName;
			std::shared_ptr<FederateAmbassador> m_federateAmbassador;
			util::ObjectDataStoreByHash m_objectDataStoreByHash;
			util::InteractionDataStoreByHash m_interactionDataStoreByHash;
			util::ObjectDataStoreByInstance m_objectDataStoreByInstance;
			std::shared_ptr<util::FederateConfiguration> m_ucefConfig;
			std::mutex m_threadSafeLock;
	};
}


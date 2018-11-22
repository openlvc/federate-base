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
			//                    Instance Methods
			//----------------------------------------------------------
			void incomingObjectRegistration( long objectInstanceHash,
			                                 long objectClassHash );
			void incomingAttributeReflection
			       ( long objectInstanceHash,
			         const std::map<rti1516e::AttributeHandle, rti1516e::VariableLengthData>& attributeValues );
			void incomingInteraction
			       ( long interactionHash,
			         const std::map<rti1516e::ParameterHandle, rti1516e::VariableLengthData>& parameterValues );
			void incomingObjectDeletion( long objectInstanceHash );
			virtual void runFederate() final;

		private:
			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			void connectToRti();
			void createFederation();
			void joinFederation();
			void enableTimePolicy();
			void publishAndSubscribe();
			void synchronize( util::SynchPoint point );
			void advanceLogicalTime();
			void resignAndDestroy();

			std::shared_ptr<util::ObjectClass> getObjectClassByClassHandle( long hash );
			std::shared_ptr<util::ObjectClass> getObjectClassByInstanceHandle( long hash );
			size_t deleteIncomingInstanceHandle( long hash );
			std::shared_ptr<util::InteractionClass> getInteractionClass( long hash );
			inline void storeObjectClassData( std::vector<std::shared_ptr<util::ObjectClass>>& objectClasses);
			inline void storeInteractionClassData( std::vector<std::shared_ptr<util::InteractionClass>>& intClasses);
			inline void pubSubAttributes();
			inline void pubSubInteractions();
			inline void tickForCallBacks();

		protected:
			util::ObjectDataStoreByName m_objectDataStoreByName;
			util::InteractionDataStoreByName m_interactionDataStoreByName;
			std::unique_ptr<RTIAmbassadorWrapper> m_rtiAmbassadorWrapper;

		private:
			std::shared_ptr<FederateAmbassador> m_federateAmbassador;
			util::ObjectDataStoreByHash m_objectDataStoreByHash;
			util::InteractionDataStoreByHash m_interactionDataStoreByHash;
			util::ObjectDataStoreByInstance m_objectDataStoreByInstance;
			std::shared_ptr<util::FederateConfiguration> m_ucefConfig;
			std::mutex m_threadSafeLock;
	};
}


#pragma once

#include <memory>
#include <mutex>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"
#include "gov/nist/ucef/hla/HLAObject.h"
#include "RTIAmbassadorWrapper.h"

namespace ucef
{
	class UCEF_API FederateBase
	{
		public:

			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			FederateBase();
			virtual ~FederateBase();
			FederateBase( const FederateBase& ) = delete;
			FederateBase& operator=(const FederateBase&) = delete;

			virtual void receiveObjectRegistration( std::shared_ptr<HLAObject>& hlaObject,
			                                        double federateTime );
			virtual void receiveAttributeReflection( std::shared_ptr<const HLAObject>& hlaObject,
			                                         double federateTime );
			virtual void receiveInteraction( std::shared_ptr<const HLAInteraction>& hlaInteraction,
			                                 double federateTime );
			void objectDelete( std::shared_ptr<HLAObject>& hlaObject );
			std::shared_ptr<util::ObjectClass> getObjectClass( long hash );
			std::shared_ptr<util::ObjectClass> getObjectClass( std::string name );
			std::shared_ptr<HLAObject> findIncomingObject( long hash );

			std::shared_ptr<util::InteractionClass> getInteractionClass( long hash );
			virtual void runFederate();

		protected:
			//----------------------------------------------------------
			//            Lifecycle hooks and callback methods
			//----------------------------------------------------------
			virtual void beforeFederationCreate() {};
			virtual void beforeFederateJoin()  {};
			virtual void beforeReadyToRun() {};
			virtual void afterReadyToRun() {};
			virtual void beforeReadyToResign() {};
			virtual void afterDeath() {};
			virtual bool step(double federateTime) = 0;
		protected:
			//----------------------------------------------------------
			//                    Protected members
			//----------------------------------------------------------
			std::unique_ptr<RTIAmbassadorWrapper> m_rtiAmbassadorWrapper;
			util::ObjectCacheStoreByName m_objectCacheStoreByName;
			util::InteractionCacheStoreByName m_interactionCacheStoreByName;

		private:
			std::shared_ptr<util::FederateConfiguration> m_ucefConfig;
			util::ObjectCacheStoreByHash m_objectCacheStoreByHash;
			util::InteractionClassStoreByHash m_interactionCacheStoreByHash;
			util::IncomingStore m_incomingStore;

			//----------------------------------------------------------
			//            Federate life-cycle calls
			//----------------------------------------------------------
			void connectToRti();
			void createFederation();
			void joinFederation();
			void enableTimePolicy();
			void publishAndSubscribe();
			void synchronize( util::SynchPoint point );
			void advanceLogicalTime();
			void resignAndDestroy();

		private:
			//----------------------------------------------------------
			//                    Business Logic
			//----------------------------------------------------------
			inline void cacheHandles( std::vector<std::shared_ptr<util::ObjectClass>>& objectClasses);
			inline void pubSubAttributes();
			inline void cacheHandles( std::vector<std::shared_ptr<util::InteractionClass>>& interactionClasses);
			inline void pubSubInteractions();
			inline void tickForCallBacks();
			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			std::shared_ptr<FederateAmbassador> m_federateAmbassador;
			std::mutex m_threadSafeLock;
	};
}


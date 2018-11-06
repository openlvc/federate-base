#pragma once

#include <memory>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"
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

			//----------------------------------------------------------
			//            Lifecycle hooks and callback methods
			//----------------------------------------------------------
			virtual void runFederate();
			virtual void beforeFederationCreate() {};
			virtual void beforeFederateJoin()  {};
			virtual void beforeReadyToRun() {};
			virtual void beforeReadyToResign() {};
			virtual bool step() = 0;
			void setResign(bool resign);
		private:

			//----------------------------------------------------------
			//            Federate life-cycle calls
			//----------------------------------------------------------
			void connectToRti();
			void createFederation();
			void joinFederation();
			void synchronize( util::SynchPoint point );
			void enableTimePolicy();
			void publishAndSubscribe();
			void resign();
			void advanceLogicalTime();
		private:
			inline void tick();
			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			bool m_resign;
			std::unique_ptr<RTIAmbassadorWrapper> m_rtiAmbassadorWrapper;
			std::shared_ptr<util::FederateConfiguration> m_ucefConfig;
			std::shared_ptr<FederateAmbassador> m_federateAmbassador;
	};
}


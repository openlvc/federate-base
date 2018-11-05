#pragma once

#include <memory>

#include "gov/nist/ucef/version.h"
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
			virtual void beforeFederationJoin()  {};
			virtual void beforeReadyToRun() {};

		private:

			//----------------------------------------------------------
			//            Federate life-cycle calls
			//----------------------------------------------------------
			void createRtiAmbassador();
			void createFederation();
			void joinFederation();
			void synchronize( util::SynchPoint point );
			void enableTimePolicy();
			void publishAndSubscribe();
		private:

			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			std::unique_ptr<RTIAmbassadorWrapper> m_rtiAmbassadorWrapper;
	};
}


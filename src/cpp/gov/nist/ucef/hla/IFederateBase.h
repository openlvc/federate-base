#pragma once

#include <memory>
#include <mutex>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"
#include "gov/nist/ucef/hla/HLAObject.h"
#include "RTIAmbassadorWrapper.h"

namespace ucef
{
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
			virtual void receiveObjectRegistration( std::shared_ptr<const HLAObject>& hlaObject,
			                                        double federateTime ) = 0;
			virtual void receiveAttributeReflection( std::shared_ptr<const HLAObject>& hlaObject,
			                                         double federateTime ) = 0;
			virtual void receiveInteraction( std::shared_ptr<const HLAInteraction>& hlaInteraction,
			                                 double federateTime ) = 0;
			virtual void receiveObjectDeletion( std::shared_ptr<const HLAObject>& hlaObject ) = 0;

			//----------------------------------------------------------
			//            Lifecycle hooks
			//----------------------------------------------------------
			virtual void beforeFederationCreate() = 0;
			virtual void beforeFederateJoin() = 0;
			virtual void beforeReadyToRun() = 0;
			virtual void afterReadyToRun() = 0;
			virtual void beforeReadyToResign() = 0;
			virtual void afterDeath() = 0;
			virtual bool step(double federateTime) = 0;
			virtual void runFederate() = 0;
	};
}


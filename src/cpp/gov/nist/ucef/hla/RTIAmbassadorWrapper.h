#pragma once

#include <memory>

#include "gov/nist/ucef/version.h"
#include "gov/nist/ucef/util/types.h"

namespace rti1516e
{
	class RTIambassador;
}

namespace ucef
{
	class FederateAmbassador;
	namespace util
	{
		class FederateConfiguration;
	}

	class UCEF_API RTIAmbassadorWrapper
	{
		// unordered_map because we do not need any ordering,
		// what we need is a faster way to get the object class
		typedef std::unordered_map<std::string, std::shared_ptr<util::ObjectClass>> ObjectClassMap;
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			RTIAmbassadorWrapper();
			virtual ~RTIAmbassadorWrapper();
			RTIAmbassadorWrapper( const RTIAmbassadorWrapper& ) = delete;

		public:
			//----------------------------------------------------------
			//            Federate life-cycle calls
			//----------------------------------------------------------
			inline void createRtiAmbassador();
			inline void createFederation();
			inline void joinFederation();
			inline void synchronize( util::SynchPoint point );
			inline void enableTimePolicy();
			inline void publishAndSubscribe();
			inline void resign();
			inline void advanceLogicalTime();
		private:
			//----------------------------------------------------------
			//             Private methods
			//----------------------------------------------------------
			inline void announceSynchronizationPoint( util::SynchPoint point );
			inline void achieveSynchronizationPoint( util::SynchPoint point );
			inline void enableTimeRegulated();
			inline void enableTimeConstrained();
			inline void initialiseClassHandles();
			inline void initialiseInstanceHandles();
			inline void publishSubscribeObjectClassAttributes();
			inline void publishSubscribeInteractionClasses();
			inline void tick();

		private:
			//----------------------------------------------------------
			//             Private members
			//----------------------------------------------------------
			std::shared_ptr<FederateAmbassador> m_federateAmbassador;
			std::unique_ptr<util::FederateConfiguration> m_ucefConfig;
			ObjectClassMap objectClassMap;
			std::unique_ptr<rti1516e::RTIambassador> m_rtiAmbassador;
	};
}


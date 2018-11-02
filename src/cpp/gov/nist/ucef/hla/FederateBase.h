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

	class UCEF_API FederateBase
	{
		// unordered_map because we do not need any ordering,
		// what we need is a faster way to get the object class
		typedef std::unordered_map<std::string, std::shared_ptr<util::ObjectClass>> ObjectClassMap;

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
			inline void createRtiAmbassador();
			inline void createFederation();
			inline void joinFederation();
			inline void initialiseHandles();
			inline void synchronize( util::SynchPoint point );
			inline void enableTimePolicy( );

			//----------------------------------------------------------
			//             Instance Methods
			//----------------------------------------------------------
			inline void announceSynchronizationPoint( util::SynchPoint point );
			inline void achieveSynchronizationPoint( util::SynchPoint point );
			inline void enableTimeRegulated();
			inline void enableTimeConstrained();
		private:

			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			std::shared_ptr<FederateAmbassador> m_federateAmbassador;
			std::unique_ptr<rti1516e::RTIambassador> m_rtiAmbassador;
			std::unique_ptr<util::FederateConfiguration> m_ucefConfig;
			ObjectClassMap objectClassMap;
	};
}


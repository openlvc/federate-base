#pragma once

#include <memory>

#include "gov/nist/ucef/config.h"
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
			void connect( std::shared_ptr<FederateAmbassador>& federateAmbassador,
			               const shared_ptr<FederateConfiguration>& config );
			void createFederation( const shared_ptr<FederateConfiguration>& config );
			void joinFederation( const shared_ptr<FederateConfiguration>& config );
			void enableTimeRegulated( const shared_ptr<FederateConfiguration>& config );
			void enableTimeConstrained( const shared_ptr<FederateConfiguration>& config );

			void synchronize( util::SynchPoint point );

			void publishAndSubscribe();
			void resign();
			void advanceLogicalTime();
			void tickForCallBacks(double min, double max);

		private:
			//----------------------------------------------------------
			//             Private methods
			//----------------------------------------------------------
			inline void announceSynchronizationPoint( util::SynchPoint point );
			inline void achieveSynchronizationPoint( util::SynchPoint point );
			inline void initialiseClassHandles();
			inline void initialiseInstanceHandles();
			inline void publishSubscribeObjectClassAttributes();
			inline void publishSubscribeInteractionClasses();

	private:
			//----------------------------------------------------------
			//             Private members
			//----------------------------------------------------------
			ObjectClassMap objectClassMap;
			std::unique_ptr<rti1516e::RTIambassador> m_rtiAmbassador;
	};
}


#pragma once

#include <set>
#include <string>
#include <mutex>

#include "RTI/Exception.h"
#include "RTI/NullFederateAmbassador.h"
#include "RTI/Typedefs.h"
#include "RTI/VariableLengthData.h"
#include "RTI/Exception.h"

namespace ucef
{
	class FederateAmbassador : public rti1516e::NullFederateAmbassador
	{

		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			FederateAmbassador();
			virtual ~FederateAmbassador() throw();
			FederateAmbassador(const FederateAmbassador&) = delete;

			//----------------------------------------------------------
			//            Federate Ambassador Callbacks
			//----------------------------------------------------------
			virtual void announceSynchronizationPoint( const std::wstring& label,
			                                           const rti1516e::VariableLengthData& tag )
			                                                            throw( rti1516e::FederateInternalError );
			virtual void federationSynchronized( const std::wstring& label,
			                                     const rti1516e::FederateHandleSet& failedSet )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void timeRegulationEnabled( const rti1516e::LogicalTime& theFederateTime )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void timeConstrainedEnabled( const rti1516e::LogicalTime& theFederateTime )
			                                                            throw( rti1516e::FederateInternalError );

			virtual void timeAdvanceGrant( const rti1516e::LogicalTime& theFederateTime )
			                                                            throw( rti1516e::FederateInternalError );

			//----------------------------------------------------------
			//             Federate Access Methods
			//----------------------------------------------------------
			bool isAnnouncedSynchPoint( std::wstring& announcedPoint );
			bool isAchievedSynchPoint( std::wstring& achievedPoint );
			bool isRegulating();
			bool isConstrained();
			bool isAdvancing();
			double getFederateTime();

			//----------------------------------------------------------
			//             Instance Methods
			//----------------------------------------------------------
			double convertTime( const rti1516e::LogicalTime& theTime );
		private:
			std::set<std::wstring> announcedSynchPoints;
			std::set<std::wstring> achievedSynchPoints;
			bool m_regulating;
			bool m_constrained;
			bool m_advancing;
			double m_federateTime;

			std::mutex threadSafeLock;
	};
}

#pragma once

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
			//            FederateAmbassador Callbacks
			//----------------------------------------------------------
			virtual void announceSynchronizationPoint( const std::wstring& label,
			                                           const rti1516e::VariableLengthData& tag )
			                                                            throw( rti1516e::FederateInternalError );
			virtual void federationSynchronized( const std::wstring& label,
			                                     const rti1516e::FederateHandleSet& failedSet )
			                                                            throw( rti1516e::FederateInternalError );

			//----------------------------------------------------------
			//             Federate Access Methods
			//----------------------------------------------------------
			std::wstring getAnnouncedSynchPoint();
			std::wstring getAchievedSynchPoint();

		private:
			std::wstring announcedSynchPoint;
			std::mutex synchAnnounceLock;

			std::wstring synchAchievedPoint;
			std::mutex synchAchievedLock;
	};
}

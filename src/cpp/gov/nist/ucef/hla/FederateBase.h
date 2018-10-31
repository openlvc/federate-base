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
		class UCEFConfig;
	}

	class UCEF_API FederateBase
	{

		public:

			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			FederateBase(std::wstring& federateName);
			virtual ~FederateBase();
			FederateBase(const FederateBase&) = delete;

		private:

			//----------------------------------------------------------
			//            RTI init methods
			//----------------------------------------------------------
			inline void initialiseRti();
			inline void initialiseFederation();
			inline void initialiseHandles();

		private:

			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------
			std::shared_ptr<FederateAmbassador> m_federateAmbassador;
			std::unique_ptr<rti1516e::RTIambassador> m_rtiAmbassador;
			std::unique_ptr<util::UCEFConfig> m_ucefConfig;
	};
}


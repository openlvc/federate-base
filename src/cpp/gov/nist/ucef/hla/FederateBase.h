#pragma once

#include <memory>

#include "FederateAmbassador.h"
#include "gov/nist/ucef/util/types.h"

#include "RTI/RTIambassador.h"

namespace ucef
{
	class FederateBase
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

			std::wstring m_federateName;
			FederateAmbassador m_federateAmbassador;
			std::unique_ptr<rti1516e::RTIambassador> m_rtiAmbassador;
	};

}


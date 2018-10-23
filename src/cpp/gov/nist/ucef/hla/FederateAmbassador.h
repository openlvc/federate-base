#pragma once

#include "RTI/Exception.h"
#include "RTI/NullFederateAmbassador.h"
#include "RTI/Typedefs.h"

namespace ucef
{
	class FederateAmbassador : public rti1516e::NullFederateAmbassador
	{

		public:

			////////////////////////////////
			//        Constructors        //
			////////////////////////////////
			FederateAmbassador();
			virtual ~FederateAmbassador() throw();

	};

}


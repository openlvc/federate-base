#pragma once

#ifdef _WIN32
#include <functional>
#endif

#include "gov/nist/ucef/hla/base/HLAInteraction.h"

namespace base
{
	namespace ucef
	{
		/**
		 * The {@link UCEFInteraction} is the base class for UCEF specific interactions
		 */
		class UCEF_API UCEFInteraction : public HLAInteraction
		{
		public:
			static const std::string UCEF_INTERACTION_ROOT;
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			UCEFInteraction( const std::string& interactionClassName );
			virtual ~UCEFInteraction();
			UCEFInteraction( const UCEFInteraction&) = delete;
			UCEFInteraction& operator=( const UCEFInteraction& ) = delete;
		};
	}
}
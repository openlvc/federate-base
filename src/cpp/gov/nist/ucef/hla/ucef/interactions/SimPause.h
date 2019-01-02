#pragma once

#ifdef _WIN32
#include <functional>
#endif

#include "gov/nist/ucef/hla/ucef/interactions/UCEFInteraction.h"

namespace base
{
	namespace ucef
	{
		/**
		 * The {@link SimPause} is a UCEF specific interaction
		 * to inform simulation pause event.
		 */
		class UCEF_API SimPause : public UCEFInteraction
		{
		public:
			static const std::string INTERACTION_NAME;
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			SimPause( const std::string& interactionClassName );
			virtual ~SimPause();
			SimPause( const SimPause& ) = delete;
			SimPause& operator=( const SimPause& ) = delete;
		};
	}
}


#pragma once

#ifdef _WIN32
#include <functional>
#endif

#include "UCEFInteraction.h"

namespace base
{
	namespace ucef
	{
		/**
		 * The {@link SimResume} is a UCEF specific interaction
		 * to inform simulation resume event.
		 */
		class UCEF_API SimResume : public UCEFInteraction
		{
		public:
			static const std::string INTERACTION_NAME;
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			SimResume( const std::string& interactionClassName );
			virtual ~SimResume();
			SimResume( const SimResume& ) = delete;
			SimResume& operator=( const SimResume& ) = delete;
		};
	}
}


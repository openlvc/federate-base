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
		 * The {@link SimEnd} is a UCEF specific interaction
		 * to inform simulation end event.
		 */
		class UCEF_API SimEnd : public UCEFInteraction
		{
		public:
			static const std::string INTERACTION_NAME;
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			SimEnd( const std::string& interactionClassName );
			virtual ~SimEnd();
			SimEnd( const SimEnd& ) = delete;
			SimEnd& operator=( const SimEnd& ) = delete;
		};
	}
}


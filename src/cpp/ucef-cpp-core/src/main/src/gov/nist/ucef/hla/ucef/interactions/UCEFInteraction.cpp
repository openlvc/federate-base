#include "gov/nist/ucef/hla/ucef/interactions/UCEFInteraction.h"

using namespace std;

namespace base
{
	namespace ucef
	{

		UCEFInteraction::UCEFInteraction( const string& interactionClassName ) : HLAInteraction( interactionClassName )
		{

		}

		UCEFInteraction::~UCEFInteraction()
		{

		}
		const std::string UCEFInteraction::UCEF_INTERACTION_ROOT = "InteractionRoot.C2WInteractionRoot.";
	}
}

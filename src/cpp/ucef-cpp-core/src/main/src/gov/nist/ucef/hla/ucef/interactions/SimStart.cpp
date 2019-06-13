#include "gov/nist/ucef/hla/ucef/interactions/SimStart.h"

using namespace std;

namespace base
{
	namespace ucef
	{

		SimStart::SimStart( const string& interactionClassName ) : UCEFInteraction( interactionClassName )
		{
		}

		SimStart::~SimStart()
		{
		}
		const std::string SimStart::INTERACTION_NAME = UCEFInteraction::UCEF_INTERACTION_ROOT + "SimStart";
	}
}

#include "SimResume.h"

using namespace std;

namespace base
{
	namespace ucef
	{

		SimResume::SimResume( const string& interactionClassName ) : UCEFInteraction( interactionClassName )
		{
		}

		SimResume::~SimResume()
		{
		}
		const std::string SimResume::INTERACTION_NAME = UCEFInteraction::UCEF_INTERACTION_ROOT + "SimResume";
	}
}

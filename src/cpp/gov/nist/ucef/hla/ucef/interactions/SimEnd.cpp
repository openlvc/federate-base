#include "SimEnd.h"

using namespace std;

namespace base
{
	namespace ucef
	{

		SimEnd::SimEnd( const string& interactionClassName ) : UCEFInteraction( interactionClassName )
		{
		}

		SimEnd::~SimEnd()
		{
		}
		const std::string SimEnd::INTERACTION_NAME = UCEFInteraction::UCEF_INTERACTION_ROOT + "SimEnd";
	}
}

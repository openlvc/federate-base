#include "SimPause.h"

using namespace std;

namespace base
{
	namespace ucef
	{

		SimPause::SimPause( const string& interactionClassName ) : UCEFInteraction( interactionClassName )
		{
		}

		SimPause::~SimPause()
		{
		}
		const std::string SimPause::INTERACTION_NAME = UCEFInteraction::UCEF_INTERACTION_ROOT + "SimPause";
	}
}

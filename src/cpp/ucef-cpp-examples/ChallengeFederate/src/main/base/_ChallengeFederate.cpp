#include "_ChallengeFederate.h"
using namespace std;
using namespace base;

void _ChallengeFederate::receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
                                              double federateTime )
{
	ResponseInteraction response( hlaInt );
	receivedResponseInteraction( response );
}




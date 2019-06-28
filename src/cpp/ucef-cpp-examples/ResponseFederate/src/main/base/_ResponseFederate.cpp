#include "_ResponseFederate.h"
using namespace std;
using namespace base;

void _ResponseFederate::receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
                                             double federateTime )
{
	ChallengeInteraction challengeInt( hlaInt );
	receivedChallengeInteraction( challengeInt );
}


void _ResponseFederate::receivedAttributeReflection( shared_ptr<const HLAObject> hlaObject,
                                                     double federateTime )
{
	ChallengeObject challengeObj( hlaObject );
	receiveChallengeObject( challengeObj );
}

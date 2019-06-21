#include <iostream>

#include "ChallengeFederate.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

using namespace base;
using namespace std;

int main( int argc, char* argv[] )
{
	ChallengeFederate *fed = new ChallengeFederate();
	bool cFound = false;
	for( int i = 1; i < argc; i++ )
	{
		if( string(argv[i]) == "-c" )
		{
			fed->setIterationCount( atoi(argv[i + 1]) );
			cout << "-----------------------------------------------------------------------" << endl;
			cout << "Federate is configured to run for " << atoi(argv[i + 1]) << " rounds." << endl;
			cout << "-----------------------------------------------------------------------" << endl;
			cFound = true;
		}
	}
	if( !cFound )
	{
		cout << "Federate will run for infinitely many rounds.";
	}

	shared_ptr<base::FederateConfiguration> federateConfig = fed->getFederateConfiguration();
	federateConfig->setFederationName( string("ChallengeResponseFederate") );
	federateConfig->setFederateName( string("CppChallenger") );
	federateConfig->setFederateType( string("CppChallenger") );
	federateConfig->setLookAhead( 0.2f );
	federateConfig->setTimeStep( 1.0f );
	federateConfig->addFomPath( string("ChallengeResponse/fom/ChallengeResponse.xml") );
	federateConfig->addSomPath( string("ChallengeResponse/som/Challenge.xml") );
	federateConfig->setPermisionToCreateFederation(true);
	try
	{
		fed->runFederate();
	}
	catch( UCEFException& e )
	{
		cout << e.what() << endl;
	}
	delete fed;
}

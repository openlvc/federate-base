#include <iostream>

#include "ChallengeFederate.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

using namespace base;
using namespace std;

int main( int argc, char* argv[] )
{
	ChallengeFederate *fed = new ChallengeFederate();
	bool countFound = false;
	bool configFound = false;
	for( int i = 1; i < argc; i++ )
	{
		if( string(argv[i]) == "-count" )
		{
			fed->setIterationCount( atoi(argv[i + 1]) );
			cout << "-----------------------------------------------------------------------" << endl;
			cout << "Federate is configured to run for " << atoi(argv[i + 1]) << " rounds." << endl;
			cout << "-----------------------------------------------------------------------" << endl;
			countFound = true;
		}
		else if( string(argv[i]) == "-config" )
		{
			fed->configureFromJSON( string(argv[i + 1]) );
			cout << "-----------------------------------------------------------------------" << endl;
			cout << "Federate is configured using " << string(argv[i + 1]) << " file." << endl;
			cout << "-----------------------------------------------------------------------" << endl;
			configFound = true;
		}
		else if( string(argv[i]) == "-help" )
		{
			cout << "-config <FILEPATH>\t\t To configure federate using a config file."  << endl;
			cout << "-count <NUMBER>\t\t To specify the number of challenges to send"  << endl;
		}
	}
	if( !countFound )
	{
		fed->setIterationCount( 100 );
		cout << "Federate will run for 100 rounds.";
	}

	if( !configFound )
	{
		shared_ptr<base::FederateConfiguration> federateConfig = fed->getFederateConfiguration();
		federateConfig->setFederationName( string("ChallengeResponseFederate") );
		federateConfig->setFederateName( string("CppChallengeFederate") );
		federateConfig->setFederateType( string("CppChallengeFederate") );
		federateConfig->setLookAhead( 0.2f );
		federateConfig->setTimeStep( 1.0f );
		federateConfig->setTimeConstrained( false );
		federateConfig->setTimeRegulated( true );
		federateConfig->setSyncBeforeResign( false );
		federateConfig->setMaxJoinAttempts( 2 );
		federateConfig->setRetryInterval( 5 );
		federateConfig->addBaseFomPath( string("resources//ChallengeResponse//fom//ChallengeResponse.xml") );
		federateConfig->addSomPath( string("resources//ChallengeResponse//som//Challenge.xml") );
		federateConfig->setPermisionToCreateFederation(true);
	}

	cout << "-----------------------------------------------------------------------" << endl;
	cout << "Federate " + fed->getFederateConfiguration()->getFederateName() + " is starting up." << endl;
	cout << "-----------------------------------------------------------------------" << endl;

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

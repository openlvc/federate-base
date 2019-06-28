#include <iostream>

#include "ResponseFederate.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

using namespace base;
using namespace std;

int main( int argc, char* argv[] )
{
	ResponseFederate *fed = new ResponseFederate();

	bool configFound = false;
	for( int i = 1; i < argc; i++ )
	{
		if( string(argv[i]) == "-config" )
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
		}
	}
	if( !configFound )
	{
		shared_ptr<base::FederateConfiguration> federateConfig = fed->getFederateConfiguration();
		federateConfig->setFederationName( string("ChallengeResponseFederate") );
		federateConfig->setFederateName( string("CppResponseFederate") );
		federateConfig->setFederateType( string("CppResponseFederate") );
		federateConfig->setLookAhead( 0.2f );
		federateConfig->setTimeStep( 1.0f );
		federateConfig->setTimeConstrained( false );
		federateConfig->setTimeRegulated( true );
		federateConfig->setSyncBeforeResign( false );
		federateConfig->setMaxJoinAttempts( 2 );
		federateConfig->setRetryInterval( 5 );
		federateConfig->addBaseFomPath( string("resources//ChallengeResponse//fom//ChallengeResponse.xml") );
		federateConfig->addSomPath( string("resources//ChallengeResponse//som//Response.xml") );
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

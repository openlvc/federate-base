#include <iostream>

#include "PingFederate.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

using namespace base;
using namespace std;

int main( int argc, char* argv[] )
{
	PingFederate *fed = new PingFederate();

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
		cout << "I need a config file to configure myself." << endl;
		cout << "Re-run with -config option and tell how to configure myself."  << endl;
		delete fed;
		exit(0);
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

#include <iostream>

#include "PingFederate.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

using namespace base;
using namespace std;

int main( int argc, char* argv[] )
{
	PingFederate *pingFed = new PingFederate();

	cout << "-----------------------------------------------------------------------" << endl;
	cout << "Federate " + pingFed->getFederateConfiguration()->getFederateName() + " is starting up." << endl;
	cout << "-----------------------------------------------------------------------" << endl;

	try
	{
		pingFed->runFederate();
	}
	catch( UCEFException& e )
	{
		cout << e.what() << endl;
	}
	delete pingFed;
}

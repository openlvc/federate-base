#include <iostream>

#include "PongFederate.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

using namespace base;
using namespace std;

int main( int argc, char* argv[] )
{
	PongFederate *pongFed = new PongFederate();

	cout << "-----------------------------------------------------------------------" << endl;
	cout << "Federate " + pongFed->getFederateConfiguration()->getFederateName() + " is starting up." << endl;
	cout << "-----------------------------------------------------------------------" << endl;

	try
	{
		pongFed->runFederate();
	}
	catch( UCEFException& e )
	{
		cout << e.what() << endl;
	}
	delete pongFed;
}

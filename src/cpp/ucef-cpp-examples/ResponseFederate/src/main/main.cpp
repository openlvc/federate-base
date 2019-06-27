#include <iostream>

#include "ResponseFederate.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

using namespace base;
using namespace std;

int main()
{
	IFederateBase *fed = new ResponseFederate();
	shared_ptr<base::FederateConfiguration> federateConfig = fed->getFederateConfiguration();
	federateConfig->setFederationName( string("ChallengeResponseFederate") );
	federateConfig->setFederateName( string("CppResponder") );
	federateConfig->setFederateType(string("CppResponder") );
	federateConfig->setLookAhead( 0.2f );
	federateConfig->setTimeStep( 1.0f );
	federateConfig->addBaseFomPath( string("ChallengeResponse/fom/ChallengeResponse.xml") );
	federateConfig->addSomPath( string("ChallengeResponse/som/Response.xml") );
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

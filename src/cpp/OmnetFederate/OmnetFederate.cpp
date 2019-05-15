#include <iostream>
#include <memory>

#include "gov/nist/ucef/config.h"

#include "gov/nist/ucef/hla/base/HLAInteraction.h"
#include "gov/nist/ucef/hla/base/HLAObject.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"
#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"

using namespace std;
using namespace base;
using namespace base::ucef;
using namespace base::util;

class OmnetFederate : public NoOpFederate
{
	
	public:
		//----------------------------------------------------------
		//                     Constructors
		//----------------------------------------------------------
		OmnetFederate() = default;
		virtual ~OmnetFederate() = default;

		//----------------------------------------------------------
		//          Lifecycle hooks implementation
		//----------------------------------------------------------
		void beforeReadyToPopulate() override
		{
			cout << "\'Ready to populate\' hook" << endl;
			pressEnterToContinue();
		}

		void beforeReadyToRun() override
		{
			cout << "\'Ready to run\' hook" << endl;

			try
			{
				pressEnterToContinue();
			}
			catch( UCEFException& e )
			{
				cout << e.what() << endl;
			}
		}

		void beforeFirstStep() override
		{
			pressEnterToContinue();
		}

		void beforeReadyToResign() override
		{
			pressEnterToContinue();
		}

		virtual void beforeExit() override 
		{
			cout << "\'Before exit\' hook" << endl;
			pressEnterToContinue();

		}

		virtual bool step( double federateTime ) override
		{
			return true;
		}

		//----------------------------------------------------------
		//         Implement Callback Methods
		//----------------------------------------------------------
		virtual void receivedObjectRegistration( shared_ptr<const HLAObject> hlaObject,
		                                         double federateTime ) override
		{
			//cout << "Received an object registration callback " << hlaObject->getClassName() << endl;
		}

		virtual void receivedAttributeReflection( shared_ptr<const HLAObject> hlaObject,
		                                          double federateTime ) override 
		{

		}

		virtual void receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
		                                  double federateTime ) override
		{

		}

		virtual void receivedObjectDeletion( shared_ptr<const HLAObject> hlaObject ) override
		{
			//cout << "Received an object deletion callback " << hlaObject->getClassName() << endl;
		}

		virtual void receivedSimStart( shared_ptr<const SimStart> hlaInt,
		                               double federateTime ) override
		{
			//cout << "Received sim start interaction";
		}

		virtual void receivedSimEnd( shared_ptr<const SimEnd> hlaInt,
		                             double federateTime ) override
		{
			//cout << "Received sim end interaction";
		}

		virtual void receivedSimPaused( shared_ptr<const SimPause> hlaInt,
		                                double federateTime) override
		{
			//cout << "Received sim paused interaction";
		}

		virtual void receivedSimResumed( shared_ptr<const SimResume> hlaInt,
		                                 double federateTime ) override
		{
			//cout << "Received sim resumed interaction";
		}

	private:
		void pressEnterToContinue()
		{
			do 
			{
				cout << '\n' << "Press ENTER to continue...";
			} while (cin.get() != '\n');
		}
};

int main( int argc, char* argv[] )
{
	OmnetFederate *fed = new OmnetFederate();

	shared_ptr<base::FederateConfiguration> federateConfig = fed->getFederateConfiguration();
	federateConfig->setFederationName( string("OmnetFederation") );
	federateConfig->setFederateName( string("OmnetFederate-Test") );
	federateConfig->setFederateType( string("OmnetFederate") );
	federateConfig->setLookAhead( 0.2f );
	federateConfig->setTimeStepSize( 1.0f );
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

#include <fstream>
#include <iostream>
#include <memory>
#include <mutex>

#include "gov/nist/ucef/hla/base/HLAInteraction.h"
#include "gov/nist/ucef/hla/base/HLAObject.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"
#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"

using namespace std;
using namespace base;
using namespace base::ucef;
using namespace base::util;

const static string PING_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.PingInteraction";
const static string PONG_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.PongInteraction";
bool sendPing = true;

class PingFederate : public NoOpFederate
{
	
	public:
		//----------------------------------------------------------
		//                     Constructors
		//----------------------------------------------------------
		PingFederate()
		{
			initConfigFromJson( ".//resources//config//pingConfig.json" );
		}

		virtual ~PingFederate() = default;

		//----------------------------------------------------------
		//          Lifecycle hooks implementation
		//----------------------------------------------------------
		void beforeReadyToPopulate() override
		{
			cout << "\'Ready to populate\' hook" << endl;
			//pressEnterToContinue();
		}

		void beforeReadyToRun() override
		{
			cout << "\'Ready to run\' hook" << endl;
			//pressEnterToContinue();
		}

		void beforeFirstStep() override
		{
			cout << "\'Before first step\' hook" << endl;
			//pressEnterToContinue();
		}

		void beforeReadyToResign() override
		{
			cout << "\'Before ready to resign\' hook" << endl;
			//pressEnterToContinue();
		}

		virtual void beforeExit() override 
		{
			cout << "\'Before exit\' hook" << endl;
			//pressEnterToContinue();
		}

		virtual bool step( double federateTime ) override
		{
			if( sendPing )
			{
				// Create a new challenge object
				shared_ptr<HLAInteraction> pingInteraction = make_shared<HLAInteraction>( PING_INTERACTION );
				pingInteraction->setValue( "stringValue", string("MyPing") );
				sendInteraction( pingInteraction );
				sendPing = false;
			}
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
			//cout << "Received an object update " + hlaObject->getClassName();
		}

		virtual void receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
		                                  double federateTime ) override
		{
			lock_guard<mutex> lock( mutexLock );
			string className = hlaInt->getInteractionClassName();
			// A ping received now we can send another Ping
			if( className.find(PONG_INTERACTION) == 0 )
				sendPing = true;
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
		mutex mutexLock;
};

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

#include <iostream>

#include "gov/nist/ucef/util/Logger.h"

#include "PingFederate.h"

using namespace base;
using namespace base::ucef;
using namespace base::util;
using namespace std;

const static string PING_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.PingInteraction";
const static string PONG_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.PongInteraction";


PingFederate::PingFederate() : sendPing(true)
{
	initFromJson( ".//resources//config//pingConfig.json" );
}

PingFederate::~PingFederate() = default;


void PingFederate::beforeReadyToPopulate()
{
	cout << "\'Ready to populate\' hook" << endl;
	//pressEnterToContinue();
}

void PingFederate::beforeReadyToRun()
{
	cout << "\'Ready to run\' hook" << endl;
	//pressEnterToContinue();
}

void PingFederate::beforeFirstStep()
{
	cout << "\'Before first step\' hook" << endl;
	//pressEnterToContinue();
}

void PingFederate::beforeReadyToResign()
{
	cout << "\'Before ready to resign\' hook" << endl;
	//pressEnterToContinue();
}

void PingFederate::beforeExit()
{
	cout << "\'Before exit\' hook" << endl;
	//pressEnterToContinue();
}

bool PingFederate::step( double federateTime )
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
void PingFederate::receivedObjectRegistration( shared_ptr<const HLAObject> hlaObject,
										       double federateTime )
{
	//cout << "Received an object registration callback " << hlaObject->getClassName() << endl;
}

void PingFederate::receivedAttributeReflection( shared_ptr<const HLAObject> hlaObject,
										        double federateTime )
{
	//cout << "Received an object update " + hlaObject->getClassName();
}

void PingFederate::receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
								        double federateTime )
{
	Logger& logger = Logger::getInstance();
	string className = hlaInt->getInteractionClassName();
	logger.log( "Received interaction " + className, LevelInfo );
	// A ping received now we can send another Ping
	if( className.find(PONG_INTERACTION) == 0 )
		sendPing = true;
}

void PingFederate::receivedObjectDeletion( shared_ptr<const HLAObject> hlaObject )
{
	//cout << "Received an object deletion callback " << hlaObject->getClassName() << endl;
}

void PingFederate::receivedSimStart( shared_ptr<const SimStart> hlaInt,
							         double federateTime )
{
	//cout << "Received sim start interaction";
}

void PingFederate::receivedSimEnd( shared_ptr<const SimEnd> hlaInt,
							       double federateTime )
{
	//cout << "Received sim end interaction";
}

void PingFederate::receivedSimPaused( shared_ptr<const SimPause> hlaInt,
							      	  double federateTime)
{
	//cout << "Received sim paused interaction";
}

void PingFederate::receivedSimResumed( shared_ptr<const SimResume> hlaInt,
								       double federateTime )
{
	//cout << "Received sim resumed interaction";
}



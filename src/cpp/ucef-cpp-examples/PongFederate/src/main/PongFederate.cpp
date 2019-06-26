#include <iostream>

#include "PongFederate.h"

#include "gov/nist/ucef/util/Logger.h"

using namespace base;
using namespace base::ucef;
using namespace base::util;
using namespace std;

const static string PING_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.PingInteraction";
const static string PONG_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.PongInteraction";


PongFederate::PongFederate() : sendPong(false)
{
	initFromJson( ".//resources//config//pongConfig.json" );
}

PongFederate::~PongFederate() = default;


void PongFederate::beforeReadyToPopulate()
{
	cout << "\'Ready to populate\' hook" << endl;
	//pressEnterToContinue();
}

void PongFederate::beforeReadyToRun()
{
	cout << "\'Ready to run\' hook" << endl;
	//pressEnterToContinue();
}

void PongFederate::beforeFirstStep()
{
	cout << "\'Before first step\' hook" << endl;
	//pressEnterToContinue();
}

void PongFederate::beforeReadyToResign()
{
	cout << "\'Before ready to resign\' hook" << endl;
	//pressEnterToContinue();
}

void PongFederate::beforeExit()
{
	cout << "\'Before exit\' hook" << endl;
	//pressEnterToContinue();
}

bool PongFederate::step( double federateTime )
{
	if( sendPong )
	{
		// Create a new challenge object
		shared_ptr<HLAInteraction> pongInteraction = make_shared<HLAInteraction>( PONG_INTERACTION );
		pongInteraction->setValue( "stringValue", string("MyPong") );
		sendInteraction( pongInteraction );
		sendPong = false;
	}
	return true;
}

//----------------------------------------------------------
//         Implement Callback Methods
//----------------------------------------------------------
void PongFederate::receivedObjectRegistration( shared_ptr<const HLAObject> hlaObject,
										       double federateTime )
{
	//cout << "Received an object registration callback " << hlaObject->getClassName() << endl;
}

void PongFederate::receivedAttributeReflection( shared_ptr<const HLAObject> hlaObject,
										        double federateTime )
{
	//cout << "Received an object update " + hlaObject->getClassName();
}

void PongFederate::receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
								        double federateTime )
{
	Logger& logger = Logger::getInstance();
	string className = hlaInt->getInteractionClassName();
	logger.log( "Received interaction " + className, LevelInfo );
	lock_guard<mutex> lock( mutexLock );

	// A ping received now we can send another Ping
	if( className.find(PING_INTERACTION) == 0 )
		sendPong = true;
}

void PongFederate::receivedObjectDeletion( shared_ptr<const HLAObject> hlaObject )
{
	//cout << "Received an object deletion callback " << hlaObject->getClassName() << endl;
}

void PongFederate::receivedSimStart( shared_ptr<const SimStart> hlaInt,
							         double federateTime )
{
	//cout << "Received sim start interaction";
}

void PongFederate::receivedSimEnd( shared_ptr<const SimEnd> hlaInt,
							       double federateTime )
{
	//cout << "Received sim end interaction";
}

void PongFederate::receivedSimPaused( shared_ptr<const SimPause> hlaInt,
							      	  double federateTime)
{
	//cout << "Received sim paused interaction";
}

void PongFederate::receivedSimResumed( shared_ptr<const SimResume> hlaInt,
								       double federateTime )
{
	//cout << "Received sim resumed interaction";
}



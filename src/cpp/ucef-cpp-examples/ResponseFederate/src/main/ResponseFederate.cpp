#include <iostream>
#include <list>
#include <mutex>

#include "ChallengeInteraction.h"
#include "ChallengeObject.h"
#include "ResponseInteraction.h"

#include "gov/nist/ucef/hla/base/HLAInteraction.h"
#include "gov/nist/ucef/hla/base/HLAObject.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"
#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"

using namespace std;
using namespace base;
using namespace base::ucef;
using namespace base::util;

const static string RESPONSE_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.Response";

struct Response
{
	string challengeId;
	string resultString;
};

class ResponseFederate : public NoOpFederate
{

public:
	//----------------------------------------------------------
	//                     Constructors
	//----------------------------------------------------------
	ResponseFederate() = default;
	virtual ~ResponseFederate() = default;

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
			cout << "\n--Object instances published by this federate--" << endl;
			vector<string> publishClassNames = getFederateConfiguration()->getClassNamesPublished();
			for( auto pubClassName : publishClassNames )
			{
				cout << "----" + pubClassName << endl;
				vector<string> attributes = getFederateConfiguration()->getAttributeNamesPublished( pubClassName );
				for( auto attribute : attributes )
				{
					cout << "--------" + attribute << endl;
				}
			}

			cout << "\n--Object instances subscribed by this federate--" << endl;
			vector<string> subClassNames = getFederateConfiguration()->getClassNamesSubscribed();
			for( auto subClassName : subClassNames )
			{
				cout << "----" + subClassName << endl;
				vector<string> attributes = getFederateConfiguration()->getAttributeNamesSubscribed( subClassName );
				for( auto attribute : attributes )
				{
					cout << "--------" + attribute << endl;
				}
			}

			cout << "\n--Interactions published by this federate--" << endl;
			vector<string> publishInteractionNames = getFederateConfiguration()->getInteractionNamesPublished();
			for( auto pubInteractionName : publishInteractionNames )
			{
				cout << "----" + pubInteractionName << endl;
				vector<string> params = getFederateConfiguration()->getParameterNames( pubInteractionName );
				for( auto param : params )
				{
					cout << "--------" + param << endl;
				}
			}

			cout << "\n--Interactions subscribed by this federate--" << endl;
			vector<string> subInteractionNames = getFederateConfiguration()->getInteractionNamesSubscribed();
			for( auto subInteractionName : subInteractionNames )
			{
				cout << "----" + subInteractionName << endl;
				vector<string> params = getFederateConfiguration()->getParameterNames( subInteractionName );
				for( auto param : params )
				{
					cout << "--------" + param << endl;
				}
			}
		}
		catch( UCEFException& e )
		{
			cout << e.what() << endl;
		}

		cout << "\'Before ready to run.\' hook" << endl;
		pressEnterToContinue();
	}

	void beforeFirstStep() override
	{
		cout << "\'Before first step\' hook" << endl;
		pressEnterToContinue();
	}

	void beforeReadyToResign() override
	{
		cout << "\'Before ready to resign\' hook" << endl;
		pressEnterToContinue();
	}

	virtual void beforeExit() override
	{
		cout << "\'Before exit\' hook" << endl;
		pressEnterToContinue();
	}

	virtual bool step(double federateTime) override
	{
		// Create a local list of received remote challenges
		unique_lock<mutex> lock( challengeMutex );
		list<shared_ptr<ChallengeObject>> challengeObjectList = remoteChallengeObjects;
		list<shared_ptr<ChallengeInteraction>> challengeInteractionList = remoteChallengeInteractions;
		remoteChallengeObjects.clear();
		remoteChallengeInteractions.clear();
		lock.unlock();

		// Generate a response for each remote challenge
		for( auto challenge : challengeObjectList )
		{
			Response response = solveChallenge( challenge );
			shared_ptr<HLAInteraction> responseInteraction = generateResponseInteraction( response );
			rtiAmbassadorWrapper->sendInteraction( responseInteraction );
		}

		for( auto challenge : challengeInteractionList )
		{
			Response response = solveChallenge( challenge );
			shared_ptr<HLAInteraction> responseInteraction = generateResponseInteraction( response );
			rtiAmbassadorWrapper->sendInteraction( responseInteraction );
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
		shared_ptr<ChallengeObject> receievedChallenge = make_shared<ChallengeObject>( hlaObject );
		cout << "Received object challenge id      : " + receievedChallenge->getChallengeId() << endl;
		cout << "Received string is                : " + receievedChallenge->getStringValue() << endl;
		cout << "Received index is                 : " + to_string( receievedChallenge->getBeginIndex() ) << endl;
		cout << "---------------------------------------------------------------------------------" << endl;
		lock_guard<mutex> lock( challengeMutex );
		remoteChallengeObjects.emplace_back( receievedChallenge );
	}

	virtual void receivedInteraction( shared_ptr<const HLAInteraction> hlaInt,
	                                  double federateTime ) override
	{
		shared_ptr<ChallengeInteraction> receievedChallenge = make_shared<ChallengeInteraction>( hlaInt );
		cout << "Received interaction challenge id : " + receievedChallenge->getChallengeId() << endl;
		cout << "Received string is                : " + receievedChallenge->getStringValue() << endl;
		cout << "Received index is                 : " + to_string( receievedChallenge->getBeginIndex() ) << endl;
		cout << "---------------------------------------------------------------------------------" << endl;
		lock_guard<mutex> lock( challengeMutex );
		remoteChallengeInteractions.emplace_back( receievedChallenge );
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
	                                double federateTime ) override
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

	shared_ptr<HLAInteraction> generateResponseInteraction( Response response )
	{
		shared_ptr<ResponseInteraction> responseInteraction = make_shared<ResponseInteraction>( RESPONSE_INTERACTION );
		responseInteraction->setChallengeId( response.challengeId );
		responseInteraction->setSubStringValue( response.resultString );
		return responseInteraction;
	}

	Response solveChallenge( shared_ptr<ChallengeObject> receievedChallenge )
	{
		string receivedString = receievedChallenge->getStringValue();
		int beginIndex = receievedChallenge->getBeginIndex();
		string resultStr = receivedString.substr( beginIndex );

		Response response;
		response.challengeId = receievedChallenge->getChallengeId();
		response.resultString = resultStr;
		return response;
	}

	Response solveChallenge( shared_ptr<ChallengeInteraction> receievedChallenge )
	{
		string receivedString = receievedChallenge->getStringValue();
		int beginIndex = receievedChallenge->getBeginIndex();
		string resultStr = receivedString.substr( beginIndex );

		Response response;
		response.challengeId = receievedChallenge->getChallengeId();
		response.resultString = resultStr;
		return response;
	}

private:
	list<shared_ptr<ChallengeObject>> remoteChallengeObjects;
	list<shared_ptr<ChallengeInteraction>> remoteChallengeInteractions;
	mutex challengeMutex;
	int count;
};

int main()
{
	IFederateBase *fed = new ResponseFederate();
	shared_ptr<base::FederateConfiguration> federateConfig = fed->getFederateConfiguration();
	federateConfig->setFederationName( string("ChallengeResponseFederate") );
	federateConfig->setFederateName( string("CppResponder") );
	federateConfig->setFederateType(string("CppResponder") );
	federateConfig->setLookAhead( 0.2f );
	federateConfig->setTimeStep( 1.0f );
	federateConfig->addFomPath( string("ChallengeResponse/fom/ChallengeResponse.xml") );
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

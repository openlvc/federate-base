#include <iostream>

#include "ResponseFederate.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"

using namespace std;
using namespace base;
using namespace base::ucef;
using namespace base::util;

const static string RESPONSE_INTERACTION = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.Response";

	ResponseFederate::ResponseFederate() : count(0)
	{

	}

	ResponseFederate::~ResponseFederate()
	{

	}

	//----------------------------------------------------------
	//          Lifecycle hooks implementation
	//----------------------------------------------------------
	void ResponseFederate::beforeReadyToPopulate()
	{
		cout << "\'Ready to populate\' hook" << endl;
		pressEnterToContinue();
	}

	void ResponseFederate::beforeReadyToRun()
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

	void ResponseFederate::beforeFirstStep()
	{
		cout << "\'Before first step\' hook" << endl;
		pressEnterToContinue();
	}

	void ResponseFederate::beforeReadyToResign()
	{
		cout << "\'Before ready to resign\' hook" << endl;
		pressEnterToContinue();
	}

	void ResponseFederate::beforeExit()
	{
		cout << "\'Before exit\' hook" << endl;
		pressEnterToContinue();
	}

	bool ResponseFederate::step(double federateTime)
	{
		// Create a local list of received remote challenges
		unique_lock<mutex> lock( challengeMutex );
		list<ChallengeObject> challengeObjectList = remoteChallengeObjects;
		list<ChallengeInteraction> challengeInteractionList = remoteChallengeInteractions;
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
	void ResponseFederate::receivedObjectRegistration( shared_ptr<const HLAObject> hlaObject,
	                                                   double federateTime )
	{
		//cout << "Received an object registration callback " << hlaObject->getClassName() << endl;
	}

	void ResponseFederate::receiveChallengeObject( ChallengeObject challengeObj )
	{
		cout << "Received object challenge id      : " + challengeObj.getChallengeId() << endl;
		cout << "Received string is                : " + challengeObj.getStringValue() << endl;
		cout << "Received index is                 : " + to_string( challengeObj.getBeginIndex() ) << endl;
		cout << "---------------------------------------------------------------------------------" << endl;
		lock_guard<mutex> lock( challengeMutex );
		remoteChallengeObjects.emplace_back( challengeObj );
	}

	void ResponseFederate::receivedChallengeInteraction( ChallengeInteraction challengeInt )
	{
		cout << "Received interaction challenge id : " + challengeInt.getChallengeId() << endl;
		cout << "Received string is                : " + challengeInt.getStringValue() << endl;
		cout << "Received index is                 : " + to_string( challengeInt.getBeginIndex() ) << endl;
		cout << "---------------------------------------------------------------------------------" << endl;
		lock_guard<mutex> lock( challengeMutex );
		remoteChallengeInteractions.emplace_back( challengeInt );
	}

	void ResponseFederate::receivedObjectDeletion( shared_ptr<const HLAObject> hlaObject )
	{
		//cout << "Received an object deletion callback " << hlaObject->getClassName() << endl;
	}

	void ResponseFederate::receivedSimStart( shared_ptr<const SimStart> hlaInt,
	                                         double federateTime )
	{
		//cout << "Received sim start interaction";
	}

	void ResponseFederate::receivedSimEnd( shared_ptr<const SimEnd> hlaInt,
	                                       double federateTime )
	{
		//cout << "Received sim end interaction";
	}

	void ResponseFederate::receivedSimPaused( shared_ptr<const SimPause> hlaInt,
	                                          double federateTime )
	{
		//cout << "Received sim paused interaction";
	}

	void ResponseFederate::receivedSimResumed( shared_ptr<const SimResume> hlaInt,
	                                           double federateTime )
	{
		//cout << "Received sim resumed interaction";
	}

	void ResponseFederate::pressEnterToContinue()
	{
		do
		{
			cout << '\n' << "Press ENTER to continue...";
		} while (cin.get() != '\n');
	}

	shared_ptr<HLAInteraction> ResponseFederate::generateResponseInteraction( Response response )
	{
		shared_ptr<ResponseInteraction> responseInteraction = make_shared<ResponseInteraction>( RESPONSE_INTERACTION );
		responseInteraction->setChallengeId( response.challengeId );
		responseInteraction->setSubStringValue( response.resultString );
		return responseInteraction;
	}

	Response ResponseFederate::solveChallenge( ChallengeObject receievedChallenge )
	{
		string receivedString = receievedChallenge.getStringValue();
		int beginIndex = receievedChallenge.getBeginIndex();
		string resultStr = receivedString.substr( beginIndex );

		Response response;
		response.challengeId = receievedChallenge.getChallengeId();
		response.resultString = resultStr;
		return response;
	}

	Response ResponseFederate::solveChallenge( ChallengeInteraction receievedChallenge )
	{
		string receivedString = receievedChallenge.getStringValue();
		int beginIndex = receievedChallenge.getBeginIndex();
		string resultStr = receivedString.substr( beginIndex );

		Response response;
		response.challengeId = receievedChallenge.getChallengeId();
		response.resultString = resultStr;
		return response;
	}

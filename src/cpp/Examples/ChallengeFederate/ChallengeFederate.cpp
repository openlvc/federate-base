#include <fstream>
#include <iostream>
#include <memory>
#include <mutex>
#include <random>

#include "ChallengeInteraction.h"
#include "ChallengeObject.h"
#include "ResponseInteraction.h"

#include "gov/nist/ucef/config.h"

#include "gov/nist/ucef/hla/base/HLAInteraction.h"
#include "gov/nist/ucef/hla/base/HLAObject.h"
#include "gov/nist/ucef/hla/base/UCEFException.h"
#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"

using namespace std;
using namespace base;
using namespace base::ucef;
using namespace base::util;

const static string CHALLENGE_OBJECT = "ObjectRoot.ParentObject.ChallengeObject";
const static string CHALLENGE_INTERACTION = "InteractionRoot.C2WInteractionRoot.ParentInteraction.ChallengeInteraction";

const static int CHALLENGE_LENGTH = 10;

static int PASS_COUNTER = 0;
static int CHALLENGE_ID = 0;
static bool sendChallengeObject = true;

struct Challenge
{
	string challengeId;
	string stringValue;
	int beginIndex;
};

class ChallengeFederate : public NoOpFederate
{
	
	public:
		//----------------------------------------------------------
		//                     Constructors
		//----------------------------------------------------------
		ChallengeFederate() = default;
		virtual ~ChallengeFederate() = default;

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
		}

		void beforeFirstStep() override
		{
			cout << "\'Before first step\' hook" << endl;
			time_t t = time(0);   // get time now
			struct tm * now = localtime(&t);

			char buffer[152];
			strftime( buffer, 152, "%Y-%m-%d_%H-%M-%S", now );
			string fileName = "logs\\error-" + string( buffer ) + ".log";
			// Create an error log for this federate
			errorLog.open( fileName );

			pressEnterToContinue();
		}

		void beforeReadyToResign() override
		{
			cout << "\'Before ready to resign\' hook" << endl;
			// Print the sent challenge
			cout << "Total challenges sent          : " << to_string( CHALLENGE_ID  ) << endl;
			cout << "Pass count                     : " << to_string( PASS_COUNTER ) << endl;
			cout << "Failed count                   : " << to_string( CHALLENGE_ID - PASS_COUNTER ) << endl;
			cout << "---------------------------------------------" << endl;

			// Close the error log
			errorLog.close();

			pressEnterToContinue();
		}

		virtual void beforeExit() override 
		{
			cout << "\'Before exit\' hook" << endl;
			for( auto object : sentChallengeObjects)
			{
				cout << "No result received for id : " << object.first << endl;
				auto tmpObject = static_pointer_cast<HLAObject>(object.second);
				rtiAmbassadorWrapper->deleteObjectInstance( tmpObject );
			}

			for( auto object : sentChallengeInteractions )
			{
				cout << "No result received for id : " << object.first << endl;
			}
		}

		virtual bool step( double federateTime ) override
		{
			if( sendChallengeObject )
			{
				// Create a new challenge object
				shared_ptr<ChallengeObject> challengeObject = make_shared<ChallengeObject>( CHALLENGE_OBJECT );
				// Generate a challenge
				Challenge challenge = generateChallenge();
				// Set attributes
				challengeObject->setChallengeId( challenge.challengeId );
				challengeObject->setStringValue( challenge.stringValue );
				challengeObject->setBeginIndex( challenge.beginIndex );

				// Send attribute update notification to users
				rtiAmbassadorWrapper->registerObjectInstance( challengeObject );
				auto tmpObject = static_pointer_cast<HLAObject>( challengeObject );
				rtiAmbassadorWrapper->updateAttributeValues( tmpObject );

				// Store the challenge
				sentChallengeObjects.
					emplace( pair<string, shared_ptr<ChallengeObject>>(challengeObject->getChallengeId(), challengeObject) );

				// Print the sent challenge
				cout << "Sending challenge object      : " << challengeObject->getChallengeId() << endl;
				cout << "with string value             : " << challengeObject->getStringValue() << endl;
				cout << "and begin index               : " << challengeObject->getBeginIndex() << endl;
				cout << "---------------------------------------------" << endl;

				sendChallengeObject = false;
			}
			else
			{
				// Create a new challenge interaction
				shared_ptr<ChallengeInteraction> challengeInteraction = make_shared<ChallengeInteraction>( CHALLENGE_INTERACTION );
				// Generate a challenge
				Challenge challenge = generateChallenge();
				// Set attributes
				challengeInteraction->setChallengeId( challenge.challengeId );
				challengeInteraction->setStringValue( challenge.stringValue );
				challengeInteraction->setBeginIndex( challenge.beginIndex );

				// Send attribute update notification to users
				auto tmpInteraction = static_pointer_cast<HLAInteraction>( challengeInteraction );
				rtiAmbassadorWrapper->sendInteraction( tmpInteraction );

				// Store the challenge
				sentChallengeInteractions.
					emplace( pair<string, shared_ptr<ChallengeInteraction>>(challengeInteraction->getChallengeId(), challengeInteraction) );

				// Print the sent challenge
				cout << "Sending challenge interaction : " << challengeInteraction->getChallengeId() << endl;
				cout << "with string value             : " << challengeInteraction->getStringValue() << endl;
				cout << "and begin index               : " << challengeInteraction->getBeginIndex() << endl;
				cout << "---------------------------------------------" << endl;

				sendChallengeObject = true;
			}

			unique_lock<mutex> lock( mutexLock );
			list<shared_ptr<ResponseInteraction>> responseCopy = responseInteractions;
			responseInteractions.clear();
			lock.unlock();
			for( auto it = responseCopy.begin(); it != responseCopy.end(); it++)
			{
				bool foundSentItem = false;
				auto itSentObject = sentChallengeObjects.find( (*it)->getChallengeId() );
				if( itSentObject != sentChallengeObjects.end() )
				{
					foundSentItem = true;
					bool valid = isCorrect( itSentObject->second->getStringValue(),
					                        (*it)->getSubStringValue(),
					                        itSentObject->second->getBeginIndex());

					if( valid ) PASS_COUNTER++;

					string resultText = valid ? "CORRECT" : "INCORRECT";

					string msg = "Challenge id Receive          : " + (*it)->getChallengeId() + "\n";
					msg += "Type                          : Object\n";
					msg += "Sent String                   : " + itSentObject->second->getStringValue() + "\n";
					msg += "Begin Index                   : " + to_string( itSentObject->second->getBeginIndex() ) + "\n";
					msg += "Substring received            : " + (*it)->getSubStringValue() + "\n";
					msg += "Status                        : " + resultText + "\n";
					msg += "---------------------------------------------\n";

					cout << msg;

					// If the result is incorrect write to error log
					if( !valid )
					{
						errorLog << msg;
					}
					// Since we received a reply fo this challange, we can delete this instace from RTI now
					auto tmpObject = static_pointer_cast<HLAObject>( itSentObject->second );
					rtiAmbassadorWrapper->deleteObjectInstance( tmpObject );
					sentChallengeObjects.erase( itSentObject );
				}

				// If not found go and search in send interactions
				if( !foundSentItem )
				{
					auto itSentInteractions = sentChallengeInteractions.find( (*it)->getChallengeId() );
					if( itSentInteractions != sentChallengeInteractions.end() )
					{
						foundSentItem = true;

						bool valid = isCorrect( itSentInteractions->second->getStringValue(),
						                        (*it)->getSubStringValue(),
						                        itSentInteractions->second->getBeginIndex());

						if( valid ) PASS_COUNTER++;


						string resultText = valid ? "CORRECT" : "INCORRECT";

						string msg = "Challenge id Receive          : " + (*it)->getChallengeId() + "\n";
						msg += "Type                          : Interaction\n";
						msg += "Sent String                   : " + itSentInteractions->second->getStringValue() + "\n";
						msg += "Begin Index                   : " + to_string( itSentInteractions->second->getBeginIndex() ) + "\n";
						msg += "Substring received            : " + (*it)->getSubStringValue() + "\n";
						msg += "Status                        : " + resultText + "\n";
						msg += "---------------------------------------------\n";

						cout << msg;

						// If the result is incorrect write to error log
						if( !valid )
						{
							errorLog << msg;
						}
						sentChallengeInteractions.erase( itSentInteractions );
					}
				}

				// If still not found that means this interaction is not a response
				// received for a challenge originated from this federate
				if( !foundSentItem )
				{

				}
			}
			if (count != 0 && CHALLENGE_ID == count)
				return false;
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
			shared_ptr<ResponseInteraction> response = make_shared<ResponseInteraction>( hlaInt );
			responseInteractions.emplace_back( response);
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

		void setIterationCount( int count )
		{
			this->count = count;
		}

	private:
		void pressEnterToContinue()
		{
			do 
			{
				cout << '\n' << "Press ENTER to continue...";
			} while (cin.get() != '\n');
		}

		string getRandomString( int challengeLength )
		{
			static const char VALID_CHARACTERS[] = "abcdefghijklmnopqrstuvwxyz0123456789";
			mt19937_64 gen{ std::random_device()() };
			uniform_int_distribution<> distribution( 0, sizeof(VALID_CHARACTERS) - 2 );

			string buffer( challengeLength, ' ' );
			for( int i = 0; i < challengeLength; i++ ) {
				buffer[i] = VALID_CHARACTERS[ distribution(gen) ];
			}
			return buffer;
		}

		int generateBeginIndex( int challengeLength ) {
			std::mt19937_64 gen{ std::random_device()() };
			std::uniform_int_distribution<> distribution( 0, sizeof(challengeLength - 1) );
			return distribution( gen );
		}

		Challenge generateChallenge()
		{
			Challenge challenge;
			CHALLENGE_ID++;
			string challengeId = getFederateConfiguration()->getFederateName() + "#" + to_string( CHALLENGE_ID );
			challenge.challengeId = challengeId;
			challenge.stringValue =  getRandomString( CHALLENGE_LENGTH );
			challenge.beginIndex = generateBeginIndex( CHALLENGE_LENGTH );
			return challenge;
		}

		bool isCorrect( string originalString, string answer, int subStrIndex)
		{
			string expectedAnswer = originalString.substr( subStrIndex );
			return expectedAnswer == answer;
		}

	private:
		map<string, shared_ptr<ChallengeObject>> sentChallengeObjects;
		map<string, shared_ptr<ChallengeInteraction>> sentChallengeInteractions;
		list<shared_ptr<ResponseInteraction>> responseInteractions;
		mutex mutexLock;
		int count = 0;
		ofstream errorLog;
};

int main( int argc, char* argv[] )
{
	ChallengeFederate *fed = new ChallengeFederate();
	bool cFound = false;
	for( int i = 1; i < argc; i++ )
	{
		if( string(argv[i]) == "-c" )
		{
			fed->setIterationCount( atoi(argv[i + 1]) );
			cout << "-----------------------------------------------------------------------" << endl;
			cout << "Federate is configured to run for " << atoi(argv[i + 1]) << " rounds." << endl;
			cout << "-----------------------------------------------------------------------" << endl;
			cFound = true;
		}
	}
	if( !cFound )
	{
		cout << "Federate will run for infinitely many rounds.";
	}

	shared_ptr<base::FederateConfiguration> federateConfig = fed->getFederateConfiguration();
	federateConfig->setFederationName( string("ChallengeResponse") );
	federateConfig->setFederateName( string("CppChallenger") );
	federateConfig->setFederateType( string("CppChallenger") );
	federateConfig->setLookAhead( 0.2f );
	federateConfig->setTimeStepSize( 1.0f );
	federateConfig->addFomPath( string("ChallengeResponse/fom/ChallengeResponse.xml") );
	federateConfig->addSomPath( string("ChallengeResponse/som/Challenge.xml") );
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

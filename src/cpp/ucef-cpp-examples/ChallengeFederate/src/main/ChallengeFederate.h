#pragma once

#include <fstream>
#include <mutex>

#include "ChallengeInteraction.h"
#include "ChallengeObject.h"

#include "_ChallengeFederate.h"

struct Challenge
{
	std::string challengeId;
	std::string stringValue;
	int beginIndex;
};

class ChallengeFederate : public _ChallengeFederate
{
	
	public:
		//----------------------------------------------------------
		//                     Constructors
		//----------------------------------------------------------
		ChallengeFederate();

		virtual ~ChallengeFederate();

		//----------------------------------------------------------
		//          Lifecycle hooks implementation
		//----------------------------------------------------------
		virtual void beforeReadyToPopulate() override;
		virtual void beforeReadyToRun() override;
		virtual void beforeFirstStep() override;
		virtual void beforeReadyToResign() override;
		virtual void beforeExit() override;
		virtual bool step( double federateTime ) override;
		//----------------------------------------------------------
		//         Implement Callback Methods
		//----------------------------------------------------------
		virtual void receivedObjectRegistration( std::shared_ptr<const base::HLAObject> hlaObject,
		                                         double federateTime ) override;

		virtual void receivedAttributeReflection( std::shared_ptr<const base::HLAObject> hlaObject,
		                                          double federateTime ) override;

		virtual void receivedObjectDeletion( std::shared_ptr<const base::HLAObject> hlaObject ) override;
		virtual void receivedSimStart( std::shared_ptr<const base::ucef::SimStart> hlaInt,
		                               double federateTime ) override;

		virtual void receivedSimEnd( std::shared_ptr<const base::ucef::SimEnd> hlaInt,
		                             double federateTime ) override;

		virtual void receivedSimPaused( std::shared_ptr<const base::ucef::SimPause> hlaInt,
		                                double federateTime) override;

		virtual void receivedSimResumed( std::shared_ptr<const base::ucef::SimResume> hlaInt,
		                                 double federateTime ) override;

		virtual void receivedResponseInteraction( ResponseInteraction hlaInt ) override;

		void setIterationCount( int count );
	private:
		void pressEnterToContinue();
		std::string getRandomString( int challengeLength );
		int generateBeginIndex( int challengeLength );
		Challenge generateChallenge();
		bool isCorrect( std::string originalString, std::string answer, int subStrIndex);

	private:
		std::map<std::string, std::shared_ptr<ChallengeObject>> sentChallengeObjects;
		std::map<std::string, std::shared_ptr<ChallengeInteraction>> sentChallengeInteractions;
		std::list<ResponseInteraction> responseInteractions;
		std::mutex mutexLock;
		int count = 0;
		std::ofstream errorLog;
};

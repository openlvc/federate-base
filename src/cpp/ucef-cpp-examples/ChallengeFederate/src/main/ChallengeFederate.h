#pragma once

#include <fstream>
#include <mutex>

#include "ChallengeInteraction.h"
#include "ChallengeObject.h"
#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"
#include "ResponseInteraction.h"

struct Challenge
{
	std::string challengeId;
	std::string stringValue;
	int beginIndex;
};

class ChallengeFederate : public base::ucef::NoOpFederate
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

		virtual void receivedInteraction( std::shared_ptr<const base::HLAInteraction> hlaInt,
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
		std::list<std::shared_ptr<ResponseInteraction>> responseInteractions;
		std::mutex mutexLock;
		int count = 0;
		std::ofstream errorLog;
};

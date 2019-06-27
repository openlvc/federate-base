#pragma once

#include <mutex>
#include <list>
#include <mutex>

#include "ChallengeInteraction.h"
#include "ChallengeObject.h"
#include "ResponseInteraction.h"
#include "_ResponseFederate.h"

struct Response
{
	std::string challengeId;
	std::string resultString;
};

class ResponseFederate : public _ResponseFederate
{
	
	public:
		//----------------------------------------------------------
		//                     Constructors
		//----------------------------------------------------------
		ResponseFederate();

		virtual ~ResponseFederate();

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

		virtual void receiveChallengeObject( ChallengeObject challengeObj ) override;

		virtual void receivedChallengeInteraction( ChallengeInteraction challengeInt ) override;

		virtual void receivedObjectDeletion( std::shared_ptr<const base::HLAObject> hlaObject ) override;
		virtual void receivedSimStart( std::shared_ptr<const base::ucef::SimStart> hlaInt,
		                               double federateTime ) override;

		virtual void receivedSimEnd( std::shared_ptr<const base::ucef::SimEnd> hlaInt,
		                             double federateTime ) override;

		virtual void receivedSimPaused( std::shared_ptr<const base::ucef::SimPause> hlaInt,
		                                double federateTime) override;

		virtual void receivedSimResumed( std::shared_ptr<const base::ucef::SimResume> hlaInt,
		                                 double federateTime ) override;
	private:
		void pressEnterToContinue();
		std::shared_ptr<base::HLAInteraction> generateResponseInteraction( Response response );
		Response solveChallenge( ChallengeObject receievedChallenge );
		Response solveChallenge( ChallengeInteraction receievedChallenge );

	private:
		std::list<ChallengeObject> remoteChallengeObjects;
		std::list<ChallengeInteraction> remoteChallengeInteractions;
		std::mutex challengeMutex;
		int count;
};

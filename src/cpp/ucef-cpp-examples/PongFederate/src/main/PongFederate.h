#pragma once

#include <mutex>

#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"

class PongFederate : public base::ucef::NoOpFederate
{
	
	public:
		//----------------------------------------------------------
		//                     Constructors
		//----------------------------------------------------------
		PongFederate();

		virtual ~PongFederate();

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
	private:
		std::mutex mutexLock;
		bool sendPong;
};

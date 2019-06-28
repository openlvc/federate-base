#pragma once

#include "ChallengeInteraction.h"
#include "ChallengeObject.h"

#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"


class _ResponseFederate : public base::ucef::NoOpFederate
{
	
	public:
		virtual void receivedInteraction( std::shared_ptr<const base::HLAInteraction> hlaInt,
		                                  double federateTime ) override;

		virtual void receivedAttributeReflection( std::shared_ptr<const base::HLAObject> hlaObject,
		                                          double federateTime) override;


		virtual void receivedChallengeInteraction( ChallengeInteraction hlaInt ) = 0;

		virtual void receiveChallengeObject( ChallengeObject hlaObj ) = 0;
};

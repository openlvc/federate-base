#pragma once

#include "gov/nist/ucef/hla/ucef/NoOpFederate.h"
#include "ResponseInteraction.h"

class _ChallengeFederate : public base::ucef::NoOpFederate
{
	
	public:
		virtual void receivedInteraction( std::shared_ptr<const base::HLAInteraction> hlaInt,
		                                  double federateTime ) override;

		virtual void receivedResponseInteraction( ResponseInteraction hlaInt ) = 0;
};

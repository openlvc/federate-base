#include "gov/nist/ucef/hla/ucef/UCEFFederateBase.h"

#include <cstring>

#include "gov/nist/ucef/hla/base/FederateAmbassador.h"
#include "gov/nist/ucef/util/Logger.h"


using namespace rti1516e;
using namespace std;
using namespace base::util;

namespace base
{
	namespace ucef
	{

		UCEFFederateBase::UCEFFederateBase() : simEndReceived( false )
		{

		}

		UCEFFederateBase::~UCEFFederateBase()
		{

		}

		void UCEFFederateBase::incomingInteraction( long interactionHash,
		                                            const ParameterHandleValueMap& parameterValues )
		{
			lock_guard<mutex> lock( threadSafeLock );
			Logger& logger = Logger::getInstance();
			shared_ptr<InteractionClass> interactionClass = getInteractionClass( interactionHash );
			logger.log( "Received interaction update for " + interactionClass->name, LevelInfo );
			if( interactionClass )
			{
				shared_ptr<HLAInteraction> hlaInteraction;
				if( interactionClass->name == SimEnd::INTERACTION_NAME )
				{
					ucefConfig->synchAtResign( true );
					// create correct interaction based on the class name
					hlaInteraction = make_shared<SimEnd>( interactionClass->name );
					// populate interaction with received data
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					// call the right hook so users can do whatever they want to do with this interaction
					receivedSimEnd( dynamic_pointer_cast<SimEnd>(hlaInteraction),
					                federateAmbassador->getFederateTime() );
					// this execure receivedSimEnd call at least once before ending the sim
					simEndReceived = true;
				}
				else if( interactionClass->name == SimPause::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimPause>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					receivedSimPaused( dynamic_pointer_cast<SimPause>(hlaInteraction),
					                   federateAmbassador->getFederateTime() );
				}
				else if( interactionClass->name == SimResume::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimResume>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					receivedSimResumed( dynamic_pointer_cast<SimResume>(hlaInteraction),
					                    federateAmbassador->getFederateTime() );
				}
				else if( interactionClass->name == SimStart::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimStart>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					receivedSimStart( dynamic_pointer_cast<SimStart>(hlaInteraction),
					                  federateAmbassador->getFederateTime() );
				}
				else
				{
					hlaInteraction = make_shared<HLAInteraction>( interactionClass->name );
					populateInteraction( interactionClass->name, hlaInteraction, parameterValues );
					receivedInteraction( const_pointer_cast<const HLAInteraction>(hlaInteraction),
					                     federateAmbassador->getFederateTime() );
				}
			}
			else
			{
				logger.log( "Received an unknown interation with interaction id " +
				            to_string(interactionHash), LevelWarn );
			}
		}

		void UCEFFederateBase::federateExecute()
		{
			while( !simEndReceived )
			{
				if( step(federateAmbassador->getFederateTime()) == false )
					break;
				advanceTime();
			}
		}
	}
}

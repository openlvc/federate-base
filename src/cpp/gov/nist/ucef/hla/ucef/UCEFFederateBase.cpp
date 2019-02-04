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

		UCEFFederateBase::UCEFFederateBase()
		{

		}

		UCEFFederateBase::~UCEFFederateBase()
		{

		}

		void UCEFFederateBase::incomingInteraction( long interactionHash,
		                                            const ParameterHandleValueMap& parameterValues )
		{
			lock_guard<mutex> lock( m_threadSafeLock );
			Logger& logger = Logger::getInstance();
			shared_ptr<InteractionClass> interactionClass = getInteractionClass( interactionHash );
			logger.log( "Received interaction update for " + interactionClass->name, LevelInfo );
			if( interactionClass )
			{
				shared_ptr<HLAInteraction> hlaInteraction;
				if( interactionClass->name == SimEnd::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimEnd>( interactionClass->name );
				}
				else if( interactionClass->name == SimPause::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimPause>( interactionClass->name );
				}
				else if( interactionClass->name == SimResume::INTERACTION_NAME )
				{
					hlaInteraction = make_shared<SimResume>( interactionClass->name );
				}
				else
				{
					hlaInteraction = make_shared<HLAInteraction>( interactionClass->name );
				}

				InteractionClassHandle interactionHandle =
					rtiAmbassadorWrapper->getInteractionHandle( interactionClass->name );

				if( !interactionHandle.isValid() )
				{
					logger.log( "No valid interaction handle found for the received interaction of " +
					            interactionClass->name, LevelWarn );
					return;
				}

				for( auto& incomingParameterValue : parameterValues )
				{
					string paramName =
						rtiAmbassadorWrapper->getParameterName( interactionHandle, incomingParameterValue.first );
					if( paramName == "" )
					{
						logger.log( "No valid parameter name found for the received parameter with id : " +
						            to_string(incomingParameterValue.first.hash()), LevelWarn );
						continue;
					}

					size_t size = incomingParameterValue.second.size();
					const void* data = incomingParameterValue.second.data();
					shared_ptr<void> arr(new char[size](), [](char *p) { delete[] p; });
					memcpy(arr.get(), data, size);
					hlaInteraction->setValue(paramName, arr, size);
				}

				if (interactionClass->name == SimEnd::INTERACTION_NAME )
				{
					receivedSimEnd( dynamic_pointer_cast<SimEnd>(hlaInteraction),
					                federateAmbassador->getFederateTime() );
				}
				else if( interactionClass->name == SimPause::INTERACTION_NAME )
				{
					receivedSimPaused( dynamic_pointer_cast<SimPause>(hlaInteraction),
					                   federateAmbassador->getFederateTime() );
				}
				else if( interactionClass->name == SimResume::INTERACTION_NAME )
				{
					receivedSimResumed( dynamic_pointer_cast<SimResume>(hlaInteraction),
					                    federateAmbassador->getFederateTime() );
				}
				else
				{
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
	}
}

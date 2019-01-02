#pragma once

#include <memory>
#include <mutex>
#include <vector>

#include "gov/nist/ucef/hla/base/FederateBase.h"
#include "gov/nist/ucef/hla/ucef/interactions/SimEnd.h"
#include "gov/nist/ucef/hla/ucef/interactions/SimPause.h"
#include "gov/nist/ucef/hla/ucef/interactions/SimResume.h"

namespace base
{
	namespace ucef
	{
		class FederateAmbassador;
		/**
		 * The {@link UCEFFederateBase} extends {@link FederateBase} to implement UCEF specific hla federate
		 */
		class UCEF_API UCEFFederateBase : public FederateBase
		{
		public:

			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			UCEFFederateBase();
			virtual ~UCEFFederateBase();
			UCEFFederateBase( const UCEFFederateBase& ) = delete;
			UCEFFederateBase& operator=( const UCEFFederateBase& ) = delete;

			//----------------------------------------------------------
			//       IFederateBase interface implementation
			//----------------------------------------------------------

			/**
			 * Called by {@link FederateAmbassador} whenever RTI receives a new interaction
			 *
			 * @param interactionHash the unique hash of the received object interaction
			 * @param parameterValues a map that contains parameters and values of the
			 *                        received interaction
			 */
			virtual void incomingInteraction
			                     ( long interactionHash,
			                       const std::map<rti1516e::ParameterHandle, rti1516e::VariableLengthData>& parameterValues );

			//----------------------------------------------------------
			//                     Callback Methods
			//----------------------------------------------------------

			/**
			 * Get called whenever RTI receives ucef specific simulation end interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the interaction class represented
			 *                       by {@link HLAInteraction#getClassName()}. Use {@link HLAInteraction#getAs***}
			 *                       methods to get the values of the received parameter updates. Since no type checking
			 *                       is carried out it is important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimEnd( std::shared_ptr<const SimEnd> hlaInteraction,
			                             double federateTime) = 0;

			/**
			 * Get called whenever RTI receives ucef specific simulation paused interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the interaction class represented
			 *                       by {@link HLAInteraction#getClassName()}. Use {@link HLAInteraction#getAs***}
			 *                       methods to get the values of the received parameter updates. Since no type checking
			 *                       is carried out it is important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimPaused( std::shared_ptr<const SimPause> hlaInteraction,
			                                double federateTime) = 0;

			/**
			 * Get called whenever RTI receives ucef specific simulation resumed interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relavant to the interaction class represented
			 *                       by {@link HLAInteraction#getClassName()}. Use {@link HLAInteraction#getAs***}
			 *                       methods to get the values of the received parameter updates. Since no type checking
			 *                       is carried out it is important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimResumed( std::shared_ptr<const SimResume> hlaInteraction,
			                                 double federateTime) = 0;
		};
	}
}
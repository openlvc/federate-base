/*
 *  This software is contributed as a public service by
 *  The National Institute of Standards and Technology(NIST)
 *  and is not subject to U.S.Copyright.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files(the "Software"), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify,
 *  merge, publish, distribute, sublicense, and / or sell copies of the
 *  Software, and to permit persons to whom the Software is furnished
 *  to do so, subject to the following conditions :
 *
 *               The above NIST contribution notice and this permission
 *               and disclaimer notice shall be included in all copies
 *               or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.THE AUTHOR
 *  OR COPYRIGHT HOLDERS SHALL NOT HAVE ANY OBLIGATION TO PROVIDE
 *  MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
#pragma once

#include <memory>
#include <mutex>
#include <vector>

#include "gov/nist/ucef/hla/base/FederateBase.h"
#include "gov/nist/ucef/hla/ucef/interactions/SimEnd.h"
#include "gov/nist/ucef/hla/ucef/interactions/SimPause.h"
#include "gov/nist/ucef/hla/ucef/interactions/SimResume.h"
#include "gov/nist/ucef/hla/ucef/interactions/SimStart.h"

namespace base
{
	namespace ucef
	{
		class FederateAmbassador;
		/**
		 * The {@link UCEFFederateBase} extends {@link FederateBase} to implement UCEF specific hla federate
		 */
		class UCEFFederateBase : public FederateBase
		{
		public:

			static std::string KEY_OMNET_INTERACTIONS;
			static std::string KEY_NET_INT_NAME;

			static std::string KEY_OMNET_CONFIG;
			static std::string KEY_SRC_HOST;
			static std::string KEY_ORG_CLASS;
			static std::string KEY_NET_DATA;
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
			                       const std::map<rti1516e::ParameterHandle, rti1516e::VariableLengthData>& parameterValues ) override;

			/**
			 * Initialize UCEF fededrate from the given JSON config file
			 *
			 * @param configFilePath path to the federate config json
			 */
			virtual void configureFromJSON( const std::string& configFilePath ) override;

		protected:

			/**
			 * Sends an interaction to the federation that this federate is part of.
			 * <p/>
			 * When sending an interaction to the federation this method will fist check
			 * whether the passed interaction type is designated to an OMNeT federate. This can
			 * be specified in federate config file. If so, it will first convert the passed
			 * interaction into a Network interaction type before sending to the federation.
			 *
			 * @param hlaInteraction object that holds the values of the interaction
			 *        parameters that need to be published by this federate
			 */
			void sendInteraction( std::shared_ptr<HLAInteraction>& hlaInteraction );

			//----------------------------------------------------------
			//                     Callback Methods
			//----------------------------------------------------------

			/**
			 * Get called whenever RTI receives UCEF specific simulation start interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relevant to the SimStart interaction.
			 *                       Use {@link HLAInteraction#getAs***} methods to get the values of the
			 *                       received parameter updates. Since no type checking is carried out it is
			 *                       important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimStart( std::shared_ptr<const SimStart> hlaInteraction,
			                               double federateTime ) = 0;
			/**
			 * Get called whenever RTI receives UCEF specific simulation end interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relevant to the SimEnd interaction.
			 *                       Use {@link HLAInteraction#getAs***} methods to get the values of the
			 *                       received parameter updates. Since no type checking is carried out it is
			 *                       important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimEnd( std::shared_ptr<const SimEnd> hlaInteraction,
			                             double federateTime ) = 0;

			/**
			 * Get called whenever RTI receives UCEF specific simulation pause interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relevant to the SimPause interaction.
			 *                       Use {@link HLAInteraction#getAs***} methods to get the values of the
			 *                       received parameter updates. Since no type checking is carried out it is
			 *                       important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimPaused( std::shared_ptr<const SimPause> hlaInteraction,
			                                double federateTime ) = 0;

			/**
			 * Get called whenever RTI receives UCEF specific simulation resume interaction
			 *
			 * @param hlaInteraction Stores the received parameter updates relevant to the SimResume interaction.
			 *                       Use {@link HLAInteraction#getAs***} methods to get the values of the
			 *                       received parameter updates. Since no type checking is carried out it is
			 *                       important to use the right methods to obtain the correct values.
			 * @param federateTime the current logical time of the federate
			 */
			virtual void receivedSimResumed( std::shared_ptr<const SimResume> hlaInteraction,
			                                 double federateTime ) = 0;
			/**
			 * The main execution loop of the UCEF federate
			 */
			virtual void federateExecute() override;

		private:
			/**
			 * A utility method to determine if a ginve interaction class name corresponds to one of the
			 * simulation control interactions
			 *
			 * @param interactionName the name of the interaction instance to check
			 * @return true if the interaction is one of the simulation control interactions, false
			 *         otherwise
			 */
			bool isSimulationControlInteraction( const std::string& interactionName );

			/**
			 * Determine if an incoming interaction should be received by this federate.
			 *
			 * This is determined by using encapsulated "federateFilter" parameter. This contains
			 * comma separated matching strings which are checked against the federate name.
			 *
			 * If the "federateFilter" parameter is absent or if any of the matching strings
			 * match the federate name, federate will receive the interaction.
			 *
			 * @param interaction the simulation control interaction
			 */
			bool shouldReceiveInteraction( std::shared_ptr<HLAInteraction>& hlaInteraction );

			/**
			 * A utility method to determine if a given interaction class must be
			 * converted to a network interaction for OMNeT++ routing
			 *
			 * @param className the name of the interaction/object instance to check
			 * @return true if the interaction must be routed via OMNeT++, false otherwise
			 */
			bool isNetworkInteraction( const std::string& className );

			/**
			 * General handler for received simulation control interactions
			 * ({@link SimStart},{@link SimEnd}, {@link SimPause}, {@link SimResume})
			 *
			 * @param interactionClass the simulation control interaction
			 * @param parameterValues a map that contains parameters and values of the
			 *                        received interaction
			 */
			void processSimControlInteraction( std::shared_ptr<InteractionClass>& interactionClass,
			                                   const std::map<rti1516e::ParameterHandle, rti1516e::VariableLengthData>& parameterValues );

			/**
			 * Convert the parameters of the given interaction into a JSON string
			 *
			 * @param hlaInteraction object interaction that holds the values of the
			 *                      parameters
			 * @return JSON string representation of interaction parameters
			 **/
			std::string hlaToJsonString( std::shared_ptr<HLAInteraction>& hlaInteraction );

		protected:
			std::string netInteractionName;
		private:
			bool simEndReceived;
			std::string srcHost;
			std::list<std::string> omnetInteractions;
			std::list<std::regex> omnetInteractionsInRegex;
		};
	}
}

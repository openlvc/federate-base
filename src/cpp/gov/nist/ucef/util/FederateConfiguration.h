#pragma once

#include <string>
#include <vector>

namespace ucef
{
	namespace util
	{
		class FederateConfiguration
		{
			public:
				//----------------------------------------------------------
				//                     Constructors
				//----------------------------------------------------------
				FederateConfiguration();

				/**
				 * Returns the name of the federation
				 * 
				 * @return the federation name
				 */
				std::string getFederationName();

				/**
				 * Returns the name of the federate
				 * 
				 * @return the federate name
				 */
				std::string getFederateName();

				/**
				 * Returns the type of the federate
				 * 
				 * @return the federate type
				 */
				std::string getFederateType();

				/**
				 * Returns paths to FOM files
				 * 
				 * @return paths to FOM files
				 */
				std::vector<std::string> getFomPaths();

				/**
				 * Returns the path to a SOM file
				 * 
				 * @return the path to a SOM file
				 */
				std::string getSomPath();

				/**
				 * Returns the lookahead value of a time regulating federate
				 * 
				 * The default lookahead value is set to 1.
				 *
				 * @return the lookahead value
				 */
				float getLookAhead();

				/**
				 * Returns the size of the time step in this federate.
				 *
				 * The default time step size is set to 1.
				 * 
				 * @return the time step size of this federate
				 */
				float getTimeStep();

				/**
				 * Returns the callback mode of this federate
				 * 
				 * @return true if HLAImmediate to be used, false for HLAEvoked
				 */
				bool isImmediate();

				/**
				 * Indicates whether this federate is a time regulated federate
				 * 
				 * @return true if this federate is time regulated, false otherwise
				 */
				bool isTimeRegulated();

				/**
				 * Indicates whether this federate is a time constrained federate
				 * 
				 * @return true if this federate is time constrained, false otherwise
				 */
				bool isTimeConstrained();

			private:
				std::string m_federateName;
				std::string m_federateType;
				float m_lookAhead;
				float m_timeStep;
				bool m_immediateCallBacks;
				bool m_timeRegulated;
				bool m_timeConstrained;
		};
	}
}
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
				 * Returns the configured federation name
				 * 
				 * @return the configured federation name
				 */
				std::string getFederationName();

				/**
				 * Returns the configured federate name
				 * 
				 * @return the configured federate name
				 */
				std::string getFederateName();

				/**
				 * Returns the configured federate type
				 * 
				 * @return the configured federate type
				 */
				std::string getFederateType();

				/**
				 * Returns the stored paths of FOM files
				 * 
				 * @return the stored paths of FOM files
				 */
				std::vector<std::string> getFomPaths();

				/**
				 * Returns the stored path of the SOM files
				 * 
				 * @return the stored path of the SOM files
				 */
				std::string getSomPath();

				/**
				 * Returns the lookahead value for time regulating federates
				 * 
				 * The default lookahead value is set to 1.
				 *
				 * @return the lookahead value
				 */
				float getLookAhead();

				/**
				 * Returns the time step value of this federate.
				 *
				 * The default time step value is set to 1.
				 * 
				 * @return the time step value of this federate
				 */
				float getTimeStep();

				/**
				 * Returns the callback configuration of this federate
				 * 
				 * @return true if HLAImmediate to be used, false HLAEvoked
				 */
				bool isImmediate();

				/**
				 * Indicates this federate is a time regulated federate
				 * 
				 * @return true if this federate is time regulated, false otherwise
				 */
				bool isTimeRegulated();

				/**
				 * Indicates this federate is a time constrained federate
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
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
				FederateConfiguration();

				std::string getFederationName();
				std::string getFederateName();
				std::string getFederateType();
				std::vector<std::wstring> getFomPaths();
				std::wstring getSomPath();
				float getLookAhead();
				float getTimeStep();
				bool isImmediate();
				bool isTimeRegulated();
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
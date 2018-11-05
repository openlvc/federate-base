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

				std::wstring getFederationName();
				std::wstring getFederateName();
				std::wstring getFederateType();
				std::vector<std::wstring> getFomPaths();
				std::string getSomPath();
				float getLookAhead();
				float getTimeStep();
				bool isImmediate();
				bool isTimeRegulated();
				bool isTimeConstrained();

			private:
				std::wstring m_federateName;
				std::wstring m_federateType;
				float m_lookAhead;
				float m_timeStep;
				bool m_immediateCallBacks;
				bool m_timeRegulated;
				bool m_timeConstrained;

		};
	}
}
#pragma once

#include <stdexcept>

#include "gov/nist/ucef/config.h"
#include "types.h"

namespace ucef
{
	namespace util
	{
		class UCEF_API UCEFException : public std::runtime_error
		{
		public:
			UCEFException( const std::string& message );
			UCEFException( const UCEFException& exception );
		};
	}
}

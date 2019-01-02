#pragma once

#include <stdexcept>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/hla/types.h"

namespace base
{
	/**
	 * The {@link UCEFException} wraps lower level exceptions (RTI exceptions)
	 * into a user friendly custom exception type.
	 */
	class UCEF_API UCEFException : public std::runtime_error
	{
	public:
		//----------------------------------------------------------
		//                     Constructors
		//----------------------------------------------------------
		UCEFException( const std::string& message );
		UCEFException( const UCEFException& exception );
	};
}

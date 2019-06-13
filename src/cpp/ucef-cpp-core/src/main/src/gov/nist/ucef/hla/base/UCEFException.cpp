#include "gov/nist/ucef/hla/base/UCEFException.h"

namespace base
{
	UCEFException::UCEFException( const std::string& message ) : std::runtime_error( message )
	{

	}

	UCEFException::UCEFException( const UCEFException& exception ) : std::runtime_error( exception )
	{

	}
}

#include "UCEFException.h"

namespace ucef
{
	namespace util
	{
		UCEFException::UCEFException( const std::string& message ) : std::runtime_error( message )
		{

		}

		UCEFException::UCEFException( const UCEFException& exception ) : std::runtime_error( exception )
		{

		}
	}
}
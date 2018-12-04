#include "UCEFDataTypeException.h"

namespace ucef
{
	namespace util
	{
		UCEFDataTypeException::UCEFDataTypeException( const std::string& message ) : UCEFException( message )
		{

		}

		UCEFDataTypeException::UCEFDataTypeException( const UCEFDataTypeException& exception ) : UCEFException( exception )
		{

		}
	}
}
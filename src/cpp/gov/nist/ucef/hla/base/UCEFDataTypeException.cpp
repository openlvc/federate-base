#include "UCEFDataTypeException.h"

using namespace std;
namespace base
{
	UCEFDataTypeException::UCEFDataTypeException( const string& message ) : UCEFException( message )
	{

	}

	UCEFDataTypeException::UCEFDataTypeException( const UCEFDataTypeException& exception ) : UCEFException( exception )
	{

	}
}
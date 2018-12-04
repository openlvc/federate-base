#pragma once

#include "gov/nist/ucef/util/UCEFException.h"

namespace ucef
{
	namespace util
	{
		/**
		 * The {@link UCEFDataTypeException} wraps any data type conversion errors
		 *
		 */
		class UCEF_API UCEFDataTypeException : public UCEFException
		{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			UCEFDataTypeException( const std::string& message );
			UCEFDataTypeException( const UCEFDataTypeException& exception );
		};
	}
}

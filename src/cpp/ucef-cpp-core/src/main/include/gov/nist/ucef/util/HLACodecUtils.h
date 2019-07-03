/*
 *  This software is contributed as a public service by
 *  The National Institute of Standards and Technology(NIST)
 *  and is not subject to U.S.Copyright.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files(the "Software"), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify,
 *  merge, publish, distribute, sublicense, and / or sell copies of the
 *  Software, and to permit persons to whom the Software is furnished
 *  to do so, subject to the following conditions :
 *
 *               The above NIST contribution notice and this permission
 *               and disclaimer notice shall be included in all copies
 *               or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.THE AUTHOR
 *  OR COPYRIGHT HOLDERS SHALL NOT HAVE ANY OBLIGATION TO PROVIDE
 *  MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
#pragma once

#include "gov/nist/ucef/hla/types.h"
#include "RTI/encoding/BasicDataElements.h"

namespace base
{
	namespace util
	{
		/**
		 * The {@link HLACodecUtils} defines static methods to encode and decode parameter and attribute data
		 * to/from Portico representation
		 */
		class HLACodecUtils
		{
			public:
				static VariableData setAsBool( const bool val );
				static VariableData setAsChar( const char val );
				static VariableData setAsShort( const short val );
				static VariableData setAsInt( const int val );
				static VariableData setAsLong( const long val );
				static VariableData setAsFloat( const float val );
				static VariableData setAsDouble( const double val );
				static VariableData setAsString( const std::string& val );

				static bool getAsBool( const VariableData& data );
				static char getAsChar( const VariableData& data );
				static short getAsShort( const VariableData& data );
				static int getAsInt( const VariableData& data );
				static long getAsLong( const VariableData& data );
				static float getAsFloat( const VariableData& data );
				static double getAsDouble( const VariableData& data );
				static std::string getAsString( const VariableData& data );
			private:
				static VariableData getVariableData( rti1516e::VariableLengthData& vData );

				static rti1516e::HLAboolean hlaBoolHelper;
				static rti1516e::HLAASCIIchar hlaCharHelper;
				static rti1516e::HLAinteger16BE hlaShortHelper;
				static rti1516e::HLAinteger32BE hlaIntHelper;
				static rti1516e::HLAinteger64BE hlaLongHelper;
				static rti1516e::HLAfloat32BE hlaFloatHelper;
				static rti1516e::HLAfloat64BE hlaDoubleHelper;
				static rti1516e::HLAASCIIstring hlaStringHelper;
		};

	}
}


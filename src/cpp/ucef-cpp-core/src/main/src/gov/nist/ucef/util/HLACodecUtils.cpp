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

#include "gov/nist/ucef/util/HLACodecUtils.h"
#include "RTI/VariableLengthData.h"

using namespace base;
using namespace base::util;
using namespace rti1516e;
using namespace std;

HLAboolean HLACodecUtils::hlaBoolHelper;
HLAASCIIchar HLACodecUtils::hlaCharHelper;
HLAinteger16BE HLACodecUtils::hlaShortHelper;
HLAinteger32BE HLACodecUtils::hlaIntHelper;
HLAinteger64BE HLACodecUtils::hlaLongHelper;
HLAfloat32BE HLACodecUtils::hlaFloatHelper;
HLAfloat64BE HLACodecUtils::hlaDoubleHelper;
HLAASCIIstring HLACodecUtils::hlaStringHelper;

VariableData HLACodecUtils::setAsBool( const bool val )
{
	hlaBoolHelper.set( val );
	VariableLengthData vData = hlaBoolHelper.encode();
	return HLACodecUtils::getVariableData( vData );

}
VariableData HLACodecUtils::setAsChar( const char val )
{
	hlaCharHelper.set( val );
	VariableLengthData vData = hlaCharHelper.encode();
	return HLACodecUtils::getVariableData( vData );

}
VariableData HLACodecUtils::setAsShort( const short val )
{
	hlaShortHelper.set( val );
	VariableLengthData vData = hlaShortHelper.encode();
	return HLACodecUtils::getVariableData( vData );
}
VariableData HLACodecUtils::setAsInt( const int val )
{
	hlaIntHelper.set( val );
	VariableLengthData vData = hlaIntHelper.encode();
	return HLACodecUtils::getVariableData( vData );
}
VariableData HLACodecUtils::setAsLong( const long val )
{
	hlaLongHelper.set( val );
	VariableLengthData vData = hlaLongHelper.encode();
	return HLACodecUtils::getVariableData( vData );
}
VariableData HLACodecUtils::setAsFloat( const float val )
{
	hlaFloatHelper.set( val );
	VariableLengthData vData = hlaFloatHelper.encode();
	return HLACodecUtils::getVariableData( vData );
}
VariableData HLACodecUtils::setAsDouble( const double val )
{
	hlaDoubleHelper.set( val );
	VariableLengthData vData = hlaDoubleHelper.encode();
	return HLACodecUtils::getVariableData( vData );
}
VariableData HLACodecUtils::setAsString( const string& val )
{
	hlaStringHelper.set( val );
	VariableLengthData vData = hlaStringHelper.encode();
	return HLACodecUtils::getVariableData( vData );
}

bool HLACodecUtils::getAsBool( const VariableData& val )
{
	 VariableLengthData vData( val.data.get(), val.size );
	 hlaBoolHelper.decode( vData );
	 return hlaBoolHelper.get();
}

char HLACodecUtils::getAsChar( const VariableData& data )
{
	 VariableLengthData vData( data.data.get(), data.size );
	 hlaCharHelper.decode( vData );
	 return hlaCharHelper.get();
}

short HLACodecUtils::getAsShort( const VariableData& data )
{
	 VariableLengthData vData( data.data.get(), data.size );
	 hlaShortHelper.decode( vData );
	 return hlaShortHelper.get();
}

int HLACodecUtils::getAsInt( const VariableData& data )
{
	 VariableLengthData vData( data.data.get(), data.size );
	 hlaIntHelper.decode( vData );
	 return hlaIntHelper.get();
}

long HLACodecUtils::getAsLong( const VariableData& data )
{
	 VariableLengthData vData( data.data.get(), data.size );
	 hlaLongHelper.decode( vData );
	 return hlaLongHelper.get();
}

float HLACodecUtils::getAsFloat( const VariableData& data )
{
	 VariableLengthData vData( data.data.get(), data.size );
	 hlaFloatHelper.decode( vData );
	 bool value = hlaFloatHelper.get();
	 return value;
}

double HLACodecUtils::getAsDouble( const VariableData& data )
{
	 VariableLengthData vData( data.data.get(), data.size );
	 hlaDoubleHelper.decode( vData );
	 return hlaDoubleHelper.get();
}

string HLACodecUtils::getAsString( const VariableData& data )
{
	 VariableLengthData vData( data.data.get(), data.size );
	 hlaStringHelper.decode( vData );
	 return hlaStringHelper.get();
}

VariableData HLACodecUtils::getVariableData( VariableLengthData& vData  )
{
	VariableData data;

	size_t size = vData.size();
	data.size = size;
	const void* dataPtr = vData.data();
	shared_ptr<void> vPtr(new char[size](), [](char *p) { delete [] p; });
	memcpy( vPtr.get(), dataPtr, size );
	data.data = vPtr;

	return data;
}

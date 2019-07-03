#include "ChallengeObject.h"

#include "gov/nist/ucef/hla/types.h"

using namespace base::util;
using namespace std;

ChallengeObject::ChallengeObject( shared_ptr<const HLAObject> hlaObject ) : HLAObject( *hlaObject )
{
}

ChallengeObject::ChallengeObject( const string& objectClassName ) : HLAObject( objectClassName )
{
}

ChallengeObject::~ChallengeObject()
{
}

void ChallengeObject::setChallengeId( string& id )
{
	setValue( "challengeId", ConversionHelper::s2ws(id) );
}

void ChallengeObject::setBeginIndex( int beginIndex )
{
	setValue( "beginIndex", beginIndex );
}

void ChallengeObject::setStringValue( string& textValue )
{
	setValue( "stringValue", ConversionHelper::s2ws(textValue) );
}

string ChallengeObject::getChallengeId()
{
	return ConversionHelper::ws2s( getAsWString(string("challengeId")) );
}

int ChallengeObject::getBeginIndex()
{
	return getAsInt("beginIndex");
}

string ChallengeObject::getStringValue()
{
	return ConversionHelper::ws2s( getAsWString(string("stringValue")) );
}

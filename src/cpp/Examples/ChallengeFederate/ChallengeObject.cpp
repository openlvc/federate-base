#include "ChallengeObject.h"

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
	setValue( "challengeId", id );
}

void ChallengeObject::setBeginIndex( int beginIndex )
{
	setValue( "beginIndex", beginIndex );
}

void ChallengeObject::setStringValue( string& textValue )
{
	setValue( "stringValue", textValue );
}

string ChallengeObject::getChallengeId()
{
	return getAsString("challengeId");
}

int ChallengeObject::getBeginIndex()
{
	return getAsInt("beginIndex");
}

string ChallengeObject::getStringValue()
{
	return getAsString("stringValue");
}

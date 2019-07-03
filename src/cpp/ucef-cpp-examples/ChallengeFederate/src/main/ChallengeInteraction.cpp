#include "ChallengeInteraction.h"

#include "gov/nist/ucef/hla/types.h"

using namespace base::util;
using namespace std;

ChallengeInteraction::ChallengeInteraction( shared_ptr<const HLAInteraction> hlaInteraction ) : HLAInteraction( *hlaInteraction)
{
}

ChallengeInteraction::ChallengeInteraction( const string& interactionName ) : HLAInteraction( interactionName )
{
}

ChallengeInteraction::~ChallengeInteraction()
{
}

void ChallengeInteraction::setChallengeId( string& id )
{
	setValue( "challengeId", ConversionHelper::s2ws(id) );
}

void ChallengeInteraction::setBeginIndex( int beginIndex )
{
	setValue( "beginIndex", beginIndex );
}

void ChallengeInteraction::setStringValue( string& textValue )
{
	setValue( "stringValue", ConversionHelper::s2ws(textValue) );
}

string ChallengeInteraction::getChallengeId()
{
	return ConversionHelper::ws2s( getAsWString(string("challengeId")) );
}

int ChallengeInteraction::getBeginIndex()
{
	return getAsInt( "beginIndex" );
}

string ChallengeInteraction::getStringValue()
{
	return ConversionHelper::ws2s( getAsWString(string("stringValue")) );
}

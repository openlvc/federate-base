#include "ChallengeInteraction.h"

using namespace base;
using namespace std;

ChallengeInteraction::ChallengeInteraction( shared_ptr<const HLAInteraction> hlaInteraction ) : HLAInteraction( *hlaInteraction)
{

}

ChallengeInteraction::~ChallengeInteraction()
{
}

void ChallengeInteraction::setChallengeId( string& id )
{
	setValue( "challengeId", id );
}

void ChallengeInteraction::setBeginIndex( int beginIndex )
{
	setValue( "beginIndex", beginIndex );
}

void ChallengeInteraction::setStringValue( string& textValue )
{
	setValue( "stringValue", textValue );
}

string ChallengeInteraction::getChallengeId()
{
	return getAsString( "challengeId" );
}

int ChallengeInteraction::getBeginIndex()
{
	return getAsInt( "beginIndex" );
}

string ChallengeInteraction::getStringValue()
{
	return getAsString( "stringValue" );
}

#include "ResponseInteraction.h"

using namespace base;
using namespace std;

ResponseInteraction::ResponseInteraction( const string& interactionName ) : HLAInteraction( interactionName )
{

}

ResponseInteraction::ResponseInteraction( shared_ptr<const HLAInteraction> hlaInteraction ) : HLAInteraction( *hlaInteraction )
{

}

ResponseInteraction::~ResponseInteraction()
{
}

void ResponseInteraction::setChallengeId( string& id )
{
	setValue( "challengeId", id );
}

void ResponseInteraction::setSubStringValue( string& textValue )
{
	setValue( "substring", textValue );
}

std::string ResponseInteraction::getChallengeId()
{
	return getAsString( "challengeId" );
}

std::string ResponseInteraction::getSubStringValue()
{
	return getAsString( "substring" );
}

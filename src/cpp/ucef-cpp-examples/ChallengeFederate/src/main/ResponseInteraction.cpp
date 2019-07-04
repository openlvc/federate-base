#include "ResponseInteraction.h"

#include "gov/nist/ucef/hla/types.h"

using namespace base::util;
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
	setValue( "challengeId", ConversionHelper::s2ws(id) );
}

void ResponseInteraction::setSubStringValue( string& textValue )
{
	setValue( "substring", ConversionHelper::s2ws(textValue) );
}

std::string ResponseInteraction::getChallengeId()
{
	return ConversionHelper::ws2s( getAsWString("challengeId") );
}

std::string ResponseInteraction::getSubStringValue()
{
	return ConversionHelper::ws2s( getAsWString(string("substring")) );
}

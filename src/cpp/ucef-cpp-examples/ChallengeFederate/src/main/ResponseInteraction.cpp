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

std::string ResponseInteraction::getChallengeId()
{
	return ConversionHelper::ws2s( getAsWString("challengeId") );
}

std::string ResponseInteraction::getSubStringValue()
{
	return ConversionHelper::ws2s( getAsWString("substring") );
}

#include "SOMParser.h"

namespace ucef
{
	namespace util
	{
		list<shared_ptr<ObjectClass>> SOMParser::getObjectClasses( const string& SomFilePath,
		                                                           const string& SomFileName )
		{
			list<shared_ptr<ObjectClass>> SomObjects;

			return SomObjects;
		}

		list<shared_ptr<InteractionClass>> SOMParser::getInteractionClasses( const string& SomFilePath,
		                                                                     const string& SomFileName )
		{
			list<shared_ptr<InteractionClass>> SomInteractions;

			return SomInteractions;
		}
	}
}


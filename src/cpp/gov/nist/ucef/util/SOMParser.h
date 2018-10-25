#pragma once

#include "gov/nist/ucef/util/types.h"

using namespace std;

namespace ucef
{
	namespace util
	{
		/**
		 * @file SOMParser.h
		 *
		 * Defines static methods to parse a given SOM file and extract the name,
		 * and sharing state of object classes, interaction classes, attributes
		 * and parameters.
		 */
		class SOMParser
		{
			public:

				//----------------------------------------------------------
				//                     Static Methods
				//----------------------------------------------------------

				/*
				 * Parse the given SOM file and generate the object class list
				 *
				 * @param SomFilePath the path of the federate SOM file
				 * @param SomFileName the name of the federate SOM file
				 *
				 * @return all the object classes found in the given SOM file
				 */
				static list<shared_ptr<ObjectClass>> getObjectClasses( const string& SomFilePath,
				                                                       const string& SomFileName);

				/*
				 * Parse the given SOM file and generate the interaction class list
				 *
				 * @param SomFilePath the path of the federate SOM file
				 * @param SomFileName the name of the federate SOM file
				 *
				 * @return all the interaction classes found in the given SOM file
				 */
				static list<shared_ptr<InteractionClass>> getInteractionClasses( const string& SomFilePath,
				                                                                 const string& SomFileName);
		};

	}
}


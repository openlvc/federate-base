#pragma once

#include <vector>

#include "gov/nist/ucef/util/types.h"

#include "tinyxml2.h"

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
				static vector<shared_ptr<ObjectClass>> getObjectClasses( const string& SomFilePath,
				                                                         const string& SomFileName);

				/*
				 * Parse the given SOM file and generate the interaction class list
				 *
				 * @param SomFilePath the path of the federate SOM file
				 * @param SomFileName the name of the federate SOM file
				 *
				 * @return all the interaction classes found in the given SOM file
				 */
				static vector<shared_ptr<InteractionClass>> getInteractionClasses( const string& SomFilePath,
				                                                                   const string& SomFileName);
			private:

				//----------------------------------------------------------
				//                    Private methods
				//----------------------------------------------------------

				/*
				 * Returns all the child XMLElements of a given parent XMLElement
				 *
				 * @param parentElement the parent element that must be used to get
				 *        the child elements
				 *
				 * @return all the child XMLElements of a given parent XMLElement
				 */
				static vector<tinyxml2::XMLElement*> getObjectClassChildElements( tinyxml2::XMLElement* parentElement );

				/*
				 * Traverse and collect fully qualified class names, object's sharing status,
				 * attribute names and their sharing statuses
				 *
				 * @param objectClassName is the fully qualified class name that get built up recursively
				 * @param attributes collect all the attributes found in each class during the
				 *        recursion
				 * @param parentElement the next XML element that is going to get explored
				 * @param objectClasses collects valid objectClasses in the passed SOM
				 *
				 */
				static void traverseObjectClasses( std::wstring objectClassName,
											std::vector<std::shared_ptr<ObjectAttribute>> attributes,
											tinyxml2::XMLElement* parentElement,
											std::vector<std::shared_ptr<ObjectClass>> &objectClasses);
		};

	}
}


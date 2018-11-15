#pragma once

#include <vector>

#include "gov/nist/ucef/util/types.h"
#include "tinyxml2.h"

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
				static std::vector<std::shared_ptr<ObjectClass>> getObjectClasses( const std::string& somFilePath );

				/*
				 * Parse the given SOM file and generate the interaction class list
				 *
				 * @param SomFilePath the path of the federate SOM file
				 * @param SomFileName the name of the federate SOM file
				 *
				 * @return all the interaction classes found in the given SOM file
				 */
				static std::vector<std::shared_ptr<InteractionClass>>
				                          getInteractionClasses( const std::string& somFilePath );
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
				static vector<tinyxml2::XMLElement*> getObjectClassChildElements( tinyxml2::XMLElement* parentElement,
				                                                                  const std::string& rootText);

				/*
				 * Traverse and collect fully qualified class names, object's sharing status,
				 * attribute names and their sharing statuses
				 *
				 * @param objectClassName is the fully qualified class name that get built recursively
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
				/*
				 * Traverse and collect fully qualified interaction class names, and parameter names.
				 *
				 * @param interactionClassName is the fully qualified interaction class
				 *        name that get built recursively
				 * @param params collect all the parameters found in each interaction class during the
				 *        recursion
				 * @param parentElement the next XML element that is going to get explored
				 * @param intClasses collects valid paramterClasses in the passed SOM
				 *
				 */
				static void traverseInteractionClasses( std::wstring interactionClassName,
				                                        std::vector<std::shared_ptr<InteractionParameter>> params,
				                                        tinyxml2::XMLElement* parentElement,
				                                        std::vector<std::shared_ptr<InteractionClass>> intClasses);
		};

	}
}


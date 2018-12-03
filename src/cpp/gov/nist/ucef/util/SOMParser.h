#pragma once

#include <vector>

#include "gov/nist/ucef/util/types.h"
#include "tinyxml2.h"

namespace ucef
{
	namespace util
	{
		/**
		 * The {@link SOMParser} defines static methods to parse a given SOM file and extract the name,
		 * and sharing state of object classes, interaction classes, attributes and parameters.
		 */
		class SOMParser
		{
			public:

				//----------------------------------------------------------
				//                     Static Methods
				//----------------------------------------------------------

				/*
				 * Parse the given SOM file and extract the object classes
				 *
				 * @param SomFilePath the full path to the federate's SOM file
				 *
				 * @return all the object classes found in the given SOM file
				 */
				static std::vector<std::shared_ptr<ObjectClass>> getObjectClasses( const std::string& somFilePath );

				/*
				 * Parse the given SOM file and extract the interaction classes
				 *
				 * @param SomFilePath the full path to the federate's SOM file
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
				static std::vector<tinyxml2::XMLElement*>
				             getClassChildElements( tinyxml2::XMLElement* parentElement,
				                                    const std::string& rootText);

				/*
				 * Traverse a given SOM file in BFS pattern to extract object classes.
				 * </p>
				 * This allows to collect fully qualified class names, attribute names,
				 * and sharing states.
				 *
				 * @param objectClassName the fully qualified class name holder that get appended recursively
				 * @param attributes collect all the attributes found in each class during the
				 *        recursion
				 * @param parentElement the next XML element that is going to get explored
				 * @param objectClasses collects valid objectClasses in the passed SOM
				 *
				 */
				static void traverseObjectClasses( std::string objectClassName,
				                                   std::vector<std::shared_ptr<ObjectAttribute>> attributes,
				                                   tinyxml2::XMLElement* parentElement,
				                                   std::vector<std::shared_ptr<ObjectClass>> &objectClasses);
				/*
				 * Traverse a given SOM file in BFS pattern to extract interaction classes.
				 * </p>
				 * This allows to collect fully qualified interaction class names, and parameter names.
				 *
				 * @param interactionClassName is the fully qualified interaction class
				 *        name holder that get appended recursively
				 * @param params collect all the parameters found in each interaction class during the
				 *        recursion
				 * @param parentElement the next XML element that is going to get explored
				 * @param intClasses collects valid paramterClasses in the passed SOM
				 *
				 */
				static void traverseInteractionClasses( std::string interactionClassName,
				                                        std::vector<std::shared_ptr<InteractionParameter>> params,
				                                        tinyxml2::XMLElement* parentElement,
				                                        std::vector<std::shared_ptr<InteractionClass>>& intClasses);
		};

	}
}


/*
 *  This software is contributed as a public service by
 *  The National Institute of Standards and Technology(NIST)
 *  and is not subject to U.S.Copyright.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files(the "Software"), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify,
 *  merge, publish, distribute, sublicense, and / or sell copies of the
 *  Software, and to permit persons to whom the Software is furnished
 *  to do so, subject to the following conditions :
 *
 *               The above NIST contribution notice and this permission
 *               and disclaimer notice shall be included in all copies
 *               or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.THE AUTHOR
 *  OR COPYRIGHT HOLDERS SHALL NOT HAVE ANY OBLIGATION TO PROVIDE
 *  MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
#pragma once

#include <vector>

#include "gov/nist/ucef/hla/types.h"
#include "tinyxml2.h"

namespace base
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


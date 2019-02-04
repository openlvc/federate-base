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

#include <string>
#include <vector>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/hla/types.h"

namespace base
{
	/**
	 * The {@link FederateConfiguration} class allows to provide configuration details to a federate as required.
	 */
	class UCEF_API FederateConfiguration
	{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			FederateConfiguration();

			//----------------------------------------------------------
			//                     Instance Methods
			//----------------------------------------------------------

			/**
			 * Returns the name of the federation that this federate wants to join
			 * 
			 * @return the name of the federation that this federate wants to join
			 */
			std::string getFederationName();

			/**
			 * Sets the name of the federation that this federate wants to join
			 *
			 * @param federationName the name of the federation that this federate wants to join
			 */
			void setFederationName( const std::string &federationName );

			/**
			 * Returns the name of the federate
			 * 
			 * @return the federate name
			 */
			std::string getFederateName();

			/**
			 * Sets the name of the federate
			 *
			 * @param federateName the name of this federate
			 */
			void setFederateName( const std::string &federateName );

			/**
			 * Returns the type of this federate
			 * 
			 * @return the federate type
			 */
			std::string getFederateType();

			/**
			 * Returns known FOM file paths by this federate
			 * 
			 * @return FOM file paths known to this federate
			 */
			std::vector<std::string> getFomPaths();

			/**
			 * Adds a FOM file path to provide information about 
			 * data that get exchanged in this federation
			 *
			 * @param path path to a FOM file
			 */
			void addFomPath( const std::string &path );

			/**
			 * Clears FOM paths added to federate configuration
			 */
			void clearFomPaths();

			/**
			 * Returns known SOM file paths by this federate
			 * 
			 * @return SOM file paths known to this federate
			 */
			std::vector<std::string> getSomPaths();

			/**
			 * Adds a SOM file path to provide information about
			 * data that must be published/subscribed by this federate.
			 * <p/>
			 * Note: Currently only a single SOM file get processed
			 *
			 * @param path path to a SOM file
			 */
			void addSomPath( const std::string &path );

			/**
			 * Returns lookahead value configured for time regulating federate
			 *
			 * @return the current lookahead value of this time regulating federate
			 */
			float getLookAhead();

			/**
			 * Sets a lookahead value for time regulating federate
			 * <p/>
			 * This value is optional and only required if this federate is a time
			 * regulating federate. The default lookahead value is set to 1.
			 *
			 * @param lookahead lookahead value to be set
			 */
			void setLookAhead( float lookahead );

			/**
			 * Returns step size of a tick.
			 * 
			 * @return the current step size
			 */
			float getTimeStep();

			/**
			 * Sets step size of a tick.
			 * <p/>
			 * The default step size is set to 1.
			 *
			 * @param timeStep step size to be set
			 */
			void setTimeStep( float stepSize );

			/**
			 * Returns the callback mode
			 * 
			 * @return true if HLAImmediate callback mode is set, false if HLAEvoked is set.
			 */
			bool isImmediate();

			/**
			 * Sets the callback mode
			 *
			 * @param callbackMode set to true if HLAImmediate to be used,
			 *        othrewise HLAEvoked is used
			 */
			void setImmediate( bool callbackMode );

			/**
			 * Indicates whether this federate is a time regulated federate
			 * 
			 * @return true if this federate is a time regulated, false otherwise
			 */
			bool isTimeRegulated();

			/**
			 * Configure this federate to be a time regulated federate
			 *
			 * @param timeRegulated set to true if this federate must be initialised as a
			 *        time regulated federate, false otherwise
			 */
			void setTimeRegulated( bool timeRegulated );

			/**
			 * Indicates whether this federate is a time-constrained federate
			 * 
			 * @return true if this federate is a time-constrained, false otherwise
			 */
			bool isTimeConstrained();

			/**
			 * Configure this federate to be a time-constrained federate
			 *
			 * @param timeConstrained set to true if this federate must be initialised as a
			 *        time-constrained, false otherwise
			 */
			void setTimeConstrained( bool timeConstrained );

			//----------------------------------------------------------
			//                 SOM data
			//----------------------------------------------------------

			/**
			 * Adds an object class that may either publish or subscribe by this federate
			 *
			 * @param objectClass object class as represented in SOM
			 */
			virtual void cacheObjectClass( std::shared_ptr<ObjectClass>& objectClass );

			/**
			 * Returns the fully qualified names of the object classes that
			 * are published by this federate.
			 *
			 * @return the names of the publishing object classes
			 */
			virtual std::vector<std::string> getClassNamesPublished();

			/**
			 * Returns the fully qualified names of the object classes that
			 * are subscribed by this federate.
			 *
			 * @return the names of the subscribed object classes
			 */
			virtual std::vector<std::string> getClassNamesSubscribed();

			/**
			 * Adds an interaction class that may either publish or subscribe by this federate
			 *
			 * @param interactionClass interaction class as represented in SOM
			 */
			virtual void cacheInteractionClass( std::shared_ptr<InteractionClass>& interactionClass );

			/**
			 * Returns the fully qualified names of the interaction classes that
			 * are published by this federate.
			 *
			 * @return the names of the publishing interaction classes
			 */
			virtual std::vector<std::string> getInteractionNamesPublished();

			/**
			 * Returns the fully qualified names of the interaction classes that
			 * are subscribed by this federate.
			 *
			 * @return the names of the subscribed interaction classes
			 */
			virtual std::vector<std::string> getInteractionNamesSubscribed();

			/**
			 * Returns the names of the publishing attributes of a given object class
			 *
			 * @param className the name of a object class
			 * @return publishing attributes of the given object class
			 */
			virtual std::vector<std::string> getAttributeNamesPublished( const std::string& className );

			/**
			 * Returns the names of the subscribed attributes of a given object class
			 *
			 * @param className the name of a object class
			 * @return subscribed attributes of the given object class
			 */
			virtual std::vector<std::string> getAttributeNamesSubscribed( const std::string& className );

			/**
			 * Returns parameter names of a given interaction class
			 *
			 * @param interactionName the name of a interaction class
			 * @return parameter names of the given interaction class
			 */
			virtual std::vector<std::string> getParameterNames( const std::string& interactionName );

		private:
			std::string federationName;
			std::string federateName;
			std::string federateType;
			std::vector<std::string> foms;
			std::string som;
			float lookAhead;
			float stepSize;
			bool immediateCallBacks;
			bool timeRegulated;
			bool timeConstrained;
			ObjectDataStoreByName objectDataStoreByName;
			InteractionDataStoreByName interactionDataStoreByName;
	};
}

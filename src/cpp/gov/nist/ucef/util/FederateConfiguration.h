#pragma once

#include <string>
#include <vector>

#include "gov/nist/ucef/util/types.h"

namespace ucef
{
	namespace util
	{
		/**
		 * The {@link FederateConfiguration} provides the configuration options to this federate.
		 */
		class FederateConfiguration
		{
			public:
				//----------------------------------------------------------
				//                     Constructors
				//----------------------------------------------------------
				FederateConfiguration();

				/**
				 * Returns the name of the federation
				 * 
				 * @return the federation name
				 */
				std::string getFederationName();

				/**
				 * Returns the name of the federate
				 * 
				 * @return the federate name
				 */
				std::string getFederateName();

				/**
				 * Returns the type of the federate
				 * 
				 * @return the federate type
				 */
				std::string getFederateType();

				/**
				 * Returns paths to FOM files
				 * 
				 * @return paths to FOM files
				 */
				std::vector<std::string> getFomPaths();

				/**
				 * Returns path to a SOM file
				 * 
				 * @return path to a SOM file
				 */
				std::string getSomPath();

				/**
				 * Returns the lookahead value of a time regulating federate
				 * <p/>
				 * The default lookahead value is set to 1.
				 *
				 * @return the lookahead value
				 */
				float getLookAhead();

				/**
				 * Returns the size of the time step.
				 * <p/>
				 * The default time step size is set to 1.
				 * 
				 * @return the time step size
				 */
				float getTimeStep();

				/**
				 * Returns the callback mode
				 * 
				 * @return true if HLAImmediate to be used, othrewise HLAEvoked is used
				 */
				bool isImmediate();

				/**
				 * Indicates whether this federate must be initialised as a time regulated federate
				 * 
				 * @return true if this federate must be initialised as a time regulated, false otherwise
				 */
				bool isTimeRegulated();

				/**
				 * Indicates whether this federate must be initialised as a time-constrained federate
				 * 
				 * @return true if this federate must be initialised as a time-constrained, false otherwise
				 */
				bool isTimeConstrained();

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
				virtual void cacheInteractionClass(std::shared_ptr<InteractionClass>& interactionClass);

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
				 * Returns the names of the publishing attributes of the given object class
				 *
				 * @param className the name of the class
				 * @return publishing attributes of the given object class
				 */
				virtual std::vector<std::string> getAttributeNamesPublished(const std::string& className);

				/**
				 * Returns the names of the subscribed attributes of the given object class
				 *
				 * @param className the name of the class
				 * @return subscribed attributes of the given object class
				 */
				virtual std::vector<std::string> getAttributeNamesSubscribed(const std::string& className);

				/**
				 * Returns the names of the parameters of the given interaction class
				 *
				 * @param interactionName the name of the interaction class
				 */
				virtual std::vector<std::string> getParameterNames(const std::string& interactionName);

			private:
				std::string m_federateName;
				std::string m_federateType;
				float m_lookAhead;
				float m_timeStep;
				bool m_immediateCallBacks;
				bool m_timeRegulated;
				bool m_timeConstrained;
				util::ObjectDataStoreByName m_objectDataStoreByName;
				util::InteractionDataStoreByName m_interactionDataStoreByName;
		};
	}
}
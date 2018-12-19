#pragma once

#include <string>
#include <vector>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"

namespace ucef
{
	namespace util
	{
		/**
		 * The {@link FederateConfiguration} class allows to configure a federate as required.
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
				 * @return the federation name
				 */
				std::string getFederationName();

				/**
				 * Sets the name of the federation that this federate wants to join
				 *
				 * @param federationName the name of the federation that this federate wants to join
				 */
				void setFederationName( std::string &federationName );

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
				void setFederateName( std::string &federateName );

				/**
				 * Returns the type of this federate
				 * 
				 * @return the federate type
				 */
				std::string getFederateType();

				/**
				 * Returns paths to FOM files
				 * 
				 * @return added FOM file paths
				 */
				std::vector<std::string> getFomPaths();

				/**
				 * Adds a FOM file path to federate configuration
				 *
				 * @param path path to a FOM file
				 */
				void addFomPath( std::string &path );

				/**
				 * Clears FOM paths added to federate configuration
				 */
				void clearFomPaths();

				/**
				 * Returns paths to SOM files
				 * 
				 * @return added SOM file paths
				 */
				std::vector<std::string> getSomPaths();

				/**
				 * Adds a SOM file path to federate configuration
				 * <p/>
				 * Note: Currently only a single SOM file get processed
				 *
				 * @param path path to a SOM file
				 */
				void addSomPath( std::string &path );

				/**
				 * Returns lookahead value of a time regulating federate
				 * <p/>
				 * The default lookahead value is set to 1.
				 *
				 * @return the lookahead value of this federate
				 */
				float getLookAhead();

				/**
				 * Sets a lookahead value of a time regulating federate
				 * <p/>
				 * The default lookahead value is set to 1.
				 *
				 * @param lookahead lookahead value to be set
				 */
				void setLookAhead( float lookahead );

				/**
				 * Returns step size of a tick.
				 * <p/>
				 * The default time step size is set to 1.
				 * 
				 * @return the time step size
				 */
				float getTimeStep();

				/**
				 * Sets the step size of a tick.
				 * <p/>
				 * The default time step size is set to 1.
				 *
			 	 * @return the time step size
				 */
				void setTimeStep( float timeStep );

				/**
				 * Returns the callback mode
				 * 
				 * @return true if HLAImmediate callback mode is set to use, othrewise HLAEvoked is used
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
				 * Sets this federate to be a time regulated federate
				 *
				 * @param timeRegulated set to true if this federate must be initialised as a
				 *        time regulated, false otherwise
				 */
				void setTimeRegulated( bool timeRegulated );

				/**
				 * Indicates whether this federate is a time-constrained federate
				 * 
				 * @return true if this federate is a time-constrained, false otherwise
				 */
				bool isTimeConstrained();

				/**
				 * Sets this federate to be a time-constrained federate
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
				 * Returns the names of the parameters of a given interaction class
				 *
				 * @param interactionName the name of a interaction class
				 */
				virtual std::vector<std::string> getParameterNames( const std::string& interactionName );

			private:
				std::string m_federationName;
				std::string m_federateName;
				std::string m_federateType;
				std::vector<std::string> m_foms;
				std::string m_som;
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
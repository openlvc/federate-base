#pragma once

#ifdef _WIN32
#include <functional>
#endif

#include <map>
#include <mutex>
#include <vector>

#include "gov/nist/ucef/config.h"
#include "gov/nist/ucef/util/types.h"

namespace ucef
{
	typedef std::map<std::string, util::VariableData> HLAInteractionParameters;
	/**
	 * The {@link HLAInteraction} represents a transient object that stores data for
	 * interaction publishing or data of a received HLA interaction.
	 *
	 * @see RTIAmbassadorWrapper#sendInteraction(std::shared_ptr<HLAInteraction>&)
	 * @see deleteObjectInstances#receiveInteraction(std::shared_ptr<const HLAInteraction>&)
	 */
	class UCEF_API HLAInteraction
	{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			HLAInteraction( const std::string& interactionClassName );
			virtual ~HLAInteraction();
			HLAInteraction( const HLAInteraction& ) = delete;
			HLAInteraction& operator=( const HLAInteraction& ) = delete;

			//----------------------------------------------------------
			//                     Instance Methods
			//----------------------------------------------------------

			/**
			 * Determines if this instance has the named parameter.
			 * 
			 * @param parameterName the name of the parameter
			 * @return true if the parameter is known by this instance, false otherwise
			 */
			bool isParameter( const std::string& parameterName ) const;

			/**
			 * Sets the value of the named parameter to a bool.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param val the value of the parameter
			 */
			void setValue( const std::string& parameterName, bool val );
			
			/**
			 * Sets the value of the named parameter to a char.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param val the value of the parameter
			 */
			void setValue( const std::string& parameterName, const char val );

			/**
			 * Sets the value of the named parameter to a short.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param val the value of the parameter
			 */
			void setValue( const std::string& parameterName, short val );

			/**
			 * Sets the value of the named parameter to a int.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param val the value of the parameter
			 */
			void setValue( const std::string& parameterName, int val );

			/**
			 * Sets the value of the named parameter to a long.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param val the value of the parameter
			 */
			void setValue( const std::string& parameterName, long val );

			/**
			 * Sets the value of the named parameter to a float.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param val the value of the parameter
			 */
			void setValue( const std::string& parameterName, float val );

			/**
			 * Sets the value of the named parameter to a double.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param val the value of the parameter
			 */
			void setValue( const std::string& parameterName, double val );

			/**
			 * Sets the value of the named parameter to a val.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param val the value of the parameter
			 */
			void setValue( const std::string& parameterName, const std::string& val );

			/**
			 * Sets the value of the named parameter to a void pointer type.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM
			 *
			 * @param parameterName the name of the parameter
			 * @param data the value of the parameter
			 * @param size size of the data
			 */
			void setValue( const std::string& parameterName,
			               std::shared_ptr<void> data,
			               const size_t size );

			/**
			 * Returns the value of the named parameter as a bool.
			 * <p/>
			 * If the named parameter cannot be found false will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			bool getAsBool( const std::string& parameterName ) const;

			/**
			 * Returns the value of the named parameter as a char.
			 * <p/>
			 * If the named parameter cannot be found an empty char will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			char getAsChar( const std::string& parameterName ) const;
			
			/**
			 * Returns the value of the named parameter as a short.
			 * <p/>
			 * If the named parameter cannot be found zero will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			short getAsShort( const std::string& parameterName ) const;

			/**
			 * Returns the value of the named parameter as a int.
			 * <p/>
			 * If the named parameter cannot be found zero will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			int getAsInt( const std::string& parameterName ) const;

			/**
			 * Returns the value of the named parameter as a long.
			 * <p/>
			 * If the named parameter cannot be found zero will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			long getAsLong( const std::string& parameterName ) const;

			/**
			 * Returns the value of the named parameter as a float.
			 * <p/>
			 * If the named parameter cannot be found zero will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			float getAsFloat( const std::string& parameterName ) const;
			
			/**
			 * Returns the value of the named parameter as a double.
			 * <p/>
			 * If the named parameter cannot be found zero will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			double getAsDouble( const std::string& parameterName ) const;
			
			/**
			 * Returns the value of the named parameter as a string.
			 * <p/>
			 * If the named parameter cannot be found an empty string
			 * will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			std::string getAsString( const std::string& parameterName ) const;

			/**
			 * Returns the value of the named parameter as a VariableData.
			 * <p/>
			 * If the named parameter cannot be found an instance of a VariableData
			 * is returned and the `data` member of this instance will point to a nullptr.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			util::VariableData getRawValue( const std::string& parameterName ) const;

			/**
			 * Returns the names of the parameters stored in this HLA interaction.
			 *
			 * @return Parameter names that are stored in this HLA interaction.
			 */
			std::vector<std::string> getParameterNames() const;

			/**
			 * Returns the fully qulified interaction name of this HLA Interaction as specified in SOM.
			 *
			 * @return the fully qulified interaction name of this HLA Interaction as specified in SOM.
			 */
			std::string getInteractionClassName() const;

			/**
			 * Clears all the parameters stored in this object
			 */
			void clear();

		private:
			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------

			// stores parameter names that required publising or
			// parameter names of a received interaction.
			std::shared_ptr<HLAInteractionParameters> m_parameterDataStore;
			// holds the unique id of an instance of this class
			std::string m_interactionClassName;
	};
}


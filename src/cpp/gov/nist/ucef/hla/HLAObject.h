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
	typedef std::map<std::string, util::VariableData> HLAObjectAttributes;
	/**
	 * The {@link HLAObject} represents a transient object that stores object class
	 * data for publishing or data of a received object class.
	 *
	 * @see RTIAmbassadorWrapper#updateObjectInstance(std::shared_ptr<HLAObject>&)
	 * @see RTIAmbassadorWrapper#deleteObjectInstances(std::shared_ptr<HLAObject>&)
	 * @see IFederateBase#receiveObjectRegistration(std::shared_ptr<const HLAObject>&, double)
	 * @see IFederateBase#receiveAttributeReflection(std::shared_ptr<const HLAObject>&, double)
	 * @see IFederateBase#receiveObjectDeletion(std::shared_ptr<const HLAObject>&, double)
	 */
	class UCEF_API HLAObject
	{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			HLAObject( const std::string& objectClassName,
			           long instanceId );
			virtual ~HLAObject();
			HLAObject( const HLAObject& ) = delete;
			HLAObject& operator=( const HLAObject& ) = delete;

			//----------------------------------------------------------
			//                     Instance Methods
			//----------------------------------------------------------

			/**
			 * Determines if this instance has the named attribute.
			 * 
			 * @param attributeName the name of the attribute
			 * @return true if the attribute is known by this instance, false otherwise
			 */
			bool isAttribute( const std::string& attributeName ) const;

			/**
			 * Sets the value of the named attribute to a bool.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, bool val );
			
			/**
			 * Sets the value of the named attribute to a char.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, const char val );

			/**
			 * Sets the value of the named attribute to a short.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, short val );

			/**
			 * Sets the value of the named attribute to a int.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, int val );

			/**
			 * Sets the value of the named attribute to a long.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, long val );

			/**
			 * Sets the value of the named attribute to a float.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, float val );

			/**
			 * Sets the value of the named attribute to a double.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, double val );

			/**
			 * Sets the value of the named attribute to a val.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, const std::string& val );

			/**
			 * Sets the value of the named attribute to a void pointer type.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute
			 * @param data the value of the attribute
			 * @param size size of the data
			 */
			void setValue( const std::string& attributeName,
			               std::shared_ptr<void> data,
			               const size_t size );

			/**
			 * Returns the value of the named attribute as a bool.
			 * <p/>
			 * If the named attribute cannot be found false will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			bool getAsBool( const std::string& attributeName ) const;

			/**
			 * Returns the value of the named attribute as a char.
			 * <p/>
			 * If the named attribute cannot be found an empty char will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			char getAsChar( const std::string& attributeName ) const;
			
			/**
			 * Returns the value of the named attribute as a short.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			short getAsShort( const std::string& attributeName ) const;

			/**
			 * Returns the value of the named attribute as a int.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			int getAsInt( const std::string& attributeName ) const;

			/**
			 * Returns the value of the named attribute as a long.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			long getAsLong( const std::string& attributeName ) const;

			/**
			 * Returns the value of the named attribute as a float.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			float getAsFloat( const std::string& attributeName ) const;
			
			/**
			 * Returns the value of the named attribute as a double.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			double getAsDouble( const std::string& attributeName ) const;
			
			/**
			 * Returns the value of the named attribute as a string.
			 * <p/>
			 * If the named attribute cannot be found an empty string
			 * will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			std::string getAsString( const std::string& attributeName ) const;

			/**
			 * Returns the value of the named attribute as a VariableData.
			 * <p/>
			 * If the named attribute cannot be found an instance of a VariableData
			 * is returned and the `data` member of this instance will point to a nullptr.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			util::VariableData getRawValue( const std::string& attributeName ) const;

			/**
			 * Returns the names of the attributes stored in this HLA Object.
			 *
			 * @return attribute names that are stored in this HLA Object.
			 */
			std::vector<std::string> getAttributeNames() const;

			/**
			 * Returns the fully qulified name of this HLA Object as specified in SOM.
			 *
			 * @return the fully qulified name of this HLA Object as specified in SOM.
			 */
			std::string getClassName() const;

			/**
			 * Clears all the attributes stored in this object
			 */
			void clear();

			/**
			 * Returns the unique instance id of this HLA object.
			 *
			 * @return the unique instance id of this HLA object.
			 */
			long getInstanceId();

		private:
			//----------------------------------------------------------
			//                    Private members
			//----------------------------------------------------------

			// stores attribute names that required publising or
			// attribute names of a received object class update.
			std::shared_ptr<HLAObjectAttributes> m_attributeDataStore;
			// holds the fully qulified name of the HLAObject
			std::string m_className;
			// holds the unique id of an instance of this class
			long m_instanceId;
	};
}


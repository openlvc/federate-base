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

#ifdef _WIN32
#include <functional>
#endif

#include <map>
#include <mutex>
#include <vector>
#include "gov/nist/ucef/hla/types.h"

namespace base
{
	typedef std::map<std::string, base::VariableData> HLAObjectAttributes;
	/**
	 * The {@link HLAObject} is a transient object that stores data of a either
	 * publishing class or received object class update.
	 *
	 * @see RTIAmbassadorWrapper#updateObjectInstance( std::shared_ptr<HLAObject>& )
	 * @see RTIAmbassadorWrapper#deleteObjectInstances( std::shared_ptr<HLAObject>& )
	 * @see IFederateBase#receiveObjectRegistration( std::shared_ptr<const HLAObject>&, double )
	 * @see IFederateBase#receiveAttributeReflection( std::shared_ptr<const HLAObject>&, double )
	 * @see IFederateBase#receiveObjectDeletion( std::shared_ptr<const HLAObject>&, double )
	 */
	class HLAObject
	{
		public:
			//----------------------------------------------------------
			//                     Constructors
			//----------------------------------------------------------
			HLAObject( const std::string& objectClassName,
			           long instanceId );
			HLAObject( const std::string& objectClassName );
			virtual ~HLAObject();
			HLAObject( const HLAObject& hlaObject );
			HLAObject& operator=( const HLAObject& ) = delete;

			//----------------------------------------------------------
			//                     Instance Methods
			//----------------------------------------------------------

			/**
			 * Checks if the given attribute is known to this instance
			 * 
			 * @param attributeName the name of the attribute as in SOM
			 * @return true if the attribute is known by this instance, false otherwise
			 */
			bool isPresent( const std::string& attributeName ) const;

			/**
			 * Sets the value of a named attribute to a bool.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, bool val );
			
			/**
			 * Sets the value of a named attribute to a char.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, const char val );

			/**
			 * Sets the value of a named attribute to a wchar_t.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, const wchar_t val );

			/**
			 * Sets the value of a named parameter to a char.
			 * <p/>
			 * <b>Note:</b> When setting the parameter, this class doesn't check for
			 * the validity of the named parameter against federate's SOM. Also this will
			 * use a `byte' encoder internally.
			 *
			 * @param parameterName the name of the parameter SOM
			 * @param val the value of the parameter
			 */
			void setValueAsByte( const std::string& parameterName, const char val );

			/**
			 * Sets the value of a named attribute to a short.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, short val );

			/**
			 * Sets the value of a named attribute to a int.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, int val );

			/**
			 * Sets the value of a named attribute to a long.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, long val );

			/**
			 * Sets the value of a named attribute to a float.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, float val );

			/**
			 * Sets the value of a named attribute to a double.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, double val );

			/**
			 * Sets the value of a named attribute to a val.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, const std::string& val );

			/**
			 * Sets the value of a named attribute to a val.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param val the value of the attribute
			 */
			void setValue( const std::string& attributeName, const std::wstring& val );

			/**
			 * Sets the value of a named attribute to a void pointer type.
			 * <p/>
			 * <b>Note:</b> When setting the attribute, this class doesn't check for
			 * the validity of the named attribute against federate's SOM
			 *
			 * @param attributeName the name of the attribute as in SOM
			 * @param data VariableData value that holds encoded data
			 */
			void setValue( const std::string& attributeName, VariableData& data );

			/**
			 * Sets the unique instance id of this HLA object.
			 *
			 * @param hash the unique instance id of this HLA object.
			 */
			void setInstanceId( long hash );

			/**
			 * Returns the value of a named attribute as a bool.
			 * <p/>
			 * If the named attribute cannot be found false will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			bool getAsBool( const std::string& attributeName ) const;

			/**
			 * Returns the value of a named attribute as a char.
			 * <p/>
			 * If the named attribute cannot be found an empty char will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			char getAsChar( const std::string& attributeName ) const;
			
			/**
			 * Returns the value of a named attribute as a wchar_t.
			 * <p/>
			 * If the named attribute cannot be found an empty char will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			wchar_t getAsWChar( const std::string& attributeName ) const;

			/**
			 * Returns the value of a named parameter as a char.
			 * <p/>
			 * If the named parameter cannot be found an empty char will be returned.
			 *
			 * @param parameterName the name of the parameter
			 * @return the value of the parameter
			 */
			char getAsByte( const std::string& attributeName ) const;

			/**
			 * Returns the value of a named attribute as a short.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			short getAsShort( const std::string& attributeName ) const;

			/**
			 * Returns the value of a named attribute as a int.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			int getAsInt( const std::string& attributeName ) const;

			/**
			 * Returns the value of a named attribute as a long.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			long getAsLong( const std::string& attributeName ) const;

			/**
			 * Returns the value of a named attribute as a float.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			float getAsFloat( const std::string& attributeName ) const;
			
			/**
			 * Returns the value of a named attribute as a double.
			 * <p/>
			 * If the named attribute cannot be found zero will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			double getAsDouble( const std::string& attributeName ) const;
			
			/**
			 * Returns the value of a named attribute as a string.
			 * <p/>
			 * If the named attribute cannot be found an empty string
			 * will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			std::string getAsString( const std::string& attributeName ) const;

			/**
			 * Returns the value of a named attribute as a wide string.
			 * <p/>
			 * If the named attribute cannot be found an empty string
			 * will be returned.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			std::wstring getAsWString( const std::string& attributeName ) const;

			/**
			 * Returns the value of a named attribute as a VariableData.
			 * <p/>
			 * If the named attribute cannot be found an instance of a VariableData
			 * is returned and the `data` member of this instance will point to a nullptr.
			 *
			 * @param attributeName the name of the attribute
			 * @return the value of the attribute
			 */
			base::VariableData getRawValue( const std::string& attributeName ) const;

			/**
			 * Returns the names of the attributes stored in a HLA Object.
			 *
			 * @return attribute names that are stored in a HLA Object.
			 */
			std::vector<std::string> getAttributeNames() const;

			/**
			 * Returns the fully qualified name of this HLA Object as specified in SOM.
			 *
			 * @return the fully qualified name of this HLA Object as specified in SOM.
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

			// stores attribute names and values that required publising or
			// received in an object update.
			std::shared_ptr<HLAObjectAttributes> attributeDataStore;
			// holds the fully qulified name of this class
			std::string className;
			// holds the unique id of an instance of this class
			long instanceId;
	};
}


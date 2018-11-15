/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-gateway.
 *
 *   NOTICE:  All information contained herein is, and remains
 *            the property of Calytrix Technologies Pty Ltd.
 *            The intellectual and technical concepts contained
 *            herein are proprietary to Calytrix Technologies Pty Ltd.
 *            Dissemination of this information or reproduction of
 *            this material is strictly forbidden unless prior written
 *            permission is obtained from Calytrix Technologies Pty Ltd.
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package gov.nist.ucef.hla.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import hla.rti1516e.ObjectInstanceHandle;

/**
 * The purpose of this class is to provide (as much as is possible) methods which are common to all
 * federate object instances in order to minimize the amount of code required in UCEF HLA federate
 * implementations.
 */
public class HLAObject
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final byte[] EMPTY_BYTE_ARRAY = {};
	private static final HLACodecUtils hlaCodec = HLACodecUtils.instance();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ObjectInstanceHandle objectInstanceHandle;
	private Map<String, byte[]> attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLAObject( ObjectInstanceHandle objectInstanceHandle, Collection<String> attributeNames )
	{
		this.objectInstanceHandle = objectInstanceHandle;
		this.attributes = new HashMap<>();
		// sanity check for null attribute names - since an object instance with no attributes wouldn't
		// be very useful, possibly we should throw an exception here...? There's nothing inherently
		// "illegal" about it though, it's just pointless.
		if(attributeNames != null)
		{
			// TODO Here we initialise each attribute with an empty/zero-length byte array - this 
			//      essentially means that the attributes are all "uninitialised", since even an 
			//      empty string would require a single byte '\0' (null terminator) character. Any
			//      attempt to retrieve a "typed" value at this stage will result in an HLA 
			//      decoding error (see the isInitialised() method). 
			//      In future we should probably initialise each attribute with at least a sensible 
			//      default value for primitive types at least (e.g., 0 for integers, 0.0 for floats,
			//      false for booleans, etc) but for now this is sufficient.
			attributeNames.forEach( (attrName) -> this.attributes.put( attrName, EMPTY_BYTE_ARRAY) );
		}
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Provide a hash code, suitable for indexing instances in a map
	 */
	@Override
	public int hashCode()
	{
		// delegate to the cached object instance handle
		return this.objectInstanceHandle.hashCode();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtain the underlying HLA handle associated with this instance
	 * @return the underlying HLA handle associated with this instance
	 */
	public ObjectInstanceHandle getInstanceHandle()
	{
		return this.objectInstanceHandle;
	}
	
	/**
	 * Determine if this instance has the named attribute 
	 * 
	 * @param attributeName the name of the attribute
	 * @return true if the attribute is known by this instance, false otherwise
	 */
	public boolean isAttribute( String attributeName )
	{
		return this.attributes.containsKey( attributeName );
	}
	
	/**
	 * Determine if the named attribute has been initialised (i.e. has a value)
	 * 
	 * In practice, this means that the byte array value associated with the 
	 * named attribute is: 
	 *  - non null, and
	 *  - not empty (i.e., is not zero bytes in length)
	 * 
	 * @param attributeName the name of the attribute
	 * @return true if the attribute as a value defined for it, false otherwise
	 */
	public boolean isInitialised( String attributeName )
	{
		byte[] rawValue = getRawValue( attributeName );
		return rawValue != null && rawValue.length != 0;
	}
	
	/**
	 * Obtain the value for the named attribute as a short
	 * 
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public short getAsShort( String attributeName )
	{
		return hlaCodec.asShort( getRawValue( attributeName ) );
	}
	
	/**
	 * Obtain the value for the named attribute as an integer
	 * 
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public int getAsInt( String attributeName )
	{
		return hlaCodec.asInt( getRawValue( attributeName ) );
	}
	
	/**
	 * Obtain the value for the named attribute as a long
	 * 
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public long getAsLong( String attributeName )
	{
		return hlaCodec.asLong( getRawValue( attributeName ) );
	}
	
	/**
	 * Obtain the value for the named attribute as a float
	 * 
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public float getAsFloat( String attributeName )
	{
		return hlaCodec.asFloat( getRawValue( attributeName ) );
	}
	
	/**
	 * Obtain the value for the named attribute as a double
	 * 
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public double getAsDouble( String attributeName )
	{
		return hlaCodec.asDouble( getRawValue( attributeName ) );
	}
	
	/**
	 * Obtain the value for the named attribute as a boolean
	 * 
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public boolean getAsBoolean( String attributeName )
	{
		return hlaCodec.asBoolean( getRawValue( attributeName ) );
	}
	
	/**
	 * Obtain the value for the named attribute as a string
	 * 
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public String getAsString( String attributeName )
	{
		return hlaCodec.asString( getRawValue( attributeName ) );
	}
	
	/**
	 * Obtain the the raw byte array value of an attribute from the handle name
	 * 
	 * @param attributeName the name of the attribute
	 * @return the raw byte array value of the attribute, or null if the given handle is not
	 *         related to this instance's class
	 */
	public byte[] getRawValue( String attributeName )
	{
		byte[] result;
		synchronized( this.attributes )
		{
			result = this.attributes.get( attributeName );
		}
		return result;
    }
    
	/**
	 * Set the value of an attribute to a short 
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void set( String attributeName, short value )
	{
		setRawValue( attributeName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to an integer 
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void set( String attributeName, int value )
	{
		setRawValue( attributeName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a long 
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void set( String attributeName, long value )
	{
		setRawValue( attributeName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a float 
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void set( String attributeName, float value )
	{
		setRawValue( attributeName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a double 
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void set( String attributeName, double value )
	{
		setRawValue( attributeName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a float 
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void set( String attributeName, boolean value )
	{
		setRawValue( attributeName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a string 
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void set( String attributeName, String value )
	{
		setRawValue( attributeName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the the raw byte array value of an attribute from the attribute name
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the raw byte array value
	 */
	public void setRawValue( String attributeName, byte[] value )
	{
		synchronized( this.attributes )
		{
    		if( value == null )
    		{
    			this.attributes.remove( attributeName );
    		}
    		else
    		{
    			this.attributes.put( attributeName, value );
    		}
		}
	}
	
	/**
	 * Get the current value of all attributes this object instance.
	 *
	 * @return An map of attribute handles names to their current values (note that this is not
	 *         modifiable but reflects changes made to the underlying data)
	 */
	public Map<String,byte[]> getState()
	{
		return Collections.unmodifiableMap( this.attributes );
	}
    
	/**
	 * Set the current value of attributes this object instance.
	 * 
	 * @param attributes the attributes and values to update from
	 * @return this instance
	 */
	public HLAObject setState( Map<String, byte[]> attributes )
	{
		// TODO should we sanity check that we are updating from 
		//      a compatible set of attributes...?
		synchronized( this.attributes )
		{
			this.attributes.putAll( attributes );
		}
		return this;
	}
    
	/**
	 * Get the attribute identifiers of this object instance.
	 *
	 * @return the attribute identifiers of this object instance.
	 */
	public Collection<String> getAttributeNames()
	{
		return Collections.unmodifiableSet( this.attributes.keySet() );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

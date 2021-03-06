/*
 * This software is contributed as a public service by The National Institute of Standards
 * and Technology (NIST) and is not subject to U.S. Copyright
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above NIST contribution notice and this permission and disclaimer notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. THE AUTHORS OR COPYRIGHT HOLDERS SHALL
 * NOT HAVE ANY OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 */
package gov.nist.ucef.hla.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.encoding.EncoderFactory;

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
	protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected final String objectClassName;
	protected Map<String, byte[]> attributes;
	protected ObjectInstanceHandle instanceHandle;

	// used for encoding/decoding byte array representations of attributes
	protected EncoderFactory encoder;

	private final Object mutex_lock = new Object();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Construct a new object instance with no attribute values
	 *
	 * NOTE: Generally speaking the RTIAmbassadorWrapper's makeObjectInstance() family of methods
	 * should be used to create a new {@link HLAObject}
	 *
	 * @param interactionClassHandle the class handle to which this instance corresponds
	 */
	protected HLAObject( String objectClassName )
	{
		this( objectClassName, null, null );
	}

	/**
	 * Construct a new object instance with no attribute values
	 *
	 * NOTE: Generally speaking the RTIAmbassadorWrapper's makeObjectInstance() family of methods
	 * should be used to create a new {@link HLAObject}
	 *
	 * @param interactionClassHandle the class handle to which this instance corresponds
	 */
	protected HLAObject( String objectClassName, ObjectInstanceHandle handle )
	{
		this( objectClassName, null, handle );
	}

	/**
	 * Construct a new object instance with attribute values
	 *
	 * NOTE: Generally speaking the RTIAmbassadorWrapper's makeObjectInstance() family of methods
	 * should be used to create a new {@link HLAObject}
	 *
	 * @param interactionClassHandle the class handle to which this instance corresponds
	 * @param initialValues the initial attribute values for the interaction (may be empty or
	 *            null)
	 */
	protected HLAObject( String objectClassName, Map<String,byte[]> initialValues )
	{
		this( objectClassName, initialValues, null );
	}

	/**
	 * Construct a new object instance with attribute values
	 *
	 * NOTE: Generally speaking the RTIAmbassadorWrapper's makeObjectInstance() family of methods
	 * should be used to create a new {@link HLAObject}
	 *
	 * @param interactionClassHandle the class handle to which this instance corresponds
	 * @param initialValues the initial attribute values for the interaction (may be empty or
	 *            null)
	 */
	protected HLAObject( String objectClassName,
	                     Map<String,byte[]> initialValues, ObjectInstanceHandle handle )
	{
		this.objectClassName = objectClassName;
		this.attributes = initialValues == null ? new HashMap<>() : initialValues;

		this.instanceHandle = handle;

		this.encoder = HLACodecUtils.getEncoder();
	}

	/**
	 * Construct from another {@link HLAObject} instance.
	 *
	 * NOTE: this will result in an instance which is "linked" to the original instance.
	 *
	 * NOTE: Generally speaking the RTIAmbassadorWrapper's makeObjectInstance() family of methods
	 * should be used to create a new {@link HLAObject}
	 *
	 * @param objectInstance the object instance to use as the base
	 */
	protected HLAObject( HLAObject objectInstance )
	{
		this.objectClassName = objectInstance.objectClassName;
		this.attributes = objectInstance.attributes;

		this.instanceHandle = objectInstance.instanceHandle;

		this.encoder = objectInstance.encoder;
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
		return this.objectClassName.hashCode();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtain the underlying HLA handle associated with this instance
	 * @return the underlying HLA handle associated with this instance
	 */
	public String getObjectClassName()
	{
		return this.objectClassName;
	}

	/**
	 * Returns the unique instance handle of this HLA object.
	 *
	 * @return the unique instance handle of this HLA object.
	 */
	public ObjectInstanceHandle getObjectInstanceHandle()
	{
		return this.instanceHandle;
	}

	/**
	 * Determine if this instance has a attribute with the given name.
	 *
	 * NOTE: this does not mean that the named attribute is initialized, only that an attribute value
	 * could be queried using the given name. See {@link #isPresent(String)} for checking whether the
	 * value for the named attribute has actually been initialized.
	 *
	 * @param attributeName the name of the attribute
	 * @return true if the attribute is known by this instance, false otherwise
	 */	public boolean isAttribute( String attributeName )
	{
		return this.attributes.containsKey( attributeName );
	}

	/**
	 * Determine if the named attribute has been initialized (i.e. has a value)
	 *
	 * In practice, this means that the byte array value associated with the
	 * named attribute is:
	 *  - non null, and
	 *  - not empty (i.e., is not zero bytes in length)
	 *
	 * @param attributeName the name of the attribute
	 * @return true if the attribute as a value defined for it, false otherwise
	 */
	public boolean isPresent( String attributeName )
	{
		byte[] rawValue = getRawValue( attributeName );
		return rawValue != null && rawValue.length != 0;
	}

	/**
	 * Obtain the value for the named attribute as a byte
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public byte getAsByte( String attributeName )
	{
		return HLACodecUtils.asByte( this.encoder, getRawValue( attributeName ) );
	}

	/**
	 * Obtain the value for the named attribute as a short
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public short getAsShort( String attributeName )
	{
		return HLACodecUtils.asShort( this.encoder, getRawValue( attributeName ) );
	}

	/**
	 * Obtain the value for the named attribute as an integer
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public int getAsInt( String attributeName )
	{
		return HLACodecUtils.asInt( this.encoder, getRawValue( attributeName ) );
	}

	/**
	 * Obtain the value for the named attribute as a long
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public long getAsLong( String attributeName )
	{
		return HLACodecUtils.asLong( this.encoder, getRawValue( attributeName ) );
	}

	/**
	 * Obtain the value for the named attribute as a float
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public float getAsFloat( String attributeName )
	{
		return HLACodecUtils.asFloat( this.encoder, getRawValue( attributeName ) );
	}

	/**
	 * Obtain the value for the named attribute as a double
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public double getAsDouble( String attributeName )
	{
		return HLACodecUtils.asDouble( this.encoder, getRawValue( attributeName ) );
	}

	/**
	 * Obtain the value for the named attribute as a boolean
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public boolean getAsBoolean( String attributeName )
	{
		return HLACodecUtils.asBoolean( this.encoder, getRawValue( attributeName ) );
	}

	/**
	 * Obtain the value for the named attribute as a char
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public char getAsChar( String attributeName )
	{
		return HLACodecUtils.asChar( this.encoder, getRawValue( attributeName ) );
	}

	/**
	 * Obtain the value for the named attribute as a string
	 *
	 * @param attributeName the name of the attribute
	 * @return the value
	 */
	public String getAsString( String attributeName )
	{
		return HLACodecUtils.asString( this.encoder, getRawValue( attributeName ) );
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
		synchronized( mutex_lock )
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
	public void setValue( String attributeName, short value )
	{
		setRawValue( attributeName, HLACodecUtils.encode( this.encoder, value ) );
	}

	/**
	 * Set the value of an attribute to an integer
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void setValue( String attributeName, int value )
	{
		setRawValue( attributeName, HLACodecUtils.encode( this.encoder, value ) );
	}

	/**
	 * Set the value of an attribute to a long
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void setValue( String attributeName, long value )
	{
		setRawValue( attributeName, HLACodecUtils.encode( this.encoder, value ) );
	}

	/**
	 * Set the value of an attribute to a float
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void setValue( String attributeName, float value )
	{
		setRawValue( attributeName, HLACodecUtils.encode( this.encoder, value ) );
	}

	/**
	 * Set the value of an attribute to a double
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void setValue( String attributeName, double value )
	{
		setRawValue( attributeName, HLACodecUtils.encode( this.encoder, value ) );
	}

	/**
	 * Set the value of an attribute to a float
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void setValue( String attributeName, boolean value )
	{
		setRawValue( attributeName, HLACodecUtils.encode( this.encoder, value ) );
	}

	/**
	 * Set the value of an attribute to a char
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void setValue( String attributeName, char value )
	{
		setRawValue( attributeName, HLACodecUtils.encode( this.encoder, value ) );
	}

	/**
	 * Set the value of an attribute to a string
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value to set
	 */
	public void setValue( String attributeName, String value )
	{
		setRawValue( attributeName, HLACodecUtils.encode( this.encoder, value ) );
	}

	/**
	 * Set the the raw byte array value of an attribute from the attribute name
	 *
	 * @param attributeName the name of the attribute
	 * @param value the raw byte array value
	 */
	public void setRawValue( String attributeName, byte[] value )
	{
		synchronized( mutex_lock )
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
	 * Set the current value of multiple attributes this object instance.
	 *
	 * @param other the {@link HLAObject} instance with state values to update from
	 * @return this instance
	 */
	public HLAObject setState( HLAObject other )
	{
		return setState( other.getState() );
	}

	/**
	 * Set the current value of multiple attributes this object instance.
	 *
	 * @param attributes the attributes and values to update from
	 * @return this instance
	 */
	public HLAObject setState( Map<String, byte[]> attributes )
	{
		synchronized( mutex_lock )
		{
			this.attributes.putAll( attributes );
		}
		return this;
	}

	/**
	 * Clear all attributes
	 *
	 * NOTE: After this method is called, there will be no attributes defined.
	 *
	 * @return this instance
	 */
	public HLAObject clearState()
	{
		synchronized( mutex_lock )
		{
			this.attributes.clear();
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

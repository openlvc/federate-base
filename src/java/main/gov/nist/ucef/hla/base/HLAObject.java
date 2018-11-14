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
import java.util.Map.Entry;

import hla.rti1516e.ObjectClassHandle;
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
	private static final HLACodecUtils hlaCodec = HLACodecUtils.instance();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIAmbassadorWrapper rtiamb;
	
	private ObjectInstanceHandle objectInstanceHandle;
	private Map<String, byte[]> attributes;

	private String objectClassName;
	private String objectInstanceName;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLAObject( ObjectInstanceHandle objectInstanceHandle, Map<String, byte[]> attributes)
	{
		this.objectInstanceHandle = objectInstanceHandle;
		this.attributes = attributes == null ? new HashMap<>() : attributes;
		
		this.rtiamb = RTIAmbassadorWrapper.instance();
		
		this.objectInstanceName = rtiamb.getObjectInstanceName( this.objectInstanceHandle );
		ObjectClassHandle objectClassHandle = rtiamb.getKnownObjectClassHandle( objectInstanceHandle );
		this.objectClassName = rtiamb.getObjectClassName( objectClassHandle );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Update this instance out to the federation with a tag (which can be null).
	 * 
	 * @param tag the tag (can be null)
	 */
	public void update( byte[] tag )
	{
		rtiamb.updateAttributeValues( this.objectInstanceHandle, this.attributes, tag, null );
	}
	
	/**
	 * Update this instance out to the federation with a tag (which can be null) and time-stamp.
	 * 
	 * @param tag the tag (can be null)
	 * @param time the time-stamp
	 */
	public void update( byte[] tag, double time )
	{
		rtiamb.updateAttributeValues( this.objectInstanceHandle, this.attributes, tag, time );
	}
	
	/**
	 * Provide a hash code, suitable for indexing instances in a map
	 */
	@Override
	public int hashCode()
	{
		// delegate to the cached object instance handle
		return this.objectInstanceHandle.hashCode();
	}
	
	/**
	 * Provide a string representation of this instance, suitable for logging and debugging
	 * purposes
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( rtiamb.assembleObjectInstanceDetails( this.objectInstanceHandle ) );
		builder.append( "\n" );
		for( Entry<String,byte[]> entry : this.attributes.entrySet() )
		{
			builder.append( "\t" );
			builder.append( entry.getKey() );
			builder.append( " = '" );
			// TODO at the moment this assumes that all values are strings, but going forward there
			// 		will need to be some sort of mapping of attribute names/handles to primitive
			//      types for parsing
			builder.append( hlaCodec.asString( entry.getValue() ) );
			builder.append( "'\n" );
		}
		return builder.toString();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Get the name of this kind of object.
	 *
	 * @return the name of this kind of object.
	 */
	public String getClassName()
	{
		return this.objectClassName;
	}

	/**
	 * Get the name of this object instance.
	 *
	 * @return the name of this object instance.
	 */
	public String getInstanceName()
	{
		return this.objectInstanceName;
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
		return this.attributes.get( attributeName );
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
		if( value == null )
		{
			this.attributes.remove( attributeName );
		}
		else
		{
			this.attributes.put( attributeName, value );
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
		this.attributes.putAll( attributes );
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

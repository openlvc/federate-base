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

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.encoding.EncoderFactory;

/**
 * The purpose of this class is to provide (as much as is possible) methods which are common to all
 * federate interactions in order to minimize the amount of code required in UCEF HLA federate
 * implementations. 
 */
public class HLAInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected InteractionClassHandle interactionClassHandle;
	protected Map<String, byte[]> parameters;
	
	// used for encoding/decoding byte array representations of interaction parameters
	protected EncoderFactory encoder;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Construct a new interaction instance with no parameter values
	 * 
	 * NOTE: Generally speaking the RTIAmbassadorWrapper's makeInteraction() method should be used
	 * to create a new {@link HLAInteraction}
	 * 
	 * @param interactionClassHandle the class handle to which this instance corresponds
	 */
	protected HLAInteraction( InteractionClassHandle interactionClassHandle )
	{
		this( interactionClassHandle, null );
	}

	/**
	 * Construct a new interaction instance with parameter values
	 * 
	 * NOTE: Generally speaking the RTIAmbassadorWrapper's makeInteraction() method should be used
	 * to create a new {@link HLAInteraction}
	 * 
	 * @param interactionClassHandle the class handle to which this instance corresponds
	 * @param parameters the parameter values for the interaction (may be empty or null)
	 */
	protected HLAInteraction( InteractionClassHandle interactionClassHandle,
	                          Map<String,byte[]> parameters )
	{
		this.interactionClassHandle = interactionClassHandle;
		this.parameters = parameters == null ? new HashMap<>() : parameters;

		this.encoder = HLACodecUtils.getEncoder();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Provide a hash code, suitable for indexing in a map
	 * 
	 * NOTE: Interactions of the same *type* are considered equivalent. This is because interactions are
	 *       transient, so there are no "instances" of an interaction as such.
	 */
	@Override
	public int hashCode()
	{
		// delegate to the cached interaction class handle
		return this.interactionClassHandle.hashCode();
	}

    ////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtain the underlying HLA interaction class handle associated with this instance
	 * @return the underlying HLA interaction class associated with this instance
	 */
	protected InteractionClassHandle getInteractionClassHandle()
	{
		return this.interactionClassHandle;
	}
	
	/**
	 * Determine if this instance has the named parameter 
	 * 
	 * @param parameterName the name of the parameter
	 * @return true if the parameter is known by this instance, false otherwise
	 */
	public boolean isParameter( String parameterName )
	{
		return this.parameters.containsKey( parameterName );
	}
	
	/**
	 * Determine if the named parameter has been initialized (i.e. has a value)
	 * 
	 * In practice, this means that the byte array value associated with the 
	 * named parameter is: 
	 *  - non null, and
	 *  - not empty (i.e., is not zero bytes in length)
	 * 
	 * @param parameterName the name of the attribute
	 * @return true if the parameter as a value defined for it, false otherwise
	 */
	public boolean isInitialized( String parameterName )
	{
		byte[] rawValue = getRawValue( parameterName );
		return rawValue != null && rawValue.length != 0;
	}
	
	
	/**
	 * Obtain the value for the named parameter as a short
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public short getAsShort( String parameterName )
	{
		return HLACodecUtils.asShort( this.encoder, getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as an integer
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public int getAsInt( String parameterName )
	{
		return HLACodecUtils.asInt( this.encoder, getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a long
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public long getAsLong( String parameterName )
	{
		return HLACodecUtils.asLong( this.encoder, getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a float
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public float getAsFloat( String parameterName )
	{
		return HLACodecUtils.asFloat( this.encoder, getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a double
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public double getAsDouble( String parameterName )
	{
		return HLACodecUtils.asDouble( this.encoder, getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a boolean
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public boolean getAsBoolean( String parameterName )
	{
		return HLACodecUtils.asBoolean( this.encoder, getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a char
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public char getAsChar( String parameterName )
	{
		return HLACodecUtils.asChar( this.encoder, getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a string
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public String getAsString( String parameterName )
	{
		return HLACodecUtils.asString( this.encoder, getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the raw byte array value of a parameter from the handle
	 * 
	 * @param handle the parameter handle
	 * @return the raw byte array value of a parameter from the handle, or null if the given
	 *         handle is not related to this interaction or the parameter has not been initialized
	 */
	public byte[] getRawValue( String parameterName )
	{
		byte[] result;
		synchronized( this.parameters )
		{
			result = parameters.get( parameterName );
		}
		return result;
	}
    
	/**
	 * Set the value of a parameter to a short 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void setValue( String parameterName, short value )
	{
		setRawValue( parameterName, HLACodecUtils.encode( this.encoder, value ) );
	}
	
	/**
	 * Set the value of a parameter to an integer 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void setValue( String parameterName, int value )
	{
		setRawValue( parameterName, HLACodecUtils.encode( this.encoder, value ) );
	}
	
	/**
	 * Set the value of a parameter to a long 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void setValue( String parameterName, long value )
	{
		setRawValue( parameterName, HLACodecUtils.encode( this.encoder, value ) );
	}
	
	/**
	 * Set the value of a parameter to a float 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void setValue( String parameterName, float value )
	{
		setRawValue( parameterName, HLACodecUtils.encode( this.encoder, value ) );
	}
	
	/**
	 * Set the value of a parameter to a double 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void setValue( String parameterName, double value )
	{
		setRawValue( parameterName, HLACodecUtils.encode( this.encoder, value ) );
	}
	
	/**
	 * Set the value of a parameter to a float 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void setValue( String parameterName, boolean value )
	{
		setRawValue( parameterName, HLACodecUtils.encode( this.encoder, value ) );
	}
	
	/**
	 * Set the value of a parameter to a character 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void setValue( String parameterName, char value )
	{
		setRawValue( parameterName, HLACodecUtils.encode( this.encoder, value ) );
	}
	
	/**
	 * Set the value of a parameter to a string 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void setValue( String parameterName, String value )
	{
		setRawValue( parameterName, HLACodecUtils.encode( this.encoder, value ) );
	}
	
	/**
	 * Set the the raw byte array value of a parameter from the parameter name
	 * 
	 * @param parameterName the parameter name
	 * @param value the new raw byte array value
	 */
	public void setRawValue( String parameterName, byte[] value )
	{
		synchronized( this.parameters )
		{
			if( value == null )
    		{
    			this.parameters.remove( parameterName );
    		}
    		else
    		{
    			this.parameters.put( parameterName, value );
    		}
		}
	}
	
    /**
     * Get the current value of all parameters of this interaction.
     *
     * @return An map of parameters handle names to their current values (note that this is not 
     * 		   modifiable but reflects changes made to the underlying data)
     */
    public Map<String, byte[]> getState()
    {
    	return Collections.unmodifiableMap( this.parameters );
    	/*
    	Map<String, byte[]> result = new HashMap<>(); 
		synchronized( this.parameters )
		{
			for( Entry<String,byte[]> entry : this.parameters.entrySet())
			{
				byte[] src = entry.getValue();
				byte[] value = new byte[src.length];
				System.arraycopy( src, 0, value, 0, src.length );
				result.put( entry.getKey(), value );
			}
		}
		return result;
		*/
    }
    
	/**
	 * Set the current value of multiple parameters this object instance.
	 * 
	 * @param parameters the parameters and values to update from
	 * @return this instance
	 */
	public HLAInteraction setState( Map<String, byte[]> parameters )
	{
		synchronized( this.parameters )
		{
			this.parameters.putAll( parameters );
		}
		return this;
	}
    
	/**
	 * Clear all parameters
	 * 
	 * NOTE: After this method is called, there will be no parameters defined.
	 * 
	 * @return this instance
	 */
	public HLAInteraction clearState()
	{
		synchronized( this.parameters )
		{
			this.parameters.clear();
		}
		return this;
	}
	
	/**
	 * Get the parameter identifiers of this interaction.
	 *
	 * @return the parameter identifiers of this interaction.
	 */
	public Collection<String> getParameterNames()
	{
		return Collections.unmodifiableSet( this.parameters.keySet() );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

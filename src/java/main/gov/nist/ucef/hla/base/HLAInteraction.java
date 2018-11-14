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
	private static final HLACodecUtils hlaCodec = HLACodecUtils.instance();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private InteractionClassHandle interactionClassHandle;
	private Map<String, byte[]> parameters;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLAInteraction( InteractionClassHandle interactionClassHandle,
	                       Map<String, byte[]> parameters )
	{
		this.interactionClassHandle = interactionClassHandle;
		this.parameters = parameters == null ? new HashMap<>() : parameters;
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
	public InteractionClassHandle getInteractionClassHandle()
	{
		return this.interactionClassHandle;
	}
	
	/**
	 * Obtain the value for the named parameter as a short
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public short getAsShort( String parameterName )
	{
		return hlaCodec.asShort( getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as an integer
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public int getAsInt( String parameterName )
	{
		return hlaCodec.asInt( getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a long
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public long getAsLong( String parameterName )
	{
		return hlaCodec.asLong( getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a float
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public float getAsFloat( String parameterName )
	{
		return hlaCodec.asFloat( getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a double
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public double getAsDouble( String parameterName )
	{
		return hlaCodec.asDouble( getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a boolean
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public boolean getAsBoolean( String parameterName )
	{
		return hlaCodec.asBoolean( getRawValue( parameterName ) );
	}
	
	/**
	 * Obtain the value for the named parameter as a string
	 * 
	 * @param parameterName the name of the parameter
	 * @return the value
	 */
	public String getAsString( String parameterName )
	{
		return hlaCodec.asString( getRawValue( parameterName ) );
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
		return parameters.get( parameterName );
	}
    
	/**
	 * Set the value of an attribute to a short 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void set( String parameterName, short value )
	{
		setRawValue( parameterName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to an integer 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void set( String parameterName, int value )
	{
		setRawValue( parameterName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a long 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void set( String parameterName, long value )
	{
		setRawValue( parameterName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a float 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void set( String parameterName, float value )
	{
		setRawValue( parameterName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a double 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void set( String parameterName, double value )
	{
		setRawValue( parameterName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a float 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void set( String parameterName, boolean value )
	{
		setRawValue( parameterName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the value of an attribute to a string 
	 * 
	 * @param parameterName the name of the parameter
	 * @param value the value to set
	 */
	public void set( String parameterName, String value )
	{
		setRawValue( parameterName, 
		             hlaCodec.encode( value ).toByteArray() );
	}
	
	/**
	 * Set the the raw byte array value of an parameter from the parameter name
	 * 
	 * @param parameterName the parameter name
	 * @param value the new raw byte array value
	 */
	public void setRawValue( String parameterName, byte[] value )
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
	
    /**
     * Get the current value of all parameters of this interaction.
     *
     * @return An map of parameters handle names to their current values (note that this is not 
     * 		   modifiable but reflects changes made to the underlying data)
     */
    public Map<String, byte[]> getState()
    {
		return Collections.unmodifiableMap( this.parameters );
    }
    
	/**
	 * Set the current value of parameters this object instance.
	 * 
	 * @param parameters the parameters and values to update from
	 * @return this instance
	 */
	public HLAInteraction setState( Map<String, byte[]> parameters )
	{
		// TODO should we sanity check that we are updating from 
		//      a compatible set of attributes...?
		this.parameters.putAll( parameters );
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

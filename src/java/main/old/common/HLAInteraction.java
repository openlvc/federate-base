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
package old.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.time.HLAfloat64Time;
import old.util.HLACodecUtils;
import old.util.RTIUtils;

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
	private static HLACodecUtils CODEC_UTILS = HLACodecUtils.instance();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private InteractionClassHandle interactionHandle;
	private ParameterHandleValueMap parameters;
	private byte[] tag;
	private LogicalTime time;

	private RTIUtils rtiUtils;

	private String interactionIdentifier;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
    public HLAInteraction( RTIUtils rtiUtils,
                           InteractionClassHandle interactionHandle, ParameterHandleValueMap parameters, 
                           byte[] tag)
    {
    	this(rtiUtils, interactionHandle, parameters, tag, null);
    }
    
	public HLAInteraction( RTIUtils rtiUtils,
	                       InteractionClassHandle interactionHandle, ParameterHandleValueMap parameters,
	                       byte[] tag, LogicalTime time)
	{
		this.rtiUtils = rtiUtils;
		this.interactionHandle = interactionHandle;
		this.parameters = parameters == null ? rtiUtils.makeParameterMap() : parameters;
		this.tag = tag;
		this.time = time;
		
		this.interactionIdentifier = this.rtiUtils.getInteractionClassName( this.interactionHandle );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Get the identifier of this interaction.
	 *
	 * @return the identifier of this interaction.
	 */
	public String getIdentifier()
	{
		return this.interactionIdentifier;
	}
	
	/**
	 * Get the class handle of this interaction.
	 *
	 * @return the class handle of this interaction.
	 */
	public InteractionClassHandle getClassHandle()
	{
		return this.interactionHandle;
	}
	
	/**
	 * Obtain the name of a parameter from the handle
	 * 
	 * @param handle the parameter handle
	 * @return the name of the parameter, or null if the given handle is not related to this interaction
	 */
    public String parameterName(ParameterHandle handle)
    {
    	return this.rtiUtils.getParameterName( this.interactionHandle, handle );
    }
    
	/**
	 * Obtain the raw byte array value of a parameter from the handle
	 * 
	 * @param handle the parameter handle
	 * @return the raw byte array value of a parameter from the handle, or null if the given handle is
	 *         not related to this interaction or the parameter has not been initialized
	 */
    public byte[] parameterRawValue(ParameterHandle handle)
    {
    	return parameters.get(handle);
    }
    
    /**
     * Obtain the String value of a parameter from the handle
     * 
     * @param handle the parameter handle
     * @return the String value of a parameter from the handle (as decoded from the raw byte array), or
     * 		   an empty string if the given handle is not related to this interaction or the parameter 
     *         has not been initialized
     */
    public String parameterValue(ParameterHandle handle)
    {
    	return CODEC_UTILS.decodeHLAString(parameterRawValue( handle ));
    }
    
    /**
     * Get the current value of all parameters of this interaction.
     *
     * @return An map of parameters handle names to their current values (note that this is not 
     * 		   modifiable but reflects changes made to the underlying data)
     */
    public Map<ParameterHandle, byte[]> getState()
    {
    	return Collections.unmodifiableMap(this.parameters);
    }
    
    /**
     * Get the parameter handles of this interaction.
     *
     * @return the parameter handles of this interaction.
     */
    public Collection<ParameterHandle> getParameterHandles()
    {
    	return Collections.unmodifiableSet(this.parameters.keySet());
    }
    
    /**
     * Get the parameter identifiers of this interaction.
     *
     * @return the parameter identifiers of this interaction.
     */
    public Collection<String> getParameterNames()
    {
		List<String> result = new ArrayList<>();
		for( ParameterHandle handle : this.parameters.keySet() )
		{
			result.add( parameterName( handle ) );
		}
		return Collections.unmodifiableList(result);
    }
    
    /**
     * Get the parameter identifiers and associated values of this interaction.
     *
     * @return An map of parameters handle names to their current values
     */
    public Map<String, String> getParameterNamesAndValues()
    {
    	Map<String, String> result = new HashMap<>();
		for( ParameterHandle handle : this.parameters.keySet() )
		{
			result.put(parameterName(handle), parameterValue(handle) );
		}
    	return Collections.unmodifiableMap(result);
    }
    
    /**
     * Basic publish of this interaction with no tag or timestamp
     */
    public void send()
    {
    	publish( null, null );
    }
    
    /**
     * Publish this interaction with a tag and timestamp. Will gracefully handle tag and/or 
     * timestamp arguments being null
     * 
     * @param tag the tag
     * @param time the timestamp
     */
    public void publish(byte[] tag, HLAfloat64Time time)
    {
    	tag = tag == null ? new byte[0] : tag;
    	this.rtiUtils.sendInteraction( this.interactionHandle, this.parameters, tag, time );
    }
    
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		// print the handle
		builder.append( "\n\thandle = " + interactionHandle );
		if( this.interactionIdentifier != null )
			builder.append( " (" + this.interactionIdentifier + ") " );
		builder.append( ": " );
		
		// print the tag
		builder.append( "\n\ttag = " + new String(tag) );
		// print the time (if we have it)
		if( time != null )
		{
			builder.append( "\n\ttime = " + ((HLAfloat64Time)time).getValue() );
		}
		
		// print the parameter information
		builder.append( "\n\tparameterCount = " + parameters.size() );
		for( Entry<ParameterHandle,byte[]> entry : parameters.entrySet() )
		{
			ParameterHandle parameterHandle = entry.getKey();
			byte[] rawValue = entry.getValue();
			
			// print the parameter handle
			String parameterIdentifier = parameterName(parameterHandle);
			builder.append( "\n\t\tparamName = " );
			builder.append( parameterIdentifier == null ? "UNKOWN PARAMETER" : parameterIdentifier );
			builder.append( "\n\t\tparamHandle = " );
			builder.append( parameterHandle );
			// print the parameter value
			builder.append( "\n\t\tparamValue = '" );
			builder.append( CODEC_UTILS.decodeHLAString(rawValue) );
			builder.append( "' (" );
			builder.append( rawValue.length );
			builder.append( " bytes)" );
		}
		builder.append( "\n" );

		return builder.toString();		
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

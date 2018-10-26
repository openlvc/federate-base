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
package gov.nist.ucef.hla.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gov.nist.ucef.hla.util.HLACodecUtils;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.time.HLAfloat64Time;

/**
 * The purpose of this class is to provide (as much as is possible) methods which are common to all
 * federate interactions in order to minimize the amount of code required in UCEF HLA federate
 * implementations. 
 */
public class InteractionBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static HLACodecUtils CODEC_UTILS = HLACodecUtils.instance();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateBase federate;
	private InteractionClassHandle interactionHandle;
	private ParameterHandleValueMap parameters;
	private byte[] tag;
	private LogicalTime time;

	private String interactionIdentifier;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
    public InteractionBase(FederateBase federate, 
                           InteractionClassHandle interactionHandle, ParameterHandleValueMap parameters, 
                           byte[] tag)
    {
    	this(federate, interactionHandle, parameters, tag, null);
    }
    
	public InteractionBase(FederateBase federate,
	                       InteractionClassHandle interactionHandle, ParameterHandleValueMap parameters,
	                       byte[] tag, LogicalTime time)
	{
		this.federate = federate;
		this.interactionHandle = interactionHandle;
		this.parameters = parameters;
		this.tag = tag;
		this.time = time;
		
		this.interactionIdentifier = this.federate.getInteractionIdentifierFromHandle( this.interactionHandle );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void update(InteractionClassHandle instanceHandle, ParameterHandleValueMap parameters,
	                   byte[] tag, LogicalTime time)
	{
		// sanity check that we are not updating from another instance's properties
		if( this.interactionHandle.equals( instanceHandle ) )
		{
			// do the things
			this.tag = tag;
			this.time = time;
			mergeParameters( parameters );
		}
	}
	
	/**
	 * Get the name of this interaction.
	 *
	 * @return the name of this interaction.
	 */
	public String getName()
	{
		return this.interactionIdentifier;
	}
	
	/**
	 * Get the handle of this interaction.
	 *
	 * @return the handle of this interaction.
	 */
	public InteractionClassHandle getHandle()
	{
		return this.interactionHandle;
	}
	
    public String parameterName(ParameterHandle handle)
    {
    	return federate.getParameterIdentifierFromHandle( this.interactionHandle, handle );
    }
    
    public byte[] parameterRawValue(ParameterHandle handle)
    {
    	return parameters.get(handle);
    }
    
    public String parameterValue(ParameterHandle handle)
    {
    	return CODEC_UTILS.decodeString(parameterRawValue( handle ));
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
    
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		// print the handle
		builder.append( "\n\thandle = " + interactionHandle );
		builder.append( ": " );
		
		builder.append( this.interactionIdentifier == null ? "UNKOWN INTERACTION" : this.interactionIdentifier );
		
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
			builder.append( CODEC_UTILS.decodeString(rawValue) );
			builder.append( "' (" );
			builder.append( rawValue.length );
			builder.append( " bytes)" );
		}
		builder.append( "\n" );

		return builder.toString();		
	}
	
	private void mergeParameters( ParameterHandleValueMap parameters )
	{
		if( parameters == null )
			return;

		this.parameters.putAll( parameters );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

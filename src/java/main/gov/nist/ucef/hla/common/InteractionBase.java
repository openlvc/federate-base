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

import java.util.Collections;
import java.util.Map;

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

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateBase federate;
	private InteractionClassHandle interactionHandle;
	private ParameterHandleValueMap parameters;
	private byte[] tag;
	private LogicalTime time;

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
     * Get the current value of all parameters this object instance.
     *
     * @return An map of parameters handle names to their current values (note that this is not 
     * 		   modifiable but reflects changes made to the underlying data)
     */
    public Map<ParameterHandle, byte[]> getState()
    {
    	return Collections.unmodifiableMap(this.parameters);
    }
    
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		// print the handle
		builder.append( "\n\thandle = " + interactionHandle );
		builder.append( ": " );
		
		String interactionIdentifier = federate.getInteractionIdentifierFromHandle( this.interactionHandle );
		
		builder.append( interactionIdentifier == null ? "UNKOWN INTERACTION" : interactionIdentifier );
		
		// print the tag
		builder.append( "\n\ttag = " + new String(tag) );
		// print the time (if we have it)
		if( time != null )
		{
			builder.append( "\n\ttime = " + ((HLAfloat64Time)time).getValue() );
		}
		
		// print the parameter information
		builder.append( "\n\tparameterCount = " + parameters.size() );
		for( ParameterHandle parameter : parameters.keySet() )
		{
			// print the parameter handle
			builder.append( "\n\t\tparamHandle = " );
			builder.append( parameter );
			// print the parameter value
			builder.append( "\n\t\tparamValue = " );
			builder.append( parameters.get(parameter).length );
			builder.append( " bytes" );
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

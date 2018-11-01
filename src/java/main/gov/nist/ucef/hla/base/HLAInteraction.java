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
import java.util.Map;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.time.HLAfloat64Time;

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

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIAmbassadorWrapper rtiamb;
	private InteractionClassHandle interactionClassHandle;
	private ParameterHandleValueMap parameters;

	private String interactionClassName;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLAInteraction( InteractionClassHandle interactionClassHandle,
	                       ParameterHandleValueMap parameters )
	{
		this.rtiamb = RTIAmbassadorWrapper.instance();
		
		this.interactionClassHandle = interactionClassHandle;
		this.parameters = parameters == null ? rtiamb.makeParameterMap() : parameters;
		
		this.interactionClassName = rtiamb.getInteractionClassName( this.interactionClassHandle );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Get the identifier of this interaction.
	 *
	 * @return the identifier of this interaction.
	 */
	public String getClassName()
	{
		return this.interactionClassName;
	}
	
	/**
	 * Get the class handle of this interaction.
	 *
	 * @return the class handle of this interaction.
	 */
	public InteractionClassHandle getClassHandle()
	{
		return this.interactionClassHandle;
	}
	
	/**
	 * Obtain the name of a parameter from the handle
	 * 
	 * @param handle the parameter handle
	 * @return the name of the parameter, or null if the given handle is not related to this interaction
	 */
    public String parameterName(ParameterHandle handle)
    {
    	return rtiamb.getParameterName( this.interactionClassHandle, handle );
    }
    
	/**
	 * Obtain the raw byte array value of a parameter from the handle
	 * 
	 * @param handle the parameter handle
	 * @return the raw byte array value of a parameter from the handle, or null if the given handle is
	 *         not related to this interaction or the parameter has not been initialized
	 */
    public byte[] parameterValue(ParameterHandle handle)
    {
    	return parameters.get(handle);
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
     * Publish this interaction with a tag and timestamp. Will gracefully handle tag and/or 
     * timestamp arguments being null
     * 
     * @param tag the tag
     * @param time the timestamp
     */
    public void send(byte[] tag, HLAfloat64Time time)
    {
    	rtiamb.sendInteraction( this.interactionClassHandle, this.parameters, tag, time );
    }
    
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

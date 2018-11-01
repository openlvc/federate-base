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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.time.HLAfloat64Time;

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
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIAmbassadorWrapper rtiamb;
	
	private ObjectClassHandle objectClassHandle;
	private ObjectInstanceHandle objectInstanceHandle;
	private AttributeHandleValueMap attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLAObject( ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributes)
	{
		this.rtiamb = RTIAmbassadorWrapper.instance();

		this.objectInstanceHandle = objectInstanceHandle;
		this.attributes = attributes == null ? rtiamb.makeAttributeMap() : attributes;
		
		this.objectClassHandle = rtiamb.getKnownObjectClassHandle( objectInstanceHandle );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public HLAObject update( ObjectInstanceHandle objectInstanceHandle,
	                         AttributeHandleValueMap attributes,
	                         byte[] tag, LogicalTime time)
	{
		// sanity check that we are not updating from another instance's properties
		if( this.objectInstanceHandle.equals( objectInstanceHandle ) )
		{
			this.attributes.putAll( attributes );
		}
		return this;
	}
	
	/**
	 * Get the handle of this object instance.
	 *
	 * @return the handle of this object instance.
	 */
	public ObjectInstanceHandle getInstanceHandle()
	{
		return this.objectInstanceHandle;
	}
	
	/**
	 * Get the class handle of this object instance.
	 *
	 * @return the handle of this interaction.
	 */
	public ObjectClassHandle getClassHandle()
	{
		return this.objectClassHandle;
	}
	
	/**
	 * Obtain the name of an attribute from the handle
	 * 
	 * @param handle the attribute handle
	 * @return the name of the attribute, or null if the given handle is not related to this
	 *         instance's class
	 */
    public String attributeName(AttributeHandle handle)
    {
    	return rtiamb.getAttributeName( this.objectClassHandle, handle );
    }
    
	/**
	 * Obtain the the raw byte array value of an attribute from the handle
	 * 
	 * @param handle the attribute handle
	 * @return the raw byte array value of the attribute, or null if the given handle is not related to
	 *         this instance's class
	 */
    public byte[] attributeValue(AttributeHandle handle)
    {
    	return this.attributes.get(handle);
    }
    
    /**
     * Get the current value of all attributes this object instance.
     *
     * @return An map of attribute handles names to their current values (note that this is not modifiable 
     *         but reflects changes made to the underlying data)
     */
    public Map<AttributeHandle, byte[]> getState()
    {
    	return Collections.unmodifiableMap(this.attributes);
    }
    
    /**
     * Get the attribute handles of this object instance.
     *
     * @return the attribute handles of this object instance.
     */
    public Collection<AttributeHandle> getAttributeHandles()
    {
    	return Collections.unmodifiableSet(this.attributes.keySet());
    }
    
    /**
     * Get the attribute identifiers of this object instance.
     *
     * @return the attribute identifiers of this object instance.
     */
    public Collection<String> getAttributeNames()
    {
		List<String> result = new ArrayList<>();
		for( AttributeHandle handle : this.attributes.keySet() )
		{
			result.add( attributeName( handle ) );
		}
		return Collections.unmodifiableList(result);
    }
    
    /**
     * Update this instance with a tag and timestamp. Will gracefully handle tag and/or 
     * timestamp arguments being null
     * 
     * @param tag the tag
     * @param time the timestamp
     */
    public void update(byte[] tag, HLAfloat64Time time)
    {
		rtiamb.updateAttributeValues( this.objectInstanceHandle, this.attributes, tag, time);
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

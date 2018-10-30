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
import gov.nist.ucef.hla.util.RTIUtils;
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
public class InstanceBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static HLACodecUtils CODEC_UTILS = HLACodecUtils.instance();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ObjectInstanceHandle instanceHandle;
	private AttributeHandleValueMap attributes;
	private byte[] tag;
	private LogicalTime time;

	private RTIUtils rtiUtils;
	private String instanceIdentifier;
	private ObjectClassHandle objectClassHandle;
	private String classIdentifier;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public InstanceBase( RTIUtils rtiUtils,
	                     ObjectClassHandle objectClassHandle, Collection<String> attributes)
	{
		this.rtiUtils = rtiUtils;
		this.objectClassHandle = objectClassHandle;
		this.instanceHandle = this.rtiUtils.registerObject( this.objectClassHandle );
		
		if(attributes == null)
		{
			this.attributes = this.rtiUtils.makeEmptyAttributeMap();
		}
		else
		{
			this.attributes = rtiUtils.makeAttributeMap( objectClassHandle, attributes );
		}

		this.classIdentifier = this.rtiUtils.getClassIdentifierFromClassHandle( this.objectClassHandle );
		this.instanceIdentifier = this.rtiUtils.getObjectInstanceIdentifierFromHandle( instanceHandle );
		
		this.tag = new byte[0];
		this.time = null; // will be null if the attribute update was local rather than from RTI
	}
	
	public InstanceBase( RTIUtils rtiUtils,
	                     ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributes)
	{
		this.rtiUtils = rtiUtils;
		this.instanceHandle = instanceHandle;
		this.attributes = attributes == null ? this.rtiUtils.makeEmptyAttributeMap() : attributes;
		
		this.instanceIdentifier = this.rtiUtils.getObjectInstanceIdentifierFromHandle( instanceHandle );
		this.objectClassHandle = this.rtiUtils.getClassHandleFromInstanceHandle( instanceHandle );
		this.classIdentifier = this.rtiUtils.getClassIdentifierFromClassHandle( this.objectClassHandle );
		
		this.tag = new byte[0];
		this.time = null; // will be null if the attribute update was local rather than from RTI
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public InstanceBase updateAttribute(String key, short value)
	{
		rtiUtils.updateAttribute( this.objectClassHandle, key, value, this.attributes );
		return this;
	}
	
	public InstanceBase updateAttribute(String key, int value)
	{
		rtiUtils.updateAttribute( this.objectClassHandle, key, value, this.attributes );
		return this;
	}
	
	public InstanceBase updateAttribute(String key, long value)
	{
		rtiUtils.updateAttribute( this.objectClassHandle, key, value, this.attributes );
		return this;
	}
	
	public InstanceBase updateAttribute(String key, float value)
	{
		rtiUtils.updateAttribute( this.objectClassHandle, key, value, this.attributes );
		return this;
	}
	
	public InstanceBase updateAttribute(String key, double value)
	{
		rtiUtils.updateAttribute( this.objectClassHandle, key, value, this.attributes );
		return this;
	}
	
	public InstanceBase updateAttribute(String key, boolean value)
	{
		rtiUtils.updateAttribute( this.objectClassHandle, key, value, this.attributes );
		return this;
	}
	
	public InstanceBase updateAttribute(String key, String value)
	{
		rtiUtils.updateAttribute( this.objectClassHandle, key, value, this.attributes );
		return this;
	}
	
	public InstanceBase update(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributes,
	                           byte[] tag, LogicalTime time)
	{
		// sanity check that we are not updating from another instance's properties
		if( this.instanceHandle.equals( instanceHandle ) )
		{
			// do the things
			this.tag = tag;
			this.time = time;
			mergeAttributes( attributes );
		}
		return this;
	}
	
	/**
	 * Get the identifier of this object instance.
	 *
	 * @return the identifier of this object instance
	 */
	public String getInstanceIdentifier()
	{
		return this.instanceIdentifier;
	}
	
	/**
	 * Get the handle of this object instance.
	 *
	 * @return the handle of this object instance.
	 */
	public ObjectInstanceHandle getInstanceHandle()
	{
		return this.instanceHandle;
	}
	
	/**
	 * Get the class identifier of this object instance.
	 *
	 * @return the class identifier of this object instance
	 */
	public String getClassIdentifier()
	{
		return this.classIdentifier;
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
	
    public String attributeName(AttributeHandle handle)
    {
    	return this.rtiUtils.getAttributeIdentifierFromHandle( this.objectClassHandle, handle );
    }
    
    public byte[] attributeRawValue(AttributeHandle handle)
    {
    	return this.attributes.get(handle);
    }
    
    public String attributeValue(AttributeHandle handle)
    {
    	return CODEC_UTILS.decodeString(attributeRawValue(handle));
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
     * Get the attribute identifiers and associated values of this object instance.
     *
     * @return An map of attribute handle names to their current values
     */
    public Map<String, String> getAttributeNamesAndValues()
    {
    	Map<String, String> result = new HashMap<>();
		for( AttributeHandle handle : this.attributes.keySet() )
		{
			result.put(attributeName(handle), attributeValue(handle) );
		}
    	return Collections.unmodifiableMap(result);
    }
    
    public void publish()
    {
    	publish( null, null );
    }
    
    public void publish(byte[] tag)
    {
    	publish( tag, null );
    }
    
    public void publish(HLAfloat64Time time)
    {
    	publish( null, time );
    }
    
    public void publish(byte[] tag, HLAfloat64Time time)
    {
    	tag = tag == null ? new byte[0] : tag;
		this.rtiUtils.publishAttributes( this.instanceHandle, this.attributes, tag, time);
    }
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append( "\n\tinstance handle = " + instanceHandle );
		if( instanceIdentifier != null )
			builder.append( " (" + instanceIdentifier + ") " );
		builder.append( " > class handle = " );
		builder.append( objectClassHandle );
		if( classIdentifier != null )
			builder.append( " (" + classIdentifier + ")" );
		builder.append( ": " );
		
		// print the tag
		builder.append( "\n\ttag = " + new String(tag) );
		// print the time (if we have it) we'll get null if we are just receiving
		// a forwarded call from the other reflect callback above
		if( time != null )
		{
			builder.append( "\n\ttime = " + ((HLAfloat64Time)time).getValue() );
		}
		
		// print the attribute information
		builder.append( "\n\tattributeCount = " + this.attributes.size() );
		for( Entry<AttributeHandle,byte[]> entry : this.attributes.entrySet() )
		{
			AttributeHandle attributeHandle = entry.getKey();
			byte[] rawValue = entry.getValue();
			
			// print the attribute handle
			builder.append( "\n\t\tattributeHandle = " );

			String attributeIdentifier = attributeName( attributeHandle );
			// if we're dealing with Flavor, decode into the appropriate enum value
			builder.append( attributeHandle );
			builder.append( ": " );
			builder.append( attributeIdentifier == null ? "UNKNOWN ATTRIBUTE" : attributeIdentifier );
			builder.append( "\n\t\tattributeValue = " );
			builder.append( CODEC_UTILS.decodeString(rawValue) );
			builder.append( "' (" );
			builder.append( rawValue.length );
			builder.append( " bytes)" );
		}
		
		return builder.toString();		
	}
	
	private void mergeAttributes( AttributeHandleValueMap attributes )
	{
		if( attributes == null )
			return;

		this.attributes.putAll( attributes );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

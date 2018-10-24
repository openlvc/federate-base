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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.util.HLACodecUtils;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.time.HLAfloat64Time;

/**
 * The purpose of this class is to provide (as much as is possible) methods which are common to all
 * federate objects in order to minimize the amount of code required in UCEF HLA federate implementations.
 */
public class ObjectBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    private static final Logger logger = LogManager.getLogger(ObjectBase.class);

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateBase federate;
	private ObjectInstanceHandle instanceHandle;
	private AttributeHandleValueMap attributes;
	private byte[] tag;
	private LogicalTime time;

	private ObjectClassHandle objectClassHandle;
	private String instanceIdentifier;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
    public ObjectBase(FederateBase federate,
                      ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributes,
                      byte[] tag)
    {
    	this(federate, instanceHandle, attributes, tag, null);
    }
    
	public ObjectBase(FederateBase federate,
	                  ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributes,
	                  byte[] tag, LogicalTime time)
	{
		this.federate = federate;
		this.instanceHandle = instanceHandle;
		this.attributes = attributes;
		this.tag = tag;
		this.time = time; // will be null if the attribute update was local rather than from RTI
		
		this.objectClassHandle = federate.getClassHandleFromInstanceHandle( instanceHandle );
		this.instanceIdentifier = federate.getClassIdentifierFromClassHandle( objectClassHandle );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void update(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributes,
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
	}
	
	public AttributeHandleValueMap getAttributes()
	{
		return attributes;
	}
	
	@Override
	public String toString()
	{
		HLACodecUtils codecUtils = HLACodecUtils.instance();
			
		StringBuilder builder = new StringBuilder();
		
		builder.append( "\n\thandle = " + instanceHandle );
		builder.append( ": " );
		builder.append( instanceIdentifier == null ? "UNKNOWN INSTANCE" : instanceIdentifier );
		
		// print the tag
		builder.append( "\n\ttag = " + new String(tag) );
		// print the time (if we have it) we'll get null if we are just receiving
		// a forwarded call from the other reflect callback above
		if( time != null )
		{
			builder.append( "\n\ttime = " + ((HLAfloat64Time)time).getValue() );
		}
		
		// print the attribute information
		builder.append( "\n\tattributeCount = " + attributes.size() );
		for( AttributeHandle attributeHandle : attributes.keySet() )
		{
			// print the attribute handle
			builder.append( "\n\t\tattributeHandle = " );

			String attributeIdentifier = federate.getAttributeIdentifierFromHandle( objectClassHandle, attributeHandle );
			// if we're dealing with Flavor, decode into the appropriate enum value
			builder.append( attributeHandle );
			builder.append( ": " );
			builder.append( attributeIdentifier == null ? "UNKNOWN ATTRIBUTE" : attributeIdentifier );
			builder.append( "\n\t\tattributeValue = " );
			// TODO decode appropriately, automatically!
			builder.append( codecUtils.decodeString(attributes.get(attributeHandle)) );
		}
		
		return builder.toString();		
	}
	
	private void mergeAttributes( AttributeHandleValueMap attributes )
	{
		if( attributes == null )
			return;

		for( AttributeHandle key : attributes.keySet() )
		{
			byte[] value = attributes.get( key );
			this.attributes.put( key, value );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

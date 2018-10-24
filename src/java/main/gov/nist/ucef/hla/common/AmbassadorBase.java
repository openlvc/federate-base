/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-java.
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

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public class AmbassadorBase extends NullFederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    private static final Logger logger = LogManager.getFormatterLogger(AmbassadorBase.class);

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateBase federate;

	// TODO - provide accessors for these rather than making them externally available
	//        within the package via `protected`
	
	// these variables are accessible in the package
	protected double federateTime        = 0.0;
	protected double federateLookahead   = 1.0;
	
	protected boolean isRegulating       = false;
	protected boolean isConstrained      = false;
	protected boolean isAdvancing        = false;
	
	protected SyncPoint announcedSyncPoint = null;
	protected SyncPoint currentSyncPoint = null;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public AmbassadorBase( FederateBase federate )
	{
		this.federate = federate;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private String decodeString( byte[] bytes )
	{
		HLAunicodeString value = federate.encoderFactory.createHLAunicodeString();
		// decode
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			return "Decoder Exception: "+de.getMessage();
		}
	}

	private short decodeShort( byte[] bytes )
	{
		HLAinteger16BE value = federate.encoderFactory.createHLAinteger16BE();
		// decode
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			de.printStackTrace();
			return 0;
		}
	}
	
	private int decodeInt( byte[] bytes )
	{
		HLAinteger32BE value = federate.encoderFactory.createHLAinteger32BE();
		// decode
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			de.printStackTrace();
			return 0;
		}
	}
	
	private long decodeLong( byte[] bytes )
	{
		HLAinteger64BE value = federate.encoderFactory.createHLAinteger64BE();
		// decode
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			de.printStackTrace();
			return 0;
		}
	}
	
	//////////////////////////////////////////////////////////////////////////
	////////////////////////// RTI Callback Methods //////////////////////////
	//////////////////////////////////////////////////////////////////////////
	@Override
	public void synchronizationPointRegistrationFailed( String label,
	                                                    SynchronizationPointFailureReason reason )
	{
		SyncPoint syncPoint = SyncPoint.fromID( label );
		logger.error( "Failed to register sync point: " + syncPoint.toString() + ", reason="+reason );
	}

	@Override
	public void synchronizationPointRegistrationSucceeded( String label )
	{
		SyncPoint syncPoint = SyncPoint.fromID( label );
		logger.error( "Successfully registered sync point: " + syncPoint.toString() );
	}

	@Override
	public void announceSynchronizationPoint( String label, byte[] tag )
	{
		SyncPoint syncPoint = SyncPoint.fromID( label );
		logger.error( "Synchronization point announced: " + syncPoint.toString() );
		this.announcedSyncPoint = syncPoint;
	}

	@Override
	public void federationSynchronized( String label, FederateHandleSet failed )
	{
		SyncPoint syncPoint = SyncPoint.fromID( label );
		logger.error( "Federation Synchronized: " + syncPoint.toString() );
		this.currentSyncPoint = syncPoint;
	}

	/**
	 * The RTI has informed us that time regulation is now enabled.
	 */
	@Override
	public void timeRegulationEnabled( LogicalTime time )
	{
		this.federateTime = ((HLAfloat64Time)time).getValue();
		this.isRegulating = true;
	}

	@Override
	public void timeConstrainedEnabled( LogicalTime time )
	{
		this.federateTime = ((HLAfloat64Time)time).getValue();
		this.isConstrained = true;
	}

	@Override
	public void timeAdvanceGrant( LogicalTime time )
	{
		this.federateTime = ((HLAfloat64Time)time).getValue();
		this.isAdvancing = false;
	}

	@Override
	public void discoverObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                    ObjectClassHandle objectClassHandle,
	                                    String objectName)
	    throws FederateInternalError
	{
		logger.error( "Discovered Object: handle=" + objectInstanceHandle + ", classHandle=" +
		     objectClassHandle + ", name=" + objectName);
	}
	
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                    ObjectClassHandle objectClassHandle,
	                                    String objectName,
	                                    FederateHandle federateHandle)
    	throws FederateInternalError
	{
		logger.error( "Discovered Object: handle=" + objectInstanceHandle + ", classHandle=" +
			objectClassHandle + ", name=" + objectName + " federate="+federateHandle);
	}

	@Override
	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] tag,
	                                    OrderType sentOrder,
	                                    TransportationTypeHandle transport,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
			// just pass it on to the other method for printing purposes
			// passing null as the time will let the other method know it
			// it from us, not from the RTI
			reflectAttributeValues( theObject,
			                        theAttributes,
			                        tag,
			                        sentOrder,
			                        transport,
			                        null,
			                        sentOrder,
			                        reflectInfo );
	}

	@Override
	public void reflectAttributeValues( ObjectInstanceHandle objectInstanceHandle,
	                                    AttributeHandleValueMap attributeHandleValueMap,
	                                    byte[] tag,
	                                    OrderType sentOrdering,
	                                    TransportationTypeHandle transportTypeHandle,
	                                    LogicalTime time,
	                                    OrderType receivedOrdering,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
		StringBuilder builder = new StringBuilder( "Reflection for object:" );
		
		// print the handle
		builder.append( "\n\thandle = " + objectInstanceHandle );
		builder.append( ": " );
		ObjectClassHandle objectClassHandle = federate.getClassHandleFromInstanceHandle( objectInstanceHandle );
		String instanceIdentifier = federate.getClassIdentifierFromClassHandle( objectClassHandle );
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
		builder.append( "\n\tattributeCount = " + attributeHandleValueMap.size() );
		for( AttributeHandle attributeHandle : attributeHandleValueMap.keySet() )
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
			builder.append( decodeString(attributeHandleValueMap.get(attributeHandle)) );
		}
		builder.append( "\n" );
		
		logger.error( builder.toString() );
	}

	@Override
	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle theTransport,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		// just pass it on to the other method for printing purposes
		// passing null as the time will let the other method know it
		// it from us, not from the RTI
		this.receiveInteraction( interactionClass,
		                         theParameters,
		                         tag,
		                         sentOrdering,
		                         theTransport,
		                         null,
		                         sentOrdering,
		                         receiveInfo );
	}

	@Override
	public void receiveInteraction( InteractionClassHandle interactionHandle,
	                                ParameterHandleValueMap theParameters,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle theTransport,
	                                LogicalTime time,
	                                OrderType receivedOrdering,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		StringBuilder builder = new StringBuilder( "Interaction Received:" );
		
		// print the handle
		builder.append( "\n\thandle = " + interactionHandle );
		builder.append( ": " );
		
		String interactionIdentifier = federate.getInteractionIdentifierFromHandle( interactionHandle );
		
		builder.append( interactionIdentifier == null ? "UNKOWN INTERACTION" : interactionIdentifier );
		
		// print the tag
		builder.append( "\n\ttag = " + new String(tag) );
		// print the time (if we have it) we'll get null if we are just receiving
		// a forwarded call from the other reflect callback above
		if( time != null )
		{
			builder.append( "\n\ttime = " + ((HLAfloat64Time)time).getValue() );
		}
		
		// print the parameter information
		builder.append( "\n\tparameterCount = " + theParameters.size() );
		for( ParameterHandle parameter : theParameters.keySet() )
		{
			// print the parameter handle
			builder.append( "\n\t\tparamHandle = " );
			builder.append( parameter );
			// print the parameter value
			builder.append( "\n\t\tparamValue = " );
			builder.append( theParameters.get(parameter).length );
			builder.append( " bytes" );
		}
		builder.append( "\n" );

		logger.error( builder.toString() );
	}

	@Override
	public void removeObjectInstance( ObjectInstanceHandle theObject,
	                                  byte[] tag,
	                                  OrderType sentOrdering,
	                                  SupplementalRemoveInfo removeInfo )
	    throws FederateInternalError
	{
		logger.error( "Object Removed: handle = " + theObject );
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

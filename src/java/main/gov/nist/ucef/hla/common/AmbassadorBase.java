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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
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

	private IUCEFFederateImplementation federateImplementation;
	private FederateConfiguration federateConfiguration;
	
	protected SyncPoint announcedSyncPoint = null;
	protected SyncPoint currentSyncPoint = null;
	private Map<ObjectInstanceHandle, ObjectBase> objectInstanceLookup;
	private Map<InteractionClassHandle, InteractionBase> interactionLookup;
	
	// TODO - should we provide accessors for these rather than making them externally available
	//        within the package via `protected`...?
	// NOTE: these variables are accessible in the package
	protected double federateTime        = 0.0;
	protected boolean isRegulating       = false;
	protected boolean isConstrained      = false;
	protected boolean isAdvancing        = false;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public AmbassadorBase( FederateBase federate )
	{
		this.federate = federate;
		
		this.federateImplementation = federate.getFederateImplementation();
		this.federateConfiguration = federate.getFederateConfiguration();
		
		this.objectInstanceLookup = new HashMap<ObjectInstanceHandle, ObjectBase>(); 
		this.interactionLookup = new HashMap<InteractionClassHandle, InteractionBase>(); 
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////
	////////////////////////// RTI Callback Methods //////////////////////////
	//////////////////////////////////////////////////////////////////////////
	@Override
	public void synchronizationPointRegistrationSucceeded( String syncPointID )
	{
		SyncPoint syncPoint = SyncPoint.fromID( syncPointID );
		logger.info( String.format( "Successfully registered synchronization point '%s'",
		                            syncPoint.toString() ) );
	}
	
	@Override
	public void synchronizationPointRegistrationFailed( String syncPointID, SynchronizationPointFailureReason reason )
	{
		SyncPoint syncPoint = SyncPoint.fromID( syncPointID );
		logger.warn( String.format( "Failed to register synchronization point '%s' because %s",
		                            syncPoint.toString(), reason ) );
	}

	@Override
	public void announceSynchronizationPoint( String syncPointID, byte[] tag )
	{
		SyncPoint syncPoint = SyncPoint.fromID( syncPointID );
		this.announcedSyncPoint = syncPoint;
		logger.info( "Synchronization point '%s' was announced.", syncPoint.toString() );
	}

	@Override
	public void federationSynchronized( String syncPointID, FederateHandleSet handleSet )
	{
		SyncPoint syncPoint = SyncPoint.fromID( syncPointID );
		this.currentSyncPoint = syncPoint;
		logger.info( "Federation of %d federate(s) has synchronized at '%s'.",
		             handleSet.size(), syncPoint.toString() );
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
		logger.debug( String.format( "Discovered Object: instance handle = %s, class handle = %s, name = %s",
		                             objectClassHandle, objectInstanceHandle, objectName) );
	}
	
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                    ObjectClassHandle objectClassHandle,
	                                    String objectName,
	                                    FederateHandle federateHandle)
    	throws FederateInternalError
	{
		logger.debug( String.format( "Discovered Object: instance handle = %s, class handle = %s, name = %s, federate handle = %s",
		                             objectClassHandle, objectInstanceHandle, objectName, federateHandle) );
	}

	@Override
	public void reflectAttributeValues( ObjectInstanceHandle objectInstanceHandle,
	                                    AttributeHandleValueMap attributeHandleValueMap,
	                                    byte[] tag,
	                                    OrderType sentOrder,
	                                    TransportationTypeHandle transportationTypeHandle,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
			// just pass it on to the other method for printing purposes
			// passing null as the time will let the other method know it
			// it from us, not from the RTI
			reflectAttributeValues( objectInstanceHandle,
			                        attributeHandleValueMap,
			                        tag,
			                        sentOrder,
			                        transportationTypeHandle,
			                        null,
			                        sentOrder,
			                        reflectInfo );
	}

	@Override
	public void reflectAttributeValues( ObjectInstanceHandle objectInstanceHandle,
	                                    AttributeHandleValueMap attributeHandleValueMap,
	                                    byte[] tag,
	                                    OrderType sentOrdering,
	                                    TransportationTypeHandle transportationTypeHandle,
	                                    LogicalTime time,
	                                    OrderType receivedOrdering,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
		ObjectBase objectBase = this.objectInstanceLookup.get(objectInstanceHandle);
		if(objectBase == null)
		{
			objectBase = new ObjectBase(federate, objectInstanceHandle, attributeHandleValueMap, tag, time);
			this.objectInstanceLookup.put(objectInstanceHandle, objectBase);
		}
		else
		{
			objectBase.update(objectInstanceHandle, attributeHandleValueMap, tag, time);
		}

		this.federateImplementation.handleReflection( objectBase );
	}
	
	@Override
	public void receiveInteraction( InteractionClassHandle interactionClassHandle,
	                                ParameterHandleValueMap parameterHandleValueMap,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle transportTypeHandle,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		// just pass it on to the other method for printing purposes
		// passing null as the time will let the other method know it
		// it came from us, not from the RTI
		this.receiveInteraction( interactionClassHandle, parameterHandleValueMap, tag, 
		                         sentOrdering, transportTypeHandle, null, sentOrdering, receiveInfo );
	}

	@Override
	public void receiveInteraction( InteractionClassHandle interactionClassHandle,
	                                ParameterHandleValueMap theParameters,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle theTransport,
	                                LogicalTime time,
	                                OrderType receivedOrdering,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		InteractionBase interactionBase = this.interactionLookup.get(interactionClassHandle);
		if(interactionBase == null)
		{
			interactionBase = new InteractionBase(federate, interactionClassHandle, theParameters, tag, time);
			this.interactionLookup.put(interactionClassHandle, interactionBase);
		}
		else
		{
			interactionBase.update(interactionClassHandle, theParameters, tag, time);
		}
		
		this.federateImplementation.handleInteraction( interactionBase );
	}

	@Override
	public void removeObjectInstance( ObjectInstanceHandle theObject,
	                                  byte[] tag,
	                                  OrderType sentOrdering,
	                                  SupplementalRemoveInfo removeInfo )
	    throws FederateInternalError
	{
		logger.info( String.format("Object Removed: handle = %s", theObject ) );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

public class FederateAmbassador extends NullFederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Set<String> announcedPoints;
	private Set<String> achievedPoints;
	
	private FederateBase federate;
	
	private String currentSyncPoint;
	private String announcedSyncPoint;
	
	private double federateTime;
	protected boolean isTimeRegulating;
	protected boolean isTimeConstrained;
	
	// discovered (remote) objects
	private Map<ObjectInstanceHandle,HLAObject> remoteHlaObjects;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateAmbassador(FederateBase federate)
	{
		this.federate = federate;
		
		announcedPoints = new HashSet<>();
		achievedPoints = new HashSet<>();
		// initialize to null here so that it can be seen to be 
		// intentional rather than just "forgotten about"
		currentSyncPoint = null;
		announcedSyncPoint = null;
		
		remoteHlaObjects = new HashMap<>();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	/////////////////////////////////////////////////////////////////////////////////////
	// Simplified Access Methods     ////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	public String getAnnouncedSyncPoint()
	{
		return announcedSyncPoint;
	}
	
	public String getCurrentSyncPoint()
	{
		return currentSyncPoint;
	}
	
	/**
	 * Check if a synchronization point has (ever) been announced
	 * 
	 * @param label the identifying label of the synchronization point
	 * @return true if this synchronization point has already been announced, false otherwise
	 */
	public boolean isAnnounced( String label )
	{
		return announcedPoints.contains( label );
	}

	/**
	 * Check if a synchronization point has (ever) been achieved
	 * 
	 * @param label the identifying label of the synchronization point
	 * @return true if this synchronization point has already been achieved, false otherwise
	 */
	public boolean isAchieved( String label )
	{
		return achievedPoints.contains( label );
	}
	
	/**
	 * Get the current federate time as a double
	 * 
	 * @return the current federate time as a double
	 */
	public double getFederateTime()
	{
		return this.federateTime;
	}
	
	public void deleteObjectInstances()
	{
		this.federate.rtiamb.deleteObjectInstances( remoteHlaObjects.keySet() );	
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	// FederateAmbassador Callbacks     /////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////
	////////////////////////// RTI Callback Methods //////////////////////////
	//////////////////////////////////////////////////////////////////////////
	@Override
	public void synchronizationPointRegistrationSucceeded( String label )
	{
		// don't really need to do anything here
	}
	
	@Override
	public void synchronizationPointRegistrationFailed( String label,
	                                                    SynchronizationPointFailureReason reason )
	{
		// possibly we should be logging something here
	}

	@Override
	public void announceSynchronizationPoint( String label, byte[] tag )
	{
		this.announcedSyncPoint = label;
		synchronized (this.announcedPoints)
		{
			this.announcedPoints.add( label );
		}
		
		if( UCEFSyncPoint.isUnknown( label ) )
		{
			// non-UCEF synchronization point - should probably just achieve it immediately
			this.federate.rtiamb.synchronizationPointAchieved( label );
		}
	}

	@Override
	public void federationSynchronized( String label, FederateHandleSet handleSet )
	{
		this.currentSyncPoint = label;
		synchronized( this.achievedPoints )
		{
			this.achievedPoints.add( label );
		}
	}

	/**
	 * The RTI has informed us that time regulation is now enabled.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void timeRegulationEnabled( LogicalTime time )
	{
		this.federateTime = logicalTimeAsDouble( time );
		this.isTimeRegulating = true;
	}

	/**
	 * The RTI has informed us that time constraining is now enabled.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void timeConstrainedEnabled( LogicalTime time )
	{
		this.federateTime = logicalTimeAsDouble( time );
		this.isTimeConstrained = true;
	}

	/**
	 * The RTI has informed us that a time advance request has been granted
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void timeAdvanceGrant( LogicalTime time )
	{
		this.federateTime = logicalTimeAsDouble( time );
	}

	/**
	 * The RTI has informed us that an object instance has been discovered
	 */
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                    ObjectClassHandle objectClassHandle,
	                                    String objectName )
	    throws FederateInternalError
	{
		// just pass it on to the other method passing null as federate handle
		discoverObjectInstance( objectInstanceHandle, objectClassHandle, objectName, null );
	}
	
	/**
	 * The RTI has informed us that an object instance has been discovered
	 */
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                    ObjectClassHandle objectClassHandle,
	                                    String objectName,
	                                    FederateHandle federateHandle)
    	throws FederateInternalError
	{
		HLAObject hlaObjectInstance = this.remoteHlaObjects.get( objectInstanceHandle );
		if( hlaObjectInstance == null )
		{
			// this is what *should* be happening - since it's a discovery, it 
			// shouldn't be in the cache yet
			Set<String> attributeNames = federate.subscribedObjectClassAttributeNames.get(objectClassHandle);
			hlaObjectInstance = new HLAObject( objectInstanceHandle, attributeNames );
			this.remoteHlaObjects.put( objectInstanceHandle, hlaObjectInstance );
		}
		this.federate.receiveObjectRegistration( hlaObjectInstance );
	}

	/**
	 * The RTI has informed us that an attribute value reflection has been received
	 */
	@Override
	public void reflectAttributeValues( ObjectInstanceHandle objectInstanceHandle,
	                                    AttributeHandleValueMap attributeHandleValueMap,
	                                    byte[] tag,
	                                    OrderType sentOrder,
	                                    TransportationTypeHandle transportationTypeHandle,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
			// just pass it on to the other method - passing null as the time will let 
			// the other method know it's from us, not from the RTI
			reflectAttributeValues( objectInstanceHandle,
			                        attributeHandleValueMap,
			                        tag,
			                        sentOrder,
			                        transportationTypeHandle,
			                        null,
			                        sentOrder,
			                        reflectInfo );
	}

	/**
	 * The RTI has informed us that an attribute value reflection has been received
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void reflectAttributeValues( ObjectInstanceHandle objectInstanceHandle,
	                                    AttributeHandleValueMap attributeMap,
	                                    byte[] tag,
	                                    OrderType sentOrdering,
	                                    TransportationTypeHandle transportationTypeHandle,
	                                    LogicalTime time,
	                                    OrderType receivedOrdering,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
		// create and populate/update existing instance
		HLAObject hlaObjectInstance = this.remoteHlaObjects.get( objectInstanceHandle );
		if( hlaObjectInstance == null )
		{
			throw new UCEFException("Attribute value reflection received for undiscovered object " +
									"instance with handle '%s'", objectInstanceHandle);
		}

		// convert AttributeHandleValueMap to Map<String, byte[]> 
		Map<String,byte[]> attributes = this.federate.convert( objectInstanceHandle, attributeMap );
		hlaObjectInstance.setState( attributes );

		// do the appropriate callback on the federate
		if( time == null )
			this.federate.receiveAttributeReflection( hlaObjectInstance );
		else
			this.federate.receiveAttributeReflection( hlaObjectInstance, logicalTimeAsDouble( time ) );
	}
	
	/**
	 * The RTI has informed us that an interaction has been received
	 */
	@Override
	public void receiveInteraction( InteractionClassHandle interactionClassHandle,
	                                ParameterHandleValueMap parameterMap,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle transportTypeHandle,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		// just pass it on to the other method for printing purposes
		// passing null as the time will let the other method know it
		// it came from us, not from the RTI
		this.receiveInteraction( interactionClassHandle, parameterMap, tag, 
		                         sentOrdering, transportTypeHandle, null, sentOrdering, receiveInfo );
	}

	/**
	 * The RTI has informed us that an interaction has been received
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void receiveInteraction( InteractionClassHandle interactionClassHandle,
	                                ParameterHandleValueMap parameterMap,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle transportTypeHandle,
	                                LogicalTime time,
	                                OrderType receivedOrdering,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		// convert ParameterHandleValueMap to Map<String, byte[]> 
		Map<String,byte[]> parameters = this.federate.convert( interactionClassHandle, parameterMap );

		// create the (transient) interaction
		HLAInteraction interaction = new HLAInteraction( interactionClassHandle, parameters );

		// do the appropriate callback on the federate
		if( time == null )
			this.federate.receiveInteraction( interaction );
		else
			this.federate.receiveInteraction( interaction, logicalTimeAsDouble( time ) );
	}

	/**
	 * The RTI has informed us that an HLA object instance has been removed
	 */
	@Override
	public void removeObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                  byte[] tag,
	                                  OrderType sentOrdering,
	                                  SupplementalRemoveInfo removeInfo )
	    throws FederateInternalError
	{
		HLAObject hlaObjectInstance = this.remoteHlaObjects.remove( objectInstanceHandle ); 
		if( hlaObjectInstance == null )
		{
			throw new UCEFException("Deletion notification received for undiscovered object " +
									"instance with handle '%s'", objectInstanceHandle);
		}
		
		this.federate.receiveObjectDeleted( hlaObjectInstance );
	}
	
	/**
	 * Convenience function to convert a logical time instance to a double
	 *  
	 * @param time the logical time
	 * @return the double equivalent of the logical time, or null if the logical time is null 
	 */
	@SuppressWarnings("rawtypes")
	protected Double logicalTimeAsDouble( LogicalTime time )
	{
		return time == null ? null : ((HLAfloat64Time)time).getValue();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Utility Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

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
import java.util.Map.Entry;
import java.util.Set;

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
	private RTIAmbassadorWrapper rtiamb;
	
	private String currentSyncPoint;
	private String announcedSyncPoint;
	
	private double federateTime;
	private boolean isTimeRegulating;
	private boolean isTimeConstrained;
	
	private Map<ObjectInstanceHandle,HLAObject> hlaObjects;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateAmbassador(FederateBase federate)
	{
		this.federate = federate;
		
		this.rtiamb = RTIAmbassadorWrapper.instance();
		
		announcedPoints = new HashSet<>();;
		achievedPoints = new HashSet<>();
		// initialize to null here so that it can be seen to be 
		// intentional rather than just "forgotten about"
		currentSyncPoint = null;
		announcedSyncPoint = null;
		
		hlaObjects = new HashMap<>();
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
	
	public boolean isTimeRegulating()
	{
		return this.isTimeRegulating;
	}
	
	public boolean isTimeConstrained()
	{
		return this.isTimeConstrained;
	}
	
	public void deleteObjectInstances()
	{
		rtiamb.deleteObjectInstances( hlaObjects.keySet() );	
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
			rtiamb.synchronizationPointAchieved( label );
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
		// 
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
		// convert AttributeHandleValueMap to Map<String, byte[]> 
		Map<String,byte[]> attributes = convert( objectInstanceHandle, attributeMap );

		// create and populate/update existing instance
		HLAObject hlaObjectInstance = this.hlaObjects.get( objectInstanceHandle );
		if( hlaObjectInstance == null )
		{
			// remote instance
			hlaObjectInstance = new HLAObject( objectInstanceHandle, attributes );
			this.hlaObjects.put( objectInstanceHandle, hlaObjectInstance );
		}
		else
		{
			hlaObjectInstance.setState( attributes );
		}

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
		Map<String,byte[]> parameters = convert( interactionClassHandle, parameterMap );

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
		this.hlaObjects.remove( objectInstanceHandle );
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
	
	/**
	 * A utility method to encapsulate the code needed to convert a {@link AttributeHandleValueMap} into
	 * a populated map containing attribute names and their associated byte values 
	 * 
	 * @param oih the object instance handle with which the attributes are associated
	 * @param source the map containing attribute names and their associated byte values
	 * @return a populated {@link Map}
	 */
	private Map<String, byte[]> convert(ObjectInstanceHandle oih, AttributeHandleValueMap phvm)
	{
		ObjectClassHandle och = rtiamb.getKnownObjectClassHandle( oih );
		HashMap<String, byte[]> result = new HashMap<>();
		for(Entry<AttributeHandle, byte[]> entry : phvm.entrySet())
		{
			String name = rtiamb.getAttributeName( och, entry.getKey() );
			result.put( name, entry.getValue() );
		}
		return result;
	}
	
	/**
	 * A utility method to encapsulate the code needed to convert a {@link ParameterHandleValueMap} into
	 * a populated map containing parameter names and their associated byte values 
	 * 
	 * @param ich the interaction class handle with which the parameters are associated
	 * @param source the map containing parameter names and their associated byte values
	 * @return a populated {@link Map}
	 */
	private Map<String, byte[]> convert(InteractionClassHandle ich, ParameterHandleValueMap phvm)
	{
		HashMap<String, byte[]> result = new HashMap<>();
		for(Entry<ParameterHandle, byte[]> entry : phvm.entrySet())
		{
			String name = rtiamb.getParameterName( ich, entry.getKey() );
			result.put( name, entry.getValue() );
		}
		return result;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

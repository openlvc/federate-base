/*
 * This software is contributed as a public service by The National Institute of Standards 
 * and Technology (NIST) and is not subject to U.S. Copyright
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above NIST contribution notice and this permission and disclaimer notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. THE AUTHORS OR COPYRIGHT HOLDERS SHALL
 * NOT HAVE ANY OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
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
	
	private FederateBase federateBase;
	
	private String currentSyncPoint;
	private String announcedSyncPoint;
	
	private double federateTime;
	private boolean isTimeRegulated;
	private boolean isTimeConstrained;
	
	// discovered (remote) objects
	private Map<ObjectInstanceHandle,HLAObject> remoteHlaObjects;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateAmbassador(FederateBase federateBase)
	{
		this.federateBase = federateBase;
		
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
	
	/**
	 * Set the time regulated state of the federate
	 * 
	 * @param isTimeRegulated true if the federate is time regulated, false otherwise
	 */
	protected void setTimeRegulated( boolean isTimeRegulated )
	{
		this.isTimeRegulated = isTimeRegulated;
	}
	
	/**
	 * Determine if the federate is time regulated
	 * 
	 * @return true if the federate is time regulated, false otherwise
	 */
	protected boolean isTimeRegulated()
	{
		return this.isTimeRegulated;
	}

	/**
	 * Set the time constrained state of the federate
	 * 
	 * @param isTimeConstrained true if the federate is time constrained, false otherwise
	 */
	protected void setTimeConstrained( boolean isTimeConstrained )
	{
		this.isTimeConstrained = isTimeConstrained;
	}

	/**
	 * Determine if the federate is time constrained
	 * 
	 * @return true if the federate is time constrained, false otherwise
	 */
	protected boolean isTimeConstrained()
	{
		return this.isTimeConstrained;
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
			this.federateBase.rtiamb.synchronizationPointAchieved( label );
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
		this.isTimeRegulated = true;
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
			hlaObjectInstance = new HLAObject( objectInstanceHandle, null );
			this.remoteHlaObjects.put( objectInstanceHandle, hlaObjectInstance );
		}
		this.federateBase.incomingObjectRegistration( hlaObjectInstance );
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
		Map<String,byte[]> attributes = federateBase.rtiamb.convert( objectInstanceHandle, attributeMap );
		hlaObjectInstance.setState( attributes );

		// do the appropriate callback on the federate
		if( time == null )
			this.federateBase.incomingAttributeReflection( hlaObjectInstance );
		else
			this.federateBase.incomingAttributeReflection( hlaObjectInstance, logicalTimeAsDouble( time ) );
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
		Map<String,byte[]> parameters = federateBase.rtiamb.convert( interactionClassHandle, parameterMap );

		// create the (transient) interaction
		HLAInteraction interaction = new HLAInteraction( interactionClassHandle, parameters );

		// do the appropriate callback on the federate
		if( time == null )
			this.federateBase.incomingInteraction( interaction );
		else
			this.federateBase.incomingInteraction( interaction, logicalTimeAsDouble( time ) );
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
			throw new UCEFException( "Deletion notification received for undiscovered object " +
									 "instance with handle '%s'", objectInstanceHandle );
		}
		this.federateBase.incomingObjectDeleted( hlaObjectInstance );
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

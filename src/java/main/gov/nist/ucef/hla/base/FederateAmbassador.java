package gov.nist.ucef.hla.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

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
	private static final double MIN_TIME = 0.1;
	private static final double MAX_TIME = 0.2;
	
	private FederateBase federate;
	private RTIAmbassadorWrapper rtiamb;
	
	private String announcedSyncPoint;
	private String currentSyncPoint;
	
	private double federateTime;
	private boolean isRegulating;
	private boolean isConstrained;
	
	private Set<String> announcedPoints;
	private Set<String> synchronizedPoints;
	private Set<String> achievedPoints;
	private Map<ObjectInstanceHandle,HLAObject> hlaObjects;
	private Queue<HLAInteraction> interactions;
	private boolean isAdvancing;
	
	public FederateAmbassador(FederateBase federate)
	{
		this.rtiamb = RTIAmbassadorWrapper.instance();
		
		this.federate = federate;
		
		// initialize to null here so that it can be seen to be 
		// intentional rather than just "forgotten about"
		announcedSyncPoint = null;
		currentSyncPoint = null;
		
		announcedPoints = new HashSet<>();
		synchronizedPoints = new HashSet<>();
		achievedPoints = new HashSet<>();
		
		hlaObjects = new HashMap<>();
		interactions  = new LinkedBlockingQueue<>();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	// Simplified Access Methods     ////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	public boolean isAnnounced( String label )
	{
		return announcedPoints.contains( label );
	}

	public boolean isSynchronized( String label )
	{
		return synchronizedPoints.contains( label );
	}
	
	public boolean isAchieved( String label )
	{
		return achievedPoints.contains( label );
	}

	public void waitForSychronized( String label ) {
		while( currentSyncPoint == null || !currentSyncPoint.equals(label) )
		{
			rtiamb.evokeMultipleCallbacks( MIN_TIME, MAX_TIME );
		}
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
	public void synchronizationPointRegistrationFailed( String label, SynchronizationPointFailureReason reason )
	{
		// this is a failure - we should probably at least log this
	}

	@Override
	public void announceSynchronizationPoint( String label, byte[] tag )
	{
		if(UCEFSyncPoint.isUnknown( label ))
		{
			// non-UCEF synchronization point - just achieve it immediately
			rtiamb.synchronizationPointAchieved( label );
		}
		this.announcedSyncPoint = label;
	}

	@Override
	public void federationSynchronized( String label, FederateHandleSet handleSet )
	{
		this.currentSyncPoint = label;
	}

	/**
	 * The RTI has informed us that time regulation is now enabled.
	 */
	@Override
	public void timeRegulationEnabled( LogicalTime time )
	{
		this.federateTime = logicalTimeAsDouble(time);
		this.isRegulating = true;
	}

	@Override
	public void timeConstrainedEnabled( LogicalTime time )
	{
		this.federateTime = logicalTimeAsDouble(time);
		this.isConstrained = true;
	}

	@Override
	public void timeAdvanceGrant( LogicalTime time )
	{
		this.federateTime = logicalTimeAsDouble(time);
		this.isAdvancing = false;
	}

	@Override
	public void discoverObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                    ObjectClassHandle objectClassHandle,
	                                    String objectName)
	    throws FederateInternalError
	{
		// just pass it on to the other method passing null as federate handle
		discoverObjectInstance( objectInstanceHandle, objectClassHandle, objectName, null);
	}
	
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                    ObjectClassHandle objectClassHandle,
	                                    String objectName,
	                                    FederateHandle federateHandle)
    	throws FederateInternalError
	{
		// 
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

	@Override
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
		HLAObject instanceBase = this.hlaObjects.get( objectInstanceHandle );
		
		if(instanceBase == null)
		{
			// remote instance
			instanceBase = new HLAObject(objectInstanceHandle, attributeMap);
			this.hlaObjects.put(objectInstanceHandle, instanceBase);
		}
		else
		{
			instanceBase.update(objectInstanceHandle, attributeMap, tag, time);
		}

		this.federate.receiveAttributeReflection( instanceBase, logicalTimeAsDouble(time) );
	}
	
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

	@Override
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
		HLAInteraction interactionBase = new HLAInteraction( interactionClassHandle, parameterMap);
		this.federate.receiveInteraction( interactionBase, logicalTimeAsDouble(time) );
	}

	@Override
	public void removeObjectInstance( ObjectInstanceHandle objectInstanceHandle,
	                                  byte[] tag,
	                                  OrderType sentOrdering,
	                                  SupplementalRemoveInfo removeInfo )
	    throws FederateInternalError
	{
		this.hlaObjects.remove( objectInstanceHandle );
	}
	
	protected double logicalTimeAsDouble( LogicalTime time )
	{
		return ((HLAfloat64Time)time).getValue();
	}
}

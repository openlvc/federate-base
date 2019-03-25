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

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ResignAction;

public abstract class FederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final double MIN_TIME = 0.1;
	private static final double MAX_TIME = 0.2;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected FederateConfiguration configuration;
	
	protected RTIAmbassadorWrapper rtiamb;
	protected FederateAmbassador fedamb;
	
	private LifecycleState lifecycleState;
	
	private Map<ObjectClassHandle, Types.ObjectClass> objectClassByClassHandle;
	private Map<ObjectInstanceHandle, Types.ObjectClass> objectClassByInstanceHandle;
	private Map<ObjectInstanceHandle, HLAObject> hlaObjectByInstanceHandle;
	private Map<InteractionClassHandle, Types.InteractionClass> interactionClassByHandle;
	
	private final Object mutex_lock = new Object();
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected FederateBase()
	{
		this.rtiamb = new RTIAmbassadorWrapper();
		this.fedamb = new FederateAmbassador( this );
		
		this.objectClassByClassHandle = new HashMap<>();
		this.objectClassByInstanceHandle = new HashMap<>();
		this.hlaObjectByInstanceHandle = new HashMap<>();
		this.interactionClassByHandle = new HashMap<>();
		
		lifecycleState = LifecycleState.GESTATING;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Lifecycle Callback Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	protected abstract void beforeFederationJoin();
	protected abstract void beforeReadyToPopulate();
	protected abstract void beforeReadyToRun();
	protected abstract void beforeFirstStep();
	protected abstract boolean step( double currentTime ); // == 0
	protected abstract void beforeReadyToResign();
	protected abstract void beforeExit();
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public abstract void receiveObjectRegistration( HLAObject hlaObject );
	
	public abstract void receiveAttributeReflection( HLAObject hlaObject);
	public abstract void receiveAttributeReflection( HLAObject hlaObject, double time );
	
	public abstract void receiveInteraction( HLAInteraction hlaInteraction );
	public abstract void receiveInteraction( HLAInteraction hlaInteraction, double time );
	
	public abstract void receiveObjectDeleted( HLAObject hlaObject );
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Lifecycle State Query //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Determine the current lifecycle state of this federate.
	 * 
	 * <b>NOTE:</b> The lifecycyle state is managed by the federate itself (i.e.
	 * {@link FederateBase} and cannot be manually altered.
	 * 
	 * See also {@link LifecycleState} for possible states.
	 * 
	 * This principally provides a mechanism for federate implementations to differentiate between
	 * the main three cases:
	 * <ol>
	 * <li>{@link LifecycleState#INITIALIZING}: during {@link #beforeReadyToPopulate()},
	 *         {@link #beforeReadyToRun()} or {@link #beforeFirstStep()}</li>
	 * <li>{@link LifecycleState#RUNNING}: received in {@link #step(double)};</li>
	 * <li>{@link LifecycleState#CLEANING_UP}: received in {@link #beforeReadyToResign()} or
	 *         {@link #beforeExit()};</li>
	 * </ol>
	 * 
	 * This allows handling of incoming interactions and attribute reflections to be tailored to
	 * the current lifecycle state of the federate.
	 * 
	 * @return the current lifecycle state of this federate.
	 */
	public LifecycleState getLifecycleState()
	{
		return this.lifecycleState;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Federate Business ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This is the main method which carries out the life cycle of the federate
	 * 
	 * @param configuration the configuration for the federate
	 */
	public void runFederate( FederateConfiguration configuration )
	{
		// sanity check
		if(configuration == null)
			throw new UCEFException("Federate configuration cannot be null.");
			
		this.configuration = configuration;

		// create and join the Federation, publish and subscribe
		// call beforeReadyToPopulate(), beforeReadyToRun() and beforeFirstStep()
		this.lifecycleState = LifecycleState.INITIALIZING;
		federateSetup();
		// repeatedly call step() until simulation ends
		this.lifecycleState = LifecycleState.RUNNING;
		federateExecution();
		// disable any time policy
		// call readyToResign() and beforeExit()
		// resign and destroy the federation
		this.lifecycleState = LifecycleState.CLEANING_UP;
		federateTeardown();
		this.lifecycleState = LifecycleState.EXPIRED;
	}
	
	/**
	 * Carry out all steps required to get the federate ready to run through its main simulation loop
	 */
	protected void federateSetup()
	{
		createAndJoinFederation();
		enableTimePolicy();
		
		publishAndSubscribe();

		beforeReadyToPopulate();
		synchronize( UCEFSyncPoint.READY_TO_POPULATE );

		beforeReadyToRun();
		synchronize( UCEFSyncPoint.READY_TO_RUN );
		
		beforeFirstStep();
	}
	
	/**
	 * Run the federate through its main simulation loop
	 */
	protected void federateExecution()
	{
		while( true )
		{
			// next step, and cease simulation loop if step() returns false
			if( step( fedamb.getFederateTime() ) == false )
				break;
			advanceTime();
		}
	}
	
	/**
	 * Carry out all steps required to clean up and exit the federate
	 */
	protected void federateTeardown()
	{
		disableTimePolicy();

		beforeReadyToResign();
		synchronize( UCEFSyncPoint.READY_TO_RESIGN );
		beforeExit();

		resignAndDestroyFederation();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// INCOMING INTERACTION/REFLECTION HANDLING /////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public void incomingObjectRegistration( ObjectInstanceHandle instanceHandle, ObjectClassHandle classHandle )
	{
		synchronized( mutex_lock )
		{
    		// just delegate to the default handler
    		Types.ObjectClass objectClass = objectClassByClassHandle.get( classHandle );
    		
    		if( objectClass != null )
    		{
    			objectClassByInstanceHandle.put( instanceHandle, objectClass );
    			
    			HLAObject hlaObject = new HLAObject( objectClass.name, instanceHandle );
    			hlaObjectByInstanceHandle.put( instanceHandle, hlaObject );
    			
    			receiveObjectRegistration( hlaObject );
    		}
    		else
    		{
    			// TODO Log warning
    			System.out.println(String.format("Discovered unrecognized object instance %s", rtiamb.makeSummary( instanceHandle )));
    		}		
		}
	}

	public void incomingAttributeReflection( ObjectInstanceHandle handle, Map<String,byte[]> attributes )
	{
		synchronized( mutex_lock )
		{
			HLAObject hlaObject = hlaObjectByInstanceHandle.get( handle );
    		
    		if( hlaObject != null )
    		{
    			hlaObject.setState( attributes );
    			receiveAttributeReflection( hlaObject );
    		}
    		else
    		{
            	throw new UCEFException("Attribute value reflection received for previously undiscovered object " +
            						    "instance %s", this.rtiamb.makeSummary( handle ) );
//    			ObjectClassHandle classHandle = this.rtiamb.getKnownObjectClassHandle( handle );
//    			String className = this.rtiamb.getObjectClassName( classHandle );
//    			hlaObject = new HLAObject( className, attributes );
//    			synchronized( hlaObjectByInstanceHandle )
//    			{
//    				hlaObjectByInstanceHandle.put( handle, hlaObject );
//    			}
    		}
		}
		
		// just delegate to the default handler
	}

	public void incomingAttributeReflection( ObjectInstanceHandle handle, Map<String,byte[]> attributes, double time )
	{
		synchronized( mutex_lock )
		{
    		HLAObject hlaObject = hlaObjectByInstanceHandle.get( handle );
    		
    		if( hlaObject != null )
    		{
    			hlaObject.setState( attributes );
    			// just delegate to the default handler
    			receiveAttributeReflection( hlaObject, time );
    		}
    		else
    		{
                	throw new UCEFException("Attribute value reflection received for previously undiscovered object " +
                						    "instance %s", this.rtiamb.makeSummary( handle ) );
    //			ObjectClassHandle classHandle = this.rtiamb.getKnownObjectClassHandle( handle );
    //			String className = this.rtiamb.getObjectClassName( classHandle );
    //			hlaObject = new HLAObject( className, attributes );
    //			synchronized( hlaObjectByInstanceHandle )
    //			{
    //				hlaObjectByInstanceHandle.put( handle, hlaObject );
    //			}
    		}
		}
	}

	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters )
	{
		synchronized( mutex_lock )
		{
    		HLAInteraction interaction = makeInteraction( handle, parameters );
    		if( interaction != null )
    		{
    			// just delegate to the default handler
    			receiveInteraction( interaction );
    		}
    		else
    		{
    			// TODO log error
    		}
		}
	}

	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters, double time )
	{
		synchronized( mutex_lock )
		{
    		HLAInteraction interaction = makeInteraction( handle, parameters );
    		if( interaction != null )
    		{
    			// just delegate to the default handler
    			receiveInteraction( interaction, time );
    		}
    		else
    		{
    			// TODO log error
    		}
    	}
	}

	public void incomingObjectDeleted( ObjectInstanceHandle handle )
	{
		synchronized( mutex_lock )
		{
    		Types.ObjectClass objectClass = objectClassByInstanceHandle.remove( handle );
    		HLAObject hlaObject = hlaObjectByInstanceHandle.remove( handle );
    		
    //		logger.log( "Received object removed notification for HLAObject with id :" +
    //		             to_string(objectInstanceHash), LevelInfo );
    
    		if( objectClass != null && hlaObject != null)
    		{
    			// TODO logging
    //			logger.log( "HLAObject with id :" + to_string( objectInstanceHash ) +
    //			            " successsfully removed from the incoming map.", LevelInfo );
    			// HLAObject hlaObject = new HLAObject( objectClass.name, handle );
    			// just delegate to the default handler
    			receiveObjectDeleted( hlaObject );
    		}
    		else
    		{
    			// TODO logging
    //			logger.log( "HLAObject with id :" + to_string(objectInstanceHash) +
    //			            " could not find for deletion.", LevelWarn );
    //			throw new UCEFException( "Deletion notification received for previously undiscovered object " +
    //									 "instance with handle '%s'", handle );
    		}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Utility Methods ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Create an interaction (with no parameter values)
	 * 
	 * @param className the name of the interaction class
	 * @return the interaction
	 */
	protected HLAInteraction makeInteraction( String className )
	{
		return makeInteraction( className, null );
	}
	
	/**
	 * Create an interaction (with no parameter values)
	 * 
	 * @param className the name of the interaction class
	 * @param parameters the parameters for the interaction (may be an empty map or null)
	 * @return the interaction
	 */
	protected HLAInteraction makeInteraction( String className, Map<String, byte[]> parameters)
	{
		return rtiamb.makeInteraction( className, parameters );
	}
	
	/**
	 * Create an interaction
	 * 
	 * @param className the name of the interaction class
	 * @return the interaction
	 */
	protected HLAInteraction makeInteraction( InteractionClassHandle handle, Map<String, byte[]> parameters)
	{
		Types.InteractionClass interactionClass;
		synchronized(interactionClassByHandle)
		{
			interactionClass = interactionClassByHandle.get( handle );
		}
		
		if(interactionClass == null)
			return null;

		return makeInteraction( interactionClass.name, parameters );
	}
	
	/**
	 * Publish the provided interaction to the federation
	 * 
	 * @param interaction the interaction
	 */
	protected void sendInteraction( HLAInteraction interaction)
	{
		rtiamb.sendInteraction( interaction, null, null );
	}
	
	/**
	 * Publish the provided interaction to the federation with a tag (which can be null).
	 * 
	 * @param interaction the interaction
	 * @param tag the tag (can be null)
	 */
	protected void sendInteraction( HLAInteraction interaction, byte[] tag )
	{
		rtiamb.sendInteraction( interaction, tag, null );
	}

	/**
	 * Publish the provided interaction to the federation with a tag (which can be null) and
	 * time-stamp.
	 * 
	 * @param interaction the interaction
	 * @param tag the tag (can be null)
	 * @param time the time-stamp
	 */
	protected void sendInteraction( HLAInteraction interaction, byte[] tag, double time )
	{
		rtiamb.sendInteraction( interaction, tag, time );
	}

	/**
	 * Register the instance with the RTI. If the instance is already registered, there is no net
	 * effect (i.e., it will not get re-registered or become a new instance).
	 * 
	 * @param instance the object instance to be registered
	 * @return the object instance
	 */
	protected HLAObject register( HLAObject instance )
	{
		return this.rtiamb.registerObjectInstance( instance );
	}
	
	/**
	 * Update the provided instance out to the federation with a tag (which can be null).
	 * 
	 * @param instance the object instance
	 * @param tag the tag (can be null)
	 */
	protected void updateAttributeValues( HLAObject instance )
	{
		rtiamb.updateAttributeValues( instance, null, null );
	}
	
	/**
	 * Update the provided instance out to the federation with a tag (which can be null).
	 * 
	 * @param instance the object instance
	 * @param tag the tag (can be null)
	 */
	protected void updateAttributeValues( HLAObject instance, byte[] tag )
	{
		rtiamb.updateAttributeValues( instance, tag, null );
	}

	/**
	 * Update the provided instance out to the federation with a tag (which can be null) and
	 * time-stamp.
	 * 
	 * @param instance the object instance
	 * @param tag the tag (can be null)
	 * @param time the time-stamp
	 */
	protected void updateAttributeValues( HLAObject instance, byte[] tag, double time )
	{
		rtiamb.updateAttributeValues( instance, tag, time );
	}
	
	/**
	 * A utility method to encapsulate the code needed to convert a {@link AttributeHandleValueMap} into
	 * a populated map containing attribute names and their associated byte values 
	 * 
	 * The object instance will most likely have been created in the first place by using the
	 * {@link #makeObjectInstance(String)} or {@link #makeObjectInstance(String, Map)} method.
	 * 
	 * We can only delete objects we created, or for which we own the privilegeToDelete attribute.
	 * 
	 * @param instance the object instance
	 * @return the deleted instance
	 */
	protected HLAObject deleteObjectInstance( HLAObject instance )
	{
		return deleteObjectInstance( instance, null );
	}

	/**
	 * This method will attempt to delete (i.e., de-register) the object instance from the RTI.
	 * 
	 * The object instance will most likely have been created in the first place by using the
	 * {@link #makeObjectInstance(String)} or {@link #makeObjectInstance(String, Map)} method.
	 * 
	 * We can only delete objects we created, or for which we own the privilegeToDelete attribute.
	 * 
	 * @param instance the object instance
	 * @param tag the tag (can be null)
	 * @return the deleted instance
	 */
	protected HLAObject deleteObjectInstance( HLAObject instance, byte[] tag )
	{
		return rtiamb.deleteObjectInstance( instance, tag );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Connects to the RTI and then creates and joins the federation (as per the provided
	 * configuration)
	 */
	protected void createAndJoinFederation()
	{
		beforeFederationJoin();

		// no more configuration changes allowed
		configuration.freeze();
		
		rtiamb.connect( fedamb, configuration.callbacksAreEvoked() );
		
		createFederation();
		
		joinFederation();
	}

	/**
	 * Creates the federation if that is appropriate (as per the provided configuration)
	 */
	protected void createFederation()
	{
		if( !configuration.canCreateFederation() )
		{
			// this federate is not allowed to create federations - they must already
			// exist (i.e., have been created by something else)
			return;
		}
			
		String federationName = configuration.getFederationName();
		URL[] modules = configuration.getModules().toArray( new URL[0] );
		rtiamb.createFederationExecution( federationName, modules );
	}
	
	/**
	 * Joins the federation, retrying on failures to join (as per the provided configuration)
	 */
	protected void joinFederation()
	{
		String federationName = configuration.getFederationName();
		String federateName = configuration.getFederateName();
		String federateType = configuration.getFederateType();
		URL[] joinModules = configuration.getJoinModules().toArray( new URL[0] );
		
		int retryCount = 0;
		long retryInterval = configuration.getReconnectRetryInterval();
		int maxRetries = configuration.getMaxReconnectAttempts();
		boolean hasJoinedFederation = false;
		while( !hasJoinedFederation && retryCount < maxRetries )
		{
			try
			{
				if(retryCount > 0)
				{
					System.err.println( String.format( "Attempt %d of %d to join federation '%s'...", 
					                                   (retryCount+1), maxRetries, federationName ) );
				}
				
				rtiamb.joinFederationExecution( federateName, federateType, federationName, joinModules );
				hasJoinedFederation = true;
				
				System.out.println( String.format("Joined federation '%s'.", federationName ) );
			}
			catch(UCEFException e)
			{
				System.err.println( String.format( "Failed to join federation '%s'",
				                                   federationName ) );
				if( ++retryCount < maxRetries )
					System.err.println( String.format( "Retrying in %d second%s...",
					                                   retryInterval,
					                                   (retryInterval == 1 ? "" : "s") ) );
				delayFor( retryInterval );
			}
		}
		
		if( !hasJoinedFederation )
		{
			System.err.println( String.format( "Failed to join federation '%s' after %d attempt%s. "+
											   "Giving up.",
			                                   federationName,
			                                   maxRetries,
			                                   (maxRetries == 1 ? "" : "s") ) );
		}
	}
	
	/**
	 * Registers the specified synchronization point, and then waits for the federation to reach
	 * the same synchronization point
	 * 
	 * @param syncPoint the UCEF standard synchronization point
	 */
	protected void synchronize( UCEFSyncPoint syncPoint )
	{
		synchronize( syncPoint.getLabel() );
	}
	
	/**
	 * Registers the specified synchronization point, and then waits for the federation to reach
	 * the same synchronization point
	 * 
	 * @param label the identifier of the synchronization point to synchronize to
	 */
	protected void synchronize( String label )
	{
		// register the sync point
		registerSyncPointAndWaitForAnnounce( label, null );
		// achieve the sync point
		achieveSyncPointAndWaitForFederation( label );
	}

	/**
	 * Registers a synchronization point and waits for the announcement of that synchronization
	 * point
	 * 
	 * @param label the synchronization point label
	 * @param tag a tag to go along with the synchronization point registration (may be null)
	 */
	protected void registerSyncPointAndWaitForAnnounce( String label, byte[] tag )
	{
		// Note that if the point already been registered, there will be a callback saying this
		// failed, but as long as *someone* registered it everything is fine
		rtiamb.registerFederationSynchronizationPoint( label, tag );
		while( !fedamb.isAnnounced( label ) )
		{
			evokeMultipleCallbacks();
		}
	}

	/**
	 * Achieves a synchronization point and waits for the federation to achieve that
	 * synchronization point
	 * 
	 * @param label the synchronization point label
	 */
	protected void achieveSyncPointAndWaitForFederation( String label )
	{
		rtiamb.synchronizationPointAchieved( label );
		while( !fedamb.isAchieved( label ) )
		{
			evokeMultipleCallbacks();
		}
	}

	/**
	 * Advance time according to configuration
	 */
	protected void advanceTime()
	{
		// advance, or tick, or nothing!
		if( this.configuration.isTimeStepped() )
			advanceTime( fedamb.getFederateTime() + this.configuration.getLookAhead() );
		else if( this.configuration.callbacksAreEvoked() )
			evokeMultipleCallbacks();
		else
			;
	}
	
	/**
	 * Request a time advance and wait for the advancement
	 * 
	 * @param nextTime the time to advance to
	 */
	protected void advanceTime( double nextTime )
	{
		rtiamb.timeAdvanceRequest( nextTime );
		while( fedamb.getFederateTime() < nextTime )
		{
			evokeMultipleCallbacks();
		}
	}
	
	/**
	 * Utility function to avoid having this same code everywhere - this will likely change in the
	 * final implementation (i.e., probably not use the MIN_TIME/MAX_TIME parameters), so it will be
	 * much simpler to update if it's only in one place.
	 */
	protected void evokeMultipleCallbacks()
	{
		rtiamb.evokeMultipleCallbacks( MIN_TIME, MAX_TIME );
	}

	/**
	 * Resign from the federation and destroy the federation execution
	 */
	protected void resignAndDestroyFederation()
	{
		resignFromFederation( null );
		destroyFederation();
	}
	
	/**
	 * Resign from the federation
	 */
	protected void resignFromFederation( ResignAction resignAction )
	{
		if( resignAction == null )
			resignAction = ResignAction.DELETE_OBJECTS_THEN_DIVEST;
		
		rtiamb.resignFederationExecution( resignAction );
	}

	/**
	 * Destroy the federation execution
	 */
	protected void destroyFederation()
	{
		rtiamb.destroyFederationExecution( configuration.getFederationName() );
	}
	
	/**
	 * Enable the time policy settings
	 */
	protected void enableTimePolicy()
	{
		// enable time regulation based on configuration
		rtiamb.enableTimeRegulation( configuration.getLookAhead() );
		while( fedamb.isTimeRegulated() == false )
		{
			// waiting for callback to confirm it's enabled
			evokeMultipleCallbacks();
		}

		// enable time constrained
		rtiamb.enableTimeConstrained();
		while( fedamb.isTimeConstrained() == false )
		{
			// waiting for callback to confirm it's enabled
			evokeMultipleCallbacks();
		}
	}

	/**
	 * Disable the time policy settings
	 */
	protected void disableTimePolicy()
	{
		// no waiting for callbacks when disabling time policies
		rtiamb.disableTimeConstrained();
		fedamb.setTimeConstrained( false );

		rtiamb.disableTimeRegulation();
		fedamb.setTimeRegulated( false );
	}
	
	/**
	 * Publish and subscribe to all configured interactions and reflected attributes 
	 */
	protected void publishAndSubscribe()
	{
		Collection<ObjectClass> objectClasses = configuration.getPublishedAndSubscribedAttributes();
		for(ObjectClass objectClass : objectClasses)
		{
			if(objectClass.publish)
			{
				Set<String> attributes = objectClass.attributes.entrySet()
					.stream()
					.filter( x -> x.getValue().publish )
					.map( x -> x.getValue().name )
					.collect(Collectors.toSet());
				rtiamb.publishObjectClassAttributes( objectClass.name, attributes );
			}
			if(objectClass.subscribe)
			{
				Set<String> attributes = objectClass.attributes.entrySet()
					.stream()
					.filter( x -> x.getValue().subscribe )
					.map( x -> x.getValue().name )
					.collect(Collectors.toSet());
				rtiamb.subscribeObjectClassAttributes( objectClass.name, attributes );
			}
		}
		storeObjectClassData( objectClasses );
		
		Collection<InteractionClass> interactionClasses = configuration.getPublishedAndSubscribedInteractions();
		for(InteractionClass interactionClass : interactionClasses)
		{
			if(interactionClass.publish)
			{
				rtiamb.publishInteractionClass( interactionClass.name);
			}
			if(interactionClass.subscribe)
			{
				rtiamb.subscribeInteractionClass( interactionClass.name);
			}
		}
		storeInteractionClassData( interactionClasses );
	}

	/**
	 * Publish an interaction
	 * 
	 * @param className the name of the interaction
	 */
	protected void publishInteraction( String className )
	{
		rtiamb.publishInteractionClass( className );
	}
	
	/**
	 * Subscribe to an interaction
	 * 
	 * @param className the name of the interaction
	 */
	protected void subscribeInteraction( String className )
	{
		rtiamb.subscribeInteractionClass( className );
	}
	
	/**
	 * Publish an attribute reflection
	 * 
	 * @param className the name of the object to which the attributes belong
	 * @param attributes the names of the attributes to be published
	 */
	protected void publishAttributes( String className, Set<String> attributes )
	{
		rtiamb.publishObjectClassAttributes( className, attributes );
	}
	
	/**
	 * Subscribe to an attribute reflection
	 * 
	 * @param className the name of the object to which the attributes belong
	 * @param attributes the names of the attributes of interest
	 */
	protected void subscribeAttributes( String className, Set<String> attributes  )
	{
		rtiamb.subscribeObjectClassAttributes( className, attributes );
	}
	
	private void storeObjectClassData( Collection<Types.ObjectClass> objectClasses )
	{
		synchronized( mutex_lock )
		{
			for(Types.ObjectClass objectClass : objectClasses)
			{
				ObjectClassHandle handle = rtiamb.getObjectClassHandle( objectClass.name );
				objectClassByClassHandle.put( handle, objectClass );
			}
		}
	}

	private void storeInteractionClassData( Collection<Types.InteractionClass> interactionClasses )
	{
		synchronized( mutex_lock )
		{
			for( Types.InteractionClass interactionClass : interactionClasses )
			{
				InteractionClassHandle handle = rtiamb.getInteractionClassHandle( interactionClass.name );
				interactionClassByHandle.put( handle, interactionClass );
			}
		}
	}
	
	private void delayFor(long seconds)
	{
		try
		{
			Thread.sleep( seconds * 1000 );
		}
		catch( InterruptedException e1 )
		{
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

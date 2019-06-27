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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ResignAction;

/**
 * The purpose of this class is to provide a base level implementation of an HLA1516e federate, as
 * well as useful "boilerplate" implementations of common methods which most federates generally
 * implement anyway in order to be manageable.
 */
public abstract class FederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getLogger( FederateBase.class );

	private static final double MIN_TIME = 0.1;
	private static final double MAX_TIME = 0.2;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected FederateConfiguration configuration;

	protected RTIAmbassadorWrapper rtiamb;
	protected FederateAmbassador fedamb;

	protected LifecycleState lifecycleState;

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
		this.configuration = new FederateConfiguration();
		this.lifecycleState = LifecycleState.GESTATING;

		this.objectClassByClassHandle = new HashMap<>();
		this.objectClassByInstanceHandle = new HashMap<>();
		this.hlaObjectByInstanceHandle = new HashMap<>();
		this.interactionClassByHandle = new HashMap<>();
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
	 * Allows to access federate's configuration parameters.
	 *
	 * @return FederateConfiguration that allows to access configuration of this federate.
	 */
	public FederateConfiguration getFederateConfiguration()
	{
		return this.configuration;
	}

	/**
	 * Convenience wrapper to configure a federate from a JSON.
	 *
	 * Equivalent to calling {@link #getFederateConfiguration()}.fromJSON( configSource );
	 *
	 * Refer to {@link FederateConfiguration} for standard configuration JSON keys and data
	 * types.
	 *
	 * @param jsonSource the {@link String} containing either JSON configuration data, or the
	 *            path to a resource (i.e., a file) containing JSON configuration data.
	 * @return the extracted {@link JSONObject} containing the extracted configuration data. This
	 *         can be used for handling of "extra", federate specific custom configuration
	 *         parameters contained in the JSON.
	 */
	public JSONObject configureFromJSON( String jsonSource )
	{
		JSONObject json = this.configuration.fromJSON( jsonSource );
		// return the JSON object (potentially for others to use)
		return json;
	}

	/**
	 * This is the main method which carries out the life cycle of the federate
	 *
	 * @param configuration the configuration for the federate
	 */
	public void runFederate()
	{
		// - create and join the Federation, then
		// - publish and subscribe, and then
		// - call the beforeReadyToPopulate(), beforeReadyToRun() and
		//   beforeFirstStep() methods
		this.lifecycleState = LifecycleState.INITIALIZING;
		federateSetup();

		// - repeatedly call step() until simulation ends
		this.lifecycleState = LifecycleState.RUNNING;
		federateExecution();

		// - disable any time policy, then
		// - call readyToResign() and beforeExit(), and then
		// - resign and destroy the federation
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

		tickForCallBacks();
		beforeReadyToPopulate();
		synchronize( UCEFSyncPoint.READY_TO_POPULATE );

		tickForCallBacks();
		beforeReadyToRun();
		synchronize( UCEFSyncPoint.READY_TO_RUN );

		tickForCallBacks();
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
			if( step( this.fedamb.getFederateTime() ) == false )
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

		tickForCallBacks();
		beforeReadyToResign();

		if( this.configuration.shouldSyncBeforeResign() )
			synchronize( UCEFSyncPoint.READY_TO_RESIGN );

		tickForCallBacks();
		beforeExit();

		resignAndDestroyFederation();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// INCOMING INTERACTION/REFLECTION HANDLING /////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public void incomingObjectRegistration( ObjectInstanceHandle instanceHandle,
	                                        ObjectClassHandle classHandle )
	{
		synchronized( this.mutex_lock )
		{
			// just delegate to the default handler
			Types.ObjectClass objectClass = this.objectClassByClassHandle.get( classHandle );

			if( objectClass != null )
			{
				this.objectClassByInstanceHandle.put( instanceHandle, objectClass );

				HLAObject hlaObject = new HLAObject( objectClass.name, instanceHandle );
				this.hlaObjectByInstanceHandle.put( instanceHandle, hlaObject );

				receiveObjectRegistration( hlaObject );
			}
			else
			{
				logger.warn(
				             "Discovered unrecognized object instance {}",
				             this.rtiamb.makeSummary( instanceHandle ) );
			}
		}
	}

	public void incomingAttributeReflection( ObjectInstanceHandle handle, Map<String,byte[]> attributes )
	{
		synchronized( this.mutex_lock )
		{
			HLAObject hlaObject = this.hlaObjectByInstanceHandle.get( handle );

			if( hlaObject != null )
			{
				hlaObject.setState( attributes );
				receiveAttributeReflection( hlaObject );
			}
			else
			{
				logger.warn( "Ignoring attribute reflection received for undiscovered object instance {}",
				             this.rtiamb.makeSummary( handle ) );
			}
		}

		// just delegate to the default handler
	}

	public void incomingAttributeReflection( ObjectInstanceHandle handle, Map<String,byte[]> attributes, double time )
	{
		synchronized( this.mutex_lock )
		{
			HLAObject hlaObject = this.hlaObjectByInstanceHandle.get( handle );

			if( hlaObject != null )
			{
				hlaObject.setState( attributes );
				// just delegate to the default handler
				receiveAttributeReflection( hlaObject, time );
			}
			else
			{
				logger.warn( "Ignoring attribute reflection received for undiscovered object instance {}",
				             this.rtiamb.makeSummary( handle ) );
			}
		}
	}

	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters )
	{
		synchronized( this.mutex_lock )
		{
			HLAInteraction interaction = makeInteraction( handle, parameters );
			if( interaction != null )
			{
				// just delegate to the default handler
				receiveInteraction( interaction );
			}
			else
			{
				logger.warn( "Ignoring unexpected interaction: {}", this.rtiamb.makeSummary( handle ) );
			}
		}
	}

	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters, double time )
	{
		synchronized( this.mutex_lock )
		{
			HLAInteraction interaction = makeInteraction( handle, parameters );
			if( interaction != null )
			{
				// just delegate to the default handler
				receiveInteraction( interaction, time );
			}
			else
			{
				logger.warn( "Ignoring unexpected interaction: {}", this.rtiamb.makeSummary( handle ) );
			}
		}
	}

	public void incomingObjectDeleted( ObjectInstanceHandle handle )
	{
		synchronized( this.mutex_lock )
		{
			// clean up object maps as required
			Types.ObjectClass objectClass = this.objectClassByInstanceHandle.remove( handle );
			HLAObject hlaObject = this.hlaObjectByInstanceHandle.remove( handle );

			if( objectClass != null && hlaObject != null )
			{
				// just delegate to the default handler
				receiveObjectDeleted( hlaObject );
			}
			else
			{
				logger.warn( "Deletion notification received for previously undiscovered object instance {}",
				             this.rtiamb.makeSummary( handle ) );
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Utility Methods ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtain a known {@link InteractionClass} based on an {@link InteractionClassHandle}
	 *
	 * @param handle the {@link InteractionClassHandle}
	 * @return an {@link InteractionClass} instance if the {@link InteractionClassHandle} has been
	 *         previously cached, null otherwise
	 */
	protected InteractionClass interactionClassByHandle( InteractionClassHandle handle )
	{
		return this.interactionClassByHandle.get( handle );
	}

	/**
	 * Obtain a known {@link ObjectClass} based on an {@link ObjectClassHandle}
	 *
	 * @param handle the {@link ObjectClassHandle}
	 * @return an {@link ObjectClass} instance if the {@link InteractionClassHandle} has been
	 *         previously cached, null otherwise
	 */
	protected ObjectClass objectClassByClassHandle( ObjectClassHandle handle )
	{
		return this.objectClassByClassHandle.get( handle );
	}

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
	 * Create an interaction
	 *
	 * @param className the name of the interaction class
	 * @param parameters the parameters for the interaction (may be an empty map or null)
	 * @return the interaction
	 */
	protected HLAInteraction makeInteraction( String className, Map<String, byte[]> parameters)
	{
		return this.rtiamb.makeInteraction( className, parameters );
	}

	/**
	 * Create an interaction (with no parameter values)
	 *
	 * @param className the name of the interaction class
	 * @return the interaction
	 */
	protected HLAInteraction makeInteraction( InteractionClassHandle handle )
	{
		return makeInteraction( handle, null );
	}

	/**
	 * Create an interaction
	 *
	 * @param className the name of the interaction class
	 * @param parameters the parameters for the interaction (may be an empty map or null)
	 * @return the interaction
	 */
	protected HLAInteraction makeInteraction( InteractionClassHandle handle, Map<String, byte[]> parameters)
	{
		Types.InteractionClass interactionClass;
		synchronized( this.mutex_lock )
		{
			interactionClass = interactionClassByHandle( handle );
		}

		if( interactionClass == null )
			return null;

		return makeInteraction( interactionClass.name, parameters );
	}

	/**
	 * Publish the provided interaction to the federation
	 *
	 * @param interaction the interaction
	 */
	protected void sendInteraction( HLAInteraction interaction )
	{
		this.rtiamb.sendInteraction( interaction, null, null );
	}

	/**
	 * Publish the provided interaction to the federation with a tag (which can be null).
	 *
	 * @param interaction the interaction
	 * @param tag the tag (can be null)
	 */
	protected void sendInteraction( HLAInteraction interaction, byte[] tag )
	{
		this.rtiamb.sendInteraction( interaction, tag, null );
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
		this.rtiamb.sendInteraction( interaction, tag, time );
	}

	/**
	 * Create an object instance (with no parameter values), also registering the instance with the RTI.
	 *
	 * @param className the name of the object class
	 * @return the object instance
	 */
	protected HLAObject makeObjectInstance( String className )
	{
		return makeObjectInstance( className, null );
	}

	/**
	 * Create an object instance, also registering the instance with the RTI.
	 *
	 * @param className the name of the object class
	 * @param attributes the attributes for the object instance (may be an empty map or null)
	 * @return the object instance
	 */
	protected HLAObject makeObjectInstance( String className, Map<String, byte[]> attributes)
	{
		return this.rtiamb.makeObjectInstance( className, attributes );
	}

	/**
	 * Create an object instance (with no parameter values), also registering the instance with the RTI.
	 *
	 * @param handle the handle of the object class
	 * @return the object instance, or null if the handle does not correspond to a previously
	 *         registered/cached object class
	 */
	protected HLAObject makeObjectInstance( ObjectClassHandle handle )
	{
		return makeObjectInstance( handle, null );
	}

	/**
	 * Create an object instance, also registering the instance with the RTI.
	 *
	 * @param handle the handle of the object class
	 * @param attributes the attributes for the object instance (may be an empty map or null)
	 * @return the object instance, or null if the handle does not correspond to a previously
	 *         registered/cached object class
	 */
	protected HLAObject makeObjectInstance( ObjectClassHandle handle, Map<String, byte[]> attributes)
	{
		Types.ObjectClass objectClass;
		synchronized( this.mutex_lock )
		{
			objectClass = objectClassByClassHandle( handle );
		}

		if( objectClass == null )
			return null;

		return makeObjectInstance( objectClass.name, attributes );
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
		this.rtiamb.updateAttributeValues( instance, null, null );
	}

	/**
	 * Update the provided instance out to the federation with a tag (which can be null).
	 *
	 * @param instance the object instance
	 * @param tag the tag (can be null)
	 */
	protected void updateAttributeValues( HLAObject instance, byte[] tag )
	{
		this.rtiamb.updateAttributeValues( instance, tag, null );
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
		this.rtiamb.updateAttributeValues( instance, tag, time );
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
		return this.rtiamb.deleteObjectInstance( instance, tag );
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

		this.rtiamb.connect( this.fedamb, this.configuration.callbacksAreImmediate() );

		logger.info( "Federate {} connected to RTI.", this.configuration.getFederateName() );

		createFederation();

		joinFederation();
	}

	/**
	 * Creates the federation if that is appropriate (as per the provided configuration)
	 */
	protected void createFederation()
	{
		String federationName = this.configuration.getFederationName();

		if( !this.configuration.canCreateFederation() )
		{
			// this federate is not allowed to create federations - they must already
			// exist (i.e., have been created by something else)
			logger.info( "No permission to create federation {} - skipping creation attempt....", federationName );
			return;
		}

		URL[] modules = this.configuration.getModules().toArray( new URL[0] );
		this.rtiamb.createFederationExecution( federationName, modules );

		logger.info( "Federation {} created.", federationName );
	}

	/**
	 * Joins the federation, retrying on failures to join (as per the provided configuration)
	 */
	protected void joinFederation()
	{
		String federationName = this.configuration.getFederationName();
		String federateName = this.configuration.getFederateName();
		String federateType = this.configuration.getFederateType();
		URL[] joinModules = this.configuration.getJoinModules().toArray( new URL[0] );

		int retryCount = 0;
		long retryInterval = this.configuration.getJoinRetryInterval();
		int maxRetries = this.configuration.getMaxJoinAttempts();
		boolean hasJoinedFederation = false;
		while( !hasJoinedFederation && retryCount < maxRetries )
		{
			try
			{
				if( retryCount > 0 )
				{
					logger.warn( "Attempt {} of {} to join federation '{}'...",
					             (retryCount+1), maxRetries, federationName );
				}

				this.rtiamb.joinFederationExecution( federateName, federateType, federationName, joinModules );
				hasJoinedFederation = true;

				logger.info( "Joined federation '{}'.", federationName );
			}
			catch( UCEFException e )
			{
				logger.warn( "Failed to join federation '{}'", federationName );
				if(logger.isDebugEnabled())
				{
					logger.debug( e.getLocalizedMessage() );
					e.printStackTrace();
				}
				if( ++retryCount < maxRetries )
					logger.warn( "Retrying in {} second{}...",
                                 retryInterval,
                                 (retryInterval == 1 ? "" : "s") );
				delayFor( retryInterval * 1000 );
			}
		}

		if( !hasJoinedFederation )
		{
			logger.error( "Failed to join federation '{}' after {} attempt{}. Giving up.",
                          federationName,
                          maxRetries,
                          (maxRetries == 1 ? "" : "s")  );
			// if we can't join the federation, there is no point in
			// continuing at all - just exit now with non-zero exit code
			System.exit( 1 );
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
		registerSyncPoint( syncPoint.getLabel(), null );
		// automatically achieve the sync point
		achieveSyncPoint( syncPoint.getLabel() );
		waitForSyncPointAchievement( syncPoint.getLabel() );
	}

	/**
	 * Registers a synchronization point
	 *
	 * @param label the synchronization point label
	 * @param tag a tag to go along with the synchronization point registration (may be null)
	 */
	protected void registerSyncPoint( String label, byte[] tag )
	{
		// Note that if the point already been registered, there will be a callback saying this
		// failed, but as long as *someone* registered it everything is fine
		this.rtiamb.registerFederationSynchronizationPoint( label, tag );
	}

	/**
	 * Waits for the announcement of a synchronization point
	 *
	 * @param label the synchronization point label
	 */
	protected void waitForSyncPointAnnouncement( String label )
	{
		while( !this.fedamb.isAnnounced( label ) )
		{
			tickForCallBacks();
		}
	}

	/**
	 * Check if a synchronization point has been announced
	 *
	 * @param label the synchronization point label
	 */
	protected boolean isAnnounced( String label )
	{
		return this.fedamb.isAnnounced( label );
	}

	/**
	 * Achieves a synchronization point
	 *
	 * @param label the synchronization point label
	 */
	protected void achieveSyncPoint( String label )
	{
		this.rtiamb.synchronizationPointAchieved( label );
	}

	/**
	 * Waits for the federation to achieve the synchronization point
	 *
	 * @param label the synchronization point label
	 */
	protected void waitForSyncPointAchievement( String label )
	{
		while( !this.fedamb.isAchieved( label ) )
		{
			tickForCallBacks();
		}
	}

	/**
	 * Check if a synchronization point has been achieved yet
	 *
	 * @param label the synchronization point label
	 */
	protected void isAchieved( String label )
	{
		this.fedamb.isAchieved( label );
	}

	/**
	 * Advance time according to configuration
	 */
	protected void advanceTime()
	{
		double nextTime = this.fedamb.getFederateTime() + this.configuration.getLookAhead();
		this.rtiamb.timeAdvanceRequest( nextTime );
		while( this.fedamb.getFederateTime() < nextTime )
		{
			tickForCallBacks();
		}
	}

	/**
	 * Utility function to avoid having this same code everywhere - this will likely change in the
	 * final implementation (i.e., probably not use the MIN_TIME/MAX_TIME parameters), so it will be
	 * much simpler to update if it's only in one place.
	 */
	protected void evokeMultipleCallbacks()
	{
		this.rtiamb.evokeMultipleCallbacks( MIN_TIME, MAX_TIME );
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

		this.rtiamb.resignFederationExecution( resignAction );
	}

	/**
	 * Destroy the federation execution
	 */
	protected void destroyFederation()
	{
		this.rtiamb.destroyFederationExecution( this.configuration.getFederationName() );
	}

	/**
	 * Enable the time policy settings
	 */
	protected void enableTimePolicy()
	{
		// enable time regulation based on configuration
		if( this.configuration.isTimeRegulated() )
		{
			this.rtiamb.enableTimeRegulation( this.configuration.getLookAhead() );
			while( this.fedamb.isTimeRegulated() == false )
			{
				// waiting for callback to confirm it's enabled
				tickForCallBacks();
			}
		}

		// enable time constrained based on configuration
		if( this.configuration.isTimeConstrained() )
		{
			this.rtiamb.enableTimeConstrained();
			while( this.fedamb.isTimeConstrained() == false )
			{
				// waiting for callback to confirm it's enabled
				tickForCallBacks();
			}
		}
	}

	/**
	 * Disable the time policy settings
	 */
	protected void disableTimePolicy()
	{
		// no waiting for callbacks when disabling time policies
		this.rtiamb.disableTimeConstrained();
		this.fedamb.setTimeConstrained( false );

		this.rtiamb.disableTimeRegulation();
		this.fedamb.setTimeRegulated( false );
	}

	/**
	 * Publish and subscribe to all configured interactions and reflected attributes
	 */
	protected void publishAndSubscribe()
	{
		Collection<ObjectClass> objectClasses = this.configuration.getPublishedAndSubscribedObjectClasses();
		for( ObjectClass objectClass : objectClasses )
		{
			if( objectClass.isPublished() )
			{
				Set<String> attributes = objectClass.attributes.entrySet()
					.stream()
					.filter( x -> x.getValue().isPublished() )
					.map( x -> x.getValue().name )
					.collect(Collectors.toSet());
				this.rtiamb.publishObjectClassAttributes( objectClass.name, attributes );
			}
			if(objectClass.isSubscribed())
			{
				Set<String> attributes = objectClass.attributes.entrySet()
					.stream()
					.filter( x -> x.getValue().isSubscribed() )
					.map( x -> x.getValue().name )
					.collect(Collectors.toSet());
				this.rtiamb.subscribeObjectClassAttributes( objectClass.name, attributes );
			}
		}
		storeObjectClassData( objectClasses );

		Collection<InteractionClass> interactionClasses = this.configuration.getPublishedAndSubscribedInteractions();
		// Collection<InteractionClass> interactionClasses = SOMParser.getInteractionClasses(configuration.getSomPaths());

		for( InteractionClass interactionClass : interactionClasses )
		{
			if( interactionClass.isPublished() )
			{
				this.rtiamb.publishInteractionClass( interactionClass.name );
			}
			if( interactionClass.isSubscribed() )
			{
				this.rtiamb.subscribeInteractionClass( interactionClass.name );
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
		this.rtiamb.publishInteractionClass( className );
	}

	/**
	 * Subscribe to an interaction
	 *
	 * @param className the name of the interaction
	 */
	protected void subscribeInteraction( String className )
	{
		this.rtiamb.subscribeInteractionClass( className );
	}

	/**
	 * Publish an attribute reflection
	 *
	 * @param className the name of the object to which the attributes belong
	 * @param attributes the names of the attributes to be published
	 */
	protected void publishAttributes( String className, Set<String> attributes )
	{
		this.rtiamb.publishObjectClassAttributes( className, attributes );
	}

	/**
	 * Subscribe to an attribute reflection
	 *
	 * @param className the name of the object to which the attributes belong
	 * @param attributes the names of the attributes of interest
	 */
	protected void subscribeAttributes( String className, Set<String> attributes  )
	{
		this.rtiamb.subscribeObjectClassAttributes( className, attributes );
	}

	private void storeObjectClassData( Collection<Types.ObjectClass> objectClasses )
	{
		synchronized( this.mutex_lock )
		{
			for(Types.ObjectClass objectClass : objectClasses)
			{
				ObjectClassHandle handle = this.rtiamb.getObjectClassHandle( objectClass.name );
				this.objectClassByClassHandle.put( handle, objectClass );
			}
		}
	}

	private void storeInteractionClassData( Collection<Types.InteractionClass> interactionClasses )
	{
		synchronized( this.mutex_lock )
		{
			for( Types.InteractionClass interactionClass : interactionClasses )
			{
				InteractionClassHandle handle = this.rtiamb.getInteractionClassHandle( interactionClass.name );
				this.interactionClassByHandle.put( handle, interactionClass );
			}
		}
	}

	private void tickForCallBacks()
	{
		if( this.configuration.callbacksAreImmediate() )
			delayFor(1);
		else
			evokeMultipleCallbacks();
	}

	private void delayFor(long milliseconds)
	{
		try
		{
			Thread.sleep( milliseconds );
		}
		catch( InterruptedException e )
		{
			// ignore
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

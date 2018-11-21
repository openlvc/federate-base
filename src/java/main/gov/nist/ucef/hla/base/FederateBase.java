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
import java.util.Map;
import java.util.Set;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.ResignAction;
import hla.rti1516e.encoding.EncoderFactory;

public abstract class FederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final double MIN_TIME = 0.1;
	private static final double MAX_TIME = 0.2;
	
	protected static final String JOINED_FEDERATION_INTERACTION = "HLAinteractionRoot.ManagedFederation.Federate.Joined";
	protected static final String RESIGNED_FEDERATION_INTERACTION = "HLAinteractionRoot.ManagedFederation.Federate.Resigned";	
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected FederateConfiguration configuration;
	
	protected RTIAmbassadorWrapper rtiamb;
	protected FederateAmbassador fedamb;
	
	private FederateHandle federateHandle;
	
	private EncoderFactory encoder = HLACodecUtils.getEncoder();
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected FederateBase()
	{
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

		this.rtiamb = new RTIAmbassadorWrapper();
		this.fedamb = new FederateAmbassador( this );

		createAndJoinFederation();
		enableTimePolicy();
		
		publishAndSubscribe();

		beforeReadyToPopulate();
		synchronize( UCEFSyncPoint.READY_TO_POPULATE );

		beforeReadyToRun();
		synchronize( UCEFSyncPoint.READY_TO_RUN );
		
		beforeFirstStep();

		double currentTime = 0.0;
		double timeStep = configuration.getLookAhead();
		while( true )
		{
			currentTime = fedamb.getFederateTime();

			// next step
			if( step( currentTime ) == false )
				break;

			// advance, or tick, or nothing!
			if( configuration.isTimeStepped() )
				advanceTime( currentTime + timeStep );
			else if( configuration.callbacksAreEvoked() )
				evokeMultipleCallbacks();
			else
				;
		}

		disableTimePolicy();

		beforeReadyToResign();
		synchronize( UCEFSyncPoint.READY_TO_RESIGN );
		beforeExit();

		resignAndDestroyFederation();
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
	 * Create an object instance (with no initial values for the attributes), also registering the
	 * instance with the RTI.
	 * 
	 * Refer to the {@link #deleteObjectInstance(HLAObject)} and
	 * {@link #deleteObjectInstance(HLAObject, byte[])} for the symmetrical deletion methods.
	 * 
	 * @param className the name of the object class
	 * @return the object instance
	 */
	protected HLAObject makeObjectInstance( String className )
	{
		return rtiamb.makeObjectInstance( className );
	}
	
	/**
	 * Create an object instance with initial values for the attributes, also registering the
	 * instance with the RTI.
	 * 
	 * Refer to the {@link #deleteObjectInstance(HLAObject)} and
	 * {@link #deleteObjectInstance(HLAObject, byte[])} for the symmetrical deletion methods.
	 * 
	 * @param className the name of the object class
	 * @param initialValues the initial values for the object's attributes (may be an empty map or
	 *            null)
	 * @return the object instance
	 */
	protected HLAObject makeObjectInstance( String className, Map<String, byte[]> initialValues)
	{
		return rtiamb.makeObjectInstance( className, initialValues );
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
	 * A utility method to allow simple instantiation of an interaction based off an interaction
	 * name and some parameters
	 * 
	 * @param name the interaction class name
	 * @param parameters the parameters (can be null)
	 * @return the interaction
	 */
	protected HLAInteraction makeFederationJoinedInteraction()
	{
		Map<String, byte[]> parameters = new HashMap<>();
		parameters.put( "FederateType", HLACodecUtils.encode( encoder, configuration.getFederateType() ));
		parameters.put( "FederateHandle", HLACodecUtils.encode( encoder, federateHandle.hashCode() ));
		return makeInteraction( JOINED_FEDERATION_INTERACTION, parameters );
	}
	
	/**
	 * A utility method to allow simple instantiation of an interaction based off an interaction
	 * name and some parameters
	 * 
	 * @param name the interaction class name
	 * @param parameters the parameters (can be null)
	 * @return the interaction
	 */
	protected HLAInteraction makeFederationResignedInteraction()
	{
		Map<String, byte[]> parameters = new HashMap<>();
		parameters.put( "FederateHandle", HLACodecUtils.encode( encoder, federateHandle.hashCode() ));
		return makeInteraction( RESIGNED_FEDERATION_INTERACTION, parameters );
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
	private void createAndJoinFederation()
	{
		rtiamb.connect( fedamb );
		rtiamb.createFederation( configuration );

		beforeFederationJoin();
		federateHandle = rtiamb.joinFederation( configuration );
	}

	/**
	 * Registers the specified synchronization point, and then waits for the federation to reach
	 * the same synchronization point
	 * 
	 * @param syncPoint the UCEF standard synchronization point
	 */
	private void synchronize( UCEFSyncPoint syncPoint )
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
	 * Request a time advance and wait for the advancement
	 * 
	 * @param nextTime the time to advance to
	 */
	private void advanceTime( double nextTime )
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
	private void evokeMultipleCallbacks()
	{
		rtiamb.evokeMultipleCallbacks( MIN_TIME, MAX_TIME );
	}

	/**
	 * Resign from the federation and destroy the federation execution
	 */
	private void resignAndDestroyFederation()
	{
		resignFromFederation( null );
		destroyFederation();
	}
	
	/**
	 * Resign from the federation
	 */
	private void resignFromFederation( ResignAction resignAction )
	{
		if( resignAction == null )
			resignAction = ResignAction.DELETE_OBJECTS_THEN_DIVEST;
		
		rtiamb.resignFederationExecution( resignAction );
	}

	/**
	 * Destroy the federation execution
	 */
	private void destroyFederation()
	{
		rtiamb.destroyFederationExecution( configuration );
	}
	
	/**
	 * Enable the time policy settings
	 */
	private void enableTimePolicy()
	{
		// enable time regulation based on configuration
		rtiamb.enableTimeRegulation( configuration.getLookAhead() );
		while( fedamb.isTimeRegulating == false )
		{
			// waiting for callback to confirm it's enabled
			evokeMultipleCallbacks();
		}

		// enable time constrained
		rtiamb.enableTimeConstrained();
		while( fedamb.isTimeConstrained == false )
		{
			// waiting for callback to confirm it's enabled
			evokeMultipleCallbacks();
		}
	}

	/**
	 * Disable the time policy settings
	 */
	private void disableTimePolicy()
	{
		// no waiting for callbacks when disabling time policies
		rtiamb.disableTimeConstrained();
		fedamb.isTimeConstrained = false;
		
		rtiamb.disableTimeRegulation();
		fedamb.isTimeRegulating = false;
	}
	
	/**
	 * Publish and subscribe to all configured interactions and reflected attributes 
	 */
	private void publishAndSubscribe()
	{
		rtiamb.publishObjectClassAttributes( configuration.getPublishedAttributes() );
		rtiamb.publishInteractionClasses( configuration.getPublishedInteractions() );
		
		rtiamb.subscribeObjectClassAttributes( configuration.getSubscribedAttributes() );
		rtiamb.subscribeInteractionClasses( configuration.getSubscribedInteractions() );
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
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

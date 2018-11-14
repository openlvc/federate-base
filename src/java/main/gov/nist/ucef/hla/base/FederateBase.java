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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
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
	private static final byte[] EMPTY_BYTE_ARRAY = {};
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected FederateConfiguration configuration;
	
	protected RTIAmbassadorWrapper rtiamb;
	protected FederateAmbassador fedamb;
	
	protected Set<HLAObject> registeredObjects;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected FederateBase()
	{
		registeredObjects = new HashSet<>();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Lifecycle Callback Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public abstract void beforeFederationJoin();
	public abstract void beforeReadyToPopulate();
	public abstract void beforeReadyToRun();
	public abstract void beforeFirstStep();
	public abstract boolean step( double currentTime ); // == 0
	public abstract void beforeReadyToResign();
	public abstract void beforeExit();
	
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
		this.configuration = configuration;

		this.rtiamb = new RTIAmbassadorWrapper();
		this.fedamb = new FederateAmbassador( this );

		createAndJoinFederation();
		enableTimePolicy();
		publishAndSubscribe();

		beforeReadyToPopulate();
		synchronize( UCEFSyncPoint.READY_TO_POPULATE.getLabel() );

		registerObjects();

		beforeReadyToRun();
		synchronize( UCEFSyncPoint.READY_TO_RUN.getLabel() );
		
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

		// time to run away
		beforeReadyToResign();
		synchronize( UCEFSyncPoint.READY_TO_RESIGN.getLabel() );
		beforeExit();

		deregisterObjects();
		resignAndDestroyFederation();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	protected HLAInteraction makeInteraction( String name, HashMap<String,byte[]> parameters )
	{
		return new HLAInteraction( rtiamb.getInteractionClassHandle( name ), parameters );
	}

	/**
	 * Publish the provided interaction to the federation with a tag (which can be null).
	 * 
	 * @param tag the tag (can be null)
	 */
	protected void send( HLAInteraction interaction, byte[] tag )
	{
		rtiamb.sendInteraction( interaction, tag, null );
	}

	/**
	 * Publish the provided interaction to the federation with a tag (which can be null) and
	 * time-stamp.
	 * 
	 * @param tag the tag (can be null)
	 * @param time the time-stamp
	 */
	protected void send( HLAInteraction interaction, byte[] tag, double time )
	{
		rtiamb.sendInteraction( interaction, tag, time );
	}

	/**
	 * Update the provided instance out to the federation with a tag (which can be null).
	 * 
	 * @param tag the tag (can be null)
	 */
	protected void update( HLAObject instance, byte[] tag )
	{
		rtiamb.updateAttributeValues( instance, tag, null );
	}

	/**
	 * Update the provided instance out to the federation with a tag (which can be null) and
	 * time-stamp.
	 * 
	 * @param tag the tag (can be null)
	 * @param time the time-stamp
	 */
	protected void update( HLAObject instance, byte[] tag, double time )
	{
		rtiamb.updateAttributeValues( instance, tag, time );
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
		rtiamb.joinFederation( configuration );
	}

	/**
	 * Registers the specified synchronization point, and then waits for the federation to reach
	 * the same synchronization point
	 * 
	 * @param label the identifier of the synchronization point to synchronize to
	 */
	private void synchronize( String label )
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
	private void registerSyncPointAndWaitForAnnounce( String label, byte[] tag )
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
	private void achieveSyncPointAndWaitForFederation( String label )
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

	private void resignAndDestroyFederation()
	{
		resignFromFederation( null );
		destroyFederation();
	}
	
	private void resignFromFederation( ResignAction resignAction )
	{
		if( resignAction == null )
			resignAction = ResignAction.DELETE_OBJECTS;
		
		rtiamb.resignFederationExecution( resignAction );
	}

	private void destroyFederation()
	{
		rtiamb.destroyFederationExecution( configuration );
	}
	
	private void enableTimePolicy()
	{
		// enable time regulation based on configuration
		rtiamb.enableTimeRegulation( configuration.getLookAhead() );
		while( fedamb.isTimeRegulating() == false )
		{
			evokeMultipleCallbacks();
		}

		// enable time constrained
		rtiamb.enableTimeConstrained();
		while( fedamb.isTimeConstrained() == false )
		{
			evokeMultipleCallbacks();
		}
	}

	private void publishAndSubscribe()
	{
		publishObjectClassAttributes( configuration.getPublishedAttributes() );
		subscribeObjectClassesAttributes( configuration.getSubscribedAttributes() );
		publishInteractionClasses( configuration.getPublishedInteractions() );
		subscribeInteractionClasses( configuration.getSubscribedInteractions() );
	}

	private void registerObjects()
	{
		// TODO this is just placeholder code which simply registers an instance for every 
		//      published attribute entry in the configuration. It's possible that the
		//      implementation might require single or multiple instances (or none, though
		//      that would be pointless) on a per entry basis.
		for(Entry<String,Set<String>> x : configuration.getPublishedAttributes().entrySet())
		{
			ObjectClassHandle classhandle = rtiamb.getObjectClassHandle( x.getKey() );
			ObjectInstanceHandle instanceHandle = rtiamb.registerObjectInstance( classhandle );
			
			Map<String, byte[]> attributes = new HashMap<>();
			for(String attributeName : x.getValue())
			{
				attributes.put(attributeName, EMPTY_BYTE_ARRAY);
			}
			
			registeredObjects.add( new HLAObject( instanceHandle, attributes ) );
		}
	}
	
	private void deregisterObjects()
	{
		Set<HLAObject> deleted = new HashSet<>();
		for( HLAObject obj : this.registeredObjects )
		{
			rtiamb.deleteObjectInstance( obj.getInstanceHandle(), null );
			deleted.add( obj );
		}
		this.registeredObjects.removeAll( deleted );
	}

	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * publishing to the federation.
	 */
	private void publishInteractionClasses( Collection<String> interactionIDs)
	{
		if( interactionIDs == null || interactionIDs.isEmpty() )
			return;

		for( String interactionID : interactionIDs )
			publishInteractionClass( interactionID );
	}

	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * publishing to the federation.
	 */
	private void publishInteractionClass( String className )
	{
		InteractionClassHandle handle = rtiamb.getInteractionClassHandle( className );
		publishInteractionClass( handle );
	}
	
	/**
	 * This method will inform the RTI about the an interaction that a federate will be publishing to 
	 * the federation.
	 */
	private void publishInteractionClass(InteractionClassHandle handle)
	{
		if( handle == null )
			throw new UCEFException( "NULL interaction class handle. Cannot publish interaction class." );

		try
		{
			rtiamb.publishInteractionClass( handle );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to publish interaction class with handle %s", 
			                         assembleInteractionClassDetails( handle ) );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and to which classes they belong.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values 
	 */
	private void publishObjectClassAttributes( Map<String,Set<String>> publishedAttributes)
	{
		if(publishedAttributes == null || publishedAttributes.isEmpty())
			return;

		for( Entry<String,Set<String>> publication : publishedAttributes.entrySet() )
		{
			String className = publication.getKey();
			ObjectClassHandle classHandle = rtiamb.getObjectClassHandle( className );
			if( classHandle == null )
			{
				throw new UCEFException( "Unknown object class name '%s'. Cannot publish attributes.",
				                         className );
			}
			
			AttributeHandleSet attributeHandleSet = rtiamb.makeAttributeHandleSet();
			for( String attributeName : publication.getValue() )
			{
				AttributeHandle attributeHandle = rtiamb.getAttributeHandle( classHandle, attributeName );
				if( classHandle == null )
				{
					throw new UCEFException( "Unknown attribute name '%s'. Cannot publish attributes " +
											 "for object class %s.",
					                         attributeName , assembleObjectClassDetails( classHandle ) );
				}
				attributeHandleSet.add( attributeHandle );
			}
			
			publishObjectClassAttributes( classHandle, attributeHandleSet );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and to which classes they belong.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values 
	 */
	private void publishObjectClassAttributes(ObjectClassHandle handle, AttributeHandleSet attributes)
	{
		if( handle == null )
			throw new UCEFException( "NULL object class handle. Cannot publish object class atributes." );

		if( attributes == null )
			throw new UCEFException( "NULL attribute handle set. Cannot publish attributes for object " +
									 "class %s.",
			                         assembleObjectClassDetails( handle ) );
			
		try
		{
			rtiamb.publishObjectClassAttributes( handle, attributes );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to publish object class atributes for object class %s.",
			                         assembleObjectClassDetails( handle ) );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * interested in hearing about as other federates produce them.
	 */
	private void subscribeInteractionClasses( Collection<String> interactionClassNames )
	{
		if( interactionClassNames == null || interactionClassNames.isEmpty() )
			return;

		for( String interactionClassName : interactionClassNames )
		{
			subscribeInteractionClass( interactionClassName );
		}
	}
	
	/**
	 * This method will inform the RTI about a class of interaction that a federate will subscribe to 
	 * 
	 * @param className the name of the interaction class to subscribe to
	 */
	private void subscribeInteractionClass( String className )
	{
		InteractionClassHandle handle = rtiamb.getInteractionClassHandle( className );

		if( handle == null )
		{
			throw new UCEFException( "Cannot subscribe to interaction class using unknown class " +
									 "name '%s'.",
			                         className );
		}

		subscribeInteractionClass( handle );
	}
	
	/**
	 * This method will inform the RTI about a class of interaction that a federate will subscribe to 
	 * 
	 * @param handle the handle of the interaction class to subscribe to
	 */
	private void subscribeInteractionClass( InteractionClassHandle handle )
	{
		if( handle == null )
			throw new UCEFException( "NULL interaction class handle. Cannot subscribe to " +
									 "interaction class." );

		try
		{
			rtiamb.subscribeInteractionClass( handle );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to subscribe to interaction class %s",
			                         assembleInteractionClassDetails( handle ) );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * interested in hearing about, and to which classes they belong.
	 * 
	 * We need to subscribe to hear about information on attributes of classes created and altered
	 * in other federates
	 * 
	 * @param subscribedAttributes a map which connects object class names to sets of attribute
	 *            names to be subscribed to
	 */
	private void subscribeObjectClassesAttributes( Map<String,Set<String>> subscribedAttributes)
	{
		if(subscribedAttributes == null || subscribedAttributes.isEmpty())
			return;

		for( Entry<String,Set<String>> subscription : subscribedAttributes.entrySet() )
		{
			// get all the handle information for the attributes of the current class
			String className = subscription.getKey();
			ObjectClassHandle classHandle = rtiamb.getObjectClassHandle( className );
			if( classHandle == null )
			{
				throw new UCEFException( "Unknown object class name '%s'. Cannot subscribe to attributes.",
				                         className );
			}
			// package the information into the handle set
			AttributeHandleSet attributeHandleSet = rtiamb.makeAttributeHandleSet();
			for( String attributeName : subscription.getValue() )
			{
				AttributeHandle attributeHandle = rtiamb.getAttributeHandle( classHandle, attributeName );
				if( attributeHandle == null )
				{
					throw new UCEFException( "Unknown attribute name '%s'. Cannot subscribe to " +
											 "attributes for object class %s.",
					                         attributeName , assembleObjectClassDetails( classHandle ));
				}
				attributeHandleSet.add( attributeHandle );
			}
			
			// do the actual subscription
			subscribeObjectClassAttributes( classHandle, attributeHandleSet );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and which class they are associated with.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values
	 * 
	 * @param handle the handle for the object class whose attributes are to be subscribed to
	 * @param attributes the attribute handles identifying the attributes to be subscribed to
	 */
	private void subscribeObjectClassAttributes(ObjectClassHandle handle, AttributeHandleSet attributes)
	{
		if( handle == null )
			throw new UCEFException( "NULL object class handle. Cannot subscribe to attributes." );

		if( attributes == null )
			throw new UCEFException( "NULL attribute handle set . Cannot subscribe to attributes for " +
									 "object class %s.",
			                         assembleObjectClassDetails( handle ) );

		try
		{
			rtiamb.subscribeObjectClassAttributes( handle, attributes );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to subscribe to object class attributes for object class %s.",
			                         assembleObjectClassDetails( handle ) );
		}
	}
	
	/**
	 * This is a utility method simply to construct a human readable description of an object
	 * class's details for the purposes populating exception text
	 * 
	 * @param handle the object class handle
	 * @return the descriptive text
	 */
	private String assembleObjectClassDetails(ObjectClassHandle handle)
	{
		String className = rtiamb.getObjectClassName( handle );
		
		StringBuilder details = new StringBuilder( "'" + className + "'" );
		details.append( " (handle'" + className + "')" );
		
		return details.toString();
	}
	
	/**
	 * This is a utility method simply to construct a human readable description of an interaction
	 * class's details for the purposes populating exception text
	 * 
	 * @param handle the interaction class handle
	 * @return the descriptive text
	 */
	private String assembleInteractionClassDetails(InteractionClassHandle handle)
	{
		String className = rtiamb.getInteractionClassName( handle );
		
		StringBuilder details = new StringBuilder( "'" + className + "'" );
		details.append( " (handle'" + className + "')" );
		
		return details.toString();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

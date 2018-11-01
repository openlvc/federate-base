package gov.nist.ucef.hla.base;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import old.common.FederateAmbassadorBase;
import old.common.UCEFException;

public abstract class FederateBase
{
	private static final double MIN_TIME = 0.1;
	private static final double MAX_TIME = 0.2;
	
	protected String federateName;
	protected String federateType;
	protected FederateConfiguration configuration;
	protected double currentTime;
	protected TimeStatus currentTimeStatus; 
	
	protected RTIAmbassadorWrapper rtiamb;
	protected FederateAmbassador fedamb;
	
	protected FederateBase()
	{
	}
	
	///////////////////////////////////////////////////////////////////
	/// Lifecycle and Callback Methods       //////////////////////////
	///////////////////////////////////////////////////////////////////
	public abstract void beforeFederationCreate();
	public abstract void beforeFederationJoin();
	public abstract void beforeReadyToPopulate();
	public abstract void beforeReadyToRun();
	public abstract boolean step( double currentTime ); // == 0
	public abstract void beforeReadyToResign();
	
	public abstract void receiveObjectRegistration( HLAObject hlaObject );
	public abstract void receiveAttributeReflection( HLAObject hlaObject, double time );
	public abstract void receiveInteraction( HLAInteraction hlaInteraction, double time );
	
	///////////////////////////////////////////////////////////////////
	/// Internal Methods       ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////
	public void runFederate( FederateConfiguration configuration )
	{
		this.configuration = configuration;
		
		this.rtiamb = RTIAmbassadorWrapper.instance();
		this.fedamb = new FederateAmbassador( this );
		
		createAndJoinFederation();
		enableTimePolicy();
		publishAndSubscribe();
		synchronize( UCEFSyncPoint.READY_TO_POPULATE.getLabel() );
		registerObjects();
		
		beforeReadyToRun();
		synchronize( UCEFSyncPoint.READY_TO_RUN.getLabel() );

		while( true )
		{
			// next step
			if( step( currentTime ) == false )
				break;
			
			// advance, or tick, or nothing!
			if( configuration.isTimeStepped() )
				advanceTime( currentTime + configuration.getLookAhead() );
			else if( configuration.callbacksAreEvoked() )
				rtiamb.evokeMultipleCallbacks( MIN_TIME, MAX_TIME );
			else
				;
		}

		// time to run away
		this.beforeReadyToResign();
		synchronize( UCEFSyncPoint.READY_TO_RESIGN.getLabel() );
		
		resignAndDestroyFederation();
	}
	
	private void createAndJoinFederation()
	{
		this.beforeFederationCreate();
		
		rtiamb.connect(fedamb);
		rtiamb.createFederation( configuration );
		
		this.beforeFederationJoin();
		rtiamb.joinFederation( configuration );
		
		this.beforeReadyToPopulate();
		this.synchronize( UCEFSyncPoint.READY_TO_POPULATE.getLabel() );
	}

	private void synchronize( String label )
	{
		rtiamb.registerFederationSynchronizationPoint( label, null );
		rtiamb.synchronizationPointAchieved( label );
		fedamb.waitForSychronized( label );
	}
	
	private void enableTimePolicy()
	{
		// RTIAmbassadorWrapper::simpleMethods()
	}

	private void publishAndSubscribe()
	{
		publishObjectClassAttributes(configuration.getPublishedAttributes());
		subscribeObjectClassesAttributes(configuration.getSubscribedAttributes());
		publishInteractionClasses(configuration.getPublishedInteractions());
		subscribeInteractionClasses(configuration.getSubscribedInteractions());
	}

	private void registerObjects()
	{
	}
	
	private void advanceTime( double nextTime )
	{
		rtiamb.timeAdvanceRequest( nextTime );
		while( this.currentTime < nextTime )
			rtiamb.evokeMultipleCallbacks( MIN_TIME, MAX_TIME );
	}
	
	private void resignAndDestroyFederation()
	{
		
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
					throw new UCEFException( "Unknown attribute name '%s'. Cannot publish attributes for object class %s.",
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
			throw new UCEFException( "NULL attribute handle set. Cannot publish attributes for object class %s.",
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
			throw new UCEFException( "Cannot subscribe to interaction class using unknown class name '%s'.",
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
			throw new UCEFException( "NULL interaction class handle. Cannot subscribe to interaction class." );

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
					throw new UCEFException( "Unknown attribute name '%s'. Cannot subscribe to attributes for object class %s.",
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
			throw new UCEFException( "NULL attribute handle set . Cannot subscribe to attributes for object class %s.",
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
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	private String assembleObjectClassDetails(ObjectClassHandle handle)
	{
		String className = rtiamb.getObjectClassName( handle );
		
		StringBuilder details = new StringBuilder( "'" + className + "'" );
		details.append( " (handle'" + className + "')" );
		
		return details.toString();
	}
	
	private String assembleInteractionClassDetails(InteractionClassHandle handle)
	{
		String className = rtiamb.getInteractionClassName( handle );
		
		StringBuilder details = new StringBuilder( "'" + className + "'" );
		details.append( " (handle'" + className + "')" );
		
		return details.toString();
	}
}

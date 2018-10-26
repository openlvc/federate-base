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
package gov.nist.ucef.hla.common;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.util.HLACodecUtils;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

/**
 * The purpose of this class is to provide (as much as is possible) methods which are common to
 * all federates in order to minimize the amount of code required in UCEF HLA federate
 * implementations.
 */
public class FederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getFormatterLogger( FederateBase.class );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// parameters which set up the federate - can only be modified *before* the federate is run
	private FederateConfiguration federateConfiguration;
	private IUCEFFederateImplementation federateImplementation;

	// Bits and pieces related to the RTI 
	private RTIambassador rtiamb;
	private AmbassadorBase fedamb; // created when we connect
	private Set<ObjectInstanceHandle> objectInstanceHandles;
	private HLAfloat64TimeFactory timeFactory; // set when we join
	protected HLACodecUtils codecUtils = HLACodecUtils.instance(); // set when we join

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateBase( FederateConfiguration federateConfiguration,
	                     IUCEFFederateImplementation federateImplementation )
	{
		// freeze the configuration now - probably it is already frozen, but since we want to
		// ensure that there are no modifications once it is used to set up the federate, we 
		// make sure that it really is frozen.
		this.federateConfiguration = federateConfiguration.freeze();
		this.federateImplementation = federateImplementation;

		this.objectInstanceHandles = new HashSet<ObjectInstanceHandle>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public FederateConfiguration getFederateConfiguration()
	{
		return this.federateConfiguration;
	}

	public IUCEFFederateImplementation getFederateImplementation()
	{
		return this.federateImplementation;
	}

	/**
	 * This is the main simulation loop. It can be thought of as the main method of the federate.
	 * For a description of the basic flow of this federate, see the class level comments
	 */
	public void runFederate() throws Exception
	{
		initializeAmbassadorAndConnect();
		createAndJoinFederation();
		
		// set up time policies - here we enable/disable all time policies
		// (note that this step is actually optional)
		enableTimePolicy();
		// tell the RTI of all the data we are going to produce, and all the 
		// data we want to know about
		initializePublishAndSubscribe();

		federateImplementation.doInitialisationTasks();

		// register the READY_TO_POPULATE sync point and announce it so that we can prepare for this state
		registerSyncPointAndWaitForAnnounce( SyncPoint.READY_TO_POPULATE );
		federateImplementation.doPostAnnouncePreAchievePopulateTasks();
		// achieve the READY_TO_POPULATE sync point and then wait until the federation has synchronized
		achieveSyncPointAndWaitForFederation( SyncPoint.READY_TO_POPULATE );
		federateImplementation.doPopulationTasks();

		// register the READY_TO_RUN sync point and announce it so that we can prepare for this state
		registerSyncPointAndWaitForAnnounce( SyncPoint.READY_TO_RUN );
		federateImplementation.doPostAnnouncePreAchieveRunTasks();
		// achieve the READY_TO_RUN sync point and then wait until the federation has synchronized
		achieveSyncPointAndWaitForFederation( SyncPoint.READY_TO_RUN );

		// -------------------------------------------------------------------------------------
		// TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
		// main simulation loop
		while(federateImplementation.shouldTick())
		{
			federateImplementation.tick();
			advanceTimeAndWait( federateImplementation.getTimeStep() );
		}
		// -------------------------------------------------------------------------------------

		// register the READY_TO_RESIGN sync point and announce it so that we can prepare for this state
		registerSyncPointAndWaitForAnnounce( SyncPoint.READY_TO_RESIGN );
		federateImplementation.doPostAnnouncePreAchieveResignTasks();
		// achieve the READY_TO_RESIGN sync point and then wait until the federation has synchronized
		achieveSyncPointAndWaitForFederation( SyncPoint.READY_TO_RESIGN );
		federateImplementation.doResignTasks();

		// delete all the objects we created and resign from the federation 
		cleanUpAndResign();

		// destroy the federation (though if other federates remain it will stay
		// up and they will destroy it instead)
		destroyFederation();
	}

	public String getClassIdentifierFromClassHandle( ObjectClassHandle handle )
	{
		try
		{
			return this.rtiamb.getObjectClassName( handle );
		}
		catch( InvalidObjectClassHandle | FederateNotExecutionMember | NotConnected |
			   RTIinternalError e )
		{
			return null;
		}
	}

	public ObjectClassHandle getClassHandleFromClassIdentifier( String identifier )
	{
		try
		{
			return this.rtiamb.getObjectClassHandle( identifier );
		}
		catch( FederateNotExecutionMember | NotConnected | RTIinternalError | NameNotFound e )
		{
			return null;
		}
	}

	public ObjectClassHandle getClassHandleFromInstanceHandle( ObjectInstanceHandle handle )
	{
		try
		{
			return rtiamb.getKnownObjectClassHandle( handle );
		}
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected |
			   RTIinternalError e )
		{
			return null;
		}
	}

	public String getAttributeIdentifierFromHandle( ObjectClassHandle objectClassHandle,
	                                                AttributeHandle attributeHandle )
	{
		try
		{
			return this.rtiamb.getAttributeName( objectClassHandle, attributeHandle );
		}
		catch( AttributeNotDefined | InvalidAttributeHandle | InvalidObjectClassHandle |
			   FederateNotExecutionMember | NotConnected | RTIinternalError e )
		{
			return null;
		}
	}

	public AttributeHandle getHandleFromAttributeIdentifier( ObjectClassHandle objectClassHandle,
	                                                         String identifier )
	{
		try
		{
			return this.rtiamb.getAttributeHandle( objectClassHandle, identifier );
		}
		catch( NameNotFound | InvalidObjectClassHandle | FederateNotExecutionMember |
			   NotConnected | RTIinternalError e )
		{
			return null;
		}
	}

	public String getInteractionIdentifierFromHandle( InteractionClassHandle handle )
	{
		try
		{
			return this.rtiamb.getInteractionClassName( handle );
		}
		catch( InvalidInteractionClassHandle | FederateNotExecutionMember | NotConnected |
			   RTIinternalError e )
		{
			return null;
		}
	}

	public InteractionClassHandle getHandleFromInteractionIdentifier( String identifier )
	{
		try
		{
			return this.rtiamb.getInteractionClassHandle( identifier );
		}
		catch( NameNotFound | FederateNotExecutionMember | NotConnected | RTIinternalError e )
		{
			return null;
		}
	}

	public ParameterHandle getHandleFromParameterIdentifier( InteractionClassHandle klassHandle, String identifier )
	{
		try
		{
			return this.rtiamb.getParameterHandle( klassHandle, identifier );
		}
		catch( NameNotFound | FederateNotExecutionMember | NotConnected | RTIinternalError |
			   InvalidInteractionClassHandle e )
		{
			return null;
		}
	}
	
	public String getParameterIdentifierFromHandle( InteractionClassHandle klassHandle, ParameterHandle handle )
	{
		try
		{
			return this.rtiamb.getParameterName( klassHandle, handle );
		}
		catch( FederateNotExecutionMember | NotConnected | RTIinternalError |
			   InvalidInteractionClassHandle | InteractionParameterNotDefined |
			   InvalidParameterHandle e )
		{
			return null;
		}
	}
	
	public String getObjectInstanceIdentifierFromHandle( ObjectInstanceHandle handle )
	{
		try
		{
			return this.rtiamb.getObjectInstanceName( handle );
		}
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected |
			   RTIinternalError e )
		{
			return null;
		}
	}

	public ObjectInstanceHandle getHandleFromObjectInstanceIdentifier( String identifier )
	{
		try
		{
			return this.rtiamb.getObjectInstanceHandle( identifier );
		}
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected | RTIinternalError e )
		{
			return null;
		}
	}

	private void initializeAmbassadorAndConnect() throws RTIexception
	{
		logger.info( "Creating RTI Ambassador" );
		this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();

		logger.info( "Connecting RTI Ambassador..." );
		this.fedamb = new AmbassadorBase( this );
		this.rtiamb.connect( fedamb, CallbackModel.HLA_EVOKED );
		logger.info( "RTI Ambassador is connected." );

		// cache the time factory for easy access
		this.timeFactory = (HLAfloat64TimeFactory)rtiamb.getTimeFactory();
	}

	private void createAndJoinFederation() throws RTIexception
	{
		logger.info( "Creating Federation..." );

		String federateName = this.federateConfiguration.getFederateName();
		String federateType = this.federateConfiguration.getFederateType();
		String federationName = this.federateConfiguration.getFederationName();
		URL[] modules = this.federateConfiguration.getModules().toArray( new URL[0] );
		URL[] joinModules = this.federateConfiguration.getJoinModules().toArray( new URL[0] );

		try
		{
			// We attempt to create a new federation with the configured FOM modules
			this.rtiamb.createFederationExecution( federationName, modules );
			logger.info( String.format( "Created federation '%s'.", federationName ) );
		}
		catch( FederationExecutionAlreadyExists exists )
		{
			// ignore - this is not an error condition as such, it just means that
			// the federation was created by someone else so we don't need to
			logger.info( String.format( "Didn't create federation '%s' because it already existed.",
			                            federationName ) );
		}

		// join the federation with the configured join FOM modules
		rtiamb.joinFederationExecution( federateName, federateType, federationName, joinModules );
		logger.info( String.format( "Joined Federation as '%s'", federateName ) );
	}

	private void registerSyncPointAndWaitForAnnounce( SyncPoint syncPoint ) throws RTIexception
	{
		registerSyncPointAndWait( syncPoint, null );
	}

	private void registerSyncPointAndWait( SyncPoint syncPoint, byte[] tag ) throws RTIexception
	{
		registerSyncPoint( syncPoint, tag );
		waitForSyncPointAnnouncement( syncPoint );
	}

	private void achieveSyncPointAndWaitForFederation( SyncPoint syncPoint ) throws RTIexception
	{
		achieveSyncPoint( syncPoint );
		waitForFederationToAchieve( syncPoint );
	}

	private void cleanUpAndResign() throws RTIexception
	{
		deleteObjects( this.objectInstanceHandles );
		resignFromFederation();
	}

	private void registerSyncPoint( SyncPoint syncPoint ) throws RTIexception
	{
		registerSyncPoint( syncPoint, null );
	}

	private void registerSyncPoint( SyncPoint syncPoint, byte[] tag )
	    throws RTIexception
	{
		// Note that if the point already been registered, there will be a callback saying this
		// failed, but as long as *someone* registered it everything is fine

		rtiamb.registerFederationSynchronizationPoint( syncPoint.getID(), tag );

		logger.info( String.format( "Synchronization point '%s' was registered.",
		                            syncPoint.toString() ) );
	}

	private void waitForSyncPointAnnouncement( SyncPoint syncPoint )
	    throws RTIexception
	{
		// TODO - Is it possible to get into a state whereby we never exit this loop...?
		// wait until the point is announced
		logger.info( String.format( "Waiting for announcement of synchronization point '%s'",
		                            syncPoint.toString() ) );

		while( syncPoint.isNot( fedamb.announcedSyncPoint ) )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}

		logger.info( String.format( "Synchronization point '%s' was announced.",
		                            syncPoint.toString() ) );
	}

	private void achieveSyncPoint( SyncPoint syncPoint )
	    throws RTIexception
	{
		rtiamb.synchronizationPointAchieved( syncPoint.getID() );
		logger.info( String.format( "Achieved synchronization point '%s'", syncPoint.toString() ) );
	}

	private void waitForFederationToAchieve( SyncPoint syncPoint )
	    throws RTIexception
	{
		logger.info( String.format( "Waiting for federation '%s' to achieve synchronization point '%s'",
		                            this.federateConfiguration.getFederationName(),
		                            syncPoint.toString() ) );

		// TODO - Is it possible to get into a state whereby we never exit this loop...?
		// wait until the synchronization point is reached by the federation
		while( syncPoint.isNot( fedamb.currentSyncPoint ) )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}

		logger.info( String.format( "Federation '%s' has achieved synchronization point '%s'",
		                            this.federateConfiguration.getFederateName(),
		                            syncPoint.toString() ) );
	}

	private void resignFromFederation() throws RTIexception
	{
		resignFromFederation( ResignAction.DELETE_OBJECTS );
	}

	private void resignFromFederation( ResignAction resignAction ) throws RTIexception
	{
		rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );

		logger.info( "Resigned from Federation" );
	}

	private void destroyFederation() throws RTIexception
	{
		String federationName = this.federateConfiguration.getFederationName();
		try
		{
			logger.info( String.format( "Attempting to destroy federation '%s'...",
			                            federationName ) );
			rtiamb.destroyFederationExecution( federationName );
			logger.info( String.format( "Federation '%s' has been destroyed.", federationName ) );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			logger.info( String.format( "Federation '%s' could not be destroyed because it does not exist.",
			                            federationName ) );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			// if other federates remain we have to leave it for them to clean up
			logger.info( String.format( "Federation '%s' was not be destroyed because federates still remain.",
			                            federationName ) );
		}
	}

	/**
	 * This method will attempt to enable the various time related properties for the federate
	 */
	private void enableTimePolicy()
	    throws Exception
	{
		// NOTE: Unfortunately, the LogicalTime/LogicalTimeInterval create code is
		//       Portico specific. You will have to alter this if you move to a
		//       different RTI implementation. As such, we've isolated it into a
		//       method so that any change only needs to happen in a couple of spots 
		HLAfloat64Interval lookahead =
		    timeFactory.makeInterval( federateConfiguration.getLookAhead() );

		////////////////////////////
		// enable time regulation //
		////////////////////////////
		this.rtiamb.enableTimeRegulation( lookahead );

		// tick until we get the callback
		while( fedamb.isRegulating == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}

		/////////////////////////////
		// enable time constrained //
		/////////////////////////////
		this.rtiamb.enableTimeConstrained();

		// tick until we get the callback
		while( fedamb.isConstrained == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}

		logger.info( "Time Policy Enabled" );
	}
	
	/**
	 * This method will inform the RTI about the types of data that the federate will be creating,
	 * and the types of data we are interested in hearing about as other federates produce it.
	 */
	private void initializePublishAndSubscribe() throws RTIexception
	{
		initializePublishInteractions();
		initializePublishAttributes();
		
		initializeSubscribeInteractions();
		initializeSubscribeAttributes();

		logger.debug( String.format( "Publications and subscriptions initialized for federate '%s'.", 
		                             this.federateConfiguration.getFederateName() ) );
	}
	
	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * publishing to the federation.
	 */
	private void initializePublishInteractions() throws RTIexception
	{
		String federateName = this.federateConfiguration.getFederateName();
		Collection<String> publishedInteractions = this.federateConfiguration.getPublishedInteractions();
		if(publishedInteractions.isEmpty())
		{
			logger.debug( String.format( "Federate '%s' will not publish any interactions.",
			                             federateName ) );
		}
		else
		{
			for( String interactionID : publishedInteractions )
			{
				InteractionClassHandle handle = rtiamb.getInteractionClassHandle( interactionID );
				// do the publication - we only need to tell the RTI about the interaction's class,
				// not the associated parameters, so this is very simple:
				rtiamb.publishInteractionClass( handle );
				logger.debug( String.format( "Federate '%s' will publish interaction '%s', handle is %s",
				                             federateName,
				                             interactionID, handle ) );
			}
		}
	}

	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and to which classes they belong.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values 
	 */
	private void initializePublishAttributes() throws RTIexception
	{
		String federateName = this.federateConfiguration.getFederateName();
		
		// publish attributes of classes
		// before we can , we need to tell the RTI that we intend to publish
		// this information
		Map<String,Set<String>> publishedAttributes = this.federateConfiguration.getPublishedAttributes();
		if(publishedAttributes.isEmpty())
		{
			logger.debug( String.format( "Federate '%s' will not publish any attributes.",
			                             federateName ) );
		}
		else
		{
			for( Entry<String,Set<String>> publication : publishedAttributes.entrySet() )
			{
				// this list is for logging purposes only
				List<String> publicationDetails = new ArrayList<>();
				
				// get all the handle information for the attributes of the current class
				String klassIdentifier = publication.getKey();
				ObjectClassHandle klassHandle = rtiamb.getObjectClassHandle( klassIdentifier );
				// package the information into the handle set
				AttributeHandleSet attributeHandleSet = rtiamb.getAttributeHandleSetFactory().create();
				for( String attribute : publication.getValue() )
				{
					AttributeHandle attributeHandle = rtiamb.getAttributeHandle( klassHandle, attribute );
					attributeHandleSet.add( attributeHandle );
					publicationDetails.add( String.format( "'%s' (handle = %s)", 
					                                       attribute, attributeHandle ) );
				}
				// do the actual publication
				rtiamb.publishObjectClassAttributes( klassHandle, attributeHandleSet );
				logger.debug( String.format( "Federate '%s' will publish the following %d attribute(s) of '%s' (handle = %s): %s", 
				                             federateName,
				                             publicationDetails.size(),
				                             klassIdentifier, klassHandle, 
				                             String.join( ", ", publicationDetails ) ) );
			}
		}
	}
	
	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * interested in hearing about as other federates produce them.
	 */
	private void initializeSubscribeInteractions() throws RTIexception
	{
		String federateName = this.federateConfiguration.getFederateName();
		Collection<String> subscribedInteractions = this.federateConfiguration.getSubscribedInteractions();
		if(subscribedInteractions.isEmpty())
		{
			logger.debug( String.format( "Federate '%s' will not subscribe to any interactions.",
			                             federateName ) );
		}
		else
		{
			for( String interactionID : subscribedInteractions )
			{
				InteractionClassHandle handle = rtiamb.getInteractionClassHandle( interactionID );
				// do the publication - we only need to tell the RTI about the interaction's class,
				// not the associated parameters, so this is very simple:
				rtiamb.subscribeInteractionClass( handle );
				logger.debug( String.format( "Federate '%s' will subscribe to interaction '%s', handle is %s",
				                             federateName,
				                             interactionID, handle ) );
			}
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * interested in hearing about, and to which classes they belong.
	 * 
	 * We need to subscribe to hear about information on attributes of classes created and altered
	 * in other federates 
	 */
	private void initializeSubscribeAttributes() throws RTIexception
	{
		String federateName = this.federateConfiguration.getFederateName();
		
		Map<String,Set<String>> subscribedAttributes = this.federateConfiguration.getSubscribedAttributes();
		if(subscribedAttributes.isEmpty())
		{
			logger.debug( String.format( "Federate '%s' will not subscribe to any attributes.",
			                             federateName ) );
		}
		else
		{
			for( Entry<String,Set<String>> subscription : subscribedAttributes.entrySet() )
			{
				// this list is for logging purposes only
				List<String> subscriptionDetails = new ArrayList<>();
				
				// get all the handle information for the attributes of the current class
				String klassIdentifier = subscription.getKey();
				ObjectClassHandle klassHandle = rtiamb.getObjectClassHandle( klassIdentifier );
				// package the information into the handle set
				AttributeHandleSet attributeHandleSet = rtiamb.getAttributeHandleSetFactory().create();
				for( String attribute : subscription.getValue() )
				{
					AttributeHandle attributeHandle = rtiamb.getAttributeHandle( klassHandle, attribute );
					attributeHandleSet.add( attributeHandle );
					subscriptionDetails.add( String.format( "'%s' (handle = %s)",
					                                        attribute, attributeHandle ) );
				}
				
				// do the actual subscription
				rtiamb.subscribeObjectClassAttributes( klassHandle, attributeHandleSet );
				logger.debug( String.format( "Federate '%s' will subscribe to the following %d attribute(s) of '%s' (handle = %s): %s", 
				                             federateName,
				                             subscriptionDetails.size(),
				                             klassIdentifier, klassHandle, 
				                             String.join( ", ", subscriptionDetails ) ) );
			}
		}
	}
	
	/**
	 * This method will register an instance of the class and will return the federation-wide
	 * unique handle for that instance. Later in the simulation, we will update the attribute
	 * values for this instance.
	 */
	public ObjectInstanceHandle registerObject( String klassIdentifier ) throws RTIexception
	{
		ObjectClassHandle klassHandle = getClassHandleFromClassIdentifier( klassIdentifier );
		if( klassHandle == null )
		{
			logger.error( String.format( "Could not register object instance. No handle was found for class identifier '%s'",
			                             klassIdentifier ) );
			return null;
		}

		ObjectInstanceHandle instanceHandle = rtiamb.registerObjectInstance( klassHandle );
		logger.info( String.format( "Registered object instance of class '%s' (class handle is %s). Instance handle is %s",
		                            klassIdentifier, klassHandle, instanceHandle ) );

		this.objectInstanceHandles.add( instanceHandle );

		return instanceHandle;
	}

	/**
	 * This method will update all the values of the given object instance. It will set the
	 * flavour of the soda to a random value from the options specified in the FOM (Cola - 101,
	 * Orange - 102, RootBeer - 103, Cream - 104) and it will set the number of cups to the same
	 * value as the current time.
	 * <p/>
	 * Note that we don't actually have to update all the attributes at once, we could update them
	 * individually, in groups or not at all!
	 */
	public void updateAttributeValues( ObjectInstanceHandle objectinstanceHandle ) throws RTIexception
	{
		///////////////////////////////////////////////
		// create the necessary container and values //
		///////////////////////////////////////////////
		ObjectClassHandle objectClassHandle = rtiamb.getKnownObjectClassHandle( objectinstanceHandle );

		// create a new map with an initial capacity - this will grow as required
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create( 2 );

		// create the collection to store the values in, as you can see
		// this is quite a lot of work. You don't have to use the encoding
		// helpers if you don't want. The RTI just wants an arbitrary byte[]

		// generate the value for the number of cups (same as the timestep)

		// HLAinteger16BE cupsValue = encoderFactory.createHLAinteger16BE( getTimeAsShort() );
		// TODO - hard coded string
		updateAttribute( objectClassHandle, "NumberCups", getTimeAsShort(), attributes );

		// generate the value for the flavour on our magically flavour changing drink
		// the values for the enum are defined in the FOM
		// TODO - hard coded string
		int randomValue = 101 + new Random().nextInt( 3 );
		// HLAinteger32BE flavValue = encoderFactory.createHLAinteger32BE( randomValue );
		updateAttribute( objectClassHandle, "Flavor", randomValue, attributes );

		//////////////////////////
		// do the actual update //
		//////////////////////////
		rtiamb.updateAttributeValues( objectinstanceHandle, attributes, generateTag() );

		// note that if you want to associate a particular timestamp with the
		// update. here we send another update, this time with a timestamp:
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + federateConfiguration.getLookAhead() );
		rtiamb.updateAttributeValues( objectinstanceHandle, attributes, generateTag(), time );
	}

	private AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                 String identifier,
	                                                 short value,
	                                                 AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle,
		                        identifier,
		                        Short.toString( value ),
		                        attributes );
	}

	private AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                 String identifier,
	                                                 int value,
	                                                 AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle,
		                        identifier,
		                        Integer.toString( value ),
		                        attributes );
	}

	private AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                 String identifier,
	                                                 long value,
	                                                 AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle, identifier, Long.toString( value ), attributes );
	}

	private AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                 String identifier,
	                                                 double value,
	                                                 AttributeHandleValueMap attributes )
	{
		return updateAttribute(
		                        objectClassHandle,
		                        identifier,
		                        Double.toString( value ),
		                        attributes );
	}

	private AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                 String identifier,
	                                                 float value,
	                                                 AttributeHandleValueMap attributes )
	{
		return updateAttribute(
		                        objectClassHandle,
		                        identifier,
		                        Float.toString( value ),
		                        attributes );
	}

	private AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                 String identifier,
	                                                 String value,
	                                                 AttributeHandleValueMap attributes )
	{
		AttributeHandle attrHandle = getHandleFromAttributeIdentifier( objectClassHandle, identifier );

		if( attrHandle != null )
		{
			HLAunicodeString attrValue = codecUtils.makeString( value );
			attributes.put( attrHandle, attrValue.toByteArray() );
		}

		return attributes;
	}

	/**
	 * This method will send out an interaction of the type FoodServed.DrinkServed. Any federates
	 * which are subscribed to it will receive a notification the next time they tick(). This
	 * particular interaction has no parameters, so you pass an empty map, but the process of
	 * encoding them is the same as for attributes.
	 */
	public void sendInteraction( String identifier ) throws RTIexception
	{
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create( 0 );
		sendInteraction( identifier, parameters); 
	}
	
	/**
	 * This method will send out an interaction of the type FoodServed.DrinkServed. Any federates
	 * which are subscribed to it will receive a notification the next time they tick(). This
	 * particular interaction has no parameters, so you pass an empty map, but the process of
	 * encoding them is the same as for attributes.
	 */
	public void sendInteraction( String identifier, ParameterHandleValueMap parameters )
	    throws RTIexception
	{
		InteractionClassHandle servedHandle = getHandleFromInteractionIdentifier( identifier );

		//////////////////////////
		// send the interaction //
		//////////////////////////
		rtiamb.sendInteraction( servedHandle, parameters, generateTag() );

		// if you want to associate a particular timestamp with the
		// interaction, you will have to supply it to the RTI. Here
		// we send another interaction, this time with a timestamp:
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + federateConfiguration.getLookAhead() );
		rtiamb.sendInteraction( servedHandle, parameters, generateTag(), time );
	}

	/**
	 * This method will request a time advance to the current time, plus the given timestep. It
	 * will then wait until a notification of the time advance grant has been received.
	 */
	private void advanceTimeAndWait( double timestep )
	    throws RTIexception
	{
		// request the advance
		fedamb.isAdvancing = true;
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + timestep );
		rtiamb.timeAdvanceRequest( time );

		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while( fedamb.isAdvancing )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
	}

	/**
	 * This method will attempt to delete the object instances withthe given handles. We can only
	 * delete objects we created, or for which we own the privilegeToDelete attribute.
	 */
	private void deleteObjects( Collection<ObjectInstanceHandle> handles )
	{
		Set<ObjectInstanceHandle> deleted = new HashSet<ObjectInstanceHandle>();

		for( ObjectInstanceHandle handle : handles )
		{
			try
			{
				rtiamb.deleteObjectInstance( handle, generateTag() );
				deleted.add( handle );
			}
			catch( RTIexception e )
			{
				logger.error( String.format( "Unable to delete object instance '%s' with handle $s",
				                             getObjectInstanceIdentifierFromHandle( handle ), handle) );
			}
		}

		deleted.forEach( ( handle ) -> this.objectInstanceHandles.remove( handle ) );

		logger.info( String.format( "Deleted %d objects.", deleted.size() ) );
	}

	private short getTimeAsShort()
	{
		return (short)fedamb.federateTime;
	}

	private byte[] generateTag()
	{
		return ("(timestamp) " + System.currentTimeMillis()).getBytes();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

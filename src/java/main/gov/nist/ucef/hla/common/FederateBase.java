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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.util.HLACodecUtils;
import gov.nist.ucef.hla.util.InputUtils;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
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
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

/**
 * The purpose of this class is to provide (as much as is possible) methods which are common to all
 * federates in order to minimize the amount of code required in UCEF HLA federate implementations.
 * 
 */
public class FederateBase
{

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    private static final Logger logger = LogManager.getFormatterLogger(FederateBase.class);

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    // parameters which set up the federate - can only be modified *before* the federate is run
    // TODO - prevent modification of these once the federate starts
	private String federationName;
	private String federateType;
    private String federateName;
    private List<URL> modules;
    private List<URL> joinModules;
	private Map<String,Set<String>> publishedAttributes;
	private Map<String,Set<String>> subscribedAttributes;
	private Set<String> publishedInteractions;
	private Set<String> subscribedInteractions;
	
	// Bits and pieces related to the RTI 
	private RTIambassador rtiamb;
	private AmbassadorBase fedamb;  // created when we connect
	private HLAfloat64TimeFactory timeFactory; // set when we join
	protected HLACodecUtils codecUtils = HLACodecUtils.instance();     // set when we join

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateBase(String federationName,
	                    String federateName, String federateType)
	{
		this(federationName, federateName, federateType,
		     null, null, 
		     null, null, 
		     null, null);
	}
	
	public FederateBase(String federationName,
	                    String federateName, String federateType, 
	                    List<URL> modules, List<URL> joinModules,
	                    Map<String, Set<String>> publishedAttributes, Map<String, Set<String>> subscribedAttributes,
	                    Set<String> publishedInteractions, Set<String> subscribedInteractions)
	{
		this.federationName = federationName;
		
		this.federateName = federateName;
		this.federateType= federateType;
		
		this.modules = modules == null ? new ArrayList<URL>() : modules;
		this.joinModules = joinModules == null ? new ArrayList<URL>() : joinModules;
		this.publishedAttributes = publishedAttributes == null ? new HashMap<>() : publishedAttributes;
		this.subscribedAttributes = subscribedAttributes == null ? new HashMap<>() : subscribedAttributes;
		this.publishedInteractions = publishedInteractions == null ? new HashSet<>() : publishedInteractions;
		this.subscribedInteractions = subscribedInteractions == null ? new HashSet<>() : subscribedInteractions;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public FederateBase addModules(List<URL> modules)
	{
		if(modules != null)
			this.modules.addAll( modules );
		
		return this;
	}
	
	public FederateBase addJoinModules(List<URL> joinModules)
	{
		if(joinModules != null)
			this.joinModules.addAll( joinModules );
		
		return this;
	}
	
	public FederateBase addPublishedAtributes(Map<String, Set<String>> publishedAttributes)
	{
		mergeSetMaps(publishedAttributes, this.publishedAttributes);
		return this;
	}
	
	public FederateBase addSubscribedAtributes(Map<String, Set<String>> subscribedAttributes)
	{
		mergeSetMaps(subscribedAttributes, this.subscribedAttributes);
		return this;
	}
	
	public FederateBase addPublishedInteractions(Set<String> publishedInteractions)
	{
		if(publishedInteractions != null)
			this.publishedInteractions.addAll( publishedInteractions );

		return this;
	}
	
	public FederateBase addSubscribedInteractions(Set<String> subscribedInteractions)
	{
		if(subscribedInteractions != null)
			this.subscribedInteractions.addAll( subscribedInteractions );
		
		return this;
	}
	
	/**
	 * Utility method to merge the content of a maps of sets into another map of sets
	 * 
	 * Doesn't really need to use generics here, but why not, eh? :)
	 * 
	 * @param src the map containing the source data
	 * @param dest the existing map to merge the source data into
	 */
	private <K, V> void mergeSetMaps(Map<K, Set<V>> src, Map<K, Set<V>> dest)
	{
		if(src == null || dest == null)
			return;
		
		for( Entry<K,Set<V>> entry : src.entrySet() )
		{
			dest.computeIfAbsent(entry.getKey(), x -> new HashSet<>()).addAll( entry.getValue() );
		}
	}

	/**
	 * This is the main simulation loop. It can be thought of as the main method of
	 * the federate. For a description of the basic flow of this federate, see the
	 * class level comments
	 */
	public void runFederate() throws Exception
	{
		/////////////////////////////////////////////////
		// 1 & 2. create the RTIambassador and Connect //
		/////////////////////////////////////////////////
		initializeAmbassadorAndConnect();

		//////////////////////////////
		// 3. create the federation //
		//////////////////////////////
		logger.error( "Creating Federation..." );
		// We attempt to create a new federation with the first three of the
		// restaurant FOM modules covering processes, food and drink
		try
		{
			rtiamb.createFederationExecution( this.federationName, this.modules.toArray(new URL[0]) );
			logger.error( "Created Federation" );
		}
		catch( FederationExecutionAlreadyExists exists )
		{
			logger.error( "Didn't create federation, it already existed" );
		}
		
		////////////////////////////
		// 4. join the federation //
		////////////////////////////
		rtiamb.joinFederationExecution( this.federateName,
		                                this.federateType,
		                                this.federationName,
		                                this.joinModules.toArray(new URL[0]) );

		logger.error( "Joined Federation as " + federateName );

		// cache the time factory for easy access
		this.timeFactory = (HLAfloat64TimeFactory)rtiamb.getTimeFactory();

		//////////////////////////////////////////////////
		// 5. announce the READY_TO_POPULATE sync point //
		//////////////////////////////////////////////////
		// announce a sync point to get everyone on the same page. if the point
		// has already been registered, we'll get a callback saying it failed,
		// but we don't care about that, as long as someone registered it
		SyncPoint syncPoint = SyncPoint.READY_TO_POPULATE;
		registerSyncPoint( syncPoint );
		// wait until the point is announced
		waitForSyncPointAnnouncement( syncPoint );

		// WAIT FOR USER
		// wait until the user hits enter before proceeding, so there is 
		// time for the human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );

		//////////////////////////////////////////////////////////////////////////////
		// 6. achieve the READY_TO_POPULATE sync point and wait for synchronization //
		//////////////////////////////////////////////////////////////////////////////
		// tell the RTI we are ready to move past the sync point and then wait
		// until the federation has synchronized on
		achieveSyncPoint( syncPoint );
		logger.error( "Achieved sync point: " + syncPoint.toString() + ", waiting for federation..." );
		waitForFederationToAchieve( syncPoint );

		/////////////////////////////////////////////
		// 5. announce the READY_TO_RUN sync point //
		/////////////////////////////////////////////
		syncPoint = SyncPoint.READY_TO_RUN;
		registerSyncPoint( syncPoint );
		// wait until the point is announced
		waitForSyncPointAnnouncement( syncPoint );
		// WAIT FOR USER
		// wait until the user hits enter before proceeding, so there is 
		// time for the human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );

		/////////////////////////////////////////////////////////////////////////
		// 6. achieve the READY_TO_RUN sync point and wait for synchronization //
		/////////////////////////////////////////////////////////////////////////
		// tell the RTI we are ready to move past the sync point and then wait
		// until the federation has synchronized on
		achieveSyncPoint( syncPoint );
		logger.error( "Achieved sync point: " + syncPoint.toString() + ", waiting for federation..." );
		waitForFederationToAchieve( syncPoint );

		/////////////////////////////
		// 7. enable time policies //
		/////////////////////////////
		// in this section we enable/disable all time policies
		// note that this step is optional!
		enableTimePolicy();
		logger.error( "Time Policy Enabled" );

		//////////////////////////////
		// 8. publish and subscribe //
		//////////////////////////////
		// in this section we tell the RTI of all the data we are going to
		// produce, and all the data we want to know about
		initializePublishAndSubscribe();
		logger.error( "Published and Subscribed" );

		/////////////////////////////////////
		// 9. register an object to update //
		/////////////////////////////////////
		// TODO - hard coded strings
		String sodaIdentifier = "HLAobjectRoot.Food.Drink.Soda";
		ObjectInstanceHandle objectInstanceHandle = registerObject( sodaIdentifier );
		logger.error( "Registered Object '" + sodaIdentifier + "' handle=" + objectInstanceHandle);

		/////////////////////////////////////
		// 10. do the main simulation loop //
		/////////////////////////////////////
		// here is where we do the meat of our work. in each iteration, we will
		// update the attribute values of the object we registered, and will
		// send an interaction.
		for( int i = 0; i < 10; i++ )
		{
			// 9.1 update the attribute values of the instance //
			updateAttributeValues( objectInstanceHandle );

			// 9.2 send an interaction
			ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create( 0 );
			// TODO - hard coded string
			sendInteraction( "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed", parameters );

			// 9.3 request a time advance and wait until we get it
			advanceTime( 1.0 );
			logger.error( "Time Advanced to " + fedamb.federateTime );
		}

		////////////////////////////////////////////////
		// 5. announce the READY_TO_RESIGN sync point //
		////////////////////////////////////////////////
		syncPoint = SyncPoint.READY_TO_RESIGN;
		registerSyncPoint( syncPoint );
		// wait until the point is announced
		waitForSyncPointAnnouncement( syncPoint );

		// WAIT FOR USER
		// wait until the user hits enter before proceeding, so there is 
		// time for the human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );

		////////////////////////////////////////////////////////////////////////////
		// 6. achieve the READY_TO_RESIGN sync point and wait for synchronization //
		////////////////////////////////////////////////////////////////////////////
		// tell the RTI we are ready to move past the sync point and then wait
		// until the federation has synchronized on
		achieveSyncPoint( syncPoint );
		logger.error( "Achieved sync point: " + syncPoint.toString() + ", waiting for federation..." );
		waitForFederationToAchieve( syncPoint );

		//////////////////////////////////////
		// 11. delete the object we created //
		//////////////////////////////////////
		deleteObject( objectInstanceHandle );
		logger.error( "Deleted Object, handle=" + objectInstanceHandle );

		////////////////////////////////////
		// 12. resign from the federation //
		////////////////////////////////////
		rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		logger.error( "Resigned from Federation" );

		////////////////////////////////////////
		// 13. try and destroy the federation //
		////////////////////////////////////////
		// NOTE: we won't die if we can't do this because other federates
		//       remain. in that case we'll leave it for them to clean up
		try
		{
			rtiamb.destroyFederationExecution( this.federationName );
			logger.error( "Destroyed Federation" );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			logger.error( "No need to destroy federation, it doesn't exist" );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			logger.error( "Didn't destroy federation, federates still joined" );
		}
	}
	
	public String getClassIdentifierFromClassHandle( ObjectClassHandle handle )
	{
		try
		{
			return this.rtiamb.getObjectClassName( handle );
		}
		catch( InvalidObjectClassHandle | FederateNotExecutionMember | NotConnected | RTIinternalError e )
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
	
	public ObjectClassHandle getClassHandleFromInstanceHandle(ObjectInstanceHandle handle)
	{
		try
		{
			return rtiamb.getKnownObjectClassHandle( handle );
		}
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected | RTIinternalError e )
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
		catch( AttributeNotDefined | InvalidAttributeHandle | InvalidObjectClassHandle
		    | FederateNotExecutionMember | NotConnected | RTIinternalError e )
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
		catch( NameNotFound | InvalidObjectClassHandle | FederateNotExecutionMember | NotConnected
		    | RTIinternalError e )
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
		catch( InvalidInteractionClassHandle | FederateNotExecutionMember | NotConnected
		    | RTIinternalError e )
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

	public String getObjectInstanceIdentifierFromHandle( ObjectInstanceHandle handle )
	{
		try
		{
			return this.rtiamb.getObjectInstanceName( handle );
		}
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected
		    | RTIinternalError e )
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
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected
		    | RTIinternalError e )
		{
			return null;
		}
	}
	
	private void initializeAmbassadorAndConnect() throws RTIexception
	{
		logger.error( "Creating RTIambassador" );
		this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		
		logger.error( "Connecting..." );
		this.fedamb = new AmbassadorBase( this );
		this.rtiamb.connect( fedamb, CallbackModel.HLA_EVOKED );
	}
	
	private FederateBase registerSyncPoint(SyncPoint syncPoint) throws RTIexception
	{
		return registerSyncPoint( syncPoint, null);		
	}
	
	private FederateBase registerSyncPoint(SyncPoint syncPoint, byte[] tag) throws RTIexception
	{
		rtiamb.registerFederationSynchronizationPoint( syncPoint.getID(), tag );		
		return this;
	}
	
	private FederateBase waitForSyncPointAnnouncement(SyncPoint syncPoint) throws RTIexception
	{
		// wait until the point is announced
		while( syncPoint.isNot( fedamb.announcedSyncPoint ) )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
		return this;
	}
	
	private FederateBase achieveSyncPoint(SyncPoint syncPoint) throws RTIexception
	{
		rtiamb.synchronizationPointAchieved( syncPoint.getID() );
		return this;
	}
	
	private FederateBase waitForFederationToAchieve(SyncPoint syncPoint) throws RTIexception
	{
		// wait until the synchronization point is reached by the federation
		while( syncPoint.isNot( fedamb.currentSyncPoint ) )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
		return this;
	}
	
	/**
	 * This method will attempt to enable the various time related properties for
	 * the federate
	 */
	private void enableTimePolicy() throws Exception
	{
		// NOTE: Unfortunately, the LogicalTime/LogicalTimeInterval create code is
		//       Portico specific. You will have to alter this if you move to a
		//       different RTI implementation. As such, we've isolated it into a
		//       method so that any change only needs to happen in a couple of spots 
		HLAfloat64Interval lookahead = timeFactory.makeInterval( fedamb.federateLookahead );
		
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
	}
	
	/**
	 * This method will inform the RTI about the types of data that the federate will
	 * be creating, and the types of data we are interested in hearing about as other
	 * federates produce it.
	 */
	private void initializePublishAndSubscribe() throws RTIexception
	{
		///////////////////////////////////
		// publish attributes of classes //
		///////////////////////////////////
		// before we can register instances of the object classes and update the values of 
		// the various attributes, we need to tell the RTI that we intend to publish
		// this information
		for(Entry<String,Set<String>> publication : this.publishedAttributes.entrySet())
		{
			String klassIdentifier = publication.getKey();
			
			// get all the handle information for the attributes of the current class
			ObjectClassHandle klassHandle = rtiamb.getObjectClassHandle( klassIdentifier );
			logger.error( "Obtained ObjectClassHandle for '" + klassIdentifier + "' handle=" + klassHandle);
			
			// package the information into the handle set
			AttributeHandleSet attributeHandleSet = rtiamb.getAttributeHandleSetFactory().create();
			for(String attribute : publication.getValue())
			{
				AttributeHandle attributeHandle = rtiamb.getAttributeHandle( klassHandle, attribute );
				logger.error( "Obtained AttributeHandle for '" + attribute + "' of '" + klassIdentifier + "' handle=" + attributeHandle);
				attributeHandleSet.add( attributeHandle );
			}
			
			// do the actual publication
			rtiamb.publishObjectClassAttributes( klassHandle, attributeHandleSet );
		}
		
		////////////////////////////////////////
		// subscribe to attributes of classes //
		////////////////////////////////////////
		// we need to subscribe to hear about information on attributes of classes
		// created and altered in other federates
		for(Entry<String,Set<String>> subscription : this.subscribedAttributes.entrySet())
		{
			String klassIdentifier = subscription.getKey();
			
			// get all the handle information for the attributes of the current class
			ObjectClassHandle klassHandle = rtiamb.getObjectClassHandle( klassIdentifier );
			logger.error( "Obtained ObjectClassHandle for '" + klassIdentifier + "' handle=" + klassHandle);
			
			// package the information into the handle set
			AttributeHandleSet attributeHandleSet = rtiamb.getAttributeHandleSetFactory().create();
			for(String attributeIdentifier : subscription.getValue())
			{
				AttributeHandle attributeHandle = rtiamb.getAttributeHandle( klassHandle, attributeIdentifier );
				logger.error( "Obtained AttributeHandle for '" + attributeIdentifier + "' of '" + klassIdentifier + "' handle=" + attributeHandle);
				attributeHandleSet.add( attributeHandle );
			}
			
			// do the actual subscription
			rtiamb.subscribeObjectClassAttributes( klassHandle, attributeHandleSet );
		}

		/////////////////////////////////////
		// publish the interaction classes //
		/////////////////////////////////////
		// we need to tell the RTI about the interactions we are publishing. We don't need to
		// inform it of the parameters, only the class, making it much simpler
		for(String interactionIdentifier : this.publishedInteractions)
		{
			InteractionClassHandle interactionHandle = rtiamb.getInteractionClassHandle( interactionIdentifier );
			logger.error( "Obtained InteractionClassHandle for '" + interactionIdentifier + " handle=" + interactionHandle);
			// do the publication
			rtiamb.publishInteractionClass( interactionHandle );
		}

		//////////////////////////////////////////
		// subscribe to the interaction classes //
		//////////////////////////////////////////
		// we need to tell the RTI about the interactions we are are interested in which are
		// sent out by other federates, so we we subscribe to those here
		for(String interactionIdentifier : this.subscribedInteractions)
		{
			InteractionClassHandle interactionHandle = rtiamb.getInteractionClassHandle( interactionIdentifier );
			logger.error( "Obtained InteractionClassHandle for '" + interactionIdentifier + " handle=" + interactionHandle);
			// do the publication
			rtiamb.subscribeInteractionClass( interactionHandle );
		}
	}
	
	/**
	 * This method will register an instance of the class and will return the federation-wide unique 
	 * handle for that instance. Later in the simulation, we will update the attribute values for
	 * this instance
	 */
	private ObjectInstanceHandle registerObject(String klassIdentifier) throws RTIexception
	{
		ObjectClassHandle klassHandle = getClassHandleFromClassIdentifier( klassIdentifier );
		logger.error( "Looked up ObjectClassHandle for '" + klassIdentifier + " handle=" + klassHandle);

		if( klassHandle == null )
			return null;

		ObjectInstanceHandle instanceHandle = rtiamb.registerObjectInstance( klassHandle );
		logger.error( "Registered object instance with class handle '" + klassHandle + " handle=" + instanceHandle);
		return instanceHandle;
	}
	
	/**
	 * This method will update all the values of the given object instance. It will
	 * set the flavour of the soda to a random value from the options specified in
	 * the FOM (Cola - 101, Orange - 102, RootBeer - 103, Cream - 104) and it will set
	 * the number of cups to the same value as the current time.
	 * <p/>
	 * Note that we don't actually have to update all the attributes at once, we
	 * could update them individually, in groups or not at all!
	 */
	private void updateAttributeValues( ObjectInstanceHandle objectinstanceHandle ) throws RTIexception
	{
		///////////////////////////////////////////////
		// create the necessary container and values //
		///////////////////////////////////////////////
		// create a new map with an initial capacity - this will grow as required
		ObjectClassHandle objectClassHandle = rtiamb.getKnownObjectClassHandle( objectinstanceHandle );
		
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
		
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
		int randomValue = 101 + new Random().nextInt(3);
		// HLAinteger32BE flavValue = encoderFactory.createHLAinteger32BE( randomValue );
		updateAttribute( objectClassHandle, "Flavor", randomValue, attributes );

		//////////////////////////
		// do the actual update //
		//////////////////////////
		rtiamb.updateAttributeValues( objectinstanceHandle, attributes, generateTag() );
		
		// note that if you want to associate a particular timestamp with the
		// update. here we send another update, this time with a timestamp:
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );
		rtiamb.updateAttributeValues( objectinstanceHandle, attributes, generateTag(), time );
	}
	
	private AttributeHandleValueMap updateAttribute(ObjectClassHandle objectClassHandle, String identifier, 
	                                                short value, AttributeHandleValueMap attributes)
	{
		return updateAttribute(objectClassHandle, identifier, Short.toString(value), attributes);
	}
	
	private AttributeHandleValueMap updateAttribute(ObjectClassHandle objectClassHandle, String identifier,
	                                                int value, AttributeHandleValueMap attributes)
	{
		return updateAttribute(objectClassHandle, identifier, Integer.toString(value), attributes);
	}
	
	private AttributeHandleValueMap updateAttribute(ObjectClassHandle objectClassHandle, String identifier,
	                                                long value, AttributeHandleValueMap attributes)
	{
		return updateAttribute(objectClassHandle, identifier, Long.toString(value), attributes);
	}
	
	private AttributeHandleValueMap updateAttribute(ObjectClassHandle objectClassHandle, String identifier,
	                                                double value, AttributeHandleValueMap attributes)
	{
		return updateAttribute(objectClassHandle, identifier, Double.toString(value), attributes);
	}
	
	private AttributeHandleValueMap updateAttribute(ObjectClassHandle objectClassHandle, String identifier,
	                                                float value, AttributeHandleValueMap attributes)
	{
		return updateAttribute(objectClassHandle, identifier, Float.toString(value), attributes);
	}
	
	private AttributeHandleValueMap updateAttribute(ObjectClassHandle objectClassHandle, String identifier, 
	                                                String value, AttributeHandleValueMap attributes)
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
	 * This method will send out an interaction of the type FoodServed.DrinkServed. Any
	 * federates which are subscribed to it will receive a notification the next time
	 * they tick(). This particular interaction has no parameters, so you pass an empty
	 * map, but the process of encoding them is the same as for attributes.
	 */
	private void sendInteraction(String identifier, ParameterHandleValueMap parameters) throws RTIexception
	{
		InteractionClassHandle servedHandle = getHandleFromInteractionIdentifier( identifier );
		
		//////////////////////////
		// send the interaction //
		//////////////////////////
		rtiamb.sendInteraction( servedHandle, parameters, generateTag() );
		
		// if you want to associate a particular timestamp with the
		// interaction, you will have to supply it to the RTI. Here
		// we send another interaction, this time with a timestamp:
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );
		rtiamb.sendInteraction( servedHandle, parameters, generateTag(), time );
	}

	/**
	 * This method will request a time advance to the current time, plus the given
	 * timestep. It will then wait until a notification of the time advance grant
	 * has been received.
	 */
	private void advanceTime( double timestep ) throws RTIexception
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
	 * This method will attempt to delete the object instance of the given
	 * handle. We can only delete objects we created, or for which we own the
	 * privilegeToDelete attribute.
	 */
	private void deleteObject( ObjectInstanceHandle handle ) throws RTIexception
	{
		rtiamb.deleteObjectInstance( handle, generateTag() );
	}

	private short getTimeAsShort()
	{
		return (short)fedamb.federateTime;
	}

	private byte[] generateTag()
	{
		return ("(timestamp) "+System.currentTimeMillis()).getBytes();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

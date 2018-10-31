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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.util.RTIUtils;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;

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
	private IFederateImplementation federateImplementation;

	// Bits and pieces related to the RTI 
	private RTIambassador rtiamb;
	private FederateAmbassadorBase federateAmbassador; // created when we connect
	
	private RTIUtils rtiUtils;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateBase( IFederateImplementation federateImplementation,
	                     FederateConfiguration federateConfiguration )
	{
		// freeze the configuration now - probably it is already frozen, but since we want to
		// ensure that there are no modifications once it is used to set up the federate, we 
		// make sure that it really is frozen.
		this.federateConfiguration = federateConfiguration.freeze();
		this.federateImplementation = federateImplementation;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public FederateConfiguration getFederateConfiguration()
	{
		return this.federateConfiguration;
	}

	public IFederateImplementation getFederateImplementation()
	{
		return this.federateImplementation;
	}
	
	public RTIUtils getRTIUtils()
	{
		return this.rtiUtils;
	}
	
	public double getFederateTime()
	{
		return this.federateAmbassador.getFederateTime();
	}
	
	public InstanceBase getInstanceBase(ObjectInstanceHandle handle)
	{
		return this.federateAmbassador.getInstanceBase(handle);
	}
	
	public void registerInstanceBase(InstanceBase instanceBase)
	{
		this.federateAmbassador.registerInstanceBase(instanceBase);
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
		// main simulation loop
		while(federateImplementation.shouldContinueSimulation())
		{
			federateImplementation.tickSimulation();
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

	private void initializeAmbassadorAndConnect() throws RTIexception
	{
		logger.info( "Creating RTI Ambassador" );
		this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		// initialize the RTI utilities with the ambassador to provide simpler
		// access to a bunch of functionality
		this.rtiUtils = new RTIUtils(rtiamb);		

		logger.info( "Connecting RTI Ambassador..." );
		this.federateAmbassador = new FederateAmbassadorBase( this.rtiUtils, this.federateImplementation );
		this.rtiamb.connect( federateAmbassador, CallbackModel.HLA_EVOKED );
		logger.info( "RTI Ambassador is connected." );
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
		deleteObjects( this.federateAmbassador.getRegisteredInstanceHandles() );
		resignFromFederation();
	}

	private void registerSyncPoint( SyncPoint syncPoint, byte[] tag )
	    throws RTIexception
	{
		tag = tag == null ? new byte[0] : tag;
		// Note that if the point already been registered, there will be a callback saying this
		// failed, but as long as *someone* registered it everything is fine
		rtiamb.registerFederationSynchronizationPoint( syncPoint.getID(), tag );

		logger.info( String.format( "Federate '%s' registered synchronization point '%s'.",
		                            this.federateConfiguration.getFederateName(), syncPoint.toString() ) );
	}

	private void waitForSyncPointAnnouncement( SyncPoint syncPoint )
	    throws RTIexception
	{
		// TODO - Is it possible to get into a state whereby we never exit this loop...?
		// wait until the point is announced
		logger.info( String.format( "Federate '%s' is waiting for announcement of synchronization point '%s'",
		                            this.federateConfiguration.getFederateName(), syncPoint.toString() ) );

		while( syncPoint.isNot( federateAmbassador.announcedSyncPoint ) )
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
		logger.info( String.format( "Federate '%s' has achieved synchronization point '%s'",
		                            this.federateConfiguration.getFederateName(), syncPoint.toString() ) );
	}

	private void waitForFederationToAchieve( SyncPoint syncPoint )
	    throws RTIexception
	{
		logger.info( String.format( "Federate '%s' is waiting for federation '%s' to achieve synchronization point '%s'",
		                            this.federateConfiguration.getFederateName(), 
		                            this.federateConfiguration.getFederationName(),
		                            syncPoint.toString() ) );

		// TODO - Is it possible to get into a state whereby we never exit this loop...?
		// wait until the synchronization point is reached by the federation
		while( syncPoint.isNot( federateAmbassador.currentSyncPoint ) )
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
		String federateName = this.federateConfiguration.getFederateName();
		try
		{
			logger.info( String.format( "Federate '%s' is about to attempt to destroy federation '%s'...",
			                            federateName, federationName ) );
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
		HLAfloat64Interval lookahead = this.rtiUtils.makeHLAInterval( federateConfiguration.getLookAhead() );

		////////////////////////////
		// enable time regulation //
		////////////////////////////
		this.rtiamb.enableTimeRegulation( lookahead );

		// tick until we get the callback
		while( federateAmbassador.isRegulating() == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}

		/////////////////////////////
		// enable time constrained //
		/////////////////////////////
		this.rtiamb.enableTimeConstrained();

		// tick until we get the callback
		while( federateAmbassador.isConstrained() == false )
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
		FederateConfiguration config = this.federateConfiguration;
		
		rtiUtils.publishInteractionClasses(config.getPublishedInteractions());
		rtiUtils.publishObjectClassAttributes(config.getPublishedAttributes());
		
		rtiUtils.subscribeInteractionClasses(config.getSubscribedInteractions());
		rtiUtils.subscribeObjectClassessAttributes(config.getSubscribedAttributes());

		logger.debug( "Federate '%s' has initialized publications and subscriptions." );
	}

	/**
	 * This method will request a time advance to the current time, plus the given timestep. It
	 * will then wait until a notification of the time advance grant has been received.
	 */
	private void advanceTimeAndWait( double timestep )
	    throws RTIexception
	{
		// request the advance
		federateAmbassador.setAdvancing(true);
		double newTime = federateAmbassador.getFederateTime() + timestep;
		HLAfloat64Time time = this.rtiUtils.makeHLATime( newTime );
		rtiamb.timeAdvanceRequest( time );
		
		logger.debug( String.format( "Federate '%s' has requested a time advance to %s.",
		                             this.federateConfiguration.getFederateName(), newTime ) );

		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while( federateAmbassador.isAdvancing() )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
	}

	/**
	 * This method will attempt to delete the object instances with the given handles. We can only
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
			catch( DeletePrivilegeNotHeld e )
			{
				logger.warn( String.format( "Unable to delete object instance '%s' with handle %s: %s",
				                             rtiUtils.getObjectInstanceName( handle ), 
				                             handle, e.getMessage() ) );
			}
			catch( ObjectInstanceNotKnown | SaveInProgress | RestoreInProgress
				| FederateNotExecutionMember | NotConnected | RTIinternalError e )
			{
				logger.error( String.format( "Unable to delete object instance '%s' with handle %s: %s",
				                             rtiUtils.getObjectInstanceName( handle ), 
				                             handle, e.getMessage() ) );
			}
		}

		deleted.forEach( ( handle ) -> this.federateAmbassador.deregisterInstanceBase( handle ) );

		logger.info( String.format( "Federate '%s' deleted %d object(s).",
		                            this.federateConfiguration.getFederateName(), deleted.size() ) );
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

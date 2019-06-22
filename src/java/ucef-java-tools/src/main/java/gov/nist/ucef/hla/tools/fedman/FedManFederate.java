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
package gov.nist.ucef.hla.tools.fedman;

import java.net.URL;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.base.FederateAmbassador;
import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.ucef.NoOpFederate;
import gov.nist.ucef.hla.ucef.interactions.SimEnd;
import gov.nist.ucef.hla.ucef.interactions.SimPause;
import gov.nist.ucef.hla.ucef.interactions.SimResume;
import gov.nist.ucef.hla.ucef.interactions.SimStart;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;

/**
 *		            ___
 *		          _/   \_     _     _
 *		         / \   / \   / \   / \
 *		        ( U )─( C )─( E )─( F )
 *		         \_/   \_/   \_/   \_/
 *		        <─┴─> <─┴─────┴─────┴─>
 *		       Universal CPS Environment
 *		             for Federation
 * 		    ______         ____  ___
 * 		   / ____/__  ____/ /  |/  /___ _____
 * 		  / /_  / _ \/ __  / /\|_/ / __`/ __ \
 * 		 / __/ /  __/ /_/ / /  / / /_/ / / / /
 * 	    /_/    \___/\__,_/_/  /_/\__,_/_/ /_/
 * 	  ─────────── Federation Manager ───────────
 */
public class FedManFederate extends NoOpFederate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getLogger( FederateBase.class );
	
	private static final long ONE_SECOND = 1000;

	private static final double MAX_TIME_DEFAULT                       = Double.MAX_VALUE;
	private static final double LOGICAL_SECOND_DEFAULT                 = 1.0;
	private static final double REAL_TIME_MULTIPLIER_DEFAULT           = 1.0;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FedManStartRequirements startRequirements;

	private double logicalSecond;
	private long wallClockStepDelay;
	private double realTimeMultiplier;
	private long nextTimeAdvance;
	private double maxTime;

	private boolean isWaitingForFederates;

	private final Object mutex_lock = new Object();

	volatile boolean simShouldStart = false;
	volatile boolean simIsPaused = false;
	Lock lock = new ReentrantLock();
	Condition simIsPausedCondition = lock.newCondition();
	Condition simShouldStartCondition = lock.newCondition();
	Condition simShouldEndCondition = lock.newCondition();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor
	 *
	 * @param args command line arguments
	 */
	public FedManFederate()
	{
		super();

		this.maxTime = MAX_TIME_DEFAULT;
		this.startRequirements = new FedManStartRequirements( Collections.emptyMap() );
		this.logicalSecond = LOGICAL_SECOND_DEFAULT;
		this.realTimeMultiplier = REAL_TIME_MULTIPLIER_DEFAULT;
		this.wallClockStepDelay = (long)(ONE_SECOND / this.realTimeMultiplier);
		this.isWaitingForFederates = false;

		this.nextTimeAdvance = -1;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void setMaxTime( double maxTime )
	{
		this.maxTime = maxTime;
	}

	public double getMaxTime()
	{
		return this.maxTime;
	}

	public void setLogicalSecond( double logicalSecond )
	{
		this.logicalSecond = logicalSecond;
	}

	public double getLogicalSecond()
	{
		return this.logicalSecond;
	}

	public void setRealTimeMultiplier( double realTimeMultiplier )
	{
		this.realTimeMultiplier = realTimeMultiplier;
	}

	public double getRealTimeMultiplier()
	{
		return this.realTimeMultiplier;
	}

	public void setWallClockStepDelay( long wallClockStepDelay )
	{
		this.wallClockStepDelay = wallClockStepDelay;
	}

	public long getWallClockStepDelay()
	{
		return this.wallClockStepDelay;
	}

	public void setStartRequirements(FedManStartRequirements startRequirements)
	{
		this.startRequirements = startRequirements;
	}

	public FedManStartRequirements getStartRequirements()
	{
		return this.startRequirements;
	}

	public boolean isWaitingForFederates()
	{
		return this.isWaitingForFederates;
	}

	public boolean canStart()
	{
		return this.startRequirements.canStart();
	}

	public boolean hasStarted()
	{
		return this.simShouldStart;
	}

	public boolean hasEnded()
	{
		return this.simShouldEnd;
	}

	public boolean isPaused()
	{
		return hasStarted() && !hasEnded() && this.simIsPaused;
	}

	public boolean isRunning()
	{
		return hasStarted() && !hasEnded() && !this.simIsPaused;
	}

	/**
	 * Signal that the simulation should start.
	 *
	 * Has no effect if the simulation has already started)
	 */
	public void requestSimStart()
	{
		if( this.simShouldStart )
			return;

		lock.lock();
		try
		{
			this.simShouldStart = true;
			this.simShouldStartCondition.signal();
			sendSimStart();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Signal that the simulation should continue after being paused.
	 *
	 * Has no effect if the simulation is already continuing (i.e. is not paused) or if the
	 * simulation has already finished due to maximum time being reached or the simulation being
	 * (forcibly) exited.
	 */
	public void requestSimResume()
	{
		if( !this.simIsPaused )
			return;

		System.out.println( "Resuming..." );
		lock.lock();
		try
		{
			sendSimResume();
			this.simIsPaused = false;

			// reset time advancing baseline, otherwise it's left back at the
			// point we paused
			this.nextTimeAdvance = System.currentTimeMillis();
			this.simIsPausedCondition.signal();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Signal that the simulation should be paused.
	 *
	 * Has no effect if the simulation is already paused or if the simulation has already finished
	 * due to maximum time being reached or the simulation being (forcibly) exited.
	 */
	public void requestSimPause()
	{
		if( this.simIsPaused )
			return;

		System.out.println( "Pausing..." );
		lock.lock();
		try
		{
			sendSimPause();
			this.simIsPaused = true;
			this.simIsPausedCondition.signal();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Signal that the simulation should end.
	 *
	 * Has no effect if the simulation has already finished due to maximum time being reached or
	 * the simulation being (forcibly) exited.
	 */
	public void requestSimEnd()
	{
		if( this.simShouldEnd )
			return;

		System.out.println( "Ending..." );
		lock.lock();
		try
		{
			sendSimEnd();
			// make sure we un-pause before we end!
			if( this.simIsPaused )
			{
				this.simIsPaused = false;
				this.simIsPausedCondition.signal();
			}
			this.simShouldEnd = true;
			this.simShouldEndCondition.signal();
		}
		finally
		{
			lock.unlock();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Lifecycle Callback Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * We override this method to provide a wait on the command to start - apart from this
	 * difference, the method implementation is identical to the overridden method.
	 */
	@Override
	protected void federateSetup()
	{
		this.rtiamb = new RTIAmbassadorWrapper();
		this.fedamb = new FederateAmbassador( this );

		createAndJoinFederation();
		enableTimePolicy();

		publishAndSubscribe();

		beforeReadyToPopulate();
		synchronize( UCEFSyncPoint.READY_TO_POPULATE );

		//--------------------------------------------------------
		// wait for start command
		System.out.println( "Waiting for SimStart command..." );
		new Thread(new KeyboardReader()).start();
		waitUntilSimShouldStart();
		//--------------------------------------------------------

		beforeReadyToRun();
		synchronize( UCEFSyncPoint.READY_TO_RUN );

		beforeFirstStep();
	}
	
	/**
	 * We override this method because if the Federation Manager cannot create the required
	 * federation it's a failure and the Federation Manager must exit - apart from this
	 * difference, the method implementation is identical to the overridden method.
	 */
	@Override
	protected void createFederation()
	{
		String federationName = configuration.getFederationName();

		if( !configuration.canCreateFederation() )
		{
			// the federation manager *must* be allowed to create the required federation -
			// this is a configuration error (which, incidentally should not be possible,
			// since the permission is programmatically applied when the federation manager
			// is initialized).
			logger.error( "No permission to create federation {}. Federation manager *must* have " +
						  "permission to create the required federation. " + 
						  "Cannot proceed - exiting now.", federationName );
			System.exit( 1 );
		}

		URL[] modules = configuration.getModules().toArray( new URL[0] );
		try
		{
			// We attempt to create a new federation with the configured FOM modules
			logger.debug( "Creating federation '{}'...", federationName );
			// NOTE: here we are using the *actual* RTI Ambassador, since we are the Federation
			// Manager, and are therefore responsible for the full consequences involved in 
			// attempting to create a federation - no cotton wool padding here!
			rtiamb.getRtiAmbassador().createFederationExecution( federationName, modules );
			logger.debug( "Federation '{}' was created.", federationName );
		}
		catch( FederationExecutionAlreadyExists exists )
		{
			// For normal federates we would catch and deliberately ignore this exception,
			// since it just means that the federation was already created by someone else 
			// so we don't need to.
			// **HOWEVER** in the case of the Federation Manager, one of it's main roles is
			// the creation of the required federation, so if it already exists it means that
			// something has gone wrong, and we cannot proceed. If we *were* to proceed, we
			// would risk "competing" with another Federation Manager, or some other
			// unpredictable situation.
			logger.error( "The federation '{}' already exists. Is another Federation Manager " + 
						  "already running?", federationName );
			logger.error( "Cannot proceed - exiting now." );
			System.exit( 1 );
		}
		catch( Exception e )
		{
			// Something else has gone wrong - throw the exception on up
			logger.error( "Unable to create required federation {}.", federationName );
			logger.error( "Cannot proceed - exiting now." );
			if( logger.isDebugEnabled() )
			{
				e.printStackTrace();
			}
			System.exit( 1 );
		}

		logger.info( "Federation {} created.", federationName );
	}

	@Override
	public void beforeReadyToPopulate()
	{
		preAnnounceSyncPoints();

		System.out.println( configurationSummary() );
		System.out.println( "Waiting for federates to join..." );

		long count = 1;
		int lastJoinedCount = -1;
		int currentJoinedCount = startRequirements.joinedCount();

		// we are currently waiting for federates to join
		this.isWaitingForFederates = true;
		while( !startRequirements.canStart() )
		{
			currentJoinedCount = startRequirements.joinedCount();
			if( currentJoinedCount != lastJoinedCount )
			{
				System.out.println( "\n" + startRequirements.summary() );
				lastJoinedCount = currentJoinedCount;
				count = 1;
			}

			waitFor( ONE_SECOND );
			// show progress bar in seconds since last joined federate as...
			//     5    10   15   20   25   30   35   40   45   50   55   60 seconds
			// ─═─═┬═─═─┼─═─═┬═─═─┼─═─═┬═─═─╬─═─═┬═─═─┼─═─═┬═─═─┼─═─═┬═─═─╣
			System.out.print( count % 60 == 0 ? '╣' : count % 30 == 0 ? '╬' : count % 10 == 0 ? '┼' :
							  count % 5 == 0 ?  '┬' : count % 2 == 0 ?  '═' : '─' );
			if( count >= 60 )
			{
				System.out.print( '\n' );
				count = 0;
			}
			count++;
		}
		// we have all our required federates - we're not waiting any more
		this.isWaitingForFederates = false;

		System.out.println( String.format( "\n%d of %d federates have joined.",
		                                   startRequirements.totalFederatesRequired(),
		                                   startRequirements.totalFederatesRequired() ) );
		System.out.println( String.format( "Start requirements met - we are now %s.",
		                                   UCEFSyncPoint.READY_TO_POPULATE ) );
	}

	@Override
	public void beforeFirstStep()
	{
		this.nextTimeAdvance = System.currentTimeMillis();
	}

	/**
	 * We override the this method here so that we can react to
	 * the arrival of a {@link SimEnd} interaction by terminating
	 * the simulation loop, and {@link SimPause}/{@link SimResume}
	 * by halting/resuming the time advancement.
	 *
	 * Apart from this difference, {@link #federateExecution()} is
	 * identical to the {@link FederateBase#federateExecution()}
	 * implementation.
	 */
	@Override
	protected void federateExecution()
	{
		while( simShouldEnd == false )
		{
			// next step, and cease simulation loop if step() returns false
			if( simShouldEnd || step( fedamb.getFederateTime() ) == false )
				break;

			waitWhileSimIsPaused();

			if( simShouldEnd == false )
				advanceTime();
		}
		System.out.println( "Federate execution has finished.");
	}

	@Override
	public boolean step( double currentTime )
	{
		double federateTime = fedamb.getFederateTime();
		System.out.println( String.format( "Federate time is %.3f ", federateTime ) );

		if(currentTime < maxTime)
		{
			this.nextTimeAdvance += wallClockStepDelay;
			waitUntil( this.nextTimeAdvance );
			if(!simShouldEnd)
			{
				System.out.println( String.format( "Advancing time to %.3f...",
				                                   (federateTime + configuration.getLookAhead() ) ) );
			}
			return !simShouldEnd;
		}

		System.out.println( "Maximum simulation time reached.");

		sendSimEnd();

		return false;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveObjectRegistration( HLAObject hlaObject )
	{
		String objectClassName = hlaObject.getObjectClassName();
		if( FedManConstants.HLAFEDERATE_OBJECT_CLASS_NAME.equals( objectClassName ) )
		{
			rtiamb.requestAttributeValueUpdate( hlaObject, FedManConstants.HLAFEDERATE_ATTRIBUTE_NAMES );
		}
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject )
	{
		synchronized(mutex_lock)
		{
    		String objectClassName = hlaObject.getObjectClassName();
    		if( FedManConstants.HLAFEDERATE_OBJECT_CLASS_NAME.equals( objectClassName ) )
    		{
        		FederateDetails joinedFederate = new FederateDetails( hlaObject );
        		String federateType = joinedFederate.getFederateType();
        		String federateName = joinedFederate.getFederateName();
        		String selfType = configuration.getFederateType();
        		String selfName = configuration.getFederateName();
        		if( selfType.equals( federateType ) &&
        			selfName.equals( federateName ) )
        		{
        			// ignore ourself joining...
        			return;
        		}
        		startRequirements.federateJoined( joinedFederate );
    		}
		}
	}

	@Override
	public void receiveObjectDeleted( HLAObject hlaObject )
	{
		synchronized(mutex_lock)
		{
    		String objectClassName = hlaObject.getObjectClassName();
    		if( FedManConstants.HLAFEDERATE_OBJECT_CLASS_NAME.equals( objectClassName ) )
    		{
        		FederateDetails departedFederate = new FederateDetails( hlaObject );
        		startRequirements.federateDeparted( departedFederate );
    		}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////// UCEF Simulation Control Interaction Transmission //////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to emit a {@link SimStart} interaction to the federation when the simulation
	 * is terminated
	 */
	private void sendSimStart()
	{
		sendInteraction( new SimStart() );
	}

	/**
	 * Utility method to emit a {@link SimEnd} interaction to the federation when the simulation
	 * is terminated
	 */
	private void sendSimEnd()
	{
		sendInteraction( new SimEnd() );
	}

	/**
	 * Utility method to emit a {@link SimPause} interaction to the federation when the simulation
	 * is paused
	 */
	private void sendSimPause()
	{
		sendInteraction( new SimPause() );
	}

	/**
	 * Utility method to emit a {@link SimResume} interaction to the federation when the simulation
	 * is resumed
	 */
	private void sendSimResume()
	{
		sendInteraction( new SimResume() );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create a human readable summary of the federate manager's configuration
	 *
	 * @return a human readable summary of the federate manager's configuration
	 */
	private String configurationSummary()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( center( " Federate Manager Details ", 80, '═' ) );
		builder.append( "\n" );
		builder.append( "Time:" );
		builder.append( "\n" );
		builder.append( String.format( "\tLogical step of %.2f = %.2f real time seconds.",
		                               this.logicalSecond, 1.0 ) );
		builder.append( "\n" );
		builder.append( "\tRunning " );
		if( this.realTimeMultiplier != 1.0 )
		{
			builder.append( String.format( "%.2f× %s than ",
			                               this.realTimeMultiplier < 1.0 ? "slower" : "faster",
			                               (this.realTimeMultiplier) ) );
		}
		else
		{
			builder.append( "in " );
		}
		builder.append( "real time (");

		builder.append( String.format( "one logical step of %.2f every %d milliseconds.",
		                               configuration.getLookAhead(), this.wallClockStepDelay) );
		builder.append( ")" );
		builder.append( "\n" );
		builder.append( "Start Requirements:" );
		builder.append( "\n" );
		builder.append( startRequirements.summary() );
		return builder.toString();
	}

	/**
	 * Pre-announce all UCEF synchronization points.
	 */
	private void preAnnounceSyncPoints()
	{
		for( UCEFSyncPoint syncPoint : UCEFSyncPoint.values() )
		{
			registerSyncPoint( syncPoint.getLabel(), null );
			waitForSyncPointAnnouncement( syncPoint.getLabel() );
		}
	}

	/**
	 * Utility method to cause execution to wait for the given duration
	 *
	 * @param duration the duration to wait in milliseconds
	 */
	private void waitFor( long duration )
	{
		if( duration <= 0 )
			return;

		try
		{
			Thread.sleep( duration );
		}
		catch( InterruptedException e )
		{
			throw new UCEFException( "Could not wait", e );
		}
	}

	/**
	 * Utility method to cause execution to wait until the given timestamp is reached
	 *
	 * @param timestamp the time to wait until (as system clock time in milliseconds)
	 */
	private void waitUntil( long timestamp )
	{
		waitFor( timestamp - System.currentTimeMillis() );
	}

	/**
	 * Wait until the simulation should start (see also
	 * {@link FedManFederate#requestSimStart()}
	 */
	private void waitUntilSimShouldStart()
	{
		lock.lock();
		try
		{
			while( !simShouldStart )
				simShouldStartCondition.await();
		}
		catch(InterruptedException e)
		{
			// ignore and continue
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Wait until the simulation should end (see also
	 * {@link FedManFederate#requestSimEnd()}
	 */
	@SuppressWarnings("unused")
	private void waitUntilSimShouldEnd()
	{
		lock.lock();
		try
		{
			while( !simShouldEnd )
				simShouldEndCondition.await();
		}
		catch(InterruptedException e)
		{
			// ignore and continue
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Wait until the simulation should continue (see also
	 * {@link FedManFederate#requestSimResume()}
	 */
	private void waitWhileSimIsPaused()
	{
		lock.lock();
		try
		{
			while( simIsPaused )
				simIsPausedCondition.await();
		}
		catch(InterruptedException e)
		{
			// ignore and continue
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Utility method to repeat a string a given number of times.
	 *
	 * @param str the string to repeat
	 * @param count the number of repetitions
	 * @return the repeated string
	 */
	private String repeat( String str, int count )
	{
		return IntStream.range( 0, count ).mapToObj( i -> str ).collect( Collectors.joining( "" ) );
	}

	/**
	 * Utility method to center a string in a given width
	 *
	 * @param str the string to center
	 * @param width the width to center the string in
	 * @param padding the padding character to use to the left and right of the string
	 * @return the centered string
	 */
	private String center( String str, int width, char padding )
	{
		int count = width - str.length();
		if( count <= 0 )
			return str;

		String leftPad = repeat( Character.toString( padding ), count / 2 );
		if( count % 2 == 0 )
			return leftPad + str + leftPad;

		return leftPad + str + leftPad.substring( 0, count + 1 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// PRIVATE CLASSES /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	private class KeyboardReader implements Runnable
	{
		@Override
		public void run()
		{
			Scanner sc = new Scanner( System.in );
			while( !simShouldEnd )
			{
				System.out.print("> ");
				String input = sc.nextLine();

				if( input.length() == 0 )
				{
					if( !simShouldStart )
						startSim();
					else
						toggleSimPause();
				}
				else
				{
					char cmdChar = input.charAt( 0 );
					switch( cmdChar )
					{
						case 's':
							startSim();
							break;
						case 'p':
						case ' ':
							toggleSimPause();
							break;
						case 'x':
						case 'q':
							endSim();
							break;
						default:
							System.out.println( "Unknown command " + cmdChar );
					}
				}
			}
			sc.close();
		}

		private void startSim()
		{
			if( !simShouldStart )
			{
				System.out.println( "Starting..." );
				requestSimStart();
			}
			else
			{
    			System.out.println( "Simulation has already started." );
			}
		}

		private void endSim()
		{
			if( !simShouldStart )
			{
				System.out.println( "Cannot terminate - simulation has not yet started." );
			}
			else if( simShouldEnd )
			{
				System.out.println( "Simulation is already terminating." );
			}
			else
			{
    			System.out.println( "Terminating..." );
    			if( simIsPaused )
    				requestSimResume();
    			requestSimEnd();
			}
		}

		private void toggleSimPause()
		{
			if( simShouldStart )
			{
    			if( simIsPaused )
    			{
    				requestSimResume();
    			}
    			else
    			{
    				requestSimPause();
    			}
			}
			else
			{
    			System.out.println( "Cannot pause/resume - simulation has not yet started." );
			}
		}
	}
}

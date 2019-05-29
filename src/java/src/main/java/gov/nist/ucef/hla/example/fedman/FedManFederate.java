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
package gov.nist.ucef.hla.example.fedman;

import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gov.nist.ucef.hla.base.FederateAmbassador;
import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.ucef.NoOpUCEFFederate;
import gov.nist.ucef.hla.ucef.interaction.SimEnd;
import gov.nist.ucef.hla.ucef.interaction.SimPause;
import gov.nist.ucef.hla.ucef.interaction.SimResume;
import gov.nist.ucef.hla.ucef.interaction.SimStart;
import gov.nist.ucef.hla.util.Constants;
import gov.nist.ucef.hla.util.StringUtils;
import gov.nist.ucef.hla.util.cmdargs.ArgException;

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
public class FedManFederate extends NoOpUCEFFederate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final long ONE_SECOND = 1000;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FedManCmdLineProcessor argProcessor;
	private FedManStartRequirements startRequirements;
	
	private double logicalSecond;
	private long wallClockStepDelay;
	private double realTimeMultiplier;
	private long nextTimeAdvance;
	private double maxTime;
	
	private final Object mutex_lock = new Object();
	
	volatile boolean simShouldStart = false;
	volatile boolean simIsPaused = false;
	Lock lock = new ReentrantLock();
	Condition simIsPausedCondition = lock.newCondition();
	Condition simShouldStartCondition = lock.newCondition();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor
	 * 
	 * @param args command line arguments
	 */
	public FedManFederate( String[] args )
	{
		super();
		
		argProcessor = new FedManCmdLineProcessor( FedManConstants.EXEC_NAME, Constants.CONSOLE_WIDTH );
        try
		{
			argProcessor.processArgs( args );
		}
		catch( ArgException e )
		{
			System.err.println( e.getMessage() );
			System.out.println( "======= Usage:\n" + argProcessor.getUsage() );
			System.out.println( "===== Options:\n" + argProcessor.getHelp() );
			System.out.println( "Cannot proceed. Exiting now." );
			System.exit( 1 );
		}
        
		this.maxTime = 30.0;
		this.nextTimeAdvance = -1;
		
		this.startRequirements = new FedManStartRequirements( argProcessor.startRequirements() );
		this.logicalSecond = argProcessor.logicalSecond();
		this.realTimeMultiplier = argProcessor.realTimeMultiplier();
		this.wallClockStepDelay = argProcessor.wallClockStepDelay();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
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
		
		lock.lock();
		try
		{
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
	
	@Override
	public void beforeFederationJoin()
	{
		// update the federate name, type and federation execution name in 
		// accordance with the values obtained from the command line args
		configuration.setFederateName( argProcessor.federateName() );
		configuration.setFederateType( argProcessor.federateType() );
		configuration.setFederationName( argProcessor.federationExecName() );
		// update the configuration lookahead value so that it is the same
		// logical step size as obtained from the command line args
		configuration.setLookAhead( argProcessor.logicalStepSize() );
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
        		if( FedManConstants.FEDMAN_FEDERATE_TYPE.equals( federateType ) )
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
		builder.append( StringUtils.center( " Federate Manager Details ", 80, '═' ) );
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
			if( simShouldStart )
			{
    			System.out.println( "Terminating..." );
    			simShouldEnd = true;
    			if( simIsPaused )
    				requestSimResume();
    			sendSimEnd();
			}
			else
			{
    			System.out.println( "Cannot terminate - simulation has not yet started." );
			}
		}
		
		private void toggleSimPause()
		{
			if( simShouldStart )
			{
    			if( simIsPaused )
    			{
    				System.out.println( "Resuming..." );
    				requestSimResume();
    				sendSimResume();
    			}
    			else
    			{
    				System.out.println( "Pausing..." );
    				simIsPaused = true;
    				sendSimPause();
    			}
			}
			else
			{
    			System.out.println( "Cannot pause/resume - simulation has not yet started." );
			}
		}
	}
}

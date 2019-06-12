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
package gov.nist.ucef.hla.ucef;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.ucef.interactions.SimEnd;
import gov.nist.ucef.hla.ucef.interactions.SimPause;
import gov.nist.ucef.hla.ucef.interactions.SimResume;
import gov.nist.ucef.hla.ucef.interactions.SimStart;
import hla.rti1516e.InteractionClassHandle;

/**
 * An abstract implementation for a UCEF Federate which is aware of certain
 * UCEF specific simulation control interactions.
 *
 * It provides default handlers for them, but more notably provides a
 * {@link #federateExecution()} implementation which is aware of the
 * receipt of {@link SimEnd} simulation control interactions.
 *
 * It terminates the simulation loop when...
 * <ul>
 * <li>a {@link SimEnd} is received, or...</li>
 * <li>when the {@link #step(double)} method returns false</li>
 * </ul>
 * ... whichever comes first.
 */
public abstract class UCEFFederateBase extends FederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getLogger( UCEFFederateBase.class );

	// if a federate joins after the READY_TO_RUN and READY_TO_POPULATE sync points
	// have already been achieved by the federation, it is a "late joiner" and does
	// not need to wait for the SimStart even to enter the simulation loop (because
	// the rest of the simulation has already started).
	private static final UCEFSyncPoint[] LATE_JOIN_SYNC_POINTS = { UCEFSyncPoint.READY_TO_RUN,
	                                                               UCEFSyncPoint.READY_TO_POPULATE
	                                                             };

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private final Object mutex_lock = new Object();

	protected Set<String> syncPointTimeouts;

	// flag which becomes true after a SimStart interaction has
	// been received (begins as false)
	protected volatile boolean simShouldStart;
	// flag which becomes true after a SimEnd interaction has
	// been received (begins as false)
	protected volatile boolean simShouldEnd;
	// flag which becomes true after a SimPause interaction has
	// been received, and false after a SimResume interaction
	// has been received (begins as false)
	protected volatile boolean simShouldPause;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UCEFFederateBase()
	{
		super();

		syncPointTimeouts = new HashSet<>();

		simShouldStart = false;
		simShouldEnd = false;
		simShouldPause = false;
	}

	//----------------------------------------------------------
	//         UCEF SPECIFIC INTERACTION CALLBACK METHODS
	//----------------------------------------------------------
	/**
	 * Called whenever the UCEF specific "simulation start" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simStart the {@link SimStart} interaction
	 */
	protected abstract void receiveSimStart( SimStart simStart );

	/**
	 * Called whenever the UCEF specific "simulation start" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simStart the {@link SimStart} interaction
	 * @param time the current logical time of the federate
	 */
	protected abstract void receiveSimStart( SimStart simStart, double time );

	/**
	 * Called whenever the UCEF specific "simulation end" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simEnd the {@link SimEnd} interaction
	 */
	protected abstract void receiveSimEnd( SimEnd simEnd );

	/**
	 * Called whenever the UCEF specific "simulation end" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simEnd the {@link SimEnd} interaction
	 * @param time the current logical time of the federate
	 */
	protected abstract void receiveSimEnd( SimEnd simEnd, double time );

	/**
	 * Called whenever the UCEF specific "simulation pause" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simPause the {@link SimPause} interaction
	 */
	protected abstract void receiveSimPause( SimPause simPause );

	/**
	 * Called whenever the UCEF specific "simulation pause" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simPause the {@link SimPause} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected abstract void receiveSimPause( SimPause simPause, double time );

	/**
	 * Called whenever the UCEF specific "simulation resume" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simResume the {@link SimResume} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected abstract void receiveSimResume( SimResume simResume );

	/**
	 * Called whenever the UCEF specific "simulation resume" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simResume the {@link SimResume} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected abstract void receiveSimResume( SimResume simResume, double time );

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * We override the this method here so that we can react to
	 * the arrival of a {@link SimEnd} interaction by terminating
	 * the simulation loop
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
			if( simShouldEnd == false)
				advanceTime();
		}
	}

	/**
	 * We override the this method here so that late joining UCEF federates
	 * can wait for a synchronization point, but time out so they don't lock
	 * up indefinitely (because the federation has already announced and
	 * achieved the synchronization point in the past)
	 *
	 * @param label the synchronization point label
	 */
	@Override
	protected void waitForSyncPointAchievement( String label )
	{
		// TODO -------------------------------------------------------
		// NOTE: This is placeholder code until the Portico updates
		//       are finalized to support querying of synchronization
		//       labels and statuses which will allow this to be 
		//       handled far more effectively.
		
		//       It's possible that the timeout may be kept (in 
		//       addition to the mechanism of directly querying
		//       synchronization point status), but as it creates the
		//       unfortunate side effect of allowing a federate to
		//       potentially "jump ahead" due to an extended (but 
		//       legitimate) delay in a synch point being achieved,
		//       it's likely that it will be removed entirely.
		// NOTE: if we keep this timeout, obtaining a timeout value
		//       from the the federate configuration (including 
		//       "infinite"/wait forever option) would be desirable.
		// TODO -------------------------------------------------------
		long timeoutDuration = 15000;
		long timeoutTime = System.currentTimeMillis() + timeoutDuration;
		boolean hasTimedOut = false;

		while( !fedamb.isAchieved( label ) && !hasTimedOut )
		{
			evokeMultipleCallbacks();
			hasTimedOut = System.currentTimeMillis() > timeoutTime;
		}

		if( hasTimedOut )
		{
			syncPointTimeouts.add( label );

			logger.warn( String.format( "Timed out after %.3f seconds while waiting to achieve "+
										"synchronization point '%s'",
										(timeoutDuration / 1000.0),
										label ) );
		}
	}

	/**
	 * Override to provide handling for specific UCEF simulation control interaction types
	 */
	@Override
	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters, double time )
	{
		synchronized( mutex_lock )
		{
    		// delegate to handlers for UCEF Simulation control interactions as required
    		HLAInteraction interaction = makeInteraction( handle, parameters );

    		if( interaction != null )
    		{
        		String interactionClassName = interaction.getInteractionClassName();

        		if( SimStart.interactionName().equals( interactionClassName ) )
        		{
        			// it is up to individual federates as to how they handle this
        			simShouldStart = true;
        			simShouldPause = false;
        			receiveSimStart( new SimStart( interaction ), time );
        		}
        		if( SimEnd.interactionName().equals( interactionClassName ) )
        		{
        			// if a SimEnd is received, a well behaved UCEF federate must
        			// synchronize with the rest of the federation before resigning
        			this.configuration.setSyncBeforeResign( true );

        			simShouldEnd = true;
        			simShouldPause = false;
        			receiveSimEnd( new SimEnd( interaction ), time );
        		}
        		else if( SimPause.interactionName().equals( interactionClassName ) )
        		{
        			// if a SimPause is received, a well behaved UCEF federate must
        			// cease its step() loop processing until a SimResume or
        			// SimEnd is received
        			simShouldPause = true;
        			receiveSimPause( new SimPause( interaction ), time );
        		}
        		else if( SimResume.interactionName().equals( interactionClassName ) )
        		{
        			// if a SimResume is received, a well behaved UCEF federate may
        			// resume its step() loop processing
        			simShouldPause = false;
        			receiveSimResume( new SimResume( interaction ), time );
        		}
        		else
        		{
        			// anything else gets generic interaction receipt handling
        			receiveInteraction( interaction, time );
        		}
    		}
		}
	}

	@Override
	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters )
	{
		synchronized( mutex_lock )
		{
    		// delegate to handlers for UCEF Simulation control interactions as required
    		HLAInteraction interaction = makeInteraction( handle, parameters );

    		if( interaction != null )
    		{
        		String interactionClassName = interaction.getInteractionClassName();

        		if( SimStart.interactionName().equals( interactionClassName ) )
        		{
        			// it is up to individual federates as to how they handle this
        			simShouldStart = true;
        			receiveSimStart( new SimStart( interaction ) );
        		}
        		else if( SimEnd.interactionName().equals( interactionClassName ) )
        		{
        			// if a SimEnd is received, a well behaved UCEF federate must
        			// synchronize with the rest of the federation before resigning
        			this.configuration.setSyncBeforeResign( true );

        			simShouldEnd = true;
        			receiveSimEnd( new SimEnd( interaction ) );
        		}
        		else if( SimPause.interactionName().equals( interactionClassName ) )
        		{
        			// if a SimPause is received, a well behaved UCEF federate must
        			// cease its step() loop processing until a SimResume or
        			// SimEnd is received
        			simShouldPause = true;
        			receiveSimPause( new SimPause( interaction ) );
        		}
        		else if( SimResume.interactionName().equals( interactionClassName ) )
        		{
        			// if a SimResume is received, a well behaved UCEF federate may
        			// resume its step() loop processing
        			simShouldPause = false;
        			receiveSimResume( new SimResume( interaction ) );
        		}
        		else
        		{
        			// anything else gets generic interaction receipt handling
        			receiveInteraction( interaction );
        		}
    		}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Determine if this federate is a "late joiner"
	 *
	 * @return true if the federate is a late joiner, false otherwise
	 */
	protected boolean isLateJoiner()
	{
		// for a federate to be a late joiner, it must have timed out
		// trying to achieve *all* sync points corresponding to before
		// the simulation starts
		for(UCEFSyncPoint syncPoint : LATE_JOIN_SYNC_POINTS)
		{
			if(!this.syncPointTimeouts.contains( syncPoint.getLabel() ))
			{
				// didn't time out on this sync point - not a late joiner
				return false;
			}
		}

		// timed out on all relevant sync points - we are late to the federation
		return true;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

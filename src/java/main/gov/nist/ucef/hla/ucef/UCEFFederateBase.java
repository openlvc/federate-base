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

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.NoOpFederateBase;
import gov.nist.ucef.hla.ucef.interaction.FederateJoin;
import gov.nist.ucef.hla.ucef.interaction.SimEnd;
import gov.nist.ucef.hla.ucef.interaction.SimPause;
import gov.nist.ucef.hla.ucef.interaction.SimResume;
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
public abstract class UCEFFederateBase extends NoOpFederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// cached handles for determination of received interaction types
	private InteractionClassHandle federateJoinHandle;
	private InteractionClassHandle simPauseHandle;
	private InteractionClassHandle simResumeHandle;
	private InteractionClassHandle simEndHandle;

	// flag for storing whether a SimEnd interaction has been received
	protected boolean simEndReceived;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UCEFFederateBase()
	{
		super();
		
		this.simEndReceived = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void beforeReadyToPopulate()
	{
		super.beforeReadyToPopulate();

		// cache UCEF simulation control handles to avoid looking 
		// them up repeatedly later on. Note that we can't do this
		// any earlier (in the constructor, for example) since the
		// RTI Ambassador is not initialized until the federation
		// has been joined.
		federateJoinHandle = rtiamb.getInteractionClassHandle( FederateJoin.interactionName() );
		simPauseHandle = rtiamb.getInteractionClassHandle( SimPause.interactionName() );
		simResumeHandle = rtiamb.getInteractionClassHandle( SimResume.interactionName() );
		simEndHandle = rtiamb.getInteractionClassHandle( SimEnd.interactionName() );		
	}
	
	/**
	 * We override the this method here so that we can react to
	 * the arrival of a {@link SimEnd} interaction by terminating
	 * the simulation loop.
	 * 
	 * Apart from this difference, {@link #federateExecution()} is 
	 * identical to the {@link FederateBase#federateExecution()}
	 * implementation. 
	 */
	@Override
	protected void federateExecution()
	{
		double currentTime = 0.0;
		double timeStep = this.configuration.getLookAhead();

		// the while loop will continue until a SimEnd is received, or
		// the `step()` (method called within the loop) returns false		
		while( this.simEndReceived == false )
		{
			currentTime = fedamb.getFederateTime();

			// next step
			if( step( currentTime ) == false )
			{
				// cease simulation loop when step() returns false
				break;
			}

			// advance, or tick, or nothing!
			if( this.configuration.isTimeStepped() )
				advanceTime( currentTime + timeStep );
			else if( this.configuration.callbacksAreEvoked() )
				evokeMultipleCallbacks();
			else
				;
		}
	}
	
	/**
	 * Override to provide handling for specific UCEF simulation control interaction types
	 */
	@Override
	public void incomingInteraction( HLAInteraction interaction, double time )
	{
		InteractionClassHandle interactionKind = rtiamb.getInteractionClassHandle( interaction );
		// delegate to handlers for UCEF Simulation control interactions as required
		if( interactionKind.equals( simPauseHandle ) )
			receiveSimPause( new SimPause( interaction ), time );
		else if( interactionKind.equals( simResumeHandle ) )
			receiveSimResume( new SimResume( interaction ), time );
		else if( interactionKind.equals( simEndHandle ) )
			receiveSimEnd( new SimEnd( interaction ), time );
		else if( interactionKind.equals( federateJoinHandle ) )
			receiveFederateJoin( new FederateJoin( interaction ), time );

		// anything else gets generic interaction receipt handling
		receiveInteraction( interaction, time );
	}
	
	@Override
	public void incomingInteraction( HLAInteraction interaction )
	{
		InteractionClassHandle interactionKind = rtiamb.getInteractionClassHandle( interaction );
		// delegate to handlers for UCEF Simulation control interactions as required
		if( interactionKind.equals( simPauseHandle ) )
			receiveSimPause( new SimPause( interaction ) );
		else if( interactionKind.equals( simResumeHandle ) )
			receiveSimResume( new SimResume( interaction ) );
		else if( interactionKind.equals( simEndHandle ) )
			receiveSimEnd( new SimEnd( interaction ) );
		else if( interactionKind.equals( federateJoinHandle ) )
			receiveFederateJoin( new FederateJoin( interaction ) );

		// anything else gets generic interaction receipt handling
		receiveInteraction( interaction );
	}

	//----------------------------------------------------------
	//         UCEF SPECIFIC INTERACTION CALLBACK METHODS
	//----------------------------------------------------------
	/**
	 * Called whenever the UCEF specific "simulation pause" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simPause the {@link SimPause} interaction
	 */
	protected void receiveSimPause( SimPause simPause )
	{
		// override this method to provide specific handling
	}

	/**
	 * Called whenever the UCEF specific "simulation pause" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simPause the {@link SimPause} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected void receiveSimPause( SimPause simPause, double time )
	{
		// delegate to handler with no time parameter as the default behaviour
		// override this method to provide specific handling
		receiveSimPause( simPause );
	}

	/**
	 * Called whenever the UCEF specific "simulation resume" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simResume the {@link SimResume} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected void receiveSimResume( SimResume simResume )
	{
		// override this method to provide specific handling
	}

	/**
	 * Called whenever the UCEF specific "simulation resume" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simResume the {@link SimResume} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected void receiveSimResume( SimResume simResume, double time )
	{
		// delegate to handler with no time parameter as the default behaviour
		// override this method to provide specific handling
		receiveSimResume( simResume );
	}

	/**
	 * Called whenever the UCEF specific "simulation end" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simEnd the {@link SimEnd} interaction
	 */
	protected void receiveSimEnd( SimEnd simEnd )
	{
		this.simEndReceived = true;
	}

	/**
	 * Called whenever the UCEF specific "simulation end" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simEnd the {@link SimEnd} interaction
	 * @param time the current logical time of the federate
	 */
	protected void receiveSimEnd( SimEnd simEnd, double time )
	{
		// delegate to handler with no time parameter as the default behaviour
		// override this method to provide specific handling
		receiveSimEnd( simEnd );
	}

	/**
	 * Called whenever the UCEF specific "federate join" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param federateJoin the {@link FederateJoin} interaction
	 */
	protected void receiveFederateJoin( FederateJoin federateJoin )
	{
		// override this method to provide specific handling
	}
	
	/**
	 * Called whenever the UCEF specific "federate join" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param federateJoin the {@link FederateJoin} interaction
	 * @param time the current logical time of the federate
	 */
	protected void receiveFederateJoin( FederateJoin federateJoin, double time )
	{
		// delegate to handler with no time parameter as the default behaviour
		// override this method to provide specific handling
		receiveFederateJoin( federateJoin );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

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

import java.util.Map;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.HLAInteraction;
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

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
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
		
		simShouldEnd = false;
		simShouldPause = false;
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
	 * Called whenever the UCEF specific "federate join" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param federateJoin the {@link FederateJoin} interaction
	 */
	protected abstract void receiveFederateJoin( FederateJoin federateJoin );
	
	/**
	 * Called whenever the UCEF specific "federate join" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param federateJoin the {@link FederateJoin} interaction
	 * @param time the current logical time of the federate
	 */
	protected abstract void receiveFederateJoin( FederateJoin federateJoin, double time );
	
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
	 * Override to provide handling for specific UCEF simulation control interaction types
	 */
	@Override
	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters, double time )
	{
		// delegate to handlers for UCEF Simulation control interactions as required
		HLAInteraction interaction = makeInteraction( handle, parameters );
		
		if( interaction != null )
		{
    		String interactionClassName = interaction.getInteractionClassName();
    		
    		if( SimEnd.interactionName().equals( interactionClassName ) )
    		{
    			simShouldEnd = true;
    			receiveSimEnd( new SimEnd( interaction ), time );
    		}
    		else if( SimPause.interactionName().equals( interactionClassName ) )
    		{
    			simShouldPause = true;
    			receiveSimPause( new SimPause( interaction ), time );
    		}
    		else if( SimResume.interactionName().equals( interactionClassName ) )
    		{
    			simShouldPause = false;
    			receiveSimResume( new SimResume( interaction ), time );
    		}
    		else if( FederateJoin.interactionName().equals( interactionClassName ) )
    		{
    			receiveFederateJoin( new FederateJoin( interaction ), time );
    		}
    		else
    		{
    			// anything else gets generic interaction receipt handling
    			receiveInteraction( interaction, time );
    		}
		}
		else
		{
			// TODO Log error
		}
	}
	
	@Override
	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters )
	{
		// delegate to handlers for UCEF Simulation control interactions as required
		HLAInteraction interaction = makeInteraction( handle, parameters );
		
		if( interaction != null )
		{
    		String interactionClassName = interaction.getInteractionClassName();
    		
    		if( SimEnd.interactionName().equals( interactionClassName ) )
    		{
    			simShouldEnd = true;
    			receiveSimEnd( new SimEnd( interaction ) );
    		}
    		else if( SimPause.interactionName().equals( interactionClassName ) )
    		{
    			simShouldPause = true;
    			receiveSimPause( new SimPause( interaction ) );
    		}
    		else if( SimResume.interactionName().equals( interactionClassName ) )
    		{
    			simShouldPause = false;
    			receiveSimResume( new SimResume( interaction ) );
    		}
    		else if( FederateJoin.interactionName().equals( interactionClassName ) )
    		{
    			receiveFederateJoin( new FederateJoin( interaction ) );
    		}
    		else
    		{
    			// anything else gets generic interaction receipt handling
    			receiveInteraction( interaction );
    		}
		}
		else
		{
			// TODO Log error
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

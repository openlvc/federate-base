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
package gov.nist.ucef.hla.example.smart.helpers;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.example.smart.interactions.Pong;
import gov.nist.ucef.hla.example.smart.reflections.Player;
import gov.nist.ucef.hla.ucef.NoOpFederate;

/**
 *		            ___
 *		          _/   \_     _     _
 *		         / \   / \   / \   / \
 *		        ( U )─( C )─( E )─( F )
 *		         \_/   \_/   \_/   \_/
 *		        <─┴─> <─┴─────┴─────┴─>
 *		       Universal CPS Environment
 *		             for Federation
 * 
 * Example federate for testing
 */
public abstract class _SmartPingFederate extends NoOpFederate
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public _SmartPingFederate()
	{
		super();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction )
	{
		String interactionClassName = hlaInteraction.getInteractionClassName(); 
		if( Pong.interactionClassName().equals( interactionClassName ) )
		{
			receivePongInteraction( new Pong( hlaInteraction ) );
		}
		else
		{
			// this is unexpected - we shouldn't receive any thing we didn't subscribe to
			System.err.println( String.format( "Received an unexpected interaction of type '%s'",
			                                   interactionClassName ) );
		}
	}
	
	@Override
	public void receiveAttributeReflection( HLAObject hlaObject ) 
	{ 
		String objectClassName = hlaObject.getObjectClassName(); 
		if( Player.objectClassName().equals( objectClassName ) )
		{
			receivePlayerUpdate( new Player( hlaObject ) );
		}
		else
		{
			// this is unexpected - we shouldn't receive any thing we didn't subscribe to
			System.err.println( String.format( "Received an unexpected attribute reflection of type '%s'",
			                                   objectClassName ) );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public Player register( Player player ) { return (Player)super.register( player ); }

	/**
	 * Handle receipt of a {@link Pong}
	 * 
	 * @param pong the interaction to handle
	 */
	protected abstract void receivePongInteraction( Pong pong );

	/**
	 * Handle receipt of a {@link Player}
	 * 
	 * @param player the object to handle
	 */
	protected abstract void receivePlayerUpdate( Player player );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

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
package gov.nist.hla.genx;

import gov.nist.hla.genx.base._GenxPongFederate;
import gov.nist.hla.genx.interactions.Ping;
import gov.nist.hla.genx.interactions.Pong;
import gov.nist.hla.genx.reflections.Player;

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
 */
public class GenxPongFederate extends _GenxPongFederate
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private char letter;
	private Player player;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public GenxPongFederate( String[] args )
	{
		super();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Lifecycle Callback Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void beforeFirstStep()
	{
		/////////////////////////////////////////////////////////////////
		// INITIALIZE SIMULATION VALUES AS REQUIRED
		/////////////////////////////////////////////////////////////////
		this.letter = '0';
		this.player = register( new Player() );
	}

	@Override
	public boolean step( double currentTime )
	{
		/////////////////////////////////////////////////////////////////
		// INSERT SIMULATION LOGIC HERE
		/////////////////////////////////////////////////////////////////
		System.out.println( "Tick... " + currentTime );
		// return true to continue simulation loop, false to terminate
		sendInteraction( new Pong().letter( this.letter ) );
		updateAttributeValues( this.player );
		// update the values
		this.letter++;
		String nextPlayerName = (this.player.isNamePresent() ? this.player.name() : "") + this.letter;
		this.player.name( nextPlayerName );

		return true;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Interaction/Reflection Handler Callbacks ////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handle receipt of a {@link Ping}
	 *
	 * @param ping the interaction to handle
	 */
	@Override
	protected void receivePingInteraction( Ping ping )
	{
		/////////////////////////////////////////////////////////////////
		// INSERT HANDLING HERE
		/////////////////////////////////////////////////////////////////
		System.out.println( String.format( "Received Ping interaction - count is %s",
		                                   ping.isCountPresent() ? ping.count() : "UNDEFINED" ) );
	}

	/**
	 * Handle receipt of a {@link Player}
	 *
	 * @param player the object to handle
	 */
	@Override
	protected void receivePlayerUpdate( Player player )
	{
		/////////////////////////////////////////////////////////////////
		// INSERT HANDLING HERE
		/////////////////////////////////////////////////////////////////
		System.out.println( String.format( "Received Player update - name is %s",
		                                   player.isNamePresent() ? player.name() : "UNDEFINED" ) );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////// UCEF Simulation Control Interaction Callbacks ///////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Main method
	 *
	 * @param args ignored
	 */
	public static void main( String[] args )
	{
		System.out.println( "------------------------------------------------------------------" );
		System.out.println( "Pong Federate starting..." );
		System.out.println( "------------------------------------------------------------------" );
		System.out.println();

		try
		{
			GenxPongFederate federate = new GenxPongFederate( args );
			federate.getFederateConfiguration().fromJSON( "config.json" );
			federate.runFederate();
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.err.println( e.getMessage() );
			System.err.println( "Cannot proceed - shutting down now." );
			System.exit( 1 );
		}

		System.out.println( "Completed - shutting down now." );
		System.exit( 0 );
	}
}

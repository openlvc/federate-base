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
package gov.nist.ucef.hla.example.smart.federates;

import java.util.Map.Entry;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.smart.helpers._SmartPingFederate;
import gov.nist.ucef.hla.example.smart.interactions.Ping;
import gov.nist.ucef.hla.example.smart.interactions.Pong;
import gov.nist.ucef.hla.example.smart.reflections.Player;
import gov.nist.ucef.hla.ucef.SimEnd;
import gov.nist.ucef.hla.ucef.SimPause;
import gov.nist.ucef.hla.ucef.SimResume;
import gov.nist.ucef.hla.util.Constants;
import gov.nist.ucef.hla.util.FileUtils;

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
public class SmartPingFederate extends _SmartPingFederate
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------
	private static String[] PLAYER_NAMES= {"Alice Ping", "Bob Ping", "Carol Ping", "David Ping"};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int count;
	private Player player;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SmartPingFederate( String[] args )
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
	public void beforeReadyToPopulate()
	{
		System.out.println( String.format( "Waiting for '%s' synchronization point...",
		                                   UCEFSyncPoint.READY_TO_POPULATE ) );
	}

	@Override
	public void beforeFirstStep()
	{
		this.count =  0;
		this.player = register( new Player() );
	}

	@Override
	public boolean step( double currentTime )
	{
		// here we end out our interaction and attribute update
		sendInteraction( new Ping().count(  this.count ) );
		updateAttributeValues( this.player );
		// update the values
		this.count++;
		String nextPlayerName = PLAYER_NAMES[this.count % PLAYER_NAMES.length];
		this.player.name( nextPlayerName );
		// keep going until time 10.0
		return (currentTime < 10.0);
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Interaction/Reflection Handler Callbacks ////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handle receipt of a {@link Pong}
	 * 
	 * @param pong the interaction to handle
	 */
	@Override
	protected void receivePongInteraction( Pong pong )
	{
		System.out.println( String.format( "Received Pong interaction - letter is '%s'.",
		                                   pong.isLetterPresent() ? pong.letter() : "UNDEFINED" ) );
	}

	/**
	 * Handle receipt of a {@link Player}
	 * 
	 * @param player the object to handle
	 */
	@Override
	protected void receivePlayerUpdate( Player player )
	{
		System.out.println( String.format( "Received Player update - name is %s",
		                                   player.isNamePresent() ? player.name() : "UNDEFINED" ) );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////// UCEF Simulation Control Interaction Callbacks ///////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void receiveSimPause( SimPause simPause )
	{
		System.out.println( "Simulation has been paused." );
	}

	@Override
	protected void receiveSimResume( SimResume simResume )
	{
		System.out.println( "Simulation has been resumed." );
	}
	
	@Override
	protected void receiveSimEnd( SimEnd simEnd )
	{
		System.out.println( "SimEnd signal received. Simulation will be terminated..." );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Utility function to set up some useful configuration
	 * 
	 * @return a usefully populated {@link FederateConfiguration} instance
	 */
	private static FederateConfiguration makeConfig( FederateConfiguration config )
	{
		config.setFederateName( "Ping" );
		config.setFederateType( "PingFederate" );
		config.setFederationName( "PingPongFederation" );

		// set up interactions to publish and subscribe to
		config.cacheInteractionClasses(
            InteractionClass.Pub( Ping.interactionClassName() ),
            InteractionClass.Sub( Pong.interactionClassName() )
        );
		
		// set up object class reflections to publish and subscribe to
		ObjectClass playerReflection = ObjectClass.PubSub( Player.objectClassName() );
		for( Entry<String,DataType> entry : Player.attributes().entrySet() )
		{
			String attributeName = entry.getKey();
			DataType attributeType = entry.getValue();
			playerReflection.addAttributePubSub( attributeName, attributeType );
		}
		config.cacheObjectClasses( playerReflection );
		
		// subscribed UCEF simulation control interactions
		config.cacheInteractionClasses( 
       		InteractionClass.Sub( SimPause.interactionName() ),
       		InteractionClass.Sub( SimResume.interactionName() ),
       		InteractionClass.Sub( SimEnd.interactionName() )
   		);

		// somebody set us up the FOM...
		try
		{
			String fomRootPath = "resources/foms/";
			// modules
			String[] moduleFoms = { fomRootPath + "SmartPingPong.xml" };
			config.addModules( FileUtils.urlsFromPaths( moduleFoms ) );

			// join modules
			String[] joinModuleFoms = {};
			config.addJoinModules( FileUtils.urlsFromPaths( joinModuleFoms ) );
		}
		catch( Exception e )
		{
			throw new UCEFException( "Exception loading one of the FOM modules from disk", e );
		}

		return config;
	}

	/**
	 * Main method
	 * 
	 * @param args ignored
	 */
	public static void main( String[] args )
	{
		System.out.println( Constants.UCEF_LOGO );
		System.out.println();
		System.out.println( "	       .__                " );
		System.out.println( "	______ |__| ____    ____" );
		System.out.println( "	\\____ \\|  |/    \\  / ___\\" );
		System.out.println( "	|  |_> >  |   |  \\/ /_/  >" );
		System.out.println( "	|   __/|__|___|  /\\___  /" );
		System.out.println( "	|__|           \\//_____/" );
		System.out.println( "	   Smart Ping Federate" );
		System.out.println();
		System.out.println( "Sends 'Ping' interactions.");
		System.out.println( "Receives 'Pong' interactions.");
		System.out.println();

		try
		{
			SmartPingFederate federate = new SmartPingFederate( args );
			makeConfig( federate.getFederateConfiguration() );
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

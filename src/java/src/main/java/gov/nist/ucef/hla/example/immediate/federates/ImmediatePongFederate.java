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
package gov.nist.ucef.hla.example.immediate.federates;

import java.util.Map.Entry;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.challenger.reflections.ChallengeObject;
import gov.nist.ucef.hla.example.immediate.helpers._ImmediatePongFederate;
import gov.nist.ucef.hla.example.smart.interactions.Ping;
import gov.nist.ucef.hla.example.smart.interactions.Pong;
import gov.nist.ucef.hla.example.smart.reflections.Player;
import gov.nist.ucef.hla.ucef.interaction.SimEnd;
import gov.nist.ucef.hla.ucef.interaction.SimPause;
import gov.nist.ucef.hla.ucef.interaction.SimResume;
import gov.nist.ucef.hla.ucef.interaction.SimStart;
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
public class ImmediatePongFederate extends _ImmediatePongFederate
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
	public ImmediatePongFederate( String[] args )
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
		this.letter = 'a';
		this.player = register( new Player() );
	}

	@Override
	public boolean step( double currentTime )
	{
		// here we send out our interaction and attribute update
		sendInteraction( new Pong().letter( this.letter ) );
		// update the values
		this.letter++;
		String nextPlayerName = (this.player.isNamePresent() ? this.player.name() : "") + this.letter;
		this.player.name( nextPlayerName );
		// keep going until time 10.0
		return (currentTime < 10.0);
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
		System.out.println( String.format( "Received Ping interaction - count is %s",
		                                   ping.isCountPresent() ? ping.count() : "UNDEFINED" ) );
		updateAttributeValues( this.player );
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
		sendInteraction( new Pong().letter( this.letter ) );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////// UCEF Simulation Control Interaction Callbacks ///////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void receiveSimStart( SimStart simStart )
	{
		System.out.println( "SimStart signal received. Ready to begin..." );
	}
	
	@Override
	protected void receiveSimEnd( SimEnd simEnd )
	{
		System.out.println( "SimEnd signal received. Simulation will be terminated..." );
	}

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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Utility function to set up salient configuration details for the federate
	 * 
	 * @param the {@link FederateConfiguration} instance to be initialized
	 */
	private static void initializeConfig( FederateConfiguration config )
	{
		config.setFederateName( "Pong-"+System.currentTimeMillis() );
		config.setFederateType( "PongFederate" );
		config.setFederationName( "PingPongFederation" );

		// set up interactions to publish and subscribe to
		config.cacheInteractionClasses(
            InteractionClass.Pub( Pong.interactionClassName() ),
		    InteractionClass.Sub( Ping.interactionClassName() )
		);
		
		// set up object class reflections to publish and subscribe to
		ObjectClass playerReflection = ObjectClass.PubSub( ChallengeObject.objectClassName() );
		for( Entry<String,DataType> entry : Player.attributes().entrySet() )
		{
			String name = entry.getKey();
			DataType dataType = entry.getValue();
			playerReflection.addAttributePubSub( name, dataType );
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
		System.out.println( "	______   ____   ____    ____  " );
		System.out.println( "	\\____ \\ /  _ \\ /    \\  / ___\\" );
		System.out.println( "	|  |_> >  <_> )   |  \\/ /_/  >" );
		System.out.println( "	|   __/ \\____/|___|  /\\___  /" );
		System.out.println( "	|__|               \\//_____/" );
		System.out.println( "	     Smart Pong Federate" );
		System.out.println();
		System.out.println( "Sends 'Pong' interactions.");
		System.out.println( "Receives 'Ping' interactions.");
		System.out.println();

		try
		{
			ImmediatePongFederate federate = new ImmediatePongFederate( args );
			initializeConfig( federate.getFederateConfiguration() );
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

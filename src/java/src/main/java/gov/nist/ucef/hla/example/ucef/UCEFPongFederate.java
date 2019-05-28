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
package gov.nist.ucef.hla.example.ucef;

import java.util.HashMap;
import java.util.Map;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLACodecUtils;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.Sharing;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.ucef.FederateJoin;
import gov.nist.ucef.hla.ucef.SimEnd;
import gov.nist.ucef.hla.ucef.SimPause;
import gov.nist.ucef.hla.ucef.SimResume;
import gov.nist.ucef.hla.ucef.UCEFFederateBase;
import gov.nist.ucef.hla.util.Constants;
import gov.nist.ucef.hla.util.FileUtils;
import hla.rti1516e.encoding.EncoderFactory;

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
 * Example UCEF federate for testing
 */
public class UCEFPongFederate extends UCEFFederateBase
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private EncoderFactory encoder;
	private char letter;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UCEFPongFederate( String[] args )
	{
		super();
		this.encoder = HLACodecUtils.getEncoder();
		this.letter = 'a';
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Lifecycle Callback Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void beforeFederationJoin()
	{
		// no action required in this example
	}

	@Override
	public void beforeReadyToPopulate()
	{
		System.out.println( String.format( "Waiting for '%s' synchronization point...",
		                                   UCEFSyncPoint.READY_TO_POPULATE ) );
	}

	@Override
	protected void beforeReadyToRun()
	{
		// no action required in this example
	}

	@Override
	public void beforeFirstStep()
	{
		// initialise the letter
		this.letter = 'a';
	}

	@Override
	public boolean step( double currentTime )
	{
		// here we end out our interaction
		System.out.println( "Sending Pong interaction at time " + currentTime + "..." );
		sendInteraction( makePongInteraction( letter++ ), null );
		// keep going until time 10.0
		return (currentTime < 10.0);
	}
	
	@Override
	protected void beforeReadyToResign()
	{
		// no action required in this example
	}

	@Override
	protected void beforeExit()
	{
		// no action required in this example
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction, double time )
	{
		// delegate to method ignoring time
		receiveInteraction(hlaInteraction);
	}

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction )
	{
		String interactionClassName = hlaInteraction.getInteractionClassName();
		if( PING_INTERACTION_ID.equals( interactionClassName ) )
		{
			// Ping interaction received
			if( hlaInteraction.isPresent( PING_PARAM_COUNT ) )
			{
				int count = hlaInteraction.getAsInt( PING_PARAM_COUNT );
				System.out.println( String.format( "Received Ping interaction - 'count' is %d",
				                                   count ) );
			}
			else
			{
				System.out.println( String.format( "Received Ping interaction - no 'count' was present" ) );
			}
		}
		else
		{
			// this is unexpected - we shouldn't receive any thing we didn't subscribe to
			System.err.println( String.format( "Received an unexpected interaction of type '%s'",
			                                   interactionClassName) );
		}
	}

	@Override
	public void receiveObjectRegistration( HLAObject hlaObject )
	{
		// no action required in this example
	}
	
	@Override
	public void receiveObjectDeleted( HLAObject hlaObject )
	{
		// no action required in this example
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject )
	{
		// does not occur in this example
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject, double time )
	{
		// does not occur in this example
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////// UCEF Sim Control Interaction Callback Methods //////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void receiveSimPause( SimPause simPause, double time )
	{
		// delegate to method ignoring time
		receiveSimPause(simPause);
	}
	
	@Override
	protected void receiveSimPause( SimPause simPause )
	{
		System.out.println( "Simulation has been paused." );
	}

	@Override
	protected void receiveSimResume( SimResume simResume, double time )
	{
		// delegate to method ignoring time
		receiveSimResume(simResume);
	}
	
	@Override
	protected void receiveSimResume( SimResume simResume )
	{
		System.out.println( "Simulation has been resumed." );
	}
	

	@Override
	protected void receiveSimEnd( SimEnd simEnd, double time )
	{
		// delegate to method ignoring time
		receiveSimEnd(simEnd);
	}

	@Override
	protected void receiveSimEnd( SimEnd simEnd )
	{
		System.out.println( "SimEnd signal received. Terminating simulation..." );
	}

	@Override
	protected void receiveFederateJoin( FederateJoin federateJoin, double time )
	{
		// delegate to method ignoring time
		receiveFederateJoin(federateJoin);
	}
	
	@Override
	protected void receiveFederateJoin( FederateJoin federateJoin )
	{
		// ignored in this example
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private HLAInteraction makePongInteraction( char letter )
	{
		Map<String,byte[]> parameters = new HashMap<>();
		parameters.put( PONG_PARAM_LETTER, 
		                HLACodecUtils.encode( encoder, letter ) );
		return makeInteraction( PONG_INTERACTION_ID, parameters );
	}

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String INTERACTION_ROOT = "HLAinteractionRoot.";
	private static final String PING_INTERACTION_ID = INTERACTION_ROOT+"Ping";
	private static final String PING_PARAM_COUNT = "count";
	private static final String PONG_INTERACTION_ID = INTERACTION_ROOT+"Pong";
	private static final String PONG_PARAM_LETTER = "letter";
	
	/**
	 * Utility function to set up some useful configuration
	 * 
	 * @return a usefully populated {@link FederateConfiguration} instance
	 */
	private static FederateConfiguration makeConfig( FederateConfiguration config )
	{
		config.setFederateName( "Pong" );
		config.setFederateType( "PongFederate" );
		config.setFederationName( "PingPongFederation" );

		// set up interactions to publish and subscribe to
		config.cacheInteractionClasses(
   		    new InteractionClass( PONG_INTERACTION_ID, Sharing.PUBLISH ),
		    new InteractionClass( PING_INTERACTION_ID, Sharing.SUBSCRIBE )
        );

		// somebody set us up the FOM...
		try
		{
			String fomRootPath = "resources/foms/";
			// modules
			String[] moduleFoms = { fomRootPath + "PingPong.xml" };
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	public static void main( String[] args )
	{
		System.out.println( Constants.UCEF_LOGO );
		System.out.println();
		System.out.println( "	______   ____   ____    ____  " );
		System.out.println( "	\\____ \\ /  _ \\ /    \\  / ___\\" );
		System.out.println( "	|  |_> >  <_> )   |  \\/ /_/  >" );
		System.out.println( "	|   __/ \\____/|___|  /\\___  /" );
		System.out.println( "	|__|               \\//_____/" );
		System.out.println( "	      UCEF Pong Federate" );
		System.out.println();
		System.out.println( "Sends 'Pong' interactions.");
		System.out.println( "Receives 'Ping' interactions.");
		System.out.println();

		try
		{
			UCEFPongFederate federate = new UCEFPongFederate( args );
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

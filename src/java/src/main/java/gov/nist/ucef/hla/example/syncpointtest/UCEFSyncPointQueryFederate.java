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
package gov.nist.ucef.hla.example.syncpointtest;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLACodecUtils;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.ucef.NoOpFederate;
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
public class UCEFSyncPointQueryFederate extends NoOpFederate
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------
	// interaction class names
	public static final String HLA_FEDERATION_ROOT = "HLAinteractionRoot.HLAmanager.HLAfederation.";
	public static final String HLA_FEDERATION_REQUEST_ROOT = HLA_FEDERATION_ROOT+"HLArequest.";
	public static final String REQUEST_SYNC_POINTS = HLA_FEDERATION_REQUEST_ROOT+"HLArequestSynchronizationPoints";
	public static final String REQUEST_SYNC_POINT_STATUS = HLA_FEDERATION_REQUEST_ROOT+"HLArequestSynchronizationPointStatus";
	public static final String HLA_FEDERATION_REPORT_ROOT = HLA_FEDERATION_ROOT+"HLAreport.";
	public static final String REPORT_SYNC_POINTS = HLA_FEDERATION_REPORT_ROOT+"HLAreportSynchronizationPoints";
	public static final String REPORT_SYNC_POINT_STATUS = HLA_FEDERATION_REPORT_ROOT+"HLAreportSynchronizationPointStatus";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private EncoderFactory encoder;
	private int count;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UCEFSyncPointQueryFederate( String[] args )
	{
		super();
		this.encoder = HLACodecUtils.getEncoder();
		this.count = 0;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Lifecycle Callback Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void beforeFederationJoin()
	{
		// no action required in this example
	}

	@Override
	public void beforeReadyToPopulate()
	{
		System.out.println( String.format( "Sending '%s' interaction...", REQUEST_SYNC_POINTS ) );
		sendInteraction( makeInteraction( REQUEST_SYNC_POINTS) );
		System.out.println( String.format( "Sending '%s' interaction...", REQUEST_SYNC_POINT_STATUS ) );
		sendInteraction( makeInteraction( REQUEST_SYNC_POINT_STATUS) );
		System.out.println( String.format( "Waiting for '%s' synchronization point...",
		                                   UCEFSyncPoint.READY_TO_POPULATE ) );
	}

	@Override
	public void beforeReadyToRun()
	{
		// no action required in this example
	}

	@Override
	public void beforeFirstStep()
	{
		// initialise the counter
		this.count =  0;
	}

	@Override
	public boolean step( double currentTime )
	{
		// here we send out our interaction
		System.out.println( "Sending Ping interaction at time " + currentTime + "..." );
		sendInteraction( makePingInteraction( count++ ), null );
		// keep going until time 10.0
		return (currentTime < 10.0);
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction )
	{
		String interactionClassName = hlaInteraction.getInteractionClassName();
		if( PONG_INTERACTION_NAME.equals( interactionClassName ) )
		{
			// Pong interaction received
			if( hlaInteraction.isPresent( PONG_PARAM_LETTER ) )
			{
    			char letter = hlaInteraction.getAsChar( PONG_PARAM_LETTER );
    			System.out.println( String.format( "Received Pong interaction - 'letter' is %s",
    			                                    letter ) );
			}
			else
			{
				System.out.println( String.format( "Received Pong interaction - no 'letter' was present" ) );
			}
		}
		else if( REPORT_SYNC_POINTS.equals( interactionClassName ) )
		{
			System.out.println( String.format( "Received '%s' interaction", interactionClassName ) );
		}
		else if( REPORT_SYNC_POINT_STATUS.equals( interactionClassName ) )
		{
			System.out.println( String.format( "Received '%s' interaction", interactionClassName ) );
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
		// does not occur in this example
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////// UCEF Sim Control Interaction Callback Methods //////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private HLAInteraction makePingInteraction( int count )
	{
		Map<String,byte[]> parameters = new HashMap<>();
		parameters.put( PING_PARAM_COUNT, 
		                HLACodecUtils.encode( encoder, count ) );
		return makeInteraction( PING_INTERACTION_NAME, parameters );
	}

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String INTERACTION_ROOT = "HLAinteractionRoot.";
	private static final String PING_INTERACTION_NAME = INTERACTION_ROOT+"Ping";
	private static final String PING_PARAM_COUNT = "count";
	private static final String PONG_INTERACTION_NAME = INTERACTION_ROOT+"Pong";
	private static final String PONG_PARAM_LETTER = "letter";
	
	/**
	 * Utility function to set up salient configuration details for the federate
	 * 
	 * @param the {@link FederateConfiguration} instance to be initialized
	 */
	private static void initializeConfig( FederateConfiguration config )
	{
		 config.setFederateName( "Ping-"+System.currentTimeMillis() );
		 config.setFederateType( "PingFederate" );
		 config.setFederationName( "PingPongFederation" );
		 config.setCanCreateFederation( false );

		// set up interactions to publish and subscribe to (described 
		// in MIM) to query current status of synchronization points.
		// from HLAStandardMIM.xml...
		config.cacheInteractionClasses(
            InteractionClass.Pub( REQUEST_SYNC_POINTS ),
            InteractionClass.Sub( REPORT_SYNC_POINTS ),
            InteractionClass.Pub( REQUEST_SYNC_POINT_STATUS ),
            InteractionClass.Sub( REPORT_SYNC_POINT_STATUS )
        );

		// set up interactions to publish and subscribe to
		config.cacheInteractionClasses(
            InteractionClass.Pub( PING_INTERACTION_NAME ),
            InteractionClass.Sub( PONG_INTERACTION_NAME )
		);
		
		// somebody set us up the FOM...
		try
		{
//			URL standardMIM = ClassLoader.getSystemResource( "fom/ieee1516e/HLAstandardMIM.xml" );
//			System.out.println("standardMIM:"+standardMIM);
//			config.addJoinModules( standardMIM );
			
			String fomRootPath = "resources/foms/";
			// modules
			String[] moduleFoms = { fomRootPath + "PingPong.xml"};
			config.addModules( FileUtils.urlsFromPaths( moduleFoms ) );

			// join modules
			String[] joinModuleFoms = {fomRootPath + "HLAstandardMIM.xml"};
			config.addJoinModules( FileUtils.urlsFromPaths( joinModuleFoms ) );
		}
		catch( Exception e )
		{
			throw new UCEFException( "Exception loading one of the FOM modules from disk", e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
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
		System.out.println( "	    UCEF Ping Federate" );
		System.out.println();
		System.out.println( "Sends 'Ping' interactions.");
		System.out.println( "Receives 'Pong' interactions.");
		System.out.println();

		try
		{
			UCEFSyncPointQueryFederate federate = new UCEFSyncPointQueryFederate( args );
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

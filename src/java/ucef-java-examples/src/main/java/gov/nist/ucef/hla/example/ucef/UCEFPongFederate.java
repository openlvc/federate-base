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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import gov.nist.ucef.hla.base.HLACodecUtils;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.ExampleConstants;
import gov.nist.ucef.hla.ucef.UCEFFederateBase;
import gov.nist.ucef.hla.ucef.interactions.SimEnd;
import gov.nist.ucef.hla.ucef.interactions.SimPause;
import gov.nist.ucef.hla.ucef.interactions.SimResume;
import gov.nist.ucef.hla.ucef.interactions.SimStart;
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
 * Example federate based on {@link UCEFFederateBase}
 */
public class UCEFPongFederate extends UCEFFederateBase
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------
	// command line options and defaults
	public static final String CMDLINE_ARG_HELP = "help";
	public static final String CMDLINE_ARG_HELP_SHORT = "h";
	public static final String CMDLINE_ARG_JSON_CONFIG_FILE = "config";
	public static final String JSON_CONFIG_FILE_DEFAULT = "ucef/pong-config.json";

	// HLA interaction identifiers
	// NOTE: These must correspond to the FOM/SOM
	private static final String INTERACTION_ROOT = "HLAinteractionRoot.";
	private static final String PING_INTERACTION_NAME = INTERACTION_ROOT+"Ping";
	private static final String PING_PARAM_COUNT = "count";
	private static final String PONG_INTERACTION_NAME = INTERACTION_ROOT+"Pong";
	private static final String PONG_PARAM_LETTER = "letter";

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
		// here we send out our interaction
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
		if( PING_INTERACTION_NAME.equals( interactionClassName ) )
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
	protected void receiveSimStart( SimStart simStart, double time )
	{
		// delegate to method ignoring time
		receiveSimStart(simStart);
	}

	@Override
	protected void receiveSimStart( SimStart simStart )
	{
		System.out.println( "SimStart signal received. Beginning simulation..." );
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

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private HLAInteraction makePongInteraction( char letter )
	{
		Map<String,byte[]> parameters = new HashMap<>();
		parameters.put( PONG_PARAM_LETTER,
		                HLACodecUtils.encode( encoder, letter ) );
		return makeInteraction( PONG_INTERACTION_NAME, parameters );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Utility method to set up the command line options for the federate
	 *
	 * @return the constructed command line options
	 */
	private static Options buildCommandLineOptions()
	{
		Option help = Option.builder( CMDLINE_ARG_HELP_SHORT )
			.longOpt( CMDLINE_ARG_HELP )
			.desc("print this message and exit." )
			.build();
		Option configLocation = Option.builder()
			.longOpt( CMDLINE_ARG_JSON_CONFIG_FILE )
			.hasArg()
			.argName( "file" )
			.required( false )
			.desc( String.format( "Set the location of the JSON configuration file for the " +
								  "federate to use. If unspecified a value of '%s' will be " +
								  "used.", JSON_CONFIG_FILE_DEFAULT ) )
			.type( PatternOptionBuilder.STRING_VALUE )
			.build();

		Options cmdLineOptions = new Options();
		cmdLineOptions.addOption( help );
		cmdLineOptions.addOption( configLocation );

		return cmdLineOptions;
	}

	/**
	 * A method which parses and validates command line arguments
	 *
	 * After calling this method, it is expected that the contents of the returned
	 * {@link CommandLine} instance will be ready for use, and no further checks on the validity
	 * of the content should be required.
	 *
	 * This means that required values are guaranteed to be present, values will be in the correct
	 * range and/or valid formats and so on.
	 *
	 * @param args the arguments
	 * @param cmdLineOptions the command line options
	 * @return the resulting {@link CommandLine} instance
	 */
	private static CommandLine parseAndValidateCommandLineOptions( String[] args, Options cmdLineOptions )
	{
		CommandLineParser parser = new DefaultParser();
		// will throw an ParseException if any of the command line args are "bad"
		// At this stage we know that all the command arguments were parsed correctly
		// perform required validation
		CommandLine cmdLine = null;
		try
		{
			cmdLine = parser.parse( cmdLineOptions, args );
			// validate options that need validation
			// ...no extra validation required...
		}
		catch( ParseException e )
		{
			System.err.println( e.getMessage() );
			System.err.println();
			displayHelp( cmdLineOptions );
			System.out.println( "Cannot proceed. Exiting now." );
			System.exit( 1 );
		}

		return cmdLine;
	}

	/**
	 * A simple utility method to display command line option help
	 *
	 * @param cmdLineOptions
	 */
	private static void displayHelp( Options cmdLineOptions )
	{
		HelpFormatter helpFormatter = new HelpFormatter();
		String header = "Verifies that messages are exchanged correctly between federates.\n\n";
		String footer = "\n";
		helpFormatter.printHelp( "ChallengeFederate", header, cmdLineOptions, footer, true );
	}

	public static void main( String[] args )
	{
		System.out.println( ExampleConstants.UCEF_LOGO );
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

		Options cmdLineOptions = buildCommandLineOptions();
		CommandLine cmdLine = parseAndValidateCommandLineOptions( args, cmdLineOptions );
		if( cmdLine.hasOption( CMDLINE_ARG_HELP ) )
		{
			// if the --help option has been used, we display help and exit immediately
			displayHelp( cmdLineOptions );
			System.exit( 1 );
		}

		try
		{
			String jsonConfig = JSON_CONFIG_FILE_DEFAULT;
			if( cmdLine.hasOption( CMDLINE_ARG_JSON_CONFIG_FILE ) )
			{
				// command line override specified for configuration JSON
				jsonConfig = cmdLine.getOptionValue( CMDLINE_ARG_JSON_CONFIG_FILE ).toString();
			}

			UCEFPongFederate federate = new UCEFPongFederate( args );
			federate.configureFromJSON( jsonConfig );
			System.out.println( federate.getFederateConfiguration().summary() );

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

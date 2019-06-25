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
package gov.nist.ucef.hla.example.challenger;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.ExampleConstants;
import gov.nist.ucef.hla.example.challenger.base._ResponseFederate;
import gov.nist.ucef.hla.example.challenger.interactions.ChallengeInteraction;
import gov.nist.ucef.hla.example.challenger.interactions.ResponseInteraction;
import gov.nist.ucef.hla.example.challenger.reflections.ChallengeObject;
import gov.nist.ucef.hla.ucef.interactions.SimEnd;
import gov.nist.ucef.hla.ucef.interactions.SimPause;
import gov.nist.ucef.hla.ucef.interactions.SimResume;
import gov.nist.ucef.hla.ucef.interactions.SimStart;

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
 * Example federate
 */
public class ResponseFederate extends _ResponseFederate
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------
	public static final String CMDLINE_ARG_HELP = "help";
	public static final String CMDLINE_ARG_HELP_SHORT = "h";
	public static final String CMDLINE_ARG_ITERATIONS = "iterations";
	public static final String CMDLINE_ARG_JSON_CONFIG_FILE = "config";

	public static final String JSON_CONFIG_FILE_DEFAULT = "challenger/response-config.json";

	//----------------------------------------------------------
	//                   INNER CLASSES
	//----------------------------------------------------------
	private static class Response
	{
		String challengeID;
		String solution;
	};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	// track the received challenges we need to respond to
	private List<ChallengeObject> remoteChallengeObjects;
	private List<ChallengeInteraction> remoteChallengeInteractions;

	// locking for thread safety
	private final Object mutex_lock = new Object();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ResponseFederate()
	{
		this.remoteChallengeObjects = new ArrayList<>();
		this.remoteChallengeInteractions = new ArrayList<>();
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
	public boolean step( double currentTime )
	{
		// Create a local list of received remote challenges
		List<ChallengeObject> challengeObjectList = new ArrayList<>();
		List<ChallengeInteraction> challengeInteractionList = new ArrayList<>();

		synchronized( this.mutex_lock )
		{
			challengeObjectList.addAll( this.remoteChallengeObjects );
			challengeInteractionList.addAll( this.remoteChallengeInteractions );

			this.remoteChallengeObjects.clear();
    		this.remoteChallengeInteractions.clear();
		}

		// Generate a response for each remote challenge
		for( ChallengeObject challenge : challengeObjectList )
		{
			System.out.println( String.format( "Responding to challenge object ID : %s", challenge.challengeId() ) );
			System.out.println( "---------------------------------------------------------------------------------" );

			Response response = solveChallenge( challenge );
			sendInteraction( generateResponseInteraction( response ) );
		}

		for( ChallengeInteraction challenge: challengeInteractionList )
		{
			System.out.println( String.format( "Responding to challenge interaction ID : %s", challenge.challengeId() ) );
			System.out.println( "---------------------------------------------------------------------------------" );

			Response response = solveChallenge( challenge );
			sendInteraction( generateResponseInteraction( response ) );
		}

		return true;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Interaction/Reflection Handler Callbacks ////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void receiveChallengeInteraction( ChallengeInteraction interaction )
	{
		System.out.println( String.format( "Received interaction challenge ID : %s", interaction.challengeId() ) );
		System.out.println( String.format( "Received string is                : %s", interaction.stringValue() ) );
		System.out.println( String.format( "Received index is                 : %d", interaction.beginIndex() ) );
		System.out.println( "---------------------------------------------------------------------------------" );

		synchronized( this.mutex_lock )
		{
			this.remoteChallengeInteractions.add( interaction );
		}
	}

	@Override
	protected void receiveChallengeObject( ChallengeObject obj )
	{
		System.out.println( String.format( "Received reflection challenge ID  : %s", obj.challengeId() ) );
		System.out.println( String.format( "Received string is                : %s", obj.stringValue() ) );
		System.out.println( String.format( "Received index is                 : %d", obj.beginIndex() ) );
		System.out.println( "---------------------------------------------------------------------------------" );

		synchronized( this.mutex_lock )
		{
			this.remoteChallengeObjects.add( obj );
		}
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

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Internal Utility Methods ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Solve a challenge received as an object attribute reflection, and construct a response
	 *
	 * See also {@link #solveChallenge(ChallengeInteraction)}
	 *
	 * @param receievedChallenge the challenge
	 * @return the appropriate response
	 */
	private Response solveChallenge( ChallengeObject receievedChallenge )
	{
		return solveChallenge( receievedChallenge.challengeId(),
		                       receievedChallenge.stringValue(),
		                       receievedChallenge.beginIndex() );
	}

	/**
	 * Solve a challenge received as an interaction, and construct a response
	 *
	 * See also {@link #solveChallenge(ChallengeObject)}
	 *
	 * @param receievedChallenge the challenge
	 * @return the appropriate response
	 */
	private Response solveChallenge( ChallengeInteraction receievedChallenge )
	{
		return solveChallenge( receievedChallenge.challengeId(),
		                       receievedChallenge.stringValue(),
		                       receievedChallenge.beginIndex() );
	}

	/**
	 * Solve a challenge and construct a response
	 *
	 * The response is simply the substring of the original string starting at the given index.
	 *
	 * @param challengeID the ID of the challenge
	 * @param stringValue the string value contained in the challenge
	 * @param beginIndex the beginning index contained in the challenge
	 * @return an appropriate response (which is the solution to the challenge).
	 */
	private Response solveChallenge( String challengeID, String stringValue, int beginIndex )
	{
		Response response = new Response();
		response.challengeID = challengeID;
		response.solution = stringValue.substring( beginIndex );
		return response;
	}

	/**
	 * Create an interaction containing the details of the response to a challenge
	 *
	 * @param response the details of the response
	 * @return the interaction to send as a response to a challenge
	 */
	private ResponseInteraction generateResponseInteraction( Response response )
	{
		ResponseInteraction responseInteraction = new ResponseInteraction();
		responseInteraction.challengeId( response.challengeID );
		responseInteraction.substring( response.solution );
		return responseInteraction;
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
			System.err.println( "!!!!!ERRORS WERE FOUND!!!!!:" );
			System.err.println( e.getMessage() );
			System.err.println( "!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
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
		String footer = "";
		helpFormatter.printHelp( "ChallengeFederate", header, cmdLineOptions, footer, true );
	}

	/**
	 * Main method
	 *
	 * @param args ignored
	 */
	public static void main( String[] args )
	{
		System.out.println( ExampleConstants.UCEF_LOGO );
		System.out.println();
		System.out.println( "Java Responder Federate" );
		System.out.println();
		System.out.println( "Publishes:");
		System.out.println( "\t'ResponseInteraction' interactions");
		System.out.println( "Subscribes to:");
		System.out.println( "\t'ChallengeInteraction' interactions.");
		System.out.println( "\t'ChallengeObject' reflections");
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

			ResponseFederate federate = new ResponseFederate();
			federate.configureFromJSON( jsonConfig );
			System.out.println(federate.getFederateConfiguration().summary());

			System.out.println( "Preparing to receive challenges..." );
			System.out.println();

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

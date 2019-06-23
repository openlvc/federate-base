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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.json.simple.JSONObject;

import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.ExampleConstants;
import gov.nist.ucef.hla.example.challenger.base._ChallengeFederate;
import gov.nist.ucef.hla.example.challenger.interactions.ChallengeInteraction;
import gov.nist.ucef.hla.example.challenger.interactions.ResponseInteraction;
import gov.nist.ucef.hla.example.challenger.reflections.ChallengeObject;
import gov.nist.ucef.hla.example.util.ConfigUtils;
import gov.nist.ucef.hla.example.util.JSONUtils;
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
 * Example federate for testing
 */
public class ChallengeFederate extends _ChallengeFederate
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------
	// command line options
	public static final String CMDLINE_ARG_HELP = "help";
	public static final String CMDLINE_ARG_HELP_SHORT = "h";
	public static final String CMDLINE_ARG_ITERATIONS = "iterations";
	public static final String CMDLINE_ARG_JSON_CONFIG_FILE = "config";

	public static final String JSON_CONFIG_FILE_DEFAULT = "challenger/challenge-config.json";
	public static final int ITERATIONS_DEFAULT = 10;

	// the characters which are allowed to be included in a challenge string
	private static final String VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
	// the length of challenge strings
	private static final int CHALLENGE_STRING_LENGTH = 10;

	// track how many items pass
	private static int passCounter = 0;
	// track the current challenge ID
	private static int challengeID = 0;
	// a flag to toggle between sending challenges as object attribute reflections or interactions
	private static boolean sendChallengeObject = true;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// the total number of challenges we will be sending
	private int totalChallenges;

	// track the challenges we send out which have so far not been responded to
	private Map<String, ChallengeObject> unansweredChallengeObjects;
	private Map<String, ChallengeInteraction> unansweredChallengeInteractions;
	// track the responses we receive to challenges
	private List<ResponseInteraction> responseInteractions;

	// locking for thread safety
	private final Object mutex_lock = new Object();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ChallengeFederate(String[] args)
	{
		super();

		this.unansweredChallengeObjects = new HashMap<>();
		this.unansweredChallengeInteractions = new HashMap<>();
		this.responseInteractions = new ArrayList<>();

		this.totalChallenges = ITERATIONS_DEFAULT;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void setTotalChallenges( int totalChallenges )
	{
		this.totalChallenges = totalChallenges;
	}

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
	public void beforeReadyToResign()
	{
		System.out.println( "'Before ready to resign' hook" );
		// Print the sent challenge
		System.out.println( String.format( "Total challenges sent          : %d", challengeID  ));
		System.out.println( String.format( "Pass count                     : %d", passCounter ));
		System.out.println( String.format( "Failed count                   : %d", (challengeID - passCounter )));
		System.out.println( "---------------------------------------------");
	}

	@Override
	public void beforeExit()
	{
		System.out.println( "\'Before exit\' hook" );

		if( !this.unansweredChallengeObjects.isEmpty() )
		{
			System.out.println( String.format(  "No response received to object challenge(s): %s",
			                                    this.unansweredChallengeObjects.keySet()
			                                    	.stream()
			                                    	.collect( Collectors.joining( ", " ) )
	                                    	 ) );

			for( ChallengeObject challengeObject : this.unansweredChallengeObjects.values() )
				this.rtiamb.deleteObjectInstance( challengeObject );
		}

		if( !this.unansweredChallengeInteractions.isEmpty() )
		{
			System.out.println( String.format(  "No response received to interaction challenge(s): %s",
			                                    this.unansweredChallengeInteractions.keySet()
			                                    .stream()
			                                    .collect( Collectors.joining( ", " ) )
			                                 ) );
		}
	}

	@Override
	public boolean step( double currentTime )
	{
		if( challengeID < this.totalChallenges )
		{
			// Generate a challenge
			Challenge challenge = generateChallenge();
			if( sendChallengeObject )
				sendChallengeObject( challenge );
			else
				sendChallengeInteraction( challenge );
			// toggle between sending challenge objects and challenge interactions
			sendChallengeObject = !sendChallengeObject;
		}
		validateResponses();

		boolean challengesRemaining = challengeID < this.totalChallenges;
		int responsesRemaining = this.unansweredChallengeObjects.size() +
								 this.unansweredChallengeInteractions.size();

		boolean shouldContinue = challengesRemaining || (responsesRemaining > 0);

		return shouldContinue;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Interaction/Reflection Handler Callbacks ////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void receiveResponseInteraction( ResponseInteraction interaction )
	{
		synchronized( this.mutex_lock )
		{
			this.responseInteractions.add( interaction );
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
	 * A utility method to send an attribute reflection containing details of a challenge
	 *
	 * See also {@link #sendChallengeInteraction(Challenge)}.
	 *
	 * @param challenge the challenge details
	 */
	private void sendChallengeObject( Challenge challenge )
	{
		// Create a new challenge object
		ChallengeObject challengeObject = register( new ChallengeObject() );
		// Set attributes
		challengeObject.challengeId( challenge.challengeId );
		challengeObject.stringValue( challenge.stringValue );
		challengeObject.beginIndex( challenge.beginIndex );

		// Send attribute update notification to users
		updateAttributeValues( challengeObject );

		// Store the challenge
		this.unansweredChallengeObjects.put( challengeObject.challengeId(), challengeObject );

		// Print the sent challenge
		System.out.println( String.format( "Sending challenge object      : %s", challengeObject.challengeId() ) );
		System.out.println( String.format( "with String value             : %s", challengeObject.stringValue() ) );
		System.out.println( String.format( "and begin index               : %d", challengeObject.beginIndex() ) );
		System.out.println( "---------------------------------------------" );
	}

	/**
	 * A utility method to send an interaction containing details of a challenge
	 *
	 * See also {@link #sendChallengeObject(Challenge)}.
	 *
	 * @param challenge the challenge details
	 */
	private void sendChallengeInteraction( Challenge challenge )
	{
		// Create a new challenge interaction
		ChallengeInteraction challengeInteraction = new ChallengeInteraction();
		// Set attributes
		challengeInteraction.challengeId( challenge.challengeId );
		challengeInteraction.stringValue( challenge.stringValue );
		challengeInteraction.beginIndex( challenge.beginIndex );

		// Send attribute update notification to users
		sendInteraction( challengeInteraction );

		// Store the challenge
		this.unansweredChallengeInteractions.put( challengeInteraction.challengeId(), challengeInteraction );

		// Print the sent challenge
		System.out.println( String.format( "Sending challenge interaction : %s", challengeInteraction.challengeId() ) );
		System.out.println( String.format( "with String value             : %s", challengeInteraction.stringValue() ) );
		System.out.println( String.format( "and begin index               : %d", challengeInteraction.beginIndex() ) );
		System.out.println( "---------------------------------------------" );
	}

	/**
	 * A utility method to process all responses to challenges which have been received so far,
	 * checking whether they are correct or not.
	 */
	private void validateResponses()
	{
		// make a local copy of responses to process
		List<ResponseInteraction> responseCopy = new ArrayList<>();
		synchronized( this.mutex_lock )
		{
    		responseCopy.addAll( this.responseInteractions );
    		this.responseInteractions.clear();
		}

		for( ResponseInteraction responseInteraction : responseCopy )
		{
			String challengeType = "";
			String stringValue = "";
			int beginIndex = -1;
			String substring = "";
			boolean found = false;
			boolean valid = false;

			String challengeId = responseInteraction.challengeId();
			ChallengeObject itSentObject = this.unansweredChallengeObjects.get( challengeId );
			if( itSentObject != null )
			{
				found = true;

				challengeType = "Object";
				stringValue = itSentObject.stringValue();
				beginIndex = itSentObject.beginIndex();
				substring = responseInteraction.substring();

				// Since we received a reply for this challenge, we can delete this instance from RTI now
				deleteObjectInstance( itSentObject );
				// this challenge object has been answered - we can remove it now
				this.unansweredChallengeObjects.remove( challengeId );
			}

			// If not found go and search in sent interactions
			if( !found )
			{
				ChallengeInteraction itSentInteraction = this.unansweredChallengeInteractions.get( challengeId );
				if( itSentInteraction != null )
				{
					found = true;

					challengeType = "Interaction";
					stringValue = itSentInteraction.stringValue();
					beginIndex = itSentInteraction.beginIndex();
					substring = responseInteraction.substring();

					// this challenge interaction has been answered - we can remove it now
					this.unansweredChallengeInteractions.remove( challengeId );
				}
			}

			if(found)
			{
				valid = isResponseCorrect( stringValue, beginIndex, substring);

				passCounter += valid ? 1 : 0;

				String msg = String.format( "Response for challenge %s received...\n", challengeId );
				msg += String.format( "Challenge Type                : %s\n", challengeType );
				msg += String.format( "Sent String                   : '%s'\n", stringValue );
				msg += String.format( "Begin Index                   : %d\n", beginIndex );
				msg += String.format( "Expected Substring            : '%s'\n", stringValue.substring( beginIndex ) );
				msg += String.format( "Actual SubString received     : '%s'\n", substring );
				msg += String.format( "Status                        : %s\n", (valid ? "CORRECT" : "INCORRECT") );
				msg += "---------------------------------------------\n";

				if( valid )
				{
					System.out.println( msg );
				}
				else
				{
					// If the result is incorrect write to error log
					System.err.println( msg );
				}
			}
			else
			{
				System.err.println( String.format( "A response for a challenge with ID '%s' was "+
												   "received, but no such ChallengeObject or "+
												   "ChallengeInteraction was sent.",
												   challengeId) );
			}
		}
	}

	/**
	 * Generate a "challenge" to send.
	 *
	 * This is basically a string and an index within the string.
	 *
	 * See also {@link #isResponseCorrect(String, int, String)}.
	 *
	 * @return a challenge
	 */
	private Challenge generateChallenge()
	{
		Challenge challenge = new Challenge();
		challengeID++;
		String challengeId = String.format( "%s#%d", this.configuration.getFederateName(), challengeID );
		challenge.challengeId = challengeId;
		challenge.stringValue = getRandomString( CHALLENGE_STRING_LENGTH );
		challenge.beginIndex = randInt( 0, CHALLENGE_STRING_LENGTH-1 );
		return challenge;
	}

	/**
	 * A utility function to determine if the response to a challenge is correct.
	 *
	 * The response is correct if the content is the substring of the original string starting
	 * at the given index.
	 *
	 * @param original the original string
	 * @param index the sub string index
	 * @param actual the "answer" to the challenge contained in the response
	 * @return
	 */
	private boolean isResponseCorrect( String original, int index, String actual)
	{
		return actual.equals( original.substring( index ) );
	}

	/**
	 * A simple utility function to generate a random string of a given length
	 *
	 * @param length the length of the desired string
	 * @return a random string of the required length
	 */
	private String getRandomString( int length )
	{
		StringBuilder builder = new StringBuilder( length );
		for( int i = 0; i < length; i++ )
		{
			int charIndex = randInt( 0, VALID_CHARACTERS.length()-1 );
			builder.append( VALID_CHARACTERS.charAt( charIndex ) );
		}
		return builder.toString();
	}

	/**
	 * A simple utility function to generate a random integer within a range
	 *
	 * @param min the minimum allowed value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return a random integer value in the given range
	 */
	private int randInt(int min, int max)
	{
		return ThreadLocalRandom.current().nextInt( min, max + 1 );
	}

	//----------------------------------------------------------
	//                   INNER CLASSES
	//----------------------------------------------------------
	/**
	 * A class which is simply for the purposes of containing the details of a "challenge"
	 */
	private static class Challenge
	{
		String challengeId;
		String stringValue;
		int beginIndex;
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
		Option iterations = Option.builder()
			.longOpt( CMDLINE_ARG_ITERATIONS )
			.hasArg()
			.argName( "count" )
			.required( false )
			.desc( String.format( "Set the number of challenges to issue. " +
								  "If unspecified a value of '%d' will be used.",
								  ITERATIONS_DEFAULT ))
			.type( PatternOptionBuilder.NUMBER_VALUE )
			.build();

		Options cmdLineOptions = new Options();
		cmdLineOptions.addOption( help );
		cmdLineOptions.addOption( configLocation );
		cmdLineOptions.addOption( iterations );

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
			if( cmdLine.hasOption( CMDLINE_ARG_ITERATIONS ) )
			{
				Object parsedValue = cmdLine.getParsedOptionValue( CMDLINE_ARG_ITERATIONS );
				if( parsedValue instanceof Long )
				{
					int intValue = ((Long)parsedValue).intValue();
					if( intValue < 0 )
					{
						throw new ParseException( String.format( "Value for '%s' must be " +
						                                         "greater than zero.",
						                                         CMDLINE_ARG_ITERATIONS ) );
					}
				}
				else
				{
					throw new ParseException( String.format( "Value for '%s' must be " +
					                                         "a whole number greater than zero.",
					                                         CMDLINE_ARG_ITERATIONS ) );
				}
			}
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
	 * A utility method to validate options in the JSON configuration file which are specific to
	 * this particular federate.
	 *
	 * @param jsonConfig the JSON configuration
	 * @return true if the JSON configuration options for this federate are valid, false otherwise
	 */
	private static boolean validateJsonOptions( JSONObject jsonConfig )
	{
		boolean isValid = true;

		if( jsonConfig.containsKey( CMDLINE_ARG_ITERATIONS ) )
		{
			Object valueObj = jsonConfig.get( CMDLINE_ARG_ITERATIONS );
			if( valueObj instanceof Long )
			{
				long value = (Long)valueObj;
				isValid = value > 0L;
			}

			if(!isValid)
			{
				System.err.println( "!!!!!ERRORS WERE FOUND!!!!!:" );
				System.out.println( String.format( "ERROR: Value for '%s' option must be " +
				                                   "a whole number greater than zero.",
				                                   CMDLINE_ARG_ITERATIONS ) );
				System.err.println( "!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
				System.err.println();
			}
		}

		return isValid;
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
		String footer = String.format( "\nNOTE: the value for '%s' may also be specified in " +
		                               "the JSON configuration.", CMDLINE_ARG_ITERATIONS );
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
		System.out.println( "Java Challenger Federate" );
		System.out.println();
		System.out.println( "Publishes:");
		System.out.println( "\t'ChallengeInteraction' interactions");
		System.out.println( "\t'ChallengeObject' reflections");
		System.out.println( "Subscribes to:");
		System.out.println( "\t'ResponseInteraction' interactions.");
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
			String jsonSource = JSON_CONFIG_FILE_DEFAULT;
			if( cmdLine.hasOption( CMDLINE_ARG_JSON_CONFIG_FILE ) )
				jsonSource = cmdLine.getOptionValue( CMDLINE_ARG_JSON_CONFIG_FILE ).toString();
			JSONObject jsonConfig = JSONUtils.toJsonObject( jsonSource );

			// validate any options in the JSON specific to this federate
			boolean isValid = validateJsonOptions(jsonConfig);
			if( !isValid )
			{
				// there something wrong in the JSON
				displayHelp( cmdLineOptions );
				System.exit( 1 );
			}

			int iterations = ConfigUtils.getConfiguredInt( jsonConfig, cmdLine,
			                                               CMDLINE_ARG_ITERATIONS, ITERATIONS_DEFAULT );

			ChallengeFederate federate = new ChallengeFederate( args );
			federate.getFederateConfiguration().fromJSON( jsonConfig );
			federate.setTotalChallenges( iterations );

			System.out.println( String.format( "Preparing to send %d challenges...", iterations ) );
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

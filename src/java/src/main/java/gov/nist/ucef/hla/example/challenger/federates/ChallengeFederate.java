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
package gov.nist.ucef.hla.example.challenger.federates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.challenger.helpers._ChallengeFederate;
import gov.nist.ucef.hla.example.challenger.interactions.ChallengeInteraction;
import gov.nist.ucef.hla.example.challenger.interactions.ResponseInteraction;
import gov.nist.ucef.hla.example.challenger.reflections.ChallengeObject;
import gov.nist.ucef.hla.ucef.SimEnd;
import gov.nist.ucef.hla.ucef.SimPause;
import gov.nist.ucef.hla.ucef.SimResume;
import gov.nist.ucef.hla.util.Constants;
import gov.nist.ucef.hla.util.FileUtils;
import gov.nist.ucef.hla.util.cmdargs.ArgException;
import gov.nist.ucef.hla.util.cmdargs.ArgProcessor;
import gov.nist.ucef.hla.util.cmdargs.StdValidators;
import gov.nist.ucef.hla.util.cmdargs.ValueArg;

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
	int totalChallenges = 0;
	
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
		
		CmdLineProcessor argProcessor = new CmdLineProcessor( "ChallengeFederate", 80 );
        try
		{
			argProcessor.processArgs( args );
		}
		catch( ArgException e )
		{
			System.err.println( e.getMessage() );
			System.out.println( "======= Usage:\n" + argProcessor.getUsage() );
			System.out.println( "===== Options:\n" + argProcessor.getHelp() );
			System.out.println( "Cannot proceed. Exiting now." );
			System.exit( 1 );
		}
        
		this.totalChallenges = argProcessor.iterations();
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
				rtiamb.deleteObjectInstance( challengeObject );
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
		if( challengeID < totalChallenges )
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
		
		boolean challengesRemaining = challengeID < totalChallenges;
		int responsesRemaining = unansweredChallengeObjects.size() + 
								 unansweredChallengeInteractions.size();
		
		boolean shouldContinue = challengesRemaining || (responsesRemaining > 0);
		
		return shouldContinue;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Interaction/Reflection Handler Callbacks ////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void receiveResponseInteraction( ResponseInteraction interaction )
	{
		synchronized( mutex_lock )
		{
			responseInteractions.add( interaction );
		}
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
		unansweredChallengeObjects.put( challengeObject.challengeId(), challengeObject );

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
		unansweredChallengeInteractions.put( challengeInteraction.challengeId(), challengeInteraction );

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
		synchronized( mutex_lock )
		{
    		responseCopy.addAll( responseInteractions );
    		responseInteractions.clear();
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
			ChallengeObject itSentObject = unansweredChallengeObjects.get( challengeId );
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
				unansweredChallengeObjects.remove( challengeId );
			}

			// If not found go and search in sent interactions
			if( !found )
			{
				ChallengeInteraction itSentInteraction = unansweredChallengeInteractions.get( challengeId );
				if( itSentInteraction != null )
				{
					found = true;

					challengeType = "Interaction";
					stringValue = itSentInteraction.stringValue();
					beginIndex = itSentInteraction.beginIndex();
					substring = responseInteraction.substring();

					// this challenge interaction has been answered - we can remove it now
					unansweredChallengeInteractions.remove( challengeId );
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

	/**
	 * A class defining the command line arguments which the federate will accept on startup
	 */
	private class CmdLineProcessor
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------
		// command line arguments and defaults
		public static final String CMDLINE_ARG_ITERATIONS = "iterations";
		public static final char CMDLINE_ARG_ITERATIONS_SHORT = 'i';
		
		public static final int ITERATIONS_DEFAULT = 10;

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		// command line processor and argument definitions
		private String execName;
		private ArgProcessor argProcessor;
		private ValueArg iterationsArg;
		
		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public CmdLineProcessor( String execName, int consoleWidth )
		{
			this.execName = execName;

			initialize( consoleWidth );
		}
		
		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public String getUsage()
		{
			return argProcessor.getUsage( this.execName );
		}
		
		public String getHelp()
		{
			return argProcessor.getHelp();
		}
		
		public int iterations()
		{
			return Integer.parseInt( iterationsArg.value(), 10 );
		}

		/**
		 * Utility method for validating and processing of command line arguments/options
		 * 
		 * Does the following:
		 * 
		 *  - Sets up the command line arguments
		 *  - validates and processes what the user provided
		 *  - populate all necessary internal state information that 
		 *    relies on information from command line arguments 
		 * 
		 * @param args the command line arguments
		 * @return true if the arguments were all valid and correct, false otherwise (execution should
		 *  	   not continue.
		 */
		public boolean processArgs( String[] args ) throws ArgException
		{
			// will throw an ArgException if any of the command line args are "bad"
			argProcessor.process( args );
			
			// At this stage we know that all command line arguments are valid,
			// so we can use the values without checking them further
			// all command line arguments are present and correct!
			return true;
		}
		
		private void initialize( int consoleWidth )
		{
			argProcessor = new ArgProcessor( consoleWidth );
			
			iterationsArg = argProcessor
	        	.addValueArg( CMDLINE_ARG_ITERATIONS_SHORT, CMDLINE_ARG_ITERATIONS )
	        	.isRequired( false )
				.defaultValue( Integer.toString( ITERATIONS_DEFAULT ) )
			    .validator( StdValidators.CheckIntGtZero )
	        	.help( String.format( "Set the number of challenges to issue. " +
					  				  "If unspecified a value of '%d' will be used.",
					  				  ITERATIONS_DEFAULT ) )
	        	.hint( "ITERATION_COUNT" );
		}
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
		config.setFederateName( "JavaChallenger" );
		config.setFederateType( "ChallengeFederate" );
		config.setFederationName( "ChallengeResponseFederation" );

		config.addFomPath( "ChallengeResponse/fom/ChallengeResponse.xml" );
		config.addSomPath( "ChallengeResponse/som/Challenge.xml" );
		
		// set up lists of objects/attributes and interactions to subscribe to
		config.addSubscribedInteraction( ResponseInteraction.interactionClassName() );
		// set up lists of objects/attributes and interactions to publish
		config.addPublishedAttributes( ChallengeObject.objectClassName(), ChallengeObject.attributeNames() );
		config.addPublishedInteraction( ChallengeInteraction.interactionClassName() );
		
		// subscribed UCEF simulation control interactions
		config.addSubscribedInteractions( SimPause.interactionName(), SimResume.interactionName(),
		                                  SimEnd.interactionName() );
		
		// somebody set us up the FOM...
		try
		{
			String fomRootPath = "resources/";
			// modules
			String[] moduleFoms = { fomRootPath + "/challenge-response/fom/ChallengeResponse.xml" };
			config.addModules( FileUtils.urlsFromPaths(moduleFoms) );
			
			// join modules
			String[] joinModuleFoms = {};
			config.addJoinModules( FileUtils.urlsFromPaths(joinModuleFoms) );
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
		System.out.println( "Java Challenger Federate" );
		System.out.println();
		System.out.println( "Publishes:");
		System.out.println( "\t'ChallengeInteraction' interactions");
		System.out.println( "\t'ChallengeObject' reflections");
		System.out.println( "Subscribes to:");
		System.out.println( "\t'ResponseInteraction' interactions.");
		System.out.println();

		try
		{
			ChallengeFederate federate = new ChallengeFederate( args );
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

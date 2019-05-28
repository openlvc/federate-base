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
import java.util.List;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.challenger.helpers._ResponseFederate;
import gov.nist.ucef.hla.example.challenger.interactions.ChallengeInteraction;
import gov.nist.ucef.hla.example.challenger.interactions.ResponseInteraction;
import gov.nist.ucef.hla.example.challenger.reflections.ChallengeObject;
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
public class ResponseFederate extends _ResponseFederate
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------
	
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
		
		synchronized( mutex_lock )
		{
			challengeObjectList.addAll( remoteChallengeObjects );
			challengeInteractionList.addAll( remoteChallengeInteractions );
			
			remoteChallengeObjects.clear();
    		remoteChallengeInteractions.clear();
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
		
		synchronized( mutex_lock )
		{
			remoteChallengeInteractions.add( interaction );
		}
	}

	@Override
	protected void receiveChallengeObject( ChallengeObject obj )
	{
		System.out.println( String.format( "Received reflection challenge ID  : %s", obj.challengeId() ) );
		System.out.println( String.format( "Received string is                : %s", obj.stringValue() ) );
		System.out.println( String.format( "Received index is                 : %d", obj.beginIndex() ) );
		System.out.println( "---------------------------------------------------------------------------------" );
		
		synchronized( mutex_lock )
		{
			remoteChallengeObjects.add( obj );
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
	 * Utility function to set up some useful configuration
	 * 
	 * @return a usefully populated {@link FederateConfiguration} instance
	 */
	private static FederateConfiguration makeConfig( FederateConfiguration config )
	{
		config.setFederateName( "JavaResponder" );
		config.setFederateType( "ResponseFederate" );
		config.setFederationName( "ChallengeResponseFederation" );

		// set up interactions to publish and subscribe to
		config.cacheInteractionClasses(
            InteractionClass.Sub( ChallengeInteraction.interactionClassName() ),
            InteractionClass.Pub( ResponseInteraction.interactionClassName() )
   		);
		
		// set up object class reflections to publish and subscribe to
		ObjectClass challengeReflection = ObjectClass.Sub( ChallengeObject.objectClassName() );
		for( String attributeName : ChallengeObject.attributeNames() )
		{
			challengeReflection.addAttributeSub( attributeName, DataType.STRING );
		}
		config.cacheObjectClasses( challengeReflection );
		
		// subscribed UCEF simulation control interactions
		config.cacheInteractionClasses( 
    		InteractionClass.Sub( SimPause.interactionName() ),
    		InteractionClass.Sub( SimResume.interactionName() ),
    		InteractionClass.Sub( SimEnd.interactionName() )
		);

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
		System.out.println();
		System.out.println( "Java Responder Federate" );
		System.out.println();
		System.out.println( "Publishes:");
		System.out.println( "\t'ResponseInteraction' interactions");
		System.out.println( "Subscribes to:");
		System.out.println( "\t'ChallengeInteraction' interactions.");
		System.out.println( "\t'ChallengeObject' reflections");
		System.out.println();

		try
		{
			ResponseFederate federate = new ResponseFederate();
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

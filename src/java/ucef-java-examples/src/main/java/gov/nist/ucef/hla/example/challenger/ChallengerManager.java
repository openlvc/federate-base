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

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.example.ExampleConstants;
import gov.nist.ucef.hla.example.util.FileUtils;
import gov.nist.ucef.hla.tools.fedman.FedManConstants;
import gov.nist.ucef.hla.tools.fedman.FedManFederate;
import gov.nist.ucef.hla.ucef.interaction.SimEnd;
import gov.nist.ucef.hla.ucef.interaction.SimPause;
import gov.nist.ucef.hla.ucef.interaction.SimResume;

/**
 *		            ___
 *		          _/   \_     _     _
 *		         / \   / \   / \   / \
 *		        ( U )─( C )─( E )─( F )
 *		         \_/   \_/   \_/   \_/
 *		        <─┴─> <─┴─────┴─────┴─>
 *		       Universal CPS Environment
 *		             for Federation
 * 		    ______         ____  ___
 * 		   / ____/__  ____/ /  |/  /___ _____
 * 		  / /_  / _ \/ __  / /\|_/ / __`/ __ \
 * 		 / __/ /  __/ /_/ / /  / / /_/ / / / /
 * 	    /_/    \___/\__,_/_/  /_/\__,_/_/ /_/
 * 	  ─────────── Federation Manager ───────────
 */
public class ChallengerManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println( ExampleConstants.UCEF_LOGO );
		System.out.println( FedManConstants.FEDMAN_LOGO );
		System.out.println( "== Challenge/Response Federation Manager ==" );
		System.out.println();

		try
		{
			FedManFederate fedman = new FedManFederate( args );
			initializeConfig( fedman.getFederateConfiguration() );
			fedman.runFederate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println( e.getMessage() );
			System.err.println( "Cannot proceed - shutting down now." );
			System.exit( 1 );
		}

		System.out.println( "Completed - shutting down now." );
		System.exit( 0 );
	}

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

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
		config.setFederateName( FedManConstants.FEDMAN_FEDERATE_NAME );
		config.setFederateType( FedManConstants.FEDMAN_FEDERATE_TYPE );
		config.setFederationName( "ChallengeResponseFederation" );

		// a federation manager is allowed to create a required federation
		config.setCanCreateFederation( true );

		// set up object class reflections to subscribe to (described
		// in MIM) to detect joining federates. Note that since this
		// is a non-UCEF reflection, the `DataType` parameter here
		// does not really matter too much
		ObjectClass mimReflection = ObjectClass.Sub( FedManConstants.HLAFEDERATE_OBJECT_CLASS_NAME );
		for( String attributeName : FedManConstants.HLAFEDERATE_ATTRIBUTE_NAMES )
		{
			mimReflection.addAttributeSub( attributeName, DataType.UNKNOWN);
		}
		config.cacheObjectClasses( mimReflection );

		// The federation manager publishes certain UCEF simulation control interactions
		config.cacheInteractionClasses(
       		InteractionClass.Pub( SimPause.interactionName() ),
       		InteractionClass.Pub( SimResume.interactionName() ),
       		InteractionClass.Pub( SimEnd.interactionName() )
    	);

		// hear about callbacks *immediately*, rather than when evoked, otherwise
		// we don't know about joined federates until after the ticking starts
		config.setCallbacksAreImmediate( true ); // use CallbackModel.HLA_IMMEDIATE

		config.setLookAhead( 0.25 );

		// somebody set us up the FOM...
		try
		{
			String fomRootPath = "resources/";
			// modules
			String[] moduleFoms = { fomRootPath + "foms/FederationManager.xml",
			                        fomRootPath + "/challenge-response/fom/ChallengeResponse.xml" };
			config.addModules( FileUtils.urlsFromPaths(moduleFoms) );

			// join modules
			String[] joinModuleFoms = {};
			config.addJoinModules( FileUtils.urlsFromPaths(joinModuleFoms) );
		}
		catch( Exception e )
		{
			throw new UCEFException("Exception loading one of the FOM modules from disk", e);
		}
	}
}

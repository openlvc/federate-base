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
package gov.nist.ucef.hla.tools.fedman;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import gov.nist.ucef.hla.base.UCEFException;
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
public class FederationManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println(FedManConstants.UCEF_LOGO);
		System.out.println(FedManConstants.FEDMAN_LOGO);
		
		try
		{
			FedManFederate fedman = new FedManFederate( args );
			initializeConfig( fedman.getFederateConfiguration() );
			FedManHttpServer httpServer = new FedManHttpServer(fedman, 8080);
			httpServer.startServer();
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
		config.setFederationName( "ManagedFederation" );

		// a federation manager is allowed to create a required federation
		config.setCanCreateFederation( true );
		
		// set up object class reflections to subscribe to (described 
		// in MIM) to detect joining federates. Note that since this
		// is a non-UCEF reflection, the `DataType` parameter here 
		// does not really matter too much
		ObjectClass mimReflection = ObjectClass.Sub( FedManConstants.HLAFEDERATE_OBJECT_CLASS_NAME );
		for( String attributeName : FedManConstants.HLAFEDERATE_ATTRIBUTE_NAMES )
		{
			mimReflection.addAttributeSub( attributeName, DataType.UNKNOWN );
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
		config.setCallbacksAreEvoked( false ); // use CallbackModel.HLA_IMMEDIATE
		
		config.setLookAhead( 0.25 );
		
		// somebody set us up the FOM...
		try
		{
			String fomRootPath = FedManConstants.FOMS_ROOT;
			// modules
			String[] moduleFoms = { fomRootPath + "FederationManager.xml",
			                        fomRootPath + "SmartPingPong.xml" };
			config.addModules( urlsFromPaths(moduleFoms) );
			
			
			// join modules
			String[] joinModuleFoms = {};
			config.addJoinModules( urlsFromPaths(joinModuleFoms) );
		}
		catch( Exception e )
		{
			throw new UCEFException("Exception loading one of the FOM modules from disk", e);
		}
	}
	
	/**
	 * Utility function to set create a bunch of URLs from file paths
	 * 
	 * NOTE: if any of the paths don't actually correspond to a file that exists on the file system, 
	 *       a {@link UCEFException} will be thrown.
	 * 
	 * @return a list of URLs corresponding to the paths provided
	 */
	private static Collection<URL> urlsFromPaths(String[] paths)
	{
		List<URL> result = new ArrayList<>();
		
		try
		{
    		for(String path : paths)
    		{
    			File file = new File( path );
    			if(file.isFile())
    					result.add( new File( path ).toURI().toURL() );
    			else
    				throw new UCEFException("The file '%s' does not exist. " +
    										"Please check the file path.", file.getAbsolutePath());
    		}
		}
		catch( MalformedURLException e )
		{
			throw new UCEFException(e);
		}
		
		return result;
	}
}
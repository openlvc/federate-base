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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import gov.nist.ucef.hla.base.UCEFException;
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
	private static final Logger logger = LogManager.getLogger( FederationManager.class );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println(FedManConstants.UCEF_LOGO);
		System.out.println(FedManConstants.FEDMAN_LOGO);

		FedManHttpServer httpServer = null;
		FedManCmdLineProcessor argProcessor = new FedManCmdLineProcessor( FedManConstants.EXEC_NAME );
		try
		{
			argProcessor.processArgs( args );
		}
		catch( ParseException e )
		{
			System.err.println( "ERROR: "+ e.getMessage() );
			argProcessor.showHelp();
			System.exit( 1 );
		}
		if( !argProcessor.hasFederationExecName() )
		{
			System.err.println( "ERROR: No federation execution name was specified." );
			argProcessor.showHelp();
			System.exit( 1 );
		}
		if( !argProcessor.hasStartRequirements() )
		{
			System.err.println( "ERROR: No federation start requirements were specified." );
			argProcessor.showHelp();
			System.exit( 1 );
		}

		try
		{
			FedManFederate federate = new FedManFederate();
			initializeConfig( federate, argProcessor );

			try
			{
				FederateConfiguration config = federate.getFederateConfiguration();
				// modules
				String[] moduleFoms = { "fedman-fom.xml" };
				config.addModules( urlsFromPaths(moduleFoms) );

				// join modules
				String[] joinModuleFoms = {};
				config.addJoinModules( urlsFromPaths(joinModuleFoms) );
			}
			catch( Exception e )
			{
				throw new UCEFException("Exception loading one of the FOM modules from disk", e);
			}

			if( argProcessor.withHttpServiceActive() )
			{
				httpServer = new FedManHttpServer( federate, argProcessor.httpServicePort() );
				httpServer.startServer();
			}
			else
			{
				logger.info( "HTTP service disabled by config. Skipping HTTP service startup." );
			}

			federate.runFederate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println( e.getMessage() );
			System.err.println( "Cannot proceed - shutting down now." );
			System.exit( 1 );
		}

		if( httpServer != null )
		{
			httpServer.stopServer();
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
	private static void initializeConfig( FedManFederate federate,
	                                      FedManCmdLineProcessor argProcessor )
	{
		// configure federate specifics from command line args
		federate.setMaxTime( argProcessor.maxTime() );
		federate.setLogicalSecond( argProcessor.logicalSecond() );
		federate.setRealTimeMultiplier( argProcessor.realTimeMultiplier() );
		federate.setWallClockStepDelay( argProcessor.wallClockStepDelay() );
		federate.setStartRequirements( argProcessor.startRequirements() );

		FederateConfiguration config = federate.getFederateConfiguration();
		// a federation manager is ALWAYS allowed to create the required federation
		config.setCanCreateFederation( true );
		// hear about callbacks *immediately*, rather than when evoked, otherwise
		// we don't know about joined federates until after the ticking starts
		config.setCallbacksAreImmediate( true ); // use CallbackModel.HLA_IMMEDIATE
		// config.setLookAhead( 0.25 );

		// configure general federate configuration from command line args
		config.setFederationName( argProcessor.federationExecName() );
		config.setFederateName( argProcessor.federateName() );
		config.setFederateType( argProcessor.federateType() );
		config.setStepSize( argProcessor.logicalStepSize() );
		config.setLookAhead( argProcessor.logicalStepSize() );

		// TODO strictly speaking this should never be needed
		//      because the federation manager should not care
		//      about anything beyond its own pub/sub
		//      interactions and reflections. [adlaws]
		config.addModules( urlsFromPaths(argProcessor.baseFOMs() ) );

		// In order to detect joining federates, we subscribe to a "built in"
		// object reflection (and related attributes) described in the MIM.
		// NOTE: this is a bit of a special case, and since we decode it
		//       internally, the `DataType` parameter of `UNKNOWN` specified
		//       here for the attributes does not have any bearing on how the
		//       data in the reflection is unpacked.
		ObjectClass mimReflection = ObjectClass.Sub( FedManConstants.HLAFEDERATE_OBJECT_CLASS_NAME );
		for( String attributeName : FedManConstants.HLAFEDERATE_ATTRIBUTE_NAMES )
		{
			mimReflection.addAttributeSub( attributeName, DataType.UNKNOWN );
		}
		config.cacheObjectClasses( mimReflection );

		// The federation manager also publishes certain UCEF simulation control interactions
		config.cacheInteractionClasses( InteractionClass.Pub( SimStart.interactionName() ),
		                                InteractionClass.Pub( SimEnd.interactionName() ),
		                                InteractionClass.Pub( SimPause.interactionName() ),
		                                InteractionClass.Pub( SimResume.interactionName() ) );
	}

	/**
	 * Utility function to set create a bunch of URLs from file paths
	 *
	 * NOTE: if any of the paths don't actually correspond either to a file that exists on the
	 * file system or a system resource a {@link UCEFException} will be thrown.
	 *
	 * @return a list of URLs corresponding to the paths provided
	 */
	private static Collection<URL> urlsFromPaths( String[] paths )
	{
		Set<URL> result = new HashSet<>();

	    try
		{
			for( String path : paths )
			{
				File file = new File( path );
				if( file.isFile() )
				{
					result.add( new File( path ).toURI().toURL() );
				}
				else
				{
					URL fileUrl = FederationManager.class.getClassLoader().getResource( path );
					if( fileUrl != null )
					{
						result.add( fileUrl );
					}
					else
					{
						throw new UCEFException( "The file '%s' does not exist. " +
						                         "Please check the file path.",
						                         file.getAbsolutePath() );
					}
				}
			}
		}
		catch( MalformedURLException e )
		{
			throw new UCEFException( e );
		}

		return result;
	}
}

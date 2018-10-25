/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-gateway.
 *
 *   NOTICE:  All information contained herein is, and remains
 *            the property of Calytrix Technologies Pty Ltd.
 *            The intellectual and technical concepts contained
 *            herein are proprietary to Calytrix Technologies Pty Ltd.
 *            Dissemination of this information or reproduction of
 *            this material is strictly forbidden unless prior written
 *            permission is obtained from Calytrix Technologies Pty Ltd.
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package gov.nist.ucef.hla;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import gov.nist.ucef.hla.common.FederateBase;
import gov.nist.ucef.hla.common.FederateConfiguration;
import gov.nist.ucef.hla.common.InteractionBase;
import gov.nist.ucef.hla.common.NullUCEFFederateImplementation;
import gov.nist.ucef.hla.common.ObjectBase;
import gov.nist.ucef.hla.util.InputUtils;

public class TestUCEFFederate extends NullUCEFFederateImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TestUCEFFederate()
	{
		// nothing special here...
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void execute()
	{
		// TODO Read configuration from JSON(?)
		FederateConfiguration config = new FederateConfiguration( "TheUnitedFederationOfPlanets", 
		                                                          "Federate-" + new Date().getTime(), 
		                                                          "TestFederate" );

		// set up maps with classes and corresponding lists of attributes to 
		// be published and subscribed to
		String drinkBase = "HLAobjectRoot.Food.Drink.";
		config.addPublishedAtributes( drinkBase+"Soda", new String[] {"NumberCups", "Flavor"} );
		config.addSubscribedAtributes( drinkBase+"Soda", new String[] {"NumberCups", "Flavor"} );
		
		// set up lists of interactions to be published and subscribed to
		String foodServedBase = "HLAinteractionRoot.CustomerTransactions.FoodServed.";
		config.addPublishedInteraction( foodServedBase+"DrinkServed" );
		config.addSubscribedInteraction( foodServedBase+"DrinkServed" );
		
		// somebody set us up the FOM...
		try
		{
			String fomRootPath = "resources/foms/";
			// modules
			String[] moduleFoms = {fomRootPath+"RestaurantProcesses.xml", 
			                       fomRootPath+"RestaurantFood.xml", 
			                       fomRootPath+"RestaurantDrinks.xml"};
			config.addModules( urlsFromPaths(moduleFoms) );
			
			// join modules
			String[] joinModuleFoms = {fomRootPath+"RestaurantSoup.xml"};
			config.addJoinModules( urlsFromPaths(joinModuleFoms) );
		}
		catch( FileNotFoundException | MalformedURLException ex )
		{
			System.err.println( "Exception loading one of the FOM modules from disk: " + ex.getMessage() );
			ex.printStackTrace();
			
			// bail out now!
			System.err.println( "Cannot proceed - shutting down now." );
			System.exit( 1 );
		}

		try
		{
			// let's go...
			new FederateBase( config, this ).runFederate();
		}
		catch( Exception rtie )
		{
			// an exception occurred, just log the information and exit
			rtie.printStackTrace();
		}				
	}
	
	private Collection<URL> urlsFromPaths(String[] paths) throws MalformedURLException, FileNotFoundException
	{
		List<URL> result = new ArrayList<>();
		for(String path : paths)
		{
			File file = new File( path );
			if(file.isFile())
				result.add( new File( path ).toURI().toURL() );
			else
				throw new FileNotFoundException(String.format( "The file '%s' does not exist. Please check the file path.", path));
		}
		return result;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// LIFECYCLE /////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void doInitialisationTasks()
	{
		System.out.println("There are no initialisation tasks.");
	}

	@Override
	public void doPostAnnouncePreAchievePopulateTasks()
	{
		// wait until the user hits enter before proceeding, so there is time for
		// a human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to advance to Ready To Populate <<<<<<<<<<" );
		System.out.println( "Off we go..." );
	}
	
	@Override
	public void doPopulationTasks()
	{
		System.out.println("There are no population tasks.");
	}

	@Override
	public void doPostAnnouncePreAchieveRunTasks()
	{
		// wait until the user hits enter before proceeding, so there is time for
		// a human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to advance to Ready To Run <<<<<<<<<<" );
		System.out.println( "Off we go..." );
	}

	@Override
	public void runSimulation()
	{
		System.out.println("There is no simulation to run. Yet.");
		/*
		// in each iteration, we will update the attribute values of the object we registered,
		// and send an interaction.
		for( int i = 0; i < 10; i++ )
		{
			// 9.1 update the attribute values of the instance //
			updateAttributeValues( objectInstanceHandle );

			// 9.2 send an interaction
			ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create( 0 );
			// TODO - hard coded string
			sendInteraction( "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed", parameters );

			// 9.3 request a time advance and wait until we get it
			advanceTime( 1.0 );
			logger.error( "Time Advanced to " + fedamb.federateTime );
		}
		*/
	}

	@Override
	public void doPostAnnouncePreAchieveResignTasks()
	{
		// wait until the user hits enter before proceeding, so there is time for
		// a human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to advance to Ready To Resign <<<<<<<<<<" );
		System.out.println( "Off we go..." );
	}

	@Override
	public void doResignTasks()
	{
		System.out.println("There are no resign tasks.");
	}

	@Override
	public void doShutdownTasks()
	{
		System.out.println("There are no shutdown tasks.");
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// INTERACTIONS ///////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void handleInteraction( InteractionBase interaction )
	{
		StringBuilder builder = new StringBuilder( "Interaction Received:" );
		builder.append( interaction.toString() );
		System.out.println( builder.toString() );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// REFLECTIONS ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void handleReflection( ObjectBase objectBase )
	{
		StringBuilder builder = new StringBuilder( "Reflection for object:" );
		builder.append( objectBase.toString() );
		System.out.println( builder.toString() );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

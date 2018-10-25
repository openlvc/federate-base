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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nist.ucef.hla.common.FederateBase;
import gov.nist.ucef.hla.common.InteractionBase;
import gov.nist.ucef.hla.common.NullUCEFFederateImplementation;
import gov.nist.ucef.hla.common.ObjectBase;
import gov.nist.ucef.hla.util.InputUtils;

public class TestUCEFFederate extends NullUCEFFederateImplementation
{
	private String federateName;

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TestUCEFFederate(String federateName)
	{
		this.federateName = federateName;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void go()
	{
		// somebody set us up the FOM... 
		List<URL> modules = new ArrayList<>();
		List<URL> joinModules = new ArrayList<>();
		try
		{
			// modules
			String[] foms = {"RestaurantProcesses.xml", 
			                 "RestaurantFood.xml", 
			                 "RestaurantDrinks.xml"};
			for(String fom : foms)
			{
				modules.add( new File( "resources/foms/"+fom ).toURI().toURL() );
			}
			
			// join modules
			foms = new String[]{"RestaurantSoup.xml"};
			for(String fom : foms)
			{
				joinModules.add( new File( "resources/foms/"+fom ).toURI().toURL() );
			}
		}
		catch( MalformedURLException urle )
		{
			modules.clear();
			joinModules.clear();
			
			System.err.println( "Exception loading one of the FOM modules from disk: " + urle.getMessage() );
			urle.printStackTrace();
			
			// bail out now!
			System.exit( 1 );
		}

		// set up maps with classes and corresponding lists of attributes to 
		// be published and subscribed to
		String drinkBase = "HLAobjectRoot.Food.Drink.";
		Map<String, Set<String>> publishedAttributes = new HashMap<>();
		publishedAttributes.put( drinkBase+"Soda",
		                         new HashSet<>(Arrays.asList( new String[] {"NumberCups", "Flavor"} ) ));
		Map<String, Set<String>> subscribedAttributes = new HashMap<>();
		subscribedAttributes.put( drinkBase+"Soda",
		                          new HashSet<>(Arrays.asList( new String[] {"NumberCups", "Flavor"} ) ));
		
		// set up lists of interactions to be published and subscribed to
		String foodServedBase = "HLAinteractionRoot.CustomerTransactions.FoodServed.";
		Set<String> publishedInteractions = new HashSet<>(Arrays.asList( new String[] {foodServedBase+"DrinkServed"} ));
		Set<String> subscribedInteractions = new HashSet<>(Arrays.asList( new String[] {foodServedBase+"DrinkServed"} ));

		try
		{
			// let's go...
			new FederateBase( this, "TheUnitedFederationOfPlanets", federateName, "TestFederate")
				.addModules( modules )
    			.addJoinModules( joinModules )
				.addPublishedAtributes( publishedAttributes )
				.addSubscribedAtributes( subscribedAttributes )
				.addPublishedInteractions( publishedInteractions )
				.addSubscribedInteractions( subscribedInteractions )
				.runFederate();
		}
		catch( Exception rtie )
		{
			// an exception occurred, just log the information and exit
			rtie.printStackTrace();
		}				
	}
	
	
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
	
	@Override
	public void handleInteraction( InteractionBase interaction )
	{
		StringBuilder builder = new StringBuilder( "Interaction Received:" );
		builder.append( interaction.toString() );
		System.out.println( builder.toString() );
	}

	@Override
	public void handleReflection( ObjectBase objectBase )
	{
		StringBuilder builder = new StringBuilder( "Reflection for object:" );
		builder.append( objectBase.toString() );
		System.out.println( builder.toString() );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

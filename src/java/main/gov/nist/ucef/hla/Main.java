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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nist.ucef.hla.common.FederateBase;

public class Main
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println( "UCEF!" );
		
		// use "federateBase" plus a timestamp value as default name for federate,
		// can also be specified from the command line.
		String federateName = "federateBase-" + new Date().getTime();
		if( args.length != 0 )
		{
			federateName = args[0];
		}
		
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
			/*
			new FederateBase( "TheUnitedFederationOfPlanets",
			                  federateName, "TestFederate",  
			                  modules, joinModules, 
			                  publishedAttributes, subscribedAttributes,
			                  publishedInteractions, subscribedInteractions
			                ).runFederate();
			*/
			
			new FederateBase( "TheUnitedFederationOfPlanets", federateName, "TestFederate")
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

		/*
		try
		{
			URL url = new File( "resources/foms/RestaurantDrinks.xml" ).toURI().toURL();
			ObjectModel fom = FOM.parseFOM( url );
			Set<IDatatype> datatypes = fom.getDatatypes();
			for(IDatatype datatype: datatypes)
			{
				System.out.println( datatype.getName() + " " + datatype.getDatatypeClass().toString());
			}

			Set<ICMetadata> interactionClasses = fom.getAllInteractionClasses();
			for(ICMetadata interactionClass: interactionClasses)
			{
				for( PCMetadata parameter : interactionClass.getAllParameters())
				{
					System.out.println( parameter.getName() );
				}
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		System.exit( 0 );
	}
}

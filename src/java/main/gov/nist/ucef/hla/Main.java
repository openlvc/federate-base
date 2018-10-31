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

import java.util.Date;

import gov.nist.ucef.hla.common.FederateConfiguration;
import gov.nist.ucef.hla.common.NullFederateImplementation;
import gov.nist.ucef.hla.common.UCEFException;
import gov.nist.ucef.hla.util.FileUtils;

public class Main extends NullFederateImplementation
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
		
		try
		{
			FederateConfiguration config = makeConfig();
			new TestFederate( config ).execute();
		}
		catch(Exception e)
		{
			System.err.println( e.getMessage() );
			System.err.println( "Cannot proceed - shutting down now." );
			System.exit( 1 );
		}

		System.out.println( "Shutting down." );
		System.exit( 0 );
	}
	
	private static FederateConfiguration makeConfig()
	{
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
			config.addModules( FileUtils.urlsFromPaths(moduleFoms) );
			
			// join modules
			String[] joinModuleFoms = {fomRootPath+"RestaurantSoup.xml"};
			config.addJoinModules( FileUtils.urlsFromPaths(joinModuleFoms) );
		}
		catch( Exception e )
		{
			throw new UCEFException("Exception loading one of the FOM modules from disk", e);
		}	
		
		return config;
	}
}

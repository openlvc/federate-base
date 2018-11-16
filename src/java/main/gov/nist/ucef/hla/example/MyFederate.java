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
package gov.nist.ucef.hla.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLACodecUtils;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.UCEFException;

public class MyFederate extends FederateBase {
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static String[] FRUITS = {"Apple", "Banana", "Cherry", "Durian", "Elderberry", "Fig", "Grape",
	                                  "Honeydew", "iFruit", "Jackfruit", "Kiwi", "Lemon", "Mango", 
	                                  "Nectarine", "Orange", "Pear", "Quaggleberry", "Raspberry", 
	                                  "Strawberry", "Tangerine", "Ugli Fruit", "Voavanga", "Watermelon",
	                                  "Xigua", "Yangmei", "Zuchinni"};
	
	//----------------------------------------------------------
	//                    STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println( "      ___" );
		System.out.println( "    _/   \\_     _     _" );
		System.out.println( "   / \\   / \\   / \\   / \\" );
		System.out.println( "  ( U )-( C )-( E )-( F )" );
		System.out.println( "   \\_/   \\_/   \\_/   \\_/" );
		System.out.println( "  <-+-> <-+-----+-----+->" );
		System.out.println( " Universal CPS Environment" );
		System.out.println( "       for Federation" );
		System.out.println();
		
		try
		{
			new MyFederate().runFederate( makeConfig() );
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
	public MyFederate()
	{
		super();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Lifecycle Callback Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void beforeFederationJoin()
	{
		// no preparation required before federation join
	}

	@Override
	public void beforeReadyToPopulate()
	{
		// allow the user to control when we are ready to populate
		waitForUser("beforeReadyToPopulate() - press ENTER to continue");
	}

	@Override
	public void beforeReadyToRun()
	{
		// allow the user to control when we are ready to run
		// waitForUser("beforeReadyToRun() - press ENTER to continue");
	}

	@Override
	public void beforeFirstStep()
	{
		// no setup required before starting the first step in the simulation
	}
	
	@Override
	public boolean step( double currentTime )
	{
		System.out.println( "sending interaction(s) at time " + currentTime + "..." );
		HLAInteraction interaction = makeInteraction( "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed",
		                                              null );
		send( interaction, null );

		System.out.println( "sending attribute reflection(s) at time " + currentTime + "..." );
		for( HLAObject hlaObject : this.registeredObjects )
		{
			for( String attrName : hlaObject.getAttributeNames() )
			{
				hlaObject.set( attrName, randomFruit() );
			}
			update( hlaObject, null );
		}

		return (currentTime < 10.0);
	}

	@Override
	public void beforeReadyToResign()
	{
		// no cleanup required before resignation
	}

	@Override
	public void beforeExit()
	{
		// no cleanup required before exit
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveObjectRegistration( HLAObject hlaObject )
	{
		System.out.println( "receiveObjectRegistration():" );
		System.out.println( makeSummary(hlaObject) );
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject )
	{
		System.out.println( "receiveAttributeReflection():" );
		System.out.println( makeSummary(hlaObject) );
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject, double time )
	{
		receiveAttributeReflection( hlaObject );
	}

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction )
	{
		System.out.println( "receiveInteraction()" );
		System.out.println( makeSummary(hlaInteraction) );
	}

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction, double time )
	{
		receiveInteraction( hlaInteraction );
	}

	@Override
	public void receiveObjectDeleted( HLAObject hlaObject )
	{
		System.out.println( "receiveObjectDeleted():" );
		System.out.println( makeSummary(hlaObject) );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method simply blocks until the user presses ENTER, allowing for a pause in
	 * proceedings.
	 */
	private void waitForUser( String msg )
	{
		if( !( msg == null || "".equals( msg )) )
		{
			System.out.println( msg );
		}

		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		try
		{
			reader.readLine();
		}
		catch( Exception e )
		{
			System.err.println( "Error while waiting for user input: " + e.getMessage() );
			e.printStackTrace();
		}
	}
	
	/**
	 * This just generates a random string from a list of fruits
	 * 
	 * @return a random fruit name
	 */
	private String randomFruit()
	{
		return FRUITS[ThreadLocalRandom.current().nextInt(FRUITS.length)];
	}
	
	/**
	 * Provide a string representation of an HLAInteraction instance, suitable for logging and
	 * debugging purposes
	 */
	private String makeSummary( HLAInteraction instance )
	{
		HLACodecUtils hlaCodec = HLACodecUtils.instance();
		StringBuilder builder = new StringBuilder();
		builder.append( rtiamb.makeSummary( instance.getInteractionClassHandle() ) );
		builder.append( "\n" );
		for( Entry<String,byte[]> entry : instance.getState().entrySet() )
		{
			builder.append( "\t" );
			builder.append( entry.getKey() );
			builder.append( " = '" );
			// TODO at the moment this assumes that all values are strings, but going forward there
			// 		will need to be some sort of mapping of parameter names/handles to primitive
			//      types for parsing
			byte[] rawValue = entry.getValue();
			if( rawValue == null || rawValue.length == 0 )
			{
				builder.append( "UNDEFINED" );
			}
			else
			{
				builder.append("'").append( hlaCodec.asString( entry.getValue() ) ).append("'");
			}
			builder.append( "'\n" );
		}
		return builder.toString();
	}
	
	/**
	 * Provide a string representation of an HLAObject instance, suitable for logging and
	 * debugging purposes
	 */
	private String makeSummary( HLAObject instance )
	{
		HLACodecUtils hlaCodec = HLACodecUtils.instance();
		StringBuilder builder = new StringBuilder();
		builder.append( rtiamb.makeSummary( instance.getInstanceHandle() ) );
		builder.append( "\n" );
		for( Entry<String,byte[]> entry : instance.getState().entrySet() )
		{
			builder.append( "\t" );
			builder.append( entry.getKey() );
			builder.append( " = " );
			// TODO at the moment this assumes that all values are strings, but going forward there
			// 		will need to be some sort of mapping of attribute names/handles to primitive
			//      types for parsing
			byte[] rawValue = entry.getValue();
			if( rawValue == null || rawValue.length == 0 )
			{
				builder.append( "UNDEFINED" );
			}
			else
			{
				builder.append("'").append( hlaCodec.asString( entry.getValue() ) ).append("'");
			}
			builder.append( "'\n" );
		}
		return builder.toString();
	}
	
	/**
	 * Utility function to set up some useful configuration
	 * 
	 * @return a usefully populated {@link FederateConfiguration} instance
	 */
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
			config.addModules( urlsFromPaths(moduleFoms) );
			
			// join modules
			String[] joinModuleFoms = {fomRootPath+"RestaurantSoup.xml"};
			config.addJoinModules( urlsFromPaths(joinModuleFoms) );
		}
		catch( Exception e )
		{
			throw new UCEFException("Exception loading one of the FOM modules from disk", e);
		}	
		
		return config;
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
    										"Please check the file path.", path);
    		}
		}
		catch( MalformedURLException e )
		{
			throw new UCEFException(e);
		}
		
		return result;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

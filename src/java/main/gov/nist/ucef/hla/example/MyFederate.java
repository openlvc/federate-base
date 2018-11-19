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
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLACodecUtils;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.example.util.Constants;
import gov.nist.ucef.hla.example.util.FileUtils;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedRecord;

/**
 * Example federate for testing
 */
public class MyFederate extends FederateBase {
	//----------------------------------------------------------
	//                    STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println( Constants.UCEF_LOGO );
		
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
	private EncoderFactory encoder;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MyFederate()
	{
		super();
		
		this.encoder = HLACodecUtils.getEncoder();
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
		publishInteractionClass( JOINED_FEDERATION_INTERACTION );
		publishInteractionClass( RESIGNED_FEDERATION_INTERACTION );
		
		// notify the federation that we have joined
		send(makeFederationJoinedInteraction(), null);
		
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
		// here we end out two interactions:
		// - a DrinkServed interaction, which has no parameters
		// - a MainCourseServed interaction, which has several parameters
		System.out.println( "sending interaction(s) at time " + currentTime + "..." );
		HLAInteraction interaction = makeDrinkServedInteraction();
		send( interaction, null );
		interaction = makeMainCourseServedInteraction( randomBool(), 
		                                               randomBool(), randomBool(), randomBool(), 
		                                               randomBool(), randomBool(), randomBool() );
		send( interaction, null );

		// here we end out attribute reflections for all attributes of all registered pbjects:
		System.out.println( "sending attribute reflection(s) at time " + currentTime + "..." );
		for( HLAObject hlaObject : this.registeredObjects )
		{
			for( String attrName : hlaObject.getAttributeNames() )
			{
				hlaObject.set( attrName, randomFruit() );
			}
			update( hlaObject, null );
		}

		// keep going until time 10.0
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
		// notify the federation that we are resigning
		send(makeFederationResignedInteraction(), null);
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
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static String[] FRUITS = {"Apple", "Banana", "Cherry", "Durian", "Elderberry", "Fig", "Grape",
	                                  "Honeydew", "iFruit", "Jackfruit", "Kiwi", "Lemon", "Mango", 
	                                  "Nectarine", "Orange", "Pear", "Quaggleberry", "Raspberry", 
	                                  "Strawberry", "Tangerine", "Ugli Fruit", "Voavanga", "Watermelon",
	                                  "Xigua", "Yangmei", "Zuchinni"};

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
		config.addPublishedInteraction( foodServedBase+"MainCourseServed" );
		config.addSubscribedInteraction( foodServedBase+"MainCourseServed" );

		// somebody set us up the FOM...
		try
		{
			String fomRootPath = "resources/foms/";
			// modules
			String[] moduleFoms = {fomRootPath+"RestaurantProcesses.xml", 
			                       fomRootPath+"RestaurantFood.xml", 
			                       fomRootPath+"RestaurantDrinks.xml",
								   fomRootPath+"FedMan.xml"};
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
	 * This just generates a random boolean
	 * 
	 * @return a random boolean
	 */
	private boolean randomBool()
	{
		return Math.random() > 0.5;
	}
	
	/**
	 * A utility method to create a "DrinkServed" interaction
	 * 
	 * @return the interaction
	 */
	protected HLAInteraction makeDrinkServedInteraction()
	{
		// no parameters, so this is easy
		return makeInteraction( "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed", null );
	}
	
	/**
	 * A utility method to create a "MainCourseServed" interaction which looks a bit like this:
	 * 
	 *  TimlinessOk: true/false
	 *  TemperatureOk
	 *      #0 - entree: true/false
	 *      #1 - vege1: true/false
	 *      #2 - vege2: true/false
	 *  AccuracyOk:
	 *      #0 - entree: true/false
	 *      #1 - vege1: true/false
	 *      #2 - vege2: true/false
	 * 
	 * @param timelinessOK true if the the timeliness was OK, false otherwise
	 * @param entreeAccuracy true if the the entree was accurate, false otherwise
	 * @param vege1Accuracy true if vegetable 1 was accurate, false otherwise
	 * @param vege2Accuracy true if vegetable 2 was accurate, false otherwise
	 * @param entreeTemp true if the entree temperature was OK, false otherwise
	 * @param vege1Temp true if vegetable 1 temperature was OK, false otherwise
	 * @param vege2Temp true if vegetable 2 temperature was OK, false otherwise
	 * @return the interaction
	 */
	protected HLAInteraction makeMainCourseServedInteraction( boolean timelinessOK,
	                                                          boolean entreeAccuracy,
	                                                          boolean vege1Accuracy,
	                                                          boolean vege2Accuracy,
	                                                          boolean entreeTemp,
	                                                          boolean vege1Temp,
	                                                          boolean vege2Temp )
	{
		Map<String, byte[]> interactionParameters = new HashMap<>();
		
		interactionParameters.put( "TimlinessOk",
		                           HLACodecUtils.encode( encoder, timelinessOK ) );
		
		byte[] accuracyOK = makeServiceStatParameter(entreeAccuracy, vege1Accuracy, vege2Accuracy);
		byte[] temperatureOK = makeServiceStatParameter(entreeTemp, vege1Temp, vege2Temp);

		interactionParameters.put( "TemperatureOk", temperatureOK );
		interactionParameters.put( "AccuracyOk", accuracyOK );
		
		return makeInteraction( "HLAinteractionRoot.CustomerTransactions.FoodServed.MainCourseServed",
		                        interactionParameters );
	}
	
	/**
	 * Make a ServiceStat type fixed record parameter, which looks a bit like this:
	 * 
	 *  #0 - entree: true/false
	 *  #1 - vege1: true/false
	 *  #2 - vege2: true/false
	 *      
	 * @param entree true if the the entree was accurate, false otherwise
	 * @param vege1 true if vegetable 1 was accurate, false otherwise
	 * @param vege2 true if vegetable 2 was accurate, false otherwise
	 * @return the byte array for the ServiceStat
	 */
	protected byte[] makeServiceStatParameter( boolean entree, boolean vege1, boolean vege2)
	{
		HLAfixedRecord serviceStat = encoder.createHLAfixedRecord();
		serviceStat.add( encoder.createHLAboolean(entree) );
		serviceStat.add( encoder.createHLAboolean(vege1) );
		serviceStat.add( encoder.createHLAboolean(vege2) );
		return serviceStat.toByteArray();
	}
	
	/**
	 * Provides a human readable summary of a ServiceStat type fixed record parameter, which looks
	 * a bit like this:
	 * 
	 *  #0 - entree: true/false
	 *  #1 - vege1: true/false
	 *  #2 - vege2: true/false
	 * 
	 * @param rawData the raw bytes of the ServiceStat
	 * @return a human readable summary of the ServiceStat
	 */
	protected String summarizeServiceStatParameter( byte[] rawData )
	{
		HLAfixedRecord serviceStat = encoder.createHLAfixedRecord();
		serviceStat.add( encoder.createHLAboolean() );
		serviceStat.add( encoder.createHLAboolean() );
		serviceStat.add( encoder.createHLAboolean() );
		
		String summary = "Entree OK: UNKNOWN";
		summary += ", Vege 1 OK: UNKNOWN";
		summary += ", Vege 2 OK: UNKNOWN";
		try
		{
			serviceStat.decode( rawData );
			byte[] rawValue = serviceStat.get( 0 ).toByteArray();
			summary = "Entree OK: " + Boolean.toString( HLACodecUtils.asBoolean( encoder, rawValue ) );
			rawValue = serviceStat.get( 1 ).toByteArray();
			summary += ", Vege 1 OK: " + Boolean.toString( HLACodecUtils.asBoolean( encoder, rawValue ) );
			rawValue = serviceStat.get( 2 ).toByteArray();
			summary += ", Vege 2 OK: " + Boolean.toString( HLACodecUtils.asBoolean( encoder, rawValue ) );
		}
		catch( DecoderException e )
		{
			// ignore
		}
		return summary;
	}
	
	/**
	 * Provide a string representation of an HLAInteraction instance, suitable for logging and
	 * debugging purposes
	 * 
	 * @param instance the interaction
	 * @return a human readable summary
	 */
	private String makeSummary( HLAInteraction instance )
	{
		StringBuilder builder = new StringBuilder();
		builder.append( rtiamb.makeSummary( instance.getInteractionClassHandle() ) );
		builder.append( "\n" );
		for( Entry<String,byte[]> entry : instance.getState().entrySet() )
		{
			String parameterName = entry.getKey();
			builder.append( "\t" );
			builder.append( parameterName );
			builder.append( " = " );
			
			byte[] rawValue = entry.getValue();
			if( rawValue == null || rawValue.length == 0 )
			{
				builder.append( "UNDEFINED" );
			}
			else
			{
				try 
				{
    				String value = "";
    				// decode using parameter name to detect data type
    				if( "TimlinessOk".equals( parameterName ) )
    				{
						// boolean types
    					value = Boolean.toString( HLACodecUtils.asBoolean( encoder, rawValue ) );
    				}
					else if( "TemperatureOk".equals( parameterName ) || 
						     "AccuracyOk".equals( parameterName ) )
					{
						// fixed record types
						value = summarizeServiceStatParameter( rawValue );
					}
    				else
    				{
						// assume anything else is a unicode String
    					value = '"' + HLACodecUtils.asUnicodeString( encoder, rawValue ) + '"';
    				}
    				builder.append( value );
				}
				catch(Exception e)
				{
					builder.append( "Unable to decode value: " + e.getMessage() );
				}
			}
				
			builder.append( "\n" );
		}
		return builder.toString();
	}
	
	/**
	 * Provide a string representation of an HLAObject instance, suitable for logging and
	 * debugging purposes
	 * 
	 * @param instance the object
	 * @return a human readable summary
	 */
	private String makeSummary( HLAObject instance )
	{
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
				builder.append("'");
				builder.append( HLACodecUtils.asUnicodeString( encoder, rawValue ) );
				builder.append("'");
			}
			builder.append( "\n" );
		}
		return builder.toString();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	// cache of object instances which we have registered with the RTI
	protected Set<HLAObject> registeredObjects;
	private EncoderFactory encoder;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MyFederate()
	{
		super();
		
		registeredObjects = new HashSet<>();
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
		publishInteraction( JOINED_FEDERATION_INTERACTION );
		publishInteraction( RESIGNED_FEDERATION_INTERACTION );
		
		// notify the federation that we have joined
		send(makeFederationJoinedInteraction(), null);
		
		// allow the user to control when we are ready to populate
		waitForUser("beforeReadyToPopulate() - press ENTER to continue");
	}

	@Override
	public void beforeReadyToRun()
	{
		// we register the objects we will be handling here 
		registerObjects();
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
		sendInteraction( interaction, null );
		interaction = makeMainCourseServedInteraction( randomBool(), 
		                                               randomBool(), randomBool(), randomBool(), 
		                                               randomBool(), randomBool(), randomBool() );
		sendInteraction( interaction, null );

		// here we end out attribute reflections for all attributes of all registered objects:
		System.out.println( "sending attribute reflection(s) at time " + currentTime + "..." );
		for( HLAObject hlaObject : this.registeredObjects )
		{
			hlaObject.setValue( FLAVOR_ATTR_ID, randomFruit() );
			hlaObject.setValue( NUMBER_CUPS_ATTR_ID, randomInt( 1, 5 ) );
			updateAttributeValues( hlaObject, null );
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
		// we de-register our objects instances here before exiting  
		deregisterObjects();
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
	private static final String DRINK_BASE = "HLAobjectRoot.Food.Drink.";
	private static final String SODA_OBJECT_ID = DRINK_BASE + "Soda";
	private static final String NUMBER_CUPS_ATTR_ID = "NumberCups";
	private static final String FLAVOR_ATTR_ID = "Flavor";

	private static final String FOOD_SERVED_BASE = "HLAinteractionRoot.CustomerTransactions.FoodServed.";
	private static final String DRINK_SERVED_INTERACTION_ID = FOOD_SERVED_BASE + "DrinkServed";
	private static final String MAIN_COURSE_SERVED_INTERACTION_ID = FOOD_SERVED_BASE + "MainCourseServed";
	private static final String TIMELINESS_OK_PARAM = "TimlinessOk";
	private static final String ACCURACY_OK_PARAM = "AccuracyOk";
	private static final String TEMPERATURE_OK_PARAM = "TemperatureOk";
	
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
		String[] attributes = new String[]{ NUMBER_CUPS_ATTR_ID, FLAVOR_ATTR_ID };
		config.addPublishedAtributes( SODA_OBJECT_ID, attributes );
		config.addSubscribedAtributes( SODA_OBJECT_ID, attributes );

		// set up lists of interactions to be published and subscribed to
		config.addPublishedInteraction( DRINK_SERVED_INTERACTION_ID );
		config.addSubscribedInteraction( DRINK_SERVED_INTERACTION_ID );
		config.addPublishedInteraction( MAIN_COURSE_SERVED_INTERACTION_ID );
		config.addSubscribedInteraction( MAIN_COURSE_SERVED_INTERACTION_ID );

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
	 * Registers any object instances that we are handling
	 */
	private void registerObjects()
	{
		// TODO this is just test code which simply registers an instance for every 
		//      published attribute entry in the configuration. It's possible that the
		//      implementation might require single or multiple instances (or none, though
		//      that would be pointless) on a per entry basis.
		for(String className: configuration.getPublishedAttributes().keySet())
		{
			this.registeredObjects.add( makeObjectInstance( className ) );
		}
	}
	
	/**
	 * De-registers any object instances that we were handling once things have finished up
	 */
	private void deregisterObjects()
	{
		Set<HLAObject> deleted = new HashSet<>();
		for( HLAObject obj : this.registeredObjects )
		{
			deleted.add( deleteObjectInstance( obj ) );
		}
		this.registeredObjects.removeAll( deleted );
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
	 * This just generates a random integer within the min/max range 
	 * 
	 * @param min the minimum allowed value (inclusive)
	 * @param max the maximum allowed value (inclusive)
	 * @return a random integer within the min/max range
	 */
	private int randomInt(int min, int max)
	{
		int range = max - min;
		return min + ThreadLocalRandom.current().nextInt(range + 1);
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
		return makeInteraction( DRINK_SERVED_INTERACTION_ID, null );
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
		
		interactionParameters.put( TIMELINESS_OK_PARAM,
		                           HLACodecUtils.encode( encoder, timelinessOK ) );
		
		byte[] accuracyOK = makeServiceStatParameter( entreeAccuracy, vege1Accuracy, vege2Accuracy );
		byte[] temperatureOK = makeServiceStatParameter( entreeTemp, vege1Temp, vege2Temp );

		interactionParameters.put( TEMPERATURE_OK_PARAM, temperatureOK );
		interactionParameters.put( ACCURACY_OK_PARAM, accuracyOK );
		
		return makeInteraction( MAIN_COURSE_SERVED_INTERACTION_ID, interactionParameters );
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
		builder.append( rtiamb.makeSummary( instance ) );
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
    				// decode using parameter name to determine data type
    				if( TIMELINESS_OK_PARAM.equals( parameterName ) )
    				{
						// boolean types
    					value = Boolean.toString( HLACodecUtils.asBoolean( encoder, rawValue ) );
    				}
					else if( TEMPERATURE_OK_PARAM.equals( parameterName ) || 
						     ACCURACY_OK_PARAM.equals( parameterName ) )
					{
						// fixed record types
						value = summarizeServiceStatParameter( rawValue );
					}
    				else
    				{
						// assume anything else is a unicode String
    					value = '"' + HLACodecUtils.asString( encoder, rawValue ) + '"';
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
		builder.append( rtiamb.makeSummary( instance ) );
		builder.append( "\n" );
		for( Entry<String,byte[]> entry : instance.getState().entrySet() )
		{
			String attributeName = entry.getKey();
			builder.append( "\t" );
			builder.append( attributeName );
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
					// decode using attribute name to determine data type
					if( NUMBER_CUPS_ATTR_ID.equals( attributeName ) )
					{
						// boolean types
						value = Integer.toString( HLACodecUtils.asInt( encoder, rawValue ) );
					}
					else
					{
						// anything else is a unicode String
						value = '"' + HLACodecUtils.asString( encoder, rawValue ) + '"';
					}
					builder.append( value );
				}
				catch( Exception e )
				{
					builder.append( "Unable to decode value: " + e.getMessage() );
				}

			}
			builder.append( "\n" );
		}
		return builder.toString();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

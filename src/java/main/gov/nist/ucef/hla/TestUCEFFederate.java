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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Consumer;

import gov.nist.ucef.hla.common.FederateBase;
import gov.nist.ucef.hla.common.FederateConfiguration;
import gov.nist.ucef.hla.common.InstanceBase;
import gov.nist.ucef.hla.common.InteractionBase;
import gov.nist.ucef.hla.common.NullFederateImplementation;
import gov.nist.ucef.hla.util.InputUtils;
import gov.nist.ucef.hla.util.RTIUtils;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.time.HLAfloat64Time;

public class TestUCEFFederate extends NullFederateImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateBase federateBase;
	private FederateConfiguration federateConfiguration;
	
	private int tickCount;
	private int maxTickCount;
	private double timeStep;

	private String objectClassIdentifier = "HLAobjectRoot.Food.Drink.Soda";
	private String interactionIdentifier = "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed";
	
	private Map<String, Consumer<String>> attributeSubscriptionHandlers;
	private Map<String, Consumer<Map<String, String>>> interactionSubscriptionHandlers;

	private InstanceBase instanceBase;
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// GENERATED - DON'T TOUCH //////////////////////////////////
	/////////////////////////////////   NOT YET, BUT SOON...  //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private int numberCups = 0;
	private String flavor = null;
	private String foodServed = null;
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TestUCEFFederate()
	{
		// nothing special here...
		this.tickCount = 0;
		this.maxTickCount = 10;
		this.timeStep = 1.0;
		
		this.attributeSubscriptionHandlers = new HashMap<>();
		this.interactionSubscriptionHandlers = new HashMap<>();
		
		this.federateConfiguration = new FederateConfiguration( "TheUnitedFederationOfPlanets", 
		                                                        "Federate-" + new Date().getTime(), 
																"TestFederate" );
		
		this.federateBase = initialiseFederateBase(this.federateConfiguration);
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void execute()
	{
		if(this.federateBase != null)
		{
			try
			{
				this.federateBase.runFederate();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
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
		RTIUtils rtiUtils = this.federateBase.getRTIUtils();
		
		ObjectClassHandle classhandle = rtiUtils.getClassHandleFromClassIdentifier( this.objectClassIdentifier );
		Collection<String> attributes = Arrays.asList( new String[] {"NumberCups", "Flavor"} );
		this.instanceBase = new InstanceBase( rtiUtils, classhandle, attributes );
		this.federateBase.registerInstanceBase( instanceBase);
		
		// wait until the user hits enter before proceeding, so there is time for
		// a human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to advance to Ready To Run <<<<<<<<<<" );
		System.out.println( "Off we go..." );
	}

	@Override
	public boolean shouldContinueSimulation()
	{
		// a very complicated choice about whether the simulation has finished or not
		return this.tickCount < this.maxTickCount;
	}
	
	@Override
	public void tickSimulation()
	{
		// in each iteration, we will update the attribute values of the object we registered,
		// and send an interaction.
		// 9.1 update the attribute values of the instance
		updateAttributeValues();
		// 9.2 send an interaction
		sendInteraction();
		
		// important - update the tick count otherwise we'll loop forever!
		this.tickCount ++;
	}
	
	@Override
	public double getTimeStep()
	{
		return this.timeStep;
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
		System.out.println("There are no resignation tasks.");
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
	public void handleInteractionSubscription( InteractionBase interaction )
	{
		String interactionID = interaction.getIdentifier();
		Map<String,String> paramsAndValues = interaction.getParameterNamesAndValues();
		
		System.out.println( "Interaction of type " + interactionID );
		
		Consumer<Map<String, String>> func = this.interactionSubscriptionHandlers.get( interactionID );
		if(func != null)
		{
			func.accept( paramsAndValues );
		}
		else
		{
			_uNkNoWnInTeRaCtIoN(interactionID, paramsAndValues); 				
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// REFLECTIONS ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void handleAttributeSubscription( InstanceBase instanceBase )
	{
		System.out.println( "Reflection of type " + instanceBase.getClassIdentifier() + " for " + instanceBase.getInstanceHandle() );
		Map<String,String> attrsAndValues = instanceBase.getAttributeNamesAndValues();
		// TODO
		for(Entry<String, String> entry : attrsAndValues.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			
			Consumer<String> func = attributeSubscriptionHandlers.get( key );
			if(func != null)
			{
				func.accept( value );
			}
			else
			{
				_uNkNoWnAtTrIbUtEuPdAtE(key, value);
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// GENERATED - DON'T TOUCH //////////////////////////////////
	/////////////////////////////////   NOT YET, BUT SOON...  //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public int getNumberCups()
	{
		return this.numberCups;
	}
	
	public String getFlavor()
	{
		return this.flavor;
	}
	
	public String getFoodServed()
	{
		return this.foodServed;
	}
	
	private void _numberCups(String value)
	{
		System.out.println( String.format( "_numberCups(%s);", value ) );
		this.numberCups = Integer.parseInt( value );
	}
	
	private void _flavor(String value)
	{
		System.out.println( String.format( "_flavor(%s);", value ) );
		this.flavor = value;
	}
	
	private void _uNkNoWnAtTrIbUtEuPdAtE(String key, String value)
	{
		System.out.println( String.format( "_uNkNoWnAtTrIbUtEuPdAtE(%s);", value ) );
	}
	
	private void _drinkServed(Map<String, String> parameters)
	{
		StringBuilder builder = new StringBuilder();
		if(parameters.isEmpty())
		{
			builder.append( "<No Parameters>" );
		}
		else
		{
			builder.append( mapToString(parameters) );
		}
		System.out.println( String.format( "_drinkServed(%s);", builder.toString() ) );
		// TODO actually "handle" the interaction, whatever that means
	}
	
	private void _uNkNoWnInTeRaCtIoN(String key, Map<String, String> parameters)
	{
		StringBuilder builder = new StringBuilder();
		if(parameters.isEmpty())
		{
			builder.append( "<No Parameters>" );
		}
		else
		{
			builder.append( mapToString(parameters) );
		}
		System.out.println( String.format( "_uNkNoWnInTeRaCtIoN(%s);", builder.toString() ) );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Utility Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private FederateBase initialiseFederateBase(FederateConfiguration config)
	{
		// set up maps with classes and corresponding lists of attributes to 
		// be published and subscribed to
		String drinkBase = "HLAobjectRoot.Food.Drink.";
		config.addPublishedAtributes( drinkBase+"Soda", new String[] {"NumberCups", "Flavor"} );
		config.addSubscribedAtributes( drinkBase+"Soda", new String[] {"NumberCups", "Flavor"} );
		
		
		// set up lists of interactions to be published and subscribed to
		String foodServedBase = "HLAinteractionRoot.CustomerTransactions.FoodServed.";
		config.addPublishedInteraction( foodServedBase+"DrinkServed" );
		config.addSubscribedInteraction( foodServedBase+"DrinkServed" );
		
		////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////// GENERATED - DON'T TOUCH //////////////////////////////////
		/////////////////////////////////   NOT YET, BUT SOON...  //////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////
		this.attributeSubscriptionHandlers.put( "NumberCups", this::_numberCups );
		this.attributeSubscriptionHandlers.put( "Flavor", this::_flavor );
		this.interactionSubscriptionHandlers.put( "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed", this::_drinkServed );
		////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////
		
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
		
		// let's go...
		return new FederateBase( this, config );
	}
	
	/**
	 * This method just updates the values of our object instances. The actual implementation will
	 * depend on the intent of the simulation 
	 */
	private void updateAttributeValues()
	{
		int randomValue = 101 + new Random().nextInt( 3 );
		this.instanceBase.updateAttribute("NumberCups", getTimeAsShort())
						 .updateAttribute("Flavor", randomValue);
		
		// do the actual update
		this.instanceBase.publish( generateTag() );
		
		// send another update, this time with a timestamp:
		RTIUtils rtiUtils = this.federateBase.getRTIUtils();
		HLAfloat64Time time = rtiUtils.makeTime( this.federateBase.getFederateTime() + 
		                                         federateConfiguration.getLookAhead() );
		this.instanceBase.publish( generateTag(), time );
	}
	
	private void sendInteraction()
	{
		RTIUtils rtiUtils = this.federateBase.getRTIUtils();
        InteractionClassHandle interactionHandle = rtiUtils.getHandleFromInteractionIdentifier( this.interactionIdentifier );
		InteractionBase interaction = new InteractionBase( rtiUtils, interactionHandle, null, null );
		interaction.publish();
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

	private byte[] generateTag()
	{
		return ("(timestamp) " + System.currentTimeMillis()).getBytes();
	}
	
	private short getTimeAsShort()
	{
		return (short)this.federateBase.getFederateTime();
	}
	
	private String mapToString(Map<String, String> map)
	{
		StringBuilder builder = new StringBuilder();
		map.entrySet().forEach( (entry) -> builder.append( entry.getKey() ).append( "=\"" ).append( entry.getValue() ).append( "\", " ) );
		return builder.toString();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

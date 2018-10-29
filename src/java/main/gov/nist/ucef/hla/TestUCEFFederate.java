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
import java.util.Map;
import java.util.Random;

import gov.nist.ucef.hla.common.FederateBase;
import gov.nist.ucef.hla.common.FederateConfiguration;
import gov.nist.ucef.hla.common.InteractionBase;
import gov.nist.ucef.hla.common.NullUCEFFederateImplementation;
import gov.nist.ucef.hla.common.ObjectBase;
import gov.nist.ucef.hla.util.InputUtils;
import gov.nist.ucef.hla.util.RTIUtils;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.time.HLAfloat64Time;

public class TestUCEFFederate extends NullUCEFFederateImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateBase federateBase;
	
	private int tickCount;
	private int maxTickCount;
	private double timeStep;
	private FederateConfiguration federateConfiguration;

	private String objectIdentifier = "HLAobjectRoot.Food.Drink.Soda";
	private String interactionIdentifier = "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed";
	
	private ObjectInstanceHandle objectInstanceHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TestUCEFFederate()
	{
		// nothing special here...
		this.tickCount = 0;
		this.maxTickCount = 10;
		this.timeStep = 1.0;
		
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
		this.objectInstanceHandle = rtiUtils.registerObject( this.federateConfiguration.getFederateName(), 
		                                                     this.objectIdentifier );
		
		if(this.objectInstanceHandle == null)
		{
			System.exit( 1 );
		}
		
		// wait until the user hits enter before proceeding, so there is time for
		// a human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to advance to Ready To Run <<<<<<<<<<" );
		System.out.println( "Off we go..." );
	}

	@Override
	public boolean shouldTick()
	{
		// a very complicated choice about whether the simulation has finished or not
		return this.tickCount < this.maxTickCount;
	}
	
	@Override
	public void tick()
	{
		// in each iteration, we will update the attribute values of the object we registered,
		// and send an interaction.
		// 9.1 update the attribute values of the instance
		updateAttributeValues( this.objectInstanceHandle );
		// 9.2 send an interaction
		sendInteraction( this.interactionIdentifier );
		
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
		StringBuilder builder = new StringBuilder( "Interaction Received:\n" );
		builder.append( "\t" + interaction.getName() + "\n" );
		Map<String,String> paramsAndValues = interaction.getParameterNamesAndValues();
		if(paramsAndValues.isEmpty())
		{
			builder.append( "\t\t<No Parameters>" );
		}
		else
		{
			paramsAndValues.entrySet().forEach( (x) -> builder.append( String.format( "\t\t%s = %s\n", x.getKey(), x.getValue() ) ) );
		}
		System.out.println( builder.toString() );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// REFLECTIONS ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void handleReflection( ObjectBase objectBase )
	{
		StringBuilder builder = new StringBuilder( "Reflection for object:\n" );
		builder.append( "\t" + objectBase.getName() + "\n" );
		Map<String,String> attrsAndValues = objectBase.getAttributeNamesAndValues();
		if(attrsAndValues.isEmpty())
		{
			builder.append( "\t\t<No Attributes>" );
		}
		else
		{
			attrsAndValues.entrySet().forEach( (x) -> builder.append( String.format( "\t\t%s = %s\n", x.getKey(), x.getValue() ) ) );
		}
		System.out.println( builder.toString() );
	}
	
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
		return new FederateBase( this, config);
	}
	
	/**
	 * This method will update all the values of the given object instance. It will set the
	 * flavour of the soda to a random value from the options specified in the FOM (Cola - 101,
	 * Orange - 102, RootBeer - 103, Cream - 104) and it will set the number of cups to the same
	 * value as the current time.
	 * <p/>
	 * Note that we don't actually have to update all the attributes at once, we could update them
	 * individually, in groups or not at all!
	 */
	private void updateAttributeValues( ObjectInstanceHandle objectinstanceHandle )
	{
		RTIUtils rtiUtils = this.federateBase.getRTIUtils();
		///////////////////////////////////////////////
		// create the necessary container and values //
		///////////////////////////////////////////////
		ObjectClassHandle objectClassHandle = rtiUtils.getClassHandleFromInstanceHandle( objectinstanceHandle );

		// create a new map with an initial capacity - this will grow as required
		AttributeHandleValueMap attributes = rtiUtils.makeAttributeMap( 2 );

		// create the collection to store the values in, as you can see this is quite a lot of work. You
		// don't have to use the encoding helpers if you don't want. The RTI just wants a byte[]

		// generate the value for the number of cups (same as the timestep)
		rtiUtils.updateAttribute( objectClassHandle, "NumberCups", getTimeAsShort(), attributes );

		// generate the value for the flavour on our magically flavour changing drink
		// the values for the enum are defined in the FOM
		int randomValue = 101 + new Random().nextInt( 3 );
		rtiUtils.updateAttribute( objectClassHandle, "Flavor", randomValue, attributes );

		//////////////////////////
		// do the actual update //
		//////////////////////////
		rtiUtils.publishAttributes( objectinstanceHandle, attributes, generateTag() );

		// note that if you want to associate a particular timestamp with the
		// update. here we send another update, this time with a timestamp:
		HLAfloat64Time time = rtiUtils.makeTime( this.federateBase.getFederateTime() + 
		                                         federateConfiguration.getLookAhead() );
		rtiUtils.publishAttributes( objectinstanceHandle, attributes, generateTag(), time );
	}
	
	private void sendInteraction(String interactionIdentifier)
	{
		RTIUtils rtiUtils = this.federateBase.getRTIUtils();
		rtiUtils.publishInteraction(interactionIdentifier);
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

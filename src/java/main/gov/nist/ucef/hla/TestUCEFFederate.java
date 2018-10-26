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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.common.FederateBase;
import gov.nist.ucef.hla.common.FederateConfiguration;
import gov.nist.ucef.hla.common.InteractionBase;
import gov.nist.ucef.hla.common.NullUCEFFederateImplementation;
import gov.nist.ucef.hla.common.ObjectBase;
import gov.nist.ucef.hla.util.InputUtils;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.RTIexception;

public class TestUCEFFederate extends NullUCEFFederateImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getFormatterLogger( TestUCEFFederate.class );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateBase federateBase;
	
	private int tickCount;
	private int maxTickCount;
	private double timeStep;

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
		
		this.federateBase = initialiseFederateBase();
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

	private FederateBase initialiseFederateBase()
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
			return new FederateBase( config, this );
		}
		catch( Exception rtie )
		{
			// an exception occurred, just log the information and exit
			rtie.printStackTrace();
		}				
		
		return null;
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
		
		try
		{
			this.objectInstanceHandle = this.federateBase.registerObject( this.objectIdentifier );
			logger.info( String.format( "Registered Object '%s' (handle = %s)",
			                            this.objectIdentifier , objectInstanceHandle ) );
		}
		catch( RTIexception e )
		{
			e.printStackTrace();
		}
		
		// wait until the user hits enter before proceeding, so there is time for
		// a human to interact with other federates.
		InputUtils.waitForUser( " >>>>>>>>>> Press Enter to advance to Ready To Run <<<<<<<<<<" );
		System.out.println( "Off we go..." );
	}

	@Override
	public boolean shouldTick()
	{
		return this.tickCount < this.maxTickCount;
	}
	
	@Override
	public void tick()
	{
		// in each iteration, we will update the attribute values of the object we registered,
		// and send an interaction.
		try
		{
			// 9.1 update the attribute values of the instance
			// TODO - this needs to happen in this class, not be done by the FederateBase instance
			this.federateBase.updateAttributeValues( this.objectInstanceHandle );
			// 9.2 send an interaction
			// TODO - this needs to happen in this class, not be done by the FederateBase instance
			this.federateBase.sendInteraction( this.interactionIdentifier );
		}
		catch( RTIexception e )
		{
			e.printStackTrace();
		}
		
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
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

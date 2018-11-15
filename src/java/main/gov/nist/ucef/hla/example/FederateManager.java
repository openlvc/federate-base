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
import java.util.Map.Entry;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLACodecUtils;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.util.Constants;
import gov.nist.ucef.hla.example.util.FileUtils;

public class FederateManager extends FederateBase {
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String FEDMAN_LOGO =
		"     ______         ____  ___\n" +
		"    / ____/__  ____/ /  |/  /___ _____\n" + 
		"   / /_  / _ \\/ __  / /\\|_/ / __ `/ __\\\n" +
		"  / __/ /  __/ /_/ / /  / / /_/ / / / /\n" +
		" /_/    \\___/\\__,_/_/  /_/\\__,_/_/ /_/\n" + 		
		"------------ Federate Manager -----------\n";
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println(Constants.UCEF_LOGO);
		System.out.println(FEDMAN_LOGO);
		
		try
		{
			new FederateManager().runFederate( makeConfig() );
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
	private long nextTimeAdvance;
	private double timeAdvanceFrequency; // in Hertz
	private double timeAdvanceStep;
	private double maxTime;
	
	private long wallClockStepDelay;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateManager()
	{
		super();
		
		this.nextTimeAdvance = -1;
		this.timeAdvanceFrequency = 4; // Hz
		this.maxTime = 15.0;
		
		this.wallClockStepDelay = (long)(1000.0 / this.timeAdvanceFrequency);
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
		subscribeInteractionClass( JOINED_FEDERATION_INTERACTION );
		subscribeInteractionClass( RESIGNED_FEDERATION_INTERACTION );
		
		// pre-announce all UCEF synchronization points
		for( UCEFSyncPoint syncPoint : UCEFSyncPoint.values() )
		{
			registerSyncPointAndWaitForAnnounce( syncPoint, null );
		}
		
		// allow the user to control when we are ready to populate
		// TODO what we actually want to do here is be listening out somehow 
		//      for federates joining the federation and what types they are
		waitForUser("beforeReadyToPopulate()\nWaiting for federates to join.\n(press ENTER to continue)");
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
		this.timeAdvanceStep = configuration.getLookAhead();
		this.nextTimeAdvance = System.currentTimeMillis();
	}
	
	@Override
	public boolean step( double currentTime )
	{
		double federateTime = fedamb.getFederateTime();
		System.out.println( String.format( "Federate time is %.3f ", federateTime ) );
		
		if(currentTime < maxTime)
		{
			this.nextTimeAdvance += wallClockStepDelay;
			waitUntil( this.nextTimeAdvance );
			System.out.println( String.format( "Advancing time to %.3f...",
			                                   (federateTime + this.timeAdvanceStep) ) );
			return true;
		}
		
		System.out.println( "Maximum simulation time reached.");
		return false;
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
		// federate manager does not care about this
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject )
	{
		// federate manager does not care about this
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject, double time )
	{
		// federate manager does not care about this
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
		// federate manager does not care about this
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void waitUntil( long timestamp )
	{
		long delay = timestamp - System.currentTimeMillis();
		if(delay <= 0)
			return;
		
		try
		{
			Thread.sleep( delay );
		}
		catch( InterruptedException e )
		{
			throw new UCEFException( "Could not wait", e );
		}
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
			String parameterName = entry.getKey();
			builder.append( "\t" );
			builder.append( parameterName );
			builder.append( " = " );
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
				if("FederateHandle".equals( parameterName) )
				{
					builder.append("'").append( hlaCodec.asInt( rawValue ) ).append("'");
				}
				else
				{
					builder.append("'").append( hlaCodec.asString( rawValue ) ).append("'");
				}
			}
			builder.append( "\n" );
		}
		return builder.toString();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Utility function to set up some useful configuration
	 * 
	 * @return a usefully populated {@link FederateConfiguration} instance
	 */
	private static FederateConfiguration makeConfig()
	{
		FederateConfiguration config = new FederateConfiguration( "TheUnitedFederationOfPlanets", 
			                                                      "FederateManager", 
																  "FederateManager" );
		
		config.setLookAhead( 0.25 );
		
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
}

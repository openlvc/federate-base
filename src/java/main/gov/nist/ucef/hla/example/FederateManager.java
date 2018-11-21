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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.util.Constants;
import gov.nist.ucef.hla.example.util.FileUtils;

/**
 *		            ___
 *		          _/   \_     _     _
 *		         / \   / \   / \   / \
 *		        ( U )-( C )-( E )-( F )
 *		         \_/   \_/   \_/   \_/
 *		        <-+-> <-+-----+-----+->
 *		       Universal CPS Environment
 *		             for Federation
 * 		     ______         ____  ___
 * 		    / ____/__  ____/ /  |/  /___ _____ 
 * 		   / /_  / _ \/ __  / /\|_/ / __ `/ __\
 * 		  / __/ /  __/ /_/ / /  / / /_/ / / / /
 * 		 /_/    \___/\__,_/_/  /_/\__,_/_/ /_/ 		
 * 		------------ Federate Manager ----------
 *
 */
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
		"------------ Federate Manager ----------\n";
	
	private static final String FEDMAN_FEDERATE_TYPE = "FederateManager";
	private static final String FEDMAN_FEDERATE_NAME = "FederateManager";
	
	private static final String HLAFEDERATE_OBJECT_CLASS_NAME = "HLAobjectRoot.HLAmanager.HLAfederate";
	private static final String HLAFEDERATE_TYPE_ATTR = "HLAfederateType";
	private static final String HLAFEDERATE_NAME_ATTR = "HLAfederateName";
	private static final String HLAFEDERATE_HANDLE_ATTR = "HLAfederateHandle";
	private static final Set<String> HLAFEDERATE_ATTRIBUTE_NAMES =
	    new HashSet<>( Arrays.asList( new String[]{ HLAFEDERATE_HANDLE_ATTR, 
	                                                HLAFEDERATE_NAME_ATTR,
	                                                HLAFEDERATE_TYPE_ATTR } ) );
	
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
	
	private Map<String, Set<JoinedFederate>> joinedFederatesByType; 
	private Map<String, Integer> startRequirements;
	private int totalFederatesRequired = 0;
	
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
		
		joinedFederatesByType = new HashMap<>();
		startRequirements = new HashMap<>();
		
		// we need at least two federates of type "FederateX" to begin
		startRequirements.put( "FederateX", 2 );
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
		totalFederatesRequired = startRequirements.values().parallelStream().mapToInt( i -> i.intValue() ).sum();
	}

	@Override
	public void beforeReadyToPopulate()
	{
		preAnnounceSyncPoints();

		System.out.print( "beforeReadyToPopulate()\nWaiting for federates to join..." );
		
		long count = 0;
		int lastJoinedCount = -1;
		int currentJoinedCount = joinedCount();
		while( !canStart() )
		{
			waitFor( 500 );
			
			if( ++count % 2 == 0 )
			{
				// show progress
				System.out.print( '.' );
				currentJoinedCount = joinedCount();
				if(currentJoinedCount != lastJoinedCount)
				{
					System.out.println( "\n" + joinedCount() + " of " + totalFederatesRequired + " federates have joined.");
					lastJoinedCount = currentJoinedCount;
				}
			}
		}
		
		System.out.println();
		System.out.println( String.format( "Start requirements met - we are now %s.",
		                                   UCEFSyncPoint.READY_TO_POPULATE ) );
	}

	@Override
	public void beforeReadyToRun()
	{
		// no preparation required before ready to run
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
		if( isType( hlaObject, HLAFEDERATE_OBJECT_CLASS_NAME ) )
		{
			rtiamb.requestAttributeValueUpdate( hlaObject, HLAFEDERATE_ATTRIBUTE_NAMES );
		}
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject )
	{
		JoinedFederate joinedFederate = new JoinedFederate( hlaObject );
		if( FEDMAN_FEDERATE_TYPE.equals( joinedFederate.getFederateType() ) )
		{
			// ignore ourself joining...
			return;
		}

		// WORKAROUND for the fact that the federate type is currently not correctly
		//            propagated (and instead is actually the federate name)
		//            see: https://github.com/openlvc/portico/issues/280
		String federateType = joinedFederate.getFederateType();
		for( String requiredType : startRequirements.keySet() )
		{
			if( federateType.startsWith( requiredType ) )
			{
				synchronized( joinedFederatesByType )
				{
					joinedFederatesByType.computeIfAbsent( requiredType,
					                                       x -> new HashSet<>() ).add( joinedFederate );
				}
			}
		}
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject, double time )
	{
		receiveAttributeReflection( hlaObject );
	}

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction )
	{
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
	/**
	 * Pre-announce all UCEF synchronization points
	 */
	private void preAnnounceSyncPoints()
	{
		for( UCEFSyncPoint syncPoint : UCEFSyncPoint.values() )
		{
			registerSyncPointAndWaitForAnnounce( syncPoint.getLabel(), null );
		}
	}
	
	private int joinedCount()
	{
		int result = 0;
		synchronized( joinedFederatesByType )
		{
			result = joinedFederatesByType.values().parallelStream().mapToInt( i -> i.size() ).sum();
		}
		return result;
	}
	
	private boolean canStart()
	{
		synchronized( joinedFederatesByType )
		{
    		for( Entry<String,Integer> x: startRequirements.entrySet() )
    		{
    			String federateType = x.getKey();
    			int minCount = x.getValue();
    			
    			Set<JoinedFederate> joined = joinedFederatesByType.get( federateType );
    			if(joined == null || joined.size() < minCount )
    			{
    				return false;
    			}
    		}
		}
		
		return true;
	}
	
	/**
	 * Utility method to cause execution to wait for the given duration
	 * 
	 * @param duration the duration to wait in milliseconds
	 */
	private void waitFor( long duration )
	{
		if( duration <= 0 )
			return;
		
		try
		{
			Thread.sleep( duration );
		}
		catch( InterruptedException e )
		{
			throw new UCEFException( "Could not wait", e );
		}
	}
	
	/**
	 * Utility method to cause execution to wait until the given timestamp is reached
	 * 
	 * @param timestamp the time to wait until (as system clock time in milliseconds)
	 */
	private void waitUntil( long timestamp )
	{
		waitFor( timestamp - System.currentTimeMillis() );
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
		                                                          FEDMAN_FEDERATE_NAME, 
		                                                          FEDMAN_FEDERATE_TYPE );
		
		// subscribe to reflections described in MIM to detected joining federates 
		config.addSubscribedAtributes( HLAFEDERATE_OBJECT_CLASS_NAME, HLAFEDERATE_ATTRIBUTE_NAMES );
		// here about callbacks *immediately*, rather than when evoked, otherwise
		// we don't know about joined federates until after the ticking starts 
		config.setCallbacksAreEvoked( false ); // use CallbackModel.HLA_IMMEDIATE
		
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
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// PRIVATE CLASSES /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	private class JoinedFederate
	{
		private HLAObject instance;

		JoinedFederate(HLAObject instance)
		{
			this.instance = instance;
		}

//		public String getFederateName()
//		{
//			return instance.getAsString( HLAFEDERATE_NAME_ATTR );
//		}
		
		public String getFederateType()
		{
			return instance.getAsString( HLAFEDERATE_TYPE_ATTR );
		}
		
		public int hashCode()
		{
			return getFederateHandle();
		}
		
		public int getFederateHandle()
		{
			return instance.getAsInt( HLAFEDERATE_HANDLE_ATTR );
		}
	}
	

}

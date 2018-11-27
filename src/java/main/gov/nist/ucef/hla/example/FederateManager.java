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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.util.Constants;
import gov.nist.ucef.hla.example.util.FileUtils;
import gov.nist.ucef.hla.example.util.cmdargs.ArgProcessor;
import gov.nist.ucef.hla.example.util.cmdargs.ListArg;
import gov.nist.ucef.hla.example.util.cmdargs.ArgException;
import gov.nist.ucef.hla.example.util.cmdargs.StdValidators;
import gov.nist.ucef.hla.example.util.cmdargs.ValidationResult;
import gov.nist.ucef.hla.example.util.cmdargs.IArgValidator;
import gov.nist.ucef.hla.example.util.cmdargs.ValueArg;

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
 * 		   / /_  / _ \/ __  / /\|_/ / __`/ __ \
 * 		  / __/ /  __/ /_/ / /  / / /_/ / / / /
 * 		 /_/    \___/\__,_/_/  /_/\__,_/_/ /_/ 		
 * 		------------ Federate Manager ----------
 *
 */
public class FederateManager extends FederateBase {

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// the Federate Manager logo as ASCII art - shown on startup in console
	private static final String FEDMAN_LOGO =
		"     ______         ____  ___\n" +
		"    / ____/__  ____/ /  |/  /___  ____\n" + 
		"   / /_  / _ \\/ __  / /\\|_/ / __`/ __ \\\n" +
		"  / __/ /  __/ /_/ / /  / / /_/ / / / /\n" +
		" /_/    \\___/\\__,_/_/  /_/\\__,_/_/ /_/\n" + 		
		"------------ Federate Manager ----------\n";

	// name of the Federate Manager executable
	private static final String EXEC_NAME = "fedman";
	
	// command line arguments and defaults
	private static final String CMDLINEARG_REQUIRE = "require";
	private static final char CMDLINEARG_REQUIRE_SHORT = 'r';
	private static final String CMDLINEARG_LOGICAL_SECOND = "logical-second";
	private static final double LOGICAL_SECOND_DEFAULT = 1.0;
	private static final String CMDLINEARG_LOGICAL_STEP_GRANULARITY = "logical-granularity";
	private static final int LOGICAL_STEP_GRANULARITY_DEFAULT = 1;
	private static final String CMDLINEARG_REALTIME_MULTIPLIER = "realtime-multiplier";
	private static final double REALTIME_MULTIPLIER_DEFAULT = 1.0;
	
	// Federate Manager federation naming conventions 
	private static final String FEDMAN_FEDERATE_TYPE = "FederateManager";
	private static final String FEDMAN_FEDERATE_NAME = "FederateManager";

	// MIM defined attribute reflections for detection of joining federates
	private static final String HLAFEDERATE_OBJECT_CLASS_NAME = "HLAobjectRoot.HLAmanager.HLAfederate";
	private static final String HLAFEDERATE_TYPE_ATTR = "HLAfederateType";
	private static final String HLAFEDERATE_NAME_ATTR = "HLAfederateName";
	private static final String HLAFEDERATE_HANDLE_ATTR = "HLAfederateHandle";
	private static final Set<String> HLAFEDERATE_ATTRIBUTE_NAMES =
	    new HashSet<>( Arrays.asList( new String[]{ HLAFEDERATE_HANDLE_ATTR, 
	                                                HLAFEDERATE_NAME_ATTR,
	                                                HLAFEDERATE_TYPE_ATTR } ) );
	
	// Various common text items used in output
	private static final String FEDERATE_TYPE_HEADING = "Type";
	private static final String NUMBER_REQUIRED_HEADING = "Required";
	private static final String NUMBER_JOINED_HEADING = "Joined";
	private static final char NEWLINE = '\n';
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println(Constants.UCEF_LOGO);
		System.out.println(FEDMAN_LOGO);
		
		try
		{
			new FederateManager(args).runFederate( makeConfig() );
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
	private double maxTime;
	
	private double logicalSecond;
	private int logicalStepGranularity;
	private double realTimeMultiplier;
	
	private double logicalStepSize;
	private long wallClockStepDelay;
	
	private Map<String, Set<JoinedFederate>> joinedFederatesByType; 
	private Map<String, Integer> startRequirements;
	private int totalFederatesRequired = 0;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor
	 * 
	 * @param args command line arguments
	 */
	public FederateManager( String[] args )
	{
		super();
		
		this.startRequirements = new HashMap<>();
		this.joinedFederatesByType = new HashMap<>();
		this.logicalSecond = LOGICAL_SECOND_DEFAULT;
		this.logicalStepGranularity = LOGICAL_STEP_GRANULARITY_DEFAULT;
		this.realTimeMultiplier = REALTIME_MULTIPLIER_DEFAULT;
		
		this.maxTime = 15.0;
		
		this.nextTimeAdvance = -1;
		
		if( !validateAndProcessCmdLineArgs( args ) )
		{
			System.out.println( "Cannot proceed. Exiting now." );
			System.exit( 1 );
		}
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
		totalFederatesRequired = startRequirements
			.values()
			.parallelStream()
			.mapToInt( i -> i.intValue() )
			.sum();
	}

	@Override
	public void beforeReadyToPopulate()
	{
		preAnnounceSyncPoints();

		System.out.println( configurationSummary() );
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
		// update the configuration lookahead value so that it is the same
		// logical step size as obtained from the command line args
		configuration.setLookAhead( logicalStepSize );
	}

	@Override
	public void beforeFirstStep()
	{
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
			                                   (federateTime + this.logicalStepSize) ) );
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
		// federate manager does not process this
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method for validating and processing of command line arguments/options
	 * 
	 * Does the following:
	 * 
	 *  - Sets up the command line arguments
	 *  - validates and processes what the user provided
	 *  - populate all necessary internal state information that 
	 *    relies on information from command line arguments 
	 * 
	 * @param args the command line arguments
	 * @return true if the arguments were all valid and correct, false otherwise (execution should
	 *  	   not continue.
	 */
	private boolean validateAndProcessCmdLineArgs( String[] args )
	{
        ArgProcessor argProcessor = new ArgProcessor();
        ListArg requiredFederateTypes = argProcessor
        	.addListArg(CMDLINEARG_REQUIRE_SHORT, CMDLINEARG_REQUIRE)
        	.isRequired(true)
		    .validator( new RequiredFedValidator() )
		    .help( String.format( "Define required federate types and counts. For example, " +
		                          "'-%s FedABC,2' would require two 'FedABC' federates to join. " +
		                          "Multiple requirements can be specified by repeated use " +
		                          "of -%s.",
		                          CMDLINEARG_REQUIRE_SHORT,
		                          CMDLINEARG_REQUIRE_SHORT ) )
		    .hint( "FEDERATE_TYPE,COUNT" );
		ValueArg logicalSecondArg = argProcessor
			.addValueArg( null, CMDLINEARG_LOGICAL_SECOND )
			.isRequired( false )
		    .validator( StdValidators.CheckDoubleGtZero )
		    .help( String.format( "Define a 'logical second'; the logical step size which " +
		    					  "equates to a real-time second. If unspecified a value " +
		    					  "of %.2f will be used.",
		    					  LOGICAL_SECOND_DEFAULT ) )
		    .hint( "1.0" );
		ValueArg logicalStepGranularityArg = argProcessor
			.addValueArg( null, CMDLINEARG_LOGICAL_STEP_GRANULARITY )
			.isRequired( false )
		    .validator( StdValidators.CheckIntGtZero )
		    .help( String.format( "Define the number of steps per logical second. If " +
		    				      "unspecified a value of %d will be used.",
		    				      LOGICAL_STEP_GRANULARITY_DEFAULT ) )
		    .hint( "1" );
        ValueArg realtimeMultiplierArg = argProcessor
        	.addValueArg(null, CMDLINEARG_REALTIME_MULTIPLIER)
        	.isRequired(false)
		    .validator( StdValidators.CheckDoubleGtZero )
        	.help( String.format( "Define the simulation rate. 1.0 is real time, 0.5 is " +
        						  "half speed, 2.0 is double speed, and so on. If unspecified " +
        						  "a value of %.2f will be used.", 
        						  REALTIME_MULTIPLIER_DEFAULT ) )
        	.hint("1.0");
        
        try
		{
			argProcessor.process( args );
		}
		catch( ArgException e )
		{
			System.err.println( e.getMessage() );
			System.out.println( "Usage: " + argProcessor.getUsage( EXEC_NAME ) );
			System.out.println( argProcessor.getHelp() );
			return false;
		}
		
		// At this stage we know that all command line arguments are valid,
		// so we can use the values without checking them further 
		if( logicalSecondArg.isSet() )
			this.logicalSecond = Double.parseDouble( logicalSecondArg.value() );

		if( logicalStepGranularityArg.isSet() )
			this.logicalStepGranularity = Integer.parseInt( logicalStepGranularityArg.value(), 10 );

		if( realtimeMultiplierArg.isSet() )
			this.realTimeMultiplier = Double.parseDouble( realtimeMultiplierArg.value() );
		
		// this is a required argument, so we don't need to check if it's set, and we know
		// it's validated so splitting on the comma and parsing the integer etc aren't
		// things we need check up on here - we know everything is fine.
		List<String> requires = requiredFederateTypes.value();
		for( String require : requires )
		{
			StringTokenizer tokenizer = new StringTokenizer( require, "," );
			String federateType = tokenizer.nextToken();
			int federateCount = Integer.parseInt( tokenizer.nextToken() );
			startRequirements.put( federateType, federateCount );
		}

		double oneSecond = 1000.0 / this.realTimeMultiplier;
		this.logicalStepSize = this.logicalSecond / this.logicalStepGranularity;
		this.wallClockStepDelay = (long)(oneSecond * this.logicalStepSize);
		
		// the values are all fine individually, but in combination they might 
		// cause problems - check for mega-fast tick rates resulting from the
		// provided command line argument values
		if( this.wallClockStepDelay < 5 )
		{
			System.err.println( String.format( "The specified value(s) for " +
											   "--%s, --%s and/or --%s " +
			                                   " cannot be achieved (tick rate is too high).",
			                                   CMDLINEARG_LOGICAL_SECOND,
			                                   CMDLINEARG_LOGICAL_STEP_GRANULARITY,
			                                   CMDLINEARG_REALTIME_MULTIPLIER ) );
			return false;
		}
		else if( this.wallClockStepDelay < 20 )
		{
			System.out.println( String.format( "WARNING: The specified value(s) for " +
											   "--%s, --%s and/or --%s " +
											   "requires a tick rate higher than 50 ticks" +
											   "per second - your simulation may not " +
											   "keep up with your requirements.",
                                			   CMDLINEARG_LOGICAL_SECOND,
                                			   CMDLINEARG_LOGICAL_STEP_GRANULARITY,
                                			   CMDLINEARG_REALTIME_MULTIPLIER ) );
		}
		
		// all command line arguments are present and correct!
		return true;
	}
	
	/**
	 * Create a human readable summary of the federate manager's configuration
	 *  
	 * @return a human readable summary of the federate manager's configuration
	 */
	private String configurationSummary()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( center( " Federate Manager Details ", 80, '=' ) );
		builder.append( NEWLINE );
		builder.append( "Time:" );
		builder.append( NEWLINE );
		builder.append( String.format( "\tLogical step of %.2f = %.2f real time seconds.", 
		                               this.logicalSecond, 1.0 ) );
		builder.append( NEWLINE );
		builder.append( "\tRunning " );
		if(this.realTimeMultiplier > 1.0)
		{
			builder.append( String.format( "%.2f× faster than ", 
			                               (this.realTimeMultiplier)) );
		}
		else if(this.realTimeMultiplier < 1.0)
		{
			builder.append( String.format( "%.2f× slower than ", 
			                               (1.0/this.realTimeMultiplier)) );
		}
		else
		{
			builder.append( "in ");
		}
		builder.append( "real time (");
		
		builder.append( String.format( "one logical step of %.2f every %d milliseconds.", 
		                               this.logicalStepSize, this.wallClockStepDelay) );
		builder.append( ")" );
		builder.append( NEWLINE );
		builder.append( "Start Requirements:" );
		builder.append( NEWLINE );
		builder.append( startRequirementsSummary() );
		return builder.toString();
	}
	
	/**
	 * Utility method which just generates an ASCII-art style table summarizing the requirements
	 * for a federation to achieve the "ready to populate" synchronization point
	 * 
	 * @return an ASCII-art style table summarizing the configured start requirements
	 */
	private String startRequirementsSummary()
	{
		// sorted federate types list
		List<String> federateTypes = new ArrayList<>(startRequirements.keySet());
		federateTypes.sort( null );
		
		// check the maximum length of federate type names so that we can
		// format the summary table nicely
		int col0width = federateTypes.parallelStream().mapToInt( str -> str.length() ).max().getAsInt();
		col0width = Math.max( col0width, FEDERATE_TYPE_HEADING.length() );
		
		int col1width = NUMBER_REQUIRED_HEADING.length();
		int col2width = NUMBER_JOINED_HEADING.length();
		
		StringBuilder builder = new StringBuilder();
		builder.append( repeat("-", col0width + 1) );
		builder.append( "+" );
		builder.append( repeat("-", NUMBER_REQUIRED_HEADING.length() + 2 ) );
		builder.append( "+" );
		builder.append( repeat("-", NUMBER_JOINED_HEADING.length() + 1 ) );
		builder.append(NEWLINE);
		String rowSeparator = builder.toString();
		
		// build the table
		builder = new StringBuilder();
		//      top border
		builder.append( rowSeparator );
		//      header row
		builder.append( FEDERATE_TYPE_HEADING );
		builder.append( repeat( " ", col0width - FEDERATE_TYPE_HEADING.length() ) );
		builder.append( " | " ).append( NUMBER_REQUIRED_HEADING );
		builder.append( repeat( " ", col1width - NUMBER_REQUIRED_HEADING.length() ) );
		builder.append( " | " ).append( NUMBER_JOINED_HEADING );
		builder.append( NEWLINE );
		//      header row/data separator
		builder.append( rowSeparator );
		//      data
		for( String federateType : federateTypes )
		{
			String requiredCount = Integer.toString( startRequirements.get( federateType ) );
			String joinedCount = Integer.toString( joinedFederatesByType.getOrDefault( federateTypes, Collections.emptySet() ).size() );
			
			builder.append(federateType);
			builder.append( repeat(" ", col0width - federateType.length()) );
			builder.append(" | " );
			builder.append(requiredCount);
			builder.append( repeat(" ", col1width - requiredCount.length()) );
			builder.append(" | " );
			builder.append(joinedCount);
			builder.append( repeat(" ", col2width - joinedCount.length()) );
			builder.append( "\n" );
		}
		//      bottom border
		builder.append( rowSeparator );
		
		return builder.toString();
	}
	
	/**
	 * Utility method to repeat a string a given number of times.
	 * 
	 * @param str the string to repeat
	 * @param count the number of repetitions
	 * @return the repeated string
	 */
	private String repeat( String str, int count )
	{
		return IntStream.range( 0, count ).mapToObj( i -> str ).collect( Collectors.joining( "" ) );
	}
	
	/**
	 * Utility class to center a string in a given width
	 * 
	 * @param str the string to center
	 * @param width the width to center the string in
	 * @param padding the padding character to use to the left and right of the string
	 * @return the centered string
	 */
	private String center( String str, int width, char padding )
	{
		int count = width - str.length();
		if( count <= 0)
			return str;
		
		String padStr = Character.toString( padding );
		String leftPad = repeat(padStr, count / 2); 
		if(count %2 ==0)
			return leftPad + str + leftPad;
			
		return leftPad + str + leftPad.substring( 0, count+1 );
	}
	
	/**
	 * Utility method which simply returns a count of the number of federates which have joined
	 * the federation.
	 * 
	 * NOTE: this only counts federates which meet the criteria for allowing the simulation to
	 * begin (i.e., as specified in the command line arguments) and ignores all others.
	 * 
	 * @return the number of joined federates
	 */
	private int joinedCount()
	{
		int result = 0;
		synchronized( joinedFederatesByType )
		{
			result = joinedFederatesByType.values().parallelStream().mapToInt( i -> i.size() ).sum();
		}
		return result;
	}
	
	/**
	 * Utility method to determine whether start requirements have been met yet
	 * 
	 * @return true if the start requirements have been met, false otherwise
	 */
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
	
	/**
	 * Pre-announce all UCEF synchronization points.
	 */
	private void preAnnounceSyncPoints()
	{
		for( UCEFSyncPoint syncPoint : UCEFSyncPoint.values() )
		{
			registerSyncPointAndWaitForAnnounce( syncPoint.getLabel(), null );
		}
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
		FederateConfiguration config = new FederateConfiguration( FEDMAN_FEDERATE_NAME, 
		                                                          FEDMAN_FEDERATE_TYPE,
		                                                          "TheUnitedFederationOfPlanets");
		
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

// TODO this method seems to be unnecessary...?
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
	
	/**
	 * Private class implementing a command line argument validator to check that any provided
	 * command line arguments specifying the number and type of required federates is in the
	 * correct format and contains no errors.
	 */
	private class RequiredFedValidator implements IArgValidator
	{
		@Override
		public ValidationResult validate( Object value )
		{
			boolean isValid = true;
			try
			{
				// if there is a class cast exception here, it means the wrong
				// type was passed in, which is just a validation error
				@SuppressWarnings("unchecked")
				List<String> val = (List<String>)value;
				for( String s : val )
				{
					String[] parts = s.split( "," );
					if( parts.length != 2 )
					{
						// something wrong with the expected comma separated value 
						isValid = false;
						break;
					}
					if( parts[0].length() == 0 )
					{
						// something wrong with the federate type name (empty) 
						isValid = false;
						break;
					}
					if( Integer.parseInt( parts[1], 10 ) < 1 )
					{
						// something wrong with the count (not a number or less than 1) 
						isValid = false;
						break;
					}
				}
				if( isValid )
					return ValidationResult.GENERIC_SUCCESS;
			}
			catch( Exception e )
			{
				// something has gone wrong - doesn't really matter 
				// what, just fall through 
			}
			return new ValidationResult( false,
			                             "Values must be a federate type name and a " +
			                             "number greater than zero separated by a comma. " +
			                             "For example, 'FedABC,2'." );
		}
	};
}

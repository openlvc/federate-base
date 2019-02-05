/*
 * This software is contributed as a public service by The National Institute of Standards 
 * and Technology (NIST) and is not subject to U.S. Copyright
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above NIST contribution notice and this permission and disclaimer notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. THE AUTHORS OR COPYRIGHT HOLDERS SHALL
 * NOT HAVE ANY OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
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

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.example.util.Constants;
import gov.nist.ucef.hla.example.util.FileUtils;
import gov.nist.ucef.hla.example.util.StringUtils;
import gov.nist.ucef.hla.example.util.cmdargs.ArgException;
import gov.nist.ucef.hla.example.util.cmdargs.ArgProcessor;
import gov.nist.ucef.hla.example.util.cmdargs.IArgValidator;
import gov.nist.ucef.hla.example.util.cmdargs.ListArg;
import gov.nist.ucef.hla.example.util.cmdargs.StdValidators;
import gov.nist.ucef.hla.example.util.cmdargs.ValidationResult;
import gov.nist.ucef.hla.example.util.cmdargs.ValueArg;
import gov.nist.ucef.hla.ucef.UCEFFederateBase;
import gov.nist.ucef.hla.ucef.interaction.c2w.FederateJoin;
import gov.nist.ucef.hla.ucef.interaction.c2w.SimEnd;
import gov.nist.ucef.hla.ucef.interaction.c2w.SimPause;
import gov.nist.ucef.hla.ucef.interaction.c2w.SimResume;

/**
 *		            ___
 *		          _/   \_     _     _
 *		         / \   / \   / \   / \
 *		        ( U )─( C )─( E )─( F )
 *		         \_/   \_/   \_/   \_/
 *		        <─┴─> <─┴─────┴─────┴─>
 *		       Universal CPS Environment
 *		             for Federation
 * 		    ______         ____  ___
 * 		   / ____/__  ____/ /  |/  /___ _____ 
 * 		  / /_  / _ \/ __  / /\|_/ / __`/ __ \
 * 		 / __/ /  __/ /_/ / /  / / /_/ / / / /
 * 	    /_/    \___/\__,_/_/  /_/\__,_/_/ /_/ 		
 * 	  ─────────── Federation Manager ───────────
 *
 */
public class FederationManager extends UCEFFederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// the Federate Manager logo as ASCII art - shown on startup in console
	private static final String FEDMAN_LOGO =
		"      ______         ____  ___\n" +
		"     / ____/__  ____/ /  |/  /___  ____\n" + 
		"    / /_  / _ \\/ __  / /\\|_/ / __`/ __ \\\n" +
		"   / __/ /  __/ /_/ / /  / / /_/ / / / /\n" +
		"  /_/    \\___/\\__,_/_/  /_/\\__,_/_/ /_/\n" + 		
		"─────────── Federation Manager ───────────\n";

	// name of the Federation Manager executable
	private static final String EXEC_NAME = "fedman";
	
	// command line arguments and defaults
	private static final String CMDLINE_ARG_FEDERATION_EXEC_NAME = "federation";
	private static final char CMDLINE_ARG_FEDERATION_EXEC_NAME_SHORT = 'f';
	private static final String CMDLINEARG_REQUIRE = "require";
	private static final char CMDLINEARG_REQUIRE_SHORT = 'r';
	private static final String CMDLINEARG_FEDMAN_FEDERATE_NAME = "fedman-name";
	private static final String DEFAULT_FEDMAN_FEDERATE_NAME = "FederateManager";
	private static final String CMDLINEARG_FEDMAN_FEDERATE_TYPE = "fedman-type";
	private static final String DEFAULT_FEDMAN_FEDERATE_TYPE = "FederateManager";
	private static final String CMDLINEARG_LOGICAL_SECOND = "logical-second";
	private static final double LOGICAL_SECOND_DEFAULT = 1.0;
	private static final String CMDLINEARG_LOGICAL_STEP_GRANULARITY = "logical-granularity";
	private static final int LOGICAL_STEP_GRANULARITY_DEFAULT = 1;
	private static final String CMDLINEARG_REALTIME_MULTIPLIER = "realtime-multiplier";
	private static final double REALTIME_MULTIPLIER_DEFAULT = 1.0;
	
	// Federate Manager federation naming conventions 
	private static final String FEDMAN_FEDERATE_TYPE = DEFAULT_FEDMAN_FEDERATE_NAME;
	private static final String FEDMAN_FEDERATE_NAME = DEFAULT_FEDMAN_FEDERATE_NAME;

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
	private static final String[] TABLE_HEADINGS = {FEDERATE_TYPE_HEADING, 
	                                                NUMBER_REQUIRED_HEADING, 
	                                                NUMBER_JOINED_HEADING};
	private static final char NEWLINE = '\n';
	private static final long ONE_SECOND = 1000;
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		System.out.println(Constants.UCEF_LOGO);
		System.out.println(FEDMAN_LOGO);
		
		try
		{
			new FederationManager(args).runFederate( makeConfig() );
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
	private String federationExecName;
	private String federateName;
	private String federateType;
	private double logicalSecond;
	private int logicalStepGranularity;
	private double realTimeMultiplier;
	
	private double logicalStepSize;
	private long wallClockStepDelay;
	
	private long nextTimeAdvance;
	private double maxTime;
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
	public FederationManager( String[] args )
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
		// update the federate name, type and federation execution name in 
		// accordance with the values obtained from the command line args
		configuration.setFederateName( this.federateName );
		configuration.setFederateType( this.federateType );
		configuration.setFederationName( this.federationExecName );
		// update the configuration lookahead value so that it is the same
		// logical step size as obtained from the command line args
		configuration.setLookAhead( logicalStepSize );
		
		// convenience calculation so we don't need to do 
		// it repeatedly later on
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
		System.out.println( "Waiting for federates to join..." );

		long count = 1;
		int lastJoinedCount = -1;
		int currentJoinedCount = joinedCount();
		
		while( !canStart() )
		{
			currentJoinedCount = joinedCount();
			if( currentJoinedCount != lastJoinedCount )
			{
				System.out.println( String.format( "\n%d of %d federates have joined.",
				                                   currentJoinedCount, totalFederatesRequired ) );
				System.out.println( startRequirementsSummary() );
				lastJoinedCount = currentJoinedCount;
				count = 1;
			}
			
			waitFor( ONE_SECOND );
			// show progress bar in seconds since last joined federate as...
			//     5    10   15   20   25   30   35   40   45   50   55   60 seconds
			// ─═─═┬═─═─┼─═─═┬═─═─┼─═─═┬═─═─╬─═─═┬═─═─┼─═─═┬═─═─┼─═─═┬═─═─╣
			System.out.print( count % 60 == 0 ? '╣' : count % 30 == 0 ? '╬' : count % 10 == 0 ? '┼' : 
							  count % 5 == 0 ?  '┬' : count % 2 == 0 ?  '═' : '─' );
			if( count >= 60 )
			{
				System.out.print( '\n' );
				count = 0;
			}
			count++;
		}
		
		System.out.println( String.format( "\n%d of %d federates have joined.",
		                                   totalFederatesRequired, totalFederatesRequired ) );
		System.out.println( String.format( "Start requirements met - we are now %s.",
		                                   UCEFSyncPoint.READY_TO_POPULATE ) );
	}

	@Override
	public void beforeReadyToRun()
	{
		// no initialization tasks required
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
		if( rtiamb.isOfKind( hlaObject, HLAFEDERATE_OBJECT_CLASS_NAME ) )
		{
			rtiamb.requestAttributeValueUpdate( hlaObject, HLAFEDERATE_ATTRIBUTE_NAMES );
		}
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject )
	{
		if( rtiamb.isOfKind( hlaObject, HLAFEDERATE_OBJECT_CLASS_NAME ) )
		{
    		JoinedFederate joinedFederate = new JoinedFederate( hlaObject );
    		String federateType = joinedFederate.getFederateType();
    		if( FEDMAN_FEDERATE_TYPE.equals( federateType ) )
    		{
    			// ignore ourself joining...
    			return;
    		}
    		
    		// WORKAROUND for the fact that the federate type is currently not correctly
    		//            propagated (and instead is actually the federate name)
    		//            see: https://github.com/openlvc/portico/issues/280
    		//                 https://github.com/openlvc/portico/pull/281
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
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void receiveFederateJoin( FederateJoin federateJoin )
	{
		super.receiveFederateJoin( federateJoin );
		System.out.println( federateJoin.toString() );
	}

	@Override
	protected void receiveSimPause( SimPause simPause )
	{
		super.receiveSimPause( simPause );
		System.out.println( simPause.toString() );
	}

	@Override
	protected void receiveSimResume( SimResume simResume )
	{
		super.receiveSimResume( simResume );
		System.out.println( simResume.toString() );
	}
	
	@Override
	protected void receiveSimEnd( SimEnd simEnd )
	{
		super.receiveSimEnd( simEnd );
		System.out.println( simEnd.toString() );
	}
	
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
		ArgProcessor argProcessor = new ArgProcessor( Constants.CONSOLE_WIDTH );
        ValueArg federationExecNameArg = argProcessor
        	.addValueArg( CMDLINE_ARG_FEDERATION_EXEC_NAME_SHORT, CMDLINE_ARG_FEDERATION_EXEC_NAME )
        	.isRequired( true )
        	.help( "Set the name of the federation execution the Federate Manager will join." )
        	.hint( "FEDERATION_EXEC_NAME" );
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
        ValueArg federateNameArg = argProcessor
        	.addValueArg( null, CMDLINEARG_FEDMAN_FEDERATE_NAME )
        	.isRequired( false )
			.defaultValue( DEFAULT_FEDMAN_FEDERATE_NAME )
        	.help( String.format( "Set the federate name for the Federate Manager to use. " +
        						  "If unspecified a value of '%s' will be used.",
        						  DEFAULT_FEDMAN_FEDERATE_NAME ) )
        	.hint( "FEDMAN_NAME" );
    	ValueArg federateTypeArg = argProcessor
        	.addValueArg( null, CMDLINEARG_FEDMAN_FEDERATE_TYPE )
        	.isRequired( false )
        	.defaultValue( DEFAULT_FEDMAN_FEDERATE_TYPE )
        	.help( String.format( "Set the federate type for the Federate Manager to use. " +
        						  "If unspecified a value of '%s' will be used.",
        						  DEFAULT_FEDMAN_FEDERATE_TYPE ) )
        	.hint( "FEDMAN_TYPE" );
		ValueArg logicalSecondArg = argProcessor
			.addValueArg( null, CMDLINEARG_LOGICAL_SECOND )
			.isRequired( false )
			.defaultValue( Double.toString( LOGICAL_SECOND_DEFAULT ) )
		    .validator( StdValidators.CheckDoubleGtZero )
		    .help( String.format( "Define a 'logical second'; the logical step size which " +
		    					  "equates to a real-time second. " +
		    					  "If unspecified a value of %.2f will be used.",
		    					  LOGICAL_SECOND_DEFAULT ) )
		    .hint( "1.0" );
		ValueArg logicalStepGranularityArg = argProcessor
			.addValueArg( null, CMDLINEARG_LOGICAL_STEP_GRANULARITY )
			.isRequired( false )
			.defaultValue( Integer.toString( LOGICAL_STEP_GRANULARITY_DEFAULT, 10 ) )
		    .validator( StdValidators.CheckIntGtZero )
		    .help( String.format( "Define the number of steps per logical second. If " +
		    				      "unspecified a value of %d will be used.",
		    				      LOGICAL_STEP_GRANULARITY_DEFAULT ) )
		    .hint( "1" );
        ValueArg realtimeMultiplierArg = argProcessor
        	.addValueArg(null, CMDLINEARG_REALTIME_MULTIPLIER)
        	.isRequired(false)
			.defaultValue( Double.toString( REALTIME_MULTIPLIER_DEFAULT ) )
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
			System.out.println( "======= Usage:\n" + argProcessor.getUsage( EXEC_NAME ) );
			System.out.println( "===== Options:\n" + argProcessor.getHelp() );
			return false;
		}
		
		// At this stage we know that all command line arguments are valid,
		// so we can use the values without checking them further
        this.federationExecName = federationExecNameArg.value();
        this.federateName = federateNameArg.value();
        this.federateType = federateTypeArg.value();
        
		this.logicalSecond = Double.parseDouble( logicalSecondArg.value() );
		this.logicalStepGranularity = Integer.parseInt( logicalStepGranularityArg.value(), 10 );
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
		builder.append( StringUtils.center( " Federate Manager Details ", 80, '═' ) );
		builder.append( NEWLINE );
		builder.append( "Time:" );
		builder.append( NEWLINE );
		builder.append( String.format( "\tLogical step of %.2f = %.2f real time seconds.", 
		                               this.logicalSecond, 1.0 ) );
		builder.append( NEWLINE );
		builder.append( "\tRunning " );
		if( this.realTimeMultiplier != 1.0 )
		{
			builder.append( String.format( "%.2f× %s than ",
			                               this.realTimeMultiplier < 1.0 ? "slower" : "faster",
			                               (this.realTimeMultiplier) ) );
		}
		else
		{
			builder.append( "in " );
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
		
		List<List<Object>> tableContent = new ArrayList<>();
		
		List<Object> row = new ArrayList<Object>();
		row.addAll( Arrays.asList( TABLE_HEADINGS ) );
		tableContent.add( row );
		
		for( String federateType : federateTypes )
		{
			row = new ArrayList<>();
			row.add(federateType);
			row.add(Integer.toString( startRequirements.get( federateType ) ));
			row.add(Integer.toString( joinedFederatesByType.getOrDefault( federateType, Collections.emptySet() ).size() ));
			tableContent.add( row );
		}
		return StringUtils.makeTable( tableContent );
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
		                                                          "ManagedFederation");
		
		// subscribe to reflections described in MIM to detected joining federates 
		config.addSubscribedAttributes( HLAFEDERATE_OBJECT_CLASS_NAME, HLAFEDERATE_ATTRIBUTE_NAMES );
		
		// subscribed UCEF interactions
		config.addSubscribedInteractions( FederateJoin.interactionName(), SimPause.interactionName(),
		                                  SimResume.interactionName(), SimEnd.interactionName() );
		
		// here about callbacks *immediately*, rather than when evoked, otherwise
		// we don't know about joined federates until after the ticking starts 
		config.setCallbacksAreEvoked( false ); // use CallbackModel.HLA_IMMEDIATE
		
		config.setLookAhead( 0.25 );
		
		// somebody set us up the FOM...
		try
		{
			String fomRootPath = "resources/foms/";
			// modules
			String[] moduleFoms = { fomRootPath + "FederationManager.xml", 
			                        fomRootPath + "SmartPingPong.xml" };
			config.addModules( FileUtils.urlsFromPaths(moduleFoms) );
			
			// join modules
			String[] joinModuleFoms = {};
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

		public String getFederateName()
		{
			return instance.getAsString( HLAFEDERATE_NAME_ATTR );
		}
		
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
			ValidationResult initialValidation = StdValidators.CheckNonEmptyList.validate( value );
			if( initialValidation.isInvalid() )
				return initialValidation;
			
			boolean isValid = true;
			String lastCheckedItem = null;
			try
			{
				// we know it's a List<String> now, so this next cast is safe
				@SuppressWarnings("unchecked")
				List<String> val = (List<String>)value;
				// check all the values
				for( String s : val )
				{
					lastCheckedItem = s;
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
			                             "'%s' is not in the correct format. Values must be a " +
			                             "federate type name and a number greater than zero separated " +
			                             "by a comma. For example, 'FedABC,2'.",
			                             lastCheckedItem );
		}
	};
}

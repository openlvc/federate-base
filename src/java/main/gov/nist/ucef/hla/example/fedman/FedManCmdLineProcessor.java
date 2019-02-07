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
package gov.nist.ucef.hla.example.fedman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import gov.nist.ucef.hla.example.util.cmdargs.ArgException;
import gov.nist.ucef.hla.example.util.cmdargs.ArgProcessor;
import gov.nist.ucef.hla.example.util.cmdargs.IArgValidator;
import gov.nist.ucef.hla.example.util.cmdargs.ListArg;
import gov.nist.ucef.hla.example.util.cmdargs.StdValidators;
import gov.nist.ucef.hla.example.util.cmdargs.ValidationResult;
import gov.nist.ucef.hla.example.util.cmdargs.ValueArg;

public class FedManCmdLineProcessor
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// command line arguments and defaults
	public static final String CMDLINE_ARG_FEDERATION_EXEC_NAME = "federation";
	public static final char CMDLINE_ARG_FEDERATION_EXEC_NAME_SHORT = 'f';
	public static final String CMDLINEARG_REQUIRE = "require";
	public static final char CMDLINEARG_REQUIRE_SHORT = 'r';
	public static final String CMDLINEARG_FEDMAN_FEDERATE_NAME = "fedman-name";
	public static final String DEFAULT_FEDMAN_FEDERATE_NAME = "FederateManager";
	public static final String CMDLINEARG_FEDMAN_FEDERATE_TYPE = "fedman-type";
	public static final String DEFAULT_FEDMAN_FEDERATE_TYPE = "FederateManager";
	public static final String CMDLINEARG_LOGICAL_SECOND = "logical-second";
	public static final String CMDLINEARG_LOGICAL_STEP_GRANULARITY = "logical-granularity";
	public static final String CMDLINEARG_REALTIME_MULTIPLIER = "realtime-multiplier";
	
	public static final double LOGICAL_SECOND_DEFAULT = 1.0;
	public static final int LOGICAL_STEP_GRANULARITY_DEFAULT = 1;
	public static final double REALTIME_MULTIPLIER_DEFAULT = 1.0;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// command line processor and argument definitions
	private ArgProcessor argProcessor;
	private ValueArg federationExecNameArg;
	private ValueArg federateNameArg;
	private ValueArg federateTypeArg;
	private ListArg requiredFederateTypes;
	private ValueArg logicalSecondArg;
	private ValueArg logicalStepGranularityArg;
	private ValueArg realtimeMultiplierArg;
	
	private Map<String, Integer> startRequirements;
	
	private String execName;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FedManCmdLineProcessor(String execName, int consoleWidth )
	{
		this.execName = execName;
		
		initialize( consoleWidth );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getUsage()
	{
		return argProcessor.getUsage( this.execName );
	}
	
	public String getHelp()
	{
		return argProcessor.getHelp();
	}
	
	
	public String federationExecName()
	{
		return federationExecNameArg.value();
	}

    public String federateName()
	{
		return federateNameArg.value();
	}
    
    public String federateType()
	{
		return federateTypeArg.value();
	}
    
    public double logicalSecond()
	{
		return Double.parseDouble( logicalSecondArg.value() );
	}
    
	public int logicalStepGranularity()
	{
		return Integer.parseInt( logicalStepGranularityArg.value(), 10 );
	}
	
	public double logicalStepSize()
	{
		return logicalSecond() / logicalStepGranularity();
	}
	
	public double realTimeMultiplier()
	{
		return Double.parseDouble( realtimeMultiplierArg.value() );
	}
	
	public long wallClockStepDelay()
	{
		double oneSecond = 1000.0 / realTimeMultiplier();
		return (long)(oneSecond * this.logicalStepSize());
	}
	
	public Map<String,Integer> startRequirements()
	{
		return this.startRequirements;
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
	public boolean processArgs( String[] args ) throws ArgException
	{
		// will throw an ArgException if any of the command line args are "bad"
		argProcessor.process( args );
		
		// At this stage we know that all command line arguments are valid,
		// so we can use the values without checking them further
        
        // this is a required argument, so we don't need to check if it's set, and we know
        // it's validated so splitting on the comma and parsing the integer etc aren't
        // things we need check up on here - we know everything is fine.
        startRequirements.clear();
        List<String> requires = requiredFederateTypes.value();
        for( String require : requires )
        {
        	StringTokenizer tokenizer = new StringTokenizer( require, "," );
        	String federateType = tokenizer.nextToken();
        	int federateCount = Integer.parseInt( tokenizer.nextToken() );
        	startRequirements.put( federateType, federateCount );
        }
        
        // we need to sanity check some arguments with respect to each other
        // to ensure that they are "sensible" - in other words, the values 
        // are all fine individually, but in combination they might cause 
        // some problems
		double logicalSecond = Double.parseDouble( logicalSecondArg.value() );
		int logicalStepGranularity = Integer.parseInt( logicalStepGranularityArg.value(), 10 );
		double realTimeMultiplier = Double.parseDouble( realtimeMultiplierArg.value() );
		double oneSecond = 1000.0 / realTimeMultiplier;
		double logicalStepSize = logicalSecond / logicalStepGranularity;
		double wallClockStepDelay = (long)(oneSecond * logicalStepSize);
		if( wallClockStepDelay < 5 )
		{
			System.err.println( String.format( "The specified value(s) for " +
											   "--%s, --%s and/or --%s " +
			                                   " cannot be achieved (tick rate is too high).",
			                                   CMDLINEARG_LOGICAL_SECOND,
			                                   CMDLINEARG_LOGICAL_STEP_GRANULARITY,
			                                   CMDLINEARG_REALTIME_MULTIPLIER ) );
			return false;
		}
		else if( wallClockStepDelay < 20 )
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
	
	private void initialize(int consoleWidth)
	{
		startRequirements = new HashMap<>();

		argProcessor = new ArgProcessor( consoleWidth );
		
        federationExecNameArg = argProcessor
        	.addValueArg( CMDLINE_ARG_FEDERATION_EXEC_NAME_SHORT, CMDLINE_ARG_FEDERATION_EXEC_NAME )
        	.isRequired( true )
        	.help( "Set the name of the federation execution the Federate Manager will join." )
        	.hint( "FEDERATION_EXEC_NAME" );
        
        federateNameArg = argProcessor
        	.addValueArg( null, CMDLINEARG_FEDMAN_FEDERATE_NAME )
        	.isRequired( false )
			.defaultValue( FedManConstants.DEFAULT_FEDMAN_FEDERATE_NAME )
        	.help( String.format( "Set the federate name for the Federate Manager to use. " +
        						  "If unspecified a value of '%s' will be used.",
        						  FedManConstants.DEFAULT_FEDMAN_FEDERATE_NAME ) )
        	.hint( "FEDMAN_NAME" );
        
    	federateTypeArg = argProcessor
        	.addValueArg( null, CMDLINEARG_FEDMAN_FEDERATE_TYPE )
        	.isRequired( false )
        	.defaultValue( FedManConstants.DEFAULT_FEDMAN_FEDERATE_TYPE )
        	.help( String.format( "Set the federate type for the Federate Manager to use. " +
        						  "If unspecified a value of '%s' will be used.",
        						  FedManConstants.DEFAULT_FEDMAN_FEDERATE_TYPE ) )
        	.hint( "FEDMAN_TYPE" );
    	
        requiredFederateTypes = argProcessor
        	.addListArg( CMDLINEARG_REQUIRE_SHORT, CMDLINEARG_REQUIRE )
        	.isRequired( true )
		    .validator( new RequiredFedValidator() )
		    .help( String.format( "Define required federate types and minimum counts. For example, " +
		                          "'-%s FedABC,2' would require at least two 'FedABC' type federates " +
		                          "to join. Multiple requirements can be specified by repeated use " +
		                          "of -%s.",
		                          CMDLINEARG_REQUIRE_SHORT,
		                          CMDLINEARG_REQUIRE_SHORT ) )
		    .hint( "FEDERATE_TYPE,COUNT" );
        
		logicalSecondArg = argProcessor
			.addValueArg( null, CMDLINEARG_LOGICAL_SECOND )
			.isRequired( false )
			.defaultValue( Double.toString( LOGICAL_SECOND_DEFAULT ) )
		    .validator( StdValidators.CheckDoubleGtZero )
		    .help( String.format( "Define a 'logical second'; the logical step size which " +
		    					  "equates to a real-time second. " +
		    					  "If unspecified a value of %.2f will be used.",
		    					  LOGICAL_SECOND_DEFAULT ) )
		    .hint( "1.0" );
		
		logicalStepGranularityArg = argProcessor
			.addValueArg( null, CMDLINEARG_LOGICAL_STEP_GRANULARITY )
			.isRequired( false )
			.defaultValue( Integer.toString( LOGICAL_STEP_GRANULARITY_DEFAULT, 10 ) )
		    .validator( StdValidators.CheckIntGtZero )
		    .help( String.format( "Define the number of steps per logical second. If " +
		    				      "unspecified a value of %d will be used.",
		    				      LOGICAL_STEP_GRANULARITY_DEFAULT ) )
		    .hint( "1" );
		
        realtimeMultiplierArg = argProcessor
        	.addValueArg( null, CMDLINEARG_REALTIME_MULTIPLIER )
        	.isRequired( false )
			.defaultValue( Double.toString( REALTIME_MULTIPLIER_DEFAULT ) )
		    .validator( StdValidators.CheckDoubleGtZero )
        	.help( String.format( "Define the simulation rate. 1.0 is real time, 0.5 is " +
        						  "half speed, 2.0 is double speed, and so on. If unspecified " +
        						  "a value of %.2f will be used.", 
        						  REALTIME_MULTIPLIER_DEFAULT ) )
        	.hint("1.0");
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// PRIVATE CLASSES /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Class implementing a command line argument validator to check that any provided
	 * command line arguments specifying the number and type of required federates is in the
	 * correct format and contains no errors.
	 */
	public class RequiredFedValidator implements IArgValidator
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
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

		////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
	}
}

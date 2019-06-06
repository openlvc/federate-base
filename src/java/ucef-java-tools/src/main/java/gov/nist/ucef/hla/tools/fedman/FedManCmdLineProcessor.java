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
package gov.nist.ucef.hla.tools.fedman;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;

public class FedManCmdLineProcessor
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// command line arguments and defaults
	public static final String CMDLINE_ARG_HELP                       = "help";
	public static final String CMDLINE_ARG_HELP_SHORT                 = "h";
	public static final String CMDLINEARG_FEDMAN_FEDERATION_EXEC_NAME = "federation";
	public static final String CMDLINE_ARG_FEDERATION_EXEC_NAME_SHORT = "f";
	public static final String CMDLINEARG_REQUIRE                     = "require";
	public static final String CMDLINEARG_REQUIRE_SHORT               = "r";
	public static final String CMDLINEARG_FEDMAN_FEDERATE_NAME        = "fedman-name";
	public static final String DEFAULT_FEDMAN_FEDERATE_NAME           = "FederateManager";
	public static final String CMDLINEARG_FEDMAN_FEDERATE_TYPE        = "fedman-type";
	public static final String DEFAULT_FEDMAN_FEDERATE_TYPE           = "FederateManager";
	public static final String CMDLINEARG_MAX_TIME                    = "max-time";
	public static final String CMDLINEARG_LOGICAL_SECOND              = "logical-second";
	public static final String CMDLINEARG_LOGICAL_STEP_GRANULARITY    = "logical-granularity";
	public static final String CMDLINEARG_REALTIME_MULTIPLIER         = "realtime-multiplier";
	public static final String CMDLINEARG_NO_HTTP                     = "no-http";
	public static final String CMDLINEARG_HTTP_PORT                   = "http-port";

	public static final double MAX_TIME_DEFAULT                       = Double.MAX_VALUE;
	public static final double LOGICAL_SECOND_DEFAULT                 = 1.0;
	public static final int LOGICAL_STEP_GRANULARITY_DEFAULT          = 1;
	public static final double REALTIME_MULTIPLIER_DEFAULT            = 1.0;
	public static final boolean PROVIDE_HTTP_ACCESS                   = true;
	public static final int HTTP_PORT_DEFAULT                         = 8080;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String execName;
	private String federationExecName;
	private String federateName;
	private String federateType;
	private double maxTime;
	private double logicalSecond;
	private int logicalStepGranularity;
	private double realtimeMultiplier;
	private boolean withHttpService;
	private int httpServicePort;

	private Options cmdLineOptions;

	private Map<String, Integer> startRequirements;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FedManCmdLineProcessor( String execName )
	{
		this.execName = execName;
		this.startRequirements = new HashMap<>();

		this.withHttpService = true;

		buildCommandLineOptions();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void showHelp()
	{
		HelpFormatter helpFormatter = new HelpFormatter();
		String header = "Manages and coordinates federates in a federation.\n\n";
		String footer = "\n";
		helpFormatter.printHelp(this.execName, header, this.cmdLineOptions, footer, true);
	}

	public String federationExecName()
	{
		return this.federationExecName;
	}

    public String federateName()
	{
		return this.federateName;
	}

    public String federateType()
	{
		return this.federateType;
	}

    public double maxTime()
    {
    	return this.maxTime;
    }

    public double logicalSecond()
	{
		return this.logicalSecond;
	}

	public int logicalStepGranularity()
	{
		return this.logicalStepGranularity;
	}

	public double logicalStepSize()
	{
		return logicalSecond() / logicalStepGranularity();
	}

	public double realTimeMultiplier()
	{
		return this.realtimeMultiplier;
	}

	public long wallClockStepDelay()
	{
		double oneSecond = 1000.0 / realTimeMultiplier();
		return (long)(oneSecond * this.logicalStepSize());
	}

	public FedManStartRequirements startRequirements()
	{
		return new FedManStartRequirements(this.startRequirements);
	}

	public boolean withHttpService()
	{
		return this.withHttpService;
	}

	public int httpServicePort()
	{
		return this.httpServicePort;
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
	public boolean processArgs( String[] args ) throws ParseException
	{
		// will throw an ArgException if any of the command line args are "bad"
		CommandLineParser parser = new DefaultParser();
		// will throw an ParseException if any of the command line args are "bad"
		// At this stage we know that all the command arguments were parsed correctly
		// perform required validation
		CommandLine cmdLine = parser.parse( this.cmdLineOptions, args );

		if(cmdLine.hasOption( CMDLINE_ARG_HELP ))
		{
			// show help message and exit
			this.showHelp();
			System.exit( 0 );
		}

		// At this stage we know that all required command line arguments are present,
		// but we still need to validate the arguments

        // this is a required argument, so we don't need to check if it's set, but we now need to
        // check that it's valid
        this.startRequirements = extractStartRequirements(cmdLine, CMDLINEARG_REQUIRE);

        // this is a *required* argument, so we don't need to check if it's set
        this.federationExecName = extractString( cmdLine, CMDLINEARG_FEDMAN_FEDERATION_EXEC_NAME, "" );
        // optional arguments
        this.federateName = extractString( cmdLine,
                                           CMDLINEARG_FEDMAN_FEDERATE_NAME,
                                           DEFAULT_FEDMAN_FEDERATE_NAME );
        this.federateType = extractString( cmdLine,
                                           CMDLINEARG_FEDMAN_FEDERATE_TYPE,
                                           DEFAULT_FEDMAN_FEDERATE_TYPE );

        // NOTE that this is a bit of a double negative - the lack of the
        //      no-http switch means that withHttpService is true.
        this.withHttpService = !cmdLine.hasOption( CMDLINEARG_NO_HTTP );
        this.httpServicePort = extractInRangeInt( cmdLine,
                                                  CMDLINEARG_HTTP_PORT,
                                                  0, 65535,
                                                  HTTP_PORT_DEFAULT );

        // we need to sanity check some arguments with respect to each other
        // to ensure that they are "sensible" - in other words, the values
        // are all fine individually, but in combination they might cause
        // some problems
        this.maxTime = extractGtZeroDouble( cmdLine,
                                            CMDLINEARG_MAX_TIME,
                                            MAX_TIME_DEFAULT );
		this.logicalSecond = extractGtZeroDouble( cmdLine,
		                                          CMDLINEARG_LOGICAL_SECOND,
		                                          LOGICAL_SECOND_DEFAULT );
		this.logicalStepGranularity = extractGtZeroInt( cmdLine,
		                                                CMDLINEARG_LOGICAL_STEP_GRANULARITY,
		                                                LOGICAL_STEP_GRANULARITY_DEFAULT );
		this.realtimeMultiplier = extractGtZeroDouble( cmdLine,
		                                               CMDLINEARG_REALTIME_MULTIPLIER,
		                                               REALTIME_MULTIPLIER_DEFAULT );

		double oneSecond = 1000.0 / this.realtimeMultiplier;
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

	/**
	 * Utility method to extract a {@link String} from a processed command line
	 *
	 * @param cmdLine the {@link CommandLine} instance
	 * @param key the {@link String} key to identify the value to be extracted
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link CommandLine} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException
	 */
	private String extractString(CommandLine cmdLine, String key, String defaultValue) throws ParseException
	{
		if(!cmdLine.hasOption( key ))
			return defaultValue;

		return cmdLine.getOptionValue( key ).toString();
	}

	/**
	 * Utility method to extract a {@link Double} from a processed command line
	 *
	 * @param cmdLine the {@link CommandLine} instance
	 * @param key the {@link String} key to identify the value to be extracted
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link CommandLine} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException if the extracted value cannot be processed as a {@link Double}
	 */
	private double extractDouble(CommandLine cmdLine, String key, double defaultValue) throws ParseException
	{
		if( !cmdLine.hasOption( key ) )
			return defaultValue;

		Object value = cmdLine.getParsedOptionValue( key );
		if( value instanceof Double )
			return ((Double)value).doubleValue();
		else if( value instanceof Long )
			return ((Long)value).doubleValue();

		throw new ParseException( String.format( "Value for '%s%s' option must be a numeric value.",
		                                         (key.length()==1?"-":"--"), key ) );
	}

	/**
	 * Utility method to extract a {@link Double} from a processed command line, ensuring that the
	 * extracted value is greater than 0.0
	 *
	 * @param cmdLine the {@link CommandLine} instance
	 * @param key the {@link String} key to identify the value to be extracted
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link CommandLine} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException if the extracted value cannot be processed as a {@link Double}, or
	 *             the value is zero or less
	 */
	private double extractGtZeroDouble( CommandLine cmdLine, String key, double defaultValue )
	    throws ParseException
	{
		try
		{
			double value = extractDouble( cmdLine, key, defaultValue );
			if( value > 0.0 )
				return value;
		}
		catch( ParseException pe )
		{
			// ignore - fall through
		}
		throw new ParseException( String.format( "Value for '%s%s' option must be a numeric value greater than zero.",
		                                         (key.length()==1?"-":"--"), key ) );
	}

	/**
	 * Utility method to extract a {@link Integer} from a processed command line
	 *
	 * @param cmdLine the {@link CommandLine} instance
	 * @param key the {@link String} key to identify the value to be extracted
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link CommandLine} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException if the extracted value cannot be processed as an {@link Integer}
	 */
	private int extractInt(CommandLine cmdLine, String key, int defaultValue) throws ParseException
	{
		if( !cmdLine.hasOption( key ) )
			return defaultValue;

		Object value = cmdLine.getParsedOptionValue( key );
		if( value instanceof Long )
			return ((Long)value).intValue();

		throw new ParseException( String.format( "Value for '%s%s' must be a whole number.",
		                                         (key.length()==1?"-":"--"), key ) );
	}

	/**
	 * Utility method to extract a {@link Integer} from a processed command line, ensuring that the
	 * extracted value is greater than 0
	 *
	 * @param cmdLine the {@link CommandLine} instance
	 * @param key the {@link String} key to identify the value to be extracted
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link CommandLine} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException if the extracted value cannot be processed as an {@link Integer}, or
	 *             the value is zero or less
	 */
	private int extractGtZeroInt( CommandLine cmdLine, String key, int defaultValue )
		throws ParseException
	{
		try
		{
			int value = extractInt( cmdLine, key, defaultValue );
			if( value > 0 )
				return value;
		}
		catch( ParseException pe )
		{
			// ignore - fall through
		}
		throw new ParseException( String.format( "Value for '%s%s' must be a whole number greater than zero.",
		                                         (key.length()==1?"-":"--"), key ) );
	}

	/**
	 * Utility method to extract a {@link Integer} from a processed command line, ensuring that the
	 * extracted value is between the provided minimum and maximum values (inclusive)
	 *
	 * @param cmdLine the {@link CommandLine} instance
	 * @param key the {@link String} key to identify the value to be extracted
	 * @param min the minimum allowed value (inclusive)
	 * @param max the maximum allowed value (inclusive)
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link CommandLine} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException if the extracted value cannot be processed as an {@link Integer}, or
	 *             the value is outside the allowed range
	 */
	private int extractInRangeInt( CommandLine cmdLine, String key, int min, int max, int defaultValue )
		throws ParseException
	{
		try
		{
			int value = extractInt( cmdLine, key, defaultValue );
			if( value >= min && value <= max )
				return value;
		}
		catch( ParseException pe )
		{
			// ignore - fall through
		}
		throw new ParseException( String.format( "Value for '%s%s' must be a whole number between %d "+
												 "and %d (inclusive).",
		                                         (key.length()==1?"-":"--"), key, min, max ) );
	}

	/**
	 * Utility method to extract a {@link Map<String, Integer>} from a processed command line,
	 * representing the start requirements for the federation as federate types to minimum counts
	 *
	 * @param cmdLine the {@link CommandLine} instance
	 * @param key the {@link String} key to identify the value to be extracted
	 * @return the extracted value
	 * @throws ParseException if the extracted value cannot be processed
	 */
	private Map<String, Integer> extractStartRequirements(CommandLine cmdLine, String key) throws ParseException
	{
		String[] values = cmdLine.getOptionValues( key );

		boolean isValid = true;
		String lastCheckedItem = null;
		Map<String, Integer> result = new HashMap<>();
		try
		{
			// check all the values - should be in the form "FEDERATE_TYPE,COUNT", where
			// FEDERATE_TYPE is a string containing the federate type, and COUNT is an integer
			// greater than 0 representing the number of that federate type required
			for( String s : values )
			{
				lastCheckedItem = s;
				String[] parts = s.split( "," );
				if( parts.length != 2 )
				{
					// something wrong with the expected comma separated value
					isValid = false;
					break;
				}
				String federateType = parts[0];
				if( federateType.length() == 0 )
				{
					// something wrong with the federate type name (empty)
					isValid = false;
					break;
				}
				int count = Integer.parseInt( parts[1], 10 );
				if( count < 1 )
				{
					// something wrong with the count (not a number or less than 1)
					isValid = false;
					break;
				}
				result.put(federateType, count);
			}

			if( isValid )
				return result;
		}
		catch( Exception e )
		{
			// something has gone wrong - doesn't really matter
			// what, just fall through
		}

		throw new ParseException( String.format( "'%s' is not in the correct format for '%s%s'. Values "+
												 "must be a federate type name and a whole number "+
												 "greater than zero separated by a comma. "+
												 "For example, 'FedABC,2'.",
												 lastCheckedItem, (key.length()==1?"-":"--"), key ) );
	}

	/**
	 * Utility method to build the command line options for the Federation Manager
	 *
	 * @return command line options for the Federation Manager
	 */
	private Options buildCommandLineOptions()
	{
		Option help = Option.builder( CMDLINE_ARG_HELP_SHORT )
			.longOpt( CMDLINE_ARG_HELP )
			.desc("print this message and exit." )
			.build();

		Option federationExecNameArg = Option.builder(CMDLINE_ARG_FEDERATION_EXEC_NAME_SHORT)
        	.longOpt(CMDLINEARG_FEDMAN_FEDERATION_EXEC_NAME)
			.hasArg()
        	.argName( "federation name" )
        	.required( true )
        	.desc( "Set the name of the federation the Federation Manager will join." )
			.type( PatternOptionBuilder.STRING_VALUE )
        	.build();

        Option federateNameArg = Option.builder()
        	.longOpt( CMDLINEARG_FEDMAN_FEDERATE_NAME )
			.hasArg()
        	.argName( "federate name" )
        	.required( false )
        	.desc( String.format( "Set the federate name for the Federation Manager to use. " +
        						  "If unspecified a value of '%s' will be used.",
        						  FedManConstants.DEFAULT_FEDMAN_FEDERATE_NAME ) )
			.type( PatternOptionBuilder.STRING_VALUE )
        	.build();

        Option federateTypeArg = Option.builder()
        	.longOpt( CMDLINEARG_FEDMAN_FEDERATE_TYPE )
			.hasArg()
        	.argName( "federate type" )
        	.required( false )
        	.desc( String.format( "Set the federate type for the Federation Manager to use. " +
        						  "If unspecified a value of '%s' will be used.",
        						  FedManConstants.DEFAULT_FEDMAN_FEDERATE_TYPE ) )
			.type( PatternOptionBuilder.STRING_VALUE )
        	.build();

        Option requiredFederateTypes = Option.builder( CMDLINEARG_REQUIRE_SHORT )
        	.longOpt( CMDLINEARG_REQUIRE )
			.hasArgs()
        	.argName( "FEDERATE_TYPE,COUNT" )
        	.required( true )
		    .desc( String.format( "Define required federate types and minimum counts. For example, " +
		                          "'-%s FedABC,2' would require at least two 'FedABC' type federates " +
		                          "to join. Multiple requirements can be specified by repeated use " +
		                          "of -%s.",
		                          CMDLINEARG_REQUIRE_SHORT,
		                          CMDLINEARG_REQUIRE_SHORT ) )
			.type( PatternOptionBuilder.STRING_VALUE )
		    .build();

        Option maxTimeArg = Option.builder()
        	.longOpt( CMDLINEARG_MAX_TIME )
			.hasArg()
        	.argName( "max" )
        	.required( false )
        	.desc( String.format( "Set the maximum logical time to which the simulation may run." +
        						  "If unspecified the simulation will run indefinitely." ) )
        	.type( PatternOptionBuilder.NUMBER_VALUE )
        	.build();

		Option logicalSecondArg = Option.builder()
        	.longOpt( CMDLINEARG_LOGICAL_SECOND )
			.required( false )
		    .desc( String.format( "Define a 'logical second'; the logical step size which " +
		    					  "equates to a real-time second. " +
		    					  "If unspecified a value of %.2f will be used.",
		    					  LOGICAL_SECOND_DEFAULT ) )
			.type( PatternOptionBuilder.NUMBER_VALUE )
		    .build();

		Option logicalStepGranularityArg = Option.builder()
        	.longOpt( CMDLINEARG_LOGICAL_STEP_GRANULARITY )
			.required( false )
		    .desc( String.format( "Define the number of steps per logical second. If " +
		    				      "unspecified a value of %d will be used.",
		    				      LOGICAL_STEP_GRANULARITY_DEFAULT ) )
			.type( PatternOptionBuilder.NUMBER_VALUE )
		    .build();

        Option realtimeMultiplierArg = Option.builder()
        	.longOpt( CMDLINEARG_REALTIME_MULTIPLIER )
        	.required( false )
        	.desc( String.format( "Define the simulation rate. 1.0 is real time, 0.5 is " +
        						  "half speed, 2.0 is double speed, and so on. If unspecified " +
        						  "a value of %.2f will be used.",
        						  REALTIME_MULTIPLIER_DEFAULT ) )
			.type( PatternOptionBuilder.NUMBER_VALUE )
        	.build();

        Option noHttpSwitch = Option.builder()
        	.longOpt( CMDLINEARG_NO_HTTP )
        	.required( false )
        	.desc( String.format( "Turn off the HTTP service which provides REST-like endpoints " +
        						  "to control the Federation Manager. If unspecified " +
        						  "the HTTP service will be active (see also --%s).",
        						  CMDLINEARG_HTTP_PORT) )
        	.build();

		Option httpPortArg = Option.builder()
        	.longOpt( CMDLINEARG_HTTP_PORT )
			.hasArg()
        	.argName( "port" )
			.required( false )
		    .desc( String.format( "Specify the port to provide the HTTP service on. Only relevant if " +
		    				      "the HTTP service is active (see also --%s). If unspecified, "+
		    				      "port %d will be used.",
		    				      CMDLINEARG_NO_HTTP, HTTP_PORT_DEFAULT ) )
			.type( PatternOptionBuilder.NUMBER_VALUE )
		    .build();

		this.cmdLineOptions = new Options();

		cmdLineOptions.addOption( help );
		cmdLineOptions.addOption( federationExecNameArg );
		cmdLineOptions.addOption( federateNameArg );
		cmdLineOptions.addOption( federateTypeArg );
		cmdLineOptions.addOption( requiredFederateTypes );
		cmdLineOptions.addOption( maxTimeArg );
		cmdLineOptions.addOption( logicalSecondArg );
		cmdLineOptions.addOption( logicalStepGranularityArg );
		cmdLineOptions.addOption( realtimeMultiplierArg );
		cmdLineOptions.addOption( noHttpSwitch );
		cmdLineOptions.addOption( httpPortArg );

		return cmdLineOptions;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

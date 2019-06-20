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

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import gov.nist.ucef.hla.base.UCEFException;

public class FedManCmdLineProcessor
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getLogger( FedManCmdLineProcessor.class );

	// command line arguments
	private static final String CMDLINE_ARG_HELP                        = "help";
	private static final String CMDLINE_ARG_HELP_SHORT                  = "h";
	private static final String CMDLINE_ARG_JSON_CONFIG_FILE            = "config";
	private static final String CMDLINE_ARG_FEDMAN_FEDERATION_EXEC_NAME = "federation-name";
	private static final String CMDLINE_ARG_FEDERATION_EXEC_NAME_SHORT  = "f";
	private static final String CMDLINE_ARG_REQUIRE                     = "require";
	private static final String CMDLINE_ARG_REQUIRE_SHORT               = "r";
	private static final String CMDLINE_ARG_FEDMAN_FEDERATE_NAME        = "fedman-name";
	private static final String CMDLINE_ARG_FEDMAN_FEDERATE_TYPE        = "fedman-type";
	private static final String CMDLINE_ARG_MAX_TIME                    = "max-time";
	private static final String CMDLINE_ARG_LOGICAL_SECOND              = "logical-second";
	private static final String CMDLINE_ARG_LOGICAL_STEP_GRANULARITY    = "logical-granularity";
	private static final String CMDLINE_ARG_REALTIME_MULTIPLIER         = "realtime-multiplier";
	private static final String CMDLINE_ARG_HTTP_PORT                   = "http-port";
	private static final String CMDLINE_SWITCH_NO_HTTP_SERVICE          = "no-http-service";

	// JSON config keys (mostly the same as command line arguments)
	private static final String JSON_CONFIG_KEY_WITH_HTTP               = "http-service";

	// default config values, as required
	private static final String FEDMAN_FEDERATE_NAME_DEFAULT           = "FederationManager";
	private static final String FEDMAN_FEDERATE_TYPE_DEFAULT           = FEDMAN_FEDERATE_NAME_DEFAULT;
	private static final double MAX_TIME_DEFAULT                       = Double.MAX_VALUE;
	private static final double LOGICAL_SECOND_DEFAULT                 = 1.0;
	private static final int LOGICAL_STEP_GRANULARITY_DEFAULT          = 1;
	private static final double REALTIME_MULTIPLIER_DEFAULT            = 1.0;
	private static final boolean HTTP_SERVICE_ACTIVE_DEFAULT           = true;
	private static final int HTTP_PORT_DEFAULT                         = 8080;

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
	private boolean withHttpServiceActive;
	private int httpServicePort;
	private String configFile;

	private Options cmdLineOptions;

	private Map<String, Integer> startRequirements;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FedManCmdLineProcessor( String execName )
	{
		this.execName = execName;

		// required arguments
		this.federationExecName     = null;
		this.startRequirements      = null;
		// optional arguments
		this.federateName           = FEDMAN_FEDERATE_NAME_DEFAULT;
		this.federateType           = FEDMAN_FEDERATE_TYPE_DEFAULT;
		this.withHttpServiceActive  = HTTP_SERVICE_ACTIVE_DEFAULT;
		this.httpServicePort        = HTTP_PORT_DEFAULT;
		this.maxTime                = MAX_TIME_DEFAULT;
		this.logicalSecond          = LOGICAL_SECOND_DEFAULT;
		this.logicalStepGranularity = LOGICAL_STEP_GRANULARITY_DEFAULT;
		this.realtimeMultiplier     = REALTIME_MULTIPLIER_DEFAULT;

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

	public boolean hasFederationExecName()
	{
		return this.federationExecName != null;
	}

	public boolean hasStartRequirements()
	{
		return this.startRequirements != null;
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
		return new FedManStartRequirements( this.startRequirements );
	}

	public boolean withHttpServiceActive()
	{
		return this.withHttpServiceActive;
	}

	public int httpServicePort()
	{
		return this.httpServicePort;
	}

	public boolean isConfigFileSpecified()
	{
		return this.configFile != null && this.configFile.length() > 0;
	}

	public String configFile()
	{
		return this.configFile;
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

		// pull data from configuration file if specified - we do this FIRST of all
		// so that command line argument values take precendence over any config file values
		this.configFile = extractCmdLineString( cmdLine, CMDLINE_ARG_JSON_CONFIG_FILE, "" );
		if( isConfigFileSpecified() )
		{
			extractConfigFromJSON( this.configFile );
		}

		// note that this is not "required" to be in the command line args, but must be specified by
		// either the command line or from JSON configuration data by the time we start up
		Map<String,Integer> tempStartRequirements = extractCmdLineStartRequirements( cmdLine,
		                                                                             CMDLINE_ARG_REQUIRE );
		if( tempStartRequirements != null )
			this.startRequirements = tempStartRequirements;
		// note that this is not "required" to be in the command line args, but must be specified by
		// either the command line or from JSON configuration data by the time we start up
		String tempFederationExecName =
		    extractCmdLineString( cmdLine, CMDLINE_ARG_FEDMAN_FEDERATION_EXEC_NAME, null );
		if( tempFederationExecName != null )
			this.federationExecName = tempFederationExecName;

        // optional arguments
        this.federateName = extractCmdLineString( cmdLine,
                                           CMDLINE_ARG_FEDMAN_FEDERATE_NAME,
                                           FEDMAN_FEDERATE_NAME_DEFAULT );
        this.federateType = extractCmdLineString( cmdLine,
                                           CMDLINE_ARG_FEDMAN_FEDERATE_TYPE,
                                           FEDMAN_FEDERATE_TYPE_DEFAULT );

        // NOTE that this is a bit of a double negative - the lack of the
        //      no-http switch means that withHttpService is true.
        this.withHttpServiceActive = !cmdLine.hasOption( CMDLINE_SWITCH_NO_HTTP_SERVICE );
        this.httpServicePort = extractCmdLineInRangeInt( cmdLine,
                                                  CMDLINE_ARG_HTTP_PORT,
                                                  0, 65535,
                                                  HTTP_PORT_DEFAULT );

        // we need to sanity check some arguments with respect to each other
        // to ensure that they are "sensible" - in other words, the values
        // are all fine individually, but in combination they might cause
        // some problems
        this.maxTime = extractCmdLineGtZeroDouble( cmdLine,
                                            CMDLINE_ARG_MAX_TIME,
                                            MAX_TIME_DEFAULT );
		this.logicalSecond = extractCmdLineGtZeroDouble( cmdLine,
		                                          CMDLINE_ARG_LOGICAL_SECOND,
		                                          LOGICAL_SECOND_DEFAULT );
		this.logicalStepGranularity = extractCmdLineGtZeroInt( cmdLine,
		                                                CMDLINE_ARG_LOGICAL_STEP_GRANULARITY,
		                                                LOGICAL_STEP_GRANULARITY_DEFAULT );
		this.realtimeMultiplier = extractCmdLineGtZeroDouble( cmdLine,
		                                               CMDLINE_ARG_REALTIME_MULTIPLIER,
		                                               REALTIME_MULTIPLIER_DEFAULT );

		double oneSecond = 1000.0 / this.realtimeMultiplier;
		double logicalStepSize = logicalSecond / logicalStepGranularity;
		double wallClockStepDelay = (long)(oneSecond * logicalStepSize);

		if( wallClockStepDelay < 5 )
		{
			System.err.println( String.format( "The specified value(s) for " +
											   "--%s, --%s and/or --%s " +
			                                   " cannot be achieved (tick rate is too high).",
			                                   CMDLINE_ARG_LOGICAL_SECOND,
			                                   CMDLINE_ARG_LOGICAL_STEP_GRANULARITY,
			                                   CMDLINE_ARG_REALTIME_MULTIPLIER ) );
			return false;
		}
		else if( wallClockStepDelay < 20 )
		{
			System.out.println( String.format( "WARNING: The specified value(s) for " +
											   "--%s, --%s and/or --%s " +
											   "requires a tick rate higher than 50 ticks" +
											   "per second - your simulation may not " +
											   "keep up with your requirements.",
											   CMDLINE_ARG_LOGICAL_SECOND,
											   CMDLINE_ARG_LOGICAL_STEP_GRANULARITY,
											   CMDLINE_ARG_REALTIME_MULTIPLIER ) );
		}

		// all command line arguments are present and correct!
		return true;
	}

	/**
	 * Obtain a {@link File} resource instance based on a path. The resource is looked for on the
	 * file system and as a resource (as in a packaged JAR)
	 *
	 * @param path the path for the resource
	 * @return the resource as a {@link File} instance, or null if no such resource could be
	 *         located
	 */
	private File getResourceFile( String path )
	{
		File file = new File( path );
		if( file.exists() && file.isFile() )
			return file;

		URL fileUrl = this.getClass().getClassLoader().getResource( path );
		if( fileUrl != null )
			return new File( fileUrl.getFile() );

		return null;
	}

	private void extractConfigFromJSON( String jsonConfigSource )
	{
		// see if the configuration source is a file
		File configFile = getResourceFile( jsonConfigSource );
		boolean isFile = configFile != null;

		// assume for the moment that the JSON is coming directly from the
		// configuration source parameter
		String json = jsonConfigSource;
		if(isFile)
		{
			// the configuration source is actually a file - read the bytes
			// from it into a string for processing
			try
			{
				json = new String( Files.readAllBytes( configFile.toPath() ) );
			}
			catch( Exception e )
			{
				throw new UCEFException( e, "Unable to read JSON configuration from '%s'.",
				                         configFile.getAbsolutePath() );
			}
		}

		// at this point, we have a string to work with - make sure it's valid JSON
		Object parsedString = null;
		try
		{
			parsedString = new JSONParser().parse(json);
		}
		catch( Exception e )
		{
			String msg = "Configuration is not valid JSON.";
			if(isFile)
			{
				msg = String.format( "Configuration is not valid JSON in '%s'.",
				                     configFile.getAbsolutePath() );
			}
			throw new UCEFException( e, msg );
		}

		// at this point, we have a valid JSON object of some form, but we
		// need to make sure that it is a single JSONObject instance (and
		// not something else like a JSONArray)
		if(!(parsedString instanceof JSONObject))
		{
			String msg = "Could not find root JSON object.";
			if(isFile)
			{
				msg = String.format( "Could not find root JSON object in '%s'.",
				                     configFile.getAbsolutePath() );
			}
			throw new UCEFException( msg );
		}

		// we now have a JSONObject to extract data from
		JSONObject configData = (JSONObject)parsedString;

		if(logger.isWarnEnabled())
		{
			// for the purposes of debugging problems, show warnings for
			// any unrecognized configuration items found so that
			// problems can be resolved quickly (such as typos in the
			// config JSON keys etc)
			Set<String> recognizedConfigurationKeys = new HashSet<>();
			recognizedConfigurationKeys.addAll( Arrays.asList(new String[]
				{
				    CMDLINE_ARG_REQUIRE,
					CMDLINE_ARG_FEDMAN_FEDERATION_EXEC_NAME,
					CMDLINE_ARG_FEDMAN_FEDERATE_NAME,
					CMDLINE_ARG_FEDMAN_FEDERATE_TYPE,
					CMDLINE_ARG_MAX_TIME,
					CMDLINE_ARG_LOGICAL_SECOND,
					CMDLINE_ARG_LOGICAL_STEP_GRANULARITY,
					CMDLINE_ARG_REALTIME_MULTIPLIER,
					CMDLINE_SWITCH_NO_HTTP_SERVICE,
					CMDLINE_ARG_HTTP_PORT
				}
			));
			for(Object key : configData.keySet())
			{
				if(!recognizedConfigurationKeys.contains( key ))
				{
					Object value = configData.get(key);
					logger.warn( String.format( "Configuration item '%s' with "+
												"value '%s' in JSON configuration data "+
												"is not recognized and will be ignored.",
					                            key.toString(), value.toString() )
					);
				}
			}
		}

		// now we can process the configuration data from the JSONObject
		try
		{
			// process configuration - note that in *all* cases we try to look
			// up the value from the JSON and fall back to the existing value
			// if there is no value available

			// note that this is not "required" to be in the JSON, but must be specified by either
			// the command line or from JSON configuration data by the time we start up
			String tempFederationExecName = extractJsonNonEmptyString( configData,
			                                                           CMDLINE_ARG_FEDMAN_FEDERATION_EXEC_NAME,
			                                                           null );
			if( tempFederationExecName != null )
				this.federationExecName = tempFederationExecName;
			// note that this is not "required" to be in the JSON, but must be specified by either
			// the command line or from JSON configuration data by the time we start up
			Map<String,Integer> tempStartRequirements = extractJsonStartRequirements( configData,
			                                                                          CMDLINE_ARG_REQUIRE );
			if( tempStartRequirements != null )
				this.startRequirements = tempStartRequirements;

			this.federateName = extractJsonNonEmptyString( configData,
			                                               CMDLINE_ARG_FEDMAN_FEDERATE_NAME,
			                                               this.federateName );
			this.federateType = extractJsonNonEmptyString( configData,
			                                               CMDLINE_ARG_FEDMAN_FEDERATE_TYPE,
			                                               this.federateType );
			this.maxTime = extractJsonGtZeroDouble( configData, CMDLINE_ARG_MAX_TIME, this.maxTime );
			this.logicalSecond = extractJsonGtZeroDouble( configData,
			                                          CMDLINE_ARG_LOGICAL_SECOND,
			                                          this.logicalSecond );
			this.logicalStepGranularity = extractJsonGtZeroInt( configData,
			                                                    CMDLINE_ARG_LOGICAL_STEP_GRANULARITY,
			                                                    this.logicalStepGranularity );
			this.realtimeMultiplier = extractJsonGtZeroDouble( configData,
			                                                   CMDLINE_ARG_REALTIME_MULTIPLIER,
			                                                   this.realtimeMultiplier );
			this.withHttpServiceActive = extractJsonBoolean( configData,
			                                                 JSON_CONFIG_KEY_WITH_HTTP,
			                                                 this.withHttpServiceActive );
			this.httpServicePort = extractJsonInRangeInt( configData,
			                                              CMDLINE_ARG_HTTP_PORT,
			                                              0, 65535,
			                                              this.httpServicePort );
		}
		catch( Exception e )
		{
			String msg = "There was a problem processing the configuration JSON.";
			if(isFile)
			{
				msg = String.format( "There was a problem processing the configuration JSON in '%s'.",
				                     configFile.getAbsolutePath() );
			}
			throw new UCEFException( e, msg );
		}
	}

	/**
	 * Utility method to extract a {@link String} value from a {@link JSONObject} based on a
	 * {@link String} key
	 *
	 * If the value extracted from the key is not a string, the value is rejected, and
	 * a {@link UCEFException} will be thrown in this case.
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return if the provided {@link JSONObject} does not contain
	 *            the given {@link String} key
	 * @return the extracted value, or the default value if there was no such key
	 */
	private String extractJsonString( JSONObject root, String key, String defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			if( value instanceof String )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s' in configuration file", value, key ) );

				return value.toString();
			}

			throw new UCEFException( "Expected a string value for '%s' but found '%s' in configuration file",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s' in configuration file, using default value '%s'", key, defaultValue ) );

		return defaultValue;
	}

	/**
	 * Utility method to extract a {@link String} value from a {@link JSONObject} based on a
	 * {@link String} key
	 *
	 * If the value extracted from the key is not a string, the value is rejected, and
	 * a {@link UCEFException} will be thrown in this case.
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return if the provided {@link JSONObject} does not contain
	 *            the given {@link String} key
	 * @return the extracted value, or the default value if there was no such key
	 */
	private String extractJsonNonEmptyString( JSONObject root, String key, String defaultValue ) throws ParseException
	{
		String value = extractJsonString( root, key, defaultValue );
		if( value == null || value.trim().length() > 0 )
			return value;
		throw new ParseException( String.format( "Value for '%s' in configuration file may not be "+
												 "an empty string or consist only of whitespace.",
		                                         key ) );
	}

	/**
	 * Utility method to extract an {@link Integer} value from a {@link JSONObject} based on a
	 * {@link String} key
	 *
	 * If the value extracted from the key is not an integer, the value is rejected, and
	 * a {@link UCEFException} will be thrown in this case.
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return if the provided {@link JSONObject} does not contain
	 *            the given {@link String} key
	 * @return the extracted value, or the default value if there was no such key
	 */
	private int extractJsonInt( JSONObject root, String key, int defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			// integers in JSON data are actually parsed out as longs
			if( value instanceof Long )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s' in configuration file.", value, key ) );

				return ((Long)value).intValue();
			}

			throw new UCEFException( "Expected an integer value for '%s' but found '%s' in configuration file.",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s' in configuration file, using default value '%s'", key, defaultValue ) );

		return defaultValue;
	}

	/**
	 * Utility method to extract a {@link Integer} from a JSON object, ensuring that the
	 * extracted value is greater than 0
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link JSONObject} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException if the extracted value cannot be processed as a {@link Integer}, or
	 *             the value is zero or less
	 */
	private int extractJsonGtZeroInt( JSONObject root, String key, int defaultValue )
	    throws ParseException
	{
		int value = extractJsonInt( root, key, defaultValue );
		if( value > 0.0 )
			return value;
		throw new ParseException( String.format( "Value for '%s' in configuration file must be a "+
												 "whole number greater than zero.",
		                                         key ) );
	}

	/**
	 * Utility method to extract a {@link Integer} from a JSON object, ensuring that the
	 * extracted value is between the provided minimum and maximum values (inclusive)
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param min the minimum allowed value (inclusive)
	 * @param max the maximum allowed value (inclusive)
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link JSONObject} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException if the extracted value cannot be processed as an {@link Integer}, or
	 *             the value is outside the allowed range
	 */
	private int extractJsonInRangeInt( JSONObject root, String key, int min, int max, int defaultValue )
		throws ParseException
	{
		int value = extractJsonInt( root, key, defaultValue );
		if( value >= min && value <= max )
			return value;
		throw new ParseException( String.format( "Value for '%s' in configuration file must be a " +
												 "whole number between %d and %d (inclusive).",
		                                         key, min, max ) );
	}


	/**
	 * Utility method to extract a {@link Double} value from a {@link JSONObject} based on a
	 * {@link String} key.
	 *
	 * The content associated with the key is expected to be a double. However it will
	 * also leniently treat an integer as a double.
	 *
	 * If the value extracted from the key is not a double, the value is rejected, and
	 * a {@link UCEFException} will be thrown in this case.
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return if the provided {@link JSONObject} does not contain
	 *            the given {@link String} key
	 * @return the extracted value, or the default value if there was no such key
	 */
	private double extractJsonDouble( JSONObject root, String key, double defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			if( value instanceof Double )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s' in configuration file", value, key ) );

				return (Double)value;
			}
			// be lenient on the parsing of doubles, and allow integer
			// values to get through as well
			if( value instanceof Long )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s' in configuration file", value, key ) );

				return ((Long)value).doubleValue();
			}

			throw new UCEFException( "Expected a double value for '%s' but found '%s' in configuration file",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s' in configuration file, using default value '%s'", key, defaultValue ) );

		return defaultValue;
	}

	/**
	 * Utility method to extract a {@link Double} from JSON object, ensuring that the
	 * extracted value is greater than 0.0
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return in the event that no such value is specified in the
	 *            given {@link CommandLine} instance
	 * @return the extracted value, or default value in the case that no value could be extracted
	 * @throws ParseException if the extracted value cannot be processed as a {@link Double}, or
	 *             the value is zero or less
	 */
	private double extractJsonGtZeroDouble( JSONObject root, String key, double defaultValue )
	    throws ParseException
	{
		double value = extractJsonDouble( root, key, defaultValue );
		if( value > 0.0 )
			return value;
		throw new ParseException( String.format( "Value for '%s' in configuration file must be a "+
												 "numeric value greater than zero.",
		                                         key ) );
	}

	/**
	 * Utility method to extract a {@link Boolean} value from a {@link JSONObject} based on a
	 * {@link String} key
	 *
	 * If the value extracted from the key is not a boolean, the value is rejected, and
	 * a {@link UCEFException} will be thrown in this case.
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return if the provided {@link JSONObject} does not contain
	 *            the given {@link String} key
	 * @return the extracted value, or the default value if there was no such key
	 */
	private boolean extractJsonBoolean( JSONObject root, String key, boolean defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			if( value instanceof Boolean )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s' in configuration file", value, key ) );

				return (Boolean)value;
			}

			throw new UCEFException( "Expected a boolean value for '%s' but found '%s' in configuration file",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s' in configuration file, using default value '%s'", key, defaultValue ) );

		return defaultValue;
	}

	/**
	 * Utility method to extract a {@link Map<String, Integer>} from a JSON object, which
	 * represents the start requirements for the federation as federate types to minimum counts
	 *
	 * @param root the {@link JSONObject} instance
	 * @param key the {@link String} key to identify the value to be extracted
	 * @return the extracted value, or null if no value for the specified key exists
	 * @throws ParseException if the extracted value cannot be processed
	 */
	private Map<String, Integer> extractJsonStartRequirements(JSONObject root, String key) throws ParseException
	{
		if(!root.containsKey( key ))
			return null;

		String typeKey = "type";
		String countKey = "count";
		String errorMsg = String.format( "'%s' is not in the correct format.%%s Values are expected " +
		                                 "to be an array of JSON objects of the form " +
		                                 "'{\"%s\":\"FEDERATE_TYPE\",\"%s\":COUNT}'. " +
		                                 "Note that COUNT *must* be a whole number greater than zero. " +
		                                 "For example, '{\"%s\":\"FedABC\",\"%s\":2}'.",
		                                 key, typeKey, countKey, typeKey, countKey );
		Object value = root.get( key );

		// value must be a JSON array
		if( !(value instanceof JSONArray) )
			throw new ParseException( String.format( errorMsg, " The value was not a JSON array." ) );

		JSONArray jsonArray = (JSONArray)value;
		Map<String, Integer> result = new HashMap<String, Integer>();
		// check all the values - each should be in the form...
		//     {"type":"FEDERATE_TYPE","count":COUNT}
		// ...where FEDERATE_TYPE is a string containing the federate type, and COUNT is an
		// integer greater than 0 representing the number of that federate type required
		int idx = 0;
		for( Object item : jsonArray )
		{
			// each item in the array must be a JSON object
			if( !(item instanceof JSONObject) )
			{
				String detail = String.format( " Item #%d in the array was not a JSON object.", idx );
				throw new ParseException( String.format( errorMsg, detail ) );
			}
			JSONObject jsonObject = (JSONObject)item;

			// the 'type' and 'count' keys must both be present on each and every item
			if( !jsonObject.containsKey( typeKey ) || !jsonObject.containsKey( countKey ) )
			{
				String detail = String.format( " Item #%d in the array was missing the '%s' and/or '%s' key(s).", idx, typeKey, countKey );
				throw new ParseException( String.format( errorMsg, detail ) );
			}
			Object typeCandidate = jsonObject.get( typeKey );

			// the value associated with the 'type' key must be a non-empty String
			if( !(typeCandidate instanceof String) )
			{
				String detail = String.format( " Item #%d in the array did not have a text value for '%s'.", idx, typeKey );
				throw new ParseException( String.format( errorMsg, detail ) );
			}
			String typeValue = typeCandidate.toString().trim();
			if( typeValue.length() == 0 )
			{
				String detail = String.format( " Item #%d in the array had an empty string for '%s'.", idx, typeKey );
				throw new ParseException( String.format( errorMsg, detail ) );
			}

			// the value associated with the 'count' key must be an integer greater than zero
			Object countCandidate = jsonObject.get( countKey );
			if( !(countCandidate instanceof Long) )
			{
				String detail = String.format( " Item #%d in the array had an non-integer value for '%s'.", idx, countKey );
				throw new ParseException( String.format( errorMsg, detail ) );
			}
			Long countValue = (Long)countCandidate;
			if( countValue <= 0 )
			{
				String detail = String.format( " Item #%d in the array had an negative value for '%s'.", idx, countKey );
				throw new ParseException( String.format( errorMsg, detail ) );
			}

			// everything checks out - add the details
			result.put( typeValue, countValue.intValue() );
		}
		return result;
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
	private String extractCmdLineString(CommandLine cmdLine, String key, String defaultValue) throws ParseException
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
	private double extractCmdLineDouble(CommandLine cmdLine, String key, double defaultValue) throws ParseException
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
	private double extractCmdLineGtZeroDouble( CommandLine cmdLine, String key, double defaultValue )
	    throws ParseException
	{
		try
		{
			double value = extractCmdLineDouble( cmdLine, key, defaultValue );
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
	private int extractCmdLineInt(CommandLine cmdLine, String key, int defaultValue) throws ParseException
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
	private int extractCmdLineGtZeroInt( CommandLine cmdLine, String key, int defaultValue )
		throws ParseException
	{
		try
		{
			int value = extractCmdLineInt( cmdLine, key, defaultValue );
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
	private int extractCmdLineInRangeInt( CommandLine cmdLine, String key, int min, int max, int defaultValue )
		throws ParseException
	{
		try
		{
			int value = extractCmdLineInt( cmdLine, key, defaultValue );
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
	private Map<String, Integer> extractCmdLineStartRequirements(CommandLine cmdLine, String key) throws ParseException
	{
		if( !cmdLine.hasOption( key ) )
			return null;

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
			.required( false )
			.desc("print this message and exit." )
			.build();

		// note that this is not "required", but must be specified by either the command line or
		// from JSON configuration data by the time we start up
		Option federationExecNameArg = Option.builder(CMDLINE_ARG_FEDERATION_EXEC_NAME_SHORT)
        	.longOpt(CMDLINE_ARG_FEDMAN_FEDERATION_EXEC_NAME)
			.hasArg()
        	.argName( "federation name" )
        	.required( false )
        	.desc( "Set the name of the federation the Federation Manager will join." )
			.type( PatternOptionBuilder.STRING_VALUE )
        	.build();

		// note that this is not "required", but must be specified by either the command line or
		// from JSON configuration data by the time we start up
        Option requiredFederateTypes = Option.builder( CMDLINE_ARG_REQUIRE_SHORT )
        	.longOpt( CMDLINE_ARG_REQUIRE )
			.hasArgs()
        	.argName( "FEDERATE_TYPE,COUNT" )
        	.required( false )
		    .desc( String.format( "Define required federate types and minimum counts. For example, " +
		                          "'-%s FedABC,2' would require at least two 'FedABC' type federates " +
		                          "to join. Multiple requirements can be specified by repeated use " +
		                          "of -%s.",
		                          CMDLINE_ARG_REQUIRE_SHORT,
		                          CMDLINE_ARG_REQUIRE_SHORT ) )
			.type( PatternOptionBuilder.STRING_VALUE )
		    .build();

        Option federateNameArg = Option.builder()
        	.longOpt( CMDLINE_ARG_FEDMAN_FEDERATE_NAME )
			.hasArg()
        	.argName( "federate name" )
        	.required( false )
        	.desc( String.format( "Set the federate name for the Federation Manager to use. " +
        						  "If unspecified a value of '%s' will be used.",
        						  FedManConstants.DEFAULT_FEDMAN_FEDERATE_NAME ) )
			.type( PatternOptionBuilder.STRING_VALUE )
        	.build();

        Option federateTypeArg = Option.builder()
        	.longOpt( CMDLINE_ARG_FEDMAN_FEDERATE_TYPE )
			.hasArg()
        	.argName( "federate type" )
        	.required( false )
        	.desc( String.format( "Set the federate type for the Federation Manager to use. " +
        						  "If unspecified a value of '%s' will be used.",
        						  FedManConstants.DEFAULT_FEDMAN_FEDERATE_TYPE ) )
			.type( PatternOptionBuilder.STRING_VALUE )
        	.build();

        Option maxTimeArg = Option.builder()
        	.longOpt( CMDLINE_ARG_MAX_TIME )
			.hasArg()
        	.argName( "max" )
        	.required( false )
        	.desc( String.format( "Set the maximum logical time to which the simulation may run." +
        						  "If unspecified the simulation will run indefinitely." ) )
        	.type( PatternOptionBuilder.NUMBER_VALUE )
        	.build();

		Option logicalSecondArg = Option.builder()
        	.longOpt( CMDLINE_ARG_LOGICAL_SECOND )
			.required( false )
		    .desc( String.format( "Define a 'logical second'; the logical step size which " +
		    					  "equates to a real-time second. " +
		    					  "If unspecified a value of %.2f will be used.",
		    					  LOGICAL_SECOND_DEFAULT ) )
			.type( PatternOptionBuilder.NUMBER_VALUE )
		    .build();

		Option logicalStepGranularityArg = Option.builder()
        	.longOpt( CMDLINE_ARG_LOGICAL_STEP_GRANULARITY )
			.required( false )
		    .desc( String.format( "Define the number of steps per logical second. If " +
		    				      "unspecified a value of %d will be used.",
		    				      LOGICAL_STEP_GRANULARITY_DEFAULT ) )
			.type( PatternOptionBuilder.NUMBER_VALUE )
		    .build();

        Option realtimeMultiplierArg = Option.builder()
        	.longOpt( CMDLINE_ARG_REALTIME_MULTIPLIER )
        	.required( false )
        	.desc( String.format( "Define the simulation rate. 1.0 is real time, 0.5 is " +
        						  "half speed, 2.0 is double speed, and so on. If unspecified " +
        						  "a value of %.2f will be used.",
        						  REALTIME_MULTIPLIER_DEFAULT ) )
			.type( PatternOptionBuilder.NUMBER_VALUE )
        	.build();

        Option noHttpSwitch = Option.builder()
        	.longOpt( CMDLINE_SWITCH_NO_HTTP_SERVICE )
        	.required( false )
        	.desc( String.format( "Turn off the HTTP service which provides REST-like endpoints " +
        						  "to control the Federation Manager. If unspecified " +
        						  "the HTTP service will be active (see also --%s).",
        						  CMDLINE_ARG_HTTP_PORT) )
        	.build();

		Option httpPortArg = Option.builder()
        	.longOpt( CMDLINE_ARG_HTTP_PORT )
			.hasArg()
        	.argName( "port" )
			.required( false )
		    .desc( String.format( "Specify the port to provide the HTTP service on. Only relevant if " +
		    				      "the HTTP service is active (see also --%s). If unspecified, "+
		    				      "port %d will be used.",
		    				      CMDLINE_SWITCH_NO_HTTP_SERVICE, HTTP_PORT_DEFAULT ) )
			.type( PatternOptionBuilder.NUMBER_VALUE )
		    .build();

        Option configLocation = Option.builder()
        	.longOpt( CMDLINE_ARG_JSON_CONFIG_FILE )
			.hasArg()
        	.argName( "file" )
			.required( false )
		    .desc( String.format( "Set the location of the JSON configuration file for the Federation " +
		    					  "Manager to use. Values specified in this file will be overridden by " +
		    					  "any corresponding command line argument values provided.") )
			.type( PatternOptionBuilder.STRING_VALUE )
		    .build();

		this.cmdLineOptions = new Options();

		cmdLineOptions.addOption( help );
		cmdLineOptions.addOption( configLocation );
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

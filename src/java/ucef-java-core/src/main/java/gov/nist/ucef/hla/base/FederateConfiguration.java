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
package gov.nist.ucef.hla.base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.InteractionParameter;
import gov.nist.ucef.hla.base.Types.ObjectAttribute;
import gov.nist.ucef.hla.base.Types.ObjectClass;

/**
 * The purpose of this class is to encapsulate all data required to configure a federate.
 *
 * Most "setter" methods return the FederateConfiguratio instance to support method chaining.
 *
 * The main usage pattern is something like:
 *
 * 		FederateConfiguration config = new FederateConfiguration( "TheUnitedFederationOfPlanets",
 * 		                                                          "FederateName",
 * 		                                                          "TestFederate" );
 * 		config.setLookAhead(0.5)
 * 			.cacheInteractionClasses(
 *              InteractionClass.Pub( PING_INTERACTION_ID ),
 *              InteractionClass.Sub( PONG_INTERACTION_ID )
 *          );
 */
public class FederateConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getLogger( FederateBase.class );

	// defaults for configuration values
	private static final boolean DEFAULT_SHOULD_CREATE_FEDERATION = false;
	private static final int DEFAULT_MAX_JOIN_ATTEMPTS            = 5;
	private static final long DEFAULT_JOIN_RETRY_INTERVAL_SEC     = 5;
	private static final boolean DEFAULT_SYNC_BEFORE_RESIGN       = false;
	private static final boolean DEFAULT_ARE_CALLBACKS_IMMEDIATE  = true;
	private static final double DEFAULT_LOOK_AHEAD                = 1.0;
	private static final double DEFAULT_STEP_SIZE                 = 0.1;
	private static final boolean DEFAULT_IS_TIME_CONSTRAINED      = true;
	private static final boolean DEFAULT_IS_TIME_REGULATED        = true;

	// keys for locating values in JSON based configuration data
	private static final String JSON_CONFIG_KEY_FEDERATE_NAME           = "federateName";
	private static final String JSON_CONFIG_KEY_AUTO_UNIQUE_NAME        = "autoUniqueName";
	private static final String JSON_CONFIG_KEY_FEDERATE_TYPE           = "federateType";
	private static final String JSON_CONFIG_KEY_FEDERATION_EXEC_NAME    = "federationExecName";
	private static final String JSON_CONFIG_KEY_CAN_CREATE_FEDERATION   = "canCreateFederation";
	private static final String JSON_CONFIG_KEY_STEP_SIZE               = "stepSize";
	private static final String JSON_CONFIG_KEY_MAX_JOIN_ATTEMPTS       = "maxJoinAttempts";
	private static final String JSON_CONFIG_KEY_JOIN_RETRY_INTERVAL_SEC = "joinRetryIntervalSec";
	private static final String JSON_CONFIG_KEY_SYNC_BEFORE_RESIGN      = "syncBeforeResign";
	private static final String JSON_CONFIG_KEY_CALLBACKS_ARE_IMMEDIATE = "callbacksAreImmediate";
	private static final String JSON_CONFIG_KEY_LOOK_AHEAD              = "lookAhead";
	private static final String JSON_CONFIG_KEY_TIME_CONSTRAINED        = "timeConstrained";
	private static final String JSON_CONFIG_KEY_TIME_REGULATED          = "timeRegulated";
	private static final String JSON_CONFIG_KEY_BASE_FOM_PATHS          = "baseFomPaths";
	private static final String JSON_CONFIG_KEY_JOIN_FOM_PATHS          = "joinFomPaths";
	private static final String JSON_CONFIG_KEY_SOM_PATH                = "somPath";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationExecName;
	private String federateName;
	private boolean autoUniqueName;
	private String federateType;
	private Set<URL> modules;
	private Set<URL> joinModules;

	private Collection<String> baseFoms;
	private Collection<String> joinFoms;
	private String somPath;

	private Map<String,Types.InteractionClass> interactionsByName;
	private Map<String,Types.ObjectClass> objectClassesByName;

	private boolean canCreateFederation;
	private int maxJoinAttempts;
	private long joinRetryIntervalSec;

	private boolean syncBeforeResign;

	private boolean isTimeStepped;
	private boolean callbacksAreImmediate;
	private double lookAhead;
	private double stepSize;
	private boolean isTimeConstrained;
	private boolean isTimeRegulated;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor
	 */
	public FederateConfiguration()
	{
		this( "UnnamedFederation",
		      "Federate_" + UUID.randomUUID(),
		      "FederateType_" + UUID.randomUUID() );
	}

	/**
	 * Constructor - the federation name, federate name and federation types are supplied, and all
	 * other properties are left as defaults and or empty.
	 * @param federateName
	 * @param federateType
	 * @param federationName
	 */
	public FederateConfiguration( String federateName, String federateType, String federationName )
	{
		this.federateName = federateName;
		this.federateType = federateType;
		this.federationExecName = federationName;

		this.autoUniqueName = false;

		this.modules = new HashSet<>();
		this.joinModules = new HashSet<>();

		this.baseFoms = new HashSet<>();
		this.joinFoms = new HashSet<>();
		this.somPath = "";

		this.objectClassesByName = new HashMap<>();
		this.interactionsByName = new HashMap<>();

		this.canCreateFederation = DEFAULT_SHOULD_CREATE_FEDERATION;
		this.maxJoinAttempts = DEFAULT_MAX_JOIN_ATTEMPTS;
		this.joinRetryIntervalSec = DEFAULT_JOIN_RETRY_INTERVAL_SEC;

		this.syncBeforeResign = DEFAULT_SYNC_BEFORE_RESIGN;

		this.callbacksAreImmediate = DEFAULT_ARE_CALLBACKS_IMMEDIATE;
		this.lookAhead = DEFAULT_LOOK_AHEAD;
		this.stepSize = DEFAULT_STEP_SIZE;

		this.isTimeConstrained = DEFAULT_IS_TIME_CONSTRAINED;
		this.isTimeRegulated = DEFAULT_IS_TIME_REGULATED;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Provide configuration details from a {@link String} containing JSON structured data
	 *
	 * Only recognized configuration items in the JSON structure will be processed; any other
	 * items will be ignored.
	 *
	 * Configuration items not mentioned in the JSON structure will not have their values changed
	 * (i.e., they will be left in their existing state).
	 *
	 * Refer to {@link #fromJSON(JSONObject)} for recognized configuration JSON keys and data
	 * types.
	 *
	 * @param configSource the {@link String} containing either JSON configuration data, or the
	 *            path to a resource (i.e., a file) containing JSON configuration data.
	 * @return the extracted {@link JSONObject} containing the extracted configuration data. This
	 *         can be used for handling of "extra", federate specific custom configuration
	 *         parameters contained in the JSON.
	 */
	public JSONObject fromJSON( String configSource )
	{
		// see if the configuration source is a file
		File configFile = getResourceFile( configSource );
		boolean isFile = configFile != null;

		// assume for the moment that the JSON is coming directly from the
		// configuration source parameter
		String json = configSource;
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
		try
		{
			return fromJSON( (JSONObject)parsedString );
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
	 * Provide configuration details from a {@link String} containing JSON structured data
	 *
	 * Only recognized configuration items in the JSON structure will be processed; any other
	 * items will be ignored.
	 *
	 * Configuration items not mentioned in the JSON structure will not have their values changed
	 * (i.e., they will be left in their existing state).
	 *
	 * Currently recognized configuration items are:
	 *
	 * {
	 *     "federateName":          STRING,
	 *     "autoUniqueName":        BOOL,
	 *     "federateType":          STRING,
	 *     "federationExecName":    STRING,
	 *     "canCreateFederation":   BOOL,
	 *     "maxJoinAttempts":       INT,
	 *     "joinRetryIntervalSec":  INT,
	 *     "syncBeforeResign":      BOOL,
	 *     "callbacksAreImmediate": BOOL,
	 *     "lookAhead":             DOUBLE,
	 *     "stepSize":              DOUBLE
	 *     "timeConstrained":       BOOL,
	 *     "timeRegulated":         BOOL,
	 *     "baseFomPaths":          ARRAY[STRING...],
	 *     "joinFomPaths":          ARRAY[STRING...],
	 *     "somPath":               STRING
	 * }
	 *
	 * @param configData the {@link JSONObject} containing configuration data
	 * @return the original {@link JSONObject}, so that it can be used for handling of "extra",
	 *         federate specific custom configuration parameters contained in the JSON.
	 */
	public JSONObject fromJSON( JSONObject configData )
	{
		if( configData == null )
		{
			logger.warn( "JSON configuration data was null!" );
			return configData;
		}

		if( configData.isEmpty() )
		{
			logger.warn( "JSON configuration data was empty - defaults will be used." );
			return configData;
		}

		if(logger.isDebugEnabled())
		{
			// for the purposes of debugging problems, show logging for
			// any unrecognized configuration items found so that problems
			// can be resolved quickly (such as typos in the
			// config JSON keys etc)
			Set<String> recognizedConfigurationKeys = new HashSet<>();
			recognizedConfigurationKeys.addAll( Arrays.asList(new String[]
				{
	                JSON_CONFIG_KEY_FEDERATE_NAME,
	                JSON_CONFIG_KEY_AUTO_UNIQUE_NAME,
	                JSON_CONFIG_KEY_FEDERATE_TYPE,
	                JSON_CONFIG_KEY_FEDERATION_EXEC_NAME,
	                JSON_CONFIG_KEY_CAN_CREATE_FEDERATION,
	                JSON_CONFIG_KEY_MAX_JOIN_ATTEMPTS,
	                JSON_CONFIG_KEY_JOIN_RETRY_INTERVAL_SEC,
	                JSON_CONFIG_KEY_SYNC_BEFORE_RESIGN,
	                JSON_CONFIG_KEY_CALLBACKS_ARE_IMMEDIATE,
	                JSON_CONFIG_KEY_LOOK_AHEAD,
	                JSON_CONFIG_KEY_STEP_SIZE,
	                JSON_CONFIG_KEY_TIME_CONSTRAINED,
	                JSON_CONFIG_KEY_TIME_REGULATED,
	                JSON_CONFIG_KEY_BASE_FOM_PATHS,
	                JSON_CONFIG_KEY_JOIN_FOM_PATHS,
	                JSON_CONFIG_KEY_SOM_PATH
	            }
			));
			for(Object key : configData.keySet())
			{
				if(!recognizedConfigurationKeys.contains( key ))
				{
					Object value = configData.get( key );
					logger.debug( String.format( "Configuration item '%s' with " +
					                             "value '%s' in JSON configuration data " +
					                             "is not recognized and will be ignored.",
					                             key.toString(), value.toString() ) );
				}
			}
		}

		// now we can process the configuration data from the JSONObject
		try
		{
			// process configuration - note that in *all* cases we try to look
			// up the value from the JSON and fall back to the existing value
			// if there is no value available
			this.autoUniqueName = jsonBooleanOrDefault( configData,
			                                            JSON_CONFIG_KEY_AUTO_UNIQUE_NAME,
			                                            this.autoUniqueName );
			if( this.autoUniqueName )
			{
				// if automatic unique naming is active, we use a
				// type 4 UUID string to ensure that the federate
				// name is universally unique
				String uniquifier = UUID.randomUUID().toString();
				String tempFederateName = jsonStringOrDefault( configData,
						                                       JSON_CONFIG_KEY_FEDERATE_NAME,
						                                       null );
				if( tempFederateName == null )
					this.federateName = "Federate_" + uniquifier;
				else
					this.federateName = tempFederateName + "_" + uniquifier;

				logger.debug( String.format( "Generated unique federate name '%s'",
				                             this.federateName ));
			}
			else
			{
				this.federateName = jsonStringOrDefault( configData,
				                                         JSON_CONFIG_KEY_FEDERATE_NAME,
				                                         this.federateName );
			}
			this.federateType = jsonStringOrDefault( configData,
			                                         JSON_CONFIG_KEY_FEDERATE_TYPE,
			                                         this.federateType );
			this.federationExecName = jsonStringOrDefault( configData,
			                                               JSON_CONFIG_KEY_FEDERATION_EXEC_NAME,
			                                               this.federationExecName );
			this.canCreateFederation = jsonBooleanOrDefault( configData,
			                                                 JSON_CONFIG_KEY_CAN_CREATE_FEDERATION,
			                                                 this.canCreateFederation );
			this.maxJoinAttempts = jsonIntOrDefault( configData,
			                                         JSON_CONFIG_KEY_MAX_JOIN_ATTEMPTS,
			                                         this.maxJoinAttempts );
			this.joinRetryIntervalSec = jsonLongOrDefault( configData,
			                                               JSON_CONFIG_KEY_JOIN_RETRY_INTERVAL_SEC,
			                                               this.joinRetryIntervalSec );
			this.syncBeforeResign = jsonBooleanOrDefault( configData,
			                                              JSON_CONFIG_KEY_SYNC_BEFORE_RESIGN,
			                                              this.syncBeforeResign );
			this.callbacksAreImmediate = jsonBooleanOrDefault( configData,
			                                                   JSON_CONFIG_KEY_CALLBACKS_ARE_IMMEDIATE,
			                                                   this.callbacksAreImmediate );
			this.isTimeConstrained = jsonBooleanOrDefault( configData,
			                                               JSON_CONFIG_KEY_TIME_CONSTRAINED,
			                                               this.isTimeConstrained );
			this.isTimeRegulated = jsonBooleanOrDefault( configData,
			                                             JSON_CONFIG_KEY_TIME_REGULATED,
			                                             this.isTimeRegulated );
			this.lookAhead = jsonDoubleOrDefault( configData,
			                                      JSON_CONFIG_KEY_LOOK_AHEAD,
			                                      this.lookAhead );
			this.stepSize = jsonDoubleOrDefault( configData,
			                                     JSON_CONFIG_KEY_STEP_SIZE,
			                                     this.stepSize );
			Set<String> extractedBaseFomPaths = jsonStringSetOrDefault( configData,
			                                                            JSON_CONFIG_KEY_BASE_FOM_PATHS,
			                                                            Collections.emptySet() );
			if( extractedBaseFomPaths.size() > 0 )
			{
				this.baseFoms = extractedBaseFomPaths;
				this.modules.clear();
				addModules( urlsFromPaths( this.baseFoms ) );
			}
			Set<String> extractedJoinFomPaths = jsonStringSetOrDefault( configData,
			                                                        JSON_CONFIG_KEY_JOIN_FOM_PATHS,
			                                                        Collections.emptySet() );
			if( extractedJoinFomPaths.size() > 0 )
			{
				this.joinFoms = extractedJoinFomPaths;
				this.joinModules.clear();
				addJoinModules( urlsFromPaths( this.joinFoms ) );
			}

			String extractedSomPath = jsonStringOrDefault( configData, JSON_CONFIG_KEY_SOM_PATH, "" );
			if( extractedSomPath.length() > 0 )
			{
				this.somPath = extractedSomPath;
				this.interactionsByName.clear();
				this.objectClassesByName.clear();
				SOMParser.somToFederateConfig( this.somPath, this );
			}
		}
		catch( Exception e )
		{
			String msg = "There was a problem processing the configuration JSON data.";
			throw new UCEFException( e, msg );
		}

		return configData;
	}

	/**
	 * Creates a human readable summary of the current configuration state.
	 *
	 * This is principally for debugging purposes, but could have wider applications.
	 *
	 * @return a human readable summary of the current configuration state.
	 */
	public String summary()
	{
		String dashRule = "------------------------------------------------------------\n";
		String dotRule = "............................................................\n";

		StringBuilder builder = new StringBuilder();

		builder.append( dashRule );
		builder.append( "Federate Name              : " + this.federateName + "\n" );
		builder.append( "Federate Type              : " + this.federateType + "\n" );
		builder.append( "Federation Name            : " + this.federationExecName + "\n" );

		builder.append( dotRule );
		builder.append( "Create Federation?         : " + (this.canCreateFederation?"Yes":"No") + "\n" );
		builder.append( "Maximum Recconect Attempts : " + this.maxJoinAttempts + "\n" );
		builder.append( "Reconnect Wait Time        : " + this.joinRetryIntervalSec + " seconds\n" );
		builder.append( "Sync before resigning?     : " + (this.syncBeforeResign?"Yes":"No") + "\n" );
		builder.append( "Time Stepped?              : " + (this.isTimeStepped?"Yes":"No") + "\n" );
		builder.append( "Are Callbacks Immediate?   : " + (this.callbacksAreImmediate?"Yes":"No") + "\n" );
		builder.append( "Look Ahead                 : " + this.lookAhead + "\n" );
		builder.append( "Step Size                  : " + this.stepSize + "\n" );

		builder.append( dotRule );
		builder.append( "Published Attributes:\n" );
		Collection<Types.ObjectClass> attributes = getPublishedObjectClasses();
		if(attributes.isEmpty())
		{
			builder.append( "\t...none...\n" );
		}
		else
		{
			List<Types.ObjectClass> pubAttrList = attributes.stream().collect( Collectors.toList() );
			pubAttrList.sort( ( a, b ) -> a.name.compareTo( b.name ) );
			for( Types.ObjectClass objectClass : pubAttrList )
			{
				builder.append( "\t" + objectClass.name + "\n" );
				List<String> attributeNames = objectClass.attributes.values()
    				.stream()
    				.map( x -> x.name + " (" + x.dataType.toString() + ")" )
    				.collect( Collectors.toList() );
				attributeNames.sort( null );
				attributeNames.forEach( ( x ) -> builder.append( "\t\t" + x + "\n" ) );
			}
		}

		builder.append( dotRule );
		builder.append( "Subscribed Attributes:\n" );
		attributes = getSubscribedObjectClasses();
		if(attributes.isEmpty())
		{
			builder.append( "\t...none...\n" );
		}
		else
		{
			List<Types.ObjectClass> subAttrList = attributes.stream().collect( Collectors.toList() );
			subAttrList.sort( ( a, b ) -> a.name.compareTo( b.name ) );
			for( Types.ObjectClass objectClass : subAttrList )
			{
				builder.append( "\t" + objectClass.name + "\n" );
				List<String> attributeNames = objectClass.attributes.values()
    				.stream()
    				.map( x -> x.name + " (" + x.dataType.toString() + ")" )
    				.collect( Collectors.toList() );
				attributeNames.sort( null );
				attributeNames.forEach( ( x ) -> builder.append( "\t\t" + x + "\n" ) );
			}
		}

		builder.append( dotRule );
		builder.append( "Published Interactions:\n" );
		Collection<Types.InteractionClass> interactions = getPublishedInteractions();
		if(interactions.isEmpty())
		{
			builder.append( "\t...none...\n" );
		}
		else
		{
			List<Types.InteractionClass> pubInteractionList = interactions.stream().collect( Collectors.toList() );
			pubInteractionList.sort( ( a, b ) -> a.name.compareTo( b.name ) );
			pubInteractionList.forEach( ( x ) -> builder.append( "\t" + x.name + "\n" ) );
		}

		builder.append( dotRule );
		builder.append( "Subscribed Interactions:\n" );
		interactions = getSubscribedInteractions();
		if(interactions.isEmpty())
		{
			builder.append( "\t...none...\n" );
		}
		else
		{
			List<Types.InteractionClass> subInteractionList = interactions.stream().collect( Collectors.toList() );
			subInteractionList.sort( ( a, b ) -> a.name.compareTo( b.name ) );
			subInteractionList.forEach( ( x ) -> builder.append( "\t" + x.name + "\n" ) );
		}

		builder.append( dashRule );

		return builder.toString();
	}

	/**
	 * Obtain a {@link File} resource instance based on a path. The resource is looked for on the
	 * file system and as a resource (as in a packaged JAR)
	 *
	 * @param path the path for the resource
	 * @return the resource as a {@link File} instance, or null if no such resource could be
	 *         located
	 */
	public File getResourceFile( String path )
	{
		File file = new File( path );
		if( file.exists() && file.isFile() )
			return file;

		URL fileUrl = this.getClass().getClassLoader().getResource( path );
		if( fileUrl != null )
			return new File( fileUrl.getFile() );

		return null;
	}

	/**
	 * Obtain a {@link URL} resource instance based on a path. The resource is looked for on the
	 * file system and as a resource (as in a packaged JAR)
	 *
	 * @param path the path for the resource
	 * @return the resource as a {@link URL} instance, or null if no such resource could be
	 *         located
	 */
	public URL getResourceURL( String path )
	{
		URL fileUrl = this.getClass().getClassLoader().getResource( path );
		if( fileUrl != null )
			return fileUrl;

		File file = new File( path );
		if( file.exists() && file.isFile() )
		{
			try
			{
				return file.toURI().toURL();
			}
			catch( MalformedURLException e )
			{
				// Note that this can't really happen if the file exists,
				// which we have checked for already. In any case, we'll
				// ignore this error and just treat it as a file not
				// found type situation
			}
		}

		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Set the federation execution name
	 *
	 * @param federationExecName the federation execution name
	 */
	public FederateConfiguration setFederationName( String federationExecName )
	{
		this.federationExecName = federationExecName;
		return this;
	}

	/**
	 * Obtain the configured federation name
	 *
	 * @return the configured federation name (not modifiable)
	 */
	public String getFederationName()
	{
		return federationExecName;
	}


	/**
	 * Set the federate name
	 *
	 * @param federateName the federate name
	 */
	public FederateConfiguration setFederateName( String federateName )
	{
		this.federateName = federateName;
		return this;
	}

	/**
	 * Obtain the configured federate name
	 *
	 * @return the configured federate name (not modifiable)
	 */
	public String getFederateName()
	{
		return federateName;
	}

	/**
	 * Set the federate type
	 *
	 * @param federateType the federate type
	 */
	public FederateConfiguration setFederateType( String federateType )
	{
		this.federateType = federateType;
		return this;
	}

	/**
	 * Obtain the configured federate type
	 *
	 * @return the configured federate type (not modifiable)
	 */
	public String getFederateType()
	{
		return federateType;
	}

	/**
	 * Configure whether the federate is able to create a federation if the federation is absent
	 * on startup
	 *
	 * @param canCreateFederation if true, the federate can attempt to create the required
	 *            federation on startup, otherwise it is not allowed to create any new federations
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setCanCreateFederation( boolean canCreateFederation )
	{
		this.canCreateFederation = canCreateFederation;
		return this;
	}

	/**
	 * Determine whether the federate should be able to create a required federation if it is
	 * absent
	 *
	 * @return true if the federate should be able to create federations, false otherwise
	 */
	public boolean canCreateFederation()
	{
		return canCreateFederation;
	}

	/**
	 * Configure the federate's maximum number of attempts to join a federation
	 *
	 * @param maxJoinAttempts the maximum number of attempts to join a federation
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setMaxJoinAttempts( int maxJoinAttempts )
	{
		this.maxJoinAttempts = maxJoinAttempts;
		return this;
	}

	/**
	 * Obtain the maximum number of attempts to join a federation
	 *
	 * @return the maximum number of attempts to join a federation
	 */
	public int getMaxJoinAttempts()
	{
		return maxJoinAttempts;
	}

	/**
	 * Configure the interval, in seconds, between attempts to join a federation
	 *
	 * @param retryIntervalSec the interval, in seconds, between attempts to join a federation
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setJoinRetryInterval( long retryIntervalSec )
	{
		this.joinRetryIntervalSec = retryIntervalSec;
		return this;
	}

	/**
	 * Obtain the interval, in seconds, between attempts to join a federation
	 *
	 * @return the interval, in seconds, between attempts to join a federation
	 */
	public long getJoinRetryInterval()
	{
		return joinRetryIntervalSec;
	}

	/**
	 * Configure whether the federate should synchronize before resigning from the federation
	 *
	 * @param syncBeforeResign if true, synchronize before resigning from the federation, otherwise
	 *        it's OK to exit without synchronizing
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setSyncBeforeResign( boolean syncBeforeResign )
	{
		this.syncBeforeResign = syncBeforeResign;
		return this;
	}

	/**
	 * Determine whether the federate should synchronize before resigning from the federation
	 *
	 * @return true if the federate must synchronize before resigning from the federation,
	 *         otherwise it's OK to exit without synchronizing
	 */
	public boolean shouldSyncBeforeResign()
	{
		return syncBeforeResign;
	}

	/**
	 * Configure the federate's lookahead
	 *
	 * @param lookAhead the lookahead
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setLookAhead( double lookAhead )
	{
		this.lookAhead = lookAhead;
		return this;
	}
	/**
	 * Obtain the lookahead
	 *
	 * @return the lookahead
	 */
	public double getLookAhead()
	{
		return lookAhead;
	}

	/**
	 * Configure the federate's step size
	 *
	 * @param stepSize the step size
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setStepSize( double stepSize )
	{
		this.stepSize = stepSize;
		return this;
	}

	/**
	 * Obtain the step size
	 *
	 * @return the step size
	 */
	public double getStepSize()
	{
		return stepSize;
	}

	/**
	 * Configure the federate's time constraint policy
	 *
	 * @param isTimeRegulated true if time constraint is enabled, false otherwise
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setTimeConstrained( boolean isTimeConstrained )
	{
		this.isTimeConstrained = isTimeConstrained;
		return this;
	}

	/**
	 * Obtain the federate's time constraint policy
	 *
	 * @return the federate's time constraint policy
	 */
	public boolean isTimeConstrained()
	{
		return this.isTimeConstrained;
	}

	/**
	 * Configure the federate's time regulation policy
	 *
	 * @param isTimeRegulated true if time reglulation is enabled, false otherwise
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setTimeRegulated( boolean isTimeRegulated )
	{
		this.isTimeRegulated = isTimeRegulated;
		return this;
	}

	/**
	 * Obtain the federate's time regulation policy
	 *
	 * @return the federate's time regulation policy
	 */
	public boolean isTimeRegulated()
	{
		return this.isTimeRegulated;
	}

	/**
	 * Configure whether the federate is configured to use immediate or evoked callbacks
	 *
	 * @param callbacksAreEvoked true if the federate is configured to use immediate callbacks, false otherwise
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration setCallbacksAreImmediate( boolean callbacksAreImmediate )
	{
		this.callbacksAreImmediate = callbacksAreImmediate;
		return this;
	}

	/**
	 * Determine if the federate is configured to use immediate or evoked callbacks
	 *
	 * @return true if the federate is configured to use immediate callbacks
	 */
	public boolean callbacksAreImmediate()
	{
		return callbacksAreImmediate;
	}

	/**
	 * Add a FOM module to the configuration
	 *
	 * @param module the FOM module to add to the configuration
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration addModule( URL module )
	{
		return addModules( new URL[]{ module } );
	}

	/**
	 * Add FOM modules to the configuration
	 *
	 * @param modules the FOM modules to add to the configuration
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration addModules( URL[] modules )
	{
		return addModules( asCollection( modules ) );
	}

	/**
	 * Add FOM modules to the configuration
	 *
	 * @param modules the FOM modules to add to the configuration
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration addModules( Collection<URL> modules )
	{
		if( notNullOrEmpty( modules ) )
		{
			this.modules.addAll( collectNonEmptyURLs( modules ) );
		}
		return this;
	}

	/**
	 * Obtain the configured FOM modules
	 *
	 * @return the configured FOM modules (not modifiable)
	 */
	public Collection<URL> getModules()
	{
		return Collections.unmodifiableSet( modules );
	}

	/**
	 * Add a join FOM module to the configuration
	 *
	 * @param joinModule the join FOM module to add to the configuration
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration addJoinModule( URL joinModule )
	{
		return addJoinModules( new URL[]{ joinModule } );
	}

	/**
	 * Add join FOM modules to the configuration
	 *
	 * @param joinModules the join FOM modules to add to the configuration
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration addJoinModules( URL[] joinModules )
	{
		return addJoinModules( asCollection( joinModules ) );
	}

	/**
	 * Add join FOM modules to the configuration
	 *
	 * @param joinModules the join FOM modules to add to the configuration
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration addJoinModules( Collection<URL> joinModules )
	{
		if( notNullOrEmpty( joinModules ) )
		{
			this.joinModules.addAll( collectNonEmptyURLs( joinModules ) );
		}
		return this;
	}

	/**
	 * Obtain the configured join FOM modules
	 *
	 * @return the configured join FOM modules (not modifiable)
	 */
	public Collection<URL> getJoinModules()
	{
		return Collections.unmodifiableSet( joinModules );
	}

	public Collection<String> getFomPaths()
	{
		return this.baseFoms;
	}

	public FederateConfiguration addFomPath( String path )
	{
		this.baseFoms.add(path);
		return this;
	}

	public FederateConfiguration clearFomPaths()
	{
		this.baseFoms.clear();
		return this;
	}

	public Set<String> getSomPaths()
	{
		// this is to support multiple SOM usage without breaking the interface
		Set<String> soms = new HashSet<>();
		soms.add( this.somPath );
		return soms;
	}

	public FederateConfiguration addSomPath( String path )
	{
		this.somPath = path;

		this.interactionsByName.clear();
		this.objectClassesByName.clear();
		SOMParser.somToFederateConfig( this.somPath, this );

		return this;
	}

	/**
	 * Add one or more object classes that may be published or subscribed (or both or neither!) by
	 * this federate
	 *
	 * @param objectClasses the {@link ObjectClass} to add
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration cacheObjectClasses( ObjectClass ... objectClasses )
	{
		return cacheObjectClasses( asCollection( objectClasses ) );
	}

	/**
	 * Add one or more object classes that may be published or subscribed (or both or neither!) by
	 * this federate
	 *
	 * @param objectClasses the {@link ObjectClass} to add
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration cacheObjectClasses( Collection<ObjectClass> objectClasses )
	{
		if( notNullOrEmpty( objectClasses ) )
		{
			for(ObjectClass objectClass : objectClasses)
			{
				this.objectClassesByName.put( objectClass.name, objectClass );
			}
		}
		return this;
	}

	/**
	 * Add one or more interaction classes that may be published or subscribed (or both or neither!) by
	 * this federate
	 *
	 * @param interactionClasses the {@link InteractionClass} to add
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration cacheInteractionClasses( InteractionClass ... interactionClasses )
	{
		return cacheInteractionClasses( asCollection( interactionClasses ) );
	}

	/**
	 * Add one or more interaction classes that may be published or subscribed (or both or neither!) by
	 * this federate
	 *
	 * @param interactionClasses the {@link InteractionClass} to add
	 * @return this instance (for method chaining)
	 */
	public FederateConfiguration cacheInteractionClasses( Collection<InteractionClass> interactionClasses )
	{
		if( notNullOrEmpty( interactionClasses ) )
		{
			for(InteractionClass interactionClass : interactionClasses )
			{
				this.interactionsByName.put( interactionClass.name, interactionClass);
			}
		}
		return this;
	}

	/**
	 * Obtain the interactions which are both published *and* subscribed by this federate
	 *
	 * @return the interactions which are both published *and* subscribed (not modifiable)
	 */
	public Collection<Types.InteractionClass> getPublishedAndSubscribedInteractions()
	{
		return Collections.unmodifiableCollection( interactionsByName.values() );
	}

	/**
	 * Obtain the interactions which are published by this federate.
	 *
	 * @return the published interactions (not modifiable)
	 */
	public Collection<Types.InteractionClass> getPublishedInteractions()
	{
		return interactionsByName.values()
			.stream()
			.filter( x -> x.isPublished() )
			.collect( Collectors.toList() );
	}

	/**
	 * Obtain the interactions which are subscribed to by this federate.
	 *
	 * @return the subscribed interactions (not modifiable)
	 */
	public Collection<Types.InteractionClass> getSubscribedInteractions()
	{
		return interactionsByName.values()
			.stream()
			.filter( x -> x.isSubscribed() )
			.collect( Collectors.toList() );
	}

	/**
	 * Obtain the object classes which are both published *and* subscribed by this federate.
	 *
	 * @return the object classes which are both published *and* subscribed (not modifiable)
	 */
	public Collection<Types.ObjectClass> getPublishedAndSubscribedObjectClasses()
	{
		return Collections.unmodifiableCollection( objectClassesByName.values() );
	}

	/**
	 * Obtain the object classes which are published by this federate.
	 *
	 * @return Obtain the object classes which are published (not modifiable)
	 */
	public Collection<Types.ObjectClass> getPublishedObjectClasses()
	{
		return objectClassesByName.values()
			.stream()
			.filter( x -> x.isPublished() )
			.collect( Collectors.toList() );
	}

	/**
	 * Obtain the object classes which are subscribed to by this federate.
	 *
	 * @return the object classes which are subscribed (not modifiable)
	 */
	public Collection<Types.ObjectClass> getSubscribedObjectClasses()
	{
		return objectClassesByName.values()
			.stream()
			.filter( x -> x.isSubscribed() )
			.collect( Collectors.toList() );
	}

	/**
	 * Returns the fully qualified names of the interaction classes which are published by this
	 * federate.
	 *
	 * @return the names of the published interaction classes (not modifiable)
	 */
	public Set<String> getPublishedInteractionNames()
	{
		return getPublishedInteractions()
			.stream()
			.map(x -> x.name)
			.collect(Collectors.toSet());
	}

	/**
	 * Returns the fully qualified names of the interaction classes which are subscribed to by
	 * this federate.
	 *
	 * @return the names of the subscribed interaction classes (not modifiable)
	 */
	public Set<String> getSubscribedInteractionNames()
	{
		return getSubscribedInteractions()
			.stream()
			.map(x -> x.name)
			.collect(Collectors.toSet());
	}

	/**
	 * Obtain the names of the published attributes of a given object class.
	 *
	 * If no object class can be resolved from the given fully qualified object class name, an
	 * empty {@link Set} is returned.
	 *
	 * @param className the fully qualified name of an object class
	 * @return published attributes of the given object class (not modifiable)
	 */
	public Set<String> getPublishedAttributeNames( String className )
	{
		ObjectClass objectClass = this.objectClassesByName.get( className );

		if( objectClass == null )
			return Collections.emptySet();

		return objectClass.attributes.values()
			.stream()
			.filter(attr -> attr.isPublished())
			.map(attr -> attr.name)
			.collect(Collectors.toSet());
	}

	/**
	 * Obtain the names of the subscribed attributes of a given object class.
	 *
	 * If no object class can be resolved from the given fully qualified object class name, an
	 * empty {@link Set} is returned.
	 *
	 * @param className the fully qualified name of an object class
	 * @return subscribed attributes of the given object class (not modifiable)
	 */
	public Set<String> getSubscribedAttributeNames( String className )
	{
		ObjectClass objectClass = this.objectClassesByName.get( className );

		if( objectClass == null )
			return Collections.emptySet();

		return objectClass.attributes.values()
			.stream()
			.filter(attr -> attr.isSubscribed())
			.map(attr -> attr.name)
			.collect(Collectors.toSet());
	}

	/**
	 * Obtain the names of the parameters of a given interaction class.
	 *
	 * If no interaction class can be resolved from the given fully qualified interaction class
	 * name, an empty {@link Set} is returned.
	 *
	 * @param interactionName the fully qualified name of a interaction class
	 * @return parameter names of the given interaction class (not modifiable)
	 */
	public Set<String> getParameterNames( String interactionName )
	{
		InteractionClass interactionClass = this.interactionsByName.get( interactionName );

		if( interactionClass == null )
			return Collections.emptySet();

		return interactionClass.parameters.keySet();
	}

	/**
	 * Obtain the fully qualified names of all object classes published by this federate.
	 *
	 * @return the fully qualified names of the published object classes (not modifiable)
	 */
	public Set<String> getPublishedClassNames()
	{
		return getPublishedObjectClasses()
			.stream()
			.map(x -> x.name)
			.collect(Collectors.toSet());
	}

	/**
	 * Obtain the fully qualified names of all object classes subscribed to by this federate.
	 *
	 * @return the fully qualified names of the subscribed object classes (not modifiable)
	 */
	public Set<String> getSubscribedClassNames()
	{
		return getSubscribedObjectClasses()
			.stream()
			.map(x -> x.name)
			.collect(Collectors.toSet());
	}

	/**
	 * Returns the data type of the an object class attribute or interaction parameter given the
	 * fully qualified name and member name
	 *
	 * If the data type cannot be resolved, {@link DataType#UNKNOWN} will be returned.
	 *
	 * NOTE: The fully qualified name of an interaction or object class includes the
	 * `HLAobjectRoot` or `HLAinteractionRoot` namespace. This means is not possible for names of
	 * interactions and object classes to collide (even if the "local" name for an interaction and
	 * object class were to coincide). For this reason, it is not necessary to specify whether the
	 * fully qualified class name refers to an interaction or object class, since it will only
	 * ever match *either* an interaction *or* an object class.
	 *
	 * @param className the fully qualified name of an object or an interaction class
	 * @param memberName the name of an attribute or parameter on the object class or interaction
	 *            referenced by the fully qualified class name
	 * @return the data type of attribute or parameter matching the provided name for of the given
	 *         object/interaction class
	 */
	public DataType getDataType( String className, String memberName )
	{
		// start by looking for an interaction matching the class name
		InteractionClass interactionClass = this.interactionsByName.get( className );
		if( interactionClass != null )
		{
			// found - determine the parameter type and return
			InteractionParameter parameter = interactionClass.parameters.get( memberName );
			return parameter == null ? DataType.UNKNOWN : parameter.dataType;
		}

		// no interaction match - try to match on known object classes...
		ObjectClass objectClass = this.objectClassesByName.get( className );
		if( objectClass != null )
		{
			// found - determine the attribute type and return
			ObjectAttribute attribute = objectClass.attributes.get( memberName );
			return attribute == null ? DataType.UNKNOWN : attribute.dataType;
		}

		// no interaction or object class found matching the provided
		// fully qualified class name
		return DataType.UNKNOWN;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Utility Methods /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to turn an array of {@link InteractionClass}s into a {@link Collection} of
	 * {@link InteractionClass}s
	 *
	 * @param values the array of {@link InteractionClass}s
	 * @return a {@link Collection} of {@link InteractionClass}s
	 */
	private Collection<InteractionClass> asCollection( InteractionClass[] values )
	{
		return values == null ? Collections.emptyList() : Arrays.asList( values );
	}

	/**
	 * Utility method to turn an array of {@link ObjectClass}s into a {@link Collection} of
	 * {@link ObjectClass}s
	 *
	 * @param values the array of {@link ObjectClass}s
	 * @return a {@link Collection} of {@link ObjectClass}s
	 */
	private Collection<ObjectClass> asCollection( ObjectClass[] values )
	{
		return values == null ? Collections.emptyList() : Arrays.asList( values );
	}

	/**
	 * Utility method to turn an array of {@link URL}s into a {@link Collection} of {@link URL}s
	 *
	 * @param values the array of {@link URL}s
	 * @return a {@link Collection} of {@link URL}s
	 */
	private Collection<URL> asCollection( URL[] values )
	{
		return values == null ? Collections.emptyList() : Arrays.asList( values );
	}

	/**
	 * Utility method to collect all the non null/non empty {@link String}s in a collection and
	 * return them as a new {@link Collection}
	 *
	 * @param values the values
	 * @return the {@link Collection} of non-null/non-empty values
	 */
	public Collection<String> collectNonEmptyStrings( Collection<String> values )
	{
		return values.stream().filter( ( str ) -> notNullOrEmpty( str ) ).collect( Collectors.toList() );
	}

	/**
	 * Utility method to collect all the non null/non empty {@link URL}s in a collection and
	 * return them as a new {@link Collection}
	 *
	 * @param values the values
	 * @return the {@link Collection} of non-null/non-empty values
	 */
	public Collection<URL> collectNonEmptyURLs( Collection<URL> values )
	{
		return values.stream().filter( ( url ) -> notNullOrEmpty( url ) ).collect( Collectors.toList() );
	}

	/**
	 * Utility method to make sure that a string is neither null nor empty
	 *
	 * @param toTest the {@link String} to test
	 * @return true if the string is neither null nor empty, false if it is null or empty
	 */
	private boolean notNullOrEmpty( String toTest )
	{
		return toTest != null && toTest.trim().length() > 0;
	}

	/**
	 * Utility method to make sure that a URL is neither null nor empty
	 *
	 * @param toTest the {@link URL} to test
	 * @return true if the URL is neither null nor empty, false if it is null or empty
	 */
	private boolean notNullOrEmpty( URL toTest )
	{
		return toTest != null && toTest.toString().length() > 0;
	}

	/**
	 * Utility method to make sure that a collection is neither null nor empty
	 *
	 * @param toTest the {@link Collection} to test
	 * @return true if the collection is neither null nor empty, false if it is null or empty
	 */
	private boolean notNullOrEmpty( Collection<?> toTest )
	{
		return toTest != null && !toTest.isEmpty();
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
	private String jsonStringOrDefault( JSONObject root, String key, String defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			if( value instanceof String )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s'", value, key ) );

				return (String)value;
			}

			throw new UCEFException( "Expected a string value for '%s' but found '%s'",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s', using default value '%s'", key, defaultValue ) );

		return defaultValue;
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
	private int jsonIntOrDefault( JSONObject root, String key, int defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			// integers in JSON data are actually parsed out as longs
			if( value instanceof Long )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s'", value, key ) );

				return ((Long)value).intValue();
			}

			throw new UCEFException( "Expected an integer value for '%s' but found '%s'",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s', using default value '%s'", key, defaultValue ) );

		return defaultValue;
	}

	/**
	 * Utility method to extract a {@link Long} value from a {@link JSONObject} based on a
	 * {@link String} key
	 *
	 * If the value extracted from the key is not a long, the value is rejected, and
	 * a {@link UCEFException} will be thrown in this case.
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return if the provided {@link JSONObject} does not contain
	 *            the given {@link String} key
	 * @return the extracted value, or the default value if there was no such key
	 */
	private long jsonLongOrDefault( JSONObject root, String key, long defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			if( value instanceof Long )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s'", value, key ) );

				return (Long)value;
			}

			throw new UCEFException( "Expected a long value for '%s' but found '%s'",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s', using default value '%s'", key, defaultValue ) );

		return defaultValue;
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
	private double jsonDoubleOrDefault( JSONObject root, String key, double defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			if( value instanceof Double )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s'", value, key ) );

				return (Double)value;
			}
			// be lenient on the parsing of doubles, and allow integer
			// values to get through as well
			if( value instanceof Long )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s'", value, key ) );

				return ((Long)value).doubleValue();
			}

			throw new UCEFException( "Expected a double value for '%s' but found '%s'",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s', using default value '%s'", key, defaultValue ) );

		return defaultValue;
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
	private boolean jsonBooleanOrDefault( JSONObject root, String key, boolean defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Object value = root.get( key );
			if( value instanceof Boolean )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s'", value, key ) );

				return (Boolean)value;
			}

			throw new UCEFException( "Expected a boolean value for '%s' but found '%s'",
			                         key, value.toString() );
		}
		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s', using default value '%s'", key, defaultValue ) );

		return defaultValue;
	}

	/**
	 * Utility method to extract a {@link Set<String>} value from a {@link JSONObject} based on a
	 * {@link String} key
	 *
	 * The content associated with the key is expected to be an array of strings. However it will
	 * also leniently treat a single string as an array of one string.
	 *
	 * If there are any non-string values in the array extracted extracted from the key, the
	 * entire content is rejected (a {@link UCEFException} will be thrown in this case).
	 *
	 * @param root the {@link JSONObject} which is expected to contain the value under the key
	 * @param key the {@link String} key to retrieve the value with
	 * @param defaultValue the value to return if the provided {@link JSONObject} does not contain
	 *            the given {@link String} key
	 * @return the extracted value, or the default value if there was no such key
	 */
	private Set<String> jsonStringSetOrDefault( JSONObject root, String key, Set<String> defaultValue )
	{
		if( root.containsKey( key ) )
		{
			Set<String> result = new HashSet<>();
			Object value = root.get( key );
			if( value instanceof String )
			{
				// leniently treat a single string as an array of one string
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s'", value, key ) );

				result.add( value.toString() );
				return result;
			}

			if( value instanceof JSONArray )
			{
				if(logger.isDebugEnabled())
					logger.debug( String.format( "Found value '%s' for item '%s'", value, key ) );

				JSONArray jsonArray = ((JSONArray)value);
				for(int idx=0; idx<jsonArray.size(); idx++)
				{
					Object current = jsonArray.get( idx );
					if(current instanceof String)
					{
						result.add(current.toString());
					}
					else
					{
						throw new UCEFException( "Expected an array of strings for '%s' but "+
												 "encountered non-string value '%s'",
						                         key, current.toString() );
					}
				}
				return result;
			}

			throw new UCEFException( "Expected a string or array value for '%s' but found '%s'",
			                         key, value.toString() );
		}

		if(logger.isDebugEnabled())
    		logger.debug( String.format( "No value found for '%s', using default value '%s'", key, defaultValue ) );

		return defaultValue;
	}

	/**
	 * Utility function to create a set of {@link URL}s from and array of {@link String} file paths
	 *
	 * NOTE: if any of the paths don't actually correspond to a file that exists on the file system,
	 *       a {@link UCEFException} will be thrown.
	 *
	 * @return a list of URLs corresponding to the paths provided
	 */
	private Collection<URL> urlsFromPaths( Collection<String> paths )
	{
		Set<URL> result = new HashSet<>();

		for( String path : paths )
		{
			URL url = getResourceURL( path );
			if( url != null )
			{
				result.add( url );
			}
			else
			{
				throw new UCEFException( "The file '%s' does not exist. " +
				                         "Please check the file path.",
				                         path );
			}
		}

		return result;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

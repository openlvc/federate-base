/*
 *   Copyright 2019 Calytrix Technologies
 *
 *   This file is part of ucef-java-examples.
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
package gov.nist.ucef.hla.example.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;

import gov.nist.ucef.hla.base.UCEFException;

/**
 * A class which provide simple utility methods for extracting the configuration value which
 * results from the combination of a JSON configuration file and command line arguments.
 * 
 * In all cases...
 *  - the command line argument and JSON keys are expected to be identical, and 
 *  - the command line argument value, if present, will take precedence over any 
 *    corresponding value set in the JSON object.
 */
public class ConfigUtils
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

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Obtain the configured value for the given key
	 * 
	 * @param json the {@link JSONObject} instance
	 * @param cmdLinethe {@link CommandLine} instance
	 * @param key the {@link String} key to obtain the value
	 * @param defaultValue the value to return in the case that neither the JSON nor the command
	 *            line have the value specified
	 * @return the extracted value (with the command line value taking precedence), or the default
	 *         value if none is specified
	 */
	public static String getConfiguredString( JSONObject json,
	                                          CommandLine cmdLine,
	                                          String key,
	                                          String defaultValue )
	{
		String value = defaultValue;
		if( json != null && json.containsKey( key ) )
		{
			Object jsonValue = json.get( key );
			if( jsonValue instanceof String )
				value = jsonValue.toString();
		}
		if( cmdLine != null && cmdLine.hasOption( key ) )
		{
			value = cmdLine.getOptionValue( key ).toString();
		}
		return value;
	}
	
	/**
	 * Obtain the configured value for the given key
	 * 
	 * @param json the {@link JSONObject} instance
	 * @param cmdLinethe {@link CommandLine} instance
	 * @param key the {@link String} key to obtain the value
	 * @param defaultValue the value to return in the case that neither the JSON nor the command
	 *            line have the value specified
	 * @return the extracted value (with the command line value taking precedence), or the default
	 *         value if none is specified
	 */
	public static int getConfiguredInt( JSONObject json,
	                                    CommandLine cmdLine,
	                                    String key,
	                                    int defaultValue )
	{
		int value = defaultValue;
		if( json != null && json.containsKey( key ) )
		{
			Object jsonValue = json.get( key );
			if( jsonValue instanceof Long )
				value = ((Long)jsonValue).intValue();
			else
				throw new UCEFException( String.format( "Expected an integer value for '%s' " +
				                                        "but found '%s'",
				                                        key, jsonValue.toString() ) );
		}
		if( cmdLine != null && cmdLine.hasOption( key ) )
		{
			try
			{
				Object parsed = cmdLine.getParsedOptionValue( key );
				if( parsed instanceof Long )
					value = ((Long)parsed).intValue();
				else
				{
					throw new UCEFException( String.format( "Expected an integer value for '%s' " +
					                                        "but found '%s'",
					                                        key, parsed.toString() ) );
				}
			}
			catch( ParseException e )
			{
				throw new UCEFException( e );
			}
		}
		return value;
	}
	
	/**
	 * Obtain the configured value for the given key
	 * 
	 * @param json the {@link JSONObject} instance
	 * @param cmdLinethe {@link CommandLine} instance
	 * @param key the {@link String} key to obtain the value
	 * @param defaultValue the value to return in the case that neither the JSON nor the command
	 *            line have the value specified
	 * @return the extracted value (with the command line value taking precedence), or the default
	 *         value if none is specified
	 */
	public static long getConfiguredLong( JSONObject json,
	                                      CommandLine cmdLine,
	                                      String key,
	                                      long defaultValue )
	{
		long value = defaultValue;
		if( json != null && json.containsKey( key ) )
		{
			Object jsonValue = json.get( key );
			if( jsonValue instanceof Long )
				value = ((Long)jsonValue).longValue();
			else
			{
				throw new UCEFException( String.format( "Expected a long value for '%s' " +
				                                        "but found '%s'",
				                                        key,
				                                        jsonValue.toString() ) );
			}
		}
		if( cmdLine != null && cmdLine.hasOption( key ) )
		{
			try
			{
				Object parsed = cmdLine.getParsedOptionValue( key );
				if( parsed instanceof Long )
					value = ((Long)parsed).longValue();
				else
				{
					throw new UCEFException( String.format( "Expected a long value for '%s' " +
					                                        "but found '%s'",
					                                        key, parsed.toString() ) );
				}
			}
			catch( ParseException e )
			{
				throw new UCEFException( e );
			}
		}
		return value;
	}
	
	/**
	 * Obtain the configured value for the given key
	 * 
	 * @param json the {@link JSONObject} instance
	 * @param cmdLinethe {@link CommandLine} instance
	 * @param key the {@link String} key to obtain the value
	 * @param defaultValue the value to return in the case that neither the JSON nor the command
	 *            line have the value specified
	 * @return the extracted value (with the command line value taking precedence), or the default
	 *         value if none is specified
	 */
	public static double getConfiguredDouble( JSONObject json,
	                                          CommandLine cmdLine,
	                                          String key,
	                                          double defaultValue )
	{
		double value = defaultValue;
		if( json != null && json.containsKey( key ) )
		{
			Object jsonValue = json.get( key );
			if( jsonValue instanceof Double )
				value = ((Double)jsonValue).doubleValue();
			else if( jsonValue instanceof Long )
				value = ((Long)jsonValue).doubleValue();
			else
				throw new UCEFException( String.format( "Expected a double value for '%s' " +
				                                        "but found '%s'",
				                                        key, jsonValue.toString() ) );
		}
		if( cmdLine != null && cmdLine.hasOption( key ) )
		{
			try
			{
				Object parsed = cmdLine.getParsedOptionValue( key );
				if( parsed instanceof Double )
					value = ((Double)value).doubleValue();
				else if( parsed instanceof Long )
					value = ((Long)parsed).doubleValue();
				{
					throw new UCEFException( String.format( "Expected a double value for '%s' " +
					                                        "but found '%s'",
					                                        key, parsed.toString() ) );
				}
			}
			catch( ParseException e )
			{
				throw new UCEFException( e );
			}
		}
		return value;
	}
	
	/**
	 * Obtain the configured value for the given key
	 * 
	 * NOTE: in this case, the command line key is expected to be a 'switch' argument - its
	 * presence indicates "true", and its absence indicates "false". The JSON object is expected
	 * to contain a boolean true or false value against the key.
	 * 
	 * @param json the {@link JSONObject} instance
	 * @param cmdLinethe {@link CommandLine} instance
	 * @param key the {@link String} key to obtain the value
	 * @param defaultValue the value to return in the case that neither the JSON nor the command
	 *            line have the value specified
	 * @return the extracted value (with the command line value taking precedence), or the default
	 *         value if none is specified
	 */
	public static boolean getConfiguredBoolean( JSONObject json,
	                                            CommandLine cmdLine,
	                                            String key,
	                                            boolean defaultValue )
	{
		boolean value = defaultValue;
		if( json != null && json.containsKey( key ) )
		{
			Object jsonValue = json.get( key );
			if( jsonValue instanceof Boolean )
				value = ((Boolean)jsonValue).booleanValue();
			else
				throw new UCEFException( String.format( "Expected a boolean value for '%s' " +
				                                        "but found '%s'",
				                                        key, jsonValue.toString() ) );
		}
		if( cmdLine != null && cmdLine.hasOption( key ) )
		{
			value = true;
		}
		return value;
	}
}

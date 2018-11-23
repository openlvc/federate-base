/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-java.
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

import java.util.Properties;
import java.util.StringTokenizer;

public class CmdLine
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
	////////////////////////// Command Line Argument Handling Methods //////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Scan the provided command line for all valid arguments. An argument is considered valid if
	 * it begins with the "--" string. The format of the argument should be as follows:
	 * <p/>
	 * <code>--property.name=propertyValue</code><br/>
	 * <code>--property.name</code>
	 * <p/>
	 * In this format, the property name should be whatever you want the property key to be. For
	 * example, consider the following line:
	 * <p/>
	 * <code>--some.value=123</code>
	 * <p/>
	 * This would result in the property "some.value" having the value "123".
	 * <p/>
	 * If there is no "=" character, the value of the argument is assumed to be the key and the
	 * value is given "true". For example, consider the following line:
	 * <p/>
	 * <code>--use.decimal</code>
	 * <p/>
	 * This would result in the property "use.decimal" having the value "true"
	 *
	 * @param arguments The set of command line arguments to extract the properties from.
	 * @return The set of properties extracted from the provided command line.
	 */
	public static Properties getCommandLineProperties( String[] arguments )
	{
		Properties properties = new Properties();

		for( String argument : arguments )
		{
			// only look at arguments that start with --
			if( argument.startsWith("--") == false )
				continue;

			// strip off the -- and break the string into key and value
			argument = argument.substring( 2 );

			// if there is an "=", we need to break it into a key/value pair
			// if there isn't, we just take the whole thing as the property name and give it "true"
			if( argument.contains("=") == false )
			{
				properties.put( argument, "true" );
				continue;
			}

			// break the string into its key/vaule pair
			StringTokenizer tokenizer = new StringTokenizer( argument, "=" );
			if( tokenizer.countTokens() != 2 )
				continue;

			// get the values and store them
			String key = tokenizer.nextToken();
			String value = tokenizer.nextToken();
			properties.put( key, value );
		}

		return properties;
	}	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

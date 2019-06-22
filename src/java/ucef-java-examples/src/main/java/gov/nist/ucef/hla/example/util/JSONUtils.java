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

import java.io.File;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import gov.nist.ucef.hla.base.UCEFException;

public class JSONUtils
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
	 * Obtain a {@link JSONObject} from the provided {@link String} which may be any of...
	 * 
	 *  - a raw JSON string
	 *  - the path to a file on the file system
	 *  - the path to a resource (as would be packaged in a JAR file)
	 *
	 * @param jsonSource the source of the JSON data
	 * @return the extractde {@link JSONObject} instance
	 */
	public static JSONObject toJsonObject( String jsonSource )
	{
		// see if the configuration source is a file
		File configFile = FileUtils.getResourceFile( jsonSource );
		boolean isFile = configFile != null;

		// assume for the moment that the JSON is coming directly from the
		// configuration source parameter
		String json = jsonSource;
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
		return (JSONObject)parsedString;
	}
}

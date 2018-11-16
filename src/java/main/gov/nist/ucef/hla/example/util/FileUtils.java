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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.nist.ucef.hla.base.UCEFException;

public class FileUtils
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
	 * Utility function to set create a bunch of URLs from file paths
	 * 
	 * NOTE: if any of the paths don't actually correspond to a file that exists on the file system, 
	 *       a {@link UCEFException} will be thrown.
	 * 
	 * @return a list of URLs corresponding to the paths provided
	 */
	public static Collection<URL> urlsFromPaths(String[] paths)
	{
		List<URL> result = new ArrayList<>();
		
		try
		{
    		for(String path : paths)
    		{
    			File file = new File( path );
    			if(file.isFile())
    					result.add( new File( path ).toURI().toURL() );
    			else
    				throw new UCEFException("The file '%s' does not exist. " +
    										"Please check the file path.", path);
    		}
		}
		catch( MalformedURLException e )
		{
			throw new UCEFException(e);
		}
		
		return result;
	}
}

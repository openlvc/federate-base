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
package gov.nist.ucef.hla.util;

public class RTIUtils
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
	 * Utility method to obtain the "simple name" for a dot delimited fully qualified name.
	 * 
	 * In practice this simply returns the portion of the fully qualified name after the final
	 * dot.
	 * 
	 * For example, given "some.namespace.here.then.thename", this method will return "thename"
	 * 
	 * @param fullyQualifiedName the fully qualified name
	 * @return the portion of the fully qualified name after the final dot, or the original string
	 *         if there is no dot, or an empty string if the source string is null.
	 */
	public static String simpleName(String fullyQualifiedName)
	{
		if( fullyQualifiedName == null )
			return "";

		int lastDot = fullyQualifiedName.lastIndexOf( '.' );

		if( lastDot < 0 )
			return fullyQualifiedName;

		return fullyQualifiedName.substring( lastDot );
	}
}

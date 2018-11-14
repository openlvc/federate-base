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
package gov.nist.ucef.hla.base;

public class UCEFException extends RuntimeException
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = -3642541500621823260L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Just create an empty exception
	 */
	public UCEFException()
	{
		super();
	}

	/**
	 * @param message The message to create the exception with
	 */
	public UCEFException( String message )
	{
		super( message );
	}

	/**
	 * @param cause The cause of the exception
	 */
	public UCEFException( Throwable cause )
	{
		super( cause );
	}

	/**
	 * @param message The message to create the exception with
	 * @param cause The cause of the exception
	 */
	public UCEFException( String message, Throwable cause )
	{
		super( message, cause );
	}

	/**
	 * @param formatString A format string to use with the arguments to construct the message
	 * @param args The arguments to use with the format string
	 */
	public UCEFException( String formatString, Object... args )
	{
		super( String.format( formatString, args ) );
	}

	/**
	 * @param cause The cause of the exception
	 * @param formatString A format string to use with the arguments to construct the message
	 * @param args The arguments to use with the format string
	 */
	public UCEFException( Throwable cause, String formatString, Object... args )
	{
		super( String.format( formatString, args ), cause );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
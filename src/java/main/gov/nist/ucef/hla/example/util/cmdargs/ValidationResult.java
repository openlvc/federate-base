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
package gov.nist.ucef.hla.example.util.cmdargs;

/**
 * A class to contain a command line argument validation result
 */
public class ValidationResult
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// to save creation of success instances all the time, this standard
	// one can be used - generally all you need is a true flag and no
	// message if everything is OK
	public final static ValidationResult GENERIC_SUCCESS = new ValidationResult( true, "" );

	//----------------------------------------------------------
	//                    INSTANCE VARIABLES
	//----------------------------------------------------------
	boolean isValid;
	private String msg;

	public ValidationResult( boolean isValid, String msg )
	{
		this.isValid = isValid;
		this.msg = msg;
	}

	public boolean isValid()
	{
		return this.isValid;
	}

	public String getMessage()
	{
		return this.msg;
	}
}

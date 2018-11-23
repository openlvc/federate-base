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

public class StandardValidator
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final Validator POS_DOUBLE = new Validator()
	{
		@Override
		public ValidationResult validate( Object value )
		{
			try
			{
				if( Double.parseDouble( (String)value ) > 0.0 )
					return ValidationResult.GENERIC_SUCCESS;
			}
			catch( Exception e )
			{
				// ignore
			}
			return new ValidationResult( false, "Value must be greater than zero." );
		}
	};

	public static final Validator POS_INT = new Validator()
	{
		@Override
		public ValidationResult validate( Object value )
		{
			try
			{
				if( Integer.parseInt( (String)value ) > 0.0 )
					return ValidationResult.GENERIC_SUCCESS;
			}
			catch( Exception e )
			{
				// ignore
			}
			return new ValidationResult( false, "Value must be a whole number greater than zero." );
		}
	};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private StandardValidator()
	{
		// hidden constructor
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

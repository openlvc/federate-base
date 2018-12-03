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

import java.util.List;

/**
 * Static class providing some validators which may be commonly used.
 */
public class StdValidators
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// validator to check that the value is a valid floating point value
	public static final IArgValidator CheckDouble = new IArgValidator()
	{
		@Override
		public ValidationResult validate( Object value )
		{
			ValidationResult initialValidation = CheckNonEmptyString.validate( value );
			if( initialValidation.isInvalid() )
				return initialValidation;
			
			try
			{
				Double.parseDouble( (String)value );
				return ValidationResult.GENERIC_SUCCESS;
			}
			catch( Exception e )
			{
				// if *any* exception is thrown, the value is invalid
			}
			return new ValidationResult( false, 
			                             "'%s' is not a number.", value );
		}
	};

	// validator to check that the value is a valid floating point value
	// which is greater than zero
	public static final IArgValidator CheckDoubleGtZero = new IArgValidator()
	{
		@Override
		public ValidationResult validate( Object value )
		{
			ValidationResult initialValidation = CheckNonEmptyString.validate( value );
			if( initialValidation.isInvalid() )
				return initialValidation;

			try
			{
				if( Double.parseDouble( (String)value ) > 0.0 )
					return ValidationResult.GENERIC_SUCCESS;
			}
			catch( Exception e )
			{
				// if *any* exception is thrown, the value is invalid
			}
			return new ValidationResult( false, 
			                             "'%s' is not a number greater than zero.", value );
		}
	};

	// validator to check that the value is a valid integer value
	public static final IArgValidator CheckInt = new IArgValidator()
	{
		@Override
		public ValidationResult validate( Object value )
		{
			ValidationResult initialValidation = CheckNonEmptyString.validate( value );
			if( initialValidation.isInvalid() )
				return initialValidation;
			
			try
			{
				Integer.parseInt( (String)value );
				return ValidationResult.GENERIC_SUCCESS;
			}
			catch( Exception e )
			{
				// if *any* exception is thrown, the value is invalid
			}
			return new ValidationResult( false, 
			                             "'%s' is not an integer value.", value );
		}
	};

	// validator to check that the value is a valid integer value
	// which is greater than zero
	public static final IArgValidator CheckIntGtZero = new IArgValidator()
	{
		@Override
		public ValidationResult validate( Object value )
		{
			ValidationResult initialValidation = CheckNonEmptyString.validate( value );
			if( initialValidation.isInvalid() )
				return initialValidation;
			
			try
			{
				if( Integer.parseInt( (String)value ) > 0 )
					return ValidationResult.GENERIC_SUCCESS;
			}
			catch( Exception e )
			{
				// if *any* exception is thrown, the value is invalid
			}
			return new ValidationResult( false, 
			                             "'%s' is not an integer value greater than zero.", value );
		}
	};
	
	// validator to check that the value is a list of valid integers
	public static final IArgValidator CheckIntList = new IArgValidator()
	{
		@Override
		public ValidationResult validate( Object value )
		{
			ValidationResult initialValidation = CheckNonEmptyList.validate( value );
			if( initialValidation.isInvalid() )
				return initialValidation;
			
			String lastItem = null;
			try
			{
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>)value;
				for( String item : list )
				{
					lastItem = item;
					Integer.parseInt( item );
				}
				return ValidationResult.GENERIC_SUCCESS;
			}
			catch( Exception e )
			{
				// if *any* exception is thrown, the value is invalid
			}
			return new ValidationResult( false, 
			                             "'%s' is not an integer value.", lastItem );
		}
	};


	// validator to check that the value is a valid floating point value
	public static final IArgValidator CheckNonEmptyString = new IArgValidator()
	{
		@Override
		public ValidationResult validate( Object value )
		{
			if( value != null )
			{
				try
				{
					if( ((String)value).trim().length() > 0 )
						return ValidationResult.GENERIC_SUCCESS;
				}
				catch( ClassCastException e )
				{
					// if *any* exception is thrown, the value is invalid
					return new ValidationResult( false,
					                             "Internal error - value was not a String." );
				}
			}
			return new ValidationResult( false, "Value may not be empty." );
		}
	};
	
	// validator to check that the value is a valid floating point value
	public static final IArgValidator CheckNonEmptyList = new IArgValidator()
	{
		@Override
		@SuppressWarnings("unchecked")
		public ValidationResult validate( Object value )
		{
			try
			{
				if( !((List<String>)value).isEmpty() )
					return ValidationResult.GENERIC_SUCCESS;
			}
			catch( ClassCastException e )
			{
				// if *any* exception is thrown, the value is invalid
				return new ValidationResult( false, "Internal error - value was not a List<String>." );
			}
			return new ValidationResult( false, "List may not be empty." );
		}
	};
	
	
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private StdValidators()
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

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

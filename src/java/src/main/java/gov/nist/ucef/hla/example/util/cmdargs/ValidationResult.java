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

	public ValidationResult( boolean isValid, String formatter, Object... args )
	{
		this.isValid = isValid;
		this.msg = String.format( formatter, args );
	}
	
	public boolean isValid()
	{
		return this.isValid;
	}
	
	public boolean isInvalid()
	{
		return !isValid();
	}

	public String getMessage()
	{
		return this.msg;
	}
}

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
package gov.nist.ucef.hla.util.cmdargs;

/**
 * Abstract class to provide the basic functionality for any command line argument
 */
public abstract class Arg
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	protected static final String SHORT_FORM_PREFIX = "-"; 
	protected static final String LONG_FORM_PREFIX = "--"; 

	//----------------------------------------------------------
	//                    INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Character shortForm;
	protected String longForm;
	protected String help;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor
	 * 
	 * @param shortForm the short form of the argument
	 * @param longForm the long form of the argument
	 */
	public Arg( Character shortForm, String longForm )
	{
		this.longForm = longForm;
		this.shortForm = shortForm;

		this.help = "";
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Reset this argument to its initial state (i.e., as if it had never been parsed)
	 */
	protected abstract void reset();

	/**
	 * Determine the kind of argument this is
	 * 
	 * @return the kind of argument this is
	 */
	protected abstract ArgKind argKind();

	/**
	 * Determine if this value argument is required
	 * 
	 * @return true if this value argument is required, false otherwise
	 */
	protected abstract boolean isRequired();

	/**
	 * Parse the string value for this argument
	 * 
	 * @param value this argument's value
	 * @return the argument instance
	 */
	protected abstract <T extends Arg> T parse( String value );
	
	/**
	 * Validate the value for this argument
	 * 
	 * @return true if this value argument is required, false otherwise
	 */
	protected abstract ValidationResult validate();
	
	/**
	 * Determine if this argument has a short form
	 * 
	 * @return true if this argument has a short form, false otherwise
	 */
	protected boolean hasShortForm()
	{
		return this.shortForm != null;
	}

	/**
	 * Determine if this argument has a long form
	 * 
	 * @return true if this argument has a long form, false otherwise
	 */
	protected boolean hasLongForm()
	{
		return this.longForm != null && this.longForm.length() > 0;
	}

	/**
	 * Determine if this argument has help text associated with it
	 * 
	 * @return true if this argument has help text associated with it, false otherwise
	 */
	protected boolean hasHelp()
	{
		return this.help != null && this.help.length() > 0;
	}

	/**
	 * Set the help text associated with this argument
	 * 
	 * @param help the help text to associate with this argument
	 */
	protected void setHelp( String help )
	{
		this.help = help;
	}

	/**
	 * Obtain the help text associated with this argument
	 * 
	 * @return the help text associated with this argument
	 */
	protected String getHelp()
	{
		return this.help;
	}
	
	/**
	 * Return the prefixed representation of this argument, preferring the short or long form as
	 * requested (if available)
	 *
	 * @param preferLong if true, use the long form of the argument (if available) in preference
	 *            to the short form. If false, use the short form of the argument (if available)
	 *            in preference to the long form.
	 * @return the prefixed representation of this argument
	 */
	protected String makePrefixedArg( boolean preferLong )
	{
		if( preferLong )
			return hasLongForm() ? LONG_FORM_PREFIX + longForm
			                     : SHORT_FORM_PREFIX + shortForm.toString();
		
		return hasShortForm() ? SHORT_FORM_PREFIX + shortForm.toString()
		                      : LONG_FORM_PREFIX + longForm;
	}

	/**
	 * Obtain a basic usage string associated with this argument
	 * 
	 * @return a basic usage string associated with this argument
	 */
	protected String getUsageString()
	{
		return makePrefixedArg( true );
	}
}

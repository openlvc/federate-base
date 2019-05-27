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
 * Class to provide the functionality for 'switch' command line arguments
 */
public class SwitchArg extends Arg
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final ArgKind argKind = ArgKind.SWITCH;

	//----------------------------------------------------------
	//                    INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean defaultValue;
	private boolean actualValue;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor
	 * 
	 * @param shortForm the short form of the switch argument
	 * @param longForm the long form of the switch argument
	 */
	public SwitchArg( Character shortForm, String longForm )
	{
		super( shortForm, longForm );
		defaultValue = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////
	// start Arg method implementations
	@Override
	protected void reset()
	{
		this.actualValue = this.defaultValue;
	}

	@Override
	public ArgKind argKind()
	{
		return argKind;
	}

	@Override
	public boolean isRequired()
	{
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected SwitchArg parse( String value )
	{
		// no action required here
		return this;
	}
	
	@Override
	protected ValidationResult validate()
	{
		return ValidationResult.GENERIC_SUCCESS;
	}
	// end Arg method implementations
	////////////////////////////////////////////////////////////
	
	/**
	 * Set the state of this switch
	 * 
	 * @param value the state of this switch
	 */
	protected void set( boolean value )
	{
		this.actualValue = value;
	}

	/**
	 * Obtain the state of this switch
	 * 
	 * @return the state of this switch
	 */
	public boolean value()
	{
		return this.actualValue;
	}

	/**
	 * Set the default value of this switch if it is not specified
	 * 
	 * @param defaultValue the default value of this switch if it is not specified
	 * @return this instance, to facilitate method chaining
	 */
	public SwitchArg defaultValue( boolean defaultValue )
	{
		this.defaultValue = defaultValue;
		return this;
	}

	/**
	 * Obtain the default value of this switch
	 * 
	 * @return the default value of this switch
	 */
	public boolean defaultValue()
	{
		return this.defaultValue;
	}

	/**
	 * Set the help text for this switch
	 * 
	 * @param help the help text for this switch
	 * @return this instance, to facilitate method chaining
	 */
	public SwitchArg help( String help )
	{
		super.setHelp( help );
		return this;
	}

	/**
	 * Obtain the help text for this switch
	 * 
	 * @return the help text for this switch
	 */
	public String help()
	{
		return super.getHelp();
	}

	/**
	 * Obtain a basic usage string for this switch
	 * 
	 * @return a basic usage string for this switch
	 */
	public String getUsageString()
	{
		StringBuilder usage = new StringBuilder();
		usage.append( "[" );
		usage.append( super.getUsageString() );
		usage.append( "]" );
		return usage.toString();
	}
}

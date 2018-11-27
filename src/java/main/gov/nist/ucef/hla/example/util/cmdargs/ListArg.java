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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to provide the functionality for multiple 'value' command line arguments
 */
public class ListArg extends Arg
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final ArgKind argKind = ArgKind.LIST;

	//----------------------------------------------------------
	//                    INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean isRequired;
	private boolean isSet;
	private List<String> defaultValue;
	private List<String> actualValue;
	private String hint;
	protected IArgValidator validator;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor
	 * 
	 * @param shortForm the short form of the value argument
	 * @param longForm the long form of the value argument
	 */
	public ListArg( Character shortForm, String longForm )
	{
		super( shortForm, longForm );
		isRequired = false;
		isSet = false;
		defaultValue = Collections.emptyList();
		actualValue = new ArrayList<>();
		hint = "";
		validator = null;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////
	// start Arg method implementations
	@Override
	protected void reset()
	{
		this.isSet = false;
		this.actualValue = new ArrayList<>();
	}

	@Override
	public ArgKind argKind()
	{
		return argKind;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected ListArg parse( String value )
	{
		this.actualValue.add( value );
		this.isSet = true;
		return this;
	}

	@Override
	public ValidationResult validate()
	{
		if( this.validator == null )
			return ValidationResult.GENERIC_SUCCESS;

		return this.validator.validate( value() );
	}

	// end Arg method implementations
	////////////////////////////////////////////////////////////

	/**
	 * Set the validator for this argument
	 * 
	 * @param validator the validator
	 * @return
	 */
	public ListArg validator( IArgValidator validator )
	{
		this.validator = validator;
		return this;
	}

	/**
	 * Determine if a value has been provided for this value argument
	 * 
	 * @return true if a value has been provided for this value argument, false otherwise
	 */
	public boolean isSet()
	{
		return this.isSet;
	}

	/**
	 * Obtain the current value for this value argument
	 * 
	 * @return the current value for this value argument
	 */
	public List<String> value()
	{
		return isSet() ? this.actualValue : this.defaultValue;
	}

	/**
	 * Obtain the current value for this value argument
	 * 
	 * @return the current value for this value argument
	 */
	public String valuesToString( String delimiter )
	{
		List<String> val = isSet() ? this.actualValue : this.defaultValue;

		if( val == null )
			return null;

		return val.stream().collect( Collectors.joining( delimiter ) );
	}

	/**
	 * Set whether this value argument is required
	 * 
	 * @param isRequired true if this value argument is required, false otherwise
	 */
	public ListArg isRequired( boolean isRequired )
	{
		this.isRequired = isRequired;
		return this;
	}

	/**
	 * Determine if this value argument is required
	 * 
	 * @return true if this value argument is required, false otherwise
	 */
	public boolean isRequired()
	{
		return this.isRequired;
	}

	/**
	 * Set the default value of this value argument if it is not specified
	 * 
	 * @param defaultValue the default value of this value argument if it is not specified
	 * @return this instance, to facilitate method chaining
	 */
	public ListArg defaultValue( List<String> defaultValue )
	{
		this.defaultValue = defaultValue;
		return this;
	}

	/**
	 * Obtain the default value of this value argument if it is not specified
	 * 
	 * @return the default value of this value argument if it is not specified
	 */
	public List<String> defaultValue()
	{
		return this.defaultValue;
	}

	/**
	 * Set the help text for this value argument
	 * 
	 * @param help the help text for this value argument
	 * @return this instance, to facilitate method chaining
	 */
	public ListArg help( String help )
	{
		super.setHelp( help );
		return this;
	}

	/**
	 * Obtain the help text for this value argument
	 * 
	 * @return the help text for this value argument
	 */
	public String help()
	{
		return super.getHelp();
	}

	/**
	 * Set the hint text for this value argument
	 * 
	 * @param hint the hint text for this value argument
	 * @return this instance, to facilitate method chaining
	 */
	public ListArg hint( String hint )
	{
		this.hint = hint;
		return this;
	}

	/**
	 * Obtain the hint text for this value argument
	 * 
	 * @return the hint text for this value argument
	 */
	public String hint()
	{
		return this.hint;
	}

	/**
	 * Determine if this argument has hint text associated with it
	 * 
	 * @return true if this argument has hint text associated with it, false otherwise
	 */
	public boolean hasHint()
	{
		return this.hint != null & this.hint.length() > 0;
	}

	/**
	 * Obtain a basic usage string associated with this argument
	 * 
	 * @return a basic usage string associated with this argument
	 */
	public String getUsageString()
	{
		StringBuilder usage = new StringBuilder();
		if( !isRequired )
			usage.append( "[" );
		usage.append( super.getUsageString() );
		if( hasHint() )
			usage.append( " <" ).append( hint() ).append( ">" );
		if( !isRequired )
			usage.append( "]" );
		return usage.toString();
	}
}

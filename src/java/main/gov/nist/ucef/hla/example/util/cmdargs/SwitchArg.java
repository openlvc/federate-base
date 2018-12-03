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

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
 * Abstract class to provide the basic functionality for any command line argument
 */
public abstract class CmdLineArgument
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

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
	public CmdLineArgument( Character shortForm, String longForm )
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
	public abstract ArgumentKind argKind();

	/**
	 * Determine if this value argument is required
	 * 
	 * @return true if this value argument is required, false otherwise
	 */
	public abstract boolean isRequired();

	/**
	 * Determine if this argument has a short form
	 * 
	 * @return true if this argument has a short form, false otherwise
	 */
	public boolean hasShortForm()
	{
		return this.shortForm != null;
	}

	/**
	 * Determine if this argument has a long form
	 * 
	 * @return true if this argument has a long form, false otherwise
	 */
	public boolean hasLongForm()
	{
		return this.longForm != null && this.longForm.length() > 0;
	}

	/**
	 * Determine if this argument has help text associated with it
	 * 
	 * @return true if this argument has help text associated with it, false otherwise
	 */
	public boolean hasHelp()
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
	 * Obtain a basic usage string associated with this argument
	 * 
	 * @return a basic usage string associated with this argument
	 */
	public String getUsageString()
	{
		StringBuilder usage = new StringBuilder();
		usage.append( '-' );
		if( hasShortForm() )
		{
			usage.append( shortForm );
		}
		else
		{
			usage.append( '-' );
			usage.append( longForm );
		}
		return usage.toString();
	}
}

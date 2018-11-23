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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A simple command line argument parser class
 *
 * Example usage:
 *
 * public static void main(String args[])
 * {
 *    CmdArgParser cmdArgParser = new CmdArgParser();
 *    SwitchArgument theSwitch = cmdArgParser.addSwitchArg('a', "activate").help("Activate the thing.");
 *    ValueArgument alphabetValue = cmdArgParser.addValueArg(null, "alphabet").isRequired(false).help("Define Alphabet").hint("ABCDEFG...");
 *    ValueArgument bradshawValue = cmdArgParser.addValueArg('b', "bradshaw").isRequired(true).help("Set the bradshaw radius").hint("RADIUS");
 *    try
 *    {
 *        cmdArgParser.parse(args);
 *        System.out.println(theSwitch.value());
 *        System.out.println(alphabetValue.value());
 *        System.out.println(bradshawValue.value());
 *    }
 *    catch (ParseException e)
 *    {
 *        System.err.println(e.getMessage());
 *        System.err.println("Usage: " + cmdArgParser.getUsage("mycommand"));
 *        System.err.println(cmdArgParser.getHelp());
 *    }
 *    System.exit(0);
 * }
 */
public class CmdArgProcessor
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE VARIABLES
	//----------------------------------------------------------
	// maps for looking up under short and long forms of command line arguments
	private Map<Character,CmdLineArgument> shortFormArgMap;
	private Map<String,CmdLineArgument> longFormArgMap;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor - nothing special here
	 */
	public CmdArgProcessor()
	{
		shortFormArgMap = new HashMap<>();
		longFormArgMap = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Add a {@link SwitchArgument} to supply a boolean argument. This is implicitly optional
	 *
	 * @param shortForm the short form of the command line argument
	 * @param longForm the long form of the command line argument
	 *
	 * @return the created {@link SwitchArgument}, or null if *both* short and long forms are null
	 *         or empty
	 */
	public SwitchArgument addSwitchArg( Character shortForm, String longForm )
	{
		if( shortForm == null && (longForm == null || longForm.length() == 0) )
			return null;

		SwitchArgument switchArgument = new SwitchArgument( shortForm, longForm );

		if( switchArgument.hasShortForm() )
			shortFormArgMap.put( shortForm, switchArgument );
		if( switchArgument.hasLongForm() )
			longFormArgMap.put( longForm, switchArgument );

		return switchArgument;
	}

	/**
	 * Add a {@link ValueArgument} to supply a string argument.
	 *
	 * @param shortForm the short form of the command line argument
	 * @param longForm the long form of the command line argument
	 *
	 * @return the created {@link ValueArgument}, or null if *both* short and long forms are null
	 *         or empty
	 */
	public ValueArgument addValueArg( Character shortForm, String longForm )
	{
		if( shortForm == null && (longForm == null || longForm.length() == 0) )
			return null;

		ValueArgument valueArgument = new ValueArgument( shortForm, longForm );

		if( valueArgument.hasShortForm() )
			shortFormArgMap.put( shortForm, valueArgument );
		if( valueArgument.hasLongForm() )
			longFormArgMap.put( longForm, valueArgument );

		return valueArgument;
	}

	/**
	 * Add a {@link ListArgument} to supply one or more string arguments under the same argument
	 *
	 * @param shortForm the short form of the command line argument
	 * @param longForm the long form of the command line argument
	 *
	 * @return the created {@link ListArgument}, or null if *both* short and long forms are null
	 *         or empty
	 */
	public ListArgument addListArg( Character shortForm, String longForm )
	{
		if( shortForm == null && (longForm == null || longForm.length() == 0) )
			return null;

		ListArgument listArgument = new ListArgument( shortForm, longForm );

		if( listArgument.hasShortForm() )
			shortFormArgMap.put( shortForm, listArgument );
		if( listArgument.hasLongForm() )
			longFormArgMap.put( longForm, listArgument );

		return listArgument;
	}

	/**
	 * Parse the provided command line arguments using the defined {@link SwitchArgument}s and
	 * {@link ValueArgument}s
	 *
	 * @param args the command line arguments to be parsed
	 * @throws CmdArgException if there are unrecognised command line arguments, or missing
	 *             required command line arguments, or arguments which do not pass validation 
	 */
	public void process( String[] args ) throws CmdArgException
	{
		// reset everything
		for( CmdLineArgument cmdLineArgument : collectAllCommandLineArguments() )
			cmdLineArgument.reset();

		// get a list of required command line arguments so that we can check them off
		// and make sure they are all specified
		List<CmdLineArgument> requiredArgList = Arrays.asList( collectRequiredArguments() );
		Set<CmdLineArgument> requiredArgs = new HashSet<>( requiredArgList );
		Map<CmdLineArgument,ValidationResult> invalidParams = new HashMap<>();
		// make a list to contain anything we came across that we don't recognise
		List<String> unknownArgs = new ArrayList<>();

		// parse the command line arguments
		for( int i = 0; i < args.length; i++ )
		{
			String arg = args[i];
			if( arg.charAt( 0 ) == '-' && arg.length() > 1 )
			{
				// figure out if it's a short or long form version of the argument,
				// and look it up in the tables as appropriate
				CmdLineArgument cmdLineArgument = null;
				if( arg.charAt( 1 ) == '-' && arg.length() > 2 )
					cmdLineArgument = longFormArgMap.get( arg.substring( 2 ) );
				else if( arg.charAt( 1 ) != '-' && arg.length() == 2 )
					cmdLineArgument = shortFormArgMap.get( arg.charAt( 1 ) );

				ArgumentKind argKind = cmdLineArgument != null ? cmdLineArgument.argKind() 
				                                               : ArgumentKind.UNKNOWN;
				// handle the argument as required
				if( ArgumentKind.VALUE.equals( argKind ) || 
					ArgumentKind.LIST.equals( argKind ) )
				{
					// ValueArgument or ListArgument, so we need to grab the 
					// next item as the value for it
					if( i + 1 < args.length )
					{
						// get the value
						if( ArgumentKind.VALUE.equals( argKind ) )
						{
							ValidationResult result = ((ValueArgument)cmdLineArgument).parse( args[i + 1] ).validate();
							if( !result.isValid )
								invalidParams.put( cmdLineArgument, result );
						}
						else
						{
							ValidationResult result = ((ListArgument)cmdLineArgument).parse( args[i + 1] ).validate();
							if( !result.isValid )
								invalidParams.put( cmdLineArgument, result );
						}
						// skip over the value so we don't parse it as the next argument
						i++;
						// tick off the argument from the required list (if it's there)
						requiredArgs.remove( cmdLineArgument );
					}
				}
				else if( ArgumentKind.SWITCH.equals( argKind ) )
				{
					// switch argument - just set it now
					((SwitchArgument)cmdLineArgument).set( true );
				}
				else
				{
					// unknown argument!
					String argName = arg.substring( arg.lastIndexOf( '-' ) + 1 );
					if( !"".equals( argName ) )
						unknownArgs.add( argName );
				}
			}
			else
			{
				// all command line arguments must be prefixed with either a '-' or '--', but
				// this one wasn't so we don't know what to do.
				unknownArgs.add( arg );
			}
		}

		// is there anything left in the required arguments that didn't get ticked off?
		if( !requiredArgs.isEmpty() )
		{
			// need to throw an exception with an informative error message
			StringBuilder message = new StringBuilder();
			message.append( "Missing required argument" );
			message.append( requiredArgs.size() == 1 ? ": " : "s: " );
			CmdLineArgument[] requiredArgsArray = requiredArgs.toArray( new CmdLineArgument[0] );
			for( int idx = 0; idx < requiredArgsArray.length; idx++ )
			{
				CmdLineArgument cmdLineArgument = requiredArgsArray[idx];
				String argName = cmdLineArgument.hasShortForm() ? "-" + cmdLineArgument.shortForm.toString()
				                                   				: "--" + cmdLineArgument.longForm;
				message.append( argName );
				if( idx + 2 < requiredArgsArray.length )
					message.append( ", " );
				else if( idx + 1 < requiredArgsArray.length )
					message.append( " and " );
			}
			throw new CmdArgException( message.toString() );
		}

		// were there any unknown arguments encountered?
		if( !unknownArgs.isEmpty() )
		{
			// need to throw an exception with an informative error message
			StringBuilder message = new StringBuilder();
			message.append( "Unknown argument" );
			message.append( unknownArgs.size() == 1 ? ": " : "s: " );
			for( int idx = 0; idx < unknownArgs.size(); idx++ )
			{
				message.append( unknownArgs.get( idx ) );
				if( idx + 2 < unknownArgs.size() )
					message.append( ", " );
				else if( idx + 1 < unknownArgs.size() )
					message.append( " and " );
			}
			throw new CmdArgException( message.toString() );
		}

		// were there any invalid parameters
		if( !invalidParams.isEmpty() )
		{
			// need to throw an exception with an informative error message
			StringBuilder message = new StringBuilder();
			message.append( "Invalid parameter" );
			message.append( invalidParams.size() == 1 ? ": " : "s:\n" );
			for( Entry<CmdLineArgument,ValidationResult> invalidParam : invalidParams.entrySet() )
			{
				CmdLineArgument cmdLineArgument = invalidParam.getKey();
				ValidationResult validationResult = invalidParam.getValue();
				String argName =
				    cmdLineArgument.hasShortForm() ? "-" + cmdLineArgument.shortForm.toString()
				                                   : "--" + cmdLineArgument.longForm;
				message.append( "The value for " + argName + " was not valid. " );
				message.append( validationResult.getMessage() );
				message.append( "\n" );
			}
			throw new CmdArgException( message.toString() );
		}
	}

	/**
	 * Create a simple usage string showing how the command should be used
	 *
	 * @param command the name of the command
	 * @return a simple usage string showing how the command should be used
	 */
	public String getUsage( String command )
	{
		CmdLineArgument[] allCmdLineArguments = collectAllCommandLineArguments();
		Arrays.sort( allCmdLineArguments, new CmdLineArgumentComparator() );

		StringBuilder usage = new StringBuilder( command );
		for( CmdLineArgument cmdLineArgument : allCmdLineArguments )
		{
			usage.append( " " );
			usage.append( cmdLineArgument.getUsageString() );
		}
		return usage.toString();
	}

	/**
	 * Create a verbose help string showing the various command line options
	 *
	 * TODO look at formatting the output a bit more intelligently
	 *
	 * @return a verbose help string showing the various command line options
	 */
	public String getHelp()
	{
		CmdLineArgument[] allCmdLineArguments = collectAllCommandLineArguments();
		Arrays.sort( allCmdLineArguments, new CmdLineArgumentComparator() );

		StringBuilder help = new StringBuilder();
		for( CmdLineArgument cmdLineArgument : allCmdLineArguments )
		{
			boolean hasShortForm = cmdLineArgument.hasShortForm();
			boolean hasLongForm = cmdLineArgument.hasLongForm();
			if( ArgumentKind.SWITCH.equals( cmdLineArgument.argKind() ) )
			{
				SwitchArgument switchArgument = (SwitchArgument)cmdLineArgument;
				if( hasShortForm )
					help.append( "[-" ).append( switchArgument.shortForm ).append( "]" );
				if( hasShortForm && hasLongForm )
					help.append( ", " );
				if( hasLongForm )
					help.append( "[--" ).append( switchArgument.longForm ).append( "]" );
				if( cmdLineArgument.hasHelp() )
				{
					help.append( "\n\t" ).append( cmdLineArgument.getHelp() );
				}
			}
			else if( ArgumentKind.VALUE.equals( cmdLineArgument.argKind() ) )
			{
				ValueArgument valueArgument = (ValueArgument)cmdLineArgument;
				boolean isRequired = valueArgument.isRequired();
				if( hasShortForm )
					help.append( (isRequired ? "" : "[") )
					.append( "-" )
					.append(valueArgument.shortForm )
					.append( (isRequired ? "" : "]") );
				if( hasShortForm && hasLongForm )
					help.append( ", " );
				if( hasLongForm )
					help.append( (isRequired ? "" : "[") )
					.append( "--" )
					.append( valueArgument.longForm ).append( (isRequired ? "" : "]") );
				if( cmdLineArgument.hasHelp() )
				{
					help.append( "\n\t" )
					.append( cmdLineArgument.isRequired() ? "(REQUIRED) " 
					                                      : "(OPTIONAL) " )
					.append( cmdLineArgument.getHelp() );
				}
			}
			else if( ArgumentKind.LIST.equals( cmdLineArgument.argKind() ) )
			{
				ListArgument listArgument = (ListArgument)cmdLineArgument;
				boolean isRequired = listArgument.isRequired();
				if( hasShortForm )
					help.append( (isRequired ? "" : "[") )
					.append( "-" )
					.append( listArgument.shortForm )
					.append( (isRequired ? "" : "]") );
				if( hasShortForm && hasLongForm )
					help.append( ", " );
				if( hasLongForm )
					help.append( (isRequired ? "" : "[") )
					.append( "--" )
					.append( listArgument.longForm ).append( (isRequired ? "" : "]") );
				if( cmdLineArgument.hasHelp() )
				{
					help.append( "\n\t" )
					.append( cmdLineArgument.isRequired() ? "(REQUIRED) " 
					                                      : "(OPTIONAL) " )
					.append( cmdLineArgument.getHelp() );
				}
			}
			help.append( "\n" );
		}
		return help.toString();
	}

	/**
	 * Utility method to collect all defined command line arguments into an array
	 *
	 * @return all defined command line arguments in an array
	 */
	private CmdLineArgument[] collectAllCommandLineArguments()
	{
		Set<CmdLineArgument> all = new HashSet<>();
		all.addAll( shortFormArgMap.values() );
		all.addAll( longFormArgMap.values() );
		return all.toArray( new CmdLineArgument[0] );
	}

	/**
	 * Utility method to collect all required command line arguments into an array
	 *
	 * @return all required command line arguments in an array
	 */
	private CmdLineArgument[] collectRequiredArguments()
	{
		Set<CmdLineArgument> required = new HashSet<>();
		List<CmdLineArgument> allCmdLineArguments = new ArrayList<>();
		allCmdLineArguments.addAll( shortFormArgMap.values() );
		allCmdLineArguments.addAll( longFormArgMap.values() );
		for( CmdLineArgument cmdLineArgument : allCmdLineArguments )
		{
			if( cmdLineArgument.isRequired() )
				required.add( cmdLineArgument );
		}
		return required.toArray( new CmdLineArgument[0] );
	}

	//----------------------------------------------------------
	//                    STATIC METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// EXAMPLE MAIN //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Example usage
	 * 
	 * @param args ignored!
	 */
	public static void main( String args[] )
	{
		CmdArgProcessor cmdArgParser = new CmdArgProcessor();
		SwitchArgument theSwitch = cmdArgParser
			.addSwitchArg( 'a', "activate-thing" )
			.help( "Activate the thing" );
		ValueArgument alphabetValue = cmdArgParser
			.addValueArg( null, "alphabet" )
			.isRequired( false )
			.help( "Define Alphabet" )
			.hint( "ABCDEFG..." );
		ValueArgument bradshawValue = cmdArgParser
			.addValueArg( 'b', "bradshaw" )
			.isRequired( true )
			.help( "Set the bradshaw radius" )
			.hint( "RADIUS" );
		
		try
		{
			cmdArgParser.process( args );
			System.out.println( theSwitch.value() );
			System.out.println( alphabetValue.value() );
			System.out.println( bradshawValue.value() );
		}
		catch( CmdArgException e )
		{
			System.err.println( e.getMessage() );
			System.err.println( "Usage: " + cmdArgParser.getUsage( "mycommand" ) );
			System.err.println( cmdArgParser.getHelp() );
		}
		System.exit( 0 );
	}
}

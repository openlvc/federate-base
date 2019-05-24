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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple command line argument parser class
 *
 * Example usage:
 *
 * public static void main(String args[])
 * {
 *    CmdArgParser cmdArgParser = new CmdArgParser();
 *    SwitchArg theSwitch = cmdArgParser.addSwitchArg('a', "activate").help("Activate the thing.");
 *    ValueArg alphabetValue = cmdArgParser.addValueArg(null, "alphabet").isRequired(false).help("Define Alphabet").hint("ABCDEFG...");
 *    ListArg bradshawValues = cmdArgParser.addValueArg('b', "bradshaw").isRequired(true).help("Set the bradshaw radii").hint("RADIUS");
 *    try
 *    {
 *        cmdArgParser.parse(args);
 *        System.out.println(theSwitch.value());
 *        System.out.println(alphabetValue.value());
 *        System.out.println(bradshawValues.valuesToString());
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
public class ArgProcessor
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// used when printing usage and help messages to the console
	private static final int CONSOLE_WIDTH_DEFAULT = 80;
	private static final String INDENT = "    ";
	private static final ArgComparator ARG_COMPARATOR = new ArgComparator();

	//----------------------------------------------------------
	//                    INSTANCE VARIABLES
	//----------------------------------------------------------
	private int consoleWidth;
	// maps for looking up under short and long forms of command line arguments
	private Map<Character,Arg> shortFormArgMap;
	private Map<String,Arg> longFormArgMap;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor - nothing special here
	 */
	public ArgProcessor()
	{
		this( CONSOLE_WIDTH_DEFAULT );
	}
	
	/**
	 * Constructor - nothing special here
	 * 
	 * @param consoleWidth the width of the console (use when formatting messages intended for
	 *            printing to the console). If the width is 0 or negative the default console
	 *            width of 80 characters will be used instead.
	 */
	public ArgProcessor( int consoleWidth )
	{
		shortFormArgMap = new HashMap<>();
		longFormArgMap = new HashMap<>();
		setConsoleWidth( consoleWidth );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Add a {@link SwitchArg} to supply a boolean argument. This is implicitly optional
	 *
	 * @param shortForm the short form of the command line argument
	 * @param longForm the long form of the command line argument
	 *
	 * @return the created {@link SwitchArg}, or null if *both* short and long forms are null
	 *         or empty
	 */
	public SwitchArg addSwitchArg( Character shortForm, String longForm )
	{
		if( shortForm == null && (longForm == null || longForm.length() == 0) )
			return null;

		SwitchArg switchArgument = new SwitchArg( shortForm, longForm );

		if( switchArgument.hasShortForm() )
			shortFormArgMap.put( shortForm, switchArgument );
		if( switchArgument.hasLongForm() )
			longFormArgMap.put( longForm, switchArgument );

		return switchArgument;
	}

	/**
	 * Add a {@link ValueArg} to supply a string argument.
	 *
	 * @param shortForm the short form of the command line argument
	 * @param longForm the long form of the command line argument
	 *
	 * @return the created {@link ValueArg}, or null if *both* short and long forms are null
	 *         or empty
	 */
	public ValueArg addValueArg( Character shortForm, String longForm )
	{
		if( shortForm == null && (longForm == null || longForm.length() == 0) )
			return null;

		ValueArg valueArgument = new ValueArg( shortForm, longForm );

		if( valueArgument.hasShortForm() )
			shortFormArgMap.put( shortForm, valueArgument );
		if( valueArgument.hasLongForm() )
			longFormArgMap.put( longForm, valueArgument );

		return valueArgument;
	}

	/**
	 * Add a {@link ListArg} to supply one or more string arguments under the same argument
	 *
	 * @param shortForm the short form of the command line argument
	 * @param longForm the long form of the command line argument
	 *
	 * @return the created {@link ListArg}, or null if *both* short and long forms are null
	 *         or empty
	 */
	public ListArg addListArg( Character shortForm, String longForm )
	{
		if( shortForm == null && (longForm == null || longForm.length() == 0) )
			return null;

		ListArg listArgument = new ListArg( shortForm, longForm );

		if( listArgument.hasShortForm() )
			shortFormArgMap.put( shortForm, listArgument );
		if( listArgument.hasLongForm() )
			longFormArgMap.put( longForm, listArgument );

		return listArgument;
	}
	
	/**
	 * Sets the width of the console - used when formatting help and error messages for printing
	 * 
	 * NOTE: if the width is 0 or negative, the default console width of 80 characters will be
	 * used instead.
	 * 
	 * @param width the console width (in characters)
	 * @return this instance
	 */
	public ArgProcessor setConsoleWidth( int width )
	{
		this.consoleWidth = width <= 0 ? CONSOLE_WIDTH_DEFAULT : width;
		return this;
	}

	/**
	 * Obtain the current console width setting used when formatting help and error messages for
	 * printing
	 * 
	 * @return the current console width setting (in characters)
	 */
	public int getConsoleWidth()
	{
		return this.consoleWidth;
	}
	
	/**
	 * Parse the provided command line arguments using the defined {@link SwitchArg}s and
	 * {@link ValueArg}s
	 *
	 * @param args the command line arguments to be parsed
	 * @throws ArgException if there are unrecognised command line arguments, or missing
	 *             required command line arguments, or arguments which do not pass validation 
	 */
	public void process( String[] args ) throws ArgException
	{
		// reset everything
		for( Arg cmdLineArgument : collectAllCommandLineArguments() )
			cmdLineArgument.reset();

		// get a list of required command line arguments so that we can check them off
		// and make sure they are all specified
		List<Arg> requiredArgList = Arrays.asList( collectRequiredArguments() );
		Set<Arg> requiredArgs = new HashSet<>( requiredArgList );
		Map<Arg,ValidationResult> invalidParams = new HashMap<>();
		// make a list to contain anything we came across that we don't recognise
		List<String> unknownArgs = new ArrayList<>();

		int longFormPrefixLen = Arg.LONG_FORM_PREFIX.length();
		int shortFormPrefixLen = Arg.SHORT_FORM_PREFIX.length();
		// parse the command line arguments
		for( int i = 0; i < args.length; i++ )
		{
			String arg = args[i];
			boolean isShortForm = arg.startsWith( Arg.SHORT_FORM_PREFIX ) && 
								  arg.length() == ( shortFormPrefixLen + 1 );
			boolean isLongForm = arg.startsWith( Arg.LONG_FORM_PREFIX ) && 
								 arg.length() > longFormPrefixLen;
			if( isLongForm || isShortForm )
			{
				Arg cmdLineArgument = null;
				if( isLongForm )
					cmdLineArgument = longFormArgMap.get( arg.substring( longFormPrefixLen ) );
				else if( isShortForm )
					cmdLineArgument = shortFormArgMap.get( arg.charAt( shortFormPrefixLen ) );

				ArgKind argKind = cmdLineArgument != null ? cmdLineArgument.argKind() 
				                                          : ArgKind.UNKNOWN;
				// handle the argument as required
				if( ArgKind.VALUE.equals( argKind ) || 
					ArgKind.LIST.equals( argKind ) )
				{
					// ValueArgument or ListArgument, so we need to grab the next item 
					// as the value for it
					if( i + 1 < args.length )
					{
						// get the value
						ValidationResult result = cmdLineArgument.parse( args[i + 1] ).validate();
						if( !result.isValid )
							invalidParams.put( cmdLineArgument, result );
						// skip over the value so we don't parse it as the next argument
						i++;
						// tick off the argument from the required list (if it's there)
						requiredArgs.remove( cmdLineArgument );
					}
				}
				else if( ArgKind.SWITCH.equals( argKind ) )
				{
					// switch argument - just set it now
					((SwitchArg)cmdLineArgument).set( true );
				}
				else
				{
					// unknown argument!
					String argName = arg.substring( isLongForm ? longFormPrefixLen : shortFormPrefixLen );
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
			message.append( "Missing required " )
				   .append( pluralize("argument", requiredArgs.size(), "s") )
				   .append( " " );
			Arg[] requiredArgsArray = requiredArgs.toArray( new Arg[0] );
			for( int idx = 0; idx < requiredArgsArray.length; idx++ )
			{
				Arg cmdLineArgument = requiredArgsArray[idx];
				String argName = cmdLineArgument.makePrefixedArg( true );
				message.append( argName );
				if( idx + 2 < requiredArgsArray.length )
					message.append( ", " );
				else if( idx + 1 < requiredArgsArray.length )
					message.append( " and " );
			}
			throw new ArgException( message.toString() );
		}

		// were there any unknown arguments encountered?
		if( !unknownArgs.isEmpty() )
		{
			// need to throw an exception with an informative error message
			StringBuilder message = new StringBuilder();
			message.append( "Unknown " )
				   .append( pluralize("argument", unknownArgs.size(), "s") )
				   .append( " " );
			for( int idx = 0; idx < unknownArgs.size(); idx++ )
			{
				message.append( unknownArgs.get( idx ) );
				if( idx + 2 < unknownArgs.size() )
					message.append( ", " );
				else if( idx + 1 < unknownArgs.size() )
					message.append( " and " );
			}
			throw new ArgException( message.toString() );
		}

		// were there any invalid parameters
		if( !invalidParams.isEmpty() )
		{
			// need to throw an exception with an informative error message
			StringBuilder message = new StringBuilder();
			message.append( "Invalid " )
				   .append( pluralize("parameter", invalidParams.size(), "s") )
				   .append( ":\n" );
			for( Entry<Arg,ValidationResult> invalidParam : invalidParams.entrySet() )
			{
				Arg cmdLineArgument = invalidParam.getKey();
				ValidationResult validationResult = invalidParam.getValue();
				String argName = cmdLineArgument.makePrefixedArg( true );
				String validationMessage = validationResult.getMessage();
				validationMessage = "The value for " + argName + " was not valid. " + validationMessage;
				List<String> lines = wrap( validationMessage, this.consoleWidth - INDENT.length() );
				for(String line : lines)
				{
					message.append( INDENT ).append( line ).append( "\n" );
				}
			}
			throw new ArgException( message.toString() );
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
		Arg[] allCmdLineArguments = collectAllCommandLineArguments();
		Arrays.sort( allCmdLineArguments, ARG_COMPARATOR );

		StringBuilder usage = new StringBuilder( command );
		for( Arg cmdLineArgument : allCmdLineArguments )
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
		Arg[] allCmdLineArguments = collectAllCommandLineArguments();
		Arrays.sort( allCmdLineArguments, ARG_COMPARATOR );

		StringBuilder help = new StringBuilder();
		for( Arg cmdLineArgument : allCmdLineArguments )
		{
			boolean hasShortForm = cmdLineArgument.hasShortForm();
			boolean hasLongForm = cmdLineArgument.hasLongForm();
			if( ArgKind.SWITCH.equals( cmdLineArgument.argKind() ) )
			{
				SwitchArg switchArgument = (SwitchArg)cmdLineArgument;
				help.append( "[");
				if( hasShortForm )
					help.append( switchArgument.makePrefixedArg( false ) );
				if( hasShortForm && hasLongForm )
					help.append( ", " );
				if( hasLongForm )
					help.append( switchArgument.makePrefixedArg( true ) );
				help.append( "]");
				if( cmdLineArgument.hasHelp() )
				{
					String helpText = cmdLineArgument.getHelp();
					helpText = "(OPTIONAL) " + helpText;
					List<String> helpLines = wrap(helpText, (this.consoleWidth - INDENT.length()) );
					for(String line : helpLines)
					{
						help.append( "\n" ).append( INDENT ).append( line );
					}
				}
			}
			else if( ArgKind.VALUE.equals( cmdLineArgument.argKind() ) )
			{
				ValueArg valueArgument = (ValueArg)cmdLineArgument;
				boolean isRequired = valueArgument.isRequired();
				help.append( (isRequired ? "" : "[") );
				if( hasShortForm )
					help.append( valueArgument.makePrefixedArg( false ) );
				if( hasShortForm && hasLongForm )
					help.append( ", " );
				if( hasLongForm )
					help.append( valueArgument.makePrefixedArg( true ) );
				help.append( (isRequired ? "" : "]") );
				if( cmdLineArgument.hasHelp() )
				{
					String helpText = cmdLineArgument.getHelp();
					helpText = (cmdLineArgument.isRequired() ? "(REQUIRED) " 
						                                     : "(OPTIONAL) ") + helpText;
					List<String> helpLines = wrap(helpText, (this.consoleWidth - INDENT.length()) );
					for(String line : helpLines)
						help.append( "\n" ).append( INDENT ).append( line );
				}
			}
			else if( ArgKind.LIST.equals( cmdLineArgument.argKind() ) )
			{
				ListArg listArgument = (ListArg)cmdLineArgument;
				boolean isRequired = listArgument.isRequired();
				help.append( (isRequired ? "" : "[") );
				if( hasShortForm )
					help.append( listArgument.makePrefixedArg( false ) );
				if( hasShortForm && hasLongForm )
					help.append( ", " );
				if( hasLongForm )
					help.append( listArgument.makePrefixedArg( true ) );
				help.append( (isRequired ? "" : "]") );
				if( cmdLineArgument.hasHelp() )
				{
					String helpText = cmdLineArgument.getHelp();
					helpText = (cmdLineArgument.isRequired() ? "(REQUIRED) " 
						                                     : "(OPTIONAL) ") + helpText;
					List<String> helpLines = wrap(helpText, (this.consoleWidth - INDENT.length()) );
					for(String line : helpLines)
						help.append( "\n" ).append( INDENT ).append( line );
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
	private Arg[] collectAllCommandLineArguments()
	{
		Set<Arg> all = new HashSet<>();
		all.addAll( shortFormArgMap.values() );
		all.addAll( longFormArgMap.values() );
		return all.toArray( new Arg[0] );
	}

	/**
	 * Utility method to collect all required command line arguments into an array
	 *
	 * @return all required command line arguments in an array
	 */
	private Arg[] collectRequiredArguments()
	{
		Set<Arg> required = new HashSet<>();
		required.addAll( shortFormArgMap.values()
		                 				.parallelStream()
		                 				.filter( arg -> arg.isRequired() )
		                 				.collect( Collectors.toList() ) );
		required.addAll( longFormArgMap.values()
		                 			   .parallelStream()
		                 			   .filter( arg -> arg.isRequired() )
		                 			   .collect( Collectors.toList() ) );
		return required.toArray( new Arg[0] );
	}
	
	/**
	 * Utility method to allow simple pluralization of text if the count is not 1. By default, use 's'
	 * as the pluralization suffix:
	 * Usage examples:
	 * 
	 *   - pluralize("vote", 0, null) returns "votes"
	 *   - pluralize("vote", 1, null) returns "vote"
	 *   - pluralize("vote", 2, null) returns "votes"
	 *   
	 *   - pluralize("class", 0, "es") returns "classes"
	 *   - pluralize("class", 1, "es") returns "class"
	 *   - pluralize("class", 2, "es") returns "classes"
	 *   
	 *   - pluralize("cand", 0, "y,ies") returns "candies"
	 *   - pluralize("cand", 1, "y,ies") returns "candy"
	 *   - pluralize("cand", 2, "y,ies") returns "candies"
	 *   
	 * @param stem the stem of the word
	 * @param count the count
	 * @param suffix the pluralizing suffix specifier
	 * @return the pluralized word
	 */
	private String pluralize( String stem, int count, String suffix )
	{
		String singularSuffix = "";
		String pluralSuffix = (suffix == null ? "s" : suffix);
		String[] bits = suffix.split( "," );
		if( bits.length > 2 )
		{
			return "";
		}
		else if( bits.length == 2 )
		{
			singularSuffix = bits[0];
			pluralSuffix = bits[1];
		}
		return (stem == null ? "" : stem) + (count == 1 ? singularSuffix : pluralSuffix);
	}

	/**
	 * A word-wrap function that preserves existing line breaks. Expects that existing line breaks
	 * are posix newlines (i.e., \n).
	 * 
	 * Preserves all white space except added line breaks consume the space on which they break the
	 * line.
	 * 
	 * NOTE: Doesn't wrap long words, thus the output text *may* have lines longer than width.
	 * 
	 * @param text the original text
	 * @param width the desired width
	 * @return the lines of the wrapped text
	 */
	private List<String> wrap( String text, int width )
	{
		List<String> lines = new ArrayList<>();
		for( String line : text.split( "\n" ) )
		{
			if( line.length() < width )
			{
				lines.add( line );
			}
			else
			{
				while( line.length() > width )
				{
					// look backwards from the width index to find the
					// last space character
					int spacePos = line.substring( 0, width ).lastIndexOf( ' ' ) + 1;
					if( spacePos == 0 )
					{
						// no space character found, look from the start of 
						// the string instead and see how we go with that
						spacePos = line.indexOf( ' ' ) + 1;
					}
					if( spacePos == 0 )
					{
						// still no space character found - can't split!
						// just add the line as it is without a split
						lines.add( line );
						line = "";
					}
					else
					{
						lines.add( line.substring( 0, spacePos - 1 ) );
						line = line.substring( spacePos );
					}
				}
				if( line.length() > 0 )
				{
					lines.add( line );
				}
			}
		}
		return lines;
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
	 * @param args the command line arguments
	 */
	public static void main( String args[] )
	{
		ArgProcessor cmdArgParser = new ArgProcessor();
		SwitchArg theSwitch = cmdArgParser
			.addSwitchArg( 'a', "activate-thing" )
			.help( "Activate the thing" );
		ValueArg alphabetValue = cmdArgParser
			.addValueArg( null, "alphabet" )
			.isRequired( false )
			.defaultValue( "ABCDEFGHIJKLMNOPQRSTUVWXYZ" )
			.help( "Define Alphabet" )
			.hint( "ABCDEFG..." );
		ListArg bradshawValues = cmdArgParser
			.addListArg( 'b', "bradshaw" )
			.isRequired( true )
			.validator( StdValidators.CheckIntList )
			.help( "Set the bradshaw radii values." )
			.hint( "RADIUS" );
		
		try
		{
			cmdArgParser.process( args );
			System.out.println( theSwitch.value() );
			System.out.println( alphabetValue.value() );
			System.out.println( bradshawValues.valuesToString( ", " ) );
		}
		catch( ArgException e )
		{
			System.err.println( e.getMessage() );
			System.err.println( "Usage: " + cmdArgParser.getUsage( "mycommand" ) );
			System.err.println( cmdArgParser.getHelp() );
		}
		System.exit( 0 );
	}
}

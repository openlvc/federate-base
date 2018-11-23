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
package gov.nist.ucef.hla.example.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
public class CmdArgParser
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	public enum ArgumentKind
	{
		SWITCH, VALUE, LIST,
		UNKNOWN
	};
	
	public static class ValidationResult
	{
		
		private boolean isValid;
		private String msg;

		public ValidationResult(boolean isValid, String msg)
		{
			this.isValid = isValid;
			this.msg = msg;
		}
		
		public boolean isValid()
		{
			return this.isValid;
		}
		
		public String getMessage()
		{
			return this.msg;
		}
	}
	
	public interface Validator
	{
		public ValidationResult validate( Object value );
	}
	
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------
	public final static ValidationResult GENERIC_SUCCESS = new ValidationResult( true, "" );

    //----------------------------------------------------------
    //                    INSTANCE VARIABLES
    //----------------------------------------------------------
    // maps for looking up under short and long forms of command line arguments
    private Map<Character, CmdLineArgument> shortFormArgMap;
    private Map<String, CmdLineArgument> longFormArgMap;

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    /**
     * Constructor - nothing special here
     */
    public CmdArgParser() {
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
     * @return the created {@link SwitchArgument}, or null if *both* short and long forms are null or empty
     */
    public SwitchArgument addSwitchArg(Character shortForm, String longForm) {
        if (shortForm == null && (longForm == null || longForm.length() == 0))
            return null;

        SwitchArgument switchArgument = new SwitchArgument(shortForm, longForm);

        if (switchArgument.hasShortForm())
            shortFormArgMap.put(shortForm, switchArgument);
        if (switchArgument.hasLongForm())
            longFormArgMap.put(longForm, switchArgument);

        return switchArgument;
    }

    /**
     * Add a {@link ValueArgument} to supply a string argument.
     *
     * @param shortForm the short form of the command line argument
     * @param longForm the long form of the command line argument
     *
     * @return the created {@link ValueArgument}, or null if *both* short and long forms are null or empty
     */
    public ValueArgument addValueArg(Character shortForm, String longForm) {
        if (shortForm == null && (longForm == null || longForm.length() == 0))
            return null;

        ValueArgument valueArgument = new ValueArgument(shortForm, longForm);

        if (valueArgument.hasShortForm())
            shortFormArgMap.put(shortForm, valueArgument);
        if (valueArgument.hasLongForm())
            longFormArgMap.put(longForm, valueArgument);

        return valueArgument;
    }

    /**
     * Add a {@link ListArgument} to supply one or more string arguments under the same argument
     *
     * @param shortForm the short form of the command line argument
     * @param longForm the long form of the command line argument
     *
     * @return the created {@link ListArgument}, or null if *both* short and long forms are null or empty
     */
    public ListArgument addListArg(Character shortForm, String longForm) {
    	if (shortForm == null && (longForm == null || longForm.length() == 0))
    		return null;
    	
    	ListArgument listArgument = new ListArgument(shortForm, longForm);
    	
    	if (listArgument.hasShortForm())
    		shortFormArgMap.put(shortForm, listArgument);
    	if (listArgument.hasLongForm())
    		longFormArgMap.put(longForm, listArgument);
    	
    	return listArgument;
    }
    
    /**
     * Parse the provided command line arguments using the defined {@link SwitchArgument}s and {@link ValueArgument}s
     *
     * @param args the command line arguments to be parsed
     * @throws ParseException if there are unrecognised command line arguments, or missing required
     *         command line arguments
     */
    public void parse(String[] args) throws ParseException {
        // reset everything
        for(CmdLineArgument cmdLineArgument : collectAllCommandLineArguments())
            cmdLineArgument.reset();

        // get a list of required command line arguments so that we can check them off
        // and make sure they are all specified
        List<CmdLineArgument> requiredArgList = Arrays.asList(collectRequiredCommandLineArguments());
        Set<CmdLineArgument> requiredArgs = new HashSet<>(requiredArgList);
        Map<CmdLineArgument, ValidationResult> invalidParams = new HashMap<>();
        // make a list to contain anything we came across that we don't recognise
        List<String> unknownArgs = new ArrayList<>();

        // parse the command line arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.charAt(0) == '-' && arg.length() > 1) {
                // figure out if it's a short or long form version of the argument,
                // and look it up in the tables as appropriate
                CmdLineArgument cmdLineArgument = null;
                if (arg.charAt(1) == '-' && arg.length() > 2)
                    cmdLineArgument = longFormArgMap.get(arg.substring(2));
                else if (arg.charAt(1) != '-' && arg.length() == 2)
                    cmdLineArgument = shortFormArgMap.get(arg.charAt(1));

                ArgumentKind argKind = cmdLineArgument != null ? cmdLineArgument.argKind()
                                                               : ArgumentKind.UNKNOWN ;
                // handle the argument as required
                if (ArgumentKind.VALUE.equals( argKind ) || 
                	ArgumentKind.LIST.equals( argKind )) {
                    // ValueArgument or ListArgument, so we need to grab the next item as the value for it
                    if (i + 1 < args.length) {
                        // get the value
                    	if(ArgumentKind.VALUE.equals( argKind ))
                    	{
                    		ValidationResult result = ((ValueArgument) cmdLineArgument).parse(args[i + 1]).validate();
                    		if(!result.isValid)
                    			invalidParams.put( cmdLineArgument, result );
                    	}
                    	else
                    	{
                    		ValidationResult result = ((ListArgument) cmdLineArgument).parse(args[i + 1]).validate();
                    		if(!result.isValid)
                    			invalidParams.put( cmdLineArgument, result );
                    	}
                        // skip over the value so we don't parse it as the next argument
                        i++;
                        // tick off the argument from the required list (if it's there)
                        requiredArgs.remove(cmdLineArgument);
                    }
                } else if (ArgumentKind.SWITCH.equals( argKind )) {
                    // switch argument - just set it now
                    ((SwitchArgument) cmdLineArgument).set(true);
                } else {
                    // unknown argument!
                    String argName = arg.substring(arg.lastIndexOf('-') + 1);
                    if (!"".equals(argName))
                        unknownArgs.add(argName);
                }
            } else {
                // all command line arguments must be prefixed with either a '-' or '--', but
                // this one wasn't so we don't know what to do.
                unknownArgs.add(arg);
            }
        }

        // is there anything left in the required arguments that didn't get ticked off?
        if (!requiredArgs.isEmpty()) {
            // need to throw an exception with an informative error message
            StringBuilder message = new StringBuilder();
            message.append("Missing required argument");
            message.append(requiredArgs.size() == 1 ? ": " : "s: ");
            CmdLineArgument[] requiredArgsArray = requiredArgs.toArray(new CmdLineArgument[0]);
            for (int idx = 0; idx < requiredArgsArray.length; idx++) {
                CmdLineArgument cmdLineArgument = requiredArgsArray[idx];
                String argName = cmdLineArgument.hasShortForm() ? "-"+cmdLineArgument.shortForm.toString() 
                												: "--"+cmdLineArgument.longForm;
                message.append(argName);
                if (idx + 2 < requiredArgsArray.length)
                    message.append(", ");
                else if (idx + 1 < requiredArgsArray.length)
                    message.append(" and ");
            }
            throw new ParseException(message.toString());
        }
        
        // were there any unknown arguments encountered?
        if (!unknownArgs.isEmpty()) {
            // need to throw an exception with an informative error message
            StringBuilder message = new StringBuilder();
            message.append("Unknown argument");
            message.append(unknownArgs.size() == 1 ? ": " : "s: ");
            for (int idx = 0; idx < unknownArgs.size(); idx++) {
                message.append(unknownArgs.get(idx));
                if (idx + 2 < unknownArgs.size())
                    message.append(", ");
                else if (idx + 1 < unknownArgs.size())
                    message.append(" and ");
            }
            throw new ParseException(message.toString());
        }
        
        // were there any invalid parameters
        if (!invalidParams.isEmpty()) {
        	// need to throw an exception with an informative error message
        	StringBuilder message = new StringBuilder();
        	message.append("Invalid parameter");
        	message.append(invalidParams.size() == 1 ? ": " : "s:\n");
			for( Entry<CmdLineArgument,ValidationResult> invalidParam : invalidParams.entrySet() )
			{
				CmdLineArgument cmdLineArgument = invalidParam.getKey();
				ValidationResult validationResult = invalidParam.getValue();
				String argName = cmdLineArgument.hasShortForm() ? "-"+cmdLineArgument.shortForm.toString()
				                                                : "--"+cmdLineArgument.longForm;
				message.append( "The value for " + argName + " was not valid. " );
				message.append( validationResult.getMessage() );
				message.append( "\n" );
			}
        	throw new ParseException(message.toString());
        }
    }

    /**
     * Create a simple usage string showing how the command should be used
     *
     * @param command the name of the command
     * @return a simple usage string showing how the command should be used
     */
    public String getUsage(String command)
    {
        CmdLineArgument[] allCmdLineArguments = collectAllCommandLineArguments();
        Arrays.sort(allCmdLineArguments, new CmdLineArgumentComparator());

        StringBuilder usage = new StringBuilder(command);
        for(CmdLineArgument cmdLineArgument : allCmdLineArguments)
        {
            usage.append(" ");
            usage.append(cmdLineArgument.getUsageString());
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
        Arrays.sort(allCmdLineArguments, new CmdLineArgumentComparator());

        StringBuilder help = new StringBuilder();
        for(CmdLineArgument cmdLineArgument : allCmdLineArguments)
        {
            boolean hasShortForm = cmdLineArgument.hasShortForm();
            boolean hasLongForm = cmdLineArgument.hasLongForm();
            if(ArgumentKind.SWITCH.equals( cmdLineArgument.argKind() ) )
            {
                SwitchArgument switchArgument = (SwitchArgument)cmdLineArgument;
                if(hasShortForm)
                    help.append("[-").append(switchArgument.shortForm).append("]");
                if(hasShortForm && hasLongForm)
                    help.append(", ");
                if(hasLongForm)
                    help.append("[--").append(switchArgument.longForm).append("]");
                if(cmdLineArgument.hasHelp())
                {
                    help.append("\n\t").append(cmdLineArgument.getHelp());
                }
            }
            else if(ArgumentKind.VALUE.equals( cmdLineArgument.argKind() ) )
            {
                ValueArgument valueArgument = (ValueArgument)cmdLineArgument;
                boolean isRequired = valueArgument.isRequired();
                if(hasShortForm)
                    help.append((isRequired?"":"[")).append("-").append(valueArgument.shortForm).append((isRequired?"":"]"));
                if(hasShortForm && hasLongForm)
                    help.append(", ");
                if(hasLongForm)
                    help.append((isRequired?"":"[")).append("--").append(valueArgument.longForm).append((isRequired?"":"]"));
                if(cmdLineArgument.hasHelp())
                {
                    help.append("\n\t").append(cmdLineArgument.isRequired()?"(REQUIRED) ":"(OPTIONAL) ").append(cmdLineArgument.getHelp());
                }
            }
            else if(ArgumentKind.LIST.equals( cmdLineArgument.argKind() ) )
            {
            	ListArgument listArgument = (ListArgument)cmdLineArgument;
            	boolean isRequired = listArgument.isRequired();
            	if(hasShortForm)
            		help.append((isRequired?"":"[")).append("-").append(listArgument.shortForm).append((isRequired?"":"]"));
            	if(hasShortForm && hasLongForm)
            		help.append(", ");
            	if(hasLongForm)
            		help.append((isRequired?"":"[")).append("--").append(listArgument.longForm).append((isRequired?"":"]"));
                if(cmdLineArgument.hasHelp())
                {
                    help.append("\n\t").append(cmdLineArgument.isRequired()?"(REQUIRED) ":"(OPTIONAL) ").append(cmdLineArgument.getHelp());
                }
            }
            help.append("\n");
        }
        return help.toString();
    }

    /**
     * Utility method to collect all defined command line arguments into an array
     *
     * @return all defined command line arguments in an array
     */
    private CmdLineArgument[] collectAllCommandLineArguments() {
        Set<CmdLineArgument> all = new HashSet<>();
        all.addAll(shortFormArgMap.values());
        all.addAll(longFormArgMap.values());
        return all.toArray(new CmdLineArgument[0]);
    }
    /**
     * Utility method to collect all optional command line arguments into an array
     *
     * @return all optional command line arguments in an array
     */
    private CmdLineArgument[] collectOptionalCommandLineArguments() {
        Set<CmdLineArgument> optional = new HashSet<>();
        List<CmdLineArgument> allCmdLineArguments = new ArrayList<>();
        allCmdLineArguments.addAll(shortFormArgMap.values());
        allCmdLineArguments.addAll(longFormArgMap.values());
        for (CmdLineArgument cmdLineArgument : allCmdLineArguments) {
            if (cmdLineArgument instanceof ValueArgument) {
                if (!((ValueArgument) cmdLineArgument).isRequired()) {
                    optional.add(cmdLineArgument);
                }
            }
            else {
                // a SwitchArgument is implicitly optional
                optional.add(cmdLineArgument);
            }
        }
        return optional.toArray(new CmdLineArgument[0]);
    }

    /**
     * Utility method to collect all required command line arguments into an array
     *
     * @return all required command line arguments in an array
     */
    private CmdLineArgument[] collectRequiredCommandLineArguments() {
        Set<CmdLineArgument> required = new HashSet<>();
        List<CmdLineArgument> allCmdLineArguments = new ArrayList<>();
        allCmdLineArguments.addAll(shortFormArgMap.values());
        allCmdLineArguments.addAll(longFormArgMap.values());
        for (CmdLineArgument cmdLineArgument : allCmdLineArguments) {
            if (cmdLineArgument.isRequired())
                required.add(cmdLineArgument);
        }
        return required.toArray(new CmdLineArgument[0]);
    }

    //----------------------------------------------------------
    //                    STATIC METHODS
    //----------------------------------------------------------

    /**
     * An Exception to use when command line parsing fals for some reason
     */
    public class ParseException extends Exception {
        //----------------------------------------------------------
        //                    STATIC VARIABLES
        //----------------------------------------------------------

        //----------------------------------------------------------
        //                    INSTANCE VARIABLES
        //----------------------------------------------------------

        //----------------------------------------------------------
        //                      CONSTRUCTORS
        //----------------------------------------------------------
        /**
         * Constructor
         * @param message the message describing the situation which caused this exception to occur
         */
        public ParseException(String message) {
            super(message);
        }

        //----------------------------------------------------------
        //                    INSTANCE METHODS
        //----------------------------------------------------------
    }

    /**
     * Utility class which simpy implements the {@link Comparator} interface to allow comparison of
     * {@link CmdLineArgument} instances for the purposes of sorting
     */
    private class CmdLineArgumentComparator implements Comparator<CmdLineArgument>
    {
        //----------------------------------------------------------
        //                    STATIC VARIABLES
        //----------------------------------------------------------

        //----------------------------------------------------------
        //                    INSTANCE VARIABLES
        //----------------------------------------------------------

        //----------------------------------------------------------
        //                      CONSTRUCTORS
        //----------------------------------------------------------

        //----------------------------------------------------------
        //                    INSTANCE METHODS
        //----------------------------------------------------------
        @Override
        public int compare(CmdLineArgument cla1, CmdLineArgument cla2) {
        	// return...
        	//   *  -ve if the first value is less than the second 
        	//   *  0 if the first value is equal to second 
        	//   *  +ve if the first value is more than the second
        	
            // basically we are trying to sort so that optional arguments come
            // after required arguments, and otherwise sort alphabetically by
            // the short form or the argument if it has one, and the long form
            // if not
        	
        	// required arguments before optional arguments
        	if( cla1.isRequired() && !cla2.isRequired())
        	{
        		return -1;
        	}
        	else if( !cla1.isRequired() && cla2.isRequired())
        	{
        		return 1;
        	}
        	
        	// sort by type - SWITCH before VALUE before LIST
        	int ord1 = cla1.argKind().ordinal(); 
        	int ord2 = cla2.argKind().ordinal();
        	int diff = ord2 - ord1;
        	if(diff != 0)
        		return diff;
        	
        	// sort by short/long form of argument
            String o1Str = cla1.hasShortForm()?cla1.shortForm.toString():cla1.longForm;
            String o2Str = cla2.hasShortForm()?cla2.shortForm.toString():cla2.longForm;
            return o1Str.compareTo(o2Str);
        }
    }

    /**
     * Abstract class to provide the basic functionality for any command line argument
     */
    private abstract class CmdLineArgument {
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
         * @param shortForm the short form of the argument
         * @param longForm the long form of the argument
         */
        public CmdLineArgument(Character shortForm, String longForm) {
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
         * @return the kind of argument this is
         */
        public abstract ArgumentKind argKind();
        
        /**
         * Determine if this value argument is required
         * @return true if this value argument is required, false otherwise
         */
        public abstract boolean isRequired();

        /**
         * Determine if this argument has a short form
         * @return true if this argument has a short form, false otherwise
         */
        public boolean hasShortForm() {
            return this.shortForm != null;
        }

        /**
         * Determine if this argument has a long form
         * @return true if this argument has a long form, false otherwise
         */
        public boolean hasLongForm() {
            return this.longForm != null && this.longForm.length() > 0;
        }
        
        /**
         * Determine if this argument has help text associated with it
         * @return true if this argument has help text associated with it, false otherwise
         */
        public boolean hasHelp() {
            return this.help != null && this.help.length() > 0;
        }

        /**
         * Set the help text associated with this argument
         * @param help the help text to associate with this argument
         */
        protected void setHelp(String help) {
            this.help = help;
        }

        /**
         * Obtain the help text associated with this argument
         * @return the help text associated with this argument
         */
        protected String getHelp() {
            return this.help;
        }

        /**
         * Obtain a basic usage string associated with this argument
         * @return a basic usage string associated with this argument
         */
        public String getUsageString() {
            StringBuilder usage = new StringBuilder();
            usage.append('-');
            if(hasShortForm()) {
                usage.append(shortForm);
            }
            else {
                usage.append('-');
                usage.append(longForm);
            }
            return usage.toString();
        }
    }

    /**
     * Class to provide the functionality for 'switch' command line arguments
     */
    public class SwitchArgument extends CmdLineArgument {
        //----------------------------------------------------------
        //                    STATIC VARIABLES
        //----------------------------------------------------------

        //----------------------------------------------------------
        //                    INSTANCE VARIABLES
        //----------------------------------------------------------
    	private final ArgumentKind argKind = ArgumentKind.SWITCH;
        private boolean defaultValue;
        private boolean actualValue;

        //----------------------------------------------------------
        //                      CONSTRUCTORS
        //----------------------------------------------------------
        /**
         * Constructor
         * @param shortForm the short form of the switch argument
         * @param longForm the long form of the switch argument
         */
        public SwitchArgument(Character shortForm, String longForm) {
            super(shortForm, longForm);
            defaultValue = false;
        }

        //----------------------------------------------------------
        //                    INSTANCE METHODS
        //----------------------------------------------------------
        @Override
        protected void reset() {
            this.actualValue = this.defaultValue;
        }

        @Override
        public ArgumentKind argKind() {
        	return this.argKind;
        }
        
        @Override
        public boolean isRequired() {
        	return false;
        }
        
        /**
         * Set the state of this switch
         * @param value the state of this switch
         */
        protected void set(boolean value) {
            this.actualValue = value;
        }

        /**
         * Obtain the state of this switch
         * @return the state of this switch
         */
        public boolean value() {
            return this.actualValue;
        }

        /**
         * Set the default value of this switch if it is not specified
         * @param defaultValue the default value of this switch if it is not specified
         * @return this instance, to facilitate method chaining
         */
        public SwitchArgument defaultValue(boolean defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Obtain the default value of this switch
         * @return the default value of this switch
         */
        public boolean defaultValue() {
            return this.defaultValue;
        }

        /**
         * Set the help text for this switch
         * @param help the help text for this switch
         * @return this instance, to facilitate method chaining
         */
        public SwitchArgument help(String help) {
            super.setHelp(help);
            return this;
        }

        /**
         * Obtain the help text for this switch
         * @return the help text for this switch
         */
        public String help() {
            return super.getHelp();
        }

        /**
         * Obtain a basic usage string for this switch
         * @return a basic usage string for this switch
         */
        public String getUsageString() {
            StringBuilder usage = new StringBuilder();
            usage.append("[");
            usage.append(super.getUsageString());
            usage.append("]");
            return usage.toString();
        }
    }

    /**
     * Private lass to provide the functionality for 'value' command line arguments
     */
    public class ValueArgument extends CmdLineArgument {
        //----------------------------------------------------------
        //                    STATIC VARIABLES
        //----------------------------------------------------------

        //----------------------------------------------------------
        //                    INSTANCE VARIABLES
        //----------------------------------------------------------
    	private final ArgumentKind argKind = ArgumentKind.VALUE;
        private boolean isRequired;
        private boolean isSet;
        private String defaultValue;
        private String actualValue;
        private String hint;

        protected Validator validator;
        
        //----------------------------------------------------------
        //                      CONSTRUCTORS
        //----------------------------------------------------------
        /**
         * Constructor
         * @param shortForm the short form of the value argument
         * @param longForm the long form of the value argument
         */
        public ValueArgument(Character shortForm, String longForm) {
            super(shortForm, longForm);
            isRequired = false;
            isSet = false;
            defaultValue = "";
            actualValue = "";
            hint = "";
            validator = null;
        }

        @Override
        protected void reset() {
            this.isSet = false;
            this.actualValue = this.defaultValue;
        }
        
        @Override
        public ArgumentKind argKind() {
        	return this.argKind;
        }

        @Override
        public boolean isRequired() {
        	return this.isRequired;
        }
        
		public ValueArgument validator( Validator validator )
		{
			this.validator = validator;
			return this;
		}

        /**
         * Parse the provided value with respect to this value argument
         * @param value the value for this value argument
         */
        protected ValueArgument parse(String value) {
            this.actualValue = value;
            this.isSet = true;
            return this;
        }
        
		public ValidationResult validate()
		{
			if( this.validator == null )
				return GENERIC_SUCCESS;
			return this.validator.validate( value() );
		}

        /**
         * Determine if a value has been provided for this value argument
         * @return true if a value has been provided for this value argument, false otherwise
         */
        public boolean isSet() {
            return this.isSet;
        }

        /**
         * Obtain the current value for this value argument
         * @return the current value for this value argument
         */
        public String value() {
            return isSet() ? this.actualValue : this.defaultValue;
        }

        /**
         * Set whether this value argument is required
         * @param isRequired true if this value argument is required, false otherwise
         */
        public ValueArgument isRequired(boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }

        /**
         * Set the default value of this value argument if it is not specified
         * @param defaultValue the default value of this value argument if it is not specified
         * @return this instance, to facilitate method chaining
         */
        public ValueArgument defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Obtain the default value of this value argument if it is not specified
         * @return the default value of this value argument if it is not specified
         */
        public String defaultValue() {
            return this.defaultValue;
        }

        /**
         * Set the help text for this value argument
         * @param help the help text for this value argument
         * @return this instance, to facilitate method chaining
         */
        public ValueArgument help(String help) {
            super.setHelp(help);
            return this;
        }

        /**
         * Obtain the help text for this value argument
         * @return the help text for this value argument
         */
        public String help() {
            return super.getHelp();
        }

        /**
         * Set the hint text for this value argument
         * @param hint the hint text for this value argument
         * @return this instance, to facilitate method chaining
         */
        public ValueArgument hint(String hint) {
            this.hint = hint;
            return this;
        }

        /**
         * Obtain the hint text for this value argument
         * @return the hint text for this value argument
         */
        public String hint() {
            return this.hint;
        }

        /**
         * Determine if this argument has hint text associated with it
         * @return true if this argument has hint text associated with it, false otherwise
         */
        public boolean hasHint() {
            return this.hint != null & this.hint.length()>0;
        }

        /**
         * Obtain a basic usage string associated with this argument
         * @return a basic usage string associated with this argument
         */
        public String getUsageString() {
            StringBuilder usage = new StringBuilder();
            if(!isRequired)
                usage.append("[");
            usage.append(super.getUsageString());
            if(hasHint())
                usage.append(" <").append(hint()).append(">");
            if(!isRequired)
                usage.append("]");
            return usage.toString();
        }
    }

    /**
     * Private lass to provide the functionality for multiple 'value' command line arguments
     */
    public class ListArgument extends CmdLineArgument {
    	//----------------------------------------------------------
    	//                    STATIC VARIABLES
    	//----------------------------------------------------------
    	
    	//----------------------------------------------------------
    	//                    INSTANCE VARIABLES
    	//----------------------------------------------------------
    	private final ArgumentKind argKind = ArgumentKind.LIST;
    	private boolean isRequired;
    	private boolean isSet;
    	private List<String> defaultValue;
    	private List<String> actualValue;
    	private String hint;
        protected Validator validator;
    	
    	//----------------------------------------------------------
    	//                      CONSTRUCTORS
    	//----------------------------------------------------------
    	/**
    	 * Constructor
    	 * @param shortForm the short form of the value argument
    	 * @param longForm the long form of the value argument
    	 */
    	public ListArgument(Character shortForm, String longForm) {
    		super(shortForm, longForm);
    		isRequired = false;
    		isSet = false;
    		defaultValue = Collections.emptyList();
    		actualValue = new ArrayList<>();
    		hint = "";
    		validator = null;
    	}
    	
    	@Override
    	protected void reset() {
    		this.isSet = false;
    		this.actualValue = new ArrayList<>();
    	}
    	
        @Override
        public ArgumentKind argKind() {
        	return this.argKind;
        }

		public ListArgument validator( Validator validator )
		{
			this.validator = validator;
			return this;
		}

		/**
    	 * Parse the provided value with respect to this value argument
    	 * @param value the value for this value argument
    	 */
    	protected ListArgument parse(String value) {
    		this.actualValue.add( value );
    		this.isSet = true;
    		return this;
    	}
    	
		public ValidationResult validate()
		{
			if( this.validator == null )
				return GENERIC_SUCCESS;
			
			return this.validator.validate( value() );
		}

    	/**
    	 * Determine if a value has been provided for this value argument
    	 * @return true if a value has been provided for this value argument, false otherwise
    	 */
    	public boolean isSet() {
    		return this.isSet;
    	}
    	
    	/**
    	 * Obtain the current value for this value argument
    	 * @return the current value for this value argument
    	 */
    	public List<String> value() {
    		return isSet() ? this.actualValue : this.defaultValue;
    	}
    	
		/**
    	 * Obtain the current value for this value argument
    	 * @return the current value for this value argument
    	 */
    	public String valueToString(String delimiter) {
    		List<String> val = isSet() ? this.actualValue : this.defaultValue;
    		
    		if(val == null)
    			return null;
    		
    		return val.stream().collect( Collectors.joining( delimiter ) );
    	}

    	/**
    	 * Set whether this value argument is required
    	 * @param isRequired true if this value argument is required, false otherwise
    	 */
    	public ListArgument isRequired(boolean isRequired) {
    		this.isRequired = isRequired;
    		return this;
    	}
    	
    	/**
    	 * Determine if this value argument is required
    	 * @return true if this value argument is required, false otherwise
    	 */
    	public boolean isRequired() {
    		return this.isRequired;
    	}
    	
    	/**
    	 * Set the default value of this value argument if it is not specified
    	 * @param defaultValue the default value of this value argument if it is not specified
    	 * @return this instance, to facilitate method chaining
    	 */
    	public ListArgument defaultValue(List<String> defaultValue) {
    		this.defaultValue = defaultValue;
    		return this;
    	}
    	
    	/**
    	 * Obtain the default value of this value argument if it is not specified
    	 * @return the default value of this value argument if it is not specified
    	 */
    	public List<String> defaultValue() {
    		return this.defaultValue;
    	}
    	
    	/**
    	 * Set the help text for this value argument
    	 * @param help the help text for this value argument
    	 * @return this instance, to facilitate method chaining
    	 */
    	public ListArgument help(String help) {
    		super.setHelp(help);
    		return this;
    	}
    	
    	/**
    	 * Obtain the help text for this value argument
    	 * @return the help text for this value argument
    	 */
    	public String help() {
    		return super.getHelp();
    	}
    	
    	/**
    	 * Set the hint text for this value argument
    	 * @param hint the hint text for this value argument
    	 * @return this instance, to facilitate method chaining
    	 */
    	public ListArgument hint(String hint) {
    		this.hint = hint;
    		return this;
    	}
    	
    	/**
    	 * Obtain the hint text for this value argument
    	 * @return the hint text for this value argument
    	 */
    	public String hint() {
    		return this.hint;
    	}
    	
    	/**
    	 * Determine if this argument has hint text associated with it
    	 * @return true if this argument has hint text associated with it, false otherwise
    	 */
    	public boolean hasHint() {
    		return this.hint != null & this.hint.length()>0;
    	}
    	
    	/**
    	 * Obtain a basic usage string associated with this argument
    	 * @return a basic usage string associated with this argument
    	 */
    	public String getUsageString() {
    		StringBuilder usage = new StringBuilder();
    		if(!isRequired)
    			usage.append("[");
    		usage.append(super.getUsageString());
    		if(hasHint())
    			usage.append(" <").append(hint()).append(">");
    		if(!isRequired)
    			usage.append("]");
    		return usage.toString();
    	}
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////// EXAMPLE MAIN //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Example usage
     * @param args ignored!
     */
    public static void main(String args[])
    {
        CmdArgParser cmdArgParser = new CmdArgParser();
        SwitchArgument theSwitch = cmdArgParser.addSwitchArg('a', "activate-thing").help("Activate the thing");
        ValueArgument alphabetValue = cmdArgParser.addValueArg(null, "alphabet").isRequired(false).help("Define Alphabet").hint("ABCDEFG...");
        ValueArgument bradshawValue = cmdArgParser.addValueArg('b', "bradshaw").isRequired(true).help("Set the bradshaw radius").hint("RADIUS");
        try
        {
            cmdArgParser.parse(args);
            System.out.println(theSwitch.value());
            System.out.println(alphabetValue.value());
            System.out.println(bradshawValue.value());
        }
        catch (ParseException e)
        {
            System.err.println(e.getMessage());
            System.err.println("Usage: " + cmdArgParser.getUsage("mycommand"));
            System.err.println(cmdArgParser.getHelp());
        }
        System.exit(0);
    }
}

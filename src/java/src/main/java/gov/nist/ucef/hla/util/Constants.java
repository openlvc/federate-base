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
package gov.nist.ucef.hla.util;

public class Constants
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	
	// the UCEF logo as ASCII art - may be shown on federate startup in console
	public static final String UCEF_LOGO =
		"            ___\n" +
		"          _/   \\_     _     _\n" +
		"         / \\   / \\   / \\   / \\\n" +
		"        ( U )─( C )─( E )─( F )\n" +
		"         \\_/   \\_/   \\_/   \\_/\n" +
		"        <─┴─> <─┴─────┴─────┴─>\n" +
		"       Universal CPS Environment\n" +
		"             for Federation\n";

	// console width for wrapping text etc 
	public static final int CONSOLE_WIDTH = 80;
	
	/** System property that identifies the value for {@link #UCEF_LOG_LEVEL}. */
	public static final String PROPERTY_UCEF_LOG_LEVEL = "ucef.loglevel";	
	/** System property for defining which directory to put the log file in */
	public static final String PROPERTY_UCEF_LOG_DIR = "ucef.logdir";	
	/** The log level to apply to all loggers under the "ucef" level.
	    This level defaults to WARN. You can alter this level through
	    a system property as long as you do before the things are loaded */
	public static String UCEF_LOG_LEVEL = "WARN";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

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

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
package gov.nist.ucef.hla.tools.fedman;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FedManConstants
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// TODO
    public static final String RESOURCES_ROOT = "src/main/resources/"; 
    public static final String FOMS_ROOT = RESOURCES_ROOT + "foms/"; 
    public static final String SOMS_ROOT = RESOURCES_ROOT + "soms/"; 
    
	// the UCEF logo as ASCII art - may be shown on federate startup in console
	public static final String UCEF_LOGO =
		"            ___\n" +
		"          _/   \\_     _     _\n" +
		"         / \\   / \\   / \\   / \\\n" +
		"        ( U )-( C )-( E )-( F )\n" +
		"         \\_/   \\_/   \\_/   \\_/\n" +
		"        <-+-> <-+-----+-----+->\n" +
		"       Universal CPS Environment\n" +
		"             for Federation\n";

	// the Federate Manager logo as ASCII art - shown on startup in console
	public static final String FEDMAN_LOGO =
		"      ______         ____  ___\n" +
		"     / ____/__  ____/ /  |/  /___  ____\n" + 
		"    / /_  / _ \\/ __  / /\\|_/ / __`/ __ \\\n" +
		"   / __/ /  __/ /_/ / /  / / /_/ / / / /\n" +
		"  /_/    \\___/\\__,_/_/  /_/\\__,_/_/ /_/\n" + 		
		"─────────── Federation Manager ───────────\n";

	// name of the Federation Manager executable
	public static final String EXEC_NAME = "fedman";
	
	// Federate Manager federation naming conventions 
	public static final String DEFAULT_FEDMAN_FEDERATE_NAME = "FederationManager";
	public static final String DEFAULT_FEDMAN_FEDERATE_TYPE = "FederationManager";
	public static final String FEDMAN_FEDERATE_TYPE = DEFAULT_FEDMAN_FEDERATE_NAME;
	public static final String FEDMAN_FEDERATE_NAME = DEFAULT_FEDMAN_FEDERATE_NAME;

	// MIM defined attribute reflections for detection of joining federates
	public static final String HLAFEDERATE_OBJECT_CLASS_NAME = "HLAobjectRoot.HLAmanager.HLAfederate";
	public static final String HLAFEDERATE_TYPE_ATTR = "HLAfederateType";
	public static final String HLAFEDERATE_NAME_ATTR = "HLAfederateName";
	public static final String HLAFEDERATE_HANDLE_ATTR = "HLAfederateHandle";
	public static final Set<String> HLAFEDERATE_ATTRIBUTE_NAMES =
	    new HashSet<>( Arrays.asList( new String[]{ HLAFEDERATE_HANDLE_ATTR, 
	                                                HLAFEDERATE_NAME_ATTR,
	                                                HLAFEDERATE_TYPE_ATTR } ) );
	
	// Various common text items used in output
	public static final String FEDERATE_TYPE_HEADING = "Type";
	public static final String NUMBER_REQUIRED_HEADING = "Required";
	public static final String NUMBER_JOINED_HEADING = "Joined";
	public static final String[] TABLE_HEADINGS = { FEDERATE_TYPE_HEADING, 
	                                                NUMBER_REQUIRED_HEADING, 
	                                                NUMBER_JOINED_HEADING };
	
	public static final int CONSOLE_WIDTH = 80;
}

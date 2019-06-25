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
	// BASE64 encoded UCEF logo JPG
	public static String UCEF_LOGO_JPG = "data:image/png;base64,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";

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
	public static final String DEFAULT_FEDMAN_FEDERATE_TYPE = DEFAULT_FEDMAN_FEDERATE_NAME;

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
	public static final String FEDERATE_TYPE_HEADING = "Federate Type";
	public static final String NUMBER_REQUIRED_HEADING = "Required";
	public static final String NUMBER_JOINED_HEADING = "Joined";
	public static final String NOTES_HEADING = "Notes";
	public static final String[] TABLE_HEADINGS = { FEDERATE_TYPE_HEADING,
	                                                NUMBER_REQUIRED_HEADING,
	                                                NUMBER_JOINED_HEADING,
	                                                NOTES_HEADING };

	public static final int CONSOLE_WIDTH = 80;
}

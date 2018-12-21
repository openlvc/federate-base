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
package gov.nist.ucef.hla.example.smart;

import java.util.Map;

import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.ucef.interaction.SmartInteraction;

public class Pong extends SmartInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition
	private static final String HLA_INTERACTION_ROOT = "HLAInteractionRoot.";
	private static final String INTERACTION_NAME = HLA_INTERACTION_ROOT+"Pong";
	
	// interaction parameters and types
	private static final String PARAM_KEY_LETTER = "letter";
	private static final ParameterType PARAM_TYPE_LETTER = ParameterType.Character;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param letter the letter
	 */
	public Pong( RTIAmbassadorWrapper rtiamb,
	             char letter)
	{
		this( rtiamb, null );
		
		letter( letter );
	}

	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param parameters the parameters to populate the interaction with
	 */
	public Pong( RTIAmbassadorWrapper rtiamb,
	             Map<String,byte[]> parameters )
	{
		super( rtiamb, interactionName(), parameters );
		// populate parameter => type lookup
		this.typeLookup.put( PARAM_KEY_LETTER, PARAM_TYPE_LETTER );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void letter( char letter )
	{
		setValue( PARAM_KEY_LETTER, letter );
	}

	public char letter()
	{
		return safeChar( getParameter( PARAM_KEY_LETTER ), ' ' );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Obtain the HLA interaction name identifying this type of interaction
	 * 
	 * @return the HLA interaction name identifying this interaction
	 */
	public static String interactionName()
	{
		return INTERACTION_NAME;
	}
}

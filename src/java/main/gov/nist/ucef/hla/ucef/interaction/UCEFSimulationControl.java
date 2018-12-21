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
package gov.nist.ucef.hla.ucef.interaction;

import java.util.Map;

import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;

public abstract class UCEFSimulationControl extends UCEFInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	protected static final String UCEF_SIMCONTROL_INTERACTION_ROOT = UCEF_INTERACTION_ROOT+"SimulationControl.";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected UCEFSimulationControl( RTIAmbassadorWrapper rtiamb, String interactionName )
	{
		this( rtiamb, interactionName, null );
	}

	protected UCEFSimulationControl( RTIAmbassadorWrapper rtiamb, 
	                                 String interactionName,
	                                 Map<String,byte[]> parameters )
	{
		super( rtiamb, interactionName, parameters );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

}

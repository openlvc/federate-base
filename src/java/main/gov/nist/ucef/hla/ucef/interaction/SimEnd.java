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
import hla.rti1516e.InteractionClassHandle;

public class SimEnd extends AbstractInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String INTERACTION_NAME = "InteractionRoot.C2WInteractionRoot.SimulationControl.SimEnd";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private SimEnd( InteractionClassHandle interactionClassHandle,
	                                 Map<String,byte[]> parameters )
	{
		super( interactionClassHandle, parameters );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static SimEnd create( RTIAmbassadorWrapper rtiamb )
	{
		InteractionClassHandle handle = rtiamb.getInteractionClassHandle( INTERACTION_NAME );
		SimEnd interaction = new SimEnd( handle, null );
		return interaction;
	}
}

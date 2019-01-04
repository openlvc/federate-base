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
package gov.nist.ucef.hla.example.smart.interactions;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.smart.AbstractInteractionRealizer;
import gov.nist.ucef.hla.smart.SmartInteraction;
import hla.rti1516e.InteractionClassHandle;


public class InteractionRealizer extends AbstractInteractionRealizer
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public InteractionRealizer( RTIAmbassadorWrapper rtiamb )
	{
		super( rtiamb );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Internal method to populate a map which provides associations for "creators" for each of
	 * the {@link SmartInteraction} types.
	 * 
	 * The populated map then used by the {@link #create(HLAInteraction)} method.
	 * 
	 * NOTE: for those who like patterns, this is essentially the "Command Pattern"; check here
	 * for more details: {@link https://en.wikipedia.org/wiki/Command_pattern}
	 * 
	 * See {@link Realizer} interface definition.
	 */
	@Override
	protected void initializeRealizers()
	{
		if( realizerLookup != null )
			return;
		
		super.initializeRealizers();
		
		InteractionClassHandle pingHandle = rtiamb.getInteractionClassHandle( Ping.interactionName() );
		InteractionClassHandle pongHandle = rtiamb.getInteractionClassHandle( Pong.interactionName() );
		
		realizerLookup.put( pingHandle, new Realizer() {
			public SmartInteraction realize( HLAInteraction x ) { return new Ping( rtiamb, x.getState() ); }
		});
		realizerLookup.put( pongHandle, new Realizer() {
			public SmartInteraction realize( HLAInteraction x ) { return new Pong( rtiamb, x.getState() ); } 
		});
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

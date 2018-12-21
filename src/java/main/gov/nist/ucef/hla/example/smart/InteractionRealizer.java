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

import java.util.HashMap;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.ucef.interaction.SmartInteraction;
import hla.rti1516e.InteractionClassHandle;


public class InteractionRealizer
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIAmbassadorWrapper rtiamb;
	private HashMap<InteractionClassHandle,Realizer> realizerLookup;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public InteractionRealizer( RTIAmbassadorWrapper rtiamb )
	{
		this.rtiamb = rtiamb;
		initializeRealizers();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Determine if we know how to realize the given {@link HLAInteraction} instance.
	 * 
	 * An alternative way to do this would be to call {@link #realize(HLAInteraction)} and check
	 * for a null return value.
	 * 
	 * @param interaction the {@link HLAInteraction} instance to process
	 * @return true if we know how to realize the {@link HLAInteraction}, false otherwise
	 */
	public boolean canProcess( HLAInteraction interaction )
	{
		if( interaction == null )
			return false;

		InteractionClassHandle handle = rtiamb.getInteractionClassHandle( interaction );
		return realizerLookup.containsKey( handle );
	}

	/**
	 * Create a specific interaction type from a generic {@HLAInteraction}. Possible instances are:
	 * 
	 * <ul>
	 * <li>{@link Ping}</li>
	 * <li>{@link Pong}</li>
	 * </ul>
	 * 
	 * if the {@link HLAInteraction} instance does not correspond to one of these, a null will be
	 * returned.
	 * 
	 * @param interaction the {@link HLAInteraction} instance from which to create the
	 *            {@link SmartInteraction}
	 * @return the {@link SmartInteraction} instance, or null if the {@link HLAInteraction}
	 *         instance does not correspond to a {@link HLAInteraction}.
	 */
	public SmartInteraction create( HLAInteraction interaction )
	{
		if( interaction == null )
			return null;
		
		InteractionClassHandle interactionKind = rtiamb.getInteractionClassHandle( interaction );
		Realizer creator = realizerLookup.get( interactionKind );
		SmartInteraction smartInteraction = creator == null ? null : creator.realize( interaction ); 
		return smartInteraction;
	}

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
	private void initializeRealizers()
	{
		if( realizerLookup != null )
			return;
		
		realizerLookup = new HashMap<InteractionClassHandle, Realizer>();
		
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
	//                    PRIVATE INTERFACES
	//----------------------------------------------------------
	/**
	 * Internal interface used to provide a suitable function for creating
	 * {@link SmartInteraction}s of known types
	 * 
	 * NOTE: Command Pattern - {@link https://en.wikipedia.org/wiki/Command_pattern}
	 */
	private interface Realizer
	{
		/**
		 * Create a {@link SmartInteraction} from a "generic" {@link HLAInteraction} instance
		 * 
		 * @param param the {@link HLAInteraction} instance from which to create the
		 *            {@link SmartInteraction}
		 * @return the {@link SmartInteraction} instance
		 */
		SmartInteraction realize( HLAInteraction param );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

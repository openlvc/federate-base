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
import java.util.Map;
import java.util.Map.Entry;

import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.ucef.interaction.SmartObject;
import hla.rti1516e.ObjectClassHandle;


public class ObjectRealizer
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIAmbassadorWrapper rtiamb;
	private HashMap<ObjectClassHandle,Realizer> realizerLookup;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ObjectRealizer( RTIAmbassadorWrapper rtiamb )
	{
		this.rtiamb = rtiamb;
		initializeRealizers();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Determine if we can realize the given {@link HLAObject} instance.
	 * 
	 * An alternative way to do this would be to call {@link #realize(HLAObject)} and check for a
	 * null return value.
	 * 
	 * @param object the {@link HLAObject} instance to realize
	 * @return true if the {@link HLAObject} instance can be realized, false otherwise
	 */
	public boolean canRealize( HLAObject object )
	{
		if( object == null )
			return false;

		ObjectClassHandle handle = rtiamb.getKnownObjectClassHandle(  object );
		return realizerLookup.containsKey( handle );
	}

	/**
	 * Create a specific interaction type from a generic {@HLAObject}. Possible instances are:
	 * 
	 * <ul>
	 * <li>{@link Ping}</li>
	 * <li>{@link Pong}</li>
	 * </ul>
	 * 
	 * if the {@link HLAObject} instance does not correspond to one of these, a null will be
	 * returned.
	 * 
	 * @param object the {@link HLAObject} instance from which to create the
	 *            {@link SmartObject}
	 * @return the {@link SmartObject} instance, or null if the {@link HLAObject}
	 *         instance does not correspond to a {@link HLAObject}.
	 */
	public SmartObject realize( HLAObject object )
	{
		if( object == null )
			return null;
		
		ObjectClassHandle interactionKind = rtiamb.getKnownObjectClassHandle( object );
		Realizer creator = realizerLookup.get( interactionKind );
		SmartObject smartObject = creator == null ? null : creator.realize( object ); 
		return smartObject;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Internal method to populate a map which provides associations for "creators" for each of
	 * the {@link SmartObject} types.
	 * 
	 * The populated map then used by the {@link #realize(HLAObject)} method.
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
		
		realizerLookup = new HashMap<ObjectClassHandle, Realizer>();
		
		ObjectClassHandle testObjectHandle = rtiamb.getObjectClassHandle( Player.objectClassName() );
		
		realizerLookup.put( testObjectHandle, new Realizer() {
			public SmartObject realize( HLAObject x ) { return new Player( rtiamb, mapCopy(x.getState()) ); }
		});
	}

	/**
	 * Provide a separate, independent copy of the original map
	 * 
	 * @param original the original map
	 * @return a separate, independent copy of the original map
	 */
	private Map<String,byte[]> mapCopy( Map<String,byte[]> original )
	{
		Map<String,byte[]> copy = new HashMap<>();
		for( Entry<String,byte[]> x : original.entrySet() )
		{
			byte[] src = x.getValue();
			byte[] dst = new byte[src.length];
			System.arraycopy( src, 0, dst, 0, src.length );
			copy.put( x.getKey(), dst );
		}
		return copy;
	}

	//----------------------------------------------------------
	//                    PRIVATE INTERFACES
	//----------------------------------------------------------
	/**
	 * Internal interface used to provide a suitable function for creating
	 * {@link SmartObject}s of known types
	 * 
	 * NOTE: Command Pattern - {@link https://en.wikipedia.org/wiki/Command_pattern}
	 */
	private interface Realizer
	{
		/**
		 * Create a {@link SmartObject} from a "generic" {@link HLAObject} instance
		 * 
		 * @param param the {@link HLAObject} instance from which to create the
		 *            {@link SmartObject}
		 * @return the {@link SmartObject} instance
		 */
		SmartObject realize( HLAObject param );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

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
package gov.nist.ucef.hla.example.smart.reflections;

import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.smart.AbstractObjectRealizer;
import gov.nist.ucef.hla.smart.SmartObject;
import hla.rti1516e.ObjectClassHandle;


public class ObjectRealizer extends AbstractObjectRealizer
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
	public ObjectRealizer( RTIAmbassadorWrapper rtiamb )
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
	 * the {@link SmartObject} types.
	 * 
	 * The populated map then used by the {@link #realize(HLAObject)} method.
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
		
		ObjectClassHandle playerHandle = rtiamb.getObjectClassHandle( Player.objectClassName() );
		
		realizerLookup.put( playerHandle, new Realizer() {
			public SmartObject realize( HLAObject x ) { return new Player( rtiamb, mapCopy(x.getState()) ); }
		});
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

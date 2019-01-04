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

import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.ucef.interaction.SmartObject;

public class Player extends SmartObject
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of object - must match FOM definition
	private static final String HLA_OBJECT_ROOT = "HLAobjectRoot.";
	private static final String OBJECT_CLASS_NAME = HLA_OBJECT_ROOT+"Player";
	
	// object attributes and types
	private static final String ATTRIBUTE_KEY_NAME = "name";
	private static final AttributeType ATTRIBUTE_TYPE_COUNT = AttributeType.String;
	
	private static final String[] ATTRIBUTE_NAMES = { ATTRIBUTE_KEY_NAME };
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param name the {@link Player} name
	 */
	public Player( RTIAmbassadorWrapper rtiamb,
	               String name )
	{
		this( rtiamb, new HashMap<>() );
		
		name( name );
	}

	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param attributes the parameters to populate the {@link Player} instance with
	 */
	public Player( RTIAmbassadorWrapper rtiamb, 
	               Map<String,byte[]> attributes )
	{
		super( rtiamb, objectClassName(), attributes );
		// populate attribute => type lookup
		this.typeLookup.put( ATTRIBUTE_KEY_NAME, ATTRIBUTE_TYPE_COUNT );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void name( String name )
	{
		setValue( ATTRIBUTE_KEY_NAME, name );
	}

	public String name()
	{
		return safeString( getAttribute( ATTRIBUTE_KEY_NAME ) );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Obtain the HLA object class name identifying this type of object
	 * 
	 * @return the HLA object class name identifying this type of object
	 */
	public static String objectClassName()
	{
		return OBJECT_CLASS_NAME;
	}
	
	/**
	 * Obtain the HLA object class name identifying this type of object
	 * 
	 * @return the HLA object class name identifying this type of object
	 */
	public static String[] attributeNames()
	{
		return ATTRIBUTE_NAMES;
	}
}

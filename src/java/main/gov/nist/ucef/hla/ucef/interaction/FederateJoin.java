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

public class FederateJoin extends UCEFInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition 
	private static final String INTERACTION_NAME = UCEF_INTERACTION_ROOT+"FederateJoinInteraction";
	
	// interaction parameters and types
	private static final String PARAM_KEY_FEDERATE_ID = "FederateId";
	private static final ParameterType PARAM_TYPE_FEDERATE_ID = ParameterType.String;
	private static final String PARAM_KEY_FEDERATE_TYPE = "FederateType";
	private static final ParameterType PARAM_TYPE_FEDERATE_TYPE = ParameterType.String;
	private static final String PARAM_KEY_IS_LATE_JOINER = "IsLateJoiner";
	private static final ParameterType PARAM_TYPE_IS_LATE_JOINER = ParameterType.Boolean;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param federateID the federate ID
	 * @param federateType the federate type
	 * @param isLateJoiner true if the federate is a late joiner, false otherwise
	 */
	public FederateJoin( RTIAmbassadorWrapper rtiamb,
	                     String federateID, String federateType, boolean isLateJoiner )
	{
		this( rtiamb, null );

		federateID( federateID );
		federateType( federateType );
		isLateJoiner( isLateJoiner );
	}

	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param parameters the parameters to populate the interaction with
	 */
	public FederateJoin( RTIAmbassadorWrapper rtiamb,
	                     Map<String,byte[]> parameters )
	{
		super( rtiamb, interactionName(), parameters );
		// populate parameter => type lookup
		this.typeLookup.put( PARAM_KEY_FEDERATE_ID, PARAM_TYPE_FEDERATE_ID );
		this.typeLookup.put( PARAM_KEY_FEDERATE_TYPE, PARAM_TYPE_FEDERATE_TYPE );
		this.typeLookup.put( PARAM_KEY_IS_LATE_JOINER, PARAM_TYPE_IS_LATE_JOINER );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void federateID( String federateID )
	{
		setValue( PARAM_KEY_FEDERATE_ID, safeString( federateID ) );
	}

	public String federateID()
	{
		return safeString( getParameter( PARAM_KEY_FEDERATE_ID ) );
	}

	public void federateType( String federateType )
	{
		setValue( PARAM_KEY_FEDERATE_TYPE, safeString( federateType ) );
	}

	public String federateType()
	{
		return safeString( getParameter( PARAM_KEY_FEDERATE_TYPE ) );
	}

	public void isLateJoiner( boolean isLateJoiner )
	{
		setValue( PARAM_KEY_IS_LATE_JOINER, isLateJoiner );
	}
	
	public boolean isLateJoiner()
	{
		return safeBoolean( getParameter( PARAM_KEY_IS_LATE_JOINER ) );
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

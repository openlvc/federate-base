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

import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import hla.rti1516e.InteractionClassHandle;

public class FederateJoin extends AbstractInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String INTERACTION_NAME =
	    "InteractionRoot.C2WInteractionRoot.FederateJoinInteraction";
	private static final String PARAM_FEDERATE_ID = "FederateID";
	private static final String PARAM_FEDERATE_TYPE = "FederateType";

	private static InteractionClassHandle interactionClassHandle = null;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private FederateJoin( InteractionClassHandle interactionClassHandle, String id, String type )
	{
		super( interactionClassHandle );
		this.typeMap.put( PARAM_FEDERATE_ID, ParameterType.String );
		this.typeMap.put( PARAM_FEDERATE_TYPE, ParameterType.String );

		setFederateID( id );
		setFederateType( type );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void setFederateID( String id )
	{
		setValue( PARAM_FEDERATE_ID, id );
	}

	public String getFederateID()
	{
		return (String)getParameter( PARAM_FEDERATE_ID );
	}

	public void setFederateType( String type )
	{
		setValue( PARAM_FEDERATE_TYPE, type );
	}

	public String getFederateType()
	{
		return (String)getParameter( PARAM_FEDERATE_TYPE );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static FederateJoin create( RTIAmbassadorWrapper rtiamb, String id, String type )
	{
		if( interactionClassHandle == null )
			interactionClassHandle = rtiamb.getInteractionClassHandle( INTERACTION_NAME );

		return new FederateJoin( interactionClassHandle, id, type );
	}
}

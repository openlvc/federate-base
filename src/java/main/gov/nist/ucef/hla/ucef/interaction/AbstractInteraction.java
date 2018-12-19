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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import hla.rti1516e.InteractionClassHandle;

public abstract class AbstractInteraction extends HLAInteraction
{
	//----------------------------------------------------------
	//                    ENUMERATIONS
	//----------------------------------------------------------
	/**
	 * An enumeration representing the various supported data types
	 */
	protected enum ParameterType
	{
		String, Character, 
		Short, Integer, Long, 
		Float, Double, 
		Boolean, 
		RAW // byte array - can be used for "non-primitive"/custom data structures 
	}
	
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Map<String, ParameterType> typeMap;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected AbstractInteraction( InteractionClassHandle interactionClassHandle )
	{
		this( interactionClassHandle, null );
	}

	protected AbstractInteraction( InteractionClassHandle interactionClassHandle,
	                         Map<String,byte[]> parameters )
	{
		super( interactionClassHandle, parameters );
		this.typeMap = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	protected Object getParameter( String parameterName )
	{
		if( isParameter( parameterName ) )
			return null;
		
		ParameterType kind = this.typeMap.get( parameterName );
		if(kind == ParameterType.Boolean)
			return getAsBoolean( parameterName );
		if(kind == ParameterType.Character)
			return getAsChar( parameterName );
		if(kind == ParameterType.String)
			return getAsString( parameterName );
		if(kind == ParameterType.Short)
			return getAsShort( parameterName );
		if(kind == ParameterType.Integer)
			return getAsInt( parameterName );
		if(kind == ParameterType.Long)
			return getAsLong( parameterName );
		if(kind == ParameterType.Float)
			return getAsFloat( parameterName );
		if(kind == ParameterType.Double)
			return getAsDouble( parameterName );
		if(kind == ParameterType.RAW)
			return getRawValue( parameterName );
		
		return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

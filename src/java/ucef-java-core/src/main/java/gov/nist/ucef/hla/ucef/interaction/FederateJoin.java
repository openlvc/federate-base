/*
 * This software is contributed as a public service by The National Institute of Standards 
 * and Technology (NIST) and is not subject to U.S. Copyright
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above NIST contribution notice and this permission and disclaimer notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. THE AUTHORS OR COPYRIGHT HOLDERS SHALL
 * NOT HAVE ANY OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 */
package gov.nist.ucef.hla.ucef.interaction;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;

public class FederateJoin extends HLAInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition 
	private static final String INTERACTION_NAME = "HLAInteractionRoot.C2WInteractionRoot.FederateJoinInteraction";
	
	// interaction parameters and types
	private static final String PARAM_KEY_FEDERATE_ID = "FederateId";
	private static final String PARAM_KEY_FEDERATE_TYPE = "FederateType";
	private static final String PARAM_KEY_IS_LATE_JOINER = "IsLateJoiner";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 */
	public FederateJoin()
	{
		super( INTERACTION_NAME );
	}

	/**
	 * @param interaction the {@link HLAInteraction} instance
	 */
	public FederateJoin( HLAInteraction interaction )
	{
		super( interaction );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public boolean federateIDIsPresent()
	{
		return isPresent( PARAM_KEY_FEDERATE_ID );
	}
	
	public void federateID( String federateID )
	{
		setValue( PARAM_KEY_FEDERATE_ID, federateID );
	}

	public String federateID()
	{
		return getAsString( PARAM_KEY_FEDERATE_ID );
	}

	public boolean federateTypeIsPresent()
	{
		return isPresent( PARAM_KEY_FEDERATE_TYPE );
	}
	
	public void federateType( String federateType )
	{
		setValue( PARAM_KEY_FEDERATE_TYPE, federateType );
	}

	public String federateType()
	{
		return getAsString( PARAM_KEY_FEDERATE_TYPE );
	}

	public boolean isLateJoinerIsPresent()
	{
		return isPresent( PARAM_KEY_IS_LATE_JOINER );
	}
	
	public void isLateJoiner( boolean isLateJoiner )
	{
		setValue( PARAM_KEY_IS_LATE_JOINER, isLateJoiner );
	}
	
	public boolean isLateJoiner()
	{
		return getAsBoolean( PARAM_KEY_IS_LATE_JOINER );
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

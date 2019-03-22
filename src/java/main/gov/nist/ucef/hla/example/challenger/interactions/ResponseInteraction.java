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
package gov.nist.ucef.hla.example.challenger.interactions;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;

public class ResponseInteraction extends HLAInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition
	private static final String INTERACTION_NAME = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.Response";
	
	// interaction parameters
	private static final String PARAM_KEY_SUBSTRING = "substring";
	private static final String PARAM_KEY_CHALLENGEID = "challengeId";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param count the count
	 */
	public ResponseInteraction()
	{
		super( INTERACTION_NAME );
	}

	/**
	 * @param interaction the {@link HLAInteraction} instance
	 */
	public ResponseInteraction( HLAInteraction interaction )
	{
		super( interaction );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void isSubstringPresent()
	{
		isPresent( PARAM_KEY_SUBSTRING );
	}
	
	public ResponseInteraction substring( String substring )
	{
		setValue( PARAM_KEY_SUBSTRING, substring );
		return this;
	}
	
	public String substring()
	{
		return getAsString( PARAM_KEY_SUBSTRING );
	}

	public boolean isChallengeIdPresent()
	{
		return isPresent( PARAM_KEY_CHALLENGEID );
	}
	
	public ResponseInteraction challengeId( String challengeId )
	{
		setValue( PARAM_KEY_CHALLENGEID, challengeId );
		return this;
	}
	
	public String challengeId()
	{
		return getAsString( PARAM_KEY_CHALLENGEID );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Obtain the HLA interaction name identifying this type of interaction
	 * 
	 * @return the HLA interaction name identifying this interaction
	 */
	public static String interactionClassName()
	{
		return INTERACTION_NAME;
	}
}

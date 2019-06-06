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

public class ChallengeInteraction extends HLAInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition
	private static final String INTERACTION_NAME = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.ChallengeInteraction";
	
	// interaction parameters
	private static final String PARAM_KEY_BEGININDEX = "beginIndex";
	private static final String PARAM_KEY_STRINGVALUE = "stringValue";
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
	public ChallengeInteraction()
	{
		super( INTERACTION_NAME );
	}

	/**
	 * @param interaction the {@link HLAInteraction} instance
	 */
	public ChallengeInteraction( HLAInteraction interaction )
	{
		super( interaction );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void isBeginIndexPresent()
	{
		isPresent( PARAM_KEY_BEGININDEX );
	}
	
	public ChallengeInteraction beginIndex( int beginIndex )
	{
		setValue( PARAM_KEY_BEGININDEX, beginIndex );
		return this;
	}

	public int beginIndex()
	{
		return getAsInt( PARAM_KEY_BEGININDEX );
	}

	public void isStringValuePresent()
	{
		isPresent( PARAM_KEY_STRINGVALUE );
	}
	
	public ChallengeInteraction stringValue( String stringValue )
	{
		setValue( PARAM_KEY_STRINGVALUE, stringValue );
		return this;
	}
	
	public String stringValue()
	{
		return getAsString( PARAM_KEY_STRINGVALUE );
	}

	public boolean isChallengeIdPresent()
	{
		return isPresent( PARAM_KEY_CHALLENGEID );
	}
	
	public ChallengeInteraction challengeId( String challengeId )
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

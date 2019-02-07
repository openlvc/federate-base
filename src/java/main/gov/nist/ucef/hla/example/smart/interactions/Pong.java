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
package gov.nist.ucef.hla.example.smart.interactions;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;

public class Pong extends HLAInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition
	private static final String HLA_INTERACTION_ROOT = "HLAInteractionRoot.";
	private static final String INTERACTION_NAME = HLA_INTERACTION_ROOT+"Pong";
	
	// interaction parameters and types
	private static final String PARAM_KEY_LETTER = "letter";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param letter the letter
	 */
	public Pong( RTIAmbassadorWrapper rtiamb, char letter )
	{
		super( rtiamb.getInteractionClassHandle( INTERACTION_NAME ), null );

		letter( letter );
	}

	/**
	 * @param interaction the {@link HLAInteraction} instance
	 */
	public Pong( HLAInteraction interaction )
	{
		super( interaction );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public boolean isLetterPresent()
	{
		return isPresent( PARAM_KEY_LETTER );
	}
	
	public void letter( char letter )
	{
		setValue( PARAM_KEY_LETTER, letter );
	}

	public char letter()
	{
		return getAsChar( PARAM_KEY_LETTER );
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

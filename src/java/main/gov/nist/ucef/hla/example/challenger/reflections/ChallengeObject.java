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
package gov.nist.ucef.hla.example.challenger.reflections;

import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;

public class ChallengeObject extends HLAObject
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of object - must match FOM definition
	private static final String OBJECT_CLASS_NAME = "HLAobjectRoot.ParentObject.ChallengeObject";
	
	// object attributes and types
	private static final String ATTRIBUTE_KEY_BEGININDEX = "beginIndex";
	private static final String ATTRIBUTE_KEY_STRINGVALUE = "stringValue";
	private static final String ATTRIBUTE_KEY_CHALLENGEID = "challengeId";
	
	private static final String[] ATTRIBUTE_NAMES = { ATTRIBUTE_KEY_BEGININDEX, 
	                                                  ATTRIBUTE_KEY_STRINGVALUE,
	                                                  ATTRIBUTE_KEY_CHALLENGEID };
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param name the {@link ChallengeObject} name
	 */
	public ChallengeObject()
	{
		super( OBJECT_CLASS_NAME );
	}

	/**
	 * @param instance the {@link HLAObject} instance
	 */
	public ChallengeObject( HLAObject instance )
	{
		super( instance );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void isBeginIndexPresent()
	{
		isPresent( ATTRIBUTE_KEY_BEGININDEX );
	}
	
	public ChallengeObject beginIndex( int beginIndex )
	{
		setValue( ATTRIBUTE_KEY_BEGININDEX, beginIndex );
		return this;
	}

	public int beginIndex()
	{
		return getAsInt( ATTRIBUTE_KEY_BEGININDEX );
	}

	public boolean isStringValuePresent()
	{
		return isPresent( ATTRIBUTE_KEY_STRINGVALUE );
	}
	
	public ChallengeObject stringValue( String stringValue )
	{
		setValue( ATTRIBUTE_KEY_STRINGVALUE, stringValue );
		return this;
	}
	
	public String stringValue()
	{
		return getAsString( ATTRIBUTE_KEY_STRINGVALUE );
	}
	
	public boolean isChallengeIdPresent()
	{
		return isPresent( ATTRIBUTE_KEY_CHALLENGEID );
	}
	
	public ChallengeObject challengeId( String challengeId )
	{
		setValue( ATTRIBUTE_KEY_CHALLENGEID, challengeId );
		return this;
	}
	
	public String challengeId()
	{
		return getAsString( ATTRIBUTE_KEY_CHALLENGEID );
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
	 * Obtain the HLA attribute names associated with this type of object
	 * 
	 * @return the HLA attribute names associated with this type of object
	 */
	public static String[] attributeNames()
	{
		return ATTRIBUTE_NAMES;
	}
}

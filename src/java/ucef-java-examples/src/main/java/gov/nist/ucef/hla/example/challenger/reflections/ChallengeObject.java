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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.Types.DataType;

public class ChallengeObject extends HLAObject
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of object - must match FOM definition
	private static final String OBJECT_CLASS_NAME = "HLAobjectRoot.ParentObject.ChallengeObject";
	
    // reflection attribute and types
    private static final String ATTRIBUTE_KEY_BEGININDEX = "beginIndex";
    private static final DataType ATTRIBUTE_TYPE_BEGININDEX = DataType.INT;
    private static final String ATTRIBUTE_KEY_STRINGVALUE = "stringValue";
    private static final DataType ATTRIBUTE_TYPE_STRINGVALUE = DataType.STRING;
    private static final String ATTRIBUTE_KEY_CHALLENGEID = "challengeId";
    private static final DataType ATTRIBUTE_TYPE_CHALLENGEID = DataType.STRING;

    // a map for finding a data type for an attribute name - this is to provide
    // quick lookups and avoid iterating over all attributes
    private static final Map<String,DataType> ATTRIBUTES_LOOKUP =
        Collections.unmodifiableMap( initializeMapping() );
	
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
    * Obtain the HLA object name identifying this type of object
    *
    * @return the HLA object name identifying this object
    */
    public static String objectClassName()
    {
        return OBJECT_CLASS_NAME;
    }

    /**
    * Determine whether a named attribute is associated with this kind of object
    *
    * @param attribute the name of the attribute to check for
    * @return true if the named attribute is associated with this kind of object,
    *         and false otherwise
    */
    public static boolean hasAttribute( String attribute )
    {
        return ATTRIBUTES_LOOKUP.containsKey( attribute );
    }

    /**
    * Obtain the names of the attributes associated with this kind of object
    *
    * @return a {@link Set<String>} containing the {@link String} names of the attributes
    */
    public static Set<String> attributeNames()
    {
        return ATTRIBUTES_LOOKUP.keySet();
    }

    /**
    * Obtain the {@link DataType} of a attribute of this kind of object
    *
    * @param attribute the name of the attribute to obtain the type for
    * @return a {@link DataType} corresponding to the type of the attribute. If no such
    *         attribute exists for this object, {@link DataType#UNKNOWN} will be
    *         returned.
    */
    public static DataType attributeType( String attribute )
    {
        return ATTRIBUTES_LOOKUP.getOrDefault( attribute, DataType.UNKNOWN );
    }

    /**
    * Obtain the attributes associated with this kind of object
    *
    * @return an (unmodifiable) {@link Map} associating the {@link String} names of the
    *         attributes and their {@link DataType}s
    */
    public static Map<String,DataType> attributes()
    {
        return Collections.unmodifiableMap( ATTRIBUTES_LOOKUP );
    }
    
    /**
    * Private initializer method for the attribute-datatype lookup map
    *
    * @return a lookup map which pairs attribute names and the corresponding {@link DataType}s
    */
    private static Map<String,DataType> initializeMapping()
    {
        Map<String,DataType> lookupMap = new HashMap<String,DataType>();
        lookupMap.put( ATTRIBUTE_KEY_BEGININDEX, ATTRIBUTE_TYPE_BEGININDEX );
        lookupMap.put( ATTRIBUTE_KEY_STRINGVALUE, ATTRIBUTE_TYPE_STRINGVALUE );
        lookupMap.put( ATTRIBUTE_KEY_CHALLENGEID, ATTRIBUTE_TYPE_CHALLENGEID );
        return lookupMap;
    }
}

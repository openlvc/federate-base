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
package gov.nist.hla.genx.reflections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.Types.DataType;

public class Player extends HLAObject
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of object - must match FOM definition
	private static final String HLA_OBJECT_ROOT   = "HLAobjectRoot.";
	private static final String OBJECT_CLASS_NAME = HLA_OBJECT_ROOT+"Player";
	
	// object attributes and types
	private static final String ATTRIBUTE_KEY_NAME    = "name";
	private static final DataType ATTRIBUTE_TYPE_NAME = DataType.STRING;
	
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
	 */
	public Player()
	{
		super( OBJECT_CLASS_NAME );
	}

	/**
	 * @param instance the {@link HLAObject} instance
	 */
	public Player( HLAObject instance )
	{
		super( instance );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public boolean isNamePresent()
	{
		return isPresent( ATTRIBUTE_KEY_NAME );
	}
	
	public Player name( String name )
	{
		setValue( ATTRIBUTE_KEY_NAME, name );
		// return instance for chaining
		return this;
	}

	public String name()
	{
		return getAsString( ATTRIBUTE_KEY_NAME );
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
	 * Obtain the attributes associated with this kind of reflection
	 * 
	 * @return a map associating the String names of the attributes and their data types
	 */
	public static Map<String,DataType> attributes()
	{
		return ATTRIBUTES_LOOKUP;
	}
	
	/**
	 * Private initializer method for the attribute-datatype lookup map
	 * 
	 * @return a lookup map which pairs attribute names and the corresponding
	 *         {@link DataType}s
	 */
	private static Map<String,DataType> initializeMapping()
	{
		Map<String,DataType> lookupMap = new HashMap<String,DataType>();
		lookupMap.put( ATTRIBUTE_KEY_NAME, ATTRIBUTE_TYPE_NAME );
		return lookupMap;
	}
}

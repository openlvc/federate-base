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
package gov.nist.ucef.hla.example.smart.reflections;

import java.util.HashMap;
import java.util.Map;

import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.smart.SmartObject;

public class Player extends SmartObject
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of object - must match FOM definition
	private static final String HLA_OBJECT_ROOT = "HLAobjectRoot.";
	private static final String OBJECT_CLASS_NAME = HLA_OBJECT_ROOT+"Player";
	
	// object attributes and types
	private static final String ATTRIBUTE_KEY_NAME = "name";
	private static final AttributeType ATTRIBUTE_TYPE_NAME = AttributeType.String;
	
	private static final String[] ATTRIBUTE_NAMES = { ATTRIBUTE_KEY_NAME };
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param name the {@link Player} name
	 */
	public Player( RTIAmbassadorWrapper rtiamb,
	               String name )
	{
		this( rtiamb, new HashMap<>() );
		
		name( name );
	}

	/**
	 * @param rtiamb the {@link RTIAmbassadorWrapper} instance
	 * @param attributes the parameters to populate the {@link Player} instance with
	 */
	public Player( RTIAmbassadorWrapper rtiamb, 
	               Map<String,byte[]> attributes )
	{
		super( rtiamb, objectClassName(), attributes );
		// populate attribute => type lookup
		this.typeLookup.put( ATTRIBUTE_KEY_NAME, ATTRIBUTE_TYPE_NAME );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void isNamePresent( String name )
	{
		isPresent( ATTRIBUTE_KEY_NAME );
	}
	
	public void name( String name )
	{
		setValue( ATTRIBUTE_KEY_NAME, name );
	}

	public String name()
	{
		return safeString( getAttribute( ATTRIBUTE_KEY_NAME ) );
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
	 * Obtain the HLA object class name identifying this type of object
	 * 
	 * @return the HLA object class name identifying this type of object
	 */
	public static String[] attributeNames()
	{
		return ATTRIBUTE_NAMES;
	}
}

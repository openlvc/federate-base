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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.Types.DataType;

public class Ping extends HLAInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition
	private static final String HLA_INTERACTION_ROOT = "HLAinteractionRoot.";
	private static final String INTERACTION_NAME = HLA_INTERACTION_ROOT+"Ping";
	
	// interaction parameters and types
	private static final String PARAM_KEY_COUNT = "count";
	private static final DataType PARAM_TYPE_COUNT = DataType.INT;
	
	// a map for finding a data type for an attribute name - this is to provide
	// quick lookups and avoid iterating over all attributes
	private static final Map<String,DataType> PARAMETERS_LOOKUP =
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
	public Ping()
	{
		super( INTERACTION_NAME, null );
	}

	/**
	 * @param interaction the {@link HLAInteraction} instance
	 */
	public Ping( HLAInteraction interaction )
	{
		super( interaction );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public boolean isCountPresent()
	{
		return isPresent( PARAM_KEY_COUNT );
	}
	
	public Ping count( int count )
	{
		setValue( PARAM_KEY_COUNT, count );
		// return instance for chaining
		return this;
	}

	public int count()
	{
		return getAsInt( PARAM_KEY_COUNT );
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
	
	/**
	 * Obtain the parameters associated with this kind of interaction
	 * 
	 * @return a map associating the String names of the parameters and their data types
	 */
	public static Map<String, DataType> parameters()
	{
		return PARAMETERS_LOOKUP;
	}
	
	/**
	 * Private initializer method for the parameter-datatype lookup map
	 * 
	 * @return a lookup map which pairs parameter names and the corresponding
	 *         {@link DataType}s
	 */
	private static Map<String,DataType> initializeMapping()
	{
		Map<String,DataType> lookupMap = new HashMap<String,DataType>();
		lookupMap.put( PARAM_KEY_COUNT, PARAM_TYPE_COUNT );
		return lookupMap;
	}
}

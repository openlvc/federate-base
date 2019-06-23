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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.Types.DataType;

public class ChallengeInteraction extends HLAInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition
	private static final String INTERACTION_NAME = "HLAinteractionRoot.C2WInteractionRoot.ParentInteraction.ChallengeInteraction";


    // interaction parameters and types
    private static final String PARAM_KEY_BEGININDEX = "beginIndex";
    private static final DataType PARAM_TYPE_BEGININDEX = DataType.INT;
    private static final String PARAM_KEY_STRINGVALUE = "stringValue";
    private static final DataType PARAM_TYPE_STRINGVALUE = DataType.INT;
    private static final String PARAM_KEY_CHALLENGEID = "challengeId";
    private static final DataType PARAM_TYPE_CHALLENGEID = DataType.INT;

    // a map for finding a data type for a parameter name - this is to provide
    // quick lookups and avoid iterating over all parameters
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
    * Determine whether a named parameter is associated with this kind of interaction
    *
    * @param parameter the name of the parameter to check for
    * @return true if the named parameter is associated with this kind of interaction,
    *         and false otherwise
    */
    public static boolean hasParameter( String parameter )
    {
        return PARAMETERS_LOOKUP.containsKey( parameter );
    }

    /**
    * Obtain the names of the parameters associated with this kind of interaction
    *
    * @return a {@link Set<String>} containing the {@link String} names of the parameters
    */
    public static Set<String> parameterNames()
    {
        return PARAMETERS_LOOKUP.keySet();
    }

    /**
    * Obtain the {@link DataType} of a parameter of this kind of interaction
    *
    * @param parameter the name of the parameter to obtain the type for
    * @return a {@link DataType} corresponding to the type of the parameter. If no such
    *         parameter exists for this interaction, {@link DataType#UNKNOWN} will be
    *         returned.
    */
    public static DataType parameterType( String parameter )
    {
        return PARAMETERS_LOOKUP.getOrDefault( parameter, DataType.UNKNOWN );
    }

    /**
    * Obtain the parameters associated with this kind of interaction
    *
    * @return an (unmodifiable) {@link Map} associating the {@link String} names of the
    *         parameters and their {@link DataType}s
    */
    public static Map<String,DataType> parameters()
    {
        return Collections.unmodifiableMap( PARAMETERS_LOOKUP );
    }

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
    * Private initializer method for the parameter-datatype lookup map
    *
    * @return a lookup map which pairs parameter names and the corresponding {@link DataType}s
    */
    private static Map<String,DataType> initializeMapping()
    {
        Map<String,DataType> lookupMap = new HashMap<String,DataType>();
        lookupMap.put( PARAM_KEY_BEGININDEX, PARAM_TYPE_BEGININDEX );
        lookupMap.put( PARAM_KEY_STRINGVALUE, PARAM_TYPE_STRINGVALUE );
        lookupMap.put( PARAM_KEY_CHALLENGEID, PARAM_TYPE_CHALLENGEID );
        return lookupMap;
    }
}

package gov.nist.hla.genx.interactions;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.Types.DataType;

public class Ping extends HLAInteraction
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------
    // HLA identifier of this type of interaction
    private static final String INTERACTION_NAME = "HLAinteractionRoot.Ping";

    // interaction parameters and types
    private static final String PARAM_KEY_SOMEBOOLEAN = "someBoolean";
    private static final DataType PARAM_TYPE_SOMEBOOLEAN = DataType.BOOLEAN;
    private static final String PARAM_KEY_SOMEBYTE = "someByte";
    private static final DataType PARAM_TYPE_SOMEBYTE = DataType.BYTE;
    private static final String PARAM_KEY_SOMECHAR = "someChar";
    private static final DataType PARAM_TYPE_SOMECHAR = DataType.CHAR;
    private static final String PARAM_KEY_SOMEDOUBLE = "someDouble";
    private static final DataType PARAM_TYPE_SOMEDOUBLE = DataType.DOUBLE;
    private static final String PARAM_KEY_SOMEFLOAT = "someFloat";
    private static final DataType PARAM_TYPE_SOMEFLOAT = DataType.FLOAT;
    private static final String PARAM_KEY_SOMEINT = "someInt";
    private static final DataType PARAM_TYPE_SOMEINT = DataType.INT;
    private static final String PARAM_KEY_SOMELONG = "someLong";
    private static final DataType PARAM_TYPE_SOMELONG = DataType.LONG;
    private static final String PARAM_KEY_SOMESHORT = "someShort";
    private static final DataType PARAM_TYPE_SOMESHORT = DataType.SHORT;
    private static final String PARAM_KEY_SOMESTRING = "someString";
    private static final DataType PARAM_TYPE_SOMESTRING = DataType.STRING;

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
     * Default constructor
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
    /**
    * Determine whether the 'someBoolean' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeBooleanPresent()
    {
        return isPresent( PARAM_KEY_SOMEBOOLEAN );
    }

    /**
    * Set the value for the 'someBoolean' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someBoolean( boolean value )
    {
        setValue( PARAM_KEY_SOMEBOOLEAN, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someBoolean' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeBooleanPresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someBooleanOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public boolean someBoolean()
    {
        return getAsBoolean( PARAM_KEY_SOMEBOOLEAN );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someBoolean' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public boolean someBooleanOrDefault( boolean defaultValue )
    {
        return isSomeBooleanPresent() ? this.someBoolean() : defaultValue;
    }
    /**
    * Determine whether the 'someByte' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeBytePresent()
    {
        return isPresent( PARAM_KEY_SOMEBYTE );
    }

    /**
    * Set the value for the 'someByte' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someByte( byte value )
    {
        setValue( PARAM_KEY_SOMEBYTE, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someByte' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeBytePresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someByteOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public byte someByte()
    {
        return getAsByte( PARAM_KEY_SOMEBYTE );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someByte' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public byte someByteOrDefault( byte defaultValue )
    {
        return isSomeBytePresent() ? this.someByte() : defaultValue;
    }
    /**
    * Determine whether the 'someChar' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeCharPresent()
    {
        return isPresent( PARAM_KEY_SOMECHAR );
    }

    /**
    * Set the value for the 'someChar' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someChar( char value )
    {
        setValue( PARAM_KEY_SOMECHAR, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someChar' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeCharPresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someCharOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public char someChar()
    {
        return getAsChar( PARAM_KEY_SOMECHAR );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someChar' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public char someCharOrDefault( char defaultValue )
    {
        return isSomeCharPresent() ? this.someChar() : defaultValue;
    }
    /**
    * Determine whether the 'someDouble' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeDoublePresent()
    {
        return isPresent( PARAM_KEY_SOMEDOUBLE );
    }

    /**
    * Set the value for the 'someDouble' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someDouble( double value )
    {
        setValue( PARAM_KEY_SOMEDOUBLE, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someDouble' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeDoublePresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someDoubleOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public double someDouble()
    {
        return getAsDouble( PARAM_KEY_SOMEDOUBLE );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someDouble' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public double someDoubleOrDefault( double defaultValue )
    {
        return isSomeDoublePresent() ? this.someDouble() : defaultValue;
    }
    /**
    * Determine whether the 'someFloat' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeFloatPresent()
    {
        return isPresent( PARAM_KEY_SOMEFLOAT );
    }

    /**
    * Set the value for the 'someFloat' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someFloat( float value )
    {
        setValue( PARAM_KEY_SOMEFLOAT, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someFloat' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeFloatPresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someFloatOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public float someFloat()
    {
        return getAsFloat( PARAM_KEY_SOMEFLOAT );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someFloat' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public float someFloatOrDefault( float defaultValue )
    {
        return isSomeFloatPresent() ? this.someFloat() : defaultValue;
    }
    /**
    * Determine whether the 'someInt' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeIntPresent()
    {
        return isPresent( PARAM_KEY_SOMEINT );
    }

    /**
    * Set the value for the 'someInt' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someInt( int value )
    {
        setValue( PARAM_KEY_SOMEINT, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someInt' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeIntPresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someIntOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public int someInt()
    {
        return getAsInt( PARAM_KEY_SOMEINT );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someInt' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public int someIntOrDefault( int defaultValue )
    {
        return isSomeIntPresent() ? this.someInt() : defaultValue;
    }
    /**
    * Determine whether the 'someLong' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeLongPresent()
    {
        return isPresent( PARAM_KEY_SOMELONG );
    }

    /**
    * Set the value for the 'someLong' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someLong( long value )
    {
        setValue( PARAM_KEY_SOMELONG, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someLong' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeLongPresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someLongOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public long someLong()
    {
        return getAsLong( PARAM_KEY_SOMELONG );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someLong' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public long someLongOrDefault( long defaultValue )
    {
        return isSomeLongPresent() ? this.someLong() : defaultValue;
    }
    /**
    * Determine whether the 'someShort' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeShortPresent()
    {
        return isPresent( PARAM_KEY_SOMESHORT );
    }

    /**
    * Set the value for the 'someShort' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someShort( short value )
    {
        setValue( PARAM_KEY_SOMESHORT, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someShort' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeShortPresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someShortOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public short someShort()
    {
        return getAsShort( PARAM_KEY_SOMESHORT );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someShort' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public short someShortOrDefault( short defaultValue )
    {
        return isSomeShortPresent() ? this.someShort() : defaultValue;
    }
    /**
    * Determine whether the 'someString' parameter has a value set for it
    *
    * @return true if a value has been set, false if the parameter value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeStringPresent()
    {
        return isPresent( PARAM_KEY_SOMESTRING );
    }

    /**
    * Set the value for the 'someString' parameter
    *
    * @param value the value to set
    * @return this {@link Ping} instance for method chaining
    */
    public Ping someString( String value )
    {
        setValue( PARAM_KEY_SOMESTRING, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someString' parameter.
    *
    * <b>NOTE:</b>The {@link #isSomeStringPresent()} method  should be used first
    * to determine whether the parameter has any value <i>>at all</i>. Trying to obtain the
    * value from a parameter which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someStringOrDefault()}, which provides a alternative
    * "safe" way to obtain the parameter's value.
    *
    * @return the current value for the parameter
    */
    public String someString()
    {
        return getAsString( PARAM_KEY_SOMESTRING );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someString' parameter,
    * using the the given default value in the case that the parameter has not been initialized
    * with a value.
    *
    * @return the current value for the parameter, or the default value if the
    *         parameter has not yet been initialized.
    */
    public String someStringOrDefault( String defaultValue )
    {
        return isSomeStringPresent() ? this.someString() : defaultValue;
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
    * Private initializer method for the parameter-datatype lookup map
    *
    * @return a lookup map which pairs parameter names and the corresponding {@link DataType}s
    */
    private static Map<String,DataType> initializeMapping()
    {
        Map<String,DataType> lookupMap = new HashMap<String,DataType>();
        lookupMap.put( PARAM_KEY_SOMEBOOLEAN, PARAM_TYPE_SOMEBOOLEAN );
        lookupMap.put( PARAM_KEY_SOMEBYTE, PARAM_TYPE_SOMEBYTE );
        lookupMap.put( PARAM_KEY_SOMECHAR, PARAM_TYPE_SOMECHAR );
        lookupMap.put( PARAM_KEY_SOMEDOUBLE, PARAM_TYPE_SOMEDOUBLE );
        lookupMap.put( PARAM_KEY_SOMEFLOAT, PARAM_TYPE_SOMEFLOAT );
        lookupMap.put( PARAM_KEY_SOMEINT, PARAM_TYPE_SOMEINT );
        lookupMap.put( PARAM_KEY_SOMELONG, PARAM_TYPE_SOMELONG );
        lookupMap.put( PARAM_KEY_SOMESHORT, PARAM_TYPE_SOMESHORT );
        lookupMap.put( PARAM_KEY_SOMESTRING, PARAM_TYPE_SOMESTRING );
        return lookupMap;
    }
}

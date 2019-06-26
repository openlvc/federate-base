package gov.nist.hla.genx.reflections;
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

import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.Types.DataType;

public class Player extends HLAObject
{
    //    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------
    // HLA identifier of this type of reflection
    private static final String OBJECT_CLASS_NAME = "HLAobjectRoot.Player";

    // reflection attribute and types
    private static final String ATTRIBUTE_KEY_SOMEBOOLEAN = "someBoolean";
    private static final DataType ATTRIBUTE_TYPE_SOMEBOOLEAN = DataType.BOOLEAN;
    private static final String ATTRIBUTE_KEY_SOMEBYTE = "someByte";
    private static final DataType ATTRIBUTE_TYPE_SOMEBYTE = DataType.BYTE;
    private static final String ATTRIBUTE_KEY_SOMECHAR = "someChar";
    private static final DataType ATTRIBUTE_TYPE_SOMECHAR = DataType.CHAR;
    private static final String ATTRIBUTE_KEY_SOMEDOUBLE = "someDouble";
    private static final DataType ATTRIBUTE_TYPE_SOMEDOUBLE = DataType.DOUBLE;
    private static final String ATTRIBUTE_KEY_SOMEFLOAT = "someFloat";
    private static final DataType ATTRIBUTE_TYPE_SOMEFLOAT = DataType.FLOAT;
    private static final String ATTRIBUTE_KEY_SOMEINT = "someInt";
    private static final DataType ATTRIBUTE_TYPE_SOMEINT = DataType.INT;
    private static final String ATTRIBUTE_KEY_SOMELONG = "someLong";
    private static final DataType ATTRIBUTE_TYPE_SOMELONG = DataType.LONG;
    private static final String ATTRIBUTE_KEY_SOMENAME = "someName";
    private static final DataType ATTRIBUTE_TYPE_SOMENAME = DataType.STRING;
    private static final String ATTRIBUTE_KEY_SOMESHORT = "someShort";
    private static final DataType ATTRIBUTE_TYPE_SOMESHORT = DataType.SHORT;

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
     * Default constructor
    */
    public Player()
    {
        super( OBJECT_CLASS_NAME, null, null );
    }

    /**
    * @param reflection the {@link HLAObject} instance
    */
    public Player( HLAObject reflection )
    {
        super( reflection );
    }

    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
    * Determine whether the 'someBoolean' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeBooleanPresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMEBOOLEAN );
    }

    /**
    * Set the value for the 'someBoolean' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someBoolean( boolean value )
    {
        setValue( ATTRIBUTE_KEY_SOMEBOOLEAN, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someBoolean' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeBooleanPresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someBooleanOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public boolean someBoolean()
    {
        return getAsBoolean( ATTRIBUTE_KEY_SOMEBOOLEAN );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someBoolean' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public boolean someBooleanOrDefault( boolean defaultValue )
    {
        return isSomeBooleanPresent() ? this.someBoolean() : defaultValue;
    }
    /**
    * Determine whether the 'someByte' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeBytePresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMEBYTE );
    }

    /**
    * Set the value for the 'someByte' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someByte( byte value )
    {
        setValue( ATTRIBUTE_KEY_SOMEBYTE, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someByte' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeBytePresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someByteOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public byte someByte()
    {
        return getAsByte( ATTRIBUTE_KEY_SOMEBYTE );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someByte' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public byte someByteOrDefault( byte defaultValue )
    {
        return isSomeBytePresent() ? this.someByte() : defaultValue;
    }
    /**
    * Determine whether the 'someChar' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeCharPresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMECHAR );
    }

    /**
    * Set the value for the 'someChar' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someChar( char value )
    {
        setValue( ATTRIBUTE_KEY_SOMECHAR, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someChar' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeCharPresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someCharOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public char someChar()
    {
        return getAsChar( ATTRIBUTE_KEY_SOMECHAR );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someChar' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public char someCharOrDefault( char defaultValue )
    {
        return isSomeCharPresent() ? this.someChar() : defaultValue;
    }
    /**
    * Determine whether the 'someDouble' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeDoublePresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMEDOUBLE );
    }

    /**
    * Set the value for the 'someDouble' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someDouble( double value )
    {
        setValue( ATTRIBUTE_KEY_SOMEDOUBLE, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someDouble' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeDoublePresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someDoubleOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public double someDouble()
    {
        return getAsDouble( ATTRIBUTE_KEY_SOMEDOUBLE );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someDouble' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public double someDoubleOrDefault( double defaultValue )
    {
        return isSomeDoublePresent() ? this.someDouble() : defaultValue;
    }
    /**
    * Determine whether the 'someFloat' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeFloatPresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMEFLOAT );
    }

    /**
    * Set the value for the 'someFloat' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someFloat( float value )
    {
        setValue( ATTRIBUTE_KEY_SOMEFLOAT, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someFloat' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeFloatPresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someFloatOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public float someFloat()
    {
        return getAsFloat( ATTRIBUTE_KEY_SOMEFLOAT );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someFloat' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public float someFloatOrDefault( float defaultValue )
    {
        return isSomeFloatPresent() ? this.someFloat() : defaultValue;
    }
    /**
    * Determine whether the 'someInt' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeIntPresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMEINT );
    }

    /**
    * Set the value for the 'someInt' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someInt( int value )
    {
        setValue( ATTRIBUTE_KEY_SOMEINT, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someInt' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeIntPresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someIntOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public int someInt()
    {
        return getAsInt( ATTRIBUTE_KEY_SOMEINT );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someInt' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public int someIntOrDefault( int defaultValue )
    {
        return isSomeIntPresent() ? this.someInt() : defaultValue;
    }
    /**
    * Determine whether the 'someLong' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeLongPresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMELONG );
    }

    /**
    * Set the value for the 'someLong' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someLong( long value )
    {
        setValue( ATTRIBUTE_KEY_SOMELONG, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someLong' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeLongPresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someLongOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public long someLong()
    {
        return getAsLong( ATTRIBUTE_KEY_SOMELONG );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someLong' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public long someLongOrDefault( long defaultValue )
    {
        return isSomeLongPresent() ? this.someLong() : defaultValue;
    }
    /**
    * Determine whether the 'someName' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeNamePresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMENAME );
    }

    /**
    * Set the value for the 'someName' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someName( String value )
    {
        setValue( ATTRIBUTE_KEY_SOMENAME, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someName' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeNamePresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someNameOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public String someName()
    {
        return getAsString( ATTRIBUTE_KEY_SOMENAME );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someName' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public String someNameOrDefault( String defaultValue )
    {
        return isSomeNamePresent() ? this.someName() : defaultValue;
    }
    /**
    * Determine whether the 'someShort' attribute has a value set for it
    *
    * @return true if a value has been set, false if the attribute value has
    *         never been initialised (i.e., is currently invalid/indeterminate)
    */
    public boolean isSomeShortPresent()
    {
        return isPresent( ATTRIBUTE_KEY_SOMESHORT );
    }

    /**
    * Set the value for the 'someShort' attribute
    *
    * @param value the value to set
    * @return this {@link Player} instance for method chaining
    */
    public Player someShort( short value )
    {
        setValue( ATTRIBUTE_KEY_SOMESHORT, value );
        return this;
    }

    /**
    * Obtain the current value for the 'someShort' attribute.
    *
    * <b>NOTE:</b>The {@link #isSomeShortPresent()} method  should be used first
    * to determine whether the attribute has any value <i>>at all</i>. Trying to obtain the
    * value from a attribute which has never been initialized with a value
    * (i.e., is currently invalid/indeterminate) will cause a {@link RuntimeException}.
    *
    * See also {@link #someShortOrDefault()}, which provides a alternative
    * "safe" way to obtain the attribute's value.
    *
    * @return the current value for the attribute
    */
    public short someShort()
    {
        return getAsShort( ATTRIBUTE_KEY_SOMESHORT );
    }

    /**
    * Provides a "safe" way to obtain the current value for the 'someShort' attribute,
    * using the the given default value in the case that the attribute has not been initialized
    * with a value.
    *
    * @return the current value for the attribute, or the default value if the
    *         attribute has not yet been initialized.
    */
    public short someShortOrDefault( short defaultValue )
    {
        return isSomeShortPresent() ? this.someShort() : defaultValue;
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
        lookupMap.put( ATTRIBUTE_KEY_SOMEBOOLEAN, ATTRIBUTE_TYPE_SOMEBOOLEAN );
        lookupMap.put( ATTRIBUTE_KEY_SOMEBYTE, ATTRIBUTE_TYPE_SOMEBYTE );
        lookupMap.put( ATTRIBUTE_KEY_SOMECHAR, ATTRIBUTE_TYPE_SOMECHAR );
        lookupMap.put( ATTRIBUTE_KEY_SOMEDOUBLE, ATTRIBUTE_TYPE_SOMEDOUBLE );
        lookupMap.put( ATTRIBUTE_KEY_SOMEFLOAT, ATTRIBUTE_TYPE_SOMEFLOAT );
        lookupMap.put( ATTRIBUTE_KEY_SOMEINT, ATTRIBUTE_TYPE_SOMEINT );
        lookupMap.put( ATTRIBUTE_KEY_SOMELONG, ATTRIBUTE_TYPE_SOMELONG );
        lookupMap.put( ATTRIBUTE_KEY_SOMENAME, ATTRIBUTE_TYPE_SOMENAME );
        lookupMap.put( ATTRIBUTE_KEY_SOMESHORT, ATTRIBUTE_TYPE_SOMESHORT );
        return lookupMap;
    }
}

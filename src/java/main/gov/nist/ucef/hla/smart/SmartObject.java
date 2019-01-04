/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-java.
 *
 *   NOTICE:  All information contained herein is, and remains
 *            the property of Calytrix Technologies Pty Ltd.
 *            The intellectual and technical concepts contained
 *            herein are proprietary to Calytrix Technologies Pty Ltd.
 *            Dissemination of this information or reproduction of
 *            this material is strictly forbidden unless prior written
 *            permission is obtained from Calytrix Technologies Pty Ltd.
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package gov.nist.ucef.hla.smart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.UCEFException;

/**
 * An extension of the basic {@link HLAObject} class which primarily provides some "smarts"
 * with regards to data types for known attributes
 * 
 * This class is not instantiated directly, but instead used as a base for other classes to
 * extend.
 */
public abstract class SmartObject extends HLAObject
{
	//----------------------------------------------------------
	//                    ENUMERATIONS
	//----------------------------------------------------------
	/**
	 * An enumeration representing the various supported data types
	 */
	protected enum AttributeType
	{
		String, Character,    // text
		Short, Integer, Long, // integer numerics 
		Float, Double,        // floating point numerics
		Boolean,              // boolean
		Bytes                 // raw byte array; can be used for "non-primitive"/custom data  
	}
	
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// map to look up an appropriate retriever to obtain correctly typed value 
	// for a known attribute type - this is a static map used by all objects
	// which extend this class 
	private static Map<AttributeType, ValueGetter> GettersLookup;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of object - must match FOM definition 
	protected String objectClassName;
	// map to look up the attribute type of a named attribute
	protected Map<String,AttributeType> typeLookup;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected SmartObject( RTIAmbassadorWrapper rtiamb,  String objectClassName )
	{
		this( rtiamb, objectClassName, null );
	}

	protected SmartObject( RTIAmbassadorWrapper rtiamb, String objectClassName,
	                       Map<String,byte[]> attributes )
	{
		super( rtiamb.registerObjectInstance( objectClassName ), attributes );
		this.objectClassName = objectClassName;
		
		this.typeLookup = new HashMap<>();
		initializeGettersLookup();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Provides a stringified summary of the interaction and its attributes' values
	 * 
	 * Provided primarily for logging and debugging purposes.
	 * 
	 * Output format is along these lines:
	 * <pre>
	 * OBJECT_CLASS_NAME {ATTR1_NAME: ATTR1_VALUE, ATTR2_NAME: ATTR2_VALUE, ... }  
	 * </pre>
	 * 
	 * Strings will be quoted.
	 */
	@Override
	public String toString()
	{
		// grab the attribute names and sort them alphabetically; after all, some 
		// poor human is (presumably) going to have to read this and easily find
		// the attribute/value they want to check
		List<String> attributeNames = new ArrayList<>( this.typeLookup.keySet() );
		attributeNames.sort( null );
		
		StringBuilder builder = new StringBuilder( this.objectClassName ).append( " {" );
		int paramCount = attributeNames.size();
		for( int idx = 0; idx < paramCount; idx++ )
		{
			String attributeName = attributeNames.get( idx );
			builder.append( attributeName ).append( ":" );

			Object value = getAttribute( attributeName );
			if( value instanceof String )
				builder.append( '"' ).append( value ).append( '"' );
			else if( value instanceof Character )
				builder.append( '\'' ).append( value ).append( '\'' );
			else if( value instanceof byte[] )
			{
				// byte array
				// don't print the actual raw bytes themselves!
				int length = ((byte[])value).length;
				String bytesPluralized = "byte" + (length == 1 ? "" : "s");
				builder.append( "[…")
					   .append( length ).append( " " ).append( bytesPluralized )
					   .append( "…]" );
			}
			else if( value != null )
			{
				// short, int, long, float, double and boolean values
				builder.append( value.toString() );
			}
			else
			{
				// null values
				builder.append( "null" );
			}

			if( idx < paramCount - 1 )
			{
				// comma separator
				builder.append( ", " );
			}
		}
		builder.append( "}" );
		return builder.toString();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtain the value of an object attribute by name
	 * 
	 * @param attributeName the name of the object attribute
	 * @return the value associated with the object attribute (may be null if there is no
	 *         value associated with the named object attribute)
	 */
	protected Object getAttribute( String attributeName )
	{
		return getAttribute( attributeName, null );
	}

	/**
	 * Obtain the value of an object attribute by name
	 * 
	 * @param attributeName the name of the object attribute
	 * @param defaultValue the value to use if there is no value associated with the named
	 *            object attribute
	 * @return the value associated with the attribute, or the default value if there is no value
	 *         associated with the named object attribute)
	 */
	protected Object getAttribute( String attributeName, Object defaultValue )
	{
		AttributeType kind = this.typeLookup.get( attributeName );
		ValueGetter c = GettersLookup.get( kind );
		return c == null ? defaultValue : c.get( this, attributeName );
	}

	/**
	 * Utility method to safely convert an object to a char
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not a character
	 * @return the character value of the object, or the default value if the object is not a
	 *         character
	 */
	protected char safeChar( Object x, char defaultValue )
	{
		return x instanceof Character ? (Character)x : defaultValue;
	}
	
	/**
	 * Utility method to safely convert an object to a {@link String}
	 * 
	 * @param x the object
	 * @return the {@link String} value of the object, or an empty {@link String} (i.e., "") if
	 *         the object is not a {@link String}
	 */
	protected String safeString( Object x ) { return safeString( x, "" ); }
	
	/**
	 * Utility method to safely convert an object to a {@link String}
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not a {@link String}
	 * @return the {@link String} value of the object, or the default value if the object is not a
	 *         {@link String}
	 */
	protected String safeString( Object x, String defaultValue )
	{
		return x instanceof String ? (String)x : defaultValue;
	}

	/**
	 * Utility method to safely convert an object to a short
	 * 
	 * @param x the object
	 * @return the short value of the object, or 0 if the object is not a short
	 */
	protected short safeShort( Object x ) { return safeShort( x, (short)0 ); }
	
	/**
	 * Utility method to safely convert an object to a short
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not a short
	 * @return the short value of the object, or the default value if the object is not a short
	 */
	protected short safeShort( Object x, short defaultValue )
	{
		return x instanceof Short ? (Short)x : defaultValue;
	}

	/**
	 * Utility method to safely convert an object to an int
	 * 
	 * @param x the object
	 * @return the int value of the object, or 0 if the object is not an int
	 */
	protected int safeInt( Object x ) { return safeInt( x, 0 ); }

	/**
	 * Utility method to safely convert an object to an int
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not an int
	 * @return the int value of the object, or the default value if the object is not an int
	 */
	protected int safeInt( Object x, int defaultValue )
	{
		return x instanceof Integer ? (Integer)x : defaultValue;
	}

	/**
	 * Utility method to safely convert an object to a long
	 * 
	 * @param x the object
	 * @return the long value of the object, or 0 if the object is not a long
	 */
	protected long safeLong( Object x ) { return safeLong( x, 0L ); }

	/**
	 * Utility method to safely convert an object to a long
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not a long
	 * @return the long value of the object, or the default value if the object is not a long
	 */
	protected long safeLong( Object x, long defaultValue )
	{
		return x instanceof Long ? (Long)x : defaultValue;
	}

	/**
	 * Utility method to safely convert an object to a float
	 * 
	 * @param x the object
	 * @return the float value of the object, or 0.0 if the object is not a float
	 */
	protected float safeFloat( Object x ) { return safeFloat( x, 0.0F ); }

	/**
	 * Utility method to safely convert an object to a float
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not a float
	 * @return the float value of the object, or the default value if the object is not a float
	 */
	protected float safeFloat( Object x, float defaultValue )
	{
		return x instanceof Float ? (Float)x : defaultValue;
	}

	/**
	 * Utility method to safely convert an object to a double
	 * 
	 * @param x the object
	 * @return the double value of the object, or 0.0 if the object is not a double
	 */
	protected double safeDouble( Object x ) { return safeDouble( x, 0.0 ); }

	/**
	 * Utility method to safely convert an object to a double
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not a double
	 * @return the double value of the object, or the default value if the object is not a double
	 */
	protected double safeDouble( Object x, double defaultValue )
	{
		return x instanceof Double ? (Double)x : defaultValue;
	}

	/**
	 * Utility method to safely convert an object to a boolean
	 * 
	 * @param x the object
	 * @return the boolean value of the object, or false if the object is not a boolean
	 */
	protected boolean safeBoolean( Object x ) { return safeBoolean( x, false ); }

	/**
	 * Utility method to safely convert an object to a boolean
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not a boolean
	 * @return the boolean value of the object, or the default value if the object is not a
	 *         boolean
	 */
	protected boolean safeBoolean( Object x, boolean defaultValue )
	{
		return x instanceof Boolean ? (Boolean)x : defaultValue;
	}

	/**
	 * Utility method to safely convert an object to a byte array
	 * 
	 * @param x the object
	 * @return the boolean value of the object, or an empty/zero-length byte array if the object
	 *         is not a byte array
	 */
	protected byte[] safeBytes( Object x ) { return safeBytes( x, EMPTY_BYTE_ARRAY ); }

	/**
	 * Utility method to safely convert an object to a byte array
	 * 
	 * @param x the object
	 * @param defaultValue the value to use if the object is not a byte array
	 * @return the boolean value of the object, or the default value if the object is not a byte
	 *         array
	 */
	protected byte[] safeBytes( Object x, byte[] defaultValue )
	{
		return x instanceof byte[] ? (byte[])x : defaultValue;
	}

	/**
	 * Internal method to populate a map which provides associations for "getters" for each of the
	 * various attribute types.
	 * 
	 * The populated map then used by the {@link #getAttribute(String, Object)} method.
	 * 
	 * NOTE: for those who like patterns, this is essentially the "Command Pattern"; check here
	 * for more details: {@link https://en.wikipedia.org/wiki/Command_pattern}
	 * 
	 * See {@link ValueGetter} interface definition.
	 */
	private void initializeGettersLookup()
	{
		if( GettersLookup != null )
			return;
		
		GettersLookup = new HashMap<AttributeType, ValueGetter>();
		
		GettersLookup.put(AttributeType.String, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getAsString( x ); } });
		GettersLookup.put(AttributeType.Character, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getAsChar( x ); } });
		GettersLookup.put(AttributeType.Short, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getAsShort( x ); } });
		GettersLookup.put(AttributeType.Integer, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getAsInt( x ); } });
		GettersLookup.put(AttributeType.Long, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getAsLong( x ); } });
		GettersLookup.put(AttributeType.Float, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getAsFloat( x ); } });
		GettersLookup.put(AttributeType.Double, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getAsDouble( x ); } });
		GettersLookup.put(AttributeType.Boolean, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getAsBoolean( x ); } });
		GettersLookup.put(AttributeType.Bytes, 
		               new ValueGetter() { public Object get(HLAObject i, String x) { return i.getRawValue( x ); } });
	}

	//----------------------------------------------------------
	//                    PRIVATE INTERFACES
	//----------------------------------------------------------
	/**
	 * Internal interface used to provide a suitable function for retrieving data of a known type.
	 * 
	 *  NOTE: Command Pattern - {@link https://en.wikipedia.org/wiki/Command_pattern}
	 */
	private interface ValueGetter
	{
		/**
		 * Look up a value using the given key
		 * 
		 * @param attributeName {@link String} the key corresponding to the desired value
		 * @return the value (possibly null) if the key does not correspond to a value
		 */
		Object get( HLAObject object, String attributeName );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	public static String objectClassName()
	{
		throw new UCEFException("objectClassName() method has not been overidden");
	}
}

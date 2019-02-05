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
package gov.nist.ucef.hla.smart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.UCEFException;

/**
 * An extension of the basic {@link HLAInteraction} class which primarily provides some "smarts"
 * with regards to data types for known parameters
 * 
 * This class is not instantiated directly, but instead used as a base for other classes to
 * extend.
 */
public abstract class SmartInteraction extends HLAInteraction
{
	//----------------------------------------------------------
	//                    ENUMERATIONS
	//----------------------------------------------------------
	/**
	 * An enumeration representing the various supported data types
	 */
	protected enum ParameterType
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
	// for a known parameter type - this is a static map used by all interactions
	// which extend this class 
	private static Map<ParameterType, ValueGetter> GettersLookup;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// HLA identifier of this type of interaction - must match FOM definition 
	protected String interactionName;
	// map to look up the parameter type of a named parameter
	protected Map<String,ParameterType> typeLookup;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected SmartInteraction( RTIAmbassadorWrapper rtiamb, String interactionName )
	{
		this( rtiamb, interactionName, null );
	}

	protected SmartInteraction( RTIAmbassadorWrapper rtiamb, String interactionName,
	                            Map<String,byte[]> parameters )
	{
		super( rtiamb.getInteractionClassHandle( interactionName ), parameters );
		this.interactionName = interactionName;

		this.typeLookup = new HashMap<>();
		initializeGettersLookup();
	}

	protected SmartInteraction( String interactionName, HLAInteraction interaction )
	{
		super( interaction );
		this.interactionName = interactionName;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Provides a stringified summary of the interaction and its parameters' values
	 * 
	 * Provided primarily for logging and debugging purposes.
	 * 
	 * Output format is along these lines:
	 * <pre>
	 * INTERACTION_NAME {PARAM1_NAME: PARAM1_VALUE, PARAM2_NAME: PARAM2_VALUE, ... }  
	 * </pre>
	 * 
	 * Strings will be quoted.
	 */
	@Override
	public String toString()
	{
		// grab the parameter names and sort them alphabetically; after all, some 
		// poor human is (presumably) going to have to read this and easily find
		// the parameter/value they want to check
		List<String> parameterNames = new ArrayList<>( this.typeLookup.keySet() );
		parameterNames.sort( null );
		
		StringBuilder builder = new StringBuilder( this.interactionName ).append( " {" );
		int paramCount = parameterNames.size();
		for( int idx = 0; idx < paramCount; idx++ )
		{
			String parameterName = parameterNames.get( idx );
			builder.append( parameterName ).append( ":" );

			Object value = getParameter( parameterName );
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
	 * Obtain the value of an interaction parameter by name
	 * 
	 * @param parameterName the name of the interaction parameter
	 * @return the value associated with the interaction parameter (may be null if there is no
	 *         value associated with the named interaction parameter)
	 */
	protected Object getParameter( String parameterName )
	{
		return getParameter( parameterName, null );
	}

	/**
	 * Obtain the value of an interaction parameter by name
	 * 
	 * @param parameterName the name of the interaction parameter
	 * @param defaultValue the value to use if there is no value associated with the named
	 *            interaction parameter
	 * @return the value associated with the parameter, or the default value if there is no value
	 *         associated with the named interaction parameter)
	 */
	protected Object getParameter( String parameterName, Object defaultValue )
	{
		ParameterType kind = this.typeLookup.get( parameterName );
		ValueGetter c = GettersLookup.get( kind );
		return c == null ? defaultValue : c.get( this, parameterName );
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
	 * various parameter types.
	 * 
	 * The populated map then used by the {@link #getParameter(String, Object)} method.
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
		
		GettersLookup = new HashMap<ParameterType, ValueGetter>();
		
		GettersLookup.put(ParameterType.String, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getAsString( x ); } });
		GettersLookup.put(ParameterType.Character, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getAsChar( x ); } });
		GettersLookup.put(ParameterType.Short, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getAsShort( x ); } });
		GettersLookup.put(ParameterType.Integer, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getAsInt( x ); } });
		GettersLookup.put(ParameterType.Long, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getAsLong( x ); } });
		GettersLookup.put(ParameterType.Float, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getAsFloat( x ); } });
		GettersLookup.put(ParameterType.Double, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getAsDouble( x ); } });
		GettersLookup.put(ParameterType.Boolean, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getAsBoolean( x ); } });
		GettersLookup.put(ParameterType.Bytes, 
		               new ValueGetter() { public Object get(HLAInteraction i, String x) { return i.getRawValue( x ); } });
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
		 * @param parameterName {@link String} the key corresponding to the desired value
		 * @return the value (possibly null) if the key does not correspond to a value
		 */
		Object get( HLAInteraction interaction, String parameterName );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static String interactionName()
	{
		throw new UCEFException("interactionName() method has not been overidden");
	}
}

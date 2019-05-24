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
package gov.nist.ucef.hla.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAunicodeChar;
import hla.rti1516e.encoding.HLAunicodeString;

public class Types
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	/**
	 *  Represents data type of an attribute or interaction parameter
	 *
	 *  @see ObjectAttribute
	 *  @see InteractionParameter
	 */
	@SuppressWarnings("rawtypes")
	enum DataType
	{
		BYTE(    "bytes",   Byte[].class,    null),
		CHAR(    "char",    Character.class, HLAunicodeChar.class),
		SHORT(   "short",   Short.class,     HLAinteger16BE.class),
		INT(     "int",     Integer.class,   HLAinteger32BE.class),
		LONG(    "long",    Long.class,      HLAinteger64BE.class),
		FLOAT(   "float",   Float.class,     HLAfloat32BE.class),
		DOUBLE(  "double",  Double.class,    HLAfloat64BE.class),
		BOOLEAN( "boolean", Boolean.class,   HLAboolean.class),
		STRING(  "string",  String.class,    HLAunicodeString.class),
		UNKNOWN( "unknown", null,            null);
		
		// a map for finding a datat type for a string key - this is to provide
		// quick lookups and avoid iterating over all data types
		private static final Map<String,DataType> DATATYPE_LOOKUP =
		    Collections.unmodifiableMap( initializeMapping() );

		public String label;
		public Class javaType;
		public Class hlaType;
		
		DataType( String label, Class javaType, Class hlaType )
		{
			this.label = label;
			this.javaType = javaType;
			this.hlaType = hlaType;
		}
		
		/**
		 * Converts a text identifier uniquely identifying a data type to a {@link DataType}
		 * instance.
		 * 
		 * NOTE: if the key is not a valid text identifier for a data type, UNKNOWN be returned
		 * 
		 * @param label the text identifier uniquely identifying a data type
		 * @return the corresponding {@link DataType}, or {@link DataType#UNKNOWN} if the key is
		 *         not a valid text identifier for a {@link DataType}.
		 */
		public static DataType fromLabel( String label )
		{
			return DATATYPE_LOOKUP.getOrDefault( label.toLowerCase().trim(), UNKNOWN );
		}

		/**
		 * Private initializer method for the key-to-{@link DataType} lookup map
		 * 
		 * @return a lookup map which pairs text identifiers and the corresponding
		 *         {@link DataType}s
		 */
		private static Map<String,DataType> initializeMapping()
		{
			Map<String,DataType> lookupMap = new HashMap<String,DataType>();
			for( DataType dataType : DataType.values() )
				lookupMap.put( dataType.label.toLowerCase().trim(), dataType );
			return lookupMap;
		}
	};
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Represents an attribute in an object class in a Simulation Object Model
	 *
	 * @see ObjectClass
	 */
	protected static class ObjectAttribute
	{
		public String name;
		public boolean publish;
		public boolean subscribe;
		public DataType dataType;

		public ObjectAttribute( String name )
		{
			this.name = name;
			this.publish = false;
			this.subscribe = false;
			this.dataType = DataType.UNKNOWN;
		}
	}

	/**
	 * Represents an object class in a Simulation Object Model
	 *
	 * @see SOMParser#getObjectClasses(string&)
	 * @see ObjectAttribute
	 */
	protected static class ObjectClass
	{
		public String name; // fully qualified object class name
		public boolean publish;
		public boolean subscribe;
		Map<String,ObjectAttribute> attributes;

		public ObjectClass( String name )
		{
			this.name = name;
			this.publish = false;
			this.subscribe = false;
			this.attributes = new HashMap<>();
		}
	}

	/**
	 * Represents a parameter in an interaction class in a Simulation Object Model
	 *
	 * @see InteractionClass
	 */
	protected static class InteractionParameter
	{
		public String name;
		public DataType dataType;

		public InteractionParameter( String name )
		{
			this.name = name;
			this.dataType = DataType.UNKNOWN;
		}
	}

	/**
	 * Represents an interaction class in a Simulation Object Model
	 *
	 * @see SOMParser#getInteractionClasses(string&)
	 * @see InteractionParameter
	 */
	protected static class InteractionClass
	{
		public String name; // fully qualified interaction class name
		public boolean publish;
		public boolean subscribe;
		public Map<String,InteractionParameter> parameters;

		public InteractionClass( String name )
		{
			this.name = name;
			this.publish = false;
			this.subscribe = false;
			this.parameters = new HashMap<>();
		}
	}
}

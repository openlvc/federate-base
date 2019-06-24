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
import java.util.stream.Collectors;

import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAbyte;
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
	 * Represents the sharing type of an object class, object class attribute or interaction
	 *
	 * See also: {@link ObjectClass}, {@link ObjectAttribute} and {@link InteractionClass}
	 */
	public enum Sharing
	{
		// note that these values conform to the ones used in SOM XML definitions
		// for `<sharing>` nodes - don't change the strings!
		PUBLISH("Publish"),
		SUBSCRIBE("Subscribe"),
		PUBLISHSUBSCRIBE("PublishSubscribe"),
		NEITHER("Neither");

		// a map for finding a data type for a string key - this is to provide
		// quick lookups and avoid iterating over all data types
		private static final Map<String,Sharing> SHARING_LOOKUP =
			Collections.unmodifiableMap( initializeMapping() );

		public String label;

		private Sharing( String label )
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return this.label;
		}

		/**
		 * Determine if this sharing policy includes publishing
		 *
		 * @return true if this sharing policy includes publishing, false otherwise
		 */
		public boolean isPublish()
		{
			return this.equals( PUBLISH ) || this.equals( PUBLISHSUBSCRIBE );
		}

		/**
		 * Determine if this sharing policy includes subscribing
		 *
		 * @return true if this sharing policy includes subscribing, false otherwise
		 */
		public boolean isSubscribe()
		{
			return this.equals( SUBSCRIBE ) || this.equals( PUBLISHSUBSCRIBE );
		}

		/**
		 * Determine if this sharing policy is neither for publishing nor for subscribing
		 *
		 * @return true if this sharing policy is neither for publishing nor for subscribing,
		 *         false otherwise
		 */
		public boolean isNeither()
		{
			return this.equals( NEITHER );
		}

		/**
		 * Converts a text identifier uniquely identifying a sharing type to a {@link Sharing}
		 * instance.
		 *
		 * NOTE: if the key is not a valid text identifier for a sharing type, NEITHER will be returned
		 *
		 * @param label the text identifier uniquely identifying a sharing type
		 * @return the corresponding {@link Sharing}, or {@link Sharing#NEITHER} if the key is
		 *         not a valid text identifier for a {@link Sharing}.
		 */
		public static Sharing fromLabel( String label )
		{
			if( label == null )
				return NEITHER;
			return SHARING_LOOKUP.getOrDefault( label.toLowerCase().trim(), NEITHER );
		}

		/**
		 * Private initializer method for the key-to-{@link Sharing} lookup map
		 *
		 * @return a lookup map which pairs text identifiers and the corresponding
		 *         {@link Sharing}s
		 */
		private static Map<String,Sharing> initializeMapping()
		{
			Map<String,Sharing> lookupMap = new HashMap<String,Sharing>();
			for( Sharing sharing : Sharing.values() )
				lookupMap.put( sharing.label.toLowerCase().trim(), sharing );
			return lookupMap;
		}
	};

	/**
	 * Represents data type of an attribute or interaction parameter
	 *
	 * See also {@link ObjectAttribute} and {@link InteractionParameter}
	 */
	@SuppressWarnings("rawtypes")
	public enum DataType
	{
		BYTE(    "byte",    "HLAbyte",          Byte.class,      HLAbyte.class),
		CHAR(    "char",    "HLAunicodeChar",   Character.class, HLAunicodeChar.class),
		SHORT(   "short",   "HLAinteger16BE",   Short.class,     HLAinteger16BE.class),
		INT(     "int",     "HLAinteger32BE",   Integer.class,   HLAinteger32BE.class),
		LONG(    "long",    "HLAinteger64BE",   Long.class,      HLAinteger64BE.class),
		FLOAT(   "float",   "HLAfloat32BE",     Float.class,     HLAfloat32BE.class),
		DOUBLE(  "double",  "HLAfloat64BE",     Double.class,    HLAfloat64BE.class),
		BOOLEAN( "boolean", "HLAboolean",       Boolean.class,   HLAboolean.class),
		STRING(  "string",  "HLAunicodeString", String.class,    HLAunicodeString.class),
		UNKNOWN( "unknown", "unknown",          null,            null);

		// a map for finding a data type for a string key - this is to provide
		// quick lookups and avoid iterating over all data types
		private static final Map<String,DataType> DATATYPE_LOOKUP =
		    Collections.unmodifiableMap( initializeMapping() );

		private String label;
		private String hlaLabel;
		@SuppressWarnings("unused")
		private Class javaType;
		@SuppressWarnings("unused")
		private Class hlaType;

		private DataType( String label, String hlaLabel, Class javaType, Class hlaType )
		{
			this.label = label;
			this.hlaLabel = hlaLabel;
			this.javaType = javaType;
			this.hlaType = hlaType;
		}

		@Override
		public String toString()
		{
			return this.label;
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
			if( label == null )
				return UNKNOWN;
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
			{
				lookupMap.put( dataType.label.toLowerCase().trim(), dataType );
				lookupMap.put( dataType.hlaLabel.toLowerCase().trim(), dataType );
			}
			return lookupMap;
		}
	};

	//----------------------------------------------------------
	//                     CLASSES
	//----------------------------------------------------------
	/**
	 * Represents an object class in a Simulation Object Model
	 *
	 * See also {@link ObjectAttribute}
	 */
	public static class ObjectClass
	{
		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		public String name; // fully qualified object class name
		public Sharing sharing;
		Map<String,ObjectAttribute> attributes;

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		/**
		 * Constructor
		 *
		 * @param name the fully qualified object class name
		 * @param sharing the sharing type
		 */
		public ObjectClass( String name, Sharing sharing )
		{
			this.name = name;
			this.sharing = sharing == null ? Sharing.NEITHER : sharing;
			this.attributes = new HashMap<>();
		}

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		@Override
		public String toString()
		{
			String attrs = this.attributes.values()
				.stream()
				.map((attribute)->attribute.toString())
				.collect(Collectors.joining(","));

			return String.format( "Class:'%s'(%s){%s}",
			                      this.name, this.sharing.toString(), attrs );
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////
		/**
		 * Determine if the sharing policy for this instance includes publishing
		 *
		 * @return if the sharing policy for this instance includes publishing, false otherwise
		 */
		public boolean isPublished()
		{
			return this.sharing.isPublish();
		}

		/**
		 * Determine if the sharing policy for this instance includes subscribing
		 *
		 * @return if the sharing policy for this instance includes subscribing, false otherwise
		 */
		public boolean isSubscribed()
		{
			return this.sharing.isSubscribe();
		}

		/**
		 * Add an attribute instance
		 *
		 * @param attribute the {@link ObjectAttribute} instance to be added
		 * @return the added {@link ObjectAttribute} instance
		 */
		public ObjectAttribute addAttribute( ObjectAttribute attribute )
		{
			this.attributes.put( attribute.name, attribute );
			return attribute;
		}

		/**
		 * Add an attribute with the given name, data type and sharing policy
		 *
		 * @param name the attribute name
		 * @param dataType the attribute data type
		 * @param sharing the sharing policy for this attribute
		 * @return the added {@link ObjectAttribute} instance
		 */
		public ObjectAttribute addAttribute( String name, DataType dataType, Sharing sharing )
		{
			return addAttribute( new ObjectAttribute( name, dataType, sharing ) );
		}

		/**
		 * Shortcut method for adding attributes which are published
		 *
		 * See also {@link #addAttribute(String, DataType, Sharing)}
		 *
		 * @param name the attribute name
		 * @param dataType the attribute data type
		 * @return the added {@link ObjectAttribute} instance
		 */
		public ObjectAttribute addAttributePub( String name, DataType dataType )
		{
			return addAttribute( name, dataType, Sharing.PUBLISH );
		}

		/**
		 * Shortcut method for adding attributes which are subscribed
		 * See also {@link #addAttribute(String, DataType, Sharing)}
		 *
		 * @param name the attribute name
		 * @param dataType the attribute data type
		 * @return the added {@link ObjectAttribute} instance
		 */
		public ObjectAttribute addAttributeSub( String name, DataType dataType )
		{
			return addAttribute( name, dataType, Sharing.SUBSCRIBE );
		}

		/**
		 * Shortcut method for adding attributes which are both published and subscribed
		 *
		 * See also {@link #addAttribute(String, DataType, Sharing)}
		 *
		 * @param name the attribute name
		 * @param dataType the attribute data type
		 * @return the added {@link ObjectAttribute} instance
		 */
		public ObjectAttribute addAttributePubSub( String name, DataType dataType )
		{
			return addAttribute( name, dataType, Sharing.PUBLISHSUBSCRIBE );
		}

		/**
		 * Obtain the attributes of this instance
		 *
		 * @return a {@link Map} relating the names of the attributes to their corresponding
		 *         {@link ObjectAttribute} definition instances
		 */
		public Map<String,ObjectAttribute> getAttributes()
		{
			return Collections.unmodifiableMap( this.attributes );
		}

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
		/**
		 * "Shortcut" static method to create published instances
		 *
		 * See also {@link ObjectClass(String, Sharing)}
		 *
		 * @param name the fully qualified object class name
		 * @return an {@link ObjectClass} instance with a {@link Sharing#PUBLISH} sharing policy
		 */
		public static ObjectClass Pub( String name )
		{
			return new ObjectClass( name, Sharing.PUBLISH );
		}

		/**
		 * "Shortcut" static method to create subscribed instances
		 *
		 * See also {@link ObjectClass(String, Sharing)}
		 *
		 * @param name the fully qualified object class name
		 * @return an {@link ObjectClass} instance with a {@link Sharing#SUBSCRIBE} sharing policy
		 */
		public static ObjectClass Sub( String name )
		{
			return new ObjectClass( name, Sharing.SUBSCRIBE );
		}

		/**
		 * "Shortcut" static method to create instances which are both published and subscribed
		 *
		 * See also {@link ObjectClass(String, Sharing)}
		 *
		 * @param name the fully qualified object class name
		 * @return an {@link ObjectClass} instance with a {@link Sharing#PUBLISHSUBSCRIBE} sharing
		 *         policy
		 */
		public static ObjectClass PubSub( String name )
		{
			return new ObjectClass( name, Sharing.PUBLISHSUBSCRIBE );
		}
	}

	/**
	 * Represents an attribute in an object class in a Simulation Object Model
	 *
	 * See also {@link ObjectClass}
	 */
	public static class ObjectAttribute
	{
		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		public String name;
		public Sharing sharing;
		public DataType dataType;

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public ObjectAttribute( String name, DataType dataType, Sharing sharing )
		{
			this.name = name;
			this.sharing = sharing == null ? Sharing.NEITHER : sharing;
			this.dataType = dataType == null ? DataType.UNKNOWN : dataType;
		}

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		@Override
		public String toString()
		{
			return String.format( "Attr:'%s'[%s](%s)",
			                      this.name,
			                      this.dataType.toString(),
			                      this.sharing.toString());
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////
		/**
		 * Determine if the sharing policy for this instance includes publishing
		 *
		 * @return if the sharing policy for this instance includes publishing, false otherwise
		 */
		public boolean isPublished()
		{
			return this.sharing.isPublish();
		}

		/**
		 * Determine if the sharing policy for this instance includes subscribing
		 *
		 * @return if the sharing policy for this instance includes subscribing, false otherwise
		 */
		public boolean isSubscribed()
		{
			return this.sharing.isSubscribe();
		}

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
		/**
		 * "Shortcut" static method to create published instances
		 *
		 * See also {@link ObjectAttribute(String, DataType, Sharing)}
		 *
		 * @param name the attribute name
		 * @param dataType the attribute data type
		 * @return an {@link ObjectAttribute} instance with a {@link Sharing#PUBLISH} sharing
		 *         policy
		 */
		public static ObjectAttribute Pub( String name, DataType dataType )
		{
			return new ObjectAttribute( name, dataType, Sharing.PUBLISH );
		}

		/**
		 * "Shortcut" static method to create subscribed instances
		 *
		 * See also {@link ObjectAttribute(String, DataType, Sharing)}
		 *
		 * @param name the attribute name
		 * @param dataType the attribute data type
		 * @return an {@link ObjectAttribute} instance with a {@link Sharing#SUBSCRIBE} sharing
		 *         policy
		 */
		public static ObjectAttribute Sub( String name, DataType dataType )
		{
			return new ObjectAttribute( name, dataType, Sharing.SUBSCRIBE );
		}

		/**
		 * "Shortcut" static method to create instances which are both published and subscribed
		 *
		 * See also {@link ObjectAttribute(String, DataType, Sharing)}
		 *
		 * @param name the attribute name
		 * @param dataType the attribute data type
		 * @return an {@link ObjectAttribute} instance with a {@link Sharing#PUBLISHSUBSCRIBE}
		 *         sharing policy
		 */
		public static ObjectAttribute PubSub( String name, DataType dataType )
		{
			return new ObjectAttribute( name, dataType, Sharing.PUBLISHSUBSCRIBE );
		}
	}

	/**
	 * Represents an interaction class in a Simulation Object Model
	 *
	 * See also {@link InteractionParameter}
	 */
	public static class InteractionClass
	{
		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		public String name; // fully qualified interaction class name
		public Sharing sharing;
		public Map<String,InteractionParameter> parameters;

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public InteractionClass( String name, Sharing sharing )
		{
			this.name = name;
			this.sharing = sharing == null ? Sharing.NEITHER : sharing;
			this.parameters = new HashMap<>();
		}

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		@Override
		public String toString()
		{
			String params = this.parameters.values()
				.stream()
				.map((parameter)->parameter.toString())
				.collect(Collectors.joining(","));

			return String.format( "Interaction:'%s'(%s){%s}",
			                      this.name, this.sharing.toString(), params );
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////
		/**
		 * Determine if the sharing policy for this instance includes publishing
		 *
		 * @return if the sharing policy for this instance includes publishing, false otherwise
		 */
		public boolean isPublished()
		{
			return this.sharing.isPublish();
		}

		/**
		 * Determine if the sharing policy for this instance includes subscribing
		 *
		 * @return if the sharing policy for this instance includes subscribing, false otherwise
		 */
		public boolean isSubscribed()
		{
			return this.sharing.isSubscribe();
		}

		/**
		 * Add an parameter instance
		 *
		 * @param parameter the {@link InteractionParameter} instance to be added
		 * @return the added {@link InteractionParameter} instance
		 */
		public InteractionParameter addParameter( InteractionParameter parameter )
		{
			this.parameters.put( parameter.name, parameter );
			return parameter;
		}

		/**
		 * Add a parameter with the given name and data type
		 *
		 * @param name the attribute name
		 * @param dataType the attribute data type
		 * @return the added {@link InteractionParameter} instance
		 */
		public InteractionParameter addParameter( String name, DataType datatType )
		{
			return addParameter( new InteractionParameter( name, datatType ) );
		}

		public Map<String,InteractionParameter> getParameters()
		{
			return Collections.unmodifiableMap( this.parameters );
		}
		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
		/**
		 * "Shortcut" static method to create instances which are published
		 *
		 * See also {@link InteractionClass(String, Sharing)}
		 *
		 * @param name the fully qualified interaction class name
		 * @return an {@link InteractionClass} instance with a {@link Sharing#PUBLISH} sharing
		 *         policy
		 */
		public static InteractionClass Pub( String name )
		{
			return new InteractionClass( name, Sharing.PUBLISH );
		}

		/**
		 * "Shortcut" static method to create instances which are subscribed
		 *
		 * See also {@link InteractionClass(String, Sharing)}
		 *
		 * @param name the fully qualified interaction class name
		 * @return an {@link InteractionClass} instance with a {@link Sharing#SUBSCRIBE} sharing
		 *         policy
		 */
		public static InteractionClass Sub( String name )
		{
			return new InteractionClass( name, Sharing.SUBSCRIBE );
		}

		/**
		 * "Shortcut" static method to create instances which are both published and subscribed
		 *
		 * See also {@link InteractionClass(String, Sharing)}
		 *
		 * @param name the fully qualified interaction class name
		 * @return an {@link InteractionClass} instance with a {@link Sharing#PUBLISHSUBSCRIBE} sharing
		 *         policy
		 */
		public static InteractionClass PubSub( String name )
		{
			return new InteractionClass( name, Sharing.PUBLISHSUBSCRIBE );
		}
	}

	/**
	 * Represents a parameter in an interaction class in a Simulation Object Model
	 *
	 * See also {@link InteractionClass}
	 */
	public static class InteractionParameter
	{
		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		public String name;
		public DataType dataType;

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public InteractionParameter( String name, DataType dataType )
		{
			this.name = name;
			this.dataType = dataType == null ? DataType.UNKNOWN : dataType;
		}

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		@Override
		public String toString()
		{
			return String.format( "Param:'%s'[%s]",
			                      this.name,
			                      this.dataType.toString());
		}
	}
}

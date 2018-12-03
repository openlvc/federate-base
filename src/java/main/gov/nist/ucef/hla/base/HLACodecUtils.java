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
package gov.nist.ucef.hla.base;

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAunicodeChar;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIinternalError;

/**
 * The purpose of this class is to provide encoding/decoding of various "primitive" data types
 * to/from HLA standard representations to minimize repeated code
 */
public class HLACodecUtils
{
	//----------------------------------------------------------
	//                    STATIC METHODS
	//----------------------------------------------------------
	public static EncoderFactory getEncoder()
	{
		try
		{
			return RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		}
		catch( RTIinternalError e )
		{
			throw new UCEFException("Failed to initialize HLA encode/decode utilities.", e);
		}
	}
	
	/**
	 * Decode HLA byte array representation of a short
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public static short asShort( EncoderFactory encoderFactory, byte[] bytes )
	{
		HLAinteger16BE value = makeHLAType(encoderFactory, (short)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException( de,
			                         "Unable to decode byte array of length %d as a short.",
			                         bytes.length );
		}
	}
	
	/**
	 * Decode HLA byte array representation of an integer
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public static int asInt( EncoderFactory encoderFactory, byte[] bytes )
	{
		HLAinteger32BE value = makeHLAType(encoderFactory, (int)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException( de,
			                         "Unable to decode byte array of length %d as a integer.",
			                         bytes.length );
		}
	}
	
	/**
	 * Decode HLA byte array representation of a long
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public static long asLong( EncoderFactory encoderFactory, byte[] bytes )
	{
		HLAinteger64BE value = makeHLAType(encoderFactory, (long)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException( de,
			                         "Unable to decode byte array of length %d as a long.",
			                         bytes.length );
		}
	}
	
	/**
	 * Decode HLA byte array representation of a float
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public static float asFloat( EncoderFactory encoderFactory, byte[] bytes )
	{
		HLAfloat32BE value = makeHLAType(encoderFactory, (float)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException( de,
			                         "Unable to decode byte array of length %d as a float.",
			                         bytes.length );
		}
	}
	
	/**
	 * Decode HLA byte array representation of a double
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public static double asDouble( EncoderFactory encoderFactory, byte[] bytes )
	{
		HLAfloat64BE value = makeHLAType(encoderFactory, (double)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException( de,
			                         "Unable to decode byte array of length %d as a double.",
			                         bytes.length );
		}
	}
	
	/**
	 * Decode HLA byte array representation of a boolean
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public static boolean asBoolean( EncoderFactory encoderFactory, byte[] bytes )
	{
		HLAboolean value = makeHLAType(encoderFactory, false);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException( de,
			                         "Unable to decode byte array of length %d as a boolean.",
			                         bytes.length );
		}
	}

	/**
	 * Decode HLA byte array representation of a char
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public static char asChar( EncoderFactory encoderFactory, byte[] bytes )
	{
		HLAunicodeChar value = makeHLAType(encoderFactory, (char)0);
		try
		{
			value.decode( bytes );
			return (char)value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException( de,
			                         "Unable to decode byte array of length %d as a short.",
			                         bytes.length );
		}
	}
	
	/**
	 * Decode HLA byte array representation of a unicode string
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public static String asString( EncoderFactory encoderFactory, byte[] bytes )
	{
		HLAunicodeString value = makeHLAType(encoderFactory, "");
		try
		{
			value.decode( bytes );
			String result = value.getValue();
			
			// check for null terminator at end of string and remove as necessary
			int length = result.length();
			if( length > 0 && result.charAt( length - 1 ) == 0 )
				result = result.substring( 0, length - 1  );
			
			return result;
		}
		catch( DecoderException de )
		{
			throw new UCEFException( de,
			                         "Unable to decode byte array of length %d as a unicode string.",
			                         bytes.length );
		}
	}

	/**
	 * Encode a short to an HLA byte array representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public static byte[] encode(EncoderFactory encoderFactory, char value)
	{
		return makeHLAType(encoderFactory, value).toByteArray();
	}
	
	/**
	 * Encode a short to an HLA byte array representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public static byte[] encode(EncoderFactory encoderFactory, short value)
	{
		return makeHLAType(encoderFactory, value).toByteArray();
	}
	
	/**
	 * Encode an integer to an HLA byte array representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public static byte[] encode(EncoderFactory encoderFactory, int value)
	{
		return makeHLAType(encoderFactory, value).toByteArray();
	}
	
	/**
	 * Encode a long to an HLA byte array representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public static byte[] encode(EncoderFactory encoderFactory, long value)
	{
		return makeHLAType(encoderFactory, value).toByteArray();
	}
	
	/**
	 * Encode a double to an HLA byte array representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public static byte[] encode(EncoderFactory encoderFactory, double value)
	{
		return makeHLAType(encoderFactory, value).toByteArray();
	}
	
	/**
	 * Encode a float to an HLA byte array representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public static byte[] encode(EncoderFactory encoderFactory, float value)
	{
		return makeHLAType(encoderFactory, value).toByteArray();
	}
	
	/**
	 * Encode a boolean to an HLA byte array representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the HLA byte array
	 */
	public static byte[] encode(EncoderFactory encoderFactory, boolean value)
	{
		return makeHLAType(encoderFactory, value).toByteArray();
	}
	
	/**
	 * Encode a string to an HLA byte array representation of a unicode string
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the HLA byte array
	 */
	public static byte[] encode(EncoderFactory encoderFactory, String value)
	{
		return makeHLAType( encoderFactory, value ).toByteArray();
	}
	
	/**
	 * Encode a short to an {@link HLAinteger16BE} representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the {@link HLAinteger16BE}
	 */
	private static HLAinteger16BE makeHLAType(EncoderFactory encoderFactory, short value)
	{
		return encoderFactory.createHLAinteger16BE(value);
	}
	
	/**
	 * Encode an integer to an {@link HLAinteger32BE} representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the {@link HLAinteger32BE}
	 */
	private static HLAinteger32BE makeHLAType(EncoderFactory encoderFactory, int value)
	{
		return encoderFactory.createHLAinteger32BE(value);
	}
	
	/**
	 * Encode a long to an {@link HLAinteger64BE} representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the HLA byte array
	 */
	private static HLAinteger64BE makeHLAType(EncoderFactory encoderFactory, long value)
	{
		return encoderFactory.createHLAinteger64BE(value);
	}
	
	/**
	 * Encode a double to an {@link HLAfloat64BE} representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the {@link HLAfloat64BE}
	 */
	private static HLAfloat64BE makeHLAType(EncoderFactory encoderFactory, double value)
	{
		return encoderFactory.createHLAfloat64BE(value);
	}
	
	/**
	 * Encode a float to an {@link HLAfloat32BE} representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the {@link HLAfloat32BE}
	 */
	private static HLAfloat32BE makeHLAType(EncoderFactory encoderFactory, float value)
	{
		return encoderFactory.createHLAfloat32BE(value);
	}
	
	/**
	 * Encode a boolean to an {@link HLAboolean} representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the {@link HLAboolean}
	 */
	private static HLAboolean makeHLAType(EncoderFactory encoderFactory, boolean value)
	{
		return encoderFactory.createHLAboolean(value);
	}
	
	/**
	 * Encode a char to an {@link HLAunicodeChar} representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the {@link HLAunicodeChar}
	 */
	private static HLAunicodeChar makeHLAType(EncoderFactory encoderFactory, char value)
	{
		// NOTE: we need to cast the char to a short here
		//       in order to create the HLAunicodeChar 
		return encoderFactory.createHLAunicodeChar((short)value);
	}
	
	/**
	 * Encode a string to an {@link HLAunicodeString} representation 
	 * 
	 * @param encoderFactory the encoder instance to use
	 * @param value the value to encode
	 * @return the {@link HLAunicodeString}
	 */
	private static HLAunicodeString makeHLAType(EncoderFactory encoderFactory, String value)
	{
		return encoderFactory.createHLAunicodeString(value == null ? "" : value);
	}
}

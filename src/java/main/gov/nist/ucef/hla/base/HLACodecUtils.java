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
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIinternalError;

/**
 * The purpose of this class is to provide encoding/decoding of various "primitive" data types
 * to/from HLA standard representations to minimize repeated code
 */
public class HLACodecUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static HLACodecUtils instance;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static HLACodecUtils instance()
	{
		if(instance == null)
		{
			instance = new HLACodecUtils(); 
		}
		return instance;
	}
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private EncoderFactory encoderFactory;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private HLACodecUtils()
	{
		try
		{
			this.encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		}
		catch( RTIinternalError e )
		{
			throw new UCEFException("Failed to initialize HLA encode/decode utilities.", e);
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Decode HLA byte array representation of a short
	 * 
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public short asShort( byte[] bytes )
	{
		HLAinteger16BE value = makeHLAType((short)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException(de);
		}
	}
	
	/**
	 * Decode HLA byte array representation of an integer
	 * 
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public int asInt( byte[] bytes )
	{
		HLAinteger32BE value = makeHLAType((int)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException(de);
		}
	}
	
	/**
	 * Decode HLA byte array representation of a long
	 * 
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public long asLong( byte[] bytes )
	{
		HLAinteger64BE value = makeHLAType((long)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException(de);
		}
	}
	
	/**
	 * Decode HLA byte array representation of a float
	 * 
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public float asFloat( byte[] bytes )
	{
		HLAfloat32BE value = makeHLAType((float)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException(de);
		}
	}
	
	/**
	 * Decode HLA byte array representation of a double
	 * 
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public double asDouble( byte[] bytes )
	{
		HLAfloat64BE value = makeHLAType((double)0);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException(de);
		}
	}
	
	/**
	 * Decode HLA byte array representation of a boolean
	 * 
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public boolean asBoolean( byte[] bytes )
	{
		HLAboolean value = makeHLAType(false);
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			throw new UCEFException(de);
		}
	}

	/**
	 * Decode HLA byte array representation of a string
	 * 
	 * @param bytes the HLA byte array
	 * @return the decoded value
	 */
	public String asString( byte[] bytes )
	{
		HLAunicodeString value = makeHLAType("");
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
			throw new UCEFException(de);
		}
	}

	/**
	 * Encode a short to an HLA byte array representation 
	 * 
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public byte[] encode(short value)
	{
		return makeHLAType(value).toByteArray();
	}
	
	/**
	 * Encode an integer to an HLA byte array representation 
	 * 
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public byte[] encode(int value)
	{
		return makeHLAType(value).toByteArray();
	}
	
	/**
	 * Encode a long to an HLA byte array representation 
	 * 
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public byte[] encode(long value)
	{
		return makeHLAType(value).toByteArray();
	}
	
	/**
	 * Encode a double to an HLA byte array representation 
	 * 
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public byte[] encode(double value)
	{
		return makeHLAType(value).toByteArray();
	}
	
	/**
	 * Encode a float to an HLA byte array representation 
	 * 
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public byte[] encode(float value)
	{
		return makeHLAType(value).toByteArray();
	}
	
	/**
	 * Encode a boolean to an HLA byte array representation 
	 * 
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public byte[] encode(boolean value)
	{
		return makeHLAType(value).toByteArray();
	}
	
	/**
	 * Encode a string to an HLA byte array representation 
	 * 
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	public byte[] encode(String value)
	{
		return makeHLAType(value).toByteArray();
	}
	
	/**
	 * Encode a short to an {@link HLAinteger16BE} representation 
	 * 
	 * @param value the value to encode 
	 * @return the {@link HLAinteger16BE}
	 */
	private HLAinteger16BE makeHLAType(short value)
	{
		return encoderFactory.createHLAinteger16BE(value);
	}
	
	/**
	 * Encode an integer to an {@link HLAinteger32BE} representation 
	 * 
	 * @param value the value to encode 
	 * @return the {@link HLAinteger32BE}
	 */
	private HLAinteger32BE makeHLAType(int value)
	{
		return encoderFactory.createHLAinteger32BE(value);
	}
	
	/**
	 * Encode a long to an {@link HLAinteger64BE} representation 
	 * 
	 * @param value the value to encode 
	 * @return the HLA byte array
	 */
	private HLAinteger64BE makeHLAType(long value)
	{
		return encoderFactory.createHLAinteger64BE(value);
	}
	
	/**
	 * Encode a double to an {@link HLAfloat64BE} representation 
	 * 
	 * @param value the value to encode 
	 * @return the {@link HLAfloat64BE}
	 */
	private HLAfloat64BE makeHLAType(double value)
	{
		return encoderFactory.createHLAfloat64BE(value);
	}
	
	/**
	 * Encode a float to an {@link HLAfloat32BE} representation 
	 * 
	 * @param value the value to encode 
	 * @return the {@link HLAfloat32BE}
	 */
	private HLAfloat32BE makeHLAType(float value)
	{
		return encoderFactory.createHLAfloat32BE(value);
	}
	
	/**
	 * Encode a boolean to an {@link HLAboolean} representation 
	 * 
	 * @param value the value to encode 
	 * @return the {@link HLAboolean}
	 */
	private HLAboolean makeHLAType(boolean value)
	{
		return encoderFactory.createHLAboolean(value);
	}
	
	/**
	 * Encode a string to an {@link HLAunicodeString} representation 
	 * 
	 * @param value the value to encode 
	 * @return the {@link HLAunicodeString}
	 */
	private HLAunicodeString makeHLAType(String value)
	{
		return encoderFactory.createHLAunicodeString(value == null ? "" : value);
	}
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
}

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
package gov.nist.ucef.hla.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.common.UCEFException;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIinternalError;

/**
 * The purpose of this class is to provide encoding/decoding of various data types to/from HLA 
 * standard representations
 */
public class HLACodecUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    private static final Logger logger = LogManager.getFormatterLogger(HLACodecUtils.class);
	
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
			throw new UCEFException("Failed to initialize HLA codec utilities.", e);
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String decodeHLAString( byte[] bytes )
	{
		HLAunicodeString value = makeHLAString();
		// decode
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			logger.error( "Decoder Exception: "+de.getMessage() );
		}
		return "";
	}

	public short decodeHLAShort( byte[] bytes )
	{
		HLAinteger16BE value = makeHLAShort();
		// decode
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			logger.error( "Decoder Exception: "+de.getMessage() );
		}
		return 0;
	}
	
	public int decodeHLAInt( byte[] bytes )
	{
		HLAinteger32BE value = makeHLAInt();
		// decode
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			logger.error( "Decoder Exception: "+de.getMessage() );
		}
		return 0;
	}
	
	public long decodeHLALong( byte[] bytes )
	{
		HLAinteger64BE value = makeHLALong();
		// decode
		try
		{
			value.decode( bytes );
			return value.getValue();
		}
		catch( DecoderException de )
		{
			de.printStackTrace();
		}
		return 0L;
	}
	
	public HLAunicodeString makeHLAString()
	{
		return encoderFactory.createHLAunicodeString("");
	}
	
	public HLAunicodeString makeHLAString(String value)
	{
		return encoderFactory.createHLAunicodeString(value == null ? "" : value);
	}
	
	public HLAinteger16BE makeHLAShort()
	{
		return makeHLAShort((short)0);
	}

	public HLAinteger16BE makeHLAShort(short value)
	{
		return encoderFactory.createHLAinteger16BE(value);
	}
	
	public HLAinteger32BE makeHLAInt()
	{
		return makeHLAInt(0);
	}
	
	public HLAinteger32BE makeHLAInt(int value)
	{
		return encoderFactory.createHLAinteger32BE(value);
	}
	
	public HLAinteger64BE makeHLALong()
	{
		return makeHLALong(0);
	}
	
	public HLAinteger64BE makeHLALong(long value)
	{
		return encoderFactory.createHLAinteger64BE(value);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
}

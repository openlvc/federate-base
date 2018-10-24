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

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIinternalError;

public class HLACodecUtils
{

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    private static final Logger logger = LogManager.getFormatterLogger(HLACodecUtils.class);
	
	private static HLACodecUtils instance;

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
			this.encoderFactory = null;
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String decodeString( byte[] bytes )
	{
		HLAunicodeString value = makeString();
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

	public short decodeShort( byte[] bytes )
	{
		HLAinteger16BE value = makeShort();
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
	
	public int decodeInt( byte[] bytes )
	{
		HLAinteger32BE value = makeInt();
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
	
	public long decodeLong( byte[] bytes )
	{
		HLAinteger64BE value = makeLong();
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
	
	public HLAunicodeString makeString()
	{
		return encoderFactory.createHLAunicodeString("");
	}
	
	public HLAunicodeString makeString(String value)
	{
		return encoderFactory.createHLAunicodeString(value == null ? "" : value);
	}
	
	public HLAinteger16BE makeShort()
	{
		return makeShort((short)0);
	}

	public HLAinteger16BE makeShort(short value)
	{
		return encoderFactory.createHLAinteger16BE(value);
	}
	
	public HLAinteger32BE makeInt()
	{
		return makeInt(0);
	}
	
	public HLAinteger32BE makeInt(int value)
	{
		return encoderFactory.createHLAinteger32BE(value);
	}
	
	public HLAinteger64BE makeLong()
	{
		return makeLong(0);
	}
	
	public HLAinteger64BE makeLong(long value)
	{
		return encoderFactory.createHLAinteger64BE(value);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

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
	
}

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
package gov.nist.ucef.hla.base.common;

import gov.nist.ucef.hla.base.HLACodecUtils;
import hla.rti1516e.encoding.EncoderFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HLACodecUtilsTest extends TestCase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private EncoderFactory encoder;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public HLACodecUtilsTest( String testName )
	{
		super( testName );
	}


	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void setUp()
	{
		this.encoder = HLACodecUtils.getEncoder();
	}

	/**
	 * Test encoding/decoding of character values
	 */
	public void testChar()
	{
		char[] testValues = {' ', 'A', 'a', '字'};
		for(char expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			char actual = HLACodecUtils.asChar( this.encoder,
			                                    HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	/**
	 * Test encoding/decoding of short values
	 */
	public void testShort()
	{
		short[] testValues = {(short)0, (short)1234, (short)-1234, Short.MAX_VALUE, Short.MIN_VALUE};
		for(short expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			short actual = HLACodecUtils.asShort( this.encoder,
			                                      HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	/**
	 * Test encoding/decoding of integer values
	 */
	public void testInt()
	{
		int[] testValues = {0, 1234, -1234, Integer.MAX_VALUE, Integer.MIN_VALUE};
		for(int expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			int actual = HLACodecUtils.asInt( this.encoder,
			                                  HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	/**
	 * Test encoding/decoding of long values
	 */
	public void testLong()
	{
		long[] testValues = {0L, 123456L, -123456L, Long.MAX_VALUE, Long.MIN_VALUE};
		for(long expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			long actual = HLACodecUtils.asLong( this.encoder,
			                                    HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	/**
	 * Test encoding/decoding of float values
	 */
	public void testFloat()
	{
		float[] testValues = {0F, 123.456F, -123.456F, Float.MAX_VALUE, Float.MIN_VALUE};
		for(float expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			float actual = HLACodecUtils.asFloat( this.encoder,
			                                      HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	/**
	 * Test encoding/decoding of double values
	 */
	public void testDouble()
	{
		double[] testValues = {0.0, 123.456, -123.456, Double.MAX_VALUE, Double.MIN_VALUE};
		for(double expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			double actual = HLACodecUtils.asDouble( this.encoder,
			                                        HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	/**
	 * Test encoding/decoding of byte values
	 */
	public void testByte()
	{
		byte[] testValues = {(byte)0, (byte)1,(byte)-1,(byte)123,(byte)-123};
		for(byte expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			byte actual = HLACodecUtils.asByte( this.encoder,
			                                    HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	/**
	 * Test encoding/decoding of boolean values
	 */
	public void testBoolean()
	{
		boolean[] testValues = {true, false};
		for(boolean expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			boolean actual = HLACodecUtils.asBoolean( this.encoder,
			                                          HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	/**
	 * Test encoding/decoding of strings
	 */
	public void testString()
	{
		String[] testValues = {"", "A", "Alphabet Soup", "日本語"};
		for(String expected : testValues)
		{
			// run through an encode/decode cycle, should come out the same
			String actual = HLACodecUtils.asString( this.encoder,
			                                        HLACodecUtils.encode( this.encoder, expected ) );
			assertEquals( expected, actual );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( HLACodecUtilsTest.class );
	}
}

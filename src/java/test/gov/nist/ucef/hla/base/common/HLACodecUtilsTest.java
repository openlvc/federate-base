/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef.
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
package gov.nist.ucef.hla.base.common;

import gov.nist.ucef.hla.base.HLACodecUtils;
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
	private HLACodecUtils codecUtils;

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
		this.codecUtils = HLACodecUtils.instance();
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
			short actual = codecUtils.asShort( codecUtils.encode( expected ).toByteArray() );
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
			int actual = codecUtils.asInt( codecUtils.encode( expected ).toByteArray() );
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
			long actual = codecUtils.asLong( codecUtils.encode( expected ).toByteArray() );
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
			float actual = codecUtils.asFloat( codecUtils.encode( expected ).toByteArray() );
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
			double actual = codecUtils.asDouble( codecUtils.encode( expected ).toByteArray() );
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
			boolean actual = codecUtils.asBoolean( codecUtils.encode( expected ).toByteArray() );
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
			String actual = codecUtils.asString( codecUtils.encode( expected ).toByteArray() );
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

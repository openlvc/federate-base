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
package gov.nist.ucef.hla.common;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SynchronizationPointTest extends TestCase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public SynchronizationPointTest( String testName )
	{
		super( testName );
	}


	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This is trivial test which validates that the IDs of the various
	 * {@link SyncPoint}s have not been changed without due consideration.
	 * 
	 * If a change is made to the IDs, and the change is correct, this test should be updated to
	 * reflect the new state of affairs.
	 */
	public void testIDs()
	{
		assertEquals( "readyToPopulate", SyncPoint.READY_TO_POPULATE.getID() );
		assertEquals( "readyToRun", SyncPoint.READY_TO_RUN.getID() );
		assertEquals( "readyToResign", SyncPoint.READY_TO_RESIGN.getID() );
	}

	/**
	 * This is trivial test which validates that the IDs of the various
	 * {@link SyncPoint}s have not been changed without due consideration.
	 * 
	 * If a change is made to the IDs, and the change is correct, this test should be updated to
	 * reflect the new state of affairs.
	 */
	public void testFromID()
	{
		for( SyncPoint sp : SyncPoint.values() )
		{
			assertEquals( sp, SyncPoint.fromID( sp.getID() ) );
		}

		// confirm that null is returned when trying to create a SynchronizationPoint from an invalid ID
		assertEquals( null, SyncPoint.fromID( "invalidID" ) );
		
		// confirm that null is returned when trying to create a SynchronizationPoint from a null ID
		assertEquals( null, SyncPoint.fromID( null ) );
	}
	
	/**
	 * This test which verifies that the chronological ordering of the {@link SyncPoint}s
	 * is reflected in the results of calls to the {@link SyncPoint#isBefore(SyncPoint)}
	 * and {@link SyncPoint#isAfter(SyncPoint)} methods.
	 */
	public void testOrder()
	{
		// isBefore() and isAfter() will always return false if the 
		// other SynchronizationPoint is null 
		for( SyncPoint sp : SyncPoint.values() )
		{
			assertFalse( sp.isBefore( null ) );
			assertFalse( sp.isAfter( null ) );
		}
		
		// READY_TO_POPULATE
		assertFalse( SyncPoint.READY_TO_POPULATE.isBefore( SyncPoint.READY_TO_POPULATE ));
		assertTrue( SyncPoint.READY_TO_POPULATE.isBefore( SyncPoint.READY_TO_RUN ));
		assertTrue( SyncPoint.READY_TO_POPULATE.isBefore( SyncPoint.READY_TO_RESIGN ));
		assertFalse( SyncPoint.READY_TO_POPULATE.isAfter( SyncPoint.READY_TO_POPULATE ));
		assertFalse( SyncPoint.READY_TO_POPULATE.isAfter( SyncPoint.READY_TO_RUN ));
		assertFalse( SyncPoint.READY_TO_POPULATE.isAfter( SyncPoint.READY_TO_RESIGN ));
		
		// READY_TO_RUN
		assertFalse( SyncPoint.READY_TO_RUN.isBefore( SyncPoint.READY_TO_POPULATE ));
		assertFalse( SyncPoint.READY_TO_RUN.isBefore( SyncPoint.READY_TO_RUN ));
		assertTrue( SyncPoint.READY_TO_RUN.isBefore( SyncPoint.READY_TO_RESIGN ));
		assertTrue( SyncPoint.READY_TO_RUN.isAfter( SyncPoint.READY_TO_POPULATE ));
		assertFalse( SyncPoint.READY_TO_RUN.isAfter( SyncPoint.READY_TO_RUN ));
		assertFalse( SyncPoint.READY_TO_RUN.isAfter( SyncPoint.READY_TO_RESIGN ));
		
		// READY_TO_RESIGN
		assertFalse( SyncPoint.READY_TO_RESIGN.isBefore( SyncPoint.READY_TO_POPULATE ));
		assertFalse( SyncPoint.READY_TO_RESIGN.isBefore( SyncPoint.READY_TO_RUN ));
		assertFalse( SyncPoint.READY_TO_RESIGN.isBefore( SyncPoint.READY_TO_RESIGN ));
		assertTrue( SyncPoint.READY_TO_RESIGN.isAfter( SyncPoint.READY_TO_POPULATE ));
		assertTrue( SyncPoint.READY_TO_RESIGN.isAfter( SyncPoint.READY_TO_RUN ));
		assertFalse( SyncPoint.READY_TO_RESIGN.isAfter( SyncPoint.READY_TO_RESIGN ));
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
		return new TestSuite( SynchronizationPointTest.class );
	}
}

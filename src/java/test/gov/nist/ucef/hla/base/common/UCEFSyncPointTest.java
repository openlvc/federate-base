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

import gov.nist.ucef.hla.base.UCEFSyncPoint;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UCEFSyncPointTest extends TestCase
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
	public UCEFSyncPointTest( String testName )
	{
		super( testName );
	}


	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This is trivial test which validates that the IDs of the various
	 * {@link UCEFSyncPoint}s have not been changed without due consideration.
	 * 
	 * If a change is made to the IDs, and the change is correct, this test should be updated to
	 * reflect the new state of affairs.
	 */
	public void testIDs()
	{
		assertEquals( "readyToPopulate", UCEFSyncPoint.READY_TO_POPULATE.getLabel() );
		assertEquals( "readyToRun", UCEFSyncPoint.READY_TO_RUN.getLabel() );
		assertEquals( "readyToResign", UCEFSyncPoint.READY_TO_RESIGN.getLabel() );
	}

	/**
	 * This is trivial test which checks that no {@link UCEFSyncPoint}s are added or removed
	 * without due consideration.
	 * 
	 * If a change is made to the {@link UCEFSyncPoint}s, and the change is correct, this test should be 
	 * updated to reflect the new state of affairs.
	 */
	public void testExpectedUCEFSyncPoints()
	{
		// these are the ONLY synchronisation points we expect. If there are others,
		// we need to make sure that the code is properly updated. If any are removed.
		// this test will cause compilation errors
		UCEFSyncPoint[] expectedUCEFSyncPoints = new UCEFSyncPoint[]{ UCEFSyncPoint.READY_TO_POPULATE,
		                                                  UCEFSyncPoint.READY_TO_RUN,
		                                                  UCEFSyncPoint.READY_TO_RESIGN };
		
		for(UCEFSyncPoint UCEFSyncPoint : UCEFSyncPoint.values())
		{
			boolean found = false;
			for(UCEFSyncPoint expectedUCEFSyncPoint : expectedUCEFSyncPoints)
			{
				if(UCEFSyncPoint.equals(expectedUCEFSyncPoint))
				{
					found = true;
					break;
				}
			}
			
			// if this assertion fails, it means that we have come across a new synchronization point
			// which was not around when this test was written. Ensure that the extra synchronization
			// point is valid and all required code changes have been made, and then update this test.
			assertEquals(String.format("The SynchronizationPoint with ID '%s' was not expected - has it been added?.", UCEFSyncPoint.getLabel()), true, found);
		}
	}
	
	/**
	 * This is trivial test which validates that the IDs of the various
	 * {@link UCEFSyncPoint}s have not been changed without due consideration.
	 * 
	 * If a change is made to the IDs, and the change is correct, this test should be updated to
	 * reflect the new state of affairs.
	 */
	public void testFromID()
	{
		for( UCEFSyncPoint sp : UCEFSyncPoint.values() )
		{
			assertEquals( sp, UCEFSyncPoint.fromLabel( sp.getLabel() ) );
		}

		// confirm that null is returned when trying to create a SynchronizationPoint from an invalid ID
		assertEquals( null, UCEFSyncPoint.fromLabel( "invalidID" ) );
		
		// confirm that null is returned when trying to create a SynchronizationPoint from a null ID
		assertEquals( null, UCEFSyncPoint.fromLabel( null ) );
	}
	
	/**
	 * This test which verifies that the chronological ordering of the {@link UCEFSyncPoint}s
	 * is reflected in the results of calls to the {@link UCEFSyncPoint#isBefore(UCEFSyncPoint)}
	 * and {@link UCEFSyncPoint#isAfter(UCEFSyncPoint)} methods.
	 */
	public void testOrder()
	{
		// isBefore() and isAfter() will always return false if the 
		// other SynchronizationPoint is null 
		for( UCEFSyncPoint sp : UCEFSyncPoint.values() )
		{
			assertFalse( sp.isBefore( null ) );
			assertFalse( sp.isAfter( null ) );
		}
		
		// READY_TO_POPULATE
		assertFalse( UCEFSyncPoint.READY_TO_POPULATE.isBefore( UCEFSyncPoint.READY_TO_POPULATE ));
		assertTrue( UCEFSyncPoint.READY_TO_POPULATE.isBefore( UCEFSyncPoint.READY_TO_RUN ));
		assertTrue( UCEFSyncPoint.READY_TO_POPULATE.isBefore( UCEFSyncPoint.READY_TO_RESIGN ));
		assertFalse( UCEFSyncPoint.READY_TO_POPULATE.isAfter( UCEFSyncPoint.READY_TO_POPULATE ));
		assertFalse( UCEFSyncPoint.READY_TO_POPULATE.isAfter( UCEFSyncPoint.READY_TO_RUN ));
		assertFalse( UCEFSyncPoint.READY_TO_POPULATE.isAfter( UCEFSyncPoint.READY_TO_RESIGN ));
		
		// READY_TO_RUN
		assertFalse( UCEFSyncPoint.READY_TO_RUN.isBefore( UCEFSyncPoint.READY_TO_POPULATE ));
		assertFalse( UCEFSyncPoint.READY_TO_RUN.isBefore( UCEFSyncPoint.READY_TO_RUN ));
		assertTrue( UCEFSyncPoint.READY_TO_RUN.isBefore( UCEFSyncPoint.READY_TO_RESIGN ));
		assertTrue( UCEFSyncPoint.READY_TO_RUN.isAfter( UCEFSyncPoint.READY_TO_POPULATE ));
		assertFalse( UCEFSyncPoint.READY_TO_RUN.isAfter( UCEFSyncPoint.READY_TO_RUN ));
		assertFalse( UCEFSyncPoint.READY_TO_RUN.isAfter( UCEFSyncPoint.READY_TO_RESIGN ));
		
		// READY_TO_RESIGN
		assertFalse( UCEFSyncPoint.READY_TO_RESIGN.isBefore( UCEFSyncPoint.READY_TO_POPULATE ));
		assertFalse( UCEFSyncPoint.READY_TO_RESIGN.isBefore( UCEFSyncPoint.READY_TO_RUN ));
		assertFalse( UCEFSyncPoint.READY_TO_RESIGN.isBefore( UCEFSyncPoint.READY_TO_RESIGN ));
		assertTrue( UCEFSyncPoint.READY_TO_RESIGN.isAfter( UCEFSyncPoint.READY_TO_POPULATE ));
		assertTrue( UCEFSyncPoint.READY_TO_RESIGN.isAfter( UCEFSyncPoint.READY_TO_RUN ));
		assertFalse( UCEFSyncPoint.READY_TO_RESIGN.isAfter( UCEFSyncPoint.READY_TO_RESIGN ));
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
		return new TestSuite( UCEFSyncPointTest.class );
	}
}
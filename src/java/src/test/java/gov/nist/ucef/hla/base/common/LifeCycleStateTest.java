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

import gov.nist.ucef.hla.base.LifecycleState;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LifeCycleStateTest extends TestCase
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
	public LifeCycleStateTest( String testName )
	{
		super( testName );
	}


	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This is trivial test which validates that the IDs of the various
	 * {@link LifecycleState}s have not been changed without due consideration.
	 * 
	 * If a change is made to the IDs, and the change is correct, this test should be updated to
	 * reflect the new state of affairs.
	 */
	public void testIDs()
	{
		assertEquals( "gestating", LifecycleState.GESTATING.getLabel());
		assertEquals( "initializing", LifecycleState.INITIALIZING.getLabel());
		assertEquals( "initialized", LifecycleState.INITIALIZED.getLabel());
		assertEquals( "running", LifecycleState.RUNNING.getLabel());
		assertEquals( "cleaningUp", LifecycleState.CLEANING_UP.getLabel());
		assertEquals( "expired", LifecycleState.EXPIRED.getLabel());
	}

	/**
	 * This is trivial test which checks that no {@link LifecycleState}s are added or removed
	 * without due consideration.
	 * 
	 * If a change is made to the {@link LifecycleState}s, and the change is correct, this test should be 
	 * updated to reflect the new state of affairs.
	 */
	public void testExpectedLifecycleStates()
	{
		// these are the ONLY synchronisation points we expect. If there are others,
		// we need to make sure that the code is properly updated. If any are removed.
		// this test will cause compilation errors
		LifecycleState[] expectedLifecycleStates = new LifecycleState[]{ LifecycleState.GESTATING,
		                                                                 LifecycleState.INITIALIZING,
		                                                                 LifecycleState.INITIALIZED,
		                                                                 LifecycleState.RUNNING,
		                                                                 LifecycleState.CLEANING_UP,
		                                                                 LifecycleState.EXPIRED,
		                                                              };
		
		for(LifecycleState lifecycleState : LifecycleState.values())
		{
			boolean found = false;
			for(LifecycleState expectedLifecycleState : expectedLifecycleStates)
			{
				if(lifecycleState.equals(expectedLifecycleState))
				{
					found = true;
					break;
				}
			}
			
			// if this assertion fails, it means that we have come across a new synchronization point
			// which was not around when this test was written. Ensure that the extra synchronization
			// point is valid and all required code changes have been made, and then update this test.
			assertEquals(String.format("The LifecycleState with ID '%s' was not expected - has it been added?.", lifecycleState.getLabel()), true, found);
		}
	}
	
	/**
	 * This is trivial test which validates that the IDs of the various
	 * {@link LifecycleState}s have not been changed without due consideration.
	 * 
	 * If a change is made to the IDs, and the change is correct, this test should be updated to
	 * reflect the new state of affairs.
	 */
	public void testFromID()
	{
		for( LifecycleState sp : LifecycleState.values() )
		{
			assertEquals( sp, LifecycleState.fromLabel( sp.getLabel() ) );
		}

		// confirm that null is returned when trying to create a LifecycleState from an invalid ID
		assertEquals( null, LifecycleState.fromLabel( "invalidID" ) );
		
		// confirm that null is returned when trying to create a LifecycleState from a null ID
		assertEquals( null, LifecycleState.fromLabel( null ) );
	}
	
	/**
	 * This test which verifies that the chronological ordering of the {@link LifecycleState}s
	 * is reflected in the results of calls to the {@link LifecycleState#isBefore(LifecycleState)},
	 * {@link LifecycleState#isAfter(LifecycleState)} and related methods.
	 */
	public void testOrder()
	{
		// isBefore() and isAfter() will always return false if the 
		// other SynchronizationPoint is null 
		for( LifecycleState sp : LifecycleState.values() )
		{
			assertFalse( sp.isBefore( null ) );
			assertFalse( sp.isAfter( null ) );
		}
		
		// GESTATING
		assertFalse( LifecycleState.GESTATING.isBefore( LifecycleState.GESTATING ));
		assertTrue( LifecycleState.GESTATING.isBefore( LifecycleState.INITIALIZING ));
		assertTrue( LifecycleState.GESTATING.isBefore( LifecycleState.INITIALIZED ));
		assertTrue( LifecycleState.GESTATING.isBefore( LifecycleState.RUNNING ));
		assertTrue( LifecycleState.GESTATING.isBefore( LifecycleState.CLEANING_UP ));
		assertTrue( LifecycleState.GESTATING.isBefore( LifecycleState.EXPIRED ));
		assertFalse( LifecycleState.GESTATING.isAfter( LifecycleState.GESTATING ));
		assertFalse( LifecycleState.GESTATING.isAfter( LifecycleState.INITIALIZING ));
		assertFalse( LifecycleState.GESTATING.isAfter( LifecycleState.INITIALIZED ));
		assertFalse( LifecycleState.GESTATING.isAfter( LifecycleState.RUNNING ));
		assertFalse( LifecycleState.GESTATING.isAfter( LifecycleState.CLEANING_UP ));
		assertFalse( LifecycleState.GESTATING.isAfter( LifecycleState.EXPIRED ));
		
		// INITIALIZING
		assertFalse( LifecycleState.INITIALIZING.isBefore( LifecycleState.GESTATING ));
		assertFalse( LifecycleState.INITIALIZING.isBefore( LifecycleState.INITIALIZING ));
		assertTrue( LifecycleState.INITIALIZING.isBefore( LifecycleState.INITIALIZED ));
		assertTrue( LifecycleState.INITIALIZING.isBefore( LifecycleState.RUNNING ));
		assertTrue( LifecycleState.INITIALIZING.isBefore( LifecycleState.CLEANING_UP ));
		assertTrue( LifecycleState.INITIALIZING.isBefore( LifecycleState.EXPIRED ));
		assertTrue( LifecycleState.INITIALIZING.isAfter( LifecycleState.GESTATING ));
		assertFalse( LifecycleState.INITIALIZING.isAfter( LifecycleState.INITIALIZING ));
		assertFalse( LifecycleState.INITIALIZING.isAfter( LifecycleState.INITIALIZED ));
		assertFalse( LifecycleState.INITIALIZING.isAfter( LifecycleState.RUNNING ));
		assertFalse( LifecycleState.INITIALIZING.isAfter( LifecycleState.CLEANING_UP ));
		assertFalse( LifecycleState.INITIALIZING.isAfter( LifecycleState.EXPIRED ));
		
		// INITIALIZED
		assertFalse( LifecycleState.INITIALIZED.isBefore( LifecycleState.GESTATING ));
		assertFalse( LifecycleState.INITIALIZED.isBefore( LifecycleState.INITIALIZING ));
		assertFalse( LifecycleState.INITIALIZED.isBefore( LifecycleState.INITIALIZED ));
		assertTrue( LifecycleState.INITIALIZED.isBefore( LifecycleState.RUNNING ));
		assertTrue( LifecycleState.INITIALIZED.isBefore( LifecycleState.CLEANING_UP ));
		assertTrue( LifecycleState.INITIALIZED.isBefore( LifecycleState.EXPIRED ));
		assertTrue( LifecycleState.INITIALIZED.isAfter( LifecycleState.GESTATING ));
		assertTrue( LifecycleState.INITIALIZED.isAfter( LifecycleState.INITIALIZING ));
		assertFalse( LifecycleState.INITIALIZED.isAfter( LifecycleState.INITIALIZED ));
		assertFalse( LifecycleState.INITIALIZED.isAfter( LifecycleState.RUNNING ));
		assertFalse( LifecycleState.INITIALIZED.isAfter( LifecycleState.CLEANING_UP ));
		assertFalse( LifecycleState.INITIALIZED.isAfter( LifecycleState.EXPIRED ));
		
		// RUNNING
		assertFalse( LifecycleState.RUNNING.isBefore( LifecycleState.GESTATING ));
		assertFalse( LifecycleState.RUNNING.isBefore( LifecycleState.INITIALIZING ));
		assertFalse( LifecycleState.RUNNING.isBefore( LifecycleState.INITIALIZED ));
		assertFalse( LifecycleState.RUNNING.isBefore( LifecycleState.RUNNING ));
		assertTrue( LifecycleState.RUNNING.isBefore( LifecycleState.CLEANING_UP ));
		assertTrue( LifecycleState.RUNNING.isBefore( LifecycleState.EXPIRED ));
		assertTrue( LifecycleState.RUNNING.isAfter( LifecycleState.GESTATING ));
		assertTrue( LifecycleState.RUNNING.isAfter( LifecycleState.INITIALIZING ));
		assertTrue( LifecycleState.RUNNING.isAfter( LifecycleState.INITIALIZED ));
		assertFalse( LifecycleState.RUNNING.isAfter( LifecycleState.RUNNING ));
		assertFalse( LifecycleState.RUNNING.isAfter( LifecycleState.CLEANING_UP ));
		assertFalse( LifecycleState.RUNNING.isAfter( LifecycleState.EXPIRED ));
		
		// CLEANING_UP
		assertFalse( LifecycleState.CLEANING_UP.isBefore( LifecycleState.GESTATING ));
		assertFalse( LifecycleState.CLEANING_UP.isBefore( LifecycleState.INITIALIZING ));
		assertFalse( LifecycleState.CLEANING_UP.isBefore( LifecycleState.INITIALIZED ));
		assertFalse( LifecycleState.CLEANING_UP.isBefore( LifecycleState.RUNNING ));
		assertFalse( LifecycleState.CLEANING_UP.isBefore( LifecycleState.CLEANING_UP ));
		assertTrue( LifecycleState.CLEANING_UP.isBefore( LifecycleState.EXPIRED ));
		assertTrue( LifecycleState.CLEANING_UP.isAfter( LifecycleState.GESTATING ));
		assertTrue( LifecycleState.CLEANING_UP.isAfter( LifecycleState.INITIALIZING ));
		assertTrue( LifecycleState.CLEANING_UP.isAfter( LifecycleState.INITIALIZED ));
		assertTrue( LifecycleState.CLEANING_UP.isAfter( LifecycleState.RUNNING ));
		assertFalse( LifecycleState.CLEANING_UP.isAfter( LifecycleState.CLEANING_UP ));
		assertFalse( LifecycleState.CLEANING_UP.isAfter( LifecycleState.EXPIRED ));
		
		// EXPIRED
		assertFalse( LifecycleState.EXPIRED.isBefore( LifecycleState.GESTATING ));
		assertFalse( LifecycleState.EXPIRED.isBefore( LifecycleState.INITIALIZING ));
		assertFalse( LifecycleState.EXPIRED.isBefore( LifecycleState.INITIALIZED ));
		assertFalse( LifecycleState.EXPIRED.isBefore( LifecycleState.RUNNING ));
		assertFalse( LifecycleState.EXPIRED.isBefore( LifecycleState.CLEANING_UP ));
		assertFalse( LifecycleState.EXPIRED.isBefore( LifecycleState.EXPIRED ));
		assertTrue( LifecycleState.EXPIRED.isAfter( LifecycleState.GESTATING ));
		assertTrue( LifecycleState.EXPIRED.isAfter( LifecycleState.INITIALIZING ));
		assertTrue( LifecycleState.EXPIRED.isAfter( LifecycleState.INITIALIZED ));
		assertTrue( LifecycleState.EXPIRED.isAfter( LifecycleState.RUNNING ));
		assertTrue( LifecycleState.EXPIRED.isAfter( LifecycleState.CLEANING_UP ));
		assertFalse( LifecycleState.EXPIRED.isAfter( LifecycleState.EXPIRED ));
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
		return new TestSuite( LifeCycleStateTest.class );
	}
}

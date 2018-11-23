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
package gov.nist.ucef.hla.base.common.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nist.ucef.hla.base.FederateConfiguration;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FederateConfigurationTest extends TestCase
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
	public FederateConfigurationTest( String testName )
	{
		super( testName );
	}


	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This just tests that the simplest constructor works as expected
	 */
	public void testBasicConstructor()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );

		// configuration should be writable at this point
		assertEquals( false, config.isFrozen());
			
		assertEquals( federationName, config.getFederationName() );
		assertEquals( federateName, config.getFederateName() );
		assertEquals( federateType, config.getFederateType() );
		
		assertEquals(5, config.getMaxReconnectAttempts());
		assertEquals(5000, config.getReconnectWaitTime());
		assertEquals(false, config.isLateJoiner());
		assertEquals(true, config.isTimeStepped());
		assertEquals(false, config.callbacksAreEvoked());
		assertEquals(1.0, config.getLookAhead());
		assertEquals(0.1, config.getStepSize());

		assertEquals( 0, config.getModules().size() );
		assertEquals( 0, config.getJoinModules().size() );
		assertEquals( 0, config.getPublishedAttributes().size() );
		assertEquals( 0, config.getSubscribedAttributes().size() );
		assertEquals( 0, config.getPublishedInteractions().size() );
		assertEquals( 0, config.getSubscribedInteractions().size() );
		
		// configuration should still be writable because we haven't frozen it
		assertEquals( false, config.isFrozen());
	}

	/**
	 * This just tests that the basics of the more complex constructor work as expected
	 */
	public void testConfiguration()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";

		// somebody set us up the FOM... 
		Set<URL> expectedModules = new HashSet<>();
		Set<URL> expectedJoinModules = new HashSet<>();
		try
		{
			// modules
			String[] foms = {"a.xml", "b.xml", "c.xml"};
			for(String fom : foms)
			{
				expectedModules.add( new File( "resources/foms/"+fom ).toURI().toURL() );
			}
			
			// join modules
			foms = new String[]{"d.xml", "e.xml", "f.xml", "g.xml"};
			for(String fom : foms)
			{
				expectedJoinModules.add( new File( "resources/foms/"+fom ).toURI().toURL() );
			}
		}
		catch( MalformedURLException urle )
		{
			expectedModules.clear();
			expectedJoinModules.clear();
			
			System.err.println( "Exception loading one of the FOM modules from disk: " + urle.getMessage() );
			urle.printStackTrace();
			
			// bail out now!
			System.exit( 1 );
		}

		// set up maps with classes and corresponding lists of attributes to 
		// be published and subscribed to
		String klassIdBase = "HLAobjectRoot.A.B.C.";
		Map<String, Collection<String>> expectedPublishedAttributes = new HashMap<>();
		expectedPublishedAttributes.put( klassIdBase+"D", new HashSet<>(Arrays.asList( new String[] {"a", "b"} ) ));
		Map<String, Collection<String>> expectedSubscribedAttributes = new HashMap<>();
		expectedSubscribedAttributes.put( klassIdBase+"E", new HashSet<>(Arrays.asList( new String[] {"c", "d", "e"} ) ));
		
		// set up lists of interactions to be published and subscribed to
		String interactionIdBase = "HLAinteractionRoot.T.U.";
		Collection<String> expectedPublishedInteractions = new HashSet<>(Arrays.asList( new String[] {interactionIdBase+"V", interactionIdBase+"W"} ));
		Collection<String> expectedSubscribedInteractions = new HashSet<>(Arrays.asList( new String[] {interactionIdBase+"X", interactionIdBase+"Y", interactionIdBase+"Z"} ));

		int expectedMaxReconnectAttempts = 123;
		int expectedReconnectWaitTime = 54321;
		double expectedStepSize = 1.234;
		double expectedLookAhead = 0.1234;
		boolean expectedIsTimeStepped = true;
		boolean expectedIsLateJoiner = true;
		boolean expectedCallbacksAreEvoked = true;
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName);
		config.addModules( expectedModules )
			  .addJoinModules( expectedJoinModules )
			  .addPublishedAtributes( expectedPublishedAttributes )
			  .addSubscribedAtributes( expectedSubscribedAttributes )
			  .addPublishedInteractions( expectedPublishedInteractions )
			  .addSubscribedInteractions( expectedSubscribedInteractions)
			  .setMaxReconnectAttempts( expectedMaxReconnectAttempts )
			  .setReconnectWaitTime( expectedReconnectWaitTime )
			  .setStepSize( expectedStepSize )
			  .setLookAhead( expectedLookAhead )
			  .setLateJoiner( expectedIsLateJoiner )
			  .setCallbacksAreEvoked( expectedCallbacksAreEvoked )
			  .setTimeStepped( expectedIsTimeStepped );
		
		// configuration should be writeable at this point
		assertEquals( false, config.isFrozen());
		
		assertEquals( federationName, config.getFederationName() );
		assertEquals( federateName, config.getFederateName() );
		assertEquals( federateType, config.getFederateType() );
		
		assertEquals( expectedMaxReconnectAttempts, config.getMaxReconnectAttempts() );
		assertEquals( expectedReconnectWaitTime, config.getReconnectWaitTime() );
		assertEquals( expectedIsLateJoiner, config.isLateJoiner() );
		assertEquals( expectedCallbacksAreEvoked, config.callbacksAreEvoked() );
		assertEquals( expectedIsTimeStepped, config.isTimeStepped() );
		assertEquals( expectedLookAhead, config.getLookAhead() );
		assertEquals( expectedStepSize, config.getStepSize() );
		
		assertEquals( expectedModules.size(), config.getModules().size() );
		assertEquals( expectedJoinModules.size(), config.getJoinModules().size() );
		assertEquals( expectedPublishedAttributes.size(), config.getPublishedAttributes().size() );
		assertEquals( expectedSubscribedAttributes.size(), config.getSubscribedAttributes().size() );
		assertEquals( expectedPublishedInteractions.size(), config.getPublishedInteractions().size() );
		assertEquals( expectedSubscribedInteractions.size(), config.getSubscribedInteractions().size() );
		
		// freeze configuration, should be read only now
		config.freeze();
		assertEquals( true, config.isFrozen());
	}
	
	/**
	 * This tests setting the maximum reconnect attempts 
	 */
	public void testMaxReconnectAttempts()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		int expectedMaxReconnectAttempts = 123;
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedMaxReconnectAttempts != config.getMaxReconnectAttempts() );
		// try changing the value
		config.setMaxReconnectAttempts( expectedMaxReconnectAttempts );
		assertEquals( expectedMaxReconnectAttempts, config.getMaxReconnectAttempts());
		// freeze the config and try to change the value - should not change
		config.freeze();
		config.setMaxReconnectAttempts( expectedMaxReconnectAttempts + 1 );
		assertEquals( expectedMaxReconnectAttempts, config.getMaxReconnectAttempts());
	}
	
	/**
	 * This tests setting the reconnection wait time 
	 */
	public void testReconnectWaitTime()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		int expectedReconnectWaitTime = 54321;
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedReconnectWaitTime != config.getReconnectWaitTime() );
		// try changing the value
		config.setReconnectWaitTime( expectedReconnectWaitTime );
		assertEquals( expectedReconnectWaitTime, config.getReconnectWaitTime());
		// freeze the config and try to change the value - should not change
		config.freeze();
		config.setMaxReconnectAttempts( expectedReconnectWaitTime + 1 );
		assertEquals( expectedReconnectWaitTime, config.getReconnectWaitTime());
	}
	
	/**
	 * This tests setting the step size 
	 */
	public void testStepSize()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		double expectedStepSize = 1.234;
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedStepSize != config.getStepSize() );
		// try changing the value
		config.setStepSize( expectedStepSize );
		assertEquals( expectedStepSize, config.getStepSize());
		// freeze the config and try to change the value - should not change
		config.freeze();
		config.setStepSize( expectedStepSize + 1 );
		assertEquals( expectedStepSize, config.getStepSize());
	}
	
	/**
	 * This tests setting the look ahead 
	 */
	public void testLookAhead()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		double expectedLookAhead = 0.1234;
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedLookAhead != config.getLookAhead() );
		// try changing the value
		config.setLookAhead( expectedLookAhead );
		assertEquals( expectedLookAhead, config.getLookAhead());
		// freeze the config and try to change the value - should not change
		config.freeze();
		config.setLookAhead( expectedLookAhead + 1 );
		assertEquals( expectedLookAhead, config.getLookAhead());
	}
	
	/**
	 * This tests setting the late joiner value 
	 */
	public void testLateJoiner()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		boolean expectedLateJoiner = true;
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedLateJoiner != config.isLateJoiner());
		// try changing the value
		config.setLateJoiner( expectedLateJoiner );
		assertEquals( expectedLateJoiner, config.isLateJoiner());
		// freeze the config and try to change the value - should not change
		config.freeze();
		config.setLateJoiner( !expectedLateJoiner );
		assertEquals( expectedLateJoiner, config.isLateJoiner());
	}
	
	/**
	 * This tests setting whether callbacks are evoked or not 
	 */
	public void testCallbacksAreEvoked()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		boolean expectedCallbacksAreEvoked = true;
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedCallbacksAreEvoked != config.callbacksAreEvoked());
		// try changing the value
		config.setCallbacksAreEvoked( expectedCallbacksAreEvoked );
		assertEquals( expectedCallbacksAreEvoked, config.callbacksAreEvoked());
		// freeze the config and try to change the value - should not change
		config.freeze();
		config.setCallbacksAreEvoked( !expectedCallbacksAreEvoked );
		assertEquals( expectedCallbacksAreEvoked, config.callbacksAreEvoked());
	}
	
	/**
	 * This tests setting the is time stepped value 
	 */
	public void testTimeStepped()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		boolean expectedTimeStepped = false;
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedTimeStepped != config.isTimeStepped());
		// try changing the value
		config.setTimeStepped( expectedTimeStepped );
		assertEquals( expectedTimeStepped, config.isTimeStepped());
		// freeze the config and try to change the value - should not change
		config.freeze();
		config.setTimeStepped( !expectedTimeStepped );
		assertEquals( expectedTimeStepped, config.isTimeStepped());
	}
	
	/**
	 * This tests adding of modules 
	 */
	public void testAddModules()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		// somebody set us up the FOM... 
		List<URL> expectedModules = new ArrayList<>();
		List<URL> extraModules = new ArrayList<>();
		try
		{
			// modules
			String[] foms = {"a.xml", "b.xml", "c.xml"};
			for(String fom : foms)
			{
				expectedModules.add( new File( "resources/foms/"+fom ).toURI().toURL() );
			}
			foms = new String[]{"d.xml", "e.xml"};
			for(String fom : foms)
			{
				extraModules.add( new File( "resources/foms/"+fom ).toURI().toURL() );
			}
		}
		catch( MalformedURLException urle )
		{
			expectedModules.clear();
			extraModules.clear();
			
			System.err.println( "Exception loading one of the FOM modules from disk: " + urle.getMessage() );
			urle.printStackTrace();
			
			// bail out now!
			System.exit( 1 );
		}
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		config.addModules( expectedModules );
		assertEquals( expectedModules.size(), config.getModules().size());
		// add the same modules again - shouldn't increase the size
		config.addModules( expectedModules );
		assertEquals( expectedModules.size(), config.getModules().size());
		// add more modules - should increase the size
		config.addModules( extraModules );
		assertEquals( expectedModules.size() + extraModules.size(), config.getModules().size());

		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// freeze it
		config.freeze();
		// try to add modules - should have done nothing because config is frozen.
		config.addModules( expectedModules );
		assertEquals( 0, config.getModules().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add single module - this actually ends up feeding through the adding
		// a list of modules method
		config.addModule( expectedModules.get( 0 ) );
		assertEquals( 1, config.getModules().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add array of modules - this actually ends up feeding through the adding
		// a list of modules method
		config.addModules( expectedModules.toArray(new URL[0]) );
		assertEquals( expectedModules.size(), config.getModules().size());
	}
	
	/**
	 * This tests adding of join modules 
	 */
	public void testAddJoinModules()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		// somebody set us up the FOM... 
		List<URL> expectedJoinModules = new ArrayList<>();
		List<URL> extraJoinModules = new ArrayList<>();
		try
		{
			// modules
			String[] foms = {"a.xml", "b.xml", "c.xml"};
			for(String fom : foms)
			{
				expectedJoinModules.add( new File( "resources/foms/"+fom ).toURI().toURL() );
			}
			foms = new String[]{"d.xml", "e.xml"};
			for(String fom : foms)
			{
				extraJoinModules.add( new File( "resources/foms/"+fom ).toURI().toURL() );
			}
		}
		catch( MalformedURLException urle )
		{
			expectedJoinModules.clear();
			extraJoinModules.clear();
			
			System.err.println( "Exception loading one of the FOM modules from disk: " + urle.getMessage() );
			urle.printStackTrace();
			
			// bail out now!
			System.exit( 1 );
		}
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		config.addJoinModules( expectedJoinModules );
		assertEquals( expectedJoinModules.size(), config.getJoinModules().size());
		// add the same modules again - shouldn't increase the size
		config.addJoinModules( expectedJoinModules );
		assertEquals( expectedJoinModules.size(), config.getJoinModules().size());
		// add more modules - should increase the size
		config.addJoinModules( extraJoinModules );
		assertEquals( expectedJoinModules.size() + extraJoinModules.size(), config.getJoinModules().size());

		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// freeze it
		config.freeze();
		// try to add modules - should have done nothing because config is frozen.
		config.addJoinModules( expectedJoinModules );
		assertEquals( 0, config.getJoinModules().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add single module - this actually ends up feeding through the adding
		// a list of modules method
		config.addModule( expectedJoinModules.get( 0 ) );
		assertEquals( 1, config.getModules().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add array of modules - this actually ends up feeding through the adding
		// a list of modules method
		config.addModules( expectedJoinModules.toArray(new URL[0]) );
		assertEquals( expectedJoinModules.size(), config.getModules().size());
	}
	
	/**
	 * This tests adding of published attributes 
	 */
	public void testAddPublishedAttributes()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		// set up maps with classes and corresponding lists of attributes to be published
		String objectClass1 = "HLAobjectRoot.A.B.C";
		List<String> attributes1 = new ArrayList<>();
		attributes1.add( "A" );
		attributes1.add( "B" );
		Map<String, Collection<String>> expectedPublishedAttributes = new HashMap<>();
		expectedPublishedAttributes.put( objectClass1, attributes1 );
		
		String objectClass2 = "HLAobjectRoot.X.Y.Z";
		List<String> attributes2 = new ArrayList<>();
		attributes1.add( "M" );
		attributes1.add( "N" );
		Map<String, Collection<String>> extraPublishedAttributes = new HashMap<>();
		extraPublishedAttributes.put( objectClass2, attributes2 );

		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		config.addPublishedAtributes( expectedPublishedAttributes );
		assertEquals( expectedPublishedAttributes.size(), config.getPublishedAttributes().size());
		// add the same attributes again - shouldn't increase the size
		config.addPublishedAtributes( expectedPublishedAttributes );
		assertEquals( expectedPublishedAttributes.size(), config.getPublishedAttributes().size());
		// add more attributes - should increase the size
		config.addPublishedAtributes( extraPublishedAttributes );
		assertEquals( expectedPublishedAttributes.size() + extraPublishedAttributes.size(), config.getPublishedAttributes().size());

		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// freeze it
		config.freeze();
		// try to add subscribed attributes - should have done nothing because config is frozen.
		config.addPublishedAtributes( expectedPublishedAttributes );
		assertEquals( 0, config.getPublishedAttributes().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add single attribute - this actually ends up feeding through the adding
		// a map of subscribed attributes method
		config.addPublishedAtribute( objectClass1, attributes1.get( 0 ) );
		assertEquals( 1, config.getPublishedAttributes().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add multiple attributes on single class with an array - this actually ends up feeding
		// through the adding a map of subscribed attributes method
		config.addPublishedAtributes( objectClass1, attributes1.toArray( new String[0] ) );
		assertEquals( 1, config.getPublishedAttributes().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add multiple attributes on single class with a collection - this actually ends up
		// feeding through the adding a map of subscribed attributes method
		config.addPublishedAtributes( objectClass1, attributes1 );
		assertEquals( 1, config.getPublishedAttributes().size());
	}
	
	/**
	 * This tests adding of subscribed attributes 
	 */
	public void testAddSubscribedAttributes()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		// set up maps with classes and corresponding lists of attributes to be subscribed to
		String objectClass1 = "HLAobjectRoot.A.B.C";
		List<String> attributes1 = new ArrayList<>();
		attributes1.add( "A" );
		attributes1.add( "B" );
		Map<String, Collection<String>> expectedSubscribedAttributes = new HashMap<>();
		expectedSubscribedAttributes.put( objectClass1, attributes1 );
		
		String objectClass2 = "HLAobjectRoot.X.Y.Z";
		List<String> attributes2 = new ArrayList<>();
		attributes1.add( "M" );
		attributes1.add( "N" );
		Map<String, Collection<String>> extraSubscribedAttributes = new HashMap<>();
		extraSubscribedAttributes.put( objectClass2, attributes2 );
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		config.addSubscribedAtributes( expectedSubscribedAttributes );
		assertEquals( expectedSubscribedAttributes.size(), config.getSubscribedAttributes().size());
		// add the same attributes again - shouldn't increase the size
		config.addSubscribedAtributes( expectedSubscribedAttributes );
		assertEquals( expectedSubscribedAttributes.size(), config.getSubscribedAttributes().size());
		// add more attributes - should increase the size
		config.addSubscribedAtributes( extraSubscribedAttributes );
		assertEquals( expectedSubscribedAttributes.size() + extraSubscribedAttributes.size(), config.getSubscribedAttributes().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// freeze it
		config.freeze();
		// try to add subscribed attributes - should have done nothing because config is frozen.
		config.addSubscribedAtributes( expectedSubscribedAttributes );
		assertEquals( 0, config.getSubscribedAttributes().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add single attribute - this actually ends up feeding through the adding
		// a map of subscribed attributes method
		config.addSubscribedAtribute( objectClass1, attributes1.get( 0 ) );
		assertEquals( 1, config.getSubscribedAttributes().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add multiple attributes on single class with an array - this actually ends up feeding
		// through the adding a map of subscribed attributes method
		config.addSubscribedAtributes( objectClass1, attributes1.toArray( new String[0] ) );
		assertEquals( 1, config.getSubscribedAttributes().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add multiple attributes on single class with a collection - this actually ends up
		// feeding through the adding a map of subscribed attributes method
		config.addSubscribedAtributes( objectClass1, attributes1 );
		assertEquals( 1, config.getSubscribedAttributes().size());
	}
	
	/**
	 * This tests adding of published interactions 
	 */
	public void testAddPublishedInteractions()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		// set up lists of interactions to be published
		String interactionIdentifierBase = "HLAinteractionRoot.A.B.C.";
		List<String> expectedInteractions = new ArrayList<>();
		expectedInteractions.add( interactionIdentifierBase+"D" );
		expectedInteractions.add( interactionIdentifierBase+"E" );
		expectedInteractions.add( interactionIdentifierBase+"F" );
		
		String extraInteractionIdentifierBase = "HLAinteractionRoot.V.W.X.";
		List<String> extraInteractions = new ArrayList<>();
		extraInteractions.add( extraInteractionIdentifierBase+"Y" );
		extraInteractions.add( extraInteractionIdentifierBase+"Z" );
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		config.addPublishedInteractions( expectedInteractions );
		assertEquals( expectedInteractions.size(), config.getPublishedInteractions().size());
		// add the same interactions again - shouldn't increase the size
		config.addPublishedInteractions( expectedInteractions );
		assertEquals( expectedInteractions.size(), config.getPublishedInteractions().size());
		// add more interactions - should increase the size
		config.addPublishedInteractions( extraInteractions );
		assertEquals( expectedInteractions.size() + extraInteractions.size(), config.getPublishedInteractions().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// freeze it
		config.freeze();
		// try to add published interactions - should have done nothing because config is frozen.
		config.addPublishedInteractions( expectedInteractions );
		assertEquals( 0, config.getPublishedInteractions().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add single interaction - this actually ends up feeding through the adding
		// a collection of published interactions method
		config.addPublishedInteraction( expectedInteractions.get( 0 ) );
		assertEquals( 1, config.getPublishedInteractions().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add multiple interactions with an array - this actually ends up feeding
		// through the adding a collection of interactions method
		config.addPublishedInteractions( expectedInteractions.toArray( new String[0] ) );
		assertEquals( expectedInteractions.size(), config.getPublishedInteractions().size());
	}
	
	/**
	 * This tests adding of published interactions 
	 */
	public void testAddSubscribedInteractions()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		// set up lists of interactions to be subscribed to
		String interactionIdentifierBase = "HLAinteractionRoot.A.B.C.";
		List<String> expectedInteractions = new ArrayList<>();
		expectedInteractions.add( interactionIdentifierBase+"D" );
		expectedInteractions.add( interactionIdentifierBase+"E" );
		expectedInteractions.add( interactionIdentifierBase+"F" );
		
		String extraInteractionIdentifierBase = "HLAinteractionRoot.V.W.X.";
		List<String> extraInteractions = new ArrayList<>();
		extraInteractions.add( extraInteractionIdentifierBase+"Y" );
		extraInteractions.add( extraInteractionIdentifierBase+"Z" );
		
		FederateConfiguration config = new FederateConfiguration( federateType, federateName, federationName );
		config.addSubscribedInteractions( expectedInteractions );
		assertEquals( expectedInteractions.size(), config.getSubscribedInteractions().size());
		// add the same interactions again - shouldn't increase the size
		config.addSubscribedInteractions( expectedInteractions );
		assertEquals( expectedInteractions.size(), config.getSubscribedInteractions().size());
		// add more interactions - should increase the size
		config.addSubscribedInteractions( extraInteractions );
		assertEquals( expectedInteractions.size() + extraInteractions.size(), config.getSubscribedInteractions().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// freeze it
		config.freeze();
		// try to add published interactions - should have done nothing because config is frozen.
		config.addSubscribedInteractions( expectedInteractions );
		assertEquals( 0, config.getSubscribedInteractions().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add single interaction - this actually ends up feeding through the adding
		// a collection of published interactions method
		config.addSubscribedInteraction( expectedInteractions.get( 0 ) );
		assertEquals( 1, config.getSubscribedInteractions().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateType, federateName, federationName );
		// try to add multiple interactions with an array - this actually ends up feeding
		// through the adding a collection of interactions method
		config.addSubscribedInteractions( expectedInteractions.toArray( new String[0] ) );
		assertEquals( expectedInteractions.size(), config.getSubscribedInteractions().size());
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
		return new TestSuite( FederateConfigurationTest.class );
	}
}

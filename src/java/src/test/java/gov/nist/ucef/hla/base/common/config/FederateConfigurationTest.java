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
import java.util.Map.Entry;
import java.util.Set;

import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.Types.ObjectClass;
import gov.nist.ucef.hla.base.Types.ObjectAttribute;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.Types.InteractionParameter;
import gov.nist.ucef.hla.base.Types.Sharing;
import gov.nist.ucef.hla.base.Types.DataType;
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
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );

		assertEquals( federationName, config.getFederationName() );
		assertEquals( federateName, config.getFederateName() );
		assertEquals( federateType, config.getFederateType() );
		
		assertEquals(5, config.getMaxJoinAttempts());
		assertEquals(5L, config.getJoinRetryInterval());
		assertEquals(true, config.isTimeStepped());
		assertEquals(false, config.callbacksAreEvoked());
		assertEquals(1.0, config.getLookAhead());
		assertEquals(0.1, config.getStepSize());

		assertEquals( 0, config.getModules().size() );
		assertEquals( 0, config.getJoinModules().size() );
		assertEquals( 0, config.getPublishedObjectClasses().size() );
		assertEquals( 0, config.getSubscribedObjectClasses().size() );
		assertEquals( 0, config.getPublishedInteractions().size() );
		assertEquals( 0, config.getSubscribedInteractions().size() );
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
		Map<String, Collection<String>> tempAttributeDefs = new HashMap<>();
		tempAttributeDefs.put( klassIdBase+"D", new HashSet<>(Arrays.asList( new String[] {"a", "b"} ) ));
		List<ObjectClass> expectedPublishedObjectClasses = new ArrayList<>();
		for( Entry<String,Collection<String>> entry : tempAttributeDefs.entrySet() )
		{
			String objectClassName = entry.getKey();
			ObjectClass publishedObjectClass = new ObjectClass( objectClassName, Sharing.PUBLISH );
			for( String attributeName : entry.getValue() )
			{
				publishedObjectClass.addAttribute( new ObjectAttribute( attributeName,
				                                                        DataType.STRING,
				                                                        Sharing.PUBLISH ) );
			}
			expectedPublishedObjectClasses.add(publishedObjectClass);
		}
		
		tempAttributeDefs = new HashMap<>();
		tempAttributeDefs.put( klassIdBase+"E", new HashSet<>(Arrays.asList( new String[] {"c", "d", "e"} ) ));
		List<ObjectClass> expectedSubscribedObjectClasses = new ArrayList<>();
		for( Entry<String,Collection<String>> entry : tempAttributeDefs.entrySet() )
		{
			String objectClassName = entry.getKey();
			ObjectClass publishedObjectClass = new ObjectClass( objectClassName, Sharing.SUBSCRIBE );
			for( String attributeName : entry.getValue() )
			{
				publishedObjectClass.addAttribute( new ObjectAttribute( attributeName,
				                                                        DataType.STRING,
				                                                        Sharing.SUBSCRIBE ) );
			}
			expectedSubscribedObjectClasses.add(publishedObjectClass);
		}
		
		// set up lists of interactions to be published and subscribed to
		String interactionIdBase = "HLAinteractionRoot.T.U.";
		Collection<String> tempInteractionDefs = new HashSet<>(Arrays.asList( new String[] {interactionIdBase+"V", interactionIdBase+"W"} ));
		List<InteractionClass> expectedPublishedInteractions = new ArrayList<>();
		for( String interactionClassName : tempInteractionDefs )
		{
			expectedPublishedInteractions.add( new InteractionClass( interactionClassName, Sharing.PUBLISH ) );
		}
		tempInteractionDefs = new HashSet<>(Arrays.asList( new String[] {interactionIdBase+"X", interactionIdBase+"Y", interactionIdBase+"Z"} ));
		List<InteractionClass> expectedSubscribedInteractions = new ArrayList<>();
		for( String interactionClassName : tempInteractionDefs )
		{
			expectedSubscribedInteractions.add( new InteractionClass( interactionClassName, Sharing.SUBSCRIBE ) );
		}

		int expectedMaxReconnectAttempts = 123;
		int expectedReconnectRetryInterval = 54321;
		double expectedStepSize = 1.234;
		double expectedLookAhead = 0.1234;
		boolean expectedIsTimeStepped = true;
		boolean expectedCallbacksAreEvoked = true;
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName);
		config.addModules( expectedModules )
			  .addJoinModules( expectedJoinModules )
			  .cacheObjectClasses( expectedPublishedObjectClasses )
			  .cacheObjectClasses( expectedSubscribedObjectClasses )
			  .cacheInteractionClasses( expectedPublishedInteractions )
			  .cacheInteractionClasses( expectedSubscribedInteractions )
			  .setMaxJoinAttempts( expectedMaxReconnectAttempts )
			  .setJoinRetryInterval( expectedReconnectRetryInterval )
			  .setStepSize( expectedStepSize )
			  .setLookAhead( expectedLookAhead )
			  .setCallbacksAreEvoked( expectedCallbacksAreEvoked )
			  .setTimeStepped( expectedIsTimeStepped );
		
		assertEquals( federationName, config.getFederationName() );
		assertEquals( federateName, config.getFederateName() );
		assertEquals( federateType, config.getFederateType() );
		
		assertEquals( expectedMaxReconnectAttempts, config.getMaxJoinAttempts() );
		assertEquals( expectedReconnectRetryInterval, config.getJoinRetryInterval() );
		assertEquals( expectedCallbacksAreEvoked, config.callbacksAreEvoked() );
		assertEquals( expectedIsTimeStepped, config.isTimeStepped() );
		assertEquals( expectedLookAhead, config.getLookAhead() );
		assertEquals( expectedStepSize, config.getStepSize() );
		
		assertEquals( expectedModules.size(), config.getModules().size() );
		assertEquals( expectedJoinModules.size(), config.getJoinModules().size() );
		assertEquals( expectedPublishedObjectClasses.size(), config.getPublishedObjectClasses().size() );
		assertEquals( expectedSubscribedObjectClasses.size(), config.getSubscribedObjectClasses().size() );
		assertEquals( expectedPublishedInteractions.size(), config.getPublishedInteractions().size() );
		assertEquals( expectedSubscribedInteractions.size(), config.getSubscribedInteractions().size() );
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
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedMaxReconnectAttempts != config.getMaxJoinAttempts() );
		// try changing the value
		config.setMaxJoinAttempts( expectedMaxReconnectAttempts );
		assertEquals( expectedMaxReconnectAttempts, config.getMaxJoinAttempts());
	}
	
	/**
	 * This tests setting the reconnection wait time 
	 */
	public void testReconnectWaitTime()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		int expectedReconnectRetryInterval = 54321;
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedReconnectRetryInterval != config.getJoinRetryInterval() );
		// try changing the value
		config.setJoinRetryInterval( expectedReconnectRetryInterval );
		assertEquals( expectedReconnectRetryInterval, config.getJoinRetryInterval());
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
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedStepSize != config.getStepSize() );
		// try changing the value
		config.setStepSize( expectedStepSize );
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
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedLookAhead != config.getLookAhead() );
		// try changing the value
		config.setLookAhead( expectedLookAhead );
		assertEquals( expectedLookAhead, config.getLookAhead());
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
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedCallbacksAreEvoked != config.callbacksAreEvoked());
		// try changing the value
		config.setCallbacksAreEvoked( expectedCallbacksAreEvoked );
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
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		// sanity check that the default value is not our test value, otherwise this test is pointless
		assertTrue( expectedTimeStepped != config.isTimeStepped());
		// try changing the value
		config.setTimeStepped( expectedTimeStepped );
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
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		config.addModules( expectedModules );
		assertEquals( expectedModules.size(), config.getModules().size());
		// add the same modules again - shouldn't increase the size
		config.addModules( expectedModules );
		assertEquals( expectedModules.size(), config.getModules().size());
		// add more modules - should increase the size
		config.addModules( extraModules );
		assertEquals( expectedModules.size() + extraModules.size(), config.getModules().size());

		// start again with clean config
		config = new FederateConfiguration( federateName, federateType, federationName );
		// try to add single module - this actually ends up feeding through the adding
		// a list of modules method
		config.addModules( expectedModules.get( 0 ) );
		assertEquals( 1, config.getModules().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateName, federateType, federationName );
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
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		config.addJoinModules( expectedJoinModules );
		assertEquals( expectedJoinModules.size(), config.getJoinModules().size());
		// add the same modules again - shouldn't increase the size
		config.addJoinModules( expectedJoinModules );
		assertEquals( expectedJoinModules.size(), config.getJoinModules().size());
		// add more modules - should increase the size
		config.addJoinModules( extraJoinModules );
		assertEquals( expectedJoinModules.size() + extraJoinModules.size(), config.getJoinModules().size());

		// start again with clean config
		config = new FederateConfiguration( federateName, federateType, federationName );
		// try to add single module - this actually ends up feeding through the adding
		// a list of modules method
		config.addModules( expectedJoinModules.get( 0 ) );
		assertEquals( 1, config.getModules().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateName, federateType, federationName );
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
		
		// set up object classes and corresponding attributes to be published
		ObjectClass objectClass1 = new ObjectClass("HLAobjectRoot.A.B.C", Sharing.PUBLISH);
		objectClass1.addAttribute( new ObjectAttribute("A", DataType.BOOLEAN, Sharing.PUBLISH ));
		objectClass1.addAttribute( new ObjectAttribute("B", DataType.BYTE, Sharing.PUBLISH ));
		
		ObjectClass objectClass2 = new ObjectClass("HLAobjectRoot.X.Y.Z", Sharing.PUBLISH);
		objectClass2.addAttribute( new ObjectAttribute("M", DataType.CHAR, Sharing.PUBLISH ));
		objectClass2.addAttribute( new ObjectAttribute("N", DataType.DOUBLE, Sharing.PUBLISH ));
		objectClass2.addAttribute( new ObjectAttribute("O", DataType.FLOAT, Sharing.PUBLISH ));
		objectClass2.addAttribute( new ObjectAttribute("P", DataType.INT, Sharing.PUBLISH ));
		objectClass2.addAttribute( new ObjectAttribute("Q", DataType.LONG, Sharing.PUBLISH ));
		objectClass2.addAttribute( new ObjectAttribute("R", DataType.SHORT, Sharing.PUBLISH ));
		objectClass2.addAttribute( new ObjectAttribute("S", DataType.STRING, Sharing.PUBLISH ));
		objectClass2.addAttribute( new ObjectAttribute("T", DataType.UNKNOWN, Sharing.PUBLISH ));
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		config.cacheObjectClasses( objectClass1 );
		assertEquals( 1, config.getPublishedObjectClasses().size());
		// add the same attributes again - shouldn't increase the size
		config.cacheObjectClasses( objectClass1 );
		assertEquals( 1, config.getPublishedObjectClasses().size());
		// add more attributes - should increase the size
		config.cacheObjectClasses( objectClass2 );
		assertEquals( 2, config.getPublishedObjectClasses().size());

		// start again with clean config
		config = new FederateConfiguration( federateName, federateType, federationName );
		// try to add multiple object class reflections at once
		config.cacheObjectClasses( objectClass1, objectClass2 );
		assertEquals( 2, config.getPublishedObjectClasses().size());
		
		// check on reported attributes
		Set<String> actualClass1AttributeNames = config.getPublishedAttributeNames( objectClass1.name );
		Set<String> actualClass2AttributeNames = config.getPublishedAttributeNames( objectClass2.name );
		assertEquals( objectClass1.getAttributes().size(), actualClass1AttributeNames.size() );
		assertEquals( objectClass2.getAttributes().size(), actualClass2AttributeNames.size() );
		
		// check reported attribute types
		for( String attributeName : actualClass1AttributeNames )
		{
			DataType expectedDataType = objectClass1.getAttributes().get( attributeName ).dataType;
			DataType actualDataType = config.getDataType( objectClass1.name, attributeName );
			assertEquals(expectedDataType, actualDataType);
		}
		for( String attributeName : actualClass2AttributeNames )
		{
			DataType expectedDataType = objectClass2.getAttributes().get( attributeName ).dataType;
			DataType actualDataType = config.getDataType( objectClass2.name, attributeName );
			assertEquals(expectedDataType, actualDataType);
		}
	}
	
	/**
	 * This tests adding of subscribed attributes 
	 */
	public void testAddSubscribedAttributes()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		// set up object classes and corresponding attributes to be subscribed to
		ObjectClass objectClass1 = new ObjectClass("HLAobjectRoot.A.B.C", Sharing.SUBSCRIBE);
		objectClass1.addAttribute( new ObjectAttribute("A", DataType.BOOLEAN, Sharing.SUBSCRIBE ));
		objectClass1.addAttribute( new ObjectAttribute("B", DataType.BYTE, Sharing.SUBSCRIBE ));
		
		ObjectClass objectClass2 = new ObjectClass("HLAobjectRoot.X.Y.Z", Sharing.SUBSCRIBE);
		objectClass2.addAttribute( new ObjectAttribute("M", DataType.CHAR, Sharing.SUBSCRIBE ));
		objectClass2.addAttribute( new ObjectAttribute("N", DataType.DOUBLE, Sharing.SUBSCRIBE ));
		objectClass2.addAttribute( new ObjectAttribute("O", DataType.FLOAT, Sharing.SUBSCRIBE ));
		objectClass2.addAttribute( new ObjectAttribute("P", DataType.INT, Sharing.SUBSCRIBE ));
		objectClass2.addAttribute( new ObjectAttribute("Q", DataType.LONG, Sharing.SUBSCRIBE ));
		objectClass2.addAttribute( new ObjectAttribute("R", DataType.SHORT, Sharing.SUBSCRIBE ));
		objectClass2.addAttribute( new ObjectAttribute("S", DataType.STRING, Sharing.SUBSCRIBE ));
		objectClass2.addAttribute( new ObjectAttribute("T", DataType.UNKNOWN, Sharing.SUBSCRIBE ));
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		config.cacheObjectClasses( objectClass1 );
		assertEquals( 1, config.getSubscribedObjectClasses().size());
		// add the same attributes again - shouldn't increase the size
		config.cacheObjectClasses( objectClass1 );
		assertEquals( 1, config.getSubscribedObjectClasses().size());
		// add more attributes - should increase the size
		config.cacheObjectClasses( objectClass2 );
		assertEquals( 2, config.getSubscribedObjectClasses().size());

		// start again with clean config
		config = new FederateConfiguration( federateName, federateType, federationName );
		// try to add multiple object class reflections at once
		config.cacheObjectClasses( objectClass1, objectClass2 );
		assertEquals( 2, config.getSubscribedObjectClasses().size());
		
		// check on reported attributes
		Set<String> actualClass1AttributeNames = config.getSubscribedAttributeNames( objectClass1.name );
		Set<String> actualClass2AttributeNames = config.getSubscribedAttributeNames( objectClass2.name );
		assertEquals( objectClass1.getAttributes().size(), actualClass1AttributeNames.size() );
		assertEquals( objectClass2.getAttributes().size(), actualClass2AttributeNames.size() );
		
		// check reported attribute types
		for( String attributeName : actualClass1AttributeNames )
		{
			DataType expectedDataType = objectClass1.getAttributes().get( attributeName ).dataType;
			DataType actualDataType = config.getDataType( objectClass1.name, attributeName );
			assertEquals(expectedDataType, actualDataType);
		}
		for( String attributeName : actualClass2AttributeNames )
		{
			DataType expectedDataType = objectClass2.getAttributes().get( attributeName ).dataType;
			DataType actualDataType = config.getDataType( objectClass2.name, attributeName );
			assertEquals(expectedDataType, actualDataType);
		}
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
		InteractionClass interaction1 = new InteractionClass( "HLAinteractionRoot.A.B.C.", Sharing.PUBLISH );
		interaction1.addParameter( new InteractionParameter("A", DataType.BOOLEAN ));
		interaction1.addParameter( new InteractionParameter("B", DataType.BYTE ));
		
		InteractionClass interaction2 = new InteractionClass("HLAobjectRoot.X.Y.Z", Sharing.PUBLISH);
		interaction2.addParameter( new InteractionParameter("M", DataType.CHAR ));
		interaction2.addParameter( new InteractionParameter("N", DataType.DOUBLE ));
		interaction2.addParameter( new InteractionParameter("O", DataType.FLOAT ));
		interaction2.addParameter( new InteractionParameter("P", DataType.INT ));
		interaction2.addParameter( new InteractionParameter("Q", DataType.LONG ));
		interaction2.addParameter( new InteractionParameter("R", DataType.SHORT ));
		interaction2.addParameter( new InteractionParameter("S", DataType.STRING ));
		interaction2.addParameter( new InteractionParameter("T", DataType.UNKNOWN ));
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		config.cacheInteractionClasses( interaction1 );
		assertEquals( 1, config.getPublishedInteractions().size());
		// add the same interactions again - shouldn't increase the size
		config.cacheInteractionClasses( interaction1 );
		assertEquals( 1, config.getPublishedInteractions().size());
		// add more interactions - should increase the size
		config.cacheInteractionClasses( interaction2 );
		assertEquals( 2, config.getPublishedInteractions().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateName, federateType, federationName );
		// try to add multiple interactions at once
		config.cacheInteractionClasses( interaction1, interaction2 );
		assertEquals( 2, config.getPublishedInteractions().size());

		// check on reported parameters
		Set<String> actualInteraction1ParameterNames = config.getParameterNames( interaction1.name );
		Set<String> actualInteraction2ParameterNames = config.getParameterNames( interaction2.name );
		assertEquals( interaction1.getParameters().size(), actualInteraction1ParameterNames.size() );
		assertEquals( interaction2.getParameters().size(), actualInteraction2ParameterNames.size() );
		
		// check reported attribute types
		for( String parameterName : actualInteraction1ParameterNames )
		{
			DataType expectedDataType = interaction1.getParameters().get( parameterName ).dataType;
			DataType actualDataType = config.getDataType( interaction1.name, parameterName );
			assertEquals(expectedDataType, actualDataType);
		}
		for( String parameterName : actualInteraction2ParameterNames )
		{
			DataType expectedDataType = interaction2.getParameters().get( parameterName ).dataType;
			DataType actualDataType = config.getDataType( interaction2.name, parameterName );
			assertEquals(expectedDataType, actualDataType);
		}
	}
	
	/**
	 * This tests adding of published interactions 
	 */
	public void testAddSubscribedInteractions()
	{
		String federationName = "federationName";
		String federateName = "federateName";
		String federateType = "federateType";
		
		// set up lists of interactions to be subscribed
		InteractionClass interaction1 = new InteractionClass( "HLAinteractionRoot.A.B.C.", Sharing.SUBSCRIBE );
		interaction1.addParameter( new InteractionParameter("A", DataType.BOOLEAN ));
		interaction1.addParameter( new InteractionParameter("B", DataType.BYTE ));
		
		InteractionClass interaction2 = new InteractionClass("HLAobjectRoot.X.Y.Z", Sharing.SUBSCRIBE);
		interaction2.addParameter( new InteractionParameter("M", DataType.CHAR ));
		interaction2.addParameter( new InteractionParameter("N", DataType.DOUBLE ));
		interaction2.addParameter( new InteractionParameter("O", DataType.FLOAT ));
		interaction2.addParameter( new InteractionParameter("P", DataType.INT ));
		interaction2.addParameter( new InteractionParameter("Q", DataType.LONG ));
		interaction2.addParameter( new InteractionParameter("R", DataType.SHORT ));
		interaction2.addParameter( new InteractionParameter("S", DataType.STRING ));
		interaction2.addParameter( new InteractionParameter("T", DataType.UNKNOWN ));
		
		FederateConfiguration config = new FederateConfiguration( federateName, federateType, federationName );
		config.cacheInteractionClasses( interaction1 );
		assertEquals( 1, config.getSubscribedInteractions().size());
		// add the same interactions again - shouldn't increase the size
		config.cacheInteractionClasses( interaction1 );
		assertEquals( 1, config.getSubscribedInteractions().size());
		// add more interactions - should increase the size
		config.cacheInteractionClasses( interaction2 );
		assertEquals( 2, config.getSubscribedInteractions().size());
		
		// start again with clean config
		config = new FederateConfiguration( federateName, federateType, federationName );
		// try to add multiple interactions at once
		config.cacheInteractionClasses( interaction1, interaction2 );
		assertEquals( 2, config.getSubscribedInteractions().size());
		
		// check on reported parameters
		Set<String> actualInteraction1ParameterNames = config.getParameterNames( interaction1.name );
		Set<String> actualInteraction2ParameterNames = config.getParameterNames( interaction2.name );
		assertEquals( interaction1.getParameters().size(), actualInteraction1ParameterNames.size() );
		assertEquals( interaction2.getParameters().size(), actualInteraction2ParameterNames.size() );
		
		// check reported attribute types
		for( String parameterName : actualInteraction1ParameterNames )
		{
			DataType expectedDataType = interaction1.getParameters().get( parameterName ).dataType;
			DataType actualDataType = config.getDataType( interaction1.name, parameterName );
			assertEquals(expectedDataType, actualDataType);
		}
		for( String parameterName : actualInteraction2ParameterNames )
		{
			DataType expectedDataType = interaction2.getParameters().get( parameterName ).dataType;
			DataType actualDataType = config.getDataType( interaction2.name, parameterName );
			assertEquals(expectedDataType, actualDataType);
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
		return new TestSuite( FederateConfigurationTest.class );
	}
}

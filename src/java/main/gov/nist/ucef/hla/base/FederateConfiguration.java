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
package gov.nist.ucef.hla.base;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The purpose of this class is to encapsulate all data required to configure a federate. The main
 * usage pattern is something like:
 * 
 * 		FederateConfiguration config = new FederateConfiguration( "TheUnitedFederationOfPlanets", 
 * 		                                                          "FederateName", 
 * 		                                                          "TestFederate" );
 * 		config.addPublishedAttributes( publishedAttributes )
 * 			  .addSubscribedAttributes( subscribedAttributes )
 * 			  .addPublishedInteractions( publishedInteractions )
 * 			  .addSubscribedInteractions( publishedInteractions )
 * 			  .setLookAhead(0.5)
 * 			  .freeze();
 * 
 * Once "frozen", the configuration cannot be modified further - attempts to do so will result in
 * errors being logged, but there is no other "adverse" impact (apart from the attempted 
 * modification to the configuration not being applied).
 * 
 * The configuration should be frozen before being used to configure a federate, and most federates
 * will freeze the configuration when it is passed in to prevent modification.
 */
public class FederateConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final boolean DEFAULT_SHOULD_CREATE_FEDERATION = false;
	private static final int DEFAULT_MAX_RECONNECT_ATTEMPTS = 5;
	private static final long DEFAULT_RECONNECT_RETRY_INTERVAL_SEC = 5;
	private static final boolean DEFAULT_IS_LATE_JOINER = false;
	private static final boolean DEFAULT_IS_TIME_STEPPED = true;
	private static final boolean DEFAULT_ARE_CALLBACKS_EVOKED = false;
	private static final double DEFAULT_LOOK_AHEAD = 1.0;
	private static final double DEFAULT_STEP_SIZE = 0.1;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationExecName;
	private String federateName;
	private String federateType;
	private Set<URL> modules;
	private Set<URL> joinModules;
	private Map<String,Types.InteractionClass> pubSubInteractions;
	private Map<String,Types.ObjectClass> pubSubAttributes;

	private boolean canCreateFederation;
	private int maxReconnectAttempts;
	private long reconnectRetryIntervalSec;
	private boolean isLateJoiner;
	private boolean isTimeStepped;
	private boolean callbacksAreEvoked;
	private double lookAhead;
	private double stepSize;

	// flag indicating whether the configuration in this instance is modifiable or not
	// IMPORTANT: Do not expose this to modification from outside of this class!
	private boolean isFrozen = false;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor - the federation name, federate name and federation types are supplied, and all
	 * other properties are left as defaults and or empty.
	 * @param federateName
	 * @param federateType
	 * @param federationName
	 */
	public FederateConfiguration( String federateName, String federateType, String federationName )
	{
		this.federateName = federateName;
		this.federateType = federateType;
		this.federationExecName = federationName;

		this.modules = new HashSet<>();
		this.joinModules = new HashSet<>();
		this.pubSubAttributes = new HashMap<>();
		this.pubSubInteractions = new HashMap<>();
		
		this.canCreateFederation = DEFAULT_SHOULD_CREATE_FEDERATION;
		this.maxReconnectAttempts = DEFAULT_MAX_RECONNECT_ATTEMPTS;
		this.reconnectRetryIntervalSec = DEFAULT_RECONNECT_RETRY_INTERVAL_SEC;
		this.isLateJoiner = DEFAULT_IS_LATE_JOINER;
		this.isTimeStepped = DEFAULT_IS_TIME_STEPPED;
		this.callbacksAreEvoked = DEFAULT_ARE_CALLBACKS_EVOKED;
		this.lookAhead = DEFAULT_LOOK_AHEAD;
		this.stepSize = DEFAULT_STEP_SIZE;
		
		this.isFrozen = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String summary()
	{
		String dashRule = "------------------------------------------------------------\n";
		String dotRule = "............................................................\n";
		
		StringBuilder builder = new StringBuilder();
		
		builder.append( dashRule );
		builder.append( "Federation Name            : " + this.federationExecName + "\n" );
		builder.append( "Federate Name              : " + this.federateName + "\n" );
		builder.append( "Federate Type              : " + this.federateType + "\n" );
		
		builder.append( dotRule );
		builder.append( "Create Federation?         : " + (this.canCreateFederation?"Yes":"No") + "\n" );
		builder.append( "Maximum Recconect Attempts : " + this.maxReconnectAttempts + "\n" );
		builder.append( "Reconnect Wait Time        : " + this.reconnectRetryIntervalSec + " seconds\n" );
		builder.append( "Late Joiner?               : " + (this.isLateJoiner?"Yes":"No") + "\n" );
		builder.append( "Time Stepped?              : " + (this.isTimeStepped?"Yes":"No") + "\n" );
		builder.append( "Are Callbacks Evoked?      : " + (this.callbacksAreEvoked?"Yes":"No") + "\n" );
		builder.append( "Look Ahead                 : " + this.lookAhead + "\n" );
		builder.append( "Step Size                  : " + this.stepSize + "\n" );
		
		builder.append( dotRule );
		builder.append( "Published Attributes:\n" );
		Collection<Types.ObjectClass> attributes = getPublishedAttributes();
		if(attributes.isEmpty())
		{
			builder.append( "\t...none...\n" );
		}
		else
		{
			List<Types.ObjectClass> pubAttrList = attributes.stream().collect( Collectors.toList() );
			pubAttrList.sort( ( a, b ) -> a.name.compareTo( b.name ) );
			for( Types.ObjectClass objectClass : pubAttrList )
			{
				builder.append( "\t" + objectClass.name + "\n" );
				List<String> attributeNames = objectClass.attributes.values()
    				.stream()
    				.map( x -> x.name )
    				.collect( Collectors.toList() );
				attributeNames.sort( null );
				attributeNames.forEach( ( x ) -> builder.append( "\t\t" + x + "\n" ) );
			}
		}
		
		builder.append( dotRule );
		builder.append( "Subscribed Attributes:\n" );
		attributes = getSubscribedAttributes();
		if(attributes.isEmpty())
		{
			builder.append( "\t...none...\n" );
		}
		else
		{
			List<Types.ObjectClass> subAttrList = attributes.stream().collect( Collectors.toList() );
			subAttrList.sort( ( a, b ) -> a.name.compareTo( b.name ) );
			for( Types.ObjectClass objectClass : subAttrList )
			{
				builder.append( "\t" + objectClass.name + "\n" );
				List<String> attributeNames = objectClass.attributes.values()
    				.stream()
    				.map( x -> x.name )
    				.collect( Collectors.toList() );
				attributeNames.sort( null );
				attributeNames.forEach( ( x ) -> builder.append( "\t\t" + x + "\n" ) );
			}
		}
		
		builder.append( dotRule );
		builder.append( "Published Interactions:\n" );
		Collection<Types.InteractionClass> interactions = getPublishedInteractions();
		if(interactions.isEmpty())
		{
			builder.append( "\t...none...\n" );
		}
		else
		{
			List<Types.InteractionClass> pubInteractionList = interactions.stream().collect( Collectors.toList() );
			pubInteractionList.sort( ( a, b ) -> a.name.compareTo( b.name ) );
			pubInteractionList.forEach( ( x ) -> builder.append( "\t" + x.name + "\n" ) );
		}
		
		builder.append( dotRule );
		builder.append( "Subscribed Interactions:\n" );
		interactions = getSubscribedInteractions();
		if(interactions.isEmpty())
		{
			builder.append( "\t...none...\n" );
		}
		else
		{
			List<Types.InteractionClass> subInteractionList = interactions.stream().collect( Collectors.toList() );
			subInteractionList.sort( ( a, b ) -> a.name.compareTo( b.name ) );
			subInteractionList.forEach( ( x ) -> builder.append( "\t" + x.name + "\n" ) );
		}
		
		builder.append( dashRule );
		
		return builder.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Determine if this instance is currently modifiable.
	 * 
	 * @return true if this instance is currently modifiable, false otherwise.
	 */
	public boolean isFrozen()
	{
		return this.isFrozen;
	}

	/**
	 * Make this this instance is unmodifiable.
	 * 
	 * Once an instance is frozen, it cannot be "thawed" (i.e., made modifiable again).
	 * 
	 * Calling this multiple times has no "additional" effect - the instance will simply remain in
	 * its "locked" state.
	 * 
	 * NOTE: This will cause the configuration to become locked. No further modifications may be
	 * made after this method is called, and this instance will become read only. See also
	 * {@link #isFrozen()}
	 * 
	 * @return this instance
	 */
	public FederateConfiguration freeze()
	{
		this.isFrozen = true;
		return this;
	}
	
	/**
	 * Obtain the configured federation name
	 * 
	 * @return the configured federation name (not modifiable)
	 */
	public String getFederationName()
	{
		return federationExecName;
	}

	/**
	 * Obtain the configured federate name
	 * 
	 * @return the configured federate name (not modifiable)
	 */
	public String getFederateName()
	{
		return federateName;
	}

	/**
	 * Obtain the configured federate type
	 * 
	 * @return the configured federate type (not modifiable)
	 */
	public String getFederateType()
	{
		return federateType;
	}

	/**
	 * Determine whether the federate should be able to create a required federation if it is
	 * absent
	 * 
	 * @return true if the federate should be able to create federations, false otherwise
	 */
	public boolean canCreateFederation()
	{
		return canCreateFederation;
	}
	
	/**
	 * Obtain the maximum number of reconnection attempts
	 * 
	 * @return the maximum number of reconnection attempts
	 */
	public int getMaxReconnectAttempts()
	{
		return maxReconnectAttempts;
	}

	/**
	 * Obtain the reconnection retry interval in seconds
	 * 
	 * @return the reconnection retry interval in seconds
	 */
	public long getReconnectRetryInterval()
	{
		return reconnectRetryIntervalSec;
	}

	/**
	 * Obtain the lookahead
	 * 
	 * @return the lookahead
	 */
	public double getLookAhead()
	{
		return lookAhead;
	}

	/**
	 * Obtain the step size
	 * 
	 * @return the step size
	 */
	public double getStepSize()
	{
		return stepSize;
	}

	/**
	 * Determine if the federate is configured to be a late joiner
	 * 
	 * @return true if the federate is configured to be a late joiner, false otherwise
	 */
	public boolean isLateJoiner()
	{
		return isLateJoiner;
	}

	/**
	 * Determine if the federate is configured to be time stepped
	 * 
	 * @return true if the federate is configured to be time stepped, false otherwise
	 */
	public boolean isTimeStepped()
	{
		return isTimeStepped;
	}
	
	/**
	 * Determine if the federate is configured to use evoked callbacks
	 * 
	 * @return true if the federate is configured to use evoked callbacks
	 */
	public boolean callbacksAreEvoked()
	{
		return callbacksAreEvoked;
	}
	
	/**
	 * Obtain the configured FOM modules
	 * 
	 * @return the configured FOM modules (not modifiable)
	 */
	public Collection<URL> getModules()
	{
		return Collections.unmodifiableSet( modules );
	}

	/**
	 * Obtain the configured join FOM modules
	 * 
	 * @return the configured join FOM modules (not modifiable)
	 */
	public Collection<URL> getJoinModules()
	{
		return Collections.unmodifiableSet( joinModules );
	}

	/**
	 * Obtain the published *and* subscribed interactions
	 * 
	 * @return the published *and* subscribed interactions (not modifiable)
	 */
	public Collection<Types.InteractionClass> getPublishedAndSubscribedInteractions()
	{
		return Collections.unmodifiableCollection( pubSubInteractions.values() );
	}
	
	/**
	 * Obtain the published interactions
	 * 
	 * @return the published interactions (not modifiable)
	 */
	public Collection<Types.InteractionClass> getPublishedInteractions()
	{
		return pubSubInteractions.values()
			.stream()
			.filter( x -> x.publish )
			.collect( Collectors.toList() );
	}

	/**
	 * Obtain the subscribed interactions
	 * 
	 * @return the subscribed interactions (not modifiable)
	 */
	public Collection<Types.InteractionClass> getSubscribedInteractions()
	{
		return pubSubInteractions.values()
			.stream()
			.filter( x -> x.subscribe )
			.collect( Collectors.toList() );
	}

	/**
	 * Obtain the published *and* subscribed attributes
	 * 
	 * @return the published *and* subscribed attributes (not modifiable)
	 */
	public Collection<Types.ObjectClass> getPublishedAndSubscribedAttributes()
	{
		return Collections.unmodifiableCollection( pubSubAttributes.values() );
	}
	
	/**
	 * Obtain the published attributes
	 * 
	 * @return the published attributes (not modifiable)
	 */
	public Collection<Types.ObjectClass> getPublishedAttributes()
	{
		return pubSubAttributes.values()
			.stream()
			.filter( x -> x.publish )
			.collect( Collectors.toList() );
	}

	/**
	 * Obtain the subscribed attributes
	 * 
	 * @return the subscribed attributes (not modifiable)
	 */
	public Collection<Types.ObjectClass> getSubscribedAttributes()
	{
		return pubSubAttributes.values()
			.stream()
			.filter( x -> x.subscribe )
			.collect( Collectors.toList() );
	}
	
	/**
	 * Set the federation execution name
	 * 
	 * @param federationExecName the federation execution name
	 */
	public FederateConfiguration setFederationName( String federationExecName )
	{
		if( canWrite() )
			this.federationExecName = federationExecName;
		return this;
	}
	
	/**
	 * Set the federate name
	 * 
	 * @param federateName the federate name
	 */
	public FederateConfiguration setFederateName( String federateName )
	{
		if( canWrite() )
			this.federateName = federateName;
		return this;
	}
	
	/**
	 * Set the federate type
	 * 
	 * @param federateType the federate type
	 */
	public FederateConfiguration setFederateType( String federateType )
	{
		if( canWrite() )
			this.federateType = federateType;
		return this;
	}
	
	/**
	 * Configure whether the federate is able to create a federation if the federation is absent
	 * on startup
	 * 
	 * @param canCreateFederation if true, the federate can attempt to create the required
	 *            federation on startup, otherwise it is not allowed to create any new federations
	 * @return this instance
	 */
	public FederateConfiguration setCanCreateFederation( boolean canCreateFederation )
	{
		if(canWrite())
			this.canCreateFederation = canCreateFederation;
		return this;
	}
	
	/**
	 * Configure the federate's maximum number of reconnection attempts
	 * 
	 * @param maxReconnectAttempts the maximum number of reconnection attempts
	 * @return this instance
	 */
	public FederateConfiguration setMaxReconnectAttempts( int maxReconnectAttempts )
	{
		if(canWrite())
			this.maxReconnectAttempts = maxReconnectAttempts;
		return this;
	}

	/**
	 * Configure the federate's reconnect retry interval in seconds
	 * 
	 * @param the federate's reconnect retry interval in seconds
	 * @return this instance
	 */
	public FederateConfiguration setReconnectRetryInterval( long retryIntervalSec )
	{
		if(canWrite())
			this.reconnectRetryIntervalSec = retryIntervalSec;
		return this;
	}

	/**
	 * Configure the federate's lookahead
	 * 
	 * @param lookahead the lookahead
	 * @return this instance
	 */
	public FederateConfiguration setLookAhead( double lookAhead )
	{
		if(canWrite())
			this.lookAhead = lookAhead;
		return this;
	}

	/**
	 * Configure the federate's step size
	 * 
	 * @param stepSize the step size
	 * @return this instance
	 */
	public FederateConfiguration setStepSize( double stepSize )
	{
		if(canWrite())
			this.stepSize = stepSize;
		return this;
	}

	/**
	 * Configure whether the federate is configured to be a late joiner
	 * 
	 * @param isLateJoiner true if the federate to be a late joiner, false otherwise
	 * @return this instance
	 */
	public FederateConfiguration setLateJoiner( boolean isLateJoiner )
	{
		if(canWrite())
			this.isLateJoiner = isLateJoiner;
		return this;
	}

	/**
	 * Configure whether the federate is configured to be time stepped
	 * 
	 * @param isLateJoiner true if the federate to be time stepped, false otherwise
	 * @return this instance
	 */
	public FederateConfiguration setTimeStepped( boolean isTimeStepped )
	{
		if(canWrite())
			this.isTimeStepped = isTimeStepped;
		return this;
	}
	
	/**
	 * Configure whether the federate is configured to use evoked callbacks
	 * 
	 * @param isLateJoiner true if the federate is configured to use evoked callbacks, false otherwise
	 * @return this instance
	 */
	public FederateConfiguration setCallbacksAreEvoked( boolean callbacksAreEvoked )
	{
		if(canWrite())
			this.callbacksAreEvoked = callbacksAreEvoked;
		return this;
	}
	
	/**
	 * Add a FOM module to the configuration
	 * 
	 * @param module the FOM module to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addModule( URL module )
	{
		return addModules( new URL[]{ module } );
	}

	/**
	 * Add FOM modules to the configuration
	 * 
	 * @param modules the FOM modules to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addModules( URL[] modules )
	{
		return addModules( asCollection( modules ) );
	}

	/**
	 * Add FOM modules to the configuration
	 * 
	 * @param modules the FOM modules to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addModules( Collection<URL> modules )
	{
		if( canWrite( modules ) )
		{
			this.modules.addAll( collectNonEmptyURLs( modules ) );
		}
		return this;
	}

	/**
	 * Add a join FOM module to the configuration
	 * 
	 * @param joinModule the join FOM module to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addJoinModule( URL joinModule )
	{
		return addJoinModules( new URL[]{ joinModule } );
	}

	/**
	 * Add join FOM modules to the configuration
	 * 
	 * @param joinModules the join FOM modules to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addJoinModules( URL[] joinModules )
	{
		return addJoinModules( asCollection( joinModules ) );
	}

	/**
	 * Add join FOM modules to the configuration
	 * 
	 * @param joinModules the join FOM modules to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addJoinModules( Collection<URL> joinModules )
	{
		if( canWrite( joinModules ) )
		{
			this.joinModules.addAll( collectNonEmptyURLs( joinModules ) );
		}
		return this;
	}

	/**
	 * Add a published attribute to the configuration
	 * 
	 * @param objectClassName the identifier of the class to which the attribute belongs
	 * @param attributeName the identifier of the attribute on the class to publish
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAttribute( String objectClassName,
	                                                    String attributeName )
	{
		return addPublishedAttributes( objectClassName, new String[]{ attributeName } );
	}

	/**
	 * Add published attributes to the configuration
	 * 
	 * @param objectClassName the identifier of the class to which the attributes belong
	 * @param attributeNames the identifiers of the attributes on the class to publish
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAttributes( String objectClassName,
	                                                     String[] attributeNames )
	{
		return addPublishedAttributes( objectClassName, asCollection( attributeNames ) );
	}

	/**
	 * Add published attributes to the configuration
	 * 
	 * @param objectClassName the identifier of the class to which the attributes belong
	 * @param attributeNames the identifiers of the attributes on the class to publish
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAttributes( String objectClassName,
	                                                     Collection<String> attributeNames )
	{
		if( canWrite( objectClassName ) && canWrite( attributeNames ) )
		{
			Types.ObjectClass objectClass = this.pubSubAttributes.computeIfAbsent( objectClassName, 
			                                                                       x -> new Types.ObjectClass(objectClassName) );
			objectClass.publish = true;
			for(String attributeName : attributeNames)
			{
				Types.ObjectAttribute objAttr = objectClass.attributes.computeIfAbsent( attributeName,
				                                                                              x -> new Types.ObjectAttribute( attributeName ) );
				objAttr.publish = true;
			}
		}
		return this;
	}

	/**
	 * Add published attributes to the configuration
	 * 
	 * @param publishedAttributes the published attributes to add to the configuration as a map
	 *            linking the identifiers of the classes with the identifiers of the attributes on
	 *            the class which are to be published
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAttributes( Map<String,Collection<String>> publishedAttributes )
	{
		if( canWrite( publishedAttributes ) )
		{
			for( Entry<String,Collection<String>> foo : publishedAttributes.entrySet() )
			{
				addPublishedAttributes( foo.getKey(), foo.getValue() );
			}
		}
		return this;
	}

	/**
	 * Add a subscribed attribute to the configuration
	 * 
	 * @param objectClassName the identifier of the class to which the attribute belongs
	 * @param attributeIdentifier the identifier of the attribute on the class to subscribe to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAttribute( String objectClassName,
	                                                     String attributeIdentifier )
	{
		return addSubscribedAttributes( objectClassName, new String[]{ attributeIdentifier } );
	}

	/**
	 * Add subscribed attributes to the configuration
	 * 
	 * @param objectClassName the identifier of the class to which the attributes belong
	 * @param attributeNames the identifiers of the attributes on the class to subscribed to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAttributes( String objectClassName,
	                                                     String[] attributeNames )
	{
		return addSubscribedAttributes( objectClassName, asCollection( attributeNames ) );
	}

	/**
	 * Add subscribed attributes to the configuration
	 * 
	 * @param objectClassName the identifier of the class to which the attributes belong
	 * @param attributeNames the identifiers of the attributes on the class to subscribe to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAttributes( String objectClassName,
	                                                      Collection<String> attributeNames )
	{
		if( canWrite( objectClassName ) && canWrite( attributeNames ) )
		{
			Types.ObjectClass objectClass = this.pubSubAttributes.computeIfAbsent( objectClassName, 
			                                                                       x -> new Types.ObjectClass(objectClassName) );
			objectClass.subscribe = true;
			for(String attributeName : attributeNames)
			{
				Types.ObjectAttribute objAttr = objectClass.attributes.computeIfAbsent( attributeName,
				                                                                              x -> new Types.ObjectAttribute( attributeName ) );
				objAttr.subscribe = true;
			}
		}
		return this;
		
	}

	/**
	 * Add subscribed attributes to the configuration
	 * 
	 * @param subscribedAttributes the subscribed attributes to add to the configuration as a map
	 *            linking the identifiers of the classes with the identifiers of the attributes on
	 *            the class which are to be subscribed to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAttributes( Map<String,Collection<String>> subscribedAttributes )
	{
		if( canWrite( subscribedAttributes ) )
		{
			for( Entry<String,Collection<String>> foo : subscribedAttributes.entrySet() )
			{
				addSubscribedAttributes( foo.getKey(), foo.getValue() );
			}
		}
		return this;
	}

	/**
	 * Add a published interaction to the configuration
	 * 
	 * @param interactionIdentifier the published interaction to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addPublishedInteraction( String interactionIdentifier )
	{
		return addPublishedInteractions( new String[]{ interactionIdentifier } );
	}

	/**
	 * Add published interactions to the configuration
	 * 
	 * @param interactionIdentifiers the published interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addPublishedInteractions( String ... interactionIdentifiers )
	{
		return addPublishedInteractions( asCollection( interactionIdentifiers ) );
	}

	/**
	 * Add published interactions to the configuration
	 * 
	 * @param interactionIdentifiers the published interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addPublishedInteractions( Collection<String> interactionIdentifiers )
	{
		if( canWrite( interactionIdentifiers ) )
		{
			for(String interactionClassName : interactionIdentifiers )
			{
				Types.InteractionClass interactionClass = this.pubSubInteractions.computeIfAbsent( interactionClassName, 
				                                                                                   x -> new Types.InteractionClass(interactionClassName) );
				interactionClass.publish = true;
			}
		}
		return this;
		
	}

	/**
	 * Add a subscribed interaction to the configuration
	 * 
	 * @param interactionIdentifier the subscribed interaction to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedInteraction( String interactionIdentifier )
	{
		return addSubscribedInteractions( new String[]{ interactionIdentifier } );
	}

	/**
	 * Add subscribed interactions to the configuration
	 * 
	 * @param interactionIdentifiers the subscribed interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedInteractions( String ... interactionIdentifiers )
	{
		return addSubscribedInteractions( asCollection( interactionIdentifiers ) );
	}

	/**
	 * Add subscribed interactions to the configuration
	 * 
	 * @param interactionIdentifiers the subscribed interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedInteractions( Collection<String> interactionIdentifiers )
	{
		if( canWrite( interactionIdentifiers ) )
		{
			for(String interactionClassName : interactionIdentifiers )
			{
				Types.InteractionClass interactionClass = this.pubSubInteractions.computeIfAbsent( interactionClassName, 
				                                                                                   x -> new Types.InteractionClass(interactionClassName) );
				interactionClass.subscribe = true;
			}
		}
		return this;	
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Utility Methods /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to turn an array of {@link String}s into a {@link Collection} of
	 * {@link String}s
	 * 
	 * @param values the array of {@link String}s
	 * @return a {@link Collection} of {@link String}s
	 */
	private Collection<String> asCollection( String[] values )
	{
		return values == null ? Collections.emptyList() : Arrays.asList( values );
	}

	/**
	 * Utility method to turn an array of {@link URL}s into a {@link Collection} of {@link URL}s
	 * 
	 * @param values the array of {@link URL}s
	 * @return a {@link Collection} of {@link URL}s
	 */
	private Collection<URL> asCollection( URL[] values )
	{
		return values == null ? Collections.emptyList() : Arrays.asList( values );
	}

	/**
	 * Utility method to collect all the non null/non empty {@link String}s in a collection and
	 * return them as a new {@link Collection}
	 * 
	 * @param values the values
	 * @return the {@link Collection} of non-null/non-empty values
	 */
	public Collection<String> collectNonEmptyStrings( Collection<String> values )
	{
		return values.stream().filter( ( str ) -> notNullOrEmpty( str ) ).collect( Collectors.toList() );
	}

	/**
	 * Utility method to collect all the non null/non empty {@link URL}s in a collection and
	 * return them as a new {@link Collection}
	 * 
	 * @param values the values
	 * @return the {@link Collection} of non-null/non-empty values
	 */
	public Collection<URL> collectNonEmptyURLs( Collection<URL> values )
	{
		return values.stream().filter( ( url ) -> notNullOrEmpty( url ) ).collect( Collectors.toList() );
	}

	/**
	 * Utility method to check if this instance is currently modifiable before carrying out
	 * modifications to the configured properties.
	 * 
	 * If it is no modifiable, an WARNING level error log will be produced
	 * 
	 * @return true if this instance is modifiable, false otherwise.
	 */
	private boolean canWrite()
	{
		if( isFrozen() )
		{
			// can't modify values
			return false;
		}
		return true;
	}

	/**
	 * Utility method to check if this instance is currently modifiable and that the parameter is
	 * not null or empty before carrying out modifications to the configured properties.
	 * 
	 * If it is no modifiable, an ERROR level error log will be produced
	 * 
	 * @return true if this instance is currently modifiable and that the parameter is not null or
	 *         empty, false otherwise
	 */
	private boolean canWrite( String value )
	{
		return canWrite() && notNullOrEmpty( value );
	}

	/**
	 * Utility method to check if this instance is currently modifiable and that the parameter is
	 * not null or empty before carrying out modifications to the configured properties.
	 * 
	 * If it is no modifiable, an ERROR level error log will be produced
	 * 
	 * @return true if this instance is currently modifiable and that the parameter is not null or
	 *         empty, false otherwise
	 */
	private boolean canWrite( Collection<?> value )
	{
		return canWrite() && notNullOrEmpty( value );
	}

	/**
	 * Utility method to check if this instance is currently modifiable and that the parameter is
	 * not null or empty before carrying out modifications to the configured properties.
	 * 
	 * If it is no modifiable, an ERROR level error log will be produced
	 * 
	 * @return true if this instance is currently modifiable and that the parameter is not null or
	 *         empty, false otherwise
	 */
	private boolean canWrite( Map<?,?> value )
	{
		return canWrite() && notNullOrEmpty( value );
	}

	/**
	 * Utility method to make sure that a string is neither null nor empty
	 * 
	 * @param toTest the {@link String} to test
	 * @return true if the string is neither null nor empty, false if it is null or empty
	 */
	private boolean notNullOrEmpty( String toTest )
	{
		return toTest != null && toTest.trim().length() > 0;
	}

	/**
	 * Utility method to make sure that a URL is neither null nor empty
	 * 
	 * @param toTest the {@link URL} to test
	 * @return true if the URL is neither null nor empty, false if it is null or empty
	 */
	private boolean notNullOrEmpty( URL toTest )
	{
		return toTest != null && toTest.toString().length() > 0;
	}

	/**
	 * Utility method to make sure that a collection is neither null nor empty
	 * 
	 * @param toTest the {@link Collection} to test
	 * @return true if the collection is neither null nor empty, false if it is null or empty
	 */
	private boolean notNullOrEmpty( Collection<?> toTest )
	{
		return toTest != null && !toTest.isEmpty();
	}

	/**
	 * Utility method to make sure that a map is neither null nor empty
	 * 
	 * @param toTest the {@link Map} to test
	 * @return true if the collection is neither null nor empty, false if it is null or empty
	 */
	private boolean notNullOrEmpty( Map<?,?> toTest )
	{
		return toTest != null && !toTest.isEmpty();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

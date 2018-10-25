/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-gateway.
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

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The purpose of this class is to encapsulate all data required to configure a federate
 */
public class FederateConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    private static final Logger logger = LogManager.getFormatterLogger(FederateConfiguration.class);

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationName;
	private String federateName;
	private String federateType;
	private Set<URL> modules;
	private Set<URL> joinModules;
	private Set<String> publishedInteractions;
	private Set<String> subscribedInteractions;
	private Map<String, Set<String>> publishedAttributes;
	private Map<String, Set<String>> subscribedAttributes;
	
	// TODO
    private int maxReconnectAttempts = 5;
    private long waitReconnectMs = 5000;
    private boolean isLateJoiner = false;
    private double lookAhead = 1.0;
    private double stepSize = 0.1;	
    
    // flag indicating whether the configuration in this instance is modifiable or not
    // IMPORTANT: Do not expose this to modification from outside of this class!
    private boolean isReadOnly = false;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateConfiguration(String federationName, String federateName, String federateType)
	{
		this( federationName, federateName, federateType, 
		      null, null, // modules, joinModules 
		      null, null, // published attributes, subscribed attributes
		      null, null );// published interactions, subscribed interactions
	}
	
	public FederateConfiguration( String federationName, String federateName, String federateType,
	                              Set<URL> modules, Set<URL> joinModules,
	                              Map<String,Set<String>> publishedAttributes,
	                              Map<String,Set<String>> subscribedAttributes,
	                              Set<String> publishedInteractions,
	                              Set<String> subscribedInteractions )
	{
		this.federationName = federationName;
		
		this.federateName = federateName;
		this.federateType= federateType;
		
		this.modules = modules == null ? new HashSet<>() : modules;
		this.joinModules = joinModules == null ? new HashSet<>() : joinModules;
		this.publishedAttributes = publishedAttributes == null ? new HashMap<>() : publishedAttributes;
		this.subscribedAttributes = subscribedAttributes == null ? new HashMap<>() : subscribedAttributes;
		this.publishedInteractions = publishedInteractions == null ? new HashSet<>() : publishedInteractions;
		this.subscribedInteractions = subscribedInteractions == null ? new HashSet<>() : subscribedInteractions;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Determine if this instance is currently modifiable.
	 * 
	 * The instance will become unmodifiable once any of the configured properties are read. 
	 * 
	 * @return true if this instance is currently modifiable, false otherwise.
	 */
	public boolean isReadOnly()
	{
		return this.isReadOnly;
	}
	
	/**
	 * Make this this instance is unmodifiable.
	 * 
	 * This is called when any of the configured properties are read. 
	 */
	private void makeReadOnly()
	{
		this.isReadOnly = true;
	}

	/**
	 * Utility method to check if this instance is currently modifiable before carrying out modifications
	 * to the configured properties.
	 * 
	 * If it is logged, an ERROR level error log will be produced
	 * 
	 * @return true if this instance is modifiable, false otherwise.
	 */
	private boolean canWrite()
	{
		if(isReadOnly())
		{
			// can't modify values
			logger.error(String.format("Configuration for federate '%s' of type '%s' in federation '%s' is locked and cannot be modified.", 
			                           this.federateName, this.federateType, this.federationName));
			return false;
		}
		return true;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Obtain the configured federation name
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the configured federation name (not modifiable)
	 */
	public String getFederationName()
	{
		makeReadOnly();
		return federationName;
	}

	/**
	 * Obtain the configured federate name
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the configured federate name (not modifiable)
	 */
	public String getFederateName()
	{
		makeReadOnly();
		return federateName;
	}

	/**
	 * Obtain the configured federate type
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the configured federate type (not modifiable)
	 */
	public String getFederateType()
	{
		makeReadOnly();
		return federateType;
	}

	/**
	 * Obtain the configured FOM modules
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the configured FOM modules (not modifiable)
	 */
	public Collection<URL> getModules()
	{
		makeReadOnly();
		return Collections.unmodifiableSet( modules );
	}

	/**
	 * Obtain the configured join FOM modules
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the configured join FOM modules (not modifiable)
	 */
	public Collection<URL> getJoinModules()
	{
		makeReadOnly();
		return Collections.unmodifiableSet( joinModules );
	}

	/**
	 * Obtain the published interactions
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the published interactions (not modifiable)
	 */
	public Collection<String> getPublishedInteractions()
	{
		makeReadOnly();
		return Collections.unmodifiableSet( publishedInteractions );
	}

	/**
	 * Obtain the subscribed interactions
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the subscribed interactions (not modifiable)
	 */
	public Collection<String> getSubscribedInteractions()
	{
		makeReadOnly();
		return Collections.unmodifiableSet( subscribedInteractions );
	}

	/**
	 * Obtain the published attributes
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the published attributes (not modifiable)
	 */
	public Map<String,Set<String>> getPublishedAttributes()
	{
		makeReadOnly();
		return Collections.unmodifiableMap( publishedAttributes );
	}

	/**
	 * Obtain the subscribed attributes
	 * 
	 * NOTE: This will cause the configiuration to become locked.
	 *       No further modfications may be made after this method
	 *       is called, and this instance will become read only.
	 *       See also {@link #isReadOnly()}
	 * 
	 * @return the subscribed attributes (not modifiable)
	 */
	public Map<String,Set<String>> getSubscribedAttributes()
	{
		makeReadOnly();
		return Collections.unmodifiableMap( subscribedAttributes );
	}

	/**
	 * Add a FOM module to the configuration
	 * 
	 * @param module the FOM module to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addModule( URL module )
	{
		if(canWrite())
		{
			if( module != null )
				this.modules.add( module );
		}
		return this;
	}

	/**
	 * Add FOM modules to the configuration
	 * 
	 * @param modules the FOM modules to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addModules( Collection<URL> modules )
	{
		if(canWrite())
		{
    		if( modules != null )
    			this.modules.addAll( modules );
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
		if(canWrite())
		{
    		if( joinModule != null )
    			this.joinModules.add( joinModule );
		}

		return this;
	}

	/**
	 * Add join FOM modules to the configuration
	 * 
	 * @param joinModules the join FOM modules to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addJoinModules( Collection<URL> joinModules )
	{
		if(canWrite())
		{
    		if( joinModules != null )
    			this.joinModules.addAll( joinModules );
		}

		return this;
	}

	/**
	 * Add a published attribute to the configuration
	 * 
	 * @param klassIdentifier the identifier of the class to which the attribute belongs
	 * @param attributeIdentifier the identifier of the attribute on the class to publish
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAtribute( String klassIdentifier,
	                                                   String attributeIdentifier )
	{
		if(canWrite())
		{
    		this.publishedAttributes.computeIfAbsent( klassIdentifier,
    		                                          x -> new HashSet<>() ).add( attributeIdentifier );
		}

		return this;
	}

	/**
	 * Add published attributes to the configuration
	 * 
	 * @param klassIdentifier the identifier of the class to which the attributes belong
	 * @param attributeIdentifiers the identifiers of the attributes on the class to publish
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAtributes( String klassIdentifier,
	                                                    String[] attributeIdentifiers )
	{
		if(canWrite())
		{
    		if( attributeIdentifiers != null )
    			addPublishedAtributes( klassIdentifier, Arrays.asList( attributeIdentifiers ) );
		}

		return this;
	}

	/**
	 * Add published attributes to the configuration
	 * 
	 * @param klassIdentifier the identifier of the class to which the attributes belong
	 * @param attributeIdentifiers the identifiers of the attributes on the class to publish
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAtributes( String klassIdentifier,
	                                                    Collection<String> attributeIdentifiers )
	{
		if(canWrite())
		{
    		this.publishedAttributes.computeIfAbsent( klassIdentifier,
    		                                          x -> new HashSet<>() ).addAll( attributeIdentifiers );
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
	public FederateConfiguration addPublishedAtributes( Map<String,Collection<String>> publishedAttributes )
	{
		if(canWrite())
		{
			mergeSetMaps( publishedAttributes, this.publishedAttributes );
		}

		return this;
	}

	/**
	 * Add a subscribed attribute to the configuration
	 * 
	 * @param klassIdentifier the identifier of the class to which the attribute belongs
	 * @param attributeIdentifier the identifier of the attribute on the class to subscribe to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAtribute( String klassIdentifier,
	                                                    String attributeIdentifier )
	{
		if(canWrite())
		{
    		this.subscribedAttributes.computeIfAbsent( klassIdentifier,
    		                                           x -> new HashSet<>() ).add( attributeIdentifier );
		}
    		
		return this;
	}

	/**
	 * Add subscribed attributes to the configuration
	 * 
	 * @param klassIdentifier the identifier of the class to which the attributes belong
	 * @param attributeIdentifiers the identifiers of the attributes on the class to subscribed to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAtributes( String klassIdentifier,
	                                                     String[] attributeIdentifiers )
	{
		if(canWrite())
		{
    		if( attributeIdentifiers != null )
    			addSubscribedAtributes( klassIdentifier, Arrays.asList( attributeIdentifiers ) );
		}

		return this;
	}

	/**
	 * Add subscribed attributes to the configuration
	 * 
	 * @param klassIdentifier the identifier of the class to which the attributes belong
	 * @param attributeIdentifiers the identifiers of the attributes on the class to subscribe to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAtributes( String klassIdentifier,
	                                                     Collection<String> attributeIdentifiers )
	{
		if(canWrite())
		{
    		this.subscribedAttributes.computeIfAbsent( klassIdentifier,
    		                                           x -> new HashSet<>() ).addAll( attributeIdentifiers );
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
	public FederateConfiguration addSubscribedAtributes( Map<String,Collection<String>> subscribedAttributes )
	{
		if(canWrite())
		{
			mergeSetMaps( subscribedAttributes, this.subscribedAttributes );
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
		if(canWrite())
		{
    		if( interactionIdentifier != null )
    			this.publishedInteractions.add( interactionIdentifier );
		}
		
		return this;
	}

	/**
	 * Add published interactions to the configuration
	 * 
	 * @param interactionIdentifiers the published interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addPublishedInteractions( String[] interactionIdentifiers )
	{
		if(canWrite())
		{
    		if( interactionIdentifiers != null )
    			addPublishedInteractions( Arrays.asList( interactionIdentifiers ) );
		}

		return this;
	}

	/**
	 * Add published interactions to the configuration
	 * 
	 * @param interactionIdentifiers the published interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addPublishedInteractions( Collection<String> interactionIdentifiers )
	{
		if(canWrite())
		{
    		if( interactionIdentifiers != null )
    			this.publishedInteractions.addAll( interactionIdentifiers );
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
		if(canWrite())
		{
    		if( interactionIdentifier != null )
    			this.subscribedInteractions.add( interactionIdentifier );
		}

		return this;
	}

	/**
	 * Add subscribed interactions to the configuration
	 * 
	 * @param interactionIdentifiers the subscribed interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedInteractions( String[] interactionIdentifiers )
	{
		if(canWrite())
		{
    		if( interactionIdentifiers != null )
    			addSubscribedInteractions( Arrays.asList( interactionIdentifiers ) );
		}

		return this;
	}

	/**
	 * Add subscribed interactions to the configuration
	 * 
	 * @param interactionIdentifiers the subscribed interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedInteractions( Collection<String> interactionIdentifiers )
	{
		if(canWrite())
		{
    		if( interactionIdentifiers != null )
    			this.subscribedInteractions.addAll( interactionIdentifiers );
		}

		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Utility Methods /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to merge the content of a maps of sets into another map of sets
	 * 
	 * @param src the map containing the source data
	 * @param dest the existing map to merge the source data into
	 */
	private void mergeSetMaps( Map<String,Collection<String>> src, Map<String,Set<String>> dest )
	{
		if( src == null || dest == null )
			return;

		// this is "clever"
		// src.entrySet().forEach( (entry) -> dest.computeIfAbsent(entry.getKey(), x -> new HashSet<>()).addAll( entry.getValue() ) );

		// this is far more readable, IMHO
		for( Entry<String,Collection<String>> entry : src.entrySet() )
		{
			dest.computeIfAbsent( entry.getKey(), x -> new HashSet<>() ).addAll( entry.getValue() );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

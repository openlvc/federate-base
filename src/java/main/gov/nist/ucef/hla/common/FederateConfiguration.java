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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The purpose of this class is to encapsulate all data required to configure a federate
 */
public class FederateConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationName;
	private String federateName;
	private String federateType;
	private List<URL> modules = new ArrayList<>();
	private List<URL> joinModules = new ArrayList<>();
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
	                              List<URL> modules, List<URL> joinModules,
	                              Map<String,Set<String>> publishedAttributes,
	                              Map<String,Set<String>> subscribedAttributes,
	                              Set<String> publishedInteractions,
	                              Set<String> subscribedInteractions )
	{
		this.federationName = federationName;
		
		this.federateName = federateName;
		this.federateType= federateType;
		
		this.modules = modules == null ? new ArrayList<URL>() : modules;
		this.joinModules = joinModules == null ? new ArrayList<URL>() : joinModules;
		this.publishedAttributes = publishedAttributes == null ? new HashMap<>() : publishedAttributes;
		this.subscribedAttributes = subscribedAttributes == null ? new HashMap<>() : subscribedAttributes;
		this.publishedInteractions = publishedInteractions == null ? new HashSet<>() : publishedInteractions;
		this.subscribedInteractions = subscribedInteractions == null ? new HashSet<>() : subscribedInteractions;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Obtain the configured federation name
	 * @return the configured federation name
	 */
	public String getFederationName()
	{
		return federationName;
	}

	/**
	 * Obtain the configured federate name
	 * @return the configured federate name
	 */
	public String getFederateName()
	{
		return federateName;
	}

	/**
	 * Obtain the configured federate type
	 * @return the configured federate type
	 */
	public String getFederateType()
	{
		return federateType;
	}

	/**
	 * Obtain the configured FOM modules
	 * @return the configured FOM modules
	 */
	public List<URL> getModules()
	{
		return modules;
	}

	/**
	 * Obtain the configured join FOM modules
	 * @return the configured join FOM modules
	 */
	public List<URL> getJoinModules()
	{
		return joinModules;
	}

	/**
	 * Obtain the published interactions
	 * @return the published interactions
	 */
	public Set<String> getPublishedInteractions()
	{
		return publishedInteractions;
	}

	/**
	 * Obtain the subscribed interactions
	 * @return the subscribed interactions
	 */
	public Set<String> getSubscribedInteractions()
	{
		return subscribedInteractions;
	}

	/**
	 * Obtain the published attributes
	 * @return the published attributes
	 */
	public Map<String,Set<String>> getPublishedAttributes()
	{
		return publishedAttributes;
	}

	/**
	 * Obtain the subscribed attributes
	 * @return the subscribed attributes
	 */
	public Map<String,Set<String>> getSubscribedAttributes()
	{
		return subscribedAttributes;
	}

	/**
	 * Add a FOM module to the configuration
	 * @param module the FOM module to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addModule(URL module)
	{
		if(module != null)
			this.modules.add( module );
		
		return this;
	}
	
	/**
	 * Add FOM modules to the configuration
	 * @param modules the FOM modules to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addModules(List<URL> modules)
	{
		if(modules != null)
			this.modules.addAll( modules );
		
		return this;
	}
	
	/**
	 * Add a join FOM module to the configuration
	 * @param joinModule the join FOM module to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addJoinModule(URL joinModule)
	{
		if(joinModule != null)
			this.joinModules.add( joinModule );
		
		return this;
	}
	
	/**
	 * Add join FOM modules to the configuration
	 * @param joinModules the join FOM modules to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addJoinModules(List<URL> joinModules)
	{
		if(joinModules != null)
			this.joinModules.addAll( joinModules );
		
		return this;
	}
	
	/**
	 * Add published attributes to the configuration
	 * 
	 * @param klassIdentifier the identifier of the class to which the attributes belong
	 * @param attributeIdentifiers the identifiers of the attributes on the class to publish
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAtributes(String klassIdentifier, Set<String> attributeIdentifiers)
	{
		this.publishedAttributes.computeIfAbsent(klassIdentifier, x -> new HashSet<>()).addAll( attributeIdentifiers );
		
		return this;
	}
	
	/**
	 * Add published attributes to the configuration
	 * @param publishedAttributes the published attributes to add to the configuration as a map linking
	 * 		  the identifiers of the classes with the identifiers of the attributes on the class which are
	 * 		  to be published
	 * @return this instance
	 */
	public FederateConfiguration addPublishedAtributes(Map<String, Set<String>> publishedAttributes)
	{
		mergeSetMaps(publishedAttributes, this.publishedAttributes);
		
		return this;
	}
	
	/**
	 * Add subscribed attributes to the configuration
	 * 
	 * @param klassIdentifier the identifier of the class to which the attributes belong
	 * @param attributeIdentifiers the identifiers of the attributes on the class to subscribe to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAtributes(String klassIdentifier, Set<String> attributeIdentifiers)
	{
		this.subscribedAttributes.computeIfAbsent(klassIdentifier, x -> new HashSet<>()).addAll( attributeIdentifiers );
		
		return this;
	}
	
	/**
	 * Add subscribed attributes to the configuration
	 * @param subscribedAttributes the subscribed attributes to add to the configuration as a map linking
	 * 		  the identifiers of the classes with the identifiers of the attributes on the class which are
	 * 		  to be subscribed to
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedAtributes(Map<String, Set<String>> subscribedAttributes)
	{
		mergeSetMaps(subscribedAttributes, this.subscribedAttributes);
		
		return this;
	}
	
	/**
	 * Add a published interaction to the configuration
	 * @param interactionIdentifier the published interaction to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addPublishedInteraction(String interactionIdentifier)
	{
		if(interactionIdentifier != null)
			this.publishedInteractions.add( interactionIdentifier );
		
		return this;
	}
	
	/**
	 * Add published interactions to the configuration
	 * @param interactionIdentifiers the published interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addPublishedInteractions(Set<String> interactionIdentifiers)
	{
		if(interactionIdentifiers != null)
			this.publishedInteractions.addAll( interactionIdentifiers );

		return this;
	}
	
	/**
	 * Add a subscribed interaction to the configuration
	 * @param interactionIdentifier the subscribed interaction to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedInteraction(String interactionIdentifier)
	{
		if(interactionIdentifier != null)
			this.subscribedInteractions.add( interactionIdentifier );
		
		return this;
	}
	
	/**
	 * Add subscribed interactions to the configuration
	 * @param interactionIdentifiers the subscribed interactions to add to the configuration
	 * @return this instance
	 */
	public FederateConfiguration addSubscribedInteractions(Set<String> interactionIdentifiers)
	{
		if(interactionIdentifiers != null)
			this.subscribedInteractions.addAll( interactionIdentifiers );
		
		return this;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Utility Methods /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to merge the content of a maps of sets into another map of sets
	 * 
	 * Doesn't really need to use generics here, but why not, eh? :)
	 * 
	 * @param src the map containing the source data
	 * @param dest the existing map to merge the source data into
	 */
	private <K, V> void mergeSetMaps(Map<K, Set<V>> src, Map<K, Set<V>> dest)
	{
		if(src == null || dest == null)
			return;
		
		// this is "clever"
		// src.entrySet().forEach( (entry) -> dest.computeIfAbsent(entry.getKey(), x -> new HashSet<>()).addAll( entry.getValue() ) );
		
		// this is far more readable, IMHO
		for( Entry<K,Set<V>> entry : src.entrySet() )
		{
			dest.computeIfAbsent(entry.getKey(), x -> new HashSet<>()).addAll( entry.getValue() );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

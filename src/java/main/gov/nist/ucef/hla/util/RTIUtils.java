/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-java.
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
package gov.nist.ucef.hla.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.ucef.hla.common.FederateBase;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleSetFactory;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeHandleValueMapFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.ParameterHandleValueMapFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

public class RTIUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getFormatterLogger( FederateBase.class );
	
	private static final HLACodecUtils codecUtils = HLACodecUtils.instance();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIambassador rtiamb;

	private HLAfloat64TimeFactory timeFactory;
	private ParameterHandleValueMapFactory parameterMapFactory;
	private AttributeHandleValueMapFactory attributeMapFactory;
	private AttributeHandleSetFactory attributeHandleSetFactory;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RTIUtils(RTIambassador rtiAmbassador)
	{
		this.rtiamb = rtiAmbassador;
		
		try
		{
			// cache the commonly used factories
			this.timeFactory = (HLAfloat64TimeFactory)rtiamb.getTimeFactory();
			this.parameterMapFactory = rtiamb.getParameterHandleValueMapFactory();
			this.attributeMapFactory = rtiamb.getAttributeHandleValueMapFactory();
			this.attributeHandleSetFactory = rtiamb.getAttributeHandleSetFactory();
		}
		catch( FederateNotExecutionMember | NotConnected e )
		{
			logger.error( "Failed to initialize RTIUtils: %s", e.getMessage());
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// TIME ///////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	public HLAfloat64Time makeTime( double time )
	{
		return this.timeFactory.makeTime( time );
	}
	
	public HLAfloat64Interval makeInterval( double interval )
	{
		return timeFactory.makeInterval( interval );		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// HANDLE MAPS ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ParameterHandleValueMap makeEmptyParameterMap()
	{
		return makeParameterMap(0);
	}
	
	public ParameterHandleValueMap makeParameterMap(int paramCount)
	{
		// create a new map with an initial capacity - this will grow as required
		return this.parameterMapFactory.create( paramCount );
	}

	public AttributeHandleSet makeAttributeHandles()
	{
		return this.attributeHandleSetFactory.create();
	}
	
	public AttributeHandleValueMap makeEmptyAttributeMap()
	{
		return makeAttributeMap(0);
	}
	
	public AttributeHandleValueMap makeAttributeMap(int paramCount)
	{
		// create a new map with an initial capacity - this will grow as required
		return attributeMapFactory.create( paramCount );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// IDENTIFIER <-> HANDLE LOOKUPS ETC /////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public String getClassIdentifierFromClassHandle( ObjectClassHandle handle )
	{
		try
		{
			return this.rtiamb.getObjectClassName( handle );
		}
		catch( InvalidObjectClassHandle | FederateNotExecutionMember | NotConnected |
			   RTIinternalError e )
		{
			return null;
		}
	}

	public ObjectClassHandle getClassHandleFromClassIdentifier( String identifier )
	{
		try
		{
			return this.rtiamb.getObjectClassHandle( identifier );
		}
		catch( FederateNotExecutionMember | NotConnected | RTIinternalError | NameNotFound e )
		{
			return null;
		}
	}

	public ObjectClassHandle getClassHandleFromInstanceHandle( ObjectInstanceHandle handle )
	{
		try
		{
			return rtiamb.getKnownObjectClassHandle( handle );
		}
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected |
			   RTIinternalError e )
		{
			return null;
		}
	}

	public String getAttributeIdentifierFromHandle( ObjectClassHandle objectClassHandle,
	                                                AttributeHandle attributeHandle )
	{
		try
		{
			return this.rtiamb.getAttributeName( objectClassHandle, attributeHandle );
		}
		catch( AttributeNotDefined | InvalidAttributeHandle | InvalidObjectClassHandle |
			   FederateNotExecutionMember | NotConnected | RTIinternalError e )
		{
			return null;
		}
	}

	public AttributeHandle getHandleFromAttributeIdentifier( ObjectClassHandle objectClassHandle,
	                                                         String identifier )
	{
		try
		{
			return this.rtiamb.getAttributeHandle( objectClassHandle, identifier );
		}
		catch( NameNotFound | InvalidObjectClassHandle | FederateNotExecutionMember |
			   NotConnected | RTIinternalError e )
		{
			return null;
		}
	}

	public String getInteractionIdentifierFromHandle( InteractionClassHandle handle )
	{
		try
		{
			return this.rtiamb.getInteractionClassName( handle );
		}
		catch( InvalidInteractionClassHandle | FederateNotExecutionMember | NotConnected |
			   RTIinternalError e )
		{
			return null;
		}
	}

	public InteractionClassHandle getHandleFromInteractionIdentifier( String identifier )
	{
		try
		{
			return this.rtiamb.getInteractionClassHandle( identifier );
		}
		catch( NameNotFound | FederateNotExecutionMember | NotConnected | RTIinternalError e )
		{
			return null;
		}
	}

	public ParameterHandle getHandleFromParameterIdentifier( InteractionClassHandle klassHandle, String identifier )
	{
		try
		{
			return this.rtiamb.getParameterHandle( klassHandle, identifier );
		}
		catch( NameNotFound | FederateNotExecutionMember | NotConnected | RTIinternalError |
			   InvalidInteractionClassHandle e )
		{
			return null;
		}
	}
	
	public String getParameterIdentifierFromHandle( InteractionClassHandle klassHandle, ParameterHandle handle )
	{
		try
		{
			return this.rtiamb.getParameterName( klassHandle, handle );
		}
		catch( FederateNotExecutionMember | NotConnected | RTIinternalError |
			   InvalidInteractionClassHandle | InteractionParameterNotDefined |
			   InvalidParameterHandle e )
		{
			return null;
		}
	}
	
	public String getObjectInstanceIdentifierFromHandle( ObjectInstanceHandle handle )
	{
		try
		{
			return this.rtiamb.getObjectInstanceName( handle );
		}
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected |
			   RTIinternalError e )
		{
			return null;
		}
	}

	public ObjectInstanceHandle getHandleFromObjectInstanceIdentifier( String identifier )
	{
		try
		{
			return this.rtiamb.getObjectInstanceHandle( identifier );
		}
		catch( ObjectInstanceNotKnown | FederateNotExecutionMember | NotConnected | RTIinternalError e )
		{
			return null;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// OBJECT REGISTRATION ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will register an instance of the class and will return the federation-wide
	 * unique handle for that instance. Later in the simulation, we will update the attribute
	 * values for this instance.
	 */
	public ObjectInstanceHandle registerObject( String federateName, String klassIdentifier )
	{
		return registerObject( federateName, klassIdentifier, null );
	}
	
	/**
	 * This method will register an instance of the class and will return the federation-wide
	 * unique handle for that instance. Later in the simulation, we will update the attribute
	 * values for this instance.
	 */
	public ObjectInstanceHandle registerObject( String federateName, 
	                                            String klassIdentifier, String instanceIdentifier )
	{
		ObjectClassHandle klassHandle = getClassHandleFromClassIdentifier( klassIdentifier );
		if( klassHandle == null )
		{
			logger.error( String.format( "Could not register object instance. No handle was found for class identifier '%s'",
			                             klassIdentifier ) );
			return null;
		}

		try
		{
			ObjectInstanceHandle instanceHandle = null;
			if( StringUtils.isNullOrEmpty( instanceIdentifier ) )
				instanceHandle = rtiamb.registerObjectInstance( klassHandle );
			else
				instanceHandle = rtiamb.registerObjectInstance( klassHandle, instanceIdentifier );
			
			logger.info( String.format( "Federate '%s' registered object instance of class '%s' (class handle is %s). Instance handle is %s",
			                            federateName, 
			                            klassIdentifier, klassHandle, instanceHandle ) );
			
			return instanceHandle;
		}
		catch( ObjectClassNotPublished | ObjectClassNotDefined | SaveInProgress
		    | RestoreInProgress | FederateNotExecutionMember | NotConnected
		    | RTIinternalError | ObjectInstanceNameInUse | ObjectInstanceNameNotReserved e )
		{
			logger.error( String.format( "Federate '%s' failed to register object instance of class '%s' (class handle is %s): %s",
			                             federateName, 
			                             klassIdentifier, klassHandle, e.getMessage() ) );
		}
		
		return null;
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// ATTRIBUTE MANIPULATION //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                String identifier,
	                                                short value,
	                                                AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle,
		                        identifier,
		                        Short.toString( value ),
		                        attributes );
	}

	public AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                String identifier,
	                                                int value,
	                                                AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle,
		                        identifier,
		                        Integer.toString( value ),
		                        attributes );
	}

	public AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                String identifier,
	                                                long value,
	                                                AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle, identifier, Long.toString( value ), attributes );
	}

	public AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                String identifier,
	                                                double value,
	                                                AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle,
		                        identifier,
		                        Double.toString( value ),
		                        attributes );
	}

	public AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                String identifier,
	                                                float value,
	                                                AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle,
		                        identifier,
		                        Float.toString( value ),
		                        attributes );
	}

	public AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                String identifier,
	                                                boolean value,
	                                                AttributeHandleValueMap attributes )
	{
		return updateAttribute( objectClassHandle,
		                        identifier,
		                        Boolean.toString( value ),
		                        attributes );
	}
	
	public AttributeHandleValueMap updateAttribute( ObjectClassHandle objectClassHandle,
	                                                String identifier,
	                                                String value,
	                                                AttributeHandleValueMap attributes )
	{
		AttributeHandle attrHandle = getHandleFromAttributeIdentifier( objectClassHandle, identifier );

		if( attrHandle != null )
		{
			HLAunicodeString attrValue = codecUtils.makeString( value );
			attributes.put( attrHandle, attrValue.toByteArray() );
		}

		return attributes;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// PUBLISH AND SUBSCRIBE REGISTRATION /////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * publishing to the federation.
	 */
	public void registerPublishedInteractions(String federateName, Collection<String> interactionIDs)
	{
		if( interactionIDs.isEmpty() )
		{
			logger.debug( String.format( "Federate '%s' will not publish any interactions.",
			                             federateName ) );
		}
		else
		{
			for( String interactionID : interactionIDs )
			{
				registerPublishedInteraction( federateName, interactionID );
			}
		}
	}

	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * publishing to the federation.
	 */
	public void registerPublishedInteraction(String federateName, String interactionID)
	{
		InteractionClassHandle handle = getHandleFromInteractionIdentifier( interactionID );
		// do the publication - we only need to tell the RTI about the interaction's class,
		// not the associated parameters, so this is very simple:
		if(registerPublishedInteraction( handle ))
		{
			logger.debug( String.format( "Federate '%s' registered intent to publish interaction '%s', handle is %s",
			                             federateName, interactionID, handle ) );
		}
		else
		{
			logger.error( String.format( "Federate '%s' failed to register intent to publish interaction '%s', handle was %s",
			                             federateName, interactionID,
			                             handle == null ? "null" : handle) );
		}
	}
	
	/**
	 * This method will inform the RTI about the an interaction that a federate will be publishing to 
	 * the federation.
	 */
	public boolean registerPublishedInteraction(InteractionClassHandle handle)
	{
		if(handle != null)
		{
			try
			{
				rtiamb.publishInteractionClass( handle );
				return true;
			}
			catch( InteractionClassNotDefined | SaveInProgress | RestoreInProgress
				| FederateNotExecutionMember | NotConnected | RTIinternalError e )
			{
				logger.error( String.format( "Failed to register intent to publish interaction with handle %s: %s",
				                             handle, e.getMessage() ) );
			}
		}
		else
		{
			logger.error( "Failed to register intent to publish interaction - handle was null." );
		}
		return false;
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and to which classes they belong.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values 
	 */
	public void registerPublishedAttributes(String federateName, Map<String,Set<String>> publishedAttributes) throws RTIexception
	{
		if(publishedAttributes.isEmpty())
		{
			logger.debug( String.format( "Federate '%s' will not publish any attributes.",
			                             federateName ) );
		}
		else
		{
			for( Entry<String,Set<String>> publication : publishedAttributes.entrySet() )
			{
				// this list is for logging purposes only
				List<String> publicationDetails = new ArrayList<>();
				
				String klassIdentifier = publication.getKey();
				Set<String> attributes = publication.getValue();
				
				// get all the handle information for the attributes of the current class
				ObjectClassHandle klassHandle = getClassHandleFromClassIdentifier( klassIdentifier );
				// package the information into a handle set
				AttributeHandleSet attributeHandleSet = makeAttributeHandles();
				for( String attribute : attributes )
				{
					AttributeHandle attributeHandle = getHandleFromAttributeIdentifier( klassHandle, attribute );
					attributeHandleSet.add( attributeHandle );
					publicationDetails.add( String.format( "'%s' (handle = %s)", 
					                                       attribute, attributeHandle ) );
				}
				
				// do the actual publication
				if(registerPublishedAttributes( klassHandle, attributeHandleSet ))
				{
					logger.debug( String.format( "Federate '%s' registered intent to publish the following %d attribute(s) of '%s' (handle = %s): %s", 
					                             federateName,
					                             publicationDetails.size(),
					                             klassIdentifier, klassHandle, 
					                             String.join( ", ", publicationDetails ) ) );
				}
				else
				{
					logger.error( String.format( "Federate '%s' failed to register intent to publish the following %d attribute(s) of '%s' (handle = %s): %s", 
					                             federateName,
					                             publicationDetails.size(),
					                             klassIdentifier, klassHandle, 
					                             String.join( ", ", publicationDetails ) ) );
				}
			}
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and to which classes they belong.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values 
	 */
	public boolean registerPublishedAttributes(ObjectClassHandle klassHandle, AttributeHandleSet attributes)
	{
		if(klassHandle != null && attributes != null)
		{
			try
			{
				rtiamb.publishObjectClassAttributes( klassHandle, attributes );
				return true;
			}
			catch( SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected 
				| RTIinternalError | AttributeNotDefined | ObjectClassNotDefined e )
			{
				logger.error( String.format( "Failed to register intent to publish attributes for class with handle %s: %s",
				                             klassHandle, e.getMessage() ) );
			}
		}
		else
		{
			if(klassHandle == null && attributes == null)
			{
				logger.error( "Failed to register intent to publish attributes - the class and attribute handles were both null." );
			}
			else if(klassHandle == null)
			{
				logger.error( "Failed to register intent to publish attributes - the class handle was null." );
			}
			else
			{
				logger.error( "Failed to register intent to publish attributes - the attributes handle was null." );
			}
		}
		return false;
	}
	
	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * interested in hearing about as other federates produce them.
	 */
	public void registerSubscribedInteractions(String federateName, Collection<String> subscribedInteractions)
	{
		if(subscribedInteractions.isEmpty())
		{
			logger.debug( String.format( "Federate '%s' will not subscribe to any interactions.",
			                             federateName ) );
		}
		else
		{
			for( String interactionID : subscribedInteractions )
			{
				registerSubscribedInteraction(federateName, interactionID);
			}
		}
	}
	
	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * publishing to the federation.
	 */
	public void registerSubscribedInteraction(String federateName, String interactionID)
	{
		InteractionClassHandle handle = getHandleFromInteractionIdentifier( interactionID );
		// do the publication - we only need to tell the RTI about the interaction's class,
		// not the associated parameters, so this is very simple:
		if(registerSubscribedInteraction( handle ))
		{
			logger.debug( String.format( "Federate '%s' registered subscription to interaction '%s', handle is %s",
			                             federateName, interactionID, handle ) );
		}
		else
		{
			logger.error( String.format( "Federate '%s' failed to register subscription to interaction '%s', handle was %s",
			                             federateName, interactionID,
			                             handle == null ? "null" : handle) );
		}
	}
	
	/**
	 * This method will inform the RTI about the an interaction that a federate will be publishing to 
	 * the federation.
	 */
	public boolean registerSubscribedInteraction(InteractionClassHandle handle)
	{
		if(handle != null)
		{
			try
			{
				rtiamb.subscribeInteractionClass( handle );
				return true;
			}
			catch( InteractionClassNotDefined | SaveInProgress | RestoreInProgress
				| FederateNotExecutionMember | NotConnected | RTIinternalError
				| FederateServiceInvocationsAreBeingReportedViaMOM e )
			{
				logger.error( String.format( "Failed to register subscription to interaction with handle %s: %s",
				                             handle, e.getMessage() ) );
			}
		}
		else
		{
			logger.error( "Failed to register subscription to interaction - handle was null." );
		}
		return false;
	}
	
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * interested in hearing about, and to which classes they belong.
	 * 
	 * We need to subscribe to hear about information on attributes of classes created and altered
	 * in other federates 
	 */
	public void registerSubscribedAttributes(String federateName, Map<String,Set<String>> subscribedAttributes)
	{
		if(subscribedAttributes.isEmpty())
		{
			logger.debug( String.format( "Federate '%s' will not subscribe to any attributes.",
			                             federateName ) );
		}
		else
		{
			for( Entry<String,Set<String>> subscription : subscribedAttributes.entrySet() )
			{
				// this list is for logging purposes only
				List<String> subscriptionDetails = new ArrayList<>();
				
				// get all the handle information for the attributes of the current class
				String klassIdentifier = subscription.getKey();
				ObjectClassHandle klassHandle = getClassHandleFromClassIdentifier( klassIdentifier );
				// package the information into the handle set
				AttributeHandleSet attributeHandleSet = makeAttributeHandles();
				for( String attribute : subscription.getValue() )
				{
					AttributeHandle attributeHandle = getHandleFromAttributeIdentifier( klassHandle, attribute );
					attributeHandleSet.add( attributeHandle );
					subscriptionDetails.add( String.format( "'%s' (handle = %s)",
					                                        attribute, attributeHandle ) );
				}
				
				// do the actual subscription
				if(registerSubscribedAttributes( klassHandle, attributeHandleSet ))
				{
					logger.debug( String.format( "Federate '%s' will subscribe to the following %d attribute(s) of '%s' (handle = %s): %s", 
					                             federateName,
					                             subscriptionDetails.size(),
					                             klassIdentifier, klassHandle, 
					                             String.join( ", ", subscriptionDetails ) ) );
				}
				else
				{
					logger.error( String.format( "Federate '%s' failed to subscribe to the following %d attribute(s) of '%s' (handle = %s): %s", 
					                             federateName,
					                             subscriptionDetails.size(),
					                             klassIdentifier, klassHandle, 
					                             String.join( ", ", subscriptionDetails ) ) );
				}
			}
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and to which classes they belong.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values 
	 */
	public boolean registerSubscribedAttributes(ObjectClassHandle klassHandle, AttributeHandleSet attributes)
	{
		if(klassHandle != null && attributes != null)
		{
			try
			{
				rtiamb.subscribeObjectClassAttributes( klassHandle, attributes );
				return true;
			}
			catch( SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected 
				| RTIinternalError | AttributeNotDefined | ObjectClassNotDefined e )
			{
				logger.error( String.format( "Failed to subscribe to attributes for class with handle %s: %s",
				                             klassHandle, e.getMessage() ) );
			}
		}
		else
		{
			if(klassHandle == null && attributes == null)
			{
				logger.error( "Failed to subscribe to attributes - the class and attribute handles were both null." );
			}
			else if(klassHandle == null)
			{
				logger.error( "Failed to subscribe to attributes - the class handle was null." );
			}
			else
			{
				logger.error( "Failed to subscribe to attributes - the attributes handle was null." );
			}
		}
		return false;
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// PUBLICATION /////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values, but with no tag or timestamp information.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param instanceIdentifier the identifier of the instance for the attributes 
	 * @param attributes the attributes and their associated values
	 * @throws RTIexception
	 */
	public void publishAttributes( String instanceIdentifier, AttributeHandleValueMap attributes )
	{
		publishAttributes( instanceIdentifier, attributes, null, null); 
	}
	
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values, but with no tag or timestamp information.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param handle the instance handle of the object to which the attributes belong 
	 * @param attributes the attributes and their associated values
	 * @throws RTIexception
	 */
	public void publishAttributes( ObjectInstanceHandle handle, AttributeHandleValueMap attributes )
	{
		publishAttributes( handle, attributes, null, null); 
	}
	
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values and timestamp information, but with no associated tag.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param instanceIdentifier the identifier of the instance for the attributes 
	 * @param attributes the attributes and their associated values
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishAttributes( String instanceIdentifier, AttributeHandleValueMap attributes, 
	                               HLAfloat64Time time )
	{
		publishAttributes( instanceIdentifier, attributes, null, time); 
	}
	
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values and timestamp information, but with no associated tag.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param handle the instance handle of the object to which the attributes belong 
	 * @param attributes the attributes and their associated values
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishAttributes( ObjectInstanceHandle handle, AttributeHandleValueMap attributes, 
	                               HLAfloat64Time time )
	{
		publishAttributes( handle, attributes, null, time); 
	}
	
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values and timestamp information, but with no associated tag.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param instanceIdentifier the identifier of the instance for the attributes 
	 * @param attributes the attributes and their associated values
	 * @param tag the tag of the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishAttributes( String instanceIdentifier, AttributeHandleValueMap attributes, 
	                               byte[] tag)
	{
		publishAttributes( instanceIdentifier, attributes, tag, null); 
	}
	
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values and timestamp information, but with no associated tag.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param handle the instance handle of the object to which the attributes belong 
	 * @param attributes the attributes and their associated values
	 * @param tag the tag of the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishAttributes( ObjectInstanceHandle handle, AttributeHandleValueMap attributes, 
	                               byte[] tag )
	{
		publishAttributes( handle, attributes, tag, null); 
	}
	
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values, timestamp and tag information.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param instanceIdentifier the identifier of the instance for the attributes
	 * @param attributes the attributes and their associated values
	 * @param tag the tag of the interaction
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishAttributes( String instanceIdentifier, AttributeHandleValueMap attributes,
	                               byte[] tag, HLAfloat64Time time)
	{
		ObjectInstanceHandle servedHandle = getHandleFromObjectInstanceIdentifier( instanceIdentifier );
		if(servedHandle == null)
		{
			logger.error(String.format( "Failed to publish attribute update for instance '%s' - unknown object instance.", 
			                            instanceIdentifier ));
			return;
		}
		
		publishAttributes( servedHandle, attributes, tag, time );
	}
	
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values, timestamp and tag information.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param handle the instance handle of the object to which the attributes belong 
	 * @param attributes the attributes and their associated values
	 * @param tag the tag of the interaction
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishAttributes( ObjectInstanceHandle handle, AttributeHandleValueMap attributes,
	                               byte[] tag, HLAfloat64Time time)
	{
		// sanity check the handle for null - without a handle we can't do anything
		if( handle == null )
		{
			// we print a stack trace here so it's a bit more obvious how the situation is arising for 
			// debugging purposes - a null handle for the object instance is unlikely, and probably will
			// be hard to debug if it does.
			StringBuilder errorMsg = new StringBuilder("Failed to publish attribute update - instance handle was null.\n");
			errorMsg.append( makeStackStrace( Thread.currentThread().getStackTrace() ) );
			logger.error( errorMsg.toString() );
			
			return;
		}
		
		// sanity check the tag for null
		tag = tag == null ? new byte[0] : tag;
		
		attributes = attributes == null ? makeEmptyAttributeMap() : attributes;
		if( attributes == null )
		{
			String instanceIdentifier = getObjectInstanceIdentifierFromHandle( handle );
			ObjectClassHandle classHandle = getClassHandleFromInstanceHandle( handle );
			String classIdentifier = getClassIdentifierFromClassHandle( classHandle );
			if( instanceIdentifier != null )
			{
				logger.error(String.format( "Failed to publish attribute update for instance '%s' with handle %s of type '%s' - attributes were null.", 
				                            instanceIdentifier, classHandle, classIdentifier ));
			}
			else
			{
				logger.error(String.format( "Failed to publish attribute update for instance with handle %s of type '%s' - attributes were null.", 
				                            classHandle,  classIdentifier ));
			}
			return;
		}
		
		try
		{
			if( time == null )
				rtiamb.updateAttributeValues( handle, attributes, tag );
			else
				rtiamb.updateAttributeValues( handle, attributes, tag, time );
		}
		catch( AttributeNotOwned | AttributeNotDefined | ObjectInstanceNotKnown | SaveInProgress
			| RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError
			| InvalidLogicalTime e )
		{
			String instanceIdentifier = getObjectInstanceIdentifierFromHandle( handle );
			ObjectClassHandle classHandle = getClassHandleFromInstanceHandle( handle );
			String classIdentifier = getClassIdentifierFromClassHandle( classHandle );
			if( instanceIdentifier != null )
			{
				logger.error(String.format( "Failed to publish attribute update for instance '%s' with handle %s of type '%s': %s", 
				                            instanceIdentifier, classHandle, classIdentifier, 
				                            e.getMessage() ));
			}
			else
			{
				logger.error(String.format( "Failed to publish attribute update for instance with handle %s of type '%s': %s", 
				                            classHandle,  classIdentifier, 
				                            e.getMessage()));
			}
			return;
		}
	}
	
	/**
	 * This method will send out an interaction of the specified type, with no parameters, tag 
	 * or timestamp information.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 * 
	 * @param interactionIdentifier the identifier of the interaction
	 * @throws RTIexception
	 */
	public void publishInteraction( String interactionIdentifier )
	{
		publishInteraction( interactionIdentifier, null); 
	}
	
	/**
	 * This method will send out an interaction of the specified type, with no parameters, tag 
	 * or timestamp information.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 * 
	 * @param handle the handle of the interaction
	 * @throws RTIexception
	 */
	public void publishInteraction( InteractionClassHandle handle )
	{
		publishInteraction( handle, null); 
	}
	
	/**
	 * This method will send out an interaction of the specified type with the given timestamp
	 * information, but with no associated parameters or tag.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 * 
	 * @param interactionIdentifier the identifier of the interaction
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishInteraction( String interactionIdentifier, HLAfloat64Time time )
	{
		publishInteraction( interactionIdentifier, null, time); 
	}
	
	/**
	 * This method will send out an interaction of the specified type with the given timestamp
	 * information, but with no associated parameters or tag.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 * 
	 * @param handle the handle of the interaction
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishInteraction( InteractionClassHandle handle, HLAfloat64Time time )
	{
		publishInteraction( handle, null, time); 
	}
	
	/**
	 * This method will send out an interaction of the specified type with the given timestamp and tag,
	 * information but with no associated parameters.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 * 
	 * @param interactionIdentifier the identifier of the interaction
	 * @param tag the tag of the interaction
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishInteraction( String interactionIdentifier, byte[] tag, HLAfloat64Time time )
	{
		publishInteraction( interactionIdentifier, null, tag, time); 
	}
	
	/**
	 * This method will send out an interaction of the specified type with the given timestamp and tag,
	 * information but with no associated parameters.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 * 
	 * @param handle the handle of the interaction
	 * @param tag the tag of the interaction
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 * 
	 */
	public void publishInteraction( InteractionClassHandle handle, byte[] tag, HLAfloat64Time time )
	{
		publishInteraction( handle, null, tag, time); 
	}
	
	/**
	 * This method will send out an interaction of the specified type, with parameters and a timestamp.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 *
	 * @param interactionIdentifier the identifier of the interaction
	 * @param parameters the parameters of the interaction
	 * @param tag the tag of the interaction
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 */
	public void publishInteraction( String interactionIdentifier, ParameterHandleValueMap parameters,
	                                byte[] tag, HLAfloat64Time time )
	{
		InteractionClassHandle servedHandle = getHandleFromInteractionIdentifier( interactionIdentifier );
		if(servedHandle == null)
		{
			logger.error(String.format( "Failed to publish interaction of type '%s' - unrecognized interaction.", 
			                            interactionIdentifier ));
			return;
		}
		
		publishInteraction( servedHandle, parameters, tag, time );
	}

	/**
	 * This method will send out an interaction of the specified type, with parameters and a timestamp.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 *
	 * @param interactionIdentifier the identifier of the interaction
	 * @param parameters the parameters of the interaction
	 * @param tag the tag of the interaction
	 * @param time the timestamp for the interaction
	 * @throws RTIexception
	 */
	public void publishInteraction( InteractionClassHandle handle, ParameterHandleValueMap parameters,
	                                byte[] tag, HLAfloat64Time time )
	{
		// sanity check the handle for null - without a handle we can't do anything
		if( handle == null )
		{
			// we print a stack trace here so it's a bit more obvious how the situation is arising for 
			// debugging purposes - a null handle for the interaction is unlikely, and probably will
			// be hard to debug if it does.
			StringBuilder errorMsg = new StringBuilder("Failed to publish attribute update - instance handle was null.\n");
			errorMsg.append( makeStackStrace( Thread.currentThread().getStackTrace() ) );
			logger.error( errorMsg.toString() );
			return;
		}
		
		parameters = parameters == null ? makeEmptyParameterMap() : parameters;
		if(parameters == null)
		{
			String interactionIdentifier = getInteractionIdentifierFromHandle( handle );
			logger.error(String.format( "Failed to publish interaction for '%s' with handle %s - parameters were null.", 
			                            interactionIdentifier, handle ));
			return;
		}
		
		// sanity check the tag for null
		tag = tag == null ? new byte[0] : tag;
		
		//////////////////////////
		// send the interaction //
		//////////////////////////
		try
		{
			if(time == null)
				rtiamb.sendInteraction( handle, parameters, tag );
			else
				rtiamb.sendInteraction( handle, parameters, tag, time );
		}
		catch( InteractionClassNotPublished | InteractionParameterNotDefined
			| InteractionClassNotDefined | SaveInProgress | RestoreInProgress
			| FederateNotExecutionMember | NotConnected | RTIinternalError | InvalidLogicalTime e )
		{
			String interactionIdentifier = getInteractionIdentifierFromHandle( handle );
			logger.error(String.format( "Failed to publish interaction of type '%s' with handle %s: %s", 
			                            interactionIdentifier, e.getMessage() ));
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// MISCELLANEOUS ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to collate a stack trace array to text suitable for printing to console or log file
	 * @param stackTrace the stack trace elements
	 * @return text of the stack trace suitable for printing to console or log file
	 */
	private String makeStackStrace(StackTraceElement[] stackTrace)
	{
		if(stackTrace == null)
			return "";
		
		StringBuilder result = new StringBuilder();
		for( int i = 1; i < stackTrace.length; i++ )
		{
			result.append( stackTrace[i].toString() ).append( "\n" );
		}
		return result.toString();
	}
	
	/**
	 * Utility method to obtain the "simple name" for a dot delimited fully qualified name.
	 * 
	 * In practice this simply returns the portion of the fully qualified name after the final
	 * dot.
	 * 
	 * For example, given "some.namespace.here.then.thename", this method will return "thename"
	 * 
	 * @param fullyQualifiedName the fully qualified name
	 * @return the portion of the fully qualified name after the final dot, or the original string
	 *         if there is no dot, or an empty string if the source string is null.
	 */
	private String simpleName(String fullyQualifiedName)
	{
		if( fullyQualifiedName == null )
			return "";

		int lastDot = fullyQualifiedName.lastIndexOf( '.' );

		if( lastDot < 0 )
			return fullyQualifiedName;

		return fullyQualifiedName.substring( lastDot );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

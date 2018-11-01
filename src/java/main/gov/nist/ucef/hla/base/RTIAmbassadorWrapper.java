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
package gov.nist.ucef.hla.base;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleSetFactory;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeHandleValueMapFactory;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.ParameterHandleValueMapFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

/**
 * The purpose of this class is to provide a collection of "safe" and useful ways to interact with the
 * functionality of the RTI Ambassador
 */
public class RTIAmbassadorWrapper
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private static RTIAmbassadorWrapper instance;
	
	private RTIambassador rtiamb;

	private HLAfloat64TimeFactory timeFactory;
	private ParameterHandleValueMapFactory parameterMapFactory;
	private AttributeHandleValueMapFactory attributeMapFactory;
	private AttributeHandleSetFactory attributeHandleSetFactory;

	//----------------------------------------------------------
	//                    STATIC METHODS
	//----------------------------------------------------------
	public static RTIAmbassadorWrapper instance()
	{
		if( instance == null )
		{
			try
			{
				instance =
				    new RTIAmbassadorWrapper( RtiFactoryFactory.getRtiFactory().getRtiAmbassador() );
			}
			catch( Exception e )
			{
				throw new UCEFException( "Failed to initialize RTI ambassador wrapper.", e );
			}
		}
		return instance;
	}
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private RTIAmbassadorWrapper(RTIambassador rtiAmbassador)
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
		catch( Exception e )
		{
			throw new UCEFException( "Failed to initialize RTI ambassador wrapper.", e );
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public RTIambassador getRtiAmbassador()
	{
		return this.rtiamb;
	}
	
	public void connect( FederateAmbassador federateAmbassador )
	{
		try
		{
			this.rtiamb.connect( federateAmbassador, CallbackModel.HLA_EVOKED );
		}
		catch( AlreadyConnected e )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that we are already connected to the
			// federation so we don't need to do it again.
		}
		catch( Exception e )
		{
			throw new UCEFException( "Failed to connect to federation.", e );
		}
	}
	
	public void createFederation( FederateConfiguration configuration )
	{
		String federationName = configuration.getFederationName();
		URL[] modules = configuration.getModules().toArray( new URL[0] );

		try
		{
			// We attempt to create a new federation with the configured FOM modules
			this.rtiamb.createFederationExecution( federationName, modules );
		}
		catch( FederationExecutionAlreadyExists exists )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that the federation was already created
			// by someone else so we don't need to. We can just carry on with our business.
		}
		catch( Exception e )
		{
			// Something else has gone wrong - throw the exception on up
			throw new UCEFException( "Failed to create federation.", e );
		}
	}

	public void joinFederation( FederateConfiguration configuration ) throws UCEFException
	{
		String federationName = configuration.getFederationName();
		String federateName = configuration.getFederateName();
		String federateType = configuration.getFederateType();
		URL[] joinModules = configuration.getJoinModules().toArray( new URL[0] );
		
		try
		{
			// join the federation with the configured join FOM modules
			rtiamb.joinFederationExecution( federateName, federateType, federationName, joinModules );
		}
		catch(Exception e)
		{
			throw new UCEFException( "Failed to join federation execution.", e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// CALLBACKS ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public void evokeCallback(double seconds)
	{
		try
		{
			rtiamb.evokeCallback(seconds);
		}
		catch( Exception e )
		{
			throw new UCEFException( e );
		}
	}
	
	public void evokeMultipleCallbacks(double minimumTime, double maximumTime)
	{
		try
		{
			rtiamb.evokeMultipleCallbacks( minimumTime, maximumTime );
		}
		catch( Exception e )
		{
			throw new UCEFException( e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// SYNC POINTS ///////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public void registerFederationSynchronizationPoint( String label, byte[] tag )
	{
		// Note that if the point already been registered, there will be a callback saying this
		// failed, but as long as *someone* registered it everything is fine
		try
		{
			rtiamb.registerFederationSynchronizationPoint( label, safeByteArray( tag ) );
		}
		catch( Exception e )
		{
			throw new UCEFException(e, "Failed to register federation synchronization point '%s'.", label );
		}
	}

	public void synchronizationPointAchieved( String label )
	{
		try
		{
			rtiamb.synchronizationPointAchieved( label );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to achieve synchronization point '%s'", label );
		}
	}

	public void resignFederationExecution( ResignAction resignAction )
	{
		try
		{
			rtiamb.resignFederationExecution( resignAction );
		}
		catch( Exception e )
		{
			throw new UCEFException( "Failed to resign from federation execution.", e );
		}
	}

	public void destroyFederationExecution(FederateConfiguration configuration) throws RTIexception
	{
		try
		{
			rtiamb.destroyFederationExecution( configuration.getFederateName() );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that the federation was already destroyed
			// by someone else so we don't need to.
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			// if other federates remain we have to leave it for them to clean up
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that other federates remain, so we 
			// have to leave it for them to clean up.
		}
		catch(Exception e)
		{
			throw new UCEFException( "Failed to destroy the federation execution.", e );
		}
	}

	/**
	 * This method will attempt to delete the object instances with the given handles. We can only
	 * delete objects we created, or for which we own the privilegeToDelete attribute.
	 */
	public void deleteObjectInstances( Collection<ObjectInstanceHandle> handles )
	{
		for( ObjectInstanceHandle handle : handles )
		{
			try
			{
				rtiamb.deleteObjectInstance( handle, EMPTY_BYTE_ARRAY );
			}
			catch( DeletePrivilegeNotHeld e )
			{
				// We catch and deliberately ignore this exception - this is not an error 
				// condition as such, it just means that the permission to delete this instance
				// is held by someone else, so we have to let them clean it up.
			}
			catch( Exception e)
			{
				throw new UCEFException(e, "Unable to delete object instance %s", assembleObjectInstanceDetails( handle ));
			}
		}
	}	
	
	/**
	 * This method will attempt to delete the object instance with the given handle. We can only
	 * delete objects we created, or for which we own the privilegeToDelete attribute.
	 */
	public void deleteObjectInstances( ObjectInstanceHandle handle, byte[] tag )
	{
		try
		{
			rtiamb.deleteObjectInstance( handle, safeByteArray( tag ) );
		}
		catch( DeletePrivilegeNotHeld e )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that the permission to delete this instance
			// is held by someone else, so we have to let them clean it up.
		}
		catch( Exception e)
		{
			throw new UCEFException(e, "Unable to delete object instance %s", assembleObjectInstanceDetails( handle ));
		}
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// TIME ///////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public HLAfloat64Time makeHLATime( double time )
	{
		return this.timeFactory.makeTime( time );
	}
	
	public HLAfloat64Interval makeHLAInterval( double interval )
	{
		return timeFactory.makeInterval( interval );		
	}
	
	public void timeAdvanceRequest( double newTime )
	{
		try
		{
			rtiamb.timeAdvanceRequest( makeHLATime( newTime ) );
		}
		catch( Exception e )
		{
			throw new UCEFException( e );
		}
	}
	
	public void enableTimeRegulation( double lookAhead )
	{
		try
		{
			this.rtiamb.enableTimeRegulation( makeHLAInterval( lookAhead ) );
		}
		catch( Exception e )
		{
			throw new UCEFException(e);
		}
	}
	
	public void disableTimeRegulation()
	{
		try
		{
			this.rtiamb.disableTimeRegulation();
		}
		catch( Exception e )
		{
			throw new UCEFException( e );
		}
	}

	public void enableTimeConstrained()
	{
		try
		{
			this.rtiamb.enableTimeConstrained();
		}
		catch( Exception e )
		{
			throw new UCEFException( e );
		}
	}

	public void disableTimeConstrained()
	{
		try
		{
			this.rtiamb.disableTimeConstrained();
		}
		catch( Exception e )
		{
			throw new UCEFException( e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// HANDLE MAPS ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to create an empty parameter handle value map (this will grow as required)
	 * 
	 * @return an empty parameter handle value map
	 */
	public ParameterHandleValueMap makeParameterMap()
	{
		return makeParameterMap(0);
	}
	
	/**
	 * Utility method to create a parameter handle value map with an initial capacity (this will 
	 * grow as required)
	 * 
	 * @param paramCount the initial capacity of the map
	 * @return a parameter handle value map with the specified initial capacity
	 */
	public ParameterHandleValueMap makeParameterMap(int paramCount)
	{
		return this.parameterMapFactory.create( paramCount );
	}

	/**
	 * Utility method to create an empty attribute handle set (this will grow as required)
	 * 
	 * @return an empty attribute handle set
	 */
	public AttributeHandleSet makeAttributeHandleSet()
	{
		return this.attributeHandleSetFactory.create();
	}
	
	/**
	 * Utility method to create an empty attribute handle value map (this will grow as required)
	 * 
	 * @return an empty attribute handle value map
	 */
	public AttributeHandleValueMap makeAttributeMap()
	{
		return makeAttributeMap(0);
	}
	
	/**
	 * Utility method to create a attribute handle value map with an initial capacity (this will 
	 * grow as required)
	 * 
	 * @param paramCount the initial capacity of the map
	 * @return a attribute handle value map with the specified initial capacity
	 */
	public AttributeHandleValueMap makeAttributeMap(int attrCount)
	{
		// create a new map with an initial capacity - this will grow as required
		return attributeMapFactory.create( attrCount );
	}
	
	/**
	 * Utility method to create a attribute handle value map populated with the specified attributes
	 * 
	 * @param objectClassHandle the object class handle associated with the attributes
	 * @param attributes the attribute names to populate the map with
	 * @return a populated attribute handle value map
	 */
	public AttributeHandleValueMap makeAttributeMap( ObjectClassHandle objectClassHandle,
	                                                 Collection<String> attributes )
	{
		// sanity check for null
		attributes = attributes == null ? Collections.emptyList() : attributes;
		
		AttributeHandleValueMap attributeMap = makeAttributeMap( attributes.size() );
		for( String attributeID : attributes )
		{
			setAttribute( objectClassHandle, attributeID, EMPTY_BYTE_ARRAY, attributeMap );
		}
		return attributeMap;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// IDENTIFIER <-> HANDLE LOOKUPS ETC /////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A "safe" way to get an object class name from a handle. If anything goes wrong, exceptions are 
	 * absorbed and a null value is returned
	 * 
	 * @param handle the object class handle to obtain the object class name for
	 * @return the corresponding object class name, or null if anything went wrong
	 */
	public String getObjectClassName( ObjectClassHandle handle )
	{
		try
		{
			return this.rtiamb.getObjectClassName( handle );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * A "safe" way to get an object class handle from a name. If anything goes wrong, exceptions are 
	 * absorbed and a null value is returned
	 * 
	 * @param name the object class name to obtain the object class handle for
	 * @return the corresponding object class handle, or null if anything went wrong
	 */
	public ObjectClassHandle getObjectClassHandle( String name )
	{
		try
		{
			return this.rtiamb.getObjectClassHandle( name );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * A "safe" way to get an object class handle from an object instance handle. If anything goes wrong,
	 * exceptions are absorbed and a null value is returned
	 * 
	 * @param handle the object instance handle to obtain the object class handle for
	 * @return the corresponding object class handle, or null if anything went wrong
	 */
	public ObjectClassHandle getKnownObjectClassHandle( ObjectInstanceHandle handle )
	{
		try
		{
			return rtiamb.getKnownObjectClassHandle( handle );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * A "safe" way to get an attribute name from an object class handle and attribute handle. If 
	 * anything goes wrong, exceptions are absorbed and a null value is returned
	 * 
	 * @param objectClassHandle the object class handle with which the attribute handle is associated
	 * @param attributeHandle the attribute handle to obtain the name for
	 * @return the corresponding attribute name, or null if anything went wrong
	 */
	public String getAttributeName( ObjectClassHandle objectClassHandle,
	                                AttributeHandle attributeHandle )
	{
		try
		{
			return this.rtiamb.getAttributeName( objectClassHandle, attributeHandle );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * A "safe" way to get an attribute handle from an object class handle and attribute name. If 
	 * anything goes wrong, exceptions are absorbed and a null value is returned
	 * 
	 * @param handle the object class handle with which the attribute handle is associated
	 * @param attributeName the attribute name to obtain the handle for
	 * @return the corresponding attribute handle, or null if anything went wrong
	 */
	public AttributeHandle getAttributeHandle( ObjectClassHandle handle, String attributeName )
	{
		try
		{
			return this.rtiamb.getAttributeHandle( handle, attributeName );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * A "safe" way to get an interaction class name from an interaction class handle. If anything goes 
	 * wrong, exceptions are absorbed and a null value is returned
	 * 
	 * @param handle the interaction class handle to obtain the name for
	 * @return the corresponding interaction class name, or null if anything went wrong
	 */
	public String getInteractionClassName( InteractionClassHandle handle )
	{
		try
		{
			return this.rtiamb.getInteractionClassName( handle );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * A "safe" way to get an interaction handle name from an interaction class name. If anything goes 
	 * wrong, exceptions are absorbed and a null value is returned
	 * 
	 * @param name the interaction class name to obtain the handle for
	 * @return the corresponding interaction class handle, or null if anything went wrong
	 */
	public InteractionClassHandle getInteractionClassHandle( String name )
	{
		try
		{
			return this.rtiamb.getInteractionClassHandle( name );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * A "safe" way to get a parameter handle from an interaction class handle and parameter name. If
	 * anything goes wrong, exceptions are absorbed and a null value is returned
	 * 
	 * @param handle the interaction class handle with which the parameter is associated
	 * @param parameterName the parameter name to obtain the handle for
	 * @return the corresponding parameter handle, or null if anything went wrong
	 */
	public ParameterHandle getParameterHandle( InteractionClassHandle handle, String parameterName )
	{
		try
		{
			return this.rtiamb.getParameterHandle( handle, parameterName );
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	/**
	 * A "safe" way to get a parameter name from an interaction class handle and parameter handle. If
	 * anything goes wrong, exceptions are absorbed and a null value is returned
	 * 
	 * @param interactionClassHandle the interaction class handle with which the parameter is associated
	 * @param parameterHandle the parameter handle to obtain the name for
	 * @return the corresponding parameter handle, or null if anything went wrong
	 */
	public String getParameterName( InteractionClassHandle interactionClassHandle,
	                                ParameterHandle parameterHandle )
	{
		try
		{
			return this.rtiamb.getParameterName( interactionClassHandle, parameterHandle );
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	/**
	 * A "safe" way to get an object instance name from an object instance handle. If anything goes
	 * wrong, exceptions are absorbed and a null value is returned
	 * 
	 * @param handle the object instance handle to obtain the name for
	 * @return the corresponding object instance name, or null if anything went wrong
	 */
	public String getObjectInstanceName( ObjectInstanceHandle handle )
	{
		try
		{
			return this.rtiamb.getObjectInstanceName( handle );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * A "safe" way to get an object instance handle from an object instance name. If anything goes
	 * wrong, exceptions are absorbed and a null value is returned
	 * 
	 * @param name the object instance name to obtain the handle for
	 * @return the corresponding object instance handle, or null if anything went wrong
	 */
	public ObjectInstanceHandle getObjectInstanceHandle( String name )
	{
		try
		{
			return this.rtiamb.getObjectInstanceHandle( name );
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// OBJECT REGISTRATION ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will register an instance of the class and will return the federation-wide
	 * unique handle for that instance.
	 */
	public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle handle)
	{
		return registerObjectInstance( handle, null );
	}
	
	/**
	 * This method will register an instance of the class and will return the federation-wide
	 * unique handle for that instance.
	 */
	public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle handle, String instanceIdentifier )
	{
		if( handle == null )
		{
			throw new UCEFException( "NULL object class handle. Cannot register object instance." );
		}
		
		ObjectInstanceHandle instanceHandle = null;
		try
		{
			if( instanceIdentifier == null )
				instanceHandle = rtiamb.registerObjectInstance( handle );
			else
				instanceHandle = rtiamb.registerObjectInstance( handle, instanceIdentifier );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to register object instance for object class %s", 
			                         assembleObjectClassDetails( handle ) );
		}
		return instanceHandle;
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// ATTRIBUTE MANIPULATION //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public AttributeHandleValueMap setAttribute( ObjectClassHandle objectClassHandle,
	                                             String attributeIdentifier,
	                                             byte[] value,
	                                             AttributeHandleValueMap attributes )
	{
		AttributeHandle attrHandle = getAttributeHandle( objectClassHandle, attributeIdentifier );
		if( attrHandle != null )
		{
			attributes.put( attrHandle, safeByteArray( value ) );
		}
		return attributes;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// PUBLISH AND SUBSCRIBE REGISTRATION /////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will inform the RTI about the an interaction that a federate will be publishing to 
	 * the federation.
	 */
	public void publishInteractionClass(InteractionClassHandle handle)
	{
		if( handle == null )
			throw new UCEFException( "NULL interaction class handle. Cannot publish interaction class." );

		try
		{
			rtiamb.publishInteractionClass( handle );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to publish interaction class with handle %s", 
			                         assembleInteractionClassDetails( handle ) );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and to which classes they belong.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values 
	 */
	public void publishObjectClassAttributes(ObjectClassHandle handle, AttributeHandleSet attributes)
	{
		if( handle == null )
			throw new UCEFException( "NULL object class handle. Cannot publish object class atributes." );

		if( attributes == null )
			throw new UCEFException( "NULL attribute handle set. Cannot publish attributes for object class %s.",
			                         assembleObjectClassDetails( handle ) );
			
		try
		{
			rtiamb.publishObjectClassAttributes( handle, attributes );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to publish object class atributes for object class %s.",
			                         assembleObjectClassDetails( handle ) );
		}
	}
	
	/**
	 * This method will inform the RTI about a class of interaction that a federate will subscribe to 
	 * 
	 * @param handle the handle of the interaction class to subscribe to
	 */
	public void subscribeInteractionClass( InteractionClassHandle handle )
	{
		if( handle == null )
			throw new UCEFException( "NULL interaction class handle. Cannot subscribe to interaction class." );

		try
		{
			rtiamb.subscribeInteractionClass( handle );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to subscribe to interaction class %s",
			                         assembleInteractionClassDetails( handle ) );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and which class they are associated with.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values
	 * 
	 * @param handle the handle for the object class whose attributes are to be subscribed to
	 * @param attributes the attribute handles identifying the attributes to be subscribed to
	 */
	public void subscribeObjectClassAttributes(ObjectClassHandle handle, AttributeHandleSet attributes)
	{
		if( handle == null )
			throw new UCEFException( "NULL object class handle. Cannot subscribe to attributes." );

		if( attributes == null )
			throw new UCEFException( "NULL attribute handle set . Cannot subscribe to attributes for object class %s.",
			                         assembleObjectClassDetails( handle ) );

		try
		{
			rtiamb.subscribeObjectClassAttributes( handle, attributes );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to subscribe to object class attributes for object class %s.",
			                         assembleObjectClassDetails( handle ) );
		}
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// PUBLICATION /////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will send out an attribute update for the specified object instance with the given
	 * attributes/values, timestamp and tag information.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will receive
	 * a notification the next time they tick().
	 * 
	 * @param handle the instance handle of the object to which the attributes belong 
	 * @param attributes the attributes and their associated values
	 * @param tag the tag of the interaction - may be null
	 * @param time the timestamp for the interaction - may be null
	 * @throws RTIexception
	 * 
	 */
	public void updateAttributeValues( ObjectInstanceHandle handle,
	                                   AttributeHandleValueMap attributes,
	                                   byte[] tag, HLAfloat64Time time)
	{
		if( handle == null )
			throw new UCEFException( "NULL object instance handle. Cannot update attribute values." );
		
		if( attributes == null )
			throw new UCEFException( "NULL attribute handle value map. Cannot update attributes for object instance %s.",
			                         assembleObjectInstanceDetails( handle ) );
		
		try
		{
			if( time == null )
				rtiamb.updateAttributeValues( handle, attributes, safeByteArray( tag ) );
			else
				rtiamb.updateAttributeValues( handle, attributes, safeByteArray( tag ), time );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to update attribute values for object instance %s",
			                         assembleObjectInstanceDetails( handle ) );
		}
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
	public void sendInteraction( InteractionClassHandle handle, ParameterHandleValueMap parameters,
	                             byte[] tag, HLAfloat64Time time )
	{
		if( handle == null )
			throw new UCEFException( "NULL interaction class handle. Cannot send interaction." );
		
		if( parameters == null )
			throw new UCEFException( "NULL attribute handle value map. Cannot send interaction %s.",
			                         assembleInteractionClassDetails( handle ) );

		// sanity check the tag for null
		try
		{
			if(time == null)
				rtiamb.sendInteraction( handle, parameters, safeByteArray( tag ) );
			else
				rtiamb.sendInteraction( handle, parameters, safeByteArray( tag ), time );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to send interaction %s",
			                         assembleInteractionClassDetails( handle ) );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A utility method to provide an zero-length byte array in place of a null as required
	 * 
	 * @param byteArray the byte array
	 * @return the original byte array if it is not null, or a zero length byte array otherwise
	 */
	private byte[] safeByteArray( byte[] byteArray )
	{
		return byteArray == null ? EMPTY_BYTE_ARRAY : byteArray;
	}
	
	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding object instances
	 * 
	 * @param handle the object instance handle
	 * @return the details of the associated object instance
	 */
	private String assembleObjectInstanceDetails(ObjectInstanceHandle handle)
	{
		String instanceName = getObjectInstanceName( handle );
		ObjectClassHandle classHandle = getKnownObjectClassHandle( handle );
		
		StringBuilder details = new StringBuilder( "'" + (instanceName==null?"NULL":instanceName) + "' " );
		details.append( " (handle '" + (handle==null?"NULL":handle) + "') of object class " );
		details.append( assembleObjectClassDetails( classHandle ) );
		
		return details.toString();
	}

	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding object class handles
	 * 
	 * @param handle the object class handle
	 * @return the details of the associated object class
	 */
	private String assembleObjectClassDetails(ObjectClassHandle handle)
	{
		String className = getObjectClassName( handle );
		
		StringBuilder details = new StringBuilder( "'" + (className==null?"NULL":className) + "'" );
		details.append( " (handle'" + (handle==null?"NULL":handle) + "')" );
		
		return details.toString();
	}
	
	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding interaction class handles
	 * 
	 * @param handle the interaction class handle
	 * @return the details of the associated interaction class
	 */
	private String assembleInteractionClassDetails(InteractionClassHandle handle)
	{
		String className = getInteractionClassName( handle );
		
		StringBuilder details = new StringBuilder( "'" + (className==null?"NULL":className) + "'" );
		details.append( " (handle'" + (handle==null?"NULL":handle) + "')" );
		
		return details.toString();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

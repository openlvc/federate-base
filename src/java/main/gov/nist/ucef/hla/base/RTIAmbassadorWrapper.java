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
import java.util.Map;
import java.util.Map.Entry;

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
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.exceptions.TimeConstrainedIsNotEnabled;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;
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
	private RTIambassador rtiAmbassador;

	private HLAfloat64TimeFactory timeFactory;
	private ParameterHandleValueMapFactory parameterMapFactory;
	private AttributeHandleValueMapFactory attributeMapFactory;
	private AttributeHandleSetFactory attributeHandleSetFactory;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RTIAmbassadorWrapper()
	{
		try
		{
			this.rtiAmbassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
			
			// cache the commonly used factories from the RTI Ambassador
			this.timeFactory = (HLAfloat64TimeFactory)rtiAmbassador.getTimeFactory();
			this.parameterMapFactory = rtiAmbassador.getParameterHandleValueMapFactory();
			this.attributeMapFactory = rtiAmbassador.getAttributeHandleValueMapFactory();
			this.attributeHandleSetFactory = rtiAmbassador.getAttributeHandleSetFactory();
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
		return this.rtiAmbassador;
	}
	
	public void connect( FederateAmbassador federateAmbassador )
	{
		try
		{
			this.rtiAmbassador.connect( federateAmbassador, CallbackModel.HLA_EVOKED );
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
			this.rtiAmbassador.createFederationExecution( federationName, modules );
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

	public void joinFederation( FederateConfiguration configuration )
	{
		String federationName = configuration.getFederationName();
		String federateName = configuration.getFederateName();
		String federateType = configuration.getFederateType();
		URL[] joinModules = configuration.getJoinModules().toArray( new URL[0] );
		
		try
		{
			// join the federation with the configured join FOM modules
			rtiAmbassador.joinFederationExecution( federateName, federateType, federationName, joinModules );
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
			rtiAmbassador.evokeCallback(seconds);
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
			rtiAmbassador.evokeMultipleCallbacks( minimumTime, maximumTime );
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
			rtiAmbassador.registerFederationSynchronizationPoint( label, safeByteArray( tag ) );
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
			rtiAmbassador.synchronizationPointAchieved( label );
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
			rtiAmbassador.resignFederationExecution( resignAction );
		}
		catch( Exception e )
		{
			throw new UCEFException( "Failed to resign from federation execution.", e );
		}
	}

	public void destroyFederationExecution(FederateConfiguration configuration)
	{
		try
		{
			rtiAmbassador.destroyFederationExecution( configuration.getFederateName() );
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
				rtiAmbassador.deleteObjectInstance( handle, EMPTY_BYTE_ARRAY );
			}
			catch( DeletePrivilegeNotHeld e )
			{
				// We catch and deliberately ignore this exception - this is not an error 
				// condition as such, it just means that the permission to delete this instance
				// is held by someone else, so we have to let them clean it up.
			}
			catch( Exception e)
			{
				throw new UCEFException(e, "Unable to delete object instance %s", makeSummary( handle ));
			}
		}
	}	
	
	/**
	 * This method will attempt to delete the object instance with the given handle. We can only
	 * delete objects we created, or for which we own the privilegeToDelete attribute.
	 */
	public void deleteObjectInstance( ObjectInstanceHandle handle, byte[] tag )
	{
		try
		{
			rtiAmbassador.deleteObjectInstance( handle, safeByteArray( tag ) );
		}
		catch( DeletePrivilegeNotHeld e )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that the permission to delete this instance
			// is held by someone else, so we have to let them clean it up.
		}
		catch( Exception e)
		{
			throw new UCEFException(e, "Unable to delete object instance %s", makeSummary( handle ));
		}
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// TIME ///////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public HLAfloat64Time makeHLATime( double time )
	{
		// NOTE: LogicalTime create code is Portico specific. 
		//       You will have to alter this if you move to a different RTI implementation. 
		//       As such, it is isolated into a single method so that any changes required
		//       should be fairly isolated.
		return this.timeFactory.makeTime( time );
	}
	
	public HLAfloat64Interval makeHLAInterval( double interval )
	{
		// NOTE: LogicalTimeInterval create code is Portico specific. 
		//       You will have to alter this if you move to a different RTI implementation. 
		//       As such, it is isolated into a single method so that any changes required
		//       should be fairly isolated.
		return timeFactory.makeInterval( interval );		
	}
	
	public void timeAdvanceRequest( double newTime )
	{
		try
		{
			rtiAmbassador.timeAdvanceRequest( makeHLATime( newTime ) );
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
			this.rtiAmbassador.enableTimeRegulation( makeHLAInterval( lookAhead ) );
		}
		catch( TimeRegulationAlreadyEnabled e )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that time regulation is already enabled
			// so we don't need to do it again.
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
			this.rtiAmbassador.disableTimeRegulation();
		}
		catch( TimeRegulationIsNotEnabled e )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that time regulation is not enabled
			// so we don't need to disable it.
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
			this.rtiAmbassador.enableTimeConstrained();
		}
		catch( TimeConstrainedAlreadyEnabled e )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that time constrained is already enabled
			// so we don't need to do it again.
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
			this.rtiAmbassador.disableTimeConstrained();
		}
		catch( TimeConstrainedIsNotEnabled e )
		{
			// We catch and deliberately ignore this exception - this is not an error 
			// condition as such, it just means that time constrained is not enabled
			// so we don't need to disable it.
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
			return this.rtiAmbassador.getObjectClassName( handle );
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
			return this.rtiAmbassador.getObjectClassHandle( name );
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
			return rtiAmbassador.getKnownObjectClassHandle( handle );
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
			return this.rtiAmbassador.getAttributeName( objectClassHandle, attributeHandle );
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
			return this.rtiAmbassador.getAttributeHandle( handle, attributeName );
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
			return this.rtiAmbassador.getInteractionClassName( handle );
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
			return this.rtiAmbassador.getInteractionClassHandle( name );
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
			return this.rtiAmbassador.getParameterHandle( handle, parameterName );
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
			return this.rtiAmbassador.getParameterName( interactionClassHandle, parameterHandle );
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
			return this.rtiAmbassador.getObjectInstanceName( handle );
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
			return this.rtiAmbassador.getObjectInstanceHandle( name );
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
				instanceHandle = rtiAmbassador.registerObjectInstance( handle );
			else
				instanceHandle = rtiAmbassador.registerObjectInstance( handle, instanceIdentifier );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to register object instance for object class %s", 
			                         makeSummary( handle ) );
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
			rtiAmbassador.publishInteractionClass( handle );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to publish interaction class with handle %s", 
			                         makeSummary( handle ) );
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
			                         makeSummary( handle ) );
			
		try
		{
			rtiAmbassador.publishObjectClassAttributes( handle, attributes );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to publish object class atributes for object class %s.",
			                         makeSummary( handle ) );
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
			rtiAmbassador.subscribeInteractionClass( handle );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to subscribe to interaction class %s",
			                         makeSummary( handle ) );
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
			                         makeSummary( handle ) );

		try
		{
			rtiAmbassador.subscribeObjectClassAttributes( handle, attributes );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to subscribe to object class attributes for object class %s.",
			                         makeSummary( handle ) );
		}
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// PUBLICATION /////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will send out an attribute update for the specified object instance with the
	 * given timestamp and tag information.
	 * 
	 * Federates which are subscribed to a matching combination of class type and attributes will
	 * receive a notification the next time they tick().
	 * 
	 * @param instance the instance
	 * @param tag the tag of the interaction (can be null)
	 * @param time the timestamp for the interaction (can be null)
	 */
	public void updateAttributeValues( HLAObject instance,
	                                   byte[] tag, Double time)
	{
		// basic sanity checks on provided arguments
		if( instance == null )
			throw new UCEFException( "NULL object instance. Cannot update attribute values." );
		
		ObjectInstanceHandle objectInstanceHandle = instance.getInstanceHandle();
		try
		{
			// we need to build up an AttributeHandleValueMap from the object state
			AttributeHandleValueMap ahvm = convert( objectInstanceHandle, instance.getState() );
			
			// now we have the information we need to do the update   
			if( time == null )
				rtiAmbassador.updateAttributeValues( objectInstanceHandle, ahvm, safeByteArray( tag ) );
			else
				rtiAmbassador.updateAttributeValues( objectInstanceHandle, ahvm, 
				                                     safeByteArray( tag ), makeHLATime( time ) );
		}
		catch( Exception e )
		{
			throw new UCEFException( e,
			                         "Failed to update attribute values for object instance %s",
			                         makeSummary( objectInstanceHandle ) );
		}
	}

	/**
	 * This method will send out an interaction with a tag and timestamp.
	 * 
	 * Federates which are subscribed to this type of interaction will receive a notification the next
	 * time they tick().
	 *
	 * @param interaction the interaction
	 * @param tag the tag of the interaction (can be null)
	 * @param time the timestamp for the interaction (can be null)
	 */
	public void sendInteraction( HLAInteraction interaction, byte[] tag, Double time )
	{
		// basic sanity checks on provided arguments
		if( interaction == null )
			throw new UCEFException( "NULL interaction. Cannot send interaction." );
		
		InteractionClassHandle interactionClassHandle = interaction.getInteractionClassHandle();
		try
		{
			// we need to build up a ParameterHandleValueMap from the interaction parameters  
			ParameterHandleValueMap phvm = convert( interactionClassHandle, interaction.getState() );
			
			// now we have the information we need to do the send
			if(time == null)
				rtiAmbassador.sendInteraction( interactionClassHandle, phvm, safeByteArray( tag ) );
			else
				rtiAmbassador.sendInteraction( interactionClassHandle, phvm, 
				                               safeByteArray( tag ), makeHLATime( time ) );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to send interaction %s",
			                         makeSummary( interactionClassHandle ) );
		}
	}
	
	/**
	 * A utility method to encapsulate the code needed to convert a map containing parameter names
	 * and their associated byte values into a populated {@link ParameterHandleValueMap}
	 * 
	 * @param ich the interaction class handle with which the parameters are associated
	 * @param source the map containing parameter names and their associated byte values
	 * @return a populated {@link ParameterHandleValueMap}
	 */
	private ParameterHandleValueMap convert( InteractionClassHandle ich, Map<String,byte[]> source )
	{
		ParameterHandleValueMap result = makeParameterMap( source.size() );
		for( Entry<String,byte[]> entry : source.entrySet() )
		{
			ParameterHandle parameterHandle = getParameterHandle( ich, entry.getKey() );
			if( parameterHandle == null )
				throw new UCEFException( "Unknown parameter '%s'. Create parameter value map.",
				                         entry.getKey() );

			result.put( parameterHandle, entry.getValue() );
		}
		return result;
	}

	/**
	 * A utility method to encapsulate the code needed to convert a map containing attribute names
	 * and their associated byte values into a populated {@link AttributeHandleValueMap}
	 * 
	 * @param oih the object instance handle with which the attributes are associated
	 * @param source the map containing attribute names and their associated byte values
	 * @return a populated {@link AttributeHandleValueMap}
	 */
	private AttributeHandleValueMap convert( ObjectInstanceHandle oih, Map<String,byte[]> source )
	{
		ObjectClassHandle och = getKnownObjectClassHandle( oih );
		AttributeHandleValueMap result = makeAttributeMap( source.size() );
		for( Entry<String,byte[]> entry : source.entrySet() )
		{
			AttributeHandle attributeHandle = getAttributeHandle( och, entry.getKey() );
			if( attributeHandle == null )
				throw new UCEFException( "Unknown attribute '%s'. Cannot create attribute value map.",
				                         entry.getKey() );

			result.put( getAttributeHandle( och, entry.getKey() ), entry.getValue() );
		}
		return result;
	}
	
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
	public String makeSummary(ObjectInstanceHandle handle)
	{
		String instanceName = getObjectInstanceName( handle );
		ObjectClassHandle classHandle = getKnownObjectClassHandle( handle );
		
		StringBuilder details = new StringBuilder( "'" + (instanceName==null?"NULL":instanceName) + "' " );
		details.append( "(handle '" + (handle==null?"NULL":handle) + "') of object class " );
		details.append( makeSummary( classHandle ) );
		
		return details.toString();
	}

	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding object class handles
	 * 
	 * @param handle the object class handle
	 * @return the details of the associated object class
	 */
	public String makeSummary(ObjectClassHandle handle)
	{
		String className = getObjectClassName( handle );
		
		StringBuilder details = new StringBuilder( "'" + (className==null?"NULL":className) + "' " );
		details.append( "(handle '" + (handle==null?"NULL":handle) + "')" );
		
		return details.toString();
	}
	
	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding interaction class handles
	 * 
	 * @param handle the interaction class handle
	 * @return the details of the associated interaction class
	 */
	public String makeSummary(InteractionClassHandle handle)
	{
		String className = getInteractionClassName( handle );
		
		StringBuilder details = new StringBuilder( "'" + (className==null?"NULL":className) + "' " );
		details.append( "(handle '" + (handle==null?"NULL":handle) + "')" );
		
		return details.toString();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

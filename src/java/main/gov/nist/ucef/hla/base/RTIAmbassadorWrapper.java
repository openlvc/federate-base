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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleSetFactory;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeHandleValueMapFactory;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
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
import hla.rti1516e.exceptions.InteractionClassNotPublished;
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
	private static final String NULL_TEXT = "NULL"; 
	
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

	public FederateHandle joinFederation( FederateConfiguration configuration )
	{
		String federationName = configuration.getFederationName();
		String federateName = configuration.getFederateName();
		String federateType = configuration.getFederateType();
		URL[] joinModules = configuration.getJoinModules().toArray( new URL[0] );
		
		try
		{
			// join the federation with the configured join FOM modules
			return rtiAmbassador.joinFederationExecution( federateName, federateType, 
			                                              federationName, joinModules );
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

	public void destroyFederationExecution( FederateConfiguration configuration )
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
	 * This method will attempt to delete the object instance with the given handle. We can only
	 * delete objects we created, or for which we own the privilegeToDelete attribute.
	 * 
	 * @param instance the object instance
	 */
	public void deleteObjectInstance( HLAObject instance )
	{
		deleteObjectInstance( instance, null );
	}

	/**
	 * This method will attempt to delete the object instance. We can only delete objects we
	 * created, or for which we own the privilegeToDelete attribute.
	 * 
	 * @param instance the object instance
	 * @param tag the tag (may be null)
	 * @return the deleted instance
	 */
	public HLAObject deleteObjectInstance( HLAObject instance, byte[] tag )
	{
		if( instance == null )
		{
			throw new UCEFException( "%s object instance. Unable to delete object instance.",
			                         NULL_TEXT );
		}

		deleteObjectInstance( instance.getInstanceHandle(), tag );
		
		return instance;
	}

	/**
	 * This method will attempt to delete the object instance with the given handle. We can only
	 * delete objects we created, or for which we own the privilegeToDelete attribute.
	 * 
	 * @param handle the handle of the object instance
	 * @param tag the tag (may be null)
	 */
	private void deleteObjectInstance( ObjectInstanceHandle handle, byte[] tag )
	{
		if( handle == null )
		{
			throw new UCEFException( "%s object instance handle. Unable to delete object instance.",
			                         NULL_TEXT );
		}
		
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
	 * Utility method to create an empty attribute handle set (this will grow as required)
	 * 
	 * @return an empty attribute handle set
	 */
	public AttributeHandleSet makeAttributeHandleSet()
	{
		return this.attributeHandleSetFactory.create();
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
			throw new UCEFException( e,
			                         "Object class handle '%s' is unknown, " +
			                         "so no object class name could be retrieved.",
			                         handle );
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
			throw new UCEFException( e,
			                         "Object class name '%s' is unknown, " +
			                         "so no object class handle could be retrieved.",
			                         name );
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
			throw new UCEFException( e,
			                         "Object instance handle '%s' is unknown," +
			                         "so no object class handle could be retrieved.",
			                         handle );
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
			throw new UCEFException( e,
			                         "Attribute handle '%s' is unknown for object class %s, " +
			                         "so no attribute class name could be retrieved.",
			                         attributeHandle, makeSummary(objectClassHandle) );
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
			throw new UCEFException( e,
			                         "Attribute name '%s' is unknown for object class %s, " +
			                         "so no attribute handle could be retrieved.",
			                         attributeName, makeSummary(handle) );
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
			throw new UCEFException( e,
			                         "Interaction handle '%s' is unknown, " +
			                         "so no interaction class name could be retrieved.",
			                         handle );
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
			throw new UCEFException( e,
			                         "Interaction name '%s' is unknown, " +
			                         "so no interaction class handle could be retrieved.",
			                         name );
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
			throw new UCEFException( e,
			                         "Parameter name '%s' is unknown for interaction class %s, " +
			                         "so no parameter handle could be retrieved.",
			                         parameterName, makeSummary(handle) );
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
			throw new UCEFException( e,
			                         "Parameter handle '%s' is unknown for interaction class %s, " +
			                         "so no parameter name could be retrieved.",
			                         parameterHandle, makeSummary(interactionClassHandle) );
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
			throw new UCEFException( e,
			                         "Object instance handle '%s' is unknown, " +
			                         "so no object instance name could be retrieved.",
			                         handle );
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
			throw new UCEFException( e,
			                         "Object instance name '%s' is unknown," +
			                         "so no object instance handle could be retrieved.",
			                         name );
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
			throw new UCEFException( "%s object class handle. Cannot register object instance.", 
			                         NULL_TEXT );
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
	//////////////////////// INTERACTION AND OBJECT INSTANCE CREATION //////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A utility method to allow simple instantiation of an no-parameter interaction based off an 
	 * interaction class name
	 * 
	 * @param name the interaction class name
	 * @return the interaction
	 */
	public HLAInteraction makeInteraction( String name )
	{
		return makeInteraction( name, null );
	}
	
	/**
	 * A utility method to allow simple instantiation of an interaction based off an interaction
	 * name and some parameters
	 * 
	 * @param name the interaction class name
	 * @param parameters the parameters (can be null)
	 * @return the interaction
	 */
	public HLAInteraction makeInteraction( String name, Map<String,byte[]> parameters )
	{
		return new HLAInteraction( getInteractionClassHandle( name ), parameters );
	}

	/**
	 * A utility method to allow simple instantiation of an object instance based off an object class 
	 * name and some initial values for the attributes, also registering the instance with the RTI. 
	 * 
	 * @param name the object class name
	 * @return the interaction
	 */
	public HLAObject makeObjectInstance( String className )
	{
		return makeObjectInstance( className, null );
	}
	
	/**
	 * A utility method to allow simple instantiation of an object instance based off an object class 
	 * name and some initial values for the attributes, also registering the instance with the RTI.
	 * 
	 * @param name the object class name
	 * @param initialValues the initial values for the attributes (can be null)
	 * @return the interaction
	 */
	public HLAObject makeObjectInstance( String className, Map<String,byte[]> initialValues )
	{
		ObjectClassHandle classhandle = getObjectClassHandle( className );
		ObjectInstanceHandle instanceHandle = registerObjectInstance( classhandle );
		return new HLAObject( instanceHandle, initialValues );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// PUBLISH AND SUBSCRIBE REGISTRATION /////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * publishing to the federation.
	 */
	public void publishInteractionClasses( Collection<String> classNames)
	{
		if( classNames == null || classNames.isEmpty() )
			return;

		for( String className : classNames )
			publishInteractionClass( className );
	}

	/**
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * publishing to the federation.
	 */
	private void publishInteractionClass( String className )
	{
		publishInteractionClass( getInteractionClassHandle( className ) );
	}
	
	/**
	 * This method will inform the RTI about the an interaction that a federate will be publishing
	 * to the federation.
	 */
	private void publishInteractionClass( InteractionClassHandle handle )
	{
		if( handle == null )
			throw new UCEFException( "%s interaction class handle. Cannot publish interaction class.",
			                         NULL_TEXT );

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
	public void publishObjectClassAttributes( Map<String,Set<String>> publishedAttributes)
	{
		if(publishedAttributes == null || publishedAttributes.isEmpty())
			return;

		for( Entry<String,Set<String>> publication : publishedAttributes.entrySet() )
		{
			String className = publication.getKey();
			ObjectClassHandle classHandle = getObjectClassHandle( className );
			if( classHandle == null )
			{
				throw new UCEFException( "Unknown object class name '%s'. Cannot publish attributes.",
				                         className );
			}
			
			AttributeHandleSet attributeHandleSet = makeAttributeHandleSet();
			for( String attributeName : publication.getValue() )
			{
				AttributeHandle attributeHandle = getAttributeHandle( classHandle, attributeName );
				if( classHandle == null )
				{
					throw new UCEFException( "Unknown attribute name '%s'. Cannot publish attributes " +
											 "for object class %s.",
					                         attributeName , makeSummary( classHandle ) );
				}
				attributeHandleSet.add( attributeHandle );
			}
			
			publishObjectClassAttributes( classHandle, attributeHandleSet );
		}
	}
	
	/**
	 * This method will inform the RTI about the types of attributes the federate will be
	 * publishing to the federation, and to which classes they belong.
	 * 
	 * This needs to be done before registering instances of the object classes and updating their
	 * attributes' values
	 */
	private void publishObjectClassAttributes( ObjectClassHandle handle,
	                                           AttributeHandleSet attributes )
	{
		if( handle == null )
			throw new UCEFException( "%s object class handle. Cannot publish object class atributes.",
			                         NULL_TEXT );

		if( attributes == null )
			throw new UCEFException( "%s attribute handle set. Cannot publish attributes for object " +
			                         "class %s.", NULL_TEXT, makeSummary( handle ) );

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
	 * This method will inform the RTI about the types of interactions that the federate will be
	 * interested in hearing about as other federates produce them.
	 */
	public void subscribeInteractionClasses( Collection<String> interactionClassNames )
	{
		if( interactionClassNames == null || interactionClassNames.isEmpty() )
			return;

		for( String interactionClassName : interactionClassNames )
		{
			subscribeInteractionClass( interactionClassName );
		}
	}
	
	/**
	 * This method will inform the RTI about a class of interaction that a federate will subscribe to 
	 * 
	 * @param className the name of the interaction class to subscribe to
	 */
	private void subscribeInteractionClass( String className )
	{
		InteractionClassHandle handle = getInteractionClassHandle( className );

		if( handle == null )
		{
			throw new UCEFException( "Cannot subscribe to interaction class using unknown class " +
									 "name '%s'.",
			                         className );
		}

		subscribeInteractionClass( handle );
	}
	
	/**
	 * This method will inform the RTI about a class of interaction that a federate will subscribe to 
	 * 
	 * @param handle the handle of the interaction class to subscribe to
	 */
	private void subscribeInteractionClass( InteractionClassHandle handle )
	{
		if( handle == null )
			throw new UCEFException( "%s interaction class handle. Cannot subscribe to " +
									 "interaction class.", NULL_TEXT );

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
	 * interested in hearing about, and to which classes they belong.
	 * 
	 * We need to subscribe to hear about information on attributes of classes created and altered
	 * in other federates
	 * 
	 * @param subscribedAttributes a map which connects object class names to sets of attribute
	 *            names to be subscribed to
	 */
	public void subscribeObjectClassesAttributes( Map<String,Set<String>> subscribedAttributes )
	{
		if( subscribedAttributes == null || subscribedAttributes.isEmpty() )
			return;

		for( Entry<String,Set<String>> subscription : subscribedAttributes.entrySet() )
		{
			// get all the handle information for the attributes of the current class
			String className = subscription.getKey();
			ObjectClassHandle classHandle = getObjectClassHandle( className );
			if( classHandle == null )
			{
				throw new UCEFException( "Unknown object class name '%s'. Cannot subscribe to attributes.",
				                         className );
			}
			// package the information into the handle set
			AttributeHandleSet attributeHandleSet = makeAttributeHandleSet();
			for( String attributeName : subscription.getValue() )
			{
				AttributeHandle attributeHandle = getAttributeHandle( classHandle, attributeName );
				if( attributeHandle == null )
				{
					throw new UCEFException( "Unknown attribute name '%s'. Cannot subscribe to " +
					                         "attributes for object class %s.", attributeName,
					                         makeSummary( classHandle ) );
				}
				attributeHandleSet.add( attributeHandle );
			}

			// do the actual subscription
			subscribeObjectClassAttributes( classHandle, attributeHandleSet );
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
	private void subscribeObjectClassAttributes( ObjectClassHandle handle,
	                                             AttributeHandleSet attributes )
	{
		if( handle == null )
			throw new UCEFException( "%s object class handle. Cannot subscribe to attributes.",
			                         NULL_TEXT );

		if( attributes == null )
			throw new UCEFException( "%s attribute handle set . Cannot subscribe to attributes for " +
			                         "object class %s.", NULL_TEXT, makeSummary( handle ) );

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
	public void updateAttributeValues( HLAObject instance, byte[] tag, Double time)
	{
		// basic sanity checks on provided arguments
		if( instance == null )
			throw new UCEFException( "%s object instance. Cannot update attribute values.",
			                         NULL_TEXT );
		
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
			throw new UCEFException( "%s interaction. Cannot send interaction.",
			                         NULL_TEXT );
		
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
		catch( InteractionClassNotPublished e )
		{
			throw new UCEFException( e, "Failed to send interaction %s because it has not been published.",
			                         makeSummary( interactionClassHandle ) );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Failed to send interaction %s",
			                         makeSummary( interactionClassHandle ) );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Utility Methods ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A utility method to convert a map containing parameter names and their associated byte
	 * values into a populated {@link ParameterHandleValueMap}
	 * 
	 * @param ich the interaction class handle with which the parameters are associated
	 * @param source the map containing parameter names and their associated byte values
	 * @return a populated {@link ParameterHandleValueMap}
	 */
	protected ParameterHandleValueMap convert( InteractionClassHandle ich, Map<String,byte[]> source )
	{
		ParameterHandleValueMap result = parameterMapFactory.create( source.size() );
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
	 * A utility method to convert a map containing attribute names and their associated byte
	 * values into a populated {@link AttributeHandleValueMap}
	 * 
	 * @param oih the object instance handle with which the attributes are associated
	 * @param source the map containing attribute names and their associated byte values
	 * @return a populated {@link AttributeHandleValueMap}
	 */
	protected AttributeHandleValueMap convert( ObjectInstanceHandle oih, Map<String,byte[]> source )
	{
		ObjectClassHandle och = getKnownObjectClassHandle( oih );
		AttributeHandleValueMap result = attributeMapFactory.create( source.size() );
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
	 * A utility method to encapsulate the code needed to convert a
	 * {@link AttributeHandleValueMap} into a populated map containing attribute names and their
	 * associated byte values
	 * 
	 * @param oih the object instance handle with which the attributes are associated
	 * @param source the map containing attribute names and their associated byte values
	 * @return a populated {@link Map}
	 */
	protected Map<String,byte[]> convert( ObjectInstanceHandle oih, AttributeHandleValueMap phvm )
	{
		ObjectClassHandle och = getKnownObjectClassHandle( oih );
		HashMap<String,byte[]> result = new HashMap<>();
		for( Entry<AttributeHandle,byte[]> entry : phvm.entrySet() )
		{
			String name = getAttributeName( och, entry.getKey() );
			result.put( name, entry.getValue() );
		}
		return result;
	}

	/**
	 * A utility method to encapsulate the code needed to convert a
	 * {@link ParameterHandleValueMap} into a populated map containing parameter names and their
	 * associated byte values
	 * 
	 * @param ich the interaction class handle with which the parameters are associated
	 * @param source the map containing parameter names and their associated byte values
	 * @return a populated {@link Map}
	 */
	protected Map<String,byte[]> convert( InteractionClassHandle ich, ParameterHandleValueMap phvm )
	{
		HashMap<String,byte[]> result = new HashMap<>();
		for( Entry<ParameterHandle,byte[]> entry : phvm.entrySet() )
		{
			String name = getParameterName( ich, entry.getKey() );
			result.put( name, entry.getValue() );
		}
		return result;
	}
	
	/**
	 * A utility method to provide an zero-length byte array in place of a null as required
	 * 
	 * @param byteArray the byte array
	 * @return the original byte array if it is not null, or a zero length byte array otherwise
	 */
	public byte[] safeByteArray( byte[] byteArray )
	{
		return byteArray == null ? EMPTY_BYTE_ARRAY : byteArray;
	}
	
	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding object instances
	 * 
	 * @param instance the object
	 * @return the details of the object
	 */
	public String makeSummary( HLAObject instance )
	{
		return makeSummary( instance.getInstanceHandle() );
	}
	
	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding object instances
	 * 
	 * @param handle the object instance handle
	 * @return the details of the associated object instance
	 */
	private String makeSummary( ObjectInstanceHandle handle )
	{
		String instanceName = NULL_TEXT;
		ObjectClassHandle classHandle = null;

		try
		{
			instanceName = getObjectInstanceName( handle );
		}
		catch( Exception e )
		{
			// ignore - null is OK as a result here
		}

		try
		{
			classHandle = getKnownObjectClassHandle( handle );
		}
		catch( Exception e )
		{
			// ignore - null is OK as a result here
		}

		StringBuilder details =
		    new StringBuilder( "'" + (instanceName == null ? NULL_TEXT : instanceName) + "' " );
		details.append(
		                "(handle '" + (handle == null ? NULL_TEXT : handle) +
		                "') of object class " );
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
	private String makeSummary( ObjectClassHandle handle )
	{
		String className = NULL_TEXT;
		try
		{
			className = getObjectClassName( handle );
		}
		catch( Exception e )
		{
			// ignore - null is OK as a result here
		}

		StringBuilder details =
		    new StringBuilder( "'" + (className == null ? NULL_TEXT : className) + "' " );
		details.append( "(handle '" + (handle == null ? NULL_TEXT : handle) + "')" );

		return details.toString();
	}

	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding interactions
	 * 
	 * @param interaction the interaction
	 * @return the details of the interaction
	 */
	public String makeSummary( HLAInteraction interaction )
	{
		return makeSummary( interaction.getInteractionClassHandle() );
	}
	
	/**
	 * A utility method purely for the purpose of constructing meaningful text for error messages
	 * regarding interaction class handles
	 * 
	 * @param handle the interaction class handle
	 * @return the details of the associated interaction class
	 */
	private String makeSummary( InteractionClassHandle handle )
	{
		String className = NULL_TEXT;
		try
		{
			className = getInteractionClassName( handle );
		}
		catch( Exception e )
		{
			// ignore - null is OK as a result here
		}

		StringBuilder details =
		    new StringBuilder( "'" + (className == null ? NULL_TEXT : className) + "' " );
		details.append( "(handle '" + (handle == null ? NULL_TEXT : handle) + "')" );

		return details.toString();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

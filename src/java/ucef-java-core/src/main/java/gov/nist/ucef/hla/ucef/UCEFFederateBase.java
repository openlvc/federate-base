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
package gov.nist.ucef.hla.ucef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.Types.DataType;
import gov.nist.ucef.hla.base.Types.InteractionClass;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.ucef.interactions.SimEnd;
import gov.nist.ucef.hla.ucef.interactions.SimPause;
import gov.nist.ucef.hla.ucef.interactions.SimResume;
import gov.nist.ucef.hla.ucef.interactions.SimStart;
import hla.rti1516e.InteractionClassHandle;


/**
 * An abstract implementation for a UCEF Federate which is aware of certain UCEF specific
 * simulation control interactions.
 *
 * It provides default handlers for them, but more notably provides a {@link #federateExecution()}
 * implementation which is aware of the receipt of {@link SimEnd} simulation control interactions.
 *
 * It terminates the simulation loop when...
 * <ul>
 * <li>a {@link SimEnd} is received, or...</li>
 * <li>when the {@link #step(double)} method returns false</li>
 * </ul>
 * ... whichever comes first.
 */
public abstract class UCEFFederateBase extends FederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getLogger( UCEFFederateBase.class );

	// OMNeT++ specific fedconfig parameter keys
	private static final String KEY_OMNET_INTERACTION_FILTERS = "omnetInteractions";
	private static final String KEY_NETWORK_INTERACTION_NAME = "networkInteractionName";
	private static final String KEY_NETWORK_OBJECT_NAME = "networkObjectName";
	// This key represents the host to inject the network msg
	private static final String KEY_SRC_HOST = "sourceHost";
	// Params in network interaction designated to the OMNeT++ federate
	// This key represents the name of the class wrapped by this interaction
	private static final String KEY_ORG_CLASS = "wrappedClassName";
	// This key represents the payload of the wrapped class
	private static final String KEY_NET_DATA = "data";
	// default interaction class name to treat as am OMNeT++ network interaction
	private static final String DEFAULT_NETWORK_INTERACTION_NAME = "HLAinteractionRoot.NetworkInteraction";
	private static final String DEFAULT_NETWORK_OBJECT_NAME = "HLAobjectRoot.NetworkObject";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private final Object mutex_lock = new Object();

	protected Set<String> syncPointTimeouts;

	// the namespaced "network interaction" and "network object" class names
	protected String networkInteractionName;
	protected String networkObjectName;
	// mapped host name
	protected String srcHost;
	// pattern matchers for identifying OMNeT++ interactions
	protected Collection<Pattern> omnetInteractionMatchers;

	// flag which becomes true after a SimStart interaction has
	// been received (begins as false)
	protected volatile boolean simShouldStart;
	// flag which becomes true after a SimEnd interaction has
	// been received (begins as false)
	protected volatile boolean simShouldEnd;
	// flag which becomes true after a SimPause interaction has
	// been received, and false after a SimResume interaction
	// has been received (begins as false)
	protected volatile boolean simShouldPause;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UCEFFederateBase()
	{
		super();

		this.syncPointTimeouts = new HashSet<>();

		this.networkInteractionName = DEFAULT_NETWORK_INTERACTION_NAME;
		this.networkObjectName = DEFAULT_NETWORK_OBJECT_NAME;
		this.srcHost = this.configuration.getFederateName();
		this.omnetInteractionMatchers = new ArrayList<>();

		this.simShouldStart = false;
		this.simShouldEnd = false;
		this.simShouldPause = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Override so that we can perform {@link UCEFFederateBase} specific configuration from the
	 * JSON once the {@link FederateBase} is finished
	 */
	@Override
	public JSONObject configureFromJSON( String jsonSource )
	{
		// call super method first...
		JSONObject json = super.configureFromJSON( jsonSource );
		// ...then custom configuration:
		this.configureUcefFederateBaseFromJSON( json );
		// return the JSON object (potentially for others to use)
		return json;
	}

	@Override
	public void sendInteraction( HLAInteraction interaction )
	{
		if( isOmnetNetworkInteraction( interaction ) )
		{
			// convert OMNeT++ routed interactions and re-send
			this.rtiamb.sendInteraction( makeOmnetInteraction( interaction ), null, null );
		}
		else
		{
			// non-OMNeT++ routed interactions get handled as usual
			super.sendInteraction( interaction );
		}
	}

	@Override
	protected void sendInteraction( HLAInteraction interaction, byte[] tag )
	{
		if( isOmnetNetworkInteraction( interaction ) )
		{
			// convert OMNeT++ routed interactions and re-send
			this.rtiamb.sendInteraction( makeOmnetInteraction( interaction ), tag, null );
		}
		else
		{
			// non-OMNeT++ routed interactions get handled as usual
			super.sendInteraction( interaction, tag );
		}
	}

	@Override
	protected void sendInteraction( HLAInteraction interaction, byte[] tag, double time )
	{
		if( isOmnetNetworkInteraction( interaction ) )
		{
			// convert OMNeT++ routed interactions and re-send
			this.rtiamb.sendInteraction( makeOmnetInteraction( interaction ), tag, time );
		}
		else
		{
			// non-OMNeT++ routed interactions get handled as usual
			super.sendInteraction( interaction, tag, time );
		}
	}

	//----------------------------------------------------------
	//         UCEF SPECIFIC INTERACTION CALLBACK METHODS
	//----------------------------------------------------------
	/**
	 * Called whenever the UCEF specific "simulation start" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simStart the {@link SimStart} interaction
	 */
	protected abstract void receiveSimStart( SimStart simStart );

	/**
	 * Called whenever the UCEF specific "simulation start" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simStart the {@link SimStart} interaction
	 * @param time the current logical time of the federate
	 */
	protected abstract void receiveSimStart( SimStart simStart, double time );

	/**
	 * Called whenever the UCEF specific "simulation end" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simEnd the {@link SimEnd} interaction
	 */
	protected abstract void receiveSimEnd( SimEnd simEnd );

	/**
	 * Called whenever the UCEF specific "simulation end" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simEnd the {@link SimEnd} interaction
	 * @param time the current logical time of the federate
	 */
	protected abstract void receiveSimEnd( SimEnd simEnd, double time );

	/**
	 * Called whenever the UCEF specific "simulation pause" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simPause the {@link SimPause} interaction
	 */
	protected abstract void receiveSimPause( SimPause simPause );

	/**
	 * Called whenever the UCEF specific "simulation pause" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simPause the {@link SimPause} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected abstract void receiveSimPause( SimPause simPause, double time );

	/**
	 * Called whenever the UCEF specific "simulation resume" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simResume the {@link SimResume} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected abstract void receiveSimResume( SimResume simResume );

	/**
	 * Called whenever the UCEF specific "simulation resume" interaction is received
	 *
	 * NOTE: this method can be overridden to provide handling suitable for a
	 *       specific federate's requirements
	 *
	 * @param simResume the {@link SimResume} interaction
	 * @param federateTime the current logical time of the federate
	 */
	protected abstract void receiveSimResume( SimResume simResume, double time );

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * We override the this method here so that we can react to
	 * the arrival of a {@link SimEnd} interaction by terminating
	 * the simulation loop
	 *
	 * Apart from this difference, {@link #federateExecution()} is
	 * identical to the {@link FederateBase#federateExecution()}
	 * implementation.
	 */
	@Override
	protected void federateExecution()
	{
		while( this.simShouldEnd == false )
		{
			// next step, and cease simulation loop if step() returns false
			if( this.simShouldEnd || step( this.fedamb.getFederateTime() ) == false )
				break;
			if( this.simShouldEnd == false)
				advanceTime();
		}
	}

	/**
	 * We override the this method here so that late joining UCEF federates
	 * can wait for a synchronization point, but time out so they don't lock
	 * up indefinitely (because the federation has already announced and
	 * achieved the synchronization point in the past)
	 *
	 * @param label the synchronization point label
	 */
	@Override
	protected void waitForSyncPointAchievement( String label )
	{
		// TODO -------------------------------------------------------
		// NOTE: This is placeholder code until the Portico updates
		//       are finalized to support querying of synchronization
		//       labels and statuses which will allow this to be
		//       handled far more effectively.

		//       It's possible that the timeout may be kept (in
		//       addition to the mechanism of directly querying
		//       synchronization point status), but as it creates the
		//       unfortunate side effect of allowing a federate to
		//       potentially "jump ahead" due to an extended (but
		//       legitimate) delay in a synch point being achieved,
		//       it's likely that it will be removed entirely.
		// NOTE: if we keep this timeout, obtaining a timeout value
		//       from the the federate configuration (including
		//       "infinite"/wait forever option) would be desirable.
		// TODO -------------------------------------------------------
		long timeoutDuration = 15000;
		long timeoutTime = System.currentTimeMillis() + timeoutDuration;
		boolean hasTimedOut = false;

		while( !this.fedamb.isAchieved( label ) && !hasTimedOut )
		{
			evokeMultipleCallbacks();
			hasTimedOut = System.currentTimeMillis() > timeoutTime;
		}

		if( hasTimedOut )
		{
			this.syncPointTimeouts.add( label );

			logger.warn( String.format( "Timed out after %.3f seconds while waiting to achieve "+
										"synchronization point '%s'",
										(timeoutDuration / 1000.0),
										label ) );
		}
	}

	/**
	 * Override to provide handling for specific UCEF simulation control interaction types
	 * and OMNeT++ destination federate filtering
	 */
	@Override
	public void incomingInteraction( InteractionClassHandle handle, Map<String,byte[]> parameters )
	{
		synchronized( this.mutex_lock )
		{
			HLAInteraction interaction = makeInteraction( handle, parameters );
			if( interaction != null )
			{
				if( isSimulationControlInteraction( interaction ) )
				{
					// simulation control interactions require their own handling
					processSimControlInteraction( interaction, null );
				}
				else if( shouldReceiveInteraction( interaction ) )
				{
					receiveInteraction( interaction );
				}
			}
		}
	}

	/**
	 * Override to provide handling for specific UCEF simulation control interaction types
	 * and OMNeT++ destination federate filtering
	 */
	@Override
	public void incomingInteraction( InteractionClassHandle handle,
	                                 Map<String,byte[]> parameters,
	                                 double time )
	{
		synchronized( this.mutex_lock )
		{
			// delegate to handlers for UCEF Simulation control interactions as required
			HLAInteraction interaction = makeInteraction( handle, parameters );

			if( interaction != null )
			{
				if( isSimulationControlInteraction( interaction ) )
				{
					// simulation control interactions require their own handling
					processSimControlInteraction( interaction, time );
				}
				else if( shouldReceiveInteraction( interaction ) )
				{
					receiveInteraction( interaction, time );
				}
			}
		}
	}

	/**
	 * General handler for received simulation control interactions
	 * ({@link SimStart},{@link SimEnd}, {@link SimPause}, {@link SimResume})
	 *
	 * @param interaction the simulation control interaction
	 * @param time the logical time (may be null)
	 */
	private void processSimControlInteraction( HLAInteraction interaction, Double time )
	{
		String interactionClassName = interaction.getInteractionClassName();

		// delegate to handlers for UCEF Simulation control interactions as required
		if( SimStart.interactionName().equals( interactionClassName ) )
		{
			// it is up to individual federates as to how they handle this
			this.simShouldStart = true;
			if( time == null )
				receiveSimStart( new SimStart( interaction ) );
			else
				receiveSimStart( new SimStart( interaction ), time );
		}
		else if( SimEnd.interactionName().equals( interactionClassName ) )
		{
			// if a SimEnd is received, a well behaved UCEF federate must
			// synchronize with the rest of the federation before resigning
			this.configuration.setSyncBeforeResign( true );

			this.simShouldEnd = true;
			if( time == null )
				receiveSimEnd( new SimEnd( interaction ) );
			else
				receiveSimEnd( new SimEnd( interaction ), time );
		}
		else if( SimPause.interactionName().equals( interactionClassName ) )
		{
			// if a SimPause is received, a well behaved UCEF federate must
			// cease its step() loop processing until a SimResume or
			// SimEnd is received
			this.simShouldPause = true;
			if( time == null )
				receiveSimPause( new SimPause( interaction ) );
			else
				receiveSimPause( new SimPause( interaction ), time );
		}
		else if( SimResume.interactionName().equals( interactionClassName ) )
		{
			// if a SimResume is received, a well behaved UCEF federate may
			// resume its step() loop processing
			this.simShouldPause = false;
			if( time == null )
				receiveSimResume( new SimResume( interaction ) );
			else
				receiveSimResume( new SimResume( interaction ), time );
		}
	}

	/**
	 * Determine if an incoming interaction should be received by us.
	 *
	 * This is determined by using encapsulated "federateFilter" parameter. This contains comma
	 * separated matching strings which are checked against our federate name.
	 *
	 * If any of the matching strings match our federate name, we are supposed to receive the
	 * interaction, otherwise we are supposed to ignore it.
	 *
	 * If the "federateFilter" parameter is absent, normal handling takes effect (i.e., we receive
	 * the interaction as normal).
	 *
	 * @param interaction the simulation control interaction
	 * @param time the logical time (may be null)
	 */
	private boolean shouldReceiveInteraction( HLAInteraction interaction )
	{
		boolean shouldReceive = true;
		if( interaction.isPresent( "federateFilter" ) )
		{
			// The interaction has a "federateFilter" set, test if the
			// interaction is supposed to be for this federate
			// split by commas
			String[] fedFilters = interaction.getAsString( "federateFilter" ).split( "," );
			// convert to regular expression patterns
			Collection<Pattern> dstFeds = stringsToRegexPatterns( Arrays.asList( fedFilters ) );
			// check for any matches with our federate name
			shouldReceive = matchesAnyPattern( this.configuration.getFederateName(), dstFeds );

			if( logger.isDebugEnabled() )
			{
				// the filter says we can handle this interaction
				logger.debug( "{} '{}' interaction as it is {}designated for " +
				              "this federate ('{}').",
				              shouldReceive ? "Handling" : "Ignoring",
				              shouldReceive ? "" : "not",
				              interaction.getInteractionClassName(),
				              this.configuration.getFederateName() );
			}
		}
		else if( logger.isDebugEnabled() )
		{
			// If the interaction doesn't have a destination filter
			// generic interaction receipt handling takes effect
			logger.debug( "Received '{}' interaction with no designated federate. Handling as usual.",
			              interaction.getInteractionClassName(),
			              this.configuration.getFederateName() );
		}
		return shouldReceive;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Determine if this federate is a "late joiner".
	 *
	 * If a federate joins after the READY_TO_RUN sync point it is a "late joiner" and does
	 * not need to wait for the SimStart to enter the simulation loop (because the rest of
	 * the simulation has already started).
	 *
	 * @return true if the federate is a late joiner, false otherwise
	 */
	protected boolean isLateJoiner()
	{
		// TODO NOTE See other notes in this file regarding the substitution of a timeout
		// mechanism with MOM interactions to determine sync point statuses
		return this.syncPointTimeouts.contains( UCEFSyncPoint.READY_TO_RUN.getLabel() );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Set up {@link UCEFFederateBase} specific configuration items from JSON
	 *
	 * Called from {@link #configureFromJSON(JSONObject)} and {@link #configureFromJSON(String)}
	 *
	 * @param configData the {@link JSONObject} containing the configuration data
	 * @return the original {@link JSONObject} containing the configuration data
	 */
	private JSONObject configureUcefFederateBaseFromJSON( JSONObject configData )
	{
		if( configData == null )
		{
			logger.warn( "JSON configuration data was null!" );
			return configData;
		}

		this.networkInteractionName = this.configuration.jsonStringOrDefault( configData,
		                                                                 KEY_NETWORK_INTERACTION_NAME,
		                                                                 DEFAULT_NETWORK_INTERACTION_NAME );
		this.networkObjectName = this.configuration.jsonStringOrDefault( configData,
		                                                                 KEY_NETWORK_OBJECT_NAME,
		                                                                 DEFAULT_NETWORK_OBJECT_NAME );
		this.srcHost = this.configuration.jsonStringOrDefault( configData,
		                                                  KEY_SRC_HOST,
		                                                  this.configuration.getFederateName() );
		Set<String> omnetInteractionFilters = this.configuration.jsonStringSetOrDefault( configData,
		                                                                            KEY_OMNET_INTERACTION_FILTERS,
		                                                                            Collections.emptySet() );
		this.omnetInteractionMatchers = stringsToRegexPatterns( omnetInteractionFilters );

		return configData;
	}

	/**
	 * Utility method to determine if an {@link InteractionClass} corresponds to one of the
	 * simulation control interactions
	 *
	 * @param interactionClass {@link InteractionClass} the instance to check
	 * @return true if the interaction is one of the simulation control interactions, false
	 *         otherwise
	 */
	private boolean isSimulationControlInteraction( HLAInteraction instance )
	{
		if( instance == null )
			return false;

		String className = instance.getInteractionClassName();
		return SimStart.interactionName().equals( className ) ||
		       SimEnd.interactionName().equals( className ) ||
		       SimPause.interactionName().equals( className ) ||
		       SimResume.interactionName().equals( className );
	}

	/**
	 * Utility method to determine if an HLA interaction is a OMNeT++ network interaction
	 *
	 * @param interaction {@link HLAInteraction} the interaction to check
	 * @return true if the interaction is an OMNeT++ network interaction, false otherwise
	 */
	private boolean isOmnetNetworkInteraction( HLAInteraction interaction )
	{
		return matchesAnyPattern( interaction.getInteractionClassName(),
		                          this.omnetInteractionMatchers );
	}

	/**
	 * Utility method which creates an "OMNeT++ interaction" from an {@link HLAInteraction}
	 * instance.
	 *
	 * An OMNeT++ interaction is actually just an {@link HLAInteraction} which has the following
	 * parameters:
	 * <ul>
	 * <li>{@link UCEFFederateBase#KEY_ORG_CLASS}: the interaction class name of the original
	 * {@link HLAInteraction}</li>
	 * <li>{@link UCEFFederateBase#KEY_SRC_HOST}: the host identifier of this federate</li>
	 * <li>{@link UCEFFederateBase#KEY_NET_DATA}: parameters and values of the original
	 * {@link HLAInteraction} encoded as a JSON string. Only those which are initialized are
	 * encoded, and any "unset" parameters are skipped.</li>
	 * </ul>
	 *
	 * @param interaction the {@link HLAInteraction} to create the OMNeT++ interaction from
	 * @return the {@link HLAInteraction} to use for the OMNeT++ interaction
	 */
	private HLAInteraction makeOmnetInteraction( HLAInteraction interaction )
	{
		HLAInteraction omnetInteraction = makeInteraction( this.networkInteractionName );
		omnetInteraction.setValue( KEY_ORG_CLASS, interaction.getInteractionClassName() );
		omnetInteraction.setValue( KEY_SRC_HOST, this.srcHost );
		omnetInteraction.setValue( KEY_NET_DATA, encodeAsJson( interaction ) );
		return omnetInteraction;
	}

	/**
	 * Utility method which creates an "OMNeT++ object" from an {@link HLAObject}
	 * instance.
	 *
	 * An OMNeT++ object is actually just an {@link HLAObject} which has the following
	 * attributes:
	 * <ul>
	 * <li>{@link UCEFFederateBase#KEY_ORG_CLASS}: the interaction class name of the original
	 * {@link HLAObject}</li>
	 * <li>{@link UCEFFederateBase#KEY_SRC_HOST}: the host identifier of this federate</li>
	 * <li>{@link UCEFFederateBase#KEY_NET_DATA}: attributes and values of the original
	 * {@link HLAObject} encoded as a JSON string. Only those which are initialized are
	 * encoded, and any "unset" attributes are skipped.</li>
	 * </ul>
	 *
	 * @param instance the {@link HLAObject} to create the OMNeT++ interaction from
	 * @return the {@link HLAObject} to use for the OMNeT++ object
	 */
	private HLAObject makeOmnetObject( HLAObject instance )
	{
		HLAObject omnetInteraction = makeObjectInstance( this.networkObjectName );
		omnetInteraction.setValue( KEY_ORG_CLASS, instance.getObjectClassName() );
		omnetInteraction.setValue( KEY_SRC_HOST, this.srcHost );
		omnetInteraction.setValue( KEY_NET_DATA, encodeAsJson( instance ) );
		return omnetInteraction;
	}

	/**
	 * Utility method to encode the parameters of an {@link HLA interaction} instance as a JSON
	 * formatted string.
	 *
	 * Parameters which are as yet unintitialized are not included in the encoded JSON.
	 *
	 * @param interaction {@link HLAInteraction} the interaction to encode
	 * @return the encoded parameters as a JSON formatted string
	 */
	@SuppressWarnings("unchecked")
	private String encodeAsJson( HLAInteraction instance )
	{
		JSONObject json = new JSONObject();
		// Get parameters of this interaction
		Set<String> paramKeys = this.configuration.getParameterNames( instance );
		for( String paramKey : paramKeys )
		{
			if( !instance.isPresent( paramKey ) )
				continue;

			// Figure out the data type of the parameter
			DataType dataType = this.configuration.getDataType( instance, paramKey );

			// Now add param values to JSON object
			switch( dataType )
			{
				case BOOLEAN:
					json.put( paramKey, instance.getAsBoolean( paramKey ) );
					break;
				case BYTE:
					json.put( paramKey, instance.getAsByte( paramKey ) );
					break;
				case CHAR:
					json.put( paramKey, instance.getAsChar( paramKey ) );
					break;
				case DOUBLE:
					json.put( paramKey, instance.getAsDouble( paramKey ) );
					break;
				case FLOAT:
					json.put( paramKey, instance.getAsFloat( paramKey ) );
					break;
				case INT:
					json.put( paramKey, instance.getAsInt( paramKey ) );
					break;
				case LONG:
					json.put( paramKey, instance.getAsLong( paramKey ) );
					break;
				case SHORT:
					json.put( paramKey, instance.getAsShort( paramKey ) );
					break;
				case STRING:
					json.put( paramKey, instance.getAsString( paramKey ) );
					break;
				case UNKNOWN:
				default:
					break;
			}
		}
		return json.toJSONString();
	}

	/**
	 * Utility method to encode the attributes of an {@link HLAObject} instance as a JSON
	 * formatted string.
	 *
	 * Attributes which are as yet unintitialized are not included in the encoded JSON.
	 *
	 * @param interaction {@link HLAObject} the instance to encode
	 * @return the encoded parameters as a JSON formatted string
	 */
	@SuppressWarnings("unchecked")
	private String encodeAsJson( HLAObject instance )
	{
		JSONObject json = new JSONObject();
		// Get parameters of this interaction
		Set<String> paramKeys = this.configuration.getAttributeNames( instance );
		for( String paramKey : paramKeys )
		{
			if( !instance.isPresent( paramKey ) )
				continue;

			// Figure out the data type of the parameter
			DataType dataType = this.configuration.getDataType( instance, paramKey );

			// Now add param values to JSON object
			switch( dataType )
			{
				case BOOLEAN:
					json.put( paramKey, instance.getAsBoolean( paramKey ) );
					break;
				case BYTE:
					json.put( paramKey, instance.getAsByte( paramKey ) );
					break;
				case CHAR:
					json.put( paramKey, instance.getAsChar( paramKey ) );
					break;
				case DOUBLE:
					json.put( paramKey, instance.getAsDouble( paramKey ) );
					break;
				case FLOAT:
					json.put( paramKey, instance.getAsFloat( paramKey ) );
					break;
				case INT:
					json.put( paramKey, instance.getAsInt( paramKey ) );
					break;
				case LONG:
					json.put( paramKey, instance.getAsLong( paramKey ) );
					break;
				case SHORT:
					json.put( paramKey, instance.getAsShort( paramKey ) );
					break;
				case STRING:
					json.put( paramKey, instance.getAsString( paramKey ) );
					break;
				case UNKNOWN:
				default:
					break;
			}
		}
		return json.toJSONString();
	}

	/**
	 * Utility method to convert simple wild card matching strings to a valid regular expressions
	 *
	 * We are expecting strings along the lines of...
	 *     "HLAinteractionRoot.C2WInteractionRoot.SomeType.*"
	 * ...which we convert into a valid Java regular expression strings like this...
	 *     "HLAinteractionRoot\.C2WInteractionRoot\.SomeType\..*"
	 *...and the compile to {@link Pattern} instances.
	 *
	 * So, we...
	 *     - trim the original string of leading/training spaces,
	 *     - split the string up on the "." characters
	 *     - join the string back together with escaped "." (i.e., "\.")
	 *     - replace any occurrences of "*" with ".*"
	 *     - put start and end string markers (i.e., "^" and "$" respectively) on the string
	 *     - ...and compile the string to a regex Pattern
	 *
	 * @param items the {@link String}s to convert to regular expression patterns
	 * @return the regular expression {@link Pattern}s
	 */
	private Collection<Pattern> stringsToRegexPatterns( Collection<String> items )
	{
		if( items == null )
			return Collections.emptyList();

		List<Pattern> patterns = new ArrayList<Pattern>();
		for( String item : items )
		{
			String regex = "^" +
				Stream.of( item.trim().split( "\\." ) )
				.collect( Collectors.joining( "\\." ) )
				.replace( "*", ".*" ) +
				"$";
			try
			{
				patterns.add( Pattern.compile( regex ) );
			}
			catch( Exception e )
			{
				// probably a PatternSyntaxException
				throw new UCEFException( String.format(
				                                        "Unable to convert '%s' to a regular expression",
				                                        item ) );
			}
		}
		return patterns;
	}

	/**
	 * Utility method to determine if the given text matches any of the provided regular
	 * expression patterns
	 *
	 * @param text the text to check
	 * @param pattern the {@link Pattern}s to check against
	 * @return true if the text matches any of the patterns, false otherwise
	 */
	private boolean matchesAnyPattern( String text, Collection<Pattern> pattern )
	{
		if( text == null || pattern == null )
			return false;

		for( Pattern regex : pattern )
		{
			if( regex.matcher( text ).matches() )
				return true;
		}
		return false;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

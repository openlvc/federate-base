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

import gov.nist.ucef.hla.base.FederateAmbassador;
import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.FederateConfiguration;
import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.base.UCEFSyncPoint;
import gov.nist.ucef.hla.ucef.interaction.SimEnd;
import gov.nist.ucef.hla.ucef.interaction.SimPause;
import gov.nist.ucef.hla.ucef.interaction.SimResume;
import gov.nist.ucef.hla.ucef.interaction.UCEFInteraction;
import gov.nist.ucef.hla.ucef.interaction.UCEFInteractionRealizer;

public abstract class UCEFFederateBase extends FederateBase
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private UCEFInteractionRealizer ucefInteractionRealizer;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UCEFFederateBase()
	{
		super();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This is the main method which carries out the life cycle of the federate
	 * 
	 * @param configuration the configuration for the federate
	 */
	public void runFederate( FederateConfiguration configuration )
	{
		// sanity check
		if(configuration == null)
			throw new UCEFException("Federate configuration cannot be null.");
			
		this.configuration = configuration;

		this.rtiamb = new RTIAmbassadorWrapper();
		this.fedamb = new FederateAmbassador( this );
		
		this.ucefInteractionRealizer = new UCEFInteractionRealizer( rtiamb );

		super.createAndJoinFederation();
		enableTimePolicy();
		
		publishAndSubscribe();

		beforeReadyToPopulate();
		synchronize( UCEFSyncPoint.READY_TO_POPULATE );

		beforeReadyToRun();
		synchronize( UCEFSyncPoint.READY_TO_RUN );
		
		beforeFirstStep();

		double currentTime = 0.0;
		double timeStep = configuration.getLookAhead();
		while( true )
		{
			currentTime = fedamb.getFederateTime();

			// next step
			if( step( currentTime ) == false )
				break;

			// advance, or tick, or nothing!
			if( configuration.isTimeStepped() )
				advanceTime( currentTime + timeStep );
			else if( configuration.callbacksAreEvoked() )
				evokeMultipleCallbacks();
			else
				;
		}

		disableTimePolicy();

		beforeReadyToResign();
		synchronize( UCEFSyncPoint.READY_TO_RESIGN );
		beforeExit();

		resignAndDestroyFederation();
	}
	
	/**
	 * Override to provide handling for UCEF specific interaction types
	 */
	@Override
	public void incomingInteraction( HLAInteraction interaction, double time )
	{
		UCEFInteraction realizedInteraction = this.ucefInteractionRealizer.realize( interaction );
		if( realizedInteraction == null )
		{
			// generic interaction
			receiveInteraction( interaction, time );
		}
		else if( realizedInteraction instanceof SimPause )
		{
			// delegate to handler for UCEF SimPause interactions
			receiveSimPause( (SimPause)realizedInteraction, time );
		}
		else if( realizedInteraction instanceof SimResume )
		{
			// delegate to handler for UCEF SimResume interactions
			receiveSimResume( (SimResume)realizedInteraction, time );
		}
		else if( realizedInteraction instanceof SimEnd )
		{
			// delegate to handler for UCEF SimEnd interactions
			receiveSimEnd( (SimEnd)realizedInteraction, time );
		}
	}
	
	@Override
	public void incomingInteraction( HLAInteraction interaction )
	{
		UCEFInteraction realizedInteraction = this.ucefInteractionRealizer.realize( interaction );
		if( realizedInteraction == null )
		{
			// generic interaction
			receiveInteraction( interaction );
		}
		else if( realizedInteraction instanceof SimPause )
		{
			// delegate to handler for UCEF SimPause interactions
			receiveSimPause( (SimPause)realizedInteraction );
		}
		else if( realizedInteraction instanceof SimResume )
		{
			// delegate to handler for UCEF SimResume interactions
			receiveSimResume( (SimResume)realizedInteraction );
		}
		else if( realizedInteraction instanceof SimEnd )
		{
			// delegate to handler for UCEF SimEnd interactions
			receiveSimEnd( (SimEnd)realizedInteraction );
		}
	}

	//----------------------------------------------------------
	//         UCEF SPECIFIC INTERACTION CALLBACK METHODS
	//----------------------------------------------------------
	/**
	 * Called whenever the UCEF specific "simulation pause" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simPause the {@link SimPause} interaction
	 */
	public void receiveSimPause( SimPause simPause )
	{
		// ignored by default - override this method to provide specific handling
	}

	/**
	 * Called whenever the UCEF specific "simulation pause" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simPause the {@link SimPause} interaction
	 * @param federateTime the current logical time of the federate
	 */
	public void receiveSimPause( SimPause simPause, double time )
	{
		// delegate to handler with no time parameter as the default behaviour
		// override this method to provide specific handling
		receiveSimPause( simPause );
	}

	/**
	 * Called whenever the UCEF specific "simulation resume" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simResume the {@link SimResume} interaction
	 * @param federateTime the current logical time of the federate
	 */
	public void receiveSimResume( SimResume simResume )
	{
		// ignored by default - override this method to provide specific handling
	}

	/**
	 * Called whenever the UCEF specific "simulation resume" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simResume the {@link SimResume} interaction
	 * @param federateTime the current logical time of the federate
	 */
	public void receiveSimResume( SimResume simResume, double time )
	{
		// delegate to handler with no time parameter as the default behaviour
		// override this method to provide specific handling
		receiveSimResume( simResume );
	}

	/**
	 * Called whenever the UCEF specific "simulation end" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simEnd the {@link SimEnd} interaction
	 */
	public void receiveSimEnd( SimEnd simEnd )
	{
		// ignored by default - override this method to provide specific handling
	}

	/**
	 * Called whenever the UCEF specific "simulation end" interaction is received
	 * 
	 * NOTE: this method can be overridden to provide handling suitable for a 
	 *       specific federate's requirements
	 *
	 * @param simEnd the {@link SimEnd} interaction
	 * @param time the current logical time of the federate
	 */
	public void receiveSimEnd( SimEnd simEnd, double time )
	{
		// delegate to handler with no time parameter as the default behaviour
		// override this method to provide specific handling
		receiveSimEnd( simEnd );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

/*
 *   Copyright 2019 Calytrix Technologies
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
package gov.nist.ucef.hla.ucef;

import gov.nist.ucef.hla.base.FederateBase;
import gov.nist.ucef.hla.base.HLAInteraction;
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
		this.ucefInteractionRealizer = new UCEFInteractionRealizer( rtiamb );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
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

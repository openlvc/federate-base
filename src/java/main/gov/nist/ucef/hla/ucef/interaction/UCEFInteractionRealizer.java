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
package gov.nist.ucef.hla.ucef.interaction;

import java.util.HashMap;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import hla.rti1516e.InteractionClassHandle;

public class UCEFInteractionRealizer
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIAmbassadorWrapper rtiamb;
	private HashMap<InteractionClassHandle,Realizer> realizerLookup;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UCEFInteractionRealizer( RTIAmbassadorWrapper rtiamb )
	{
		this.rtiamb = rtiamb;
		initializeRealizers();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Determine if we know how to realize the given {@link HLAInteraction} instance.
	 * 
	 * An alternative way to do this would be to call {@link #realize(HLAInteraction)} and check
	 * for a null return value.
	 * 
	 * @param interaction the {@link HLAInteraction} instance to process
	 * @return true if we know how to realize the {@link HLAInteraction}, false otherwise
	 */
	public boolean canRealize( HLAInteraction interaction )
	{
		if( interaction == null )
			return false;

		InteractionClassHandle handle = rtiamb.getInteractionClassHandle( interaction );
		return realizerLookup.containsKey( handle );
	}

	/**
	 * Realize a specific {@link UCEFInteraction} from a generic {@HLAInteraction}. Possible
	 * {@link UCEFInteraction} instances are:
	 * 
	 * <ul>
	 * <li>{@link FederateJoin}</li>
	 * <li>{@link SimPause}</li>
	 * <li>{@link SimResume}</li>
	 * <li>{@link SimEnd}</li>
	 * </ul>
	 * 
	 * if the {@link HLAInteraction} instance does not correspond to one of these, a null will be
	 * returned.
	 * 
	 * @param interaction the {@link HLAInteraction} instance from which to create the
	 *            {@link UCEFInteraction}
	 * @return the {@link UCEFInteraction} instance, or null if the {@link HLAInteraction}
	 *         instance does not correspond to a {@link HLAInteraction}.
	 */
	public UCEFInteraction realize( HLAInteraction interaction )
	{
		if( interaction == null )
			return null;
		
		InteractionClassHandle interactionKind = rtiamb.getInteractionClassHandle( interaction );
		Realizer realizer = realizerLookup.get( interactionKind );
		return realizer == null ? null : realizer.realize( interaction );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Internal method to populate a map which provides associations for "realizers" for each of
	 * the {@link UCEFInteraction} types.
	 * 
	 * The populated map then used to select the correct "realizer" for a given
	 * {@link HLAInteraction} instance.
	 * 
	 * NOTE: for those who like patterns, this is essentially the "Command Pattern"; check here
	 * for more details: {@link https://en.wikipedia.org/wiki/Command_pattern}
	 * 
	 * See {@link Realizer} interface definition.
	 */
	private void initializeRealizers()
	{
		if( realizerLookup != null )
			return;
		
		realizerLookup = new HashMap<InteractionClassHandle, Realizer>();
		
		InteractionClassHandle federateJoin = rtiamb.getInteractionClassHandle( FederateJoin.interactionName() );
		InteractionClassHandle simPause = rtiamb.getInteractionClassHandle( SimPause.interactionName() );
		InteractionClassHandle simResume = rtiamb.getInteractionClassHandle( SimResume.interactionName() );
		InteractionClassHandle simEnd = rtiamb.getInteractionClassHandle( SimEnd.interactionName() );
		
		realizerLookup.put(federateJoin, new Realizer() { 
			public UCEFInteraction realize( HLAInteraction x ) { return new FederateJoin( rtiamb, x.getState() ); }
		});
		realizerLookup.put(simPause, new Realizer() { 
			public UCEFInteraction realize( HLAInteraction x ) { return new SimPause( rtiamb, x.getState() ); } 
		});
		realizerLookup.put(simResume, new Realizer() { 
			public UCEFInteraction realize( HLAInteraction x ) { return new SimResume( rtiamb, x.getState() ); }
		});
		realizerLookup.put(simEnd, new Realizer() {
			public UCEFInteraction realize( HLAInteraction x ) { return new SimEnd( rtiamb, x.getState() ); }
		});
	}

	//----------------------------------------------------------
	//                    PRIVATE INTERFACES
	//----------------------------------------------------------
	/**
	 * Internal interface used to provide a suitable function for "realizing" generic
	 * {@link HLAInteraction}s to specific known types of {@link UCEFInteraction}s
	 * 
	 * NOTE: Command Pattern - {@link https://en.wikipedia.org/wiki/Command_pattern}
	 */
	private interface Realizer
	{
		/**
		 * "Realize" a specific kind of {@link UCEFInteraction} from a "generic"
		 * {@link HLAInteraction} instance
		 * 
		 * @param param the {@link HLAInteraction} instance from which to create the
		 *            {@link UCEFInteraction}
		 * @return the {@link UCEFInteraction} instance
		 */
		UCEFInteraction realize( HLAInteraction param );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

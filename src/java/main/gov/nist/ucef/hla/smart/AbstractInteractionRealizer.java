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
package gov.nist.ucef.hla.smart;

import java.util.HashMap;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import hla.rti1516e.InteractionClassHandle;


/**
 * An abstract class providing the base functionality for "realizing" concrete types of
 * {@link HLAInteraction} instances.
 * 
 * This class is not instantiated directly, but instead used as a base for other classes to
 * extend.
 * 
 * Classes extending this will need to provide an implementation for
 * {@link #initializeRealizers()} to populate the realizer lookup table. Refer to the comments for
 * that method for further details
 */
public abstract class AbstractInteractionRealizer
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected RTIAmbassadorWrapper rtiamb;
	protected HashMap<InteractionClassHandle,Realizer> realizerLookup;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public AbstractInteractionRealizer( RTIAmbassadorWrapper rtiamb )
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
	public boolean canProcess( HLAInteraction interaction )
	{
		if( interaction == null )
			return false;

		InteractionClassHandle handle = rtiamb.getInteractionClassHandle( interaction );
		return realizerLookup.containsKey( handle );
	}

	/**
	 * Create a specific interaction type from a generic {@HLAInteraction}
	 * 
	 * If the {@link HLAInteraction} instance does not correspond a known interaction, a null will
	 * be returned.
	 * 
	 * @param interaction the {@link HLAInteraction} instance from which to create the
	 *            {@link SmartInteraction}
	 * @return the {@link SmartInteraction} instance, or null if the {@link HLAInteraction}
	 *         instance does not correspond to a known {@link HLAInteraction}.
	 */
	public SmartInteraction realize( HLAInteraction interaction )
	{
		if( interaction == null )
			return null;
		
		InteractionClassHandle interactionKind = rtiamb.getInteractionClassHandle( interaction );
		Realizer creator = realizerLookup.get( interactionKind );
		SmartInteraction smartInteraction = creator == null ? null : creator.realize( interaction ); 
		return smartInteraction;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Base method to populate a map which provides associations for "creators" for each of
	 * the {@link SmartInteraction} types.
	 * 
	 * The populated map then used by the {@link #realize(HLAInteraction)} method.
	 * 
	 * Classes extending this class will need to provide a fuller implementation for this 
	 * method - the below is a simple example of what needs to be done:
	 * 
	 * <code>
	 * @Override
	 * protected void initializeRealizers()
	 * {
	 *      if( realizerLookup != null )
	 *          return;
	 * 		super.initializeRealizers();
	 * 		
	 * 		// get handles for interactions we deal with
	 * 		InteractionClassHandle pingHandle = rtiamb.getInteractionClassHandle( Ping.interactionName() );
	 * 		InteractionClassHandle pongHandle = rtiamb.getInteractionClassHandle( Pong.interactionName() );
	 * 		
	 * 		// associate handles with Realizer implementations
	 * 		realizerLookup.put( pingHandle, new Realizer() {
	 *      	public SmartInteraction realize( HLAInteraction x ) { return new Ping( rtiamb, x.getState() ); }
	 *      });
	 * 		realizerLookup.put( pongHandle, new Realizer() {
	 * 			public SmartInteraction realize( HLAInteraction x ) { return new Pong( rtiamb, x.getState() ); } 
	 * 		});
	 * 	}
	 * </code>
	 * 
	 * NOTE: for those who like patterns, this is essentially the "Command Pattern"; check here
	 * for more details: {@link https://en.wikipedia.org/wiki/Command_pattern}
	 * 
	 * See {@link Realizer} interface definition.
	 */
	protected void initializeRealizers()
	{
		if( realizerLookup != null )
			return;
		
		realizerLookup = new HashMap<InteractionClassHandle, Realizer>();
	}

	//----------------------------------------------------------
	//                    PRIVATE INTERFACES
	//----------------------------------------------------------
	/**
	 * Internal interface used to provide a suitable function for creating
	 * {@link SmartInteraction}s of known types
	 * 
	 * NOTE: Command Pattern - {@link https://en.wikipedia.org/wiki/Command_pattern}
	 */
	protected interface Realizer
	{
		/**
		 * Create a {@link SmartInteraction} from a "generic" {@link HLAInteraction} instance
		 * 
		 * @param param the {@link HLAInteraction} instance from which to create the
		 *            {@link SmartInteraction}
		 * @return the {@link SmartInteraction} instance
		 */
		SmartInteraction realize( HLAInteraction param );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

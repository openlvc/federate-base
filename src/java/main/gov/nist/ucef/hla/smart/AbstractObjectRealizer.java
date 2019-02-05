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
import java.util.Map;
import java.util.Map.Entry;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.base.RTIAmbassadorWrapper;
import gov.nist.ucef.hla.example.smart.reflections.Player;
import gov.nist.ucef.hla.smart.AbstractInteractionRealizer.Realizer;
import hla.rti1516e.ObjectClassHandle;

/**
 * An abstract class providing the base functionality for "realizing" concrete types of
 * {@link HLAObject} instances.
 * 
 * This class is not instantiated directly, but instead used as a base for other classes to
 * extend.
 * 
 * Classes extending this will need to provide an implementation for
 * {@link #initializeRealizers()} to populate the realizer lookup table. Refer to the comments for
 * that method for further details
 */
public abstract class AbstractObjectRealizer
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected RTIAmbassadorWrapper rtiamb;
	protected HashMap<ObjectClassHandle,Realizer> realizerLookup;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public AbstractObjectRealizer( RTIAmbassadorWrapper rtiamb )
	{
		this.rtiamb = rtiamb;
		initializeRealizers();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Determine if we can realize the given {@link HLAObject} instance.
	 * 
	 * An alternative way to do this would be to call {@link #realize(HLAObject)} and check for a
	 * null return value.
	 * 
	 * @param object the {@link HLAObject} instance to realize
	 * @return true if the {@link HLAObject} instance can be realized, false otherwise
	 */
	public boolean canRealize( HLAObject object )
	{
		if( object == null )
			return false;

		ObjectClassHandle handle = rtiamb.getKnownObjectClassHandle(  object );
		return realizerLookup.containsKey( handle );
	}

	/**
	 * Create a specific attribute reflection type from a generic {@HLAObject}
	 * 
	 * If the {@link HLAObject} instance does not correspond a known attribute refelction, a null
	 * will be returned.
	 * 
	 * @param object the {@link HLAObject} instance from which to create the
	 *            {@link SmartObject}
	 * @return the {@link SmartObject} instance, or null if the {@link HLAObject}
	 *         instance does not correspond to a known {@link HLAObject}.
	 */
	public SmartObject realize( HLAObject object )
	{
		if( object == null )
			return null;
		
		ObjectClassHandle objectKind = rtiamb.getKnownObjectClassHandle( object );
		Realizer creator = realizerLookup.get( objectKind );
		SmartObject smartObject = creator == null ? null : creator.realize( object ); 
		return smartObject;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Base method to populate a map which provides associations for "creators" for each of
	 * the {@link SmartObject} types.
	 * 
	 * The populated map then used by the {@link #realize(HLAObject)} method.
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
	 * 		// get handles for attribute reflections we deal with
	 * 		ObjectClassHandle playerObjectHandle = rtiamb.getObjectClassHandle( Player.objectClassName() );
	 * 		
	 * 		// associate handles with Realizer implementations
	 * 		realizerLookup.put( playerObjectHandle, new Realizer() {
	 *      	public SmartObject realize( HLAObject x ) { return new Player( rtiamb, mapCopy(x.getState())); }
	 *      });
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
		
		realizerLookup = new HashMap<ObjectClassHandle, Realizer>();
	}

	/**
	 * Provide a separate, independent copy of the original map
	 * 
	 * @param original the original map
	 * @return a separate, independent copy of the original map
	 */
	protected Map<String,byte[]> mapCopy( Map<String,byte[]> original )
	{
		Map<String,byte[]> copy = new HashMap<>();
		for( Entry<String,byte[]> x : original.entrySet() )
		{
			byte[] src = x.getValue();
			byte[] dst = new byte[src.length];
			System.arraycopy( src, 0, dst, 0, src.length );
			copy.put( x.getKey(), dst );
		}
		return copy;
	}

	//----------------------------------------------------------
	//                    PRIVATE INTERFACES
	//----------------------------------------------------------
	/**
	 * Internal interface used to provide a suitable function for creating
	 * {@link SmartObject}s of known types
	 * 
	 * NOTE: Command Pattern - {@link https://en.wikipedia.org/wiki/Command_pattern}
	 */
	protected interface Realizer
	{
		/**
		 * Create a {@link SmartObject} from a "generic" {@link HLAObject} instance
		 * 
		 * @param param the {@link HLAObject} instance from which to create the
		 *            {@link SmartObject}
		 * @return the {@link SmartObject} instance
		 */
		SmartObject realize( HLAObject param );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

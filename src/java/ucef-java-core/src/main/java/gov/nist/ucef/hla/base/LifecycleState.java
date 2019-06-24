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
package gov.nist.ucef.hla.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * This enumeration provides the possible lifecycle states which UCEF HLA federates may be in.
 *
 * This allows a mechanism for federate implementations to differentiate between three main cases:
 * <ol>
 * <li>{@link #INITIALIZING}: during beforeReadyToPopulate(), beforeReadyToRun() or
 * beforeFirstStep()</li>
 * <li>{@link #RUNNING}: received in step(double)</li>
 * <li>{@link #CLEANING_UP}: received in beforeReadyToResign() or beforeExit();</li>
 * </ol>
 *
 * If required, interactions and reflections can then be processed differently at these different
 * points in the federate life cycle.
 *
 * There are two additional cases included for mainly for completeness of lifecycle
 * representation. These are the {@link #GESTATING} and {@link #EXPIRED} stages.
 *
 * It is not possible for interactions and reflections to be sent or received during
 * {@link #GESTATING} and {@link #EXPIRED} stages. This is because the federate has not yet
 * joined the federation in the case of the {@link #GESTATING} state, and has already left the
 * federation in the case of the {@link #EXPIRED} state.
 */
public enum LifecycleState
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	// possible lifecycle stages, in expected chronological order of execution in the federation
	GESTATING("gestating", "Gestating"),          // not a federation member
	INITIALIZING("initializing", "Initializing"), // federation joined, but still setting up the simulation
	RUNNING("running", "Running"),                // simulation is active
	CLEANING_UP("cleaningUp", "Cleaning Up"),     // simulation completed, but still a federation member
	EXPIRED("expired", "Expired");                // left the federation

	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------
	// a map for finding a lifecycle stage for a string key - this is to provide
	// quick lookups and avoid iterating over all lifecycle stages (though admittedly
	// there are only three of them)
	private static final Map<String,LifecycleState> LIFECYCLE_STATE_LOOKUP =
	    Collections.unmodifiableMap( initializeMapping() );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// the string identifier for the lifecycle stage
	private String label;
	// the "human readable" text name for the lifecycle stage, primarily used for
	// logging/debugging purposes
	private String name;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private LifecycleState( String label, String name )
	{
		this.label = label;
		this.name = name;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the human readable text describing this {@link LifecycleState}
	 */
	@Override
	public String toString()
	{
		return this.name;
	}

	/**
	 * Obtain the text identifier uniquely identifying this lifecycle stage (internal use)
	 *
	 * @return the text identifier uniquely identifying this lifecycle stage (internal use)
	 */
	public String getLabel()
	{
		return this.label;
	}

	/**
	 * Determine if this lifecycle stage is before the provided lifecycle stage
	 *
	 * @param other the other lifecycle stage
	 * @return true if this lifecycle stage is before the provided lifecycle stage,
	 *         false otherwise
	 */
	public boolean isBefore( LifecycleState other )
	{
		// NOTE: relies on the enumerated values being defined in the expected
		//       chronological order
		return other != null && this.ordinal() < other.ordinal();
	}

	/**
	 * Determine if this lifecycle stage is at or before the provided lifecycle stage
	 *
	 * @param other the other lifecycle stage
	 * @return true if this lifecycle stage is at or before the provided synchronization
	 *         point, false otherwise
	 */
	public boolean isAtOrBefore( LifecycleState other )
	{
		// NOTE: relies on the enumerated values being defined in the expected
		//       chronological order
		return other != null && this.ordinal() <= other.ordinal();
	}

	/**
	 * Determine if this lifecycle stage is after the provided lifecycle stage
	 *
	 * @param other the other lifecycle stage
	 * @return true if this lifecycle stage is before the provided lifecycle stage,
	 *         false otherwise
	 */
	public boolean isAfter( LifecycleState other )
	{
		// NOTE: relies on the enumerated values being defined in the expected
		//       chronological order
		return other != null && this.ordinal() > other.ordinal();
	}

	/**
	 * Determine if this lifecycle stage is at or after the provided lifecycle stage
	 *
	 * @param other the other lifecycle stage
	 * @return true if this lifecycle stage is at or after the provided synchronization
	 *         point, false otherwise
	 */
	public boolean isAtOrAfter( LifecycleState other )
	{
		// NOTE: relies on the enumerated values being defined in the expected
		//       chronological order
		return other != null && this.ordinal() >= other.ordinal();
	}

	/**
	 * Determine if this lifecycle stage is the same as the provided lifecycle stage
	 *
	 * NOTE: this is just wrapper around the standard enumeration equals() method provided to
	 * improve code readability
	 *
	 * @param other the other lifecycle stage
	 * @return true if this lifecycle stage is the same as the provided synchronization
	 *         point, false otherwise
	 */
	public boolean is( LifecycleState other )
	{
		return this.equals( other );
	}

	/**
	 * Determine if this lifecycle stage is not the same as the provided synchronization
	 * point
	 *
	 * NOTE: this is just a negation of the standard enumeration equals() method provided to
	 * improve code readability
	 *
	 * @param other the other lifecycle stage
	 * @return true if this lifecycle stage is not the same as the provided synchronization
	 *         point, false otherwise
	 */
	public boolean isNot( LifecycleState other )
	{
		return !this.equals(other);
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Determine if a label identifies a standard UCEF lifecycle stage
	 *
	 * @param label the text identifier uniquely identifying a lifecycle stage
	 * @return true if the label is for a known UCEF lifecycle stage, false otherwise
	 *         text identifier for a {@link LifecycleState}.
	 */
	public static boolean isKnown( String label )
	{
		return !LifecycleState.isUnknown( label );
	}

	/**
	 * Determine if a label does not identify a standard UCEF lifecycle stage
	 *
	 * @param label the text identifier uniquely identifying a lifecycle stage
	 * @return true if the label is not a known UCEF lifecycle stage, false otherwise
	 *         text identifier for a {@link LifecycleState}.
	 */
	public static boolean isUnknown( String label )
	{
		return LifecycleState.fromLabel( label ) == null;
	}

	/**
	 * Converts a text identifier uniquely identifying a lifecycle stage to a
	 * {@link LifecycleState} instance.
	 *
	 * NOTE: if the key is not a valid text identifier for a lifecycle stage, null will be
	 * returned
	 *
	 * @param label the text identifier uniquely identifying a lifecycle stage
	 * @return the corresponding {@link LifecycleState}, or null if the key is not a valid
	 *         text identifier for a {@link LifecycleState}.
	 */
	public static LifecycleState fromLabel( String label )
	{
		return LIFECYCLE_STATE_LOOKUP.get( label );
	}

	/**
	 * Private initializer method for the key-to-{@link LifecycleState} lookup map
	 *
	 * @return a lookup map which pairs text identifiers and the corresponding
	 *         SyncronizationPoints
	 */
	private static Map<String,LifecycleState> initializeMapping()
	{
		Map<String,LifecycleState> lookupMap = new HashMap<String,LifecycleState>();
		for( LifecycleState s : LifecycleState.values() )
		{
			lookupMap.put( s.label, s );
		}
		return lookupMap;
	}
}

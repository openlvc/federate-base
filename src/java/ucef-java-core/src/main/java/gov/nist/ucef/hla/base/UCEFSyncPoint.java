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
 * This enumeration provides the possible synchronization points which UCEF HLA federates must at
 * least notionally acknowledge, and preferably adhere to in order to interact sensibly with the
 * federation manager.
 */
public enum UCEFSyncPoint
{
    //----------------------------------------------------------
    //                        VALUES
    //----------------------------------------------------------
    // possible synchronization points, in expected chronological order of execution in
	// the federation
	READY_TO_POPULATE("readyToPopulate", "Ready to Populate"),
	READY_TO_RUN("readyToRun", "Ready to Run"),
	READY_TO_RESIGN("readyToResign", "Ready to Resign");

	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------
	// a map for finding a Synchronization point for a string key - this is to provide
	// quick lookups and avoid iterating over all SynchronizationPoints (though admittedly
	// there are only three of them)
	private static final Map<String,UCEFSyncPoint> SYNC_POINT_LOOKUP =
	    Collections.unmodifiableMap( initializeMapping() );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// the string identifier for the synchronization point
	private String label;
	// the "human readable" text name for the synchronization point, primarily used for
	// logging/debugging purposes
	private String name;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private UCEFSyncPoint( String label, String name )
	{
		this.label = label;
		this.name = name;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the human readable text describing this {@link UCEFSyncPoint}
	 */
	@Override
	public String toString()
	{
		return this.name;
	}

	/**
	 * Obtain the text identifier uniquely identifying this synchronization point (internal use)
	 * 
	 * @return the text identifier uniquely identifying this synchronization point (internal use)
	 */
	public String getLabel()
	{
		return this.label;
	}

	/**
	 * Determine if this synchronization point is before the provided synchronization point
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is before the provided synchronization point,
	 *         false otherwise
	 */
	public boolean isBefore( UCEFSyncPoint other )
	{
		// NOTE: relies on the enumerated values being defined in the expected 
		//       chronological order
		return other != null && this.ordinal() < other.ordinal();
	}
	
	/**
	 * Determine if this synchronization point is at or before the provided synchronization point
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is at or before the provided synchronization
	 *         point, false otherwise
	 */
	public boolean isAtOrBefore( UCEFSyncPoint other )
	{
		// NOTE: relies on the enumerated values being defined in the expected 
		//       chronological order
		return other != null && this.ordinal() <= other.ordinal();
	}
	
	/**
	 * Determine if this synchronization point is after the provided synchronization point
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is before the provided synchronization point,
	 *         false otherwise
	 */
	public boolean isAfter( UCEFSyncPoint other )
	{
		// NOTE: relies on the enumerated values being defined in the expected 
		//       chronological order
		return other != null && this.ordinal() > other.ordinal();
	}

	/**
	 * Determine if this synchronization point is at or after the provided synchronization point
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is at or after the provided synchronization
	 *         point, false otherwise
	 */
	public boolean isAtOrAfter( UCEFSyncPoint other )
	{
		// NOTE: relies on the enumerated values being defined in the expected 
		//       chronological order
		return other != null && this.ordinal() >= other.ordinal();
	}
	
	/**
	 * Determine if this synchronization point is the same as the provided synchronization point
	 * 
	 * NOTE: this is just wrapper around the standard enumeration equals() method provided to
	 * improve code readability
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is the same as the provided synchronization
	 *         point, false otherwise
	 */
	public boolean is( UCEFSyncPoint other )
	{
		return this.equals( other );
	}

	/**
	 * Determine if this synchronization point is not the same as the provided synchronization
	 * point
	 * 
	 * NOTE: this is just a negation of the standard enumeration equals() method provided to
	 * improve code readability
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is not the same as the provided synchronization
	 *         point, false otherwise
	 */
	public boolean isNot( UCEFSyncPoint other )
	{
		return !this.equals(other);
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Determine if a label identifies a standard UCEF synchronization point
	 * 
	 * @param label the text identifier uniquely identifying a synchronization point
	 * @return true if the label is for a known UCEF synchronization point, false otherwise
	 *         text identifier for a {@link UCEFSyncPoint}.
	 */
	public static boolean isKnown( String label )
	{
		return !UCEFSyncPoint.isUnknown( label );
	}
	
	/**
	 * Determine if a label does not identify a standard UCEF synchronization point
	 * 
	 * @param label the text identifier uniquely identifying a synchronization point
	 * @return true if the label is not a known UCEF synchronization point, false otherwise
	 *         text identifier for a {@link UCEFSyncPoint}.
	 */
	public static boolean isUnknown( String label )
	{
		return UCEFSyncPoint.fromLabel( label ) == null;
	}
	
	/**
	 * Converts a text identifier uniquely identifying a synchronization point to a
	 * {@link UCEFSyncPoint} instance.
	 * 
	 * NOTE: if the key is not a valid text identifier for a synchronization point, null will be
	 * returned
	 * 
	 * @param label the text identifier uniquely identifying a synchronization point
	 * @return the corresponding {@link UCEFSyncPoint}, or null if the key is not a valid
	 *         text identifier for a {@link UCEFSyncPoint}.
	 */
	public static UCEFSyncPoint fromLabel( String label )
	{
		return SYNC_POINT_LOOKUP.get( label );
	}

	/**
	 * Private initializer method for the key-to-{@link UCEFSyncPoint} lookup map
	 * 
	 * @return a lookup map which pairs text identifiers and the corresponding
	 *         SyncronizationPoints
	 */
	private static Map<String,UCEFSyncPoint> initializeMapping()
	{
		Map<String,UCEFSyncPoint> lookupMap = new HashMap<String,UCEFSyncPoint>();
		for( UCEFSyncPoint s : UCEFSyncPoint.values() )
		{
			lookupMap.put( s.label, s );
		}
		return lookupMap;
	}
}

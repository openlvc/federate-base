/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-gateway.
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
package gov.nist.ucef.hla.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * This enumeration provides the possible synchronization points which UCEF HLA federates must at
 * least notionally acknowledge, and preferably adhere to in order to interact sensibly with the
 * federate manager.
 */
public enum SyncPoint
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
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// a map for finding a Synchronization point for a string key - this is to provide
	// quick lookups and avoid iterating over all SynchronizationPoints (though admittedly
	// there are only three of them)
	private static final Map<String,SyncPoint> keyToSynchronizationPointLookup =
	    Collections.unmodifiableMap( initializeMapping() );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// the string identifier for the synchronization point
	private String id;
	// the "human readable" text name for the synchronization point, primarily used for
	// logging/debugging purposes
	private String name;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private SyncPoint( String id, String name )
	{
		this.id = id;
		this.name = name;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the human readable text describing this {@link SyncPoint}
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
	public String getID()
	{
		return this.id;
	}

	/**
	 * Determine if this synchronization point is before the provided synchronization point
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is before the provided synchronization point,
	 *         false otherwise
	 */
	public boolean isBefore( SyncPoint other )
	{
		// NOTE: relies on the enumerated values being defined in the expected 
		//       chronological order
		return other != null && this.ordinal() < other.ordinal();
	}

	/**
	 * Determine if this synchronization point is after the provided synchronization point
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is before the provided synchronization point,
	 *         false otherwise
	 */
	public boolean isAfter( SyncPoint other )
	{
		// NOTE: relies on the enumerated values being defined in the expected 
		//       chronological order
		return other != null && this.ordinal() > other.ordinal();
	}

	/**
	 * Determine if this synchronization point is not the same as the provided synchronization
	 * point
	 * 
	 * NOTE: this is just a negatiion of the standard enumeration equals() method provided to
	 * improve code readbility
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point not the same as the provided synchronization
	 *         point, false otherwise
	 */
	public boolean isNot( SyncPoint other )
	{
		return !this.equals(other);
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Converts a text identifier uniquely identifying a synchronization point to a
	 * {@link SyncPoint} instance.
	 * 
	 * NOTE: if the key is not a valid text identifier for a synchronization point, null will be
	 * returned
	 * 
	 * @param id the text identifier uniquely identifying a synchronization point
	 * @return the corresponding {@link SyncPoint}, or null if the key is not a valid
	 *         text identifier for a {@link SyncPoint}.
	 */
	public static SyncPoint fromID( String id )
	{
		return keyToSynchronizationPointLookup.get( id );
	}

	/**
	 * Private initializer method for the key-to-{@link SyncPoint} lookup map
	 * 
	 * @return a lookup map which pairs text identifiers and the corresponding
	 *         SyncronizationPoints
	 */
	private static Map<String,SyncPoint> initializeMapping()
	{
		Map<String,SyncPoint> lookupMap = new HashMap<String,SyncPoint>();
		for( SyncPoint s : SyncPoint.values() )
		{
			lookupMap.put( s.id, s );
		}
		return lookupMap;
	}
}

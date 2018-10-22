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
package gov.nist.hla.ucef_gateway.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * This enumeration provides the possible synchronization points which UCEF HLA federates must at
 * least notionally acknowledge, and preferably adhere to in order to interact sensibly with the
 * federate manager.
 */
public enum SynchronizationPoint
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
	private static final Map<String,SynchronizationPoint> keyToSynchronizationPointLookup =
	    Collections.unmodifiableMap( initializeMapping() );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// the string identifier for the synchronization point
	private String key;
	// the "human readable" text name for the synchronization point, primarily used for
	// and debugging purposes
	private String name;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private SynchronizationPoint( String key, String name )
	{
		this.key = key;
		this.name = name;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the human readable text describing this {@link SynchronizationPoint}
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
	public String getKey()
	{
		return this.key;
	}

	/**
	 * Determine if this synchronization point is before the provided synchronization point
	 * 
	 * @param other the other synchronization point
	 * @return true if this synchronization point is before the provided synchronization point,
	 *         false otherwise
	 */
	public boolean isBefore( SynchronizationPoint other )
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
	public boolean isAfter( SynchronizationPoint other )
	{
		// NOTE: relies on the enumerated values being defined in the expected 
		//       chronological order
		return other != null && this.ordinal() > other.ordinal();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Converts a text identifier uniquely identifying a synchronization point to a
	 * {@link SynchronizationPoint} instance.
	 * 
	 * NOTE: if the key is not a valid text identifier for a synchronization point, null will be
	 * returned
	 * 
	 * @param key the text identifier uniquely identifying a synchronization point
	 * @return the corresponding {@link SynchronizationPoint}, or null if the key is not a valid
	 *         text identifier for a {@link SynchronizationPoint}.
	 */
	public static SynchronizationPoint fromKey( String key )
	{
		return keyToSynchronizationPointLookup.get( key );
	}

	/**
	 * Private initializer method for the key-to-{@link SynchronizationPoint} lookup map
	 * 
	 * @return a lookup map which pairs text identifiers and the corresponding
	 *         SyncronizationPoints
	 */
	private static Map<String,SynchronizationPoint> initializeMapping()
	{
		Map<String,SynchronizationPoint> lookupMap = new HashMap<String,SynchronizationPoint>();
		for( SynchronizationPoint s : SynchronizationPoint.values() )
		{
			lookupMap.put( s.key, s );
		}
		return lookupMap;
	}
}

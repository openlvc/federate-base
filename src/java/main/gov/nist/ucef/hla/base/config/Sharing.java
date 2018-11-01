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
package gov.nist.ucef.hla.base.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * This enumeration provides the possible sharing policies for interactions and attributes
 */
public enum Sharing
{
    //----------------------------------------------------------
    //                        VALUES
    //----------------------------------------------------------
    // possible sharing policiess, in expected chronological order of execution in
	// the federation
	PUBLISH("Publish"),
	SUBSCRIBE("Subscribe"),
	PUBLISH_SUBSCRIBE("PublishSubscribe"),
	NEITHER("Neither");

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// a map for finding a sharing policy for a string key - this is to provide
	// quick lookups and avoid iterating over all SynchronizationPoints (though admittedly
	// there are only three of them)
	private static final Map<String,Sharing> keyToSynchronizationPointLookup =
	    Collections.unmodifiableMap( initializeMapping() );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// the string identifier for the sharing policy
	private String id;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Sharing( String id )
	{
		this.id = id;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the human readable text describing this {@link Sharing}
	 */
	@Override
	public String toString()
	{
		return getID();
	}

	/**
	 * Obtain the text identifier uniquely identifying this sharing policy (internal use)
	 * 
	 * @return the text identifier uniquely identifying this sharing policy (internal use)
	 */
	public String getID()
	{
		return this.id;
	}

	/**
	 * Determine if this sharing policy is the same as the provided sharing policy
	 * 
	 * NOTE: this is just a wrapper around standard enumeration equals() method provided to
	 * improve code readability
	 * 
	 * @param other the other sharing policy
	 * @return true if this sharing policy is the same as the provided sharing policy, false
	 *         otherwise
	 */
	public boolean is( Sharing other )
	{
		return this.equals(other);
	}
	
	/**
	 * Determine if this sharing policy is not the same as the provided sharing policy
	 * 
	 * NOTE: this is just a negation of the standard enumeration equals() method provided to
	 * improve code readability
	 * 
	 * @param other the other sharing policy
	 * @return true if this sharing policy is not the same as the provided sharing policy, false
	 *         otherwise
	 */
	public boolean isNot( Sharing other )
	{
		return !this.equals(other);
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Converts a text identifier uniquely identifying a sharing policy to a
	 * {@link Sharing} instance.
	 * 
	 * NOTE: if the key is not a valid text identifier for a sharing policy, null will be
	 * returned
	 * 
	 * @param id the text identifier uniquely identifying a sharing policy
	 * @return the corresponding {@link Sharing}, or null if the key is not a valid
	 *         text identifier for a {@link Sharing}.
	 */
	public static Sharing fromID( String id )
	{
		return keyToSynchronizationPointLookup.get( id );
	}

	/**
	 * Private initializer method for the key-to-{@link Sharing} lookup map
	 * 
	 * @return a lookup map which pairs text identifiers and the corresponding
	 *         SyncronizationPoints
	 */
	private static Map<String,Sharing> initializeMapping()
	{
		Map<String,Sharing> lookupMap = new HashMap<String,Sharing>();
		for( Sharing s : Sharing.values() )
		{
			lookupMap.put( s.id, s );
		}
		return lookupMap;
	}
}

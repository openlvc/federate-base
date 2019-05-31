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
package gov.nist.ucef.hla.example.fedman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gov.nist.ucef.hla.util.StringUtils;

import java.util.Set;

/**
 * A class providing functionality to check whether the start requirements for the Federation
 * Manager have been met - this is essentially the conditions which must be met before the
 * federation may advance to the the "start to populate" synchronisation point
 * 
 * The current implementation waits for minimum counts of specified federation types to join the
 * federation
 */
public class FedManStartRequirements
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// a map of federate types to minimum counts
	private Map<String, Integer> startRequirements;
	// used to track the types of federates which have joined the federation
	private Map<String, Set<FederateDetails>> joinedFederatesByType;
	// keeps track of the total number of federates required to start (regardless of type)
	private int totalFederatesRequired;
	
	private final Object mutex_lock = new Object();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FedManStartRequirements( Map<String,Integer> startRequirements )
	{
		this.startRequirements = startRequirements;
		
		joinedFederatesByType = new HashMap<>();
		
		// convenience calculation so we don't need to do 
		// it repeatedly later on
		totalFederatesRequired = startRequirements.values()
			.parallelStream()
			.mapToInt( i -> i.intValue() )
			.sum();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Obtain a count of the total number of federates required, regardless of type
	 * @return the total number of federates required, regardless of type
	 */
	public int totalFederatesRequired()
	{
		return totalFederatesRequired;
	}

	/**
	 * Utility method which simply returns a count of the number of federates which have joined
	 * the federation.
	 * 
	 * NOTE: this only counts federates which meet the criteria for allowing the simulation to
	 * begin (i.e., as specified in the command line arguments) and ignores all others.
	 * 
	 * @return the number of joined federates
	 */
	public int joinedCount()
	{
		int result = 0;
		synchronized( mutex_lock )
		{
			result = joinedFederatesByType.values().parallelStream().mapToInt( i -> i.size() ).sum();
		}
		return result;
	}
	
	/**
	 * Update when a federate joins 
	 * @param joinedFederate the salient details of the federate which joined
	 */
	public void federateJoined( FederateDetails joinedFederate )
	{
		String federateType = joinedFederate.getFederateType();
		
		// WORKAROUND for the fact that the federate type is currently not correctly
		//            propagated (and instead is actually the federate name)
		//            see: https://github.com/openlvc/portico/issues/280
		//                 https://github.com/openlvc/portico/pull/281
		/*
		for( String requiredType : startRequirements.keySet() )
		{
			if( federateType.startsWith( requiredType ) )
			{
				synchronized( mutex_lock )
				{
					joinedFederatesByType.computeIfAbsent( requiredType,
					                                       x -> new HashSet<>() ).add( joinedFederate );
				}
			}
		}
		*/
		if( startRequirements.containsKey( federateType ) )
		{
			synchronized( mutex_lock )
			{
				joinedFederatesByType.computeIfAbsent( federateType,
				                                       x -> new HashSet<>() ).add( joinedFederate );
			}
		}
	}
	
	/**
	 * Update when a federate leaves
	 * @param joinedFederate the salient details of the federate which left
	 */
	public void federateDeparted( FederateDetails joinedFederate )
	{
		String federateType = joinedFederate.getFederateType();
		
		// WORKAROUND for the fact that the federate type is currently not correctly
		//            propagated (and instead is actually the federate name)
		//            see: https://github.com/openlvc/portico/issues/280
		//                 https://github.com/openlvc/portico/pull/281
		/*
		for( String requiredType : startRequirements.keySet() )
		{
			if( federateType.startsWith( requiredType ) )
			{
				synchronized( mutex_lock )
				{
					joinedFederatesByType.computeIfAbsent( requiredType,
					                                       x -> new HashSet<>() ).add( joinedFederate );
				}
			}
		}
		 */
		if( startRequirements.containsKey( federateType ) )
		{
			synchronized( mutex_lock )
			{
				joinedFederatesByType.remove( federateType );
			}
		}
	}
	
	/**
	 * Utility method to determine whether start requirements have been met yet
	 * 
	 * @return true if the start requirements have been met, false otherwise
	 */
	public boolean canStart()
	{
		synchronized( mutex_lock )
		{
    		for( Entry<String,Integer> x: startRequirements.entrySet() )
    		{
    			String federateType = x.getKey();
    			int minCount = x.getValue();
    			
    			Set<FederateDetails> joined = joinedFederatesByType.get( federateType );
    			if(joined == null || joined.size() < minCount )
    			{
    				return false;
    			}
    		}
		}
		
		return true;
	}

	/**
	 * Utility method which just generates an ASCII-art style table summarizing the requirements
	 * for a federation to achieve the "ready to populate" synchronization point
	 * 
	 * @return an ASCII-art style table summarizing the configured start requirements
	 */
	public String summary()
	{
		StringBuilder summary = new StringBuilder();
		
		// sorted federate types list
		List<String> federateTypes = new ArrayList<>(startRequirements.keySet());
		federateTypes.sort( null );
		
		List<List<Object>> tableContent = new ArrayList<>();
		
		List<Object> row = new ArrayList<Object>();
		row.addAll( Arrays.asList( FedManConstants.TABLE_HEADINGS ) );
		tableContent.add( row );
		
		for( String federateType : federateTypes )
		{
			row = new ArrayList<>();
			row.add(federateType);
			row.add(Integer.toString( startRequirements.get( federateType ) ));
			row.add(Integer.toString( joinedFederatesByType.getOrDefault( federateType, Collections.emptySet() ).size() ));
			tableContent.add( row );
		}
		
		summary.append( StringUtils.makeTable( tableContent ) );

		summary.append( String.format( "\n%d of %d federates have joined.",
		                               joinedCount(), totalFederatesRequired() ) );

		return summary.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

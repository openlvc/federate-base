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
package gov.nist.ucef.hla.base;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

import hla.rti1516e.InteractionClassHandle;

public class MyFederate extends FederateBase {
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static String[] FRUITS = {"Apple", "Banana", "Cherry", "Durian", "Elderberry", "Fig", "Grape",
	                                  "Honeydew", "iFruit", "Jackfruit", "Kiwi", "Lemon", "Mango", 
	                                  "Nectarine", "Orange", "Pear", "Quaggleberry", "Raspberry", 
	                                  "Strawberry", "Tangerine", "Ugli Fruit", "Voavanga", "Watermelon",
	                                  "Xigua", "Yangmei", "Zuchinni"}; 
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MyFederate()
	{
		super();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Lifecycle Callback Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void beforeFederationJoin()
	{
		// no preparation required before federation join
	}

	@Override
	public void beforeReadyToPopulate()
	{
		// allow the user to control when we are ready to populate
		waitForUser("beforeReadyToPopulate() - press ENTER to continue");
	}

	@Override
	public void beforeReadyToRun()
	{
		// allow the user to control when we are ready to run
		// waitForUser("beforeReadyToRun() - press ENTER to continue");
	}

	@Override
	public void beforeFirstStep()
	{
		// no setup required before starting the first step in the simulation
	}
	
	@Override
	public boolean step( double currentTime )
	{
		System.out.println( "sending interaction(s) at time " + currentTime + "..." );
		InteractionClassHandle interactionHandle =
		    rtiamb.getInteractionClassHandle( "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed" );
		new HLAInteraction( interactionHandle, null ).send( null );

		System.out.println( "sending attribute reflection(s) at time " + currentTime + "..." );
		for( HLAObject hlaObject : this.registeredObjects )
		{
			for( String attrName : hlaObject.getAttributeNames() )
			{
				hlaObject.set( attrName, randomFruit() );
			}
			hlaObject.update( null );
		}

		return (currentTime < 10.0);
	}

	@Override
	public void beforeReadyToResign()
	{
		// no cleanup required before resignation
	}

	@Override
	public void beforeExit()
	{
		// no cleanup required before exit
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveObjectRegistration( HLAObject hlaObject )
	{
		System.out.println( "receiveObjectRegistration():" );
		System.out.println( hlaObject.toString() );
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject )
	{
		System.out.println( "receiveAttributeReflection():" );
		System.out.println( hlaObject.toString() );
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject, double time )
	{
		receiveAttributeReflection( hlaObject );
	}
	
	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction)
	{
		System.out.println("receiveInteraction()");
		System.out.println( hlaInteraction.toString() );
	}
	
	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction, double time )
	{
		receiveInteraction(hlaInteraction);
	}
	
	@Override
	public void receiveObjectDeleted( HLAObject hlaObject )
	{
		System.out.println( "receiveObjectDeleted():" );
		System.out.println( hlaObject.toString() );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method simply blocks until the user presses ENTER, allowing for a pause in
	 * proceedings.
	 */
	private void waitForUser( String msg )
	{
		if( !( msg == null || "".equals( msg )) )
		{
			System.out.println( msg );
		}

		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		try
		{
			reader.readLine();
		}
		catch( Exception e )
		{
			System.err.println( "Error while waiting for user input: " + e.getMessage() );
			e.printStackTrace();
		}
	}
	
	/**
	 * This just generates a random string from a list of fruits
	 * 
	 * @return a random fruit name
	 */
	private String randomFruit()
	{
		return FRUITS[ThreadLocalRandom.current().nextInt(FRUITS.length)];
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
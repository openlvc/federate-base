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

public class FederateManager extends FederateBase {
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateManager()
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
		// pre-announce all UCEF synchronization points
		for( UCEFSyncPoint syncPoint : UCEFSyncPoint.values() )
		{
			registerSyncPointAndWaitForAnnounce( syncPoint, null );
		}
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
		return (currentTime < 15.0);
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
		// federate manager does not care about this
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject )
	{
		// federate manager does not care about this
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject, double time )
	{
		// federate manager does not care about this
	}

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction )
	{
		// federate manager does not care about this
	}

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction, double time )
	{
		// federate manager does not care about this
	}

	@Override
	public void receiveObjectDeleted( HLAObject hlaObject )
	{
		// federate manager does not care about this
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Internal Utility Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

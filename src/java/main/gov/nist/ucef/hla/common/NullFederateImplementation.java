/*
 *   Copyright 2018 Calytrix Technologies
 *
 *   This file is part of ucef-java.
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

/**
 * This is an "empty" abstract implementation of the IUCEFFederateImplementation with no-op methods
 * for all required overrides so that developers can simply implement only those methods relevant
 * to their particular federate.
 */
public abstract class NullFederateImplementation implements IFederateImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	// LIFECYCLE MAINTENANCE CALLBACKS /////////////////////////
	@Override
	public void doInitialisationTasks() {}
	
	// announce READY_TO_POPULATE
	@Override
	public void doPostAnnouncePreAchievePopulateTasks() {}
	// achieve READY_TO_POPULATE
	@Override
	public void doPopulationTasks() {}
	
	// announce READY_TO_RUN
	@Override
	public void doPostAnnouncePreAchieveRunTasks() {}
	// achieve READY_TO_RUN
	@Override
	public boolean shouldContinueSimulation() { return false; }
	@Override
	public void tickSimulation() {}
	@Override
	public double getTimeStep() {return 1.0;}
	// announce READY_TO_RESIGN
	@Override
	public void doPostAnnouncePreAchieveResignTasks() {}
	// achieve READY_TO_RESIGN
	@Override
	public void doResignTasks() {}
	
	@Override
	public void doShutdownTasks() {}

	// EVENT HANDLING CALLBACKS ////////////////////////////////
	@Override
	public void handleInteractionSubscription( InteractionBase interactionBase ) {}
	@Override
	public void handleAttributeSubscription( InstanceBase objectBase ) {}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

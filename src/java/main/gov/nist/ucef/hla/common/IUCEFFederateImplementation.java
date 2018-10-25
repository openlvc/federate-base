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

public interface IUCEFFederateImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	// LIFECYCLE MAINTENANCE CALLBACKS /////////////////////////
	// 0. START
	public void doInitialisationTasks();
	// 1. announce READY_TO_POPULATE
	public void doPostAnnouncePreAchievePopulateTasks();
	// 2. achieve READY_TO_POPULATE
	public void doPopulationTasks();
	// 3. announce READY_TO_RUN
	public void doPostAnnouncePreAchieveRunTasks();
	// 4. achieve READY_TO_RUN
	public void runSimulation();
	// 5. announce READY_TO_RESIGN
	public void doPostAnnouncePreAchieveResignTasks();
	// 6. achieve READY_TO_RESIGN
	public void doResignTasks();
	// 7. achieve READY_TO_RESIGN
	public void doShutdownTasks();
	// 8. EXIT

	// EVENT HANDLING CALLBACKS ////////////////////////////////
	public void handleInteraction( InteractionBase interactionBase );
	public void handleReflection( ObjectBase objectBase );
}

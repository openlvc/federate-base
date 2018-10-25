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

public class NullUCEFFederateImplementation implements IUCEFFederateImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
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
	public void runSimulation() {}
	
	// announce READY_TO_RESIGN
	@Override
	public void doPostAnnouncePreAchieveResignTasks() {}
	// achieve READY_TO_RESIGN
	@Override
	public void doResignTasks() {}
	
	@Override
	public void doShutdownTasks() {}

	
	
	
	
	
	@Override
	public void handleInteraction( InteractionBase interactionBase ) {}
	@Override
	public void handleReflection( ObjectBase objectBase ) {}
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

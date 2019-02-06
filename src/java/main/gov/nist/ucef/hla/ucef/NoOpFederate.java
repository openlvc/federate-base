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
package gov.nist.ucef.hla.ucef;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.base.HLAObject;
import gov.nist.ucef.hla.ucef.interaction.c2w.FederateJoin;
import gov.nist.ucef.hla.ucef.interaction.c2w.SimEnd;
import gov.nist.ucef.hla.ucef.interaction.c2w.SimPause;
import gov.nist.ucef.hla.ucef.interaction.c2w.SimResume;

/**
 * An abstract class with all required method implementations in which perform no operation
 * (no-op)
 * 
 * This makes it simpler to create a federate, since only the methods which contain actual
 * functional code need be over-ridden.
 */
public abstract class NoOpFederate extends UCEFFederateBase
{
	//----------------------------------------------------------
	//                   STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public NoOpFederate()
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
	public void beforeFederationJoin() { }

	@Override
	public void beforeReadyToPopulate() { }

	@Override
	public void beforeReadyToRun() { }

	@Override
	public void beforeFirstStep() { }

	@Override
	public void beforeReadyToResign() { }

	@Override
	public void beforeExit() { }

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveObjectRegistration( HLAObject hlaObject ) { }

	@Override
	public void receiveObjectDeleted( HLAObject hlaObject ) { }

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject ) { }

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject, double time ) {
		// delegate to method ignoring time parameter
		receiveAttributeReflection( hlaObject );
	}

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction ) { }

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction, double time ) {
		// delegate to method ignoring time parameter
		receiveInteraction( hlaInteraction );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////// UCEF Sim Control Interaction Callback Methods //////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void receiveSimPause( SimPause simPause ) { }

	@Override
	protected void receiveSimPause( SimPause simPause, double time )
	{
		// delegate to method ignoring time parameter
		receiveSimPause( simPause );
	}

	@Override
	protected void receiveSimResume( SimResume simResume ) { }

	@Override
	protected void receiveSimResume( SimResume simResume, double time )
	{
		// delegate to method ignoring time parameter
		receiveSimResume( simResume );
	}

	@Override
	protected void receiveSimEnd( SimEnd simEnd ) { }

	@Override
	protected void receiveSimEnd( SimEnd simEnd, double time )
	{
		// delegate to method ignoring time parameter
		receiveSimEnd( simEnd );
	}

	@Override
	protected void receiveFederateJoin( FederateJoin federateJoin ) { }
	
	@Override
	protected void receiveFederateJoin( FederateJoin federateJoin, double time ) {
		// delegate to method ignoring time parameter
		receiveFederateJoin( federateJoin );
	}		
}

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
package gov.nist.ucef.hla.example.challenger.base;

import gov.nist.ucef.hla.base.HLAInteraction;
import gov.nist.ucef.hla.example.challenger.interactions.ResponseInteraction;
import gov.nist.ucef.hla.example.challenger.reflections.ChallengeObject;
import gov.nist.ucef.hla.ucef.NoOpFederate;

/**
 *		            ___
 *		          _/   \_     _     _
 *		         / \   / \   / \   / \
 *		        ( U )─( C )─( E )─( F )
 *		         \_/   \_/   \_/   \_/
 *		        <─┴─> <─┴─────┴─────┴─>
 *		       Universal CPS Environment
 *		             for Federation
 * 
 * Example federate for testing
 */
public abstract class _ChallengeFederate extends NoOpFederate
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
	public _ChallengeFederate()
	{
		super();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// RTI Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction )
	{
		String interactionClassName = hlaInteraction.getInteractionClassName();
		if( ResponseInteraction.interactionClassName().equals( interactionClassName ) )
		{
			receiveResponseInteraction( new ResponseInteraction( hlaInteraction ) );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Interaction/Reflection Handler Callbacks ////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handle receipt of a {@link ResponseInteraction}
	 * 
	 * @param interaction the interaction to handle
	 */
	protected abstract void receiveResponseInteraction( ResponseInteraction interaction );

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Internal Utility Methods ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	protected ChallengeObject register( ChallengeObject instance ) { return (ChallengeObject)super.register( instance ); }
}

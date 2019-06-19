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
package gov.nist.hla.genx;

import gov.nist.hla.genx.base._GenxPingFederate;

import gov.nist.hla.genx.interactions.Ping;
import gov.nist.hla.genx.interactions.Pong;
import gov.nist.hla.genx.reflections.Player;

/**
 *                    ___
 *                  _/   \_     _     _
 *                 / \   / \   / \   / \
 *                ( U )─( C )─( E )─( F )
 *                 \_/   \_/   \_/   \_/
 *                <─┴─> <─┴─────┴─────┴─>
 *               Universal CPS Environment
 *                     for Federation
 */
public class GenxPingFederate extends _GenxPingFederate
{
    //----------------------------------------------------------
    //                   STATIC VARIABLES
    //----------------------------------------------------------

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    // published object attribute reflections
    private Player player;

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public GenxPingFederate( String[] args )
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
    public void beforeFirstStep()
    {
        /////////////////////////////////////////////////////////////////
        // INITIALIZE SIMULATION VALUES AS REQUIRED
        /////////////////////////////////////////////////////////////////

        // initialise published object attribute reflections
        this.player = register( new Player() );
    }

    @Override
    public boolean step( double currentTime )
    {
        /////////////////////////////////////////////////////////////////
        // INSERT SIMULATION LOGIC HERE
        /////////////////////////////////////////////////////////////////
        System.out.println( "Tick... " + currentTime );

        // send interactions
        Ping ping = new Ping();
        // set Ping interaction parameter values as required
        // ping.someBoolean( false );
        // ping.someByte( (byte)0 );
        // ping.someChar( '-' );
        // ping.someDouble( 0.0 );
        // ping.someFloat( 0.0F );
        // ping.someInt( 0 );
        // ping.someLong( 0L );
        // ping.someShort( (short)0 );
        // ping.someString( "-" );
        sendInteraction( ping );


        // send object attribute reflections
        // set Player object attribute values as required
        // this.player.someBoolean( false );
        // this.player.someByte( (byte)0 );
        // this.player.someChar( '-' );
        // this.player.someDouble( 0.0 );
        // this.player.someFloat( 0.0F );
        // this.player.someInt( 0 );
        // this.player.someLong( 0L );
        // this.player.someName( "-" );
        // this.player.someShort( (short)0 );
        updateAttributeValues( this.player );

        // return true to continue simulation loop, false to terminate
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Interaction/Reflection Handler Callbacks ////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
    * Handle receipt of a {@link Pong} interaction
    *
    * @param pong the interaction to handle
    */
    @Override
    protected void receivePongInteraction( Pong pong )
    {
        /////////////////////////////////////////////////////////////////
        // INSERT HANDLING HERE
        /////////////////////////////////////////////////////////////////
        System.out.println( "Received Pong interaction" );
        System.out.println( "\tInteraction parameter 'someBoolean' is " + (pong.isSomeBooleanPresent()?pong.someBoolean():"not set") );
        System.out.println( "\tInteraction parameter 'someByte' is " + (pong.isSomeBytePresent()?pong.someByte():"not set") );
        System.out.println( "\tInteraction parameter 'someChar' is " + (pong.isSomeCharPresent()?pong.someChar():"not set") );
        System.out.println( "\tInteraction parameter 'someDouble' is " + (pong.isSomeDoublePresent()?pong.someDouble():"not set") );
        System.out.println( "\tInteraction parameter 'someFloat' is " + (pong.isSomeFloatPresent()?pong.someFloat():"not set") );
        System.out.println( "\tInteraction parameter 'someInt' is " + (pong.isSomeIntPresent()?pong.someInt():"not set") );
        System.out.println( "\tInteraction parameter 'someLong' is " + (pong.isSomeLongPresent()?pong.someLong():"not set") );
        System.out.println( "\tInteraction parameter 'someShort' is " + (pong.isSomeShortPresent()?pong.someShort():"not set") );
        System.out.println( "\tInteraction parameter 'someString' is " + (pong.isSomeStringPresent()?pong.someString():"not set") );
    }

    /**
    * Handle receipt of a {@link Player} object attribute reflection
    *
    * @param player the object attribute reflection to handle
    */
    @Override
    protected void receivePlayerUpdate( Player player )
    {
        /////////////////////////////////////////////////////////////////
        // INSERT HANDLING HERE
        /////////////////////////////////////////////////////////////////
        System.out.println( "Received Player object attribute reflection" );
        this.player.setState( player );
        System.out.println( "\tObject attribute 'someBoolean' is " + (this.player.isSomeBooleanPresent()?this.player.someBoolean():"not set") );
        System.out.println( "\tObject attribute 'someByte' is " + (this.player.isSomeBytePresent()?this.player.someByte():"not set") );
        System.out.println( "\tObject attribute 'someChar' is " + (this.player.isSomeCharPresent()?this.player.someChar():"not set") );
        System.out.println( "\tObject attribute 'someDouble' is " + (this.player.isSomeDoublePresent()?this.player.someDouble():"not set") );
        System.out.println( "\tObject attribute 'someFloat' is " + (this.player.isSomeFloatPresent()?this.player.someFloat():"not set") );
        System.out.println( "\tObject attribute 'someInt' is " + (this.player.isSomeIntPresent()?this.player.someInt():"not set") );
        System.out.println( "\tObject attribute 'someLong' is " + (this.player.isSomeLongPresent()?this.player.someLong():"not set") );
        System.out.println( "\tObject attribute 'someName' is " + (this.player.isSomeNamePresent()?this.player.someName():"not set") );
        System.out.println( "\tObject attribute 'someShort' is " + (this.player.isSomeShortPresent()?this.player.someShort():"not set") );
    }

    //----------------------------------------------------------
    //                     STATIC METHODS
    //----------------------------------------------------------
    /**
     * Main method
     *
     * @param args ignored
     */
    public static void main( String[] args )
    {
        System.out.println( "------------------------------------------------------------------" );
        System.out.println( "GenxPingFederate Federate starting..." );
        System.out.println( "------------------------------------------------------------------" );
        System.out.println();

        try
        {
            GenxPingFederate federate = new GenxPingFederate( args );
            federate.getFederateConfiguration().fromJSON( "config.json" );
            federate.runFederate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.err.println( e.getMessage() );
            System.err.println( "Cannot proceed - shutting down now." );
            System.exit( 1 );
        }

        System.out.println( "Completed - shutting down now." );
        System.exit( 0 );
    }
}

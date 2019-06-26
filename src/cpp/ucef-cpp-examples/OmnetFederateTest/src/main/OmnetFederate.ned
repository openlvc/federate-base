//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see http://www.gnu.org/licenses/.
// 

//
// TODO auto-generated type
//

simple OmnetFederateModule
{
    @display("i=block/join");
    gates:
        input omnet @directIn;
}

simple SimpleModule
{
    gates:
        input in;
        output out;
}

simple RouterModule
{
    gates:
        input in[];
        output out[];
}

network OmnetFederateNet
{
    @display("bgb=350,264");
    submodules:
        OMNeTFed: OmnetFederateModule {
            @display("p=129,193");
        }
        PingA: SimpleModule {
            @display("p=50,71;i=block/rxtx");
        }
        PingB: SimpleModule {
            @display("p=50,171;i=block/rxtx");
        }
        PongA: SimpleModule {
            @display("p=250,71;i=block/rxtx");
        }
        PongB: SimpleModule {
            @display("p=250,171;i=block/rxtx");
        }
        router: RouterModule {
            @display("p=125,100;i=block/rxtx");
        }
    connections allowunconnected:
        PingA.out --> {  delay = 1000ms; } --> router.in++;
        PingB.out --> {  delay = 5000ms; } --> router.in++;
        router.out++ --> {  delay = 1000ms; } --> PongA.in;
        router.out++ --> {  delay = 5000ms; } --> PongB.in;
}

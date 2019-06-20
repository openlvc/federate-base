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

simple OmnetFederateModuleA
{
    @display("i=block/join");
    gates:
        output out;
}

simple OmnetFederateModuleB
{
    @display("i=block/join");
    gates:
        input in;
        output out;
}

simple OmnetFederateModuleC
{
    @display("i=block/join");
    gates:
        input in;
        output out;
}

simple OmnetFederateModuleD
{
    @display("i=block/join");
    gates:
        input in;
}

simple SimpleResponseModule
{
    gates:
        input in;
        output out;
}

network OmnetFederateNet
{
    @display("bgb=297,264");
    submodules:
        omA: OmnetFederateModuleA {
            @display("p=66,163");
        }
        omB: OmnetFederateModuleB {
            @display("p=146,163");
        }
        omC: OmnetFederateModuleC {
            @display("p=226,163");
        }
        omD: OmnetFederateModuleD {
            @display("p=306,163");
        }
        sim: SimpleResponseModule {
            @display("p=106,48;i=block/rxtx");
        }
    connections:
        omA.out --> {  delay = 1000ms; } --> sim.in;
        omB.in <-- {  delay = 1000ms; } <-- sim.out;
        omB.out --> {  delay = 1000ms; } --> omC.in;
        omD.in <-- {  delay = 1000ms; } <-- omC.out;
                
}
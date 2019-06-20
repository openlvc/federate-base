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

#include "OmnetFederateModuleA.h"

using namespace std;

Define_Module(OmnetFederateModuleA);

OmnetFederateModuleA::OmnetFederateModuleA()
{
    fedConfigFile = ".//resources//config//fedConfigA.json";
}

OmnetFederateModuleA::~OmnetFederateModuleA()
{

}

void OmnetFederateModuleA::beforeReadyToPopulate()
{
    cout << "Before ready to populate." << endl;
    //pressEnterToContinue();
}

void OmnetFederateModuleA::beforeReadyToRun()
{
    cout << "Before ready to run." << endl;
    //pressEnterToContinue();
}

void OmnetFederateModuleA::beforeFirstStep()
{
    cout << "Before first step." << endl;
    //pressEnterToContinue();
}

void OmnetFederateModuleA::beforeReadyToResign()
{
    cout << "Before ready to resign." << endl;
    //pressEnterToContinue();
}

void OmnetFederateModuleA::beforeExit()
{
    cout << "Before exit." << endl;
    //pressEnterToContinue();
}

/*void OmnetFederateModuleA::pressEnterToContinue()
{
    do
    {
        cout << '\n' << "Press ENTER to continue...";
    } while (cin.get() != '\n');
}*/

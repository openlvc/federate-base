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

#include "OmnetFederateModuleD.h"

using namespace std;

Define_Module(OmnetFederateModuleD);

OmnetFederateModuleD::OmnetFederateModuleD()
{
    fedConfigFile = ".//resources//config//fedConfigD.json";
}

OmnetFederateModuleD::~OmnetFederateModuleD()
{

}

void OmnetFederateModuleD::beforeReadyToPopulate()
{
    cout << "Before ready to populate." << endl;
    //pressEnterToContinue();
}

void OmnetFederateModuleD::beforeReadyToRun()
{
    cout << "Before ready to run." << endl;
    //pressEnterToContinue();
}

void OmnetFederateModuleD::beforeFirstStep()
{
    cout << "Before first step." << endl;
    //pressEnterToContinue();
}

void OmnetFederateModuleD::beforeReadyToResign()
{
    cout << "Before ready to resign." << endl;
    //pressEnterToContinue();
}

void OmnetFederateModuleD::beforeExit()
{
    cout << "Before exit." << endl;
    //pressEnterToContinue();
}

/*void OmnetFederateModuleD::pressEnterToContinue()
{
    do
    {
        cout << '\n' << "Press ENTER to continue...";
    } while (cin.get() != '\n');
}*/

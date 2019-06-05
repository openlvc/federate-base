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

#ifndef OMNETFEDERATEMODULE_H_
#define OMNETFEDERATEMODULE_H_

#include "OmnetFederate.h"

#include <iostream>
#include <list>
#include <memory>
#include <omnetpp.h>

#include "FedData.h"

class OmnetFederateModule : public base::ucef::omnet::OmnetFederate
{

public:
    OmnetFederateModule();

    virtual ~OmnetFederateModule();

    //----------------------------------------------------------
    //                Federate Methods
    //----------------------------------------------------------

    /**
     * Get called just before announcing and achieving the 'READY_TO_POPULATE'
     * synchronization point
     */
    virtual void beforeReadyToPopulate() override;

    /**
     * Get called just before announcing and achieving the 'READY_TO_RUN'
     * synchronization point
     */
    virtual void beforeReadyToRun() override;

    /**
     * Get called just before entering the main update loop
     */
    virtual void beforeFirstStep() override;

    /**
     * Get called just before announcing and achieving the 'READY_TO_RESIGN'
     * synchronization point
     */
    virtual void beforeReadyToResign() override;

    /**
     * Get called just before resigning from the federation
     */
    virtual void beforeExit() override;

    /**
     * Get called whenever RTI receives a object class update
     */
    virtual void receivedAttributeReflection( std::shared_ptr<const base::HLAObject> hlaObject,
                                              double federateTime ) override;

    /**
     * Get called whenever RTI receives a new object interaction
     */
    virtual void receivedInteraction( std::shared_ptr<const base::HLAInteraction> hlaInt,
                                      double federateTime ) override;

    /**
     * Step function of this federate
     */
    virtual bool step( double federateTime ) override;

    //----------------------------------------------------------
    //                IOmnetFederate Methods
    //----------------------------------------------------------

    /**
     * Get called to initialize Omnet modules
     */
    virtual void initModule() override;

    /**
     * Get called when received Omnet message
     */
    virtual void handleNetMessage( omnetpp::cMessage *msg ) override;

    /**
     * Get called to teardown Omnet modules
     */
    virtual void tearDownModule() override;

private:
    //Response solveChallenge( Challenge &receievedChallenge );
    void pressEnterToContinue();

private:
    std::list<std::shared_ptr<const base::HLAInteraction>> remoteChallenges;
    std::list<std::shared_ptr<base::HLAInteraction>> challengeReply;
    omnetpp::cMessage *timerMessage;
    bool canProcess;
};

#endif /* OMNETFEDERATEMODULE_H */



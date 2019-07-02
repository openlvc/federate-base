# Overview
This contains the C++ version of UCEF federate implementation

 - ucef-cpp-core
	 - A simplified programing interface to work with HLA1516e
 - ucef-cpp-examples
	 - A Challenge-Response example federate
	 - An example OmnetFederate that demonstrates communication co-simulation using Ping and Pong federates
 - ucef-omnet
	 - A gateway that allows federates to use OMNeT++ simulation for communication co-simulation
# Quick Start

## Eclipse CDT
- To work with `FederateBase` in `ucef-cpp-core`,  and example federates named `ChallengeFederate`, `ResponseFederate`, `PingFederate` and `PongFederate`  in `ucef-cpp-examples`, import the projects into Eclipse-CDT using `General` ►`Existing Projects into Workspace`.

## Omnet++ IDE
- To work with `OmnetFederate` in `ucef-omnet`,  and an example omnet federate named `OmnetFederateTest` in `ucef-cpp-examples`, import the projects into Omnet++IDE using `General` ►`Existing Projects into Workspace`.

## Command Line

- From the command line `cd` to the root CPP folder and run `mvn clean install`
- This will compile and install `FederateBase` and `OmnetFederate` in local maven repository

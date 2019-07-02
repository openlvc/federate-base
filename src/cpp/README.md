# Overview
This contains the C++ version of UCEF federate implementation

 - ucef-cpp-core
	 - A simplified programing interface to work with HLA1516e
 - ucef-cpp-examples
	 - Challenge-Response example federate
	 - Example OmnetFederate that demonstrates communication co-simulation using Ping and Pong federates
 - ucef-omnet
	 - A gateway that allows HLAInteractions to flow through OMNeT++ simulation
# Quick Start

## Eclipse CDT
- To work with FederateBase in `ucef-cpp-core`,  and example federates in  `ucef-cpp-examples` named ChallengeFederate, ResponseFederate, PingFederate and PongFederate, import the projects into Eclipse-CDT using `General` ►`Existing Projects into Workspace`.

## Omnet++ IDE
- To work with OmnetFederate in `ucef-omnet`,  and example omnet federate in  `ucef-cpp-examples` named OmnetFederateTest, import the projects into Omnet++IDE using `General` ►`Existing Projects into Workspace`.

## Command Line

- From the command line `cd` to the root CPP folder and run
- `> mvn clean install`
- This will compile and install `FederateBase` and `OmnetFederate` in local maven repository

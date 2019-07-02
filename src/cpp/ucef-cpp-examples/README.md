

# Overview  
This contains two main examples to demonstrate the use of FederateBase to create custom federates and OMNeT++ for communication co-simulation.  
  
# Quick Start  
  
## Challenge-Response example federate  
  
In this example, Challenge federate is configured to generate 20 challenges that have random strings and sub-string indexes, and to validate the responses received from Response federate.  
  
The Response federate must reply to the received challenges by computing the correct sub-string.  

  ### Setting-up
- If you haven't, run `mvn clean install` in root cpp directory  
- Follow the below instructions and bring up `Challenge` and `Response` federates respectively  
- Once both of them are waiting at `beforeReadyToPopulate` synch point, press &#x23CE; in each console alternatively until they progress into the execution phase  
  
#### Running Challenge federate  
- Navigate to `ChallengeFederate`  
- Run `.\challenge.sh clean`  
- Run `.\challenge.sh compile`  
- Run `.\challenge.sh execute`  
- This will bring`ChallengeFederate` up and wait at the `beforeReadyToPopulate` synch point for user input  
  
#### Running Response federate  
- Open a new console and navigate to `ResponseFederate`  
- Run `.\response.sh clean`  
- Run `.\response.sh compile`  
- Run `.\response.sh execute`  
- This will bring`Responsefederate` up and wait at the `beforeReadyToPopulate` synch point for user input  
  
## OMNeT++ Communication Co-Simulation example  
  
The main purpose of this example is to demonstrate the communication co-simulation capability of federates using OMNet++ network simulator. Here, `PingFederate` is configured to send `Ping` interactions to a designated `PongFederate` using OMNet++ network simulator and `PongFederate` will reply to each `Ping` with a `Pong` via RTI.  
  
### Setting-up
- If you haven't, run `mvn clean install` in root cpp directory  
- Follow the below instructions to bring up `OmnetFederateTest`, `PongFederate` and `PingFederate` federates respectively.  
- When all three federates are running,   `Ping` messages generated from  `PingFederate` will be routed via OMNeT++ simulator and `PingFederate`  will receive `Pong` replies from the `PongFederate` via RTI.  
  
#### Running OMNeT++ federate  
- Navigate to `OmnetFederateTest`  
- Run `.\omnetFederateTest.sh clean`  
- Run `.\omnetFederateTest.sh compile`  
- Run `.\omnetFederateTest.sh execute`  
- This will bring`omnetFederateTest` up and when federate is in execution phase (OMNeT++ simulator UI is not frozen) hit `run` in the UI.  
  
#### Running Pong federate  
- Navigate to `PongFederate`  
- Run `.\pong.sh clean`  
- Run `.\pong.sh compile`  
- Run `.\pong.sh execute`  
- This will bring`PongFederate` up and will wait at the main execution untill it receives a `ping` interaction  
-  
#### Running Ping federate  
- Navigate to `PingFederate`  
- Run `.\ping.sh clean`  
- Run `.\ping.sh compile`  
- Run `.\ping.sh execute`  
- This will bring`PingFederate` up and will send the first `Ping` interaction  
  
# Federate configuration file  
  
A federate configuration file can be used to configure federates and a  sample configuration file is provided below. In the given JSON configuration, `omnet` attribute is optional and only need to specify if a federate wants to route certain interactions via OMNeT++ simulator.  
  
```json  
{  
 "logLevel":"info",  
 "federateName":"CppPingFederate",  
 "federateType":"CppPingFederate",  
 "federationExecName":"PingPongOmnetFederation",  
 "canCreateFederation":true,  
 "maxJoinAttempts":2,  
 "joinRetryIntervalSec":5,  
 "syncBeforeResign":true,  
 "callbacksAreImmediate":true,  
 "lookAhead":0.2,  
 "stepSize":1,  
 "timeConstrained":false,  
 "timeRegulated":true,  
 "baseFomPaths":["resources//ObjectModels//fom//UCEF-EXP-PINGPONG.xml"],  
  
 "joinFomPaths":[ ],  
 "somPaths":["resources//ObjectModels//som//UCEF-EXP-PING.xml"],  
  
 "omnet":{  
	"sourceHost":"PingB",  
	"networkInteractionName":"HLAinteractionRoot.NetworkInteraction",  
	"interactions":["HLAinteractionRoot.C2WInteractionRoot.*"]  
	}  
}  
```  
  
# Simulation configuration file  
This configuration is required to configure the routing of the interactions via OMNeT++ simulator. This is only needed by the `OmnetFederate` and it's relative path is currently fixed at `//resources//config//omnetSimConfig.json`. A sample configuration is provided below.  
  
```json  
{  
 "config":
     [  
	{  
	"sourceHosts":["PingA", "PingB"],  
	"interactions":["HLAinteractionRoot.C2WInteractionRoot.*"],  
	"destinations":[{ "host": "PongA", "app": "UDPApp1"}, { "host": "PongB", "app": "UDPApp2"}]  
	},  
	{  
	"sourceHosts":["PingB"],  
	"interactions":	["HLAinteractionRoot.C2WInteractionRoot.*"],  
	"destinations":[{ "host": "PongA", "app": "UDPApp1"}]  
	}  
     ]  
}  
```

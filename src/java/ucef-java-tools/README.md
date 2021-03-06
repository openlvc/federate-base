# Overview

This project contains tools for use in running federations.

Currently the only tool is the "Federation Manager", which is detailed below.

# Federation Manager

The Federation Manager is actually just a UCEF federate whose purpose
it is to…
- initially *create* a federation (it is generally the only federate 
  which should be allowed to do so)
- monitor joining federates to determine whether that certain conditions
  are met which will allow the start of a simulation run
- control the stepping forward of time in the federation
- issue `SimPause`, `SimResume` and `SimEnd` control interactions; well behaved
  UCEF federates behave in accordance with this interaction signaling system.

It also provides simple keyboard controls and a REST-like interface via a 
basic HTTP service as control mechanisms.

## Quick Start

The Federation Manager can be run using Maven:

> **NOTE:** The first run of these may take some time as Maven retrieves required resources.

You can use the command line directly, like so:
```
mvn exec:java -Dexec.mainClass="gov.nist.ucef.hla.tools.fedman.FederationManager" -Dexec.args="…"
```
The `-Dexec.args` section should, of course, be filled with command line arguments as required. 

There are also utility batch and shell script file provided to make this somewhat simpler - for 
example…
```
feredation-manager.bat --federationName TheFederation --require FedABC,1 --require FedXYZ,2
```
…on Windows, or…
```
feredation-manager.sh --federationName TheFederation --require FedABC,1 --require FedXYZ,2
```
…on *nix systems.

Configuration options are detailed in the following section.

## Configuration

The Federation Manager may be configured via command line options, a JSON formatted
configuration file, or a combination of both.

If a JSON configuration file specified, its content is processed first, and 
then any command line arguments supplied will act as overrides.

This makes it possible, for example to start with a "general" JSON configuration 
file containing default values, and tweak individual simulation runs with command 
line arguments as required.

> **NOTE:** At a minumum, You *must* configure…
 - a federation name and
 - simulation start requirements
   
 …to start the Federation Manager. If either of these are absent, the
 Federation Manager will exit. 

### Command Line Configuration

 - `--config <file>` : Set the location of the JSON configuration file for the
   Federation Manager to use. Values specified in this file will be overridden
   by any corresponding command line argument values provided.
 - `-r`,`--require <FEDERATE_TYPE,COUNT>`: Define required federate types
   and minimum counts. For example, `-r FedABC,2` would require at least two 
   'FedABC' type federates to join. Multiple requirements can be specified
   by repeated use of this option.
 - `-f`, `--federationExecName <federation name>`: Set the name of the federation
   the Federation Manager will join.
 - `--fedmanName <federate name>`: Set the federate name for the Federation 
   Manager to use. If unspecified a value of 'FederationManager' will be used.
 - `--fedmanType <federate type>`: Set the federate type for the Federation
   Manager to use. If unspecified a value of 'FederationManager' will be used.
 - `--maxTime <max>`: Set the maximum logical time to which the simulation 
   may run. If unspecified the simulation will run indefinitely.
 - `--logicalSecond`: Define a 'logical second'; the logical step size which
   equates to a real-time second. If unspecified a value of 1.00 will be used.
 - `--logicalGranularity`: Define the number of steps per logical second. If 
   unspecified a value of 1 will be used.
 - `--realtimeMultiplier`: Define the simulation rate. 1.0 is real time, 0.5
   is half speed, 2.0 is double speed, and so on. If unspecified a value of
   1.00 will be used.
 - `--httpPort <port>`: Specify the port to provide the HTTP service on. Only
   relevant if the HTTP service is active (see also `--noHttpService`). If
   unspecified, port 8888 will be used.
 - `--noHttpService`: Turn off the HTTP service which provides REST-like 
   endpoints to control the Federation Manager. If unspecified the HTTP
   service *will* be active by default (see also `--httpPort`).
 - `-h`,`--help`: print help and usage text, and then exits

The available command line arguments can be obtained using the `--help` or `-h`
switch when starting the Federation Manager:

-f PingPongFederation -r PingFederate,1 -r PongFederate,1  

### JSON Configuration

The JSON configuration keys are basically the "long" command line options with the
leading `--` removed.

The few exceptions are:
 - the `help` option is not available in the JSON
 - the `config` option is not available in the JSON
 - rather than specificying multiple `require`ments as one would when using the 
   command line, the JSON `require` option occurs just once, and expects a JSON
   array consisting of JSON objects which have the following form: 
   `{"type":FEDERATE_TYPE, "count": COUNT}`.
 - the `noHttpService` switch is not available in the JSON. Instead use the
   `withHttpService` key, which takes a boolean `true` or `false` as its value.
 - The shortcut options (such as `r` for `require` and `f` for `federationName`) may
   not be used in the JSON configuration - only the "long" options may be used.

Some other items of note:
 - JSON recognizes text, numeric and boolean types.   
    -- text items must be quoted  
    -- numeric and boolean items must *not* be quoted  

An example JSON configuration file content is shown below - all available options
are exercised in this example:

```json
{
    "fedmanName":            "FederationManager",
    "fedmanType":            "FederationManager",
    "federationExecName":    "ManagedFederation",
    "require":
    [
        {"type":"FederateABC", "count":1},
        {"type":"FederateXYZ", "count":2}
    ],
    "maxTime":            999999.0,
    "logicalSecond":      1.0,
    "logicalGranularity": 1,
    "realtimeMultiplier": 1.0,
    "withHttpService":    true,
    "httpPort":           8888
}
```

## Keyboard Controls
Once the start conditions have been met, the Federation Manager provides for simple
keyboard control:

 - pressing the `ENTER` key or `s` followed by `ENTER` will start the simulation
 - pressing `p` or `SPACE` followed by the `ENTER` key once the simulation has started
   will toggle pause/resume of the simulation.    
 - pressing `x` or `q` followed by the `ENTER` key once the simulation has started
   will end the simulation.    

## REST-like HTTP Service
By default, the Federation Manager will start a simple HTTP service on port 8888
of the host running the Federation Manager.

The port can be changed via the `httpPort` option, and the service can be disabled
entirely using the `noHttpService` command line switch, or specifying
`"withHttpService":false` in the JSON configuration.

Once the service has started, you can provide GET and POST requests to various
endpoints to query state and issue commands for the Federation Manager.

If you simply point a browser at port 8888 (or your `httpPort` configured port),
an web page will be displayed showing all available endpoints in a table, as well as 
the types of request method(s) accepted by each endpoint.

You can simply click on the endpoints in the table to GET or POST to them, 
allowing Federation Manager to be queried and controlled directly from this
web page.

> **CAUTION:** There is no security or authentication applied here, so *anyone* can 
 access this page, provided the network topology allows for it.

Currently available endpoints are:

 - `/ping` (GET)
 - `/query/status` (GET)
 - `/query/start-conditions` (GET)
 - `/query/can-start` (GET)
 - `/query/has-started` (GET)
 - `/query/has-ended` (GET)
 - `/query/is-paused` (GET)
 - `/query/is-running` (GET)
 - `/query/is-waiting-for-federates` (GET)
 - `/command/start` (POST)
 - `/command/pause` (POST)
 - `/command/resume` (POST)
 - `/command/end` (POST)

The endpoints provide a simple "text only" response by default, apart from 
`/query/start-conditions` which renders an HTML table showing the start 
conditions and current status by default.

Adding the query parameter `?json=true` will cause a richer, machine readable
JSON response to be supplied, which may be more useful in some situations. For
example…

 - `/ping/`: `UCEF Federation Manager`
 - `/ping/?json=true`: `{"path":"ping","response":"UCEF Federation Manager","query":{"json":"true"},"timestamp":1561100734100}`



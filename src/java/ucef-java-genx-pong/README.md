# Overview

This project represents the intended "target" or "final output" from a
WebGME project.

Its only intended purpose is to provide a "finish line" or "definition
of done" for validating WebGME modifications.

As such, it is somewhat temporary, and may be removed in the final
deliverables.

## Quick Start

A utility batch and shell script file are provided to make startup simple 
example...

```
run-pong-federate.bat
```
...on Windows, or...
```
run-pong-federate.sh
```
...on *nix systems.

The script will query the Federation Manager's HTTP service to determine
when it is ready to accept federate joins, and then join the federation
automatically.

For further details, please inspect the contents of the script files.

# Breakdown

## src/main/java

The Java source files for the federate.

### `_federate_name_here_Federate.java`

At the base of the project is a single Java source file which represents
the `main` federate class. It contains the elementary logic and 
implementation needed for the federate to function in a federation.

Areas for the developer to implement are marked as such by (hopefully)
obvious comment blocks declaring them to be such.

Also note that only the "essential" method implementations are generated
as "guidance" for the developer to begin implenentation of the federate.

It is up to the developer to override or implement other methods
in order to provide more complex behaviours. 

There is something of a balancing act here, in that generating less 
code could appear "mysterious", and generating too much code would make 
it hard for a developer (especially one unfamiliar with HLA) to identify
where to begin. It is hoped that the the correct balance has been struck.

### `base` package

Contains a "helper" class which the `GenXPingFederate` extends, and
provides implementation for receiving interactions and attribute 
reflections, identifying their type, converting the to a concrete
implementation and redirecting them to the appropriate handler
function.

It also "enforces" the implementation of the handler functions by
declaring abstract implementations of them (they are implemented in
the `main` federate class - see above). 

### `interactions` package

Contains implementations of the interactions which are published and/or
subscribed to by the `main` federate.

### `reflections` package

Contains implementations of the attribute reflections which are 
published and/or subscribed to by the `main` federate.

## src/main/resources

The configuration and other resource files required by the federate.

### `config.json`

A JSON formatted file containing configuration details required by 
the federate for normal operations.

### `fom-base.xml`

An XML file representing the base FOM common to *all* federates 
participating in a managed UCEF federation. This is used during
federation creation.

**NOTE** by default, UCEF federates are not permitted to create
federations, so this configuration item is, generally speaking,
not actually used.

### `fom-join.xml`

An XML file representing the FOM specific to *this* particular
federate. This is used during federation joining.

### `som.xml`

An XML file representing the SOM specific to *this* particular
federate. This is used during initialization to configure
interaction and attribute reflection publications and
subscriptions.

### `log4j.xml`

An XML file for configuring the Apache Log4J logging output.
For more information on the content of this file, please refer
to the 
[Apache Log4J documentation](https://logging.apache.org/log4j/2.x/manual/index.html)


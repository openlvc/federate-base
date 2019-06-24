# Examples

The examples in this project show various approaches to creating HLA federates

All are intended to be run as managed federations with the Federation Manager.

In order of complexity they are:

## `base`
An example of Ping/Pong federates created using only the classes in `gov.nist.ucef.hla.base`.

These require the most implementation work on the part of the developer.

## `ucef`
An example of Ping/Pong federates extending from the `UCEFFederateBase` class available in 
`gov.nist.ucef.hla.ucef`.

These reqiure less implementation work on the part of the developer, but provide much of the 
boilerplate code.

## `noopbase`
An example of Ping/Pong federates extending from the `NoOpFederate` class available in 
`gov.nist.ucef.hla.ucef`.

Since there are "no-operation" method implementations (hence, `NoOpFederate`) for all the methods 
you would otherwise have to implement if extending from the `UCEFFederateBase`, this requires the
least initial work.

## `challenger`

An implementation of the Challenge/Response federates.

This is used to test cross compatibility of message exchange between Java and C++ federates.

The "challenge" federate creates an interaction containing details of a simple problem to be solved
which is sent out and received by the "response" federate.

The response federate must then solve the challenge, and send back the appropriate response.

If the challenger receives the correct response to it's challenge, the challenge is passed (the
expected outcome).

The underlying challenge is extremely simple - a random string is created, and a random character
index within the random string is selected.

These details are sent to the response federate, which must send back the an appropriate substring
based on the original string and character index contained in the challenge.

The challenger checks for the correct substring, and confirms that the challenge has been passed.

Running the example is fairly simepl on both *nix and Windows environments.
### *nix
Open three terminal windows:
 - In one console, enter the `ucef-java-tools` folder, and start the Federation Manager as follows:
```
federation-manager.sh --config ../ucef-java-examples/src/main/resources/challenger/fedman-config.json
```
 - In the other two consoles, enter the `ucef-java-examples` folder, and start the challenge and
   response federates as follows:
```
run-challenge-federate.sh
```
```
run-response-federate.sh
```

When you see the following output in the Federation Manager terminal…
```
2 of the 2 required federates have joined.
Start requirements met - we are now Ready to Populate.
Waiting for SimStart command...
```
…press `ENTER` to begin. You should see the challenger federate sending challenges and receiving 
reponses, and the responder federate receiving challenges and sending its responses. At the end of the
run, depending on how many challenges were sent, you should see output like the following:

```
'Before ready to resign' hook
Total challenges sent          : 10
Pass count                     : 10
Failed count                   : 0
---------------------------------------------
'Before exit' hook
Completed - shutting down now.
```

### Windows
Open three command prompts:
 - In one console, enter the `ucef-java-tools` folder, and start the Federation Manager as follows:
```
federation-manager.bat --config ..\ucef-java-examples\src\main\resources\challenger\fedman-config.json
```
 - In the other two consoles, enter the `ucef-java-examples` folder, and start the challenge and
   response federates as follows:
```
run-challenge-federate.bat
```
```
run-response-federate.bat
```

When you see the following output in the Federation Manager console…
```
2 of the 2 required federates have joined.
Start requirements met - we are now Ready to Populate.
Waiting for SimStart command...
```
…press `ENTER` to begin. You should see the challenger federate sending challenges and receiving 
reponses, and the responder federate receiving challenges and sending its responses. At the end of the
run, depending on how many challenges were sent, you should see output like the following:

```
'Before ready to resign' hook
Total challenges sent          : 10
Pass count                     : 10
Failed count                   : 0
---------------------------------------------
'Before exit' hook
Completed - shutting down now.
```

### Additional Notes:
The configuration file for the Federation Manager for this example limits the maximum simulation time to
`20.0`. Once this time is reached, the simulation exit signal is sent out by the Federation Manager. Any
federates which have not yet exited will terminate when they receive this signal.

This will limit the number of challenges which can be sent - if you wish to send more challenges, be sure
to update the `maxTime` configuration value in the referenced `fedman-config.json` file appropriately.

Since one challenge is sent and answered per `1.0` logical time unit, simply allow at least `1.0` units 
of time per challenge. For example, to send 20 challenges, set `maxTime` to something like `25.0`. 

The default configuration files for the `ChallengeFederate` and `ResponseFederate` are 
`challenge-config.json` and `response-config.json` respectively. This can be changed
using the `--config` command line option. For example:
```
run-challenge-federate.bat --config my/config/file.json
```
By default the challenger is configured to issue ten challenges and then exit,
but this can be changed by using the `--iterations` command line option, or 
the `iterations` key in the JSON configuration. For example:  
```
run-challenge-federate.bat --iterations 15
```
…or in the JSON…
```
{
    "other": "config items here…",

    "iterations":            15
}
```
The command line value will take precedence if both configuration options are used. Also refer to the
note about the Federation Manager's `maxTime` setting if you wish to issue large numbers of challenges.


## Others

The other examples are not intended to be kept, but demonstrate som aspect
of federate behaviour (principally for testing purposes).

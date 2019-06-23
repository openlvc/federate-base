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

Open three consoles:
 - In one console, enter the `ucef-java-tools` folder, and start the Federation Manager as follows:
```
federation-manager.sh --config ..\ucef-java-examples\src\main\resources\challenger\fedman-config.json
```
 - In the other two consoles, enter the `ucef-java-examples` folder, and start the challenge and
   response federates as follows:
```
run-challenge-federate.sh
```
```
run-response-federate.sh
```

On Windows, use the `*.bat` file equivalents instead - that is `federation-manager.sh`,
`run-challenge-federate.sh`, and `run-response-federate.sh`.

### Additional Notes:
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
...or in the JSON...
```
{
    "other": "config items here...",

    "iterations":            15
}
```
The command line value will take precedence if both configuration options are used.

## Others

The other examples are not intended to be kept, but demonstrate som aspect
of federate behaviour (principally for testing purposes).

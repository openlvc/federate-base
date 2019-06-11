# Overview
This is the parent module of the Maven multi-module project encapsulating
 - ucef-java-core
 - ucef-java-examples
 - ucef-java-tools

# Quick Start

## Eclipse

- Import the project as a new Maven project
- Build all projects by right clicking on the `pom.xml` under the `ucef-java-all-modules` sub-project and select `Run as...` ► `Maven build` and entering `clean verify` for the `Goals` option.
- Individual Java targets can be run by right-clicking on the `main` class and selecting `Run as...` ► `Java Application`

### Troublshooting
If you see an error message such as…
```
[ERROR] No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?
```
...when attempting to build, ensure that the active installed JRE under `Window` ► `Preferences` ► `Java` ► `Installed JREs` is pointing to a specific *JDK* rather than a *JRE*.

## Command Line

- Install Maven (http://maven.apache.org) if it has not already been installed.
- From the command line `cd` to the root folder (i.e., the folder that this `README.md` is in)
- `> mvn clean verify install`
- Once the initial build sequence has completed, `cd` into one of the sub-folders, such as `ucef-java-tools`
- Run the main method with `mvn exec:java -Dexec.mainClass="gov.nist.ucef.hla.tools.fedman.FederationManager" -Dexec.args="--help"`

### Troublshooting
If you see an error message such as…
```
[ERROR] No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?
```
...when running Maven commands, ensure that the `JAVA_HOME` environment variable is pointing to a specific *JDK* (**not** a *JRE*).




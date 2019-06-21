#!/bin/bash
function join_by { local d=$1; shift; echo -n "$1"; shift; printf "%s" "${@/#/$d}"; }

MVN=`which mvn`

if [ -z "$MVN" ]
then
    echo "The `mvn` (Maven) application could not be found. Ensure it is installed and placed in your PATH."
    exit
else
    ARGS=$(join_by ' ' $@)
    $MVN exec:java -Dexec.mainClass="gov.nist.ucef.hla.tools.fedman.FederationManager" -Dexec.args="$ARGS"
fi

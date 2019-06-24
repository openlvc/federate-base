#!/bin/bash
JAVA_MAIN_CLASS="gov.nist.ucef.hla.example.challenger.ChallengeFederate"
FEDMAN_HOST=localhost
FEDMAN_PORT=8888

# utility function used to concatenate the list of command line arguments
# to this bash script into a space delimited string
function join_by { local d=$1; shift; echo -n "$1"; shift; printf "%s" "${@/#/$d}"; }

# verify Maven exists
MVN=`which mvn`
if [ -z "$MVN" ]
then
    echo "The `mvn` (Maven) application could not be found. Ensure it is installed and placed in your PATH."
    exit
fi

# verify curl exists
CURL=`which curl`
if [ -z "$CURL" ]
then
    echo "The `curl` application could not be found. Ensure it is installed and placed in your PATH."
    exit
fi

# periodically check on the federation manager's REST-like endpoints on 
# its HTTP service to determine when it's a good time to start
CURL_RESPONSE=""
while [ "$CURL_RESPONSE" != "true" ]
do
    CURL_RESPONSE=$($CURL -s http://$FEDMAN_HOST:$FEDMAN_PORT/query/is-waiting-for-federates/)
    if [ "$CURL_RESPONSE" != "true" ]
    then
        echo "The Federation Manager does not seem to be ready on $FEDMAN_HOST:$FEDMAN_PORT yet..."
        sleep 5s
    fi
done

$MVN exec:java -Dexec.mainClass="$JAVA_MAIN_CLASS" -Dexec.args="$ARGS"

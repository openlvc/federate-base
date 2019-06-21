#!/bin/bash
JAVA_MAIN_CLASS="gov.nist.hla.genx.GenxPingFederate"
HTTP_HOST=localhost
HTTP_PORT=8888

function join_by { local d=$1; shift; echo -n "$1"; shift; printf "%s" "${@/#/$d}"; }

MVN=`which mvn`
if [ -z "$MVN" ]
then
    echo "The `mvn` (Maven) application could not be found. Ensure it is installed and placed in your PATH."
    exit
fi

CURL=`which curl`
if [ -z "$CURL" ]
then
    echo "The `curl` application could not be found. Ensure it is installed and placed in your PATH."
    exit
fi


CURL_RESPONSE=""
while [ "$CURL_RESPONSE" != "true" ]
do
    # 'ping' the HTTP server on the federation manager
    # to determine when it's a good time to start
    CURL_RESPONSE=$($CURL -s http://$HTTP_HOST:$HTTP_PORT/query/is-waiting-for-federates/)
    if [ "$CURL_RESPONSE" != "true" ]
    then
        echo "The Federation Manager does not seem to be ready yet..."
        sleep 5s
    fi
done

ARGS=$(join_by ' ' $@)
$MVN exec:java -Dexec.mainClass="$JAVA_MAIN_CLASS" -Dexec.args="$ARGS"

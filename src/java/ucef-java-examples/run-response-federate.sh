#!/bin/bash
JAVA_MAIN_CLASS="gov.nist.hla.example.challenger.ResponseFederate"
CONFIG="response-config.json"
HTTP_HOST=localhost
HTTP_PORT=8888

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
    CURL_RESPONSE=$($CURL -s http://$HTTP_HOST:$HTTP_PORT/query/is-waiting-for-federates/)
    if [ "$CURL_RESPONSE" != "true" ]
    then
        echo "The Federation Manager does not seem to be ready yet..."
        sleep 5s
    fi
done

$MVN exec:java -Dexec.mainClass="$JAVA_MAIN_CLASS" -Dexec.args="--config $CONFIG"

USAGE="usage: build.sh [compile] [clean] [execute]"

################################
# check command line arguments #
################################
if [ $# = 0 ]
then
	echo $USAGE
	exit;
fi

############################################
### (target) clean #########################
############################################
if [ $1 = "clean" ]
then
	mvn clean
	exit;
fi

############################################
### (target) compile #######################
############################################
if [ $1 = "compile" ]
then
	mvn compile
	exit;
fi

############################################
### (target) execute #######################
############################################
if [ $1 = "execute" ]
then
	###################
	# Set up RTI_HOME #
	###################
	RTI_HOME=/usr/local/portico-2.1.0
	echo RTI_HOME environment variable is set to $RTI_HOME

	cd ./target/bin/
	LD_LIBRARY_PATH=".:$RTI_HOME/lib/gcc4:$RTI_HOME/jre/lib/amd64/server:/opt/omnetpp/omnetpp-5.2.1/lib"
	export LD_LIBRARY_PATH
	chmod 750 ./OmnetFederateTest
	./OmnetFederateTest
	exit;
fi

echo $USAGE

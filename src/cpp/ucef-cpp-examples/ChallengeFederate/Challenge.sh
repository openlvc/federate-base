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
	cd ../../ucef-cpp-core
	mvn clean
	cd ../ucef-cpp-examples/ChallengeFederate
	mvn clean
	cd ..
	exit;
fi

############################################
### (target) compile #######################
############################################
if [ $1 = "compile" ]
then
	cd ../../ucef-cpp-core
	mvn compile
	cd ../ucef-cpp-examples/ChallengeFederate
	mvn compile
	cd ..
	exit;
fi

###################
# Set up RTI_HOME #
###################
RTI_HOME=/home/vagrant/Downloads/portico-2.1.0
echo RTI_HOME environment variable is set to $RTI_HOME

############################################
### (target) execute #######################
############################################
if [ $1 = "execute" ]
then
	###################
	# Set up RTI_HOME #
	###################
	RTI_HOME=/home/vagrant/Downloads/portico-2.1.0
	echo RTI_HOME environment variable is set to $RTI_HOME

	cd ./target/bin/
	LD_LIBRARY_PATH=".:$RTI_HOME/lib/gcc4:$RTI_HOME/jre/lib/amd64/server"
	export LD_LIBRARY_PATH
	chmod 750 ./ChallengeFederate
	./ChallengeFederate -count 20 -config "resources/config/fedConfig.json"
	exit;
fi

echo $USAGE

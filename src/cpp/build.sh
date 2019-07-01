USAGE="usage: build.sh [compile] [clean]"

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

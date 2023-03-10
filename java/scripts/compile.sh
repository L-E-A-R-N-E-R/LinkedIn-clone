#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Indicate the path of the java compiler to use
export JAVA_HOME=/usr/csshare/pkgs/jdk1.7.0_17
export PATH=$JAVA_HOME/bin:$PATH

mkdir -p $DIR/../classes

# compile the java program
javac -d $DIR/../classes $DIR/../src/ProfNetwork.java

#run the java program
#Use your database name, port number and login
java -classpath $DIR/../classes:$DIR/../lib/pg73jdbc3.jar ProfNetwork $USER"_DB" $PGPORT $USER


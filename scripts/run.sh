#!/bin/bash

if [ -z $WORKLOAD_HOME ]
then
    echo "ERROR: WORKLOAD_HOME is not defined."
    exit 1
fi

if [ -z $CLIENT_HOME ]
then
    echo "ERROR: CLIENT_HOME is not defined."
    exit 1
fi

CONFIGFILE=${WORKLOAD_HOME}/conf/conf.json
if [ ! -f ${CONFIGFILE} ]; then
    echo -e "ERROR: The configuration file for the client (with the name conf.json ) can not be found under ${WORKLOAD_HOME}/conf directory."
    exit 1
fi

WORKLOADFILE=${WORKLOAD_HOME}/conf/workload.txt
if [ ! -f ${WORKLOADFILE} ]; then
    echo -e "ERROR: The workload file (with the name workload.txt ) can not be found under ${WORKLOAD_HOME}/conf directory."
    exit 1
fi


mkdir -p ${WORKLOAD_HOME}/output

java -cp ${CLIENT_HOME}/target/asterixdb-client-jar-with-dependencies.jar driver.Driver ${WORKLOAD_HOME}

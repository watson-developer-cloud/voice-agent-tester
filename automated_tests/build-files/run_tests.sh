#!/bin/bash

restCredentials=$restCredentials
couchCredentials=$couchCredentials
outboundCredentials=$outboundCredentials
couchUri=http://couchdb:5984
testerURI=http://at.microservice:9080
outboundURI=http://sip.orchestrator:9080
databaseName=automated_test
toIp=$(ping -c 1 sip.orchestrator.callee | cut -d"(" -f2 | cut -d")" -f1 | head -c -1 -n 1 | tr -d '\n')
fromIp=$(ping -c 1 sip.orchestrator | cut -d"(" -f2 | cut -d")" -f1 | head -c -1 -n 1 | tr -d '\n')

timeToWait=90
timer=0
currentCondition="$(curl -s --head -X GET -i $couchUri -u $couchCredentials | grep 200)";


# Test that couchdb is up

echo "Waiting $timeToWait seconds for couchdb to load"

while [ -z "$currentCondition" ]; do
    sleep 2s;
    currentCondition="$(curl -s --head -X GET -i $couchUri -u $couchCredentials | grep 200)";
    ((timer+=2))
    if [ $timer -gt $timeToWait ]
    then
        echo "CouchDB didnt load in time. Exiting"
        exit 1
    fi
done

sleep 2s

# Delete the database everytime you start up

curl -s -X DELETE $couchUri/$databaseName -u $couchCredentials

# Test that VAT is up
currentCondition="$(curl -s --head -X GET -i $testerURI/voice-agent-tester/v1/case -u $restCredentials | grep 200)";
timer=0

echo "Waiting $timeToWait seconds for VAT to load"

while [ -z "$currentCondition" ]; do
    sleep 2s;
    currentCondition="$(curl -s --head -X GET -i $testerURI/voice-agent-tester/v1/case -u $restCredentials | grep 200)";
    ((timer+=2))
    if [ $timer -gt $timeToWait ]
    then
        echo "The VAT didnt load in time. Exiting"
        exit 1
    fi
done

# Test that SO outbound is up
currentCondition="$(curl -s --head -X GET -i $outboundURI/vgw/maintenance/numOfCalls -u $outboundCredentials | grep 200)";
timer=0

echo "Waiting $timeToWait seconds for SO outbound calls to load"

while [ -z "$currentCondition" ]; do
    sleep 2s;
    currentCondition="$(curl -s --head -X GET -i $outboundURI/vgw/maintenance/numOfCalls -u $outboundCredentials | grep 200)";
    ((timer+=2))
    if [ $timer -gt $timeToWait ]
    then
        echo "The SO didnt load in time. Exiting"
        exit 1
    fi
done

# Start tests

sleep 3s

echo "Everything is up. Starting tests"

./run_test_case_test.sh $restCredentials $testerURI
testCase=$?
./run_worker_test.sh $restCredentials $testerURI
worker=$?
./run_job_test.sh $restCredentials $testerURI $toIp $fromIp
job=$?
./run_batch_job_test.sh $restCredentials $testerURI $toIp $fromIp
batchJob=$?


if [ $testCase -eq $worker ] && [ $worker -eq $job ] && [ $job -eq $batchJob ] && [ $batchJob -eq 0 ]
then 
    exit 0
else
    exit 1
fi
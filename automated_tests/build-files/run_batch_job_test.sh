#!/bin/bash

##### This tests all the features of jobs

restCredentials=$1
testerURI=$2
toHost=$3
toPort="5070"
fromHost=$4
fromPort="5060"
tenantTo="16152059388"
tenantFrom="16152059387"

jbfails=0
jbran=0
jbskipped=0
jbpassed=0


waitSecondsPause=150
waitSeconds=40

### Jobs

# Create a test case and a worker with no cases
echo "Batch Job: Stage 1: Create test case and worker"
tc1=$(curl -sb --header "Accept: application/json" --header "Content-Type:application/json" -X POST $testerURI/voice-agent-tester/v1/case -u $restCredentials -d @./resources/tc1.json | jq -r '.id')
wk1=$(curl -sb --header "Accept: application/json" --header "Content-Type:application/json" -X POST $testerURI/voice-agent-tester/v1/worker -u $restCredentials -d @./resources/wk1.json | jq -r '.id')
((jbran++))
if [ ! -z $tc1 ] && [ ! -z $wk1 ]
then
    ((jbpassed++))
    # Try to start Batch job with the worker with no cases and verify error
    echo "Batch Job: Stage 2: Start Batch job with worker with no cases"
    nocases=$(curl -o /dev/null -s -w "%{http_code}\n" -X POST "$testerURI/voice-agent-tester/v1/worker/$wk1/batchJob" -u $restCredentials )
    ((jbran++))
    if [ $nocases == '400' ]
    then
        ((jbpassed++))
        echo "Batch job: Stage 3: Create Batch job "
        # Modify the worker to include the test case
        ((jbran++))
        workerJson=$(cat ./resources/wk1_cases.json | jq --arg tc1 "$tc1" '.cases +=[{"caseId":$tc1}]')
        workerJson=$(echo $workerJson | jq --arg to "sip:$tenantTo@$toHost:$toPort" '.callDefinition.to = $to')
        workerJson=$(echo $workerJson | jq --arg from "sip:$tenantFrom@$fromHost:$fromPort" '.callDefinition.from = $from')
        if [ '200' == $(echo $workerJson | curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1 -u $restCredentials -d @-) ]
        then
            # Create a Batch job to that worker
            jb1=$(curl -sb --header "Accept: application/json" --header "Content-Type:application/json" -X POST $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob -u $restCredentials | jq -r '.id')
            if [ ! -z $jb1 ]
            then
                ((jbpassed++))
                timeWaited=0
                echo "Batch Job: Stage 4: Stop Batch job "
                # When status changes from starting to running stop the Batch job
                while [ 'running' != $(curl -sb -X GET $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1 -u $restCredentials | jq -r '.status') ] && [ $timeWaited -lt $waitSeconds ]
                do  
                    sleep 2s
                    ((timeWaited+=2))
                done
                ((jbran++))
                if [ $timeWaited -lt $waitSeconds ] && [ '200' == $(curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1/stop -u $restCredentials) ]
                then
                    ((jbpassed++))
                    echo "Batch Job: Stage 5: Unpause Batch job "
                    timeWaited=0
                    # When Batch  is stopped, unpause the Batch job
                    while [ 'stopped' != $(curl -sb -X GET $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1 -u $restCredentials | jq -r '.status') ] && [ $timeWaited -lt $waitSeconds ]
                    do  
                        sleep 2s
                        ((timeWaited+=2))
                    done
                    ((jbran++))
                    if [ $timeWaited -lt $waitSeconds ] && [ '200' == $(curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1/unpause -u $restCredentials) ]
                    then
                        ((jbpassed++))
                        echo "Batch Job: Stage 6: Starting Batch job with empty worker cases"
                        # Change the worker to empty cases array
                        workerJson=$(echo $workerJson | jq '.cases=[]')
                        if [ '200' == $(echo $workerJson | curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1 -u $restCredentials -d @-) ]
                        then
                            # Try to start the Batch job. Verify error
                            if [ '400' == $(curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1/start -u $restCredentials) ]
                            then
                                ((jbpassed++))
                            else
                                echo "Failed Batch Job: Stage 6. Batch Job started with empty cases"
                                ((jbfails++))
                            fi
                        else
                            echo "Failed Batch Job: Stage 6. Worker could not be updated"
                            ((jbfails++))
                        fi
                        ((jbran++))
                        # Add the test case you left twice and start the Batch job again
                        workerJson=$(echo $workerJson | jq --arg tc1 "$tc1" '.cases +=[{"caseId":$tc1}]')
                        workerJson=$(echo $workerJson | jq --arg tc1 "$tc1" '.cases +=[{"caseId":$tc1}]')
                        workerUpdate=$(echo $workerJson | curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1 -u $restCredentials -d @-)
                        jobStart=$(curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1/start -u $restCredentials)
                    else
                        echo "Failed Batch Job: Stage 5. Batch Job was not stopped"
                        ((jbfails++))
                        ((jbskipped++))
                    fi
                    # When status changes from starting to running pause the Batch job.
                    echo "Batch Job: Stage 7: Pause Batch job"
                    timeWaited=0
                    while [ 'running' != $(curl -sb -X GET $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1 -u $restCredentials | jq -r '.status') ] && [ $timeWaited -lt $waitSeconds ]
                    do  
                        sleep 2s
                        ((timeWaited+=2))
                    done
                    ((jbran++))
                    if [ $timeWaited -lt $waitSeconds ] && [ '200' == $(curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1/pause -u $restCredentials) ] 
                    then
                        timeWaited=0
                        while [ 'paused' != $(curl -sb -X GET $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1 -u $restCredentials | jq -r '.status') ] && [ $timeWaited -lt $waitSecondsPause ]
                        do  
                            sleep 2s
                            ((timeWaited+=2))
                        done
                        if [ $timeWaited -lt $waitSecondsPause ] && [ 'paused' == $(curl -sb -X GET $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1 -u $restCredentials | jq -r '.status') ]
                        then
                            ((jbpassed++))
                            # Start and make sure it changed to running
                            echo "Batch : Stage 8: Start Batch job"
                            start=$(curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1/start -u $restCredentials)
                            if [ $start == '200' ]
                            then
                                timeWaited=0
                                while [ 'running' != $(curl -sb -X GET $testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1 -u $restCredentials | jq -r '.status') ] && [ $timeWaited -lt $waitSeconds ]
                                do  
                                    sleep 2s
                                    ((timeWaited+=2))
                                done
                                if [ $timeWaited -lt $waitSeconds ]
                                then
                                    ((jbpassed++))
                                else
                                    ((jbfails++))
                                fi
                            else
                                echo "Failed Batch : Stage 8. Batch Job could not be started"
                                ((jbfails++))
                            fi
                            ((jbran++))
                        else
                            echo "Failed Batch Job: Stage 7. Batch Job could not be paused"
                            ((jbfails++))
                            ((jbskipped+=1))
                        fi
                    else
                        echo "Failed Batch Job: Stage 7. Batch Job could not be paused"
                        ((jbfails++))
                        ((jbskipped+=1))
                    fi
                else
                    echo "Failed Batch : Stage 4. Batch Job could not be stopped"
                    ((jbfails++))
                    ((jbskipped+=4))
                fi
            else
                echo "Failed Batch Job: Stage 3. Batch Job was not started"
                ((jbfails++))
                ((jbskipped+=5))
            fi
        else
            echo "Failed Batch Job: Stage 3. Worker could not be modified"
            ((jbfails++))
            ((jbskipped+=5))
        fi
    else
        echo "Failed Batch Job: Stage 2. Batch Job started with no cases. Skipping other stages"
        ((jbfails++))
        ((jbskipped+=6))
    fi
    # Clean up Batch , worker and test case
    echo "Batch Job: Stage 9: Cleaning up"
    tcdel=$(curl -o /dev/null -s -w "%{http_code}\n" -X DELETE "$testerURI/voice-agent-tester/v1/case?namespace=test" -u $restCredentials )
    wkdel=$(curl -o /dev/null -s -w "%{http_code}\n" -X DELETE "$testerURI/voice-agent-tester/v1/worker?namespace=test" -u $restCredentials )
    jbconf=$(curl -o /dev/null -s -w "%{http_code}\n" -X DELETE "$testerURI/voice-agent-tester/v1/worker/$wk1/batchJob/$jb1" -u $restCredentials )
    ((jbran++))
    if [ $tcdel == $wkdel ] && [ $wkdel == '200' ] && [ $jbconf == '404' ]
    then
        ((jbpassed++))
    else
        echo "Failed Batch : Stage 9. Clean up was not done as expected"
        ((jbfails++))
    fi
else
    echo "Failed Batch Job: Stage 1. Resources could not be created. Skipping other stages"
    ((jbfails++))
    ((jbskipped+=8))
fi

echo "Finished with Batch job stages"
echo "Stages ran: $jbran"
echo "Stages passed: $jbpassed"
echo "Stages failed: $jbfails"
echo "Stages skipped: $jbskipped"

if [ $jbpassed -eq $jbran ] && [ $jbfails -eq 0 ] && [ $jbskipped -eq 0 ]
then 
    exit 0
else
    exit 1
fi
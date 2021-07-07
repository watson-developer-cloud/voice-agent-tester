#!/bin/bash

##### This tests all the features of workers

restCredentials=$1
testerURI=$2

### Workers: Details which test cases to run and who to call. They need name, iterations, and callDefinition, optional description, and cases and default namespace if not specified

echo "Running tests for workers"
wkfails=0
wkran=0
wkskipped=0
wkpassed=0

# Create invalid worker and verify errors
echo "Worker: Stage 1: Worker bean validation"
bodies=('{}' '@./resources/wk_no_name.json' '@./resources/wk_no_iter.json' '@./resources/wk_no_calldef.json');
failures=0
for body in ${bodies[@]}
do
    response=$(curl -o /dev/null -s -w "%{http_code}\n" -X POST $testerURI/voice-agent-tester/v1/worker --header "Content-Type:application/json" -u $restCredentials -d $body);
    [ $response == '400' ] || (echo "Failed validation with body: $body" && ((failures++)));
done
[ $failures -eq 0 ] && ((wkpassed++))
[ $failures -eq 0 ] || (echo "Failed Worker: Stage 1" && ((wkfails++)))
((wkran++))
unset bodies

# Create a worker with no cases and get id
echo "Worker: Stage 2: Creating worker"
wk1=$(curl -sb --header "Accept: application/json" --header "Content-Type:application/json" -X POST $testerURI/voice-agent-tester/v1/worker -u $restCredentials -d @./resources/wk1.json | jq -r '.id')

# Verify that worker exists with the id and is in namespace specified
response=$(curl -o /dev/null -s -w "%{http_code}\n" -X GET $testerURI/voice-agent-tester/v1/worker/$wk1 -u $restCredentials);
((wkran++))
failures=0
if [ ! -z $wk1 ] && [ $response == '200' ]
then
    # Modify worker and verify that it changed
    echo "Worker: Stage 3: Modifying worker"
    response=$(curl -o /dev/null -s -w "%{http_code}\n" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1 --header "Content-Type:application/json" -u $restCredentials -d @./resources/wk1_modify.json);
    if [ $response == '200' ]
    then
        ((wkpassed++))
        # Try to change namespace and verify error
        response=$(curl -o /dev/null -s -w "%{http_code}\n" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1 --header "Content-Type:application/json" -u $restCredentials -d @./resources/wk1_bad_modify.json);
        # Verify that namespace has not been changed and exists in previous namespace
        if [ $response == '400' ]
        then
            [ 'test' == $(curl -sb -X GET $testerURI/voice-agent-tester/v1/worker/$wk1 -u $restCredentials | jq -r '.namespace') ] || (echo "Failed validation: Invalid namespace change detected" && ((failures++)));
        else
            echo "Failed Worker: Stage 3" && ((failures++))
        fi
    else
        echo "Failed Worker: Stage 3. Worker was not modified" && ((failures++))
        ((wkskipped++))
    fi
    ((wkran++))
    [ $failures -eq 0 ] || ((wkfails++))
    [ $failures -eq 0 ] && ((wkpassed++))

    echo "Worker: Stage 4: Worker cases different namespace"
    failures=0
    # Modify worker and add test case in different namespace verify error
    tc2=$(curl -sb --header "Accept: application/json" --header "Content-Type:application/json" -X POST $testerURI/voice-agent-tester/v1/case -u $restCredentials -d @./resources/tc2.json | jq -r '.id')
    [ '400' == $(cat ./resources/wk1_cases.json | jq --arg tc2 "$tc2" '.cases +=[{"caseId":$tc2}]' | curl -o /dev/null -s -w "%{http_code}\n" --header "Accept: application/json" --header "Content-Type:application/json" -X PUT $testerURI/voice-agent-tester/v1/worker/$wk1 -u $restCredentials -d @-) ] || (echo "Failed Worker: Stage 4. Added test cases from different namespaces" && ((failures++)))
    ((wkran++))
    [ $failures -eq 0 ] || ((wkfails++))
    [ $failures -eq 0 ] && ((wkpassed++))
    echo "Worker: Stage 5: Clean up"
    # One of those test cases will be used to run jobs doing a call so delete the other one
    delete1=$(curl -o /dev/null -s -w "%{http_code}\n" -X DELETE "$testerURI/voice-agent-tester/v1/case/$tc2" -u $restCredentials )
    confirmation1=$(curl -o /dev/null -s -w "%{http_code}\n" -X GET "$testerURI/voice-agent-tester/v1/case/$tc2" -u $restCredentials )
    delete2=$(curl -o /dev/null -s -w "%{http_code}\n" -X DELETE "$testerURI/voice-agent-tester/v1/worker?namespace=test" -u $restCredentials )
    confirmation2=$(curl -o /dev/null -s -w "%{http_code}\n" -X GET "$testerURI/voice-agent-tester/v1/worker/$wk1" -u $restCredentials )
    if [ $confirmation1 == '404' ] && [ $delete1 == '200' ] && [ $confirmation2 == '404' ] && [ $delete2 == '200' ]
    then
        ((wkpassed++))
    else
        if [ $confirmation1 != '404' ] && [ $delete1 != '200' ] && [ $confirmation2 != '404' ] && [ $delete2 != '200' ]
        then
            echo "Failed Worker: Stage 5. Test case nor worker were deleted" && ((wkfails++))
        elif [ $confirmation2 != '404' ] && [ $delete2 != '200' ]
        then
            echo "Failed Worker: Stage 5. Worker was not deleted" && ((wkfails++))
        elif [ $confirmation1 != '404' ] && [ $delete1 != '200' ]
        then
            echo "Failed Worker: Stage 5. Test case was not deleted" && ((wkfails++))
        else
            echo "Failed Worker: Stage 5. Something bad occurred" && ((wkfails++))
        fi
    fi
    ((wkran++))
else
    echo "Failed Worker: Stage 2. Skipping other stages"
    ((wkfails++))
    ((wkskipped+=3))
fi

echo "Finished with worker stages"
echo "Stages ran: $wkran"
echo "Stages passed: $wkpassed"
echo "Stages failed: $wkfails"
echo "Stages skipped: $wkskipped"

if [ $wkpassed -eq $wkran ] && [ $wkfails -eq 0 ] && [ $wkskipped -eq 0 ]
then 
    exit 0
else
    exit 1
fi
#!/bin/bash

##### This tests all the features of test cases

restCredentials=$1
testerURI=$2

### Test cases: Outline of conversation. They need to contain name and turns (with at least one object), optional description and default namespace if not specified

echo "Running tests for test cases"
tcfails=0
tcran=0
tcskipped=0
tcpassed=0
# Create test case without name or valid turns to verify error
echo "Test Case: Stage 1: Test case bean validation"
testCases=('{}' '@./resources/tc_no_turns_or_name.json' '@./resources/tc_no_name.json' '@./resources/tc_no_turns.json' '@./resources/tc_empty_turns.json');
failures=0
for body in ${testCases[@]}
do
    response=$(curl -o /dev/null -s -w "%{http_code}\n" -X POST $testerURI/voice-agent-tester/v1/case --header "Content-Type:application/json" -u $restCredentials -d $body);
    [ $response == '400' ] || (echo "Failed validation with body: $body" && ((failures++)));
done
[ $failures -eq 0 ] && ((tcpassed++))
[ $failures -eq 0 ] || (echo "Failed Test Case: Stage 1" && ((tcfails++)))
((tcran++))
unset testCases

# Create a test case and get the id var tc1. Also create tc2 in other namespace
echo "Test Case: Stage 2: Creating test cases"
tc1=$(curl -sb --header "Accept: application/json" --header "Content-Type:application/json" -X POST $testerURI/voice-agent-tester/v1/case -u $restCredentials -d @./resources/tc1.json | jq -r '.id')
tc2=$(curl -sb --header "Accept: application/json" --header "Content-Type:application/json" -X POST $testerURI/voice-agent-tester/v1/case -u $restCredentials -d @./resources/tc2.json | jq -r '.id')
((tcran++))


# Verify that the test case exists with the id specified and is in namespace specified
response=$(curl -o /dev/null -s -w "%{http_code}\n" -X GET $testerURI/voice-agent-tester/v1/case/$tc1 -u $restCredentials);
if [ ! -z $tc1 ] && [ ! -z $tc2 ] && [ $response == '200' ]
then
    ((tcpassed++))
    # Modify the test case and verify that the test case changed
    echo "Test Case: Stage 3: Modifying test case"
    response=$(curl -o /dev/null -s -w "%{http_code}\n" -X PUT $testerURI/voice-agent-tester/v1/case/$tc1 --header "Content-Type:application/json" -u $restCredentials -d @./resources/tc1_modify.json);
    if [ $response == '200' ]
    then
        # Try to change namespace and verify error
        response=$(curl -o /dev/null -s -w "%{http_code}\n" -X PUT $testerURI/voice-agent-tester/v1/case/$tc1 --header "Content-Type:application/json" -u $restCredentials -d @./resources/tc1_bad_modify.json);
        # Verify that namespace has not been changed and exists in previous namespace
        if [ $response == '400' ]
        then
            if [ 'test' == $(curl -sb -X GET $testerURI/voice-agent-tester/v1/case/$tc1 -u $restCredentials | jq -r '.namespace') ] 
            then 
                ((tcpassed++))
            else
                echo "Failed validation: Invalid namespace change detected" && ((tcfails++));
            fi
        else
            echo "Failed Test Case: Stage 3" && ((tcfails++))
        fi
    else
        echo "Failed Test Case: Stage 3" && ((tcfails++))
    fi
    ((tcran++))
    # Verify other test case is in other namespace
    echo "Test Case: Stage 4: Verify test cases are in different namespaces"

    if [ "test2" == $(curl -sb -X GET $testerURI/voice-agent-tester/v1/case/$tc2 -u $restCredentials | jq -r '.namespace') ]
    then 
        # Verify test cases exist on different namespaces
        if [ "Tc1" == $(curl -sb -X GET "$testerURI/voice-agent-tester/v1/case?namespace=test" -u $restCredentials | jq -r --arg tc1 "$tc1" '.[] | select(.id == $tc1) | .name') ] && [ "Tc2" == $(curl -sb -X GET "$testerURI/voice-agent-tester/v1/case?namespace=test2" -u $restCredentials | jq -r --arg tc2 "$tc2" '.[] | select(.id == $tc2) | .name') ]
        then
            ((tcpassed++))
        else
            echo "Failed Test Case: Stage 4. Namespaces didn't match as expected" && ((tcfails++))
        fi
    else
        echo "Failed Test Case: Stage 4. Tc2 was not found in database" && ((tcfails++))
    fi
    ((tcran++))
    echo "Test Case: Stage 5: Cleaning up"
    # One of those test cases will be used to run jobs doing a call so delete the other one
    delete1=$(curl -o /dev/null -s -w "%{http_code}\n" -X DELETE "$testerURI/voice-agent-tester/v1/case/$tc2" -u $restCredentials )
    confirmation1=$(curl -o /dev/null -s -w "%{http_code}\n" -X GET "$testerURI/voice-agent-tester/v1/case/$tc2" -u $restCredentials )
    delete2=$(curl -o /dev/null -s -w "%{http_code}\n" -X DELETE "$testerURI/voice-agent-tester/v1/case?namespace=test" -u $restCredentials )
    confirmation2=$(curl -o /dev/null -s -w "%{http_code}\n" -X GET "$testerURI/voice-agent-tester/v1/case/$tc1" -u $restCredentials )
    if [ $confirmation1 == '404' ] && [ $delete1 == '200' ] && [ $confirmation2 == '404' ] && [ $delete2 == '200' ]
    then
        ((tcpassed++))
    else
        echo "Failed Test Case: Stage 5. Test cases were not deleted" && ((tcfails++))
    fi
    ((tcran++))

else
    echo "Failed Test Case: Stage 2. Skipping other stages"
    ((tcfails++))
    ((tcskipped+=3))
fi

echo "Finished with test cases"
echo "Stages ran: $tcran"
echo "Stages passed: $tcpassed"
echo "Stages failed: $tcfails"
echo "Stages skipped: $tcskipped"

if [ $tcpassed -eq $tcran ] && [ $tcfails -eq 0 ] && [ $tcskipped -eq 0 ]
then 
    exit 0
else
    exit 1
fi
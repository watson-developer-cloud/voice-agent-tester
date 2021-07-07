#!/bin/bash
if [ ! -z "$DOT_ENV_PATH" ]
then
    export $(cat $DOT_ENV_PATH | xargs)
    echo "Successfully loaded environment variables from $DOT_ENV_PATH"
elif [ ! -f .env ]
then
    export $(cat .env | xargs)
    echo "Successfully loaded environment variables from .env file"
else
    echo "No configuration file was found"
	echo "$DOT_ENV_PATH"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX down

docker rmi $DOCKER_PREFIX-automated-tests

[ -d ./automated_tests/logs/ ] && rm -r ./automated_tests/logs/ && mkdir ./automated_tests/logs/
[ -d ./automated_tests/logs/ ] || mkdir ./automated_tests/logs/
mkdir ./automated_tests/logs/callee-MR
mkdir ./automated_tests/logs/callee-SO
mkdir ./automated_tests/logs/caller-MR
mkdir ./automated_tests/logs/caller-SO
mkdir ./automated_tests/logs/AT

docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX up --build --abort-on-container-exit >> /dev/null

exitVal=$?

docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX logs -t tests | cut -d "|" -f 2 | cut -d " " -f 2-

# Copy logs
docker cp "$(docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX ps -q at.microservice)":/logs/ ./automated_tests/logs/AT
docker cp "$(docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX ps -q sip.orchestrator)":/logs/ ./automated_tests/logs/caller-SO
docker cp "$(docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX ps -q media.relay)":/vgw-media-relay/logs/ ./automated_tests/logs/caller-MR
docker cp "$(docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX ps -q sip.orchestrator.callee)":/logs/ ./automated_tests/logs/callee-SO
docker cp "$(docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX ps -q media.relay.callee)":/vgw-media-relay/logs ./automated_tests/logs/callee-MR


docker-compose -f $DIR/docker-compose.yml -p $DOCKER_PREFIX down

docker rmi $DOCKER_PREFIX-automated-tests

exit $exitVal

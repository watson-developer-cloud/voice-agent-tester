FROM ubuntu:18.04

RUN apt-get update && apt-get install -y jq curl iputils-ping

ADD ./ /tests

WORKDIR /tests

CMD /tests/run_tests.sh
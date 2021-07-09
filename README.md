# Voice Agent Tester Microservice

## Overview

This is the overview for the testing voice agents RESTful microservice. It enables users of Voice Agent to setup automated voice tests that can be executed against existing Voice Agents.

The Microservice provides users the following capabilities:

1. Ability to create, list, edit and delete individual test cases which define a complete dialog flow.
2. Ability to create, list, edit and delete a worker which can excute sets of tests.
3. Ability to create, list, delete jobs which is the actual execution of a set of test by a worker.

**Prerequisites:** To run the Agent Tester you would need to have docker and docker-compose installed on your computer. To run jobs and test you Voice Gateway or Voice Agent deployment you will also need speech to text and text to speech services

## Setting up using docker

You can take the example docker-compose.yml file which includes a SIP Orchestrator which will drive the outbound calls, Media Relay and Couch Database for data persitence. Fill out the variables for your services or create a .env file and fill in the variables there. The necessary variables for using the microservice are as stated below. For details of the variables look at the Environment Variable Configuration section

On the command line run:

***IF you dont** have the built image, run `./gradlew clean build` to build the war app. This is only necessary until the image is uploaded to docker hub and can be downloaded from there.

To start the bundled services in the docker-compose just run

```
docker-compose up
```

Which will build the image if its not already built, and start all the services in the compose file. Once the microservice is up and running you can then view the OpenAPI Specification of the service in:

```
{protocol}://{host}:{port}/openapi/ui
```

And you can go ahead and start writing and posting test cases and workers and starting jobs to verify your Voice Gateway/ Voice Agent installation. Take a look at the `samples` directory for additional information and samples of the resources used by the Agent Tester

**Please** only use the REST interface to create and modify resources. If you manually change a resource in the database it's stored in, it could affect other resources.

## Environment Variable Configuration

The following tables list all Docker environment variables that can be used to configure the Agent Tester microservice

### General deployment configuration

 Environment variable name   | Default value   | Description   |
| ---------------------- | --------------- | :-----------:|
| TESTER_WEBHOOK_URI | N/A | (Optional) URI of the microservice where all the endpoints are found. If not defined, it is assumed that it is being run from a docker container and that the Voice Gateway that will start the outbound calls is in the same container network. Inside the application the URI will be built with a protocol of http with these assumptions. |
| CLOUDANT_URL | N/A | URL endpoint of the database on which all the associated test data will be found. Takes precedence over CLOUDANT_ACCOUNT|
| CLOUDANT_USERNAME | N/A | Username for authenticating to the database where the resources are stored. Along with CLOUDANT_PASSWORD they take precedence over CLOUDANT_APIKEY|
| CLOUDANT_PASSWORD | N/A | Password for authenticating to the database where the resources are stored. Along with CLOUDANT_USERNAME they take precedence over CLOUDANT_APIKEY|
| CLOUDANT_ACCOUNT | N/A | A string that defines your IBM Cloudant account name, if the user name and account name are different |
| CLOUDANT_APIKEY | N/A | Apikey for authenticating to the database where the resources are stored |
| CLOUDANT_DATABASE_NAME | N/A | The name of the database where you want your data stored. If it doesn't exist it will be created. |
| CACHE_TIME_TO_LIVE | 10 | (Optional) Number of minutes which the resources inside the cache will last |
| CALLER_VOICE_GATEWAY_URI | N/A | URI of the Voice Gateway's REST application that contains the endpoints for outbound calling |
| CALLER_VOICE_GATEWAY_USERNAME | N/A | (Optional) The username for authenticating to the outbound calling REST API inside the URI CALLER_VOICE_GATEWAY_URI |
| CALLER_VOICE_GATEWAY_PASSWORD | N/A | (Optional) The password for authenticating to the outbound calling REST API inside the URI CALLER_VOICE_GATEWAY_URI |
| REST_ADMIN_USERNAME | defaultAdmin | (Optional) Admin username for quickstarting the service |
| REST_ADMIN_PASSWORD | defaultPassword | (Optional) Admin password for quickstarting the service |
| TESTER_WEBHOOK_USERNAME | defaultUser | Username for securing the tester webhook endpoints. If you define a custom registry you would need to add it as a user in the registry (see samples/security for more details) |
| TESTER_WEBHOOK_PASSWORD | defaultPass | Password for securing the tester webhook endpoints. If you define a custom registry you would need to add it as a user in the registry (see samples/security for more details) |

### Logging and tracing configuration

 Environment variable name   | Default value   | Description   |
| ---------------------- | --------------- | :-----------:|
| LOG_LEVEL | info | This is the log level for the microservice. Valid values from least information to most information are off, fatal, severe, warning, audit, fine, finest, and all. Note that when set to fine, finest, or all, the logs might contain sensitive data. |
| LOG_MAX_FILES | 5 | The maximum number of log files, trace files, and exception summary log files. When this limit is reached, the oldest file is deleted and a new file is created. For example, when this variable is set to 5, the microservice can generate up to 5 message logs, 5 trace logs, and 5 exception summaries. |
| LOG_MAX_FILE_SIZE | 100 | The maximum size in megabytes (MB) that a log file can reach before a new file is created. |

### Security configuration

#### SSL

 Environment variable name   | Default value   | Description   |
| ---------------------- | --------------- | :-----------:|
| SSL_TRUST_STORE_FILE | Provided by the container Java™ SDK | The file that contains the trusted keys for the outbound SSL connections, including connections to the Watson Assistant service, a service orchestration engine, the SMS provider, or XSLD caching server. |
| SSL_TRUST_PASSPHRASE | N/A | The passphrase that was used to secure the SSL_TRUST_STORE_FILE truststore file. |
| SSL_TRUST_FILE_TYPE | JKS | The format of the SSL_TRUST_STORE_FILE.|
| SSL_KEY_STORE_FILE | N/A | The keystore file that contains the trusted keys for inbound SSL connections. |
| SSL_KEY_PASSPHRASE | N/A | The passphrase that was used to secure the SSL_KEY_STORE_FILE keystore file. |
| SSL_KEY_FILE_TYPE | JKS | The format of the SSL_KEY_STORE_FILE. |

#### Authentication

Environment variable name   | Default value   | Description   |
| ---------------------- | --------------- | :-----------:|
| SERVER_REGISTRY_INCLUDE_PATH | Default Registry for quick starting the service that uses REST_ADMIN_USERNAME and REST_ADMIN_PASSWORD for authentication | (Optional) The path to the custom registry you which to include for authenticating to the service |
| ROLE_NAME_ADMINISTRATOR | Administrator | The name of the security group that has Administrator access to the service. This group has access to all the REST calls |
| ROLE_NAME_EDITOR | Editor | The name of the security group that has Editor access to the service. This group has access to all the REST calls |
| ROLE_NAME_OPERATOR | Operator | The name of the security group that has Operator access to the service. This group only has access to all the REST calls that involve viewing resources (GETs) and managing jobs |
| ROLE_NAME_VIEWER | Viewer | The name of the security group that has Viewer access to the service. This group only has access to all the REST calls that involve viewing resources (GETs) |

### Current limitations

1. A limitation of the microservice for now is that the SIP Orchestrator that drives the outbound calling can NOT be driving calls against itself. A multi tenant environment where one is a tester and other a testee is not supported, neither are two separate testers running different jobs testing each other

### Outbound calls to a phone number

If you wish to test against a Watson Assistant that is using a Phone Integration you would need to configure a SIP trunk which will make the call to the phone number associated with the Assistant. Here is and examples for setting one up with Twilio

  1. You would need to have a Twilio account set up (trial account will not work for this) or some other SIP trunking service provider that is Allow Listed with Watson Assistant.

  2. For Twilio create a SIP trunk on your account and in the SIP trunk add a termination and allow list the IP of the Voice Agent Tester Voice Gateway. Make note of the termination sip uri that you created

  3. When creating a worker, inside the call definition set the 'to' following this format `sip:+<Voice_Agent_Number>@<Termination_SIP_URI_of_trunk>`

  4. When running jobs from that worker it will call the trunk and the call will establish the communication to the phone number specified.

  **Warning:** Twilios SIP trunk by default can only start 1 call per second. This can be changed by talking to Twilio's sales force but if you don't wish to upgrade you could use the `jobsPerSecond` with the amount of calls you wish to start to be the amount of calls per second that the SIP trunk can handle

### Additional comments

* If you're using the compose and running a couchdb image for your data you need to map the volume to where you want your data to be persisted and may need to [set-up](https://docs.couchdb.org/en/master/setup/index.html#setup) the first time you run
* Be careful if you have recording turned on since a lot of calls could be generated and your memory space could fill up easily
* The jobs include the worker definition for running the test so any changes to the worker will not affect the job directly. If you wish that the job follows that new outline you have to restart the job
* If you delete a running job, the tester webhook will manage the failure of not finding it and hangup the call
* If you delete a worker, all the jobs that the worker ran and their data will be deleted as well
* If a test case thats being used by a worker gets deleted, when the jobs of that worker reach that test case they will fail and will be marked as invalid until you change the worker definition and remove that test case from the cases array
* If the gateway you would like to test is on kubernetes you may need to apply a different rule to the network configuration (See above in current limitations)
* If you're testing against a Voice Agent you must first [whitelist](https://cloud.ibm.com/docs/services/voice-agent?topic=voice-agent-whitelist_IP#whitelist_IP) the external IP address of the Caller Voice Gateway
* When the time comes to change the design and add another cache instance for the service like redis all that needs to be done is to implement a class implementing the CacheWrapper interface and modify the CloudantUtils class to change the cache instance to that cache

## Troubleshooting

* When writing test cases it is easier to use `substring` instead of mathing full text with `string` due to inaccuracies with speech to text services
* If you run a job from a worker without errors but running batch job of the same worker with multiple jobs fails then you would need to make sure the `MEDIA_RELAY_LOG_LEVEL` is NOT set to `TRACE`. If so then maybe the amount of calls you are running concurrently are too much to handle for the Caller Voice Gateway and you would need to scale up your deployments with a container management tool like kubernetes
* Depending on the amount of time the voice agent you are testing takes between turns, you could need to adjust the `POST_RESPONSE_TIMEOUT` either in the Caller Voice Gateway or on the worker which starts the calls. Also if there are long pauses to make the voice agent sound more human you would also need to adjust the `WATSON_STT_FIRMUP_SILENCE_TIME` accordingly

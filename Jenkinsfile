#!groovy

requireGhenkins()
abortOlderBuilds()

library identifier: 'wa-pipeline-library@master', retriever: modernSCM(
[$class: 'GitSCMSource',
remote: 'git@github.ibm.com:watson-engagement-advisor/wa-pipeline-library.git',
credentialsId: 'ghenkins-ssh-key'])

def VGW_VERSION = "1.0.7.0"

def dockerRegistry = 'watson-vg-docker-local.artifactory.swg-devops.com'
def dockerRegistryUrl = "https://${dockerRegistry}"
def artifactoryCreds = 'artifactory-docker-registry-vgwfun01'
def dockerTestEnv = 'watson-vg-docker-local.artifactory.swg-devops.com/voice-test-env:latest'
def javaBuildEnv = 'watson-vg-docker-local.artifactory.swg-devops.com/voice-java-build-env:8.0.5.41' // Java 8

def waDockerRegistry = 'https://us.icr.io';
def waDockerRegistryCreds = 'WA_REDSONJA_IAM_USERPASS'

def bxInfo = [
    [registry: "us.icr.io",
     namespace: "watson_assistant",
     iamAPIKey: "WA_US_SOUTH_REDSONJA_TOKEN"]
  ]

pipeline {
    agent {
        // Use a pickle node.
        label 'pickle'
    }
    environment {
        ARTIFACTORY = credentials('artifactory-docker-registry-vgwfun01')
        DOCKER_REGISTRY_URL = "https://us.icr.io"
        DOCKER_REGISTRY_CREDENTIALS = 'WA_REDSONJA_IAM_USERPASS'
        DOCKER_IMAGE_NAME = "watson-vg-docker-local.artifactory.swg-devops.com/${env.JOB_NAME}".toLowerCase()
        DOCKER_PREFIX="${env.JOB_NAME}-${env.BUILD_NUMBER}".toLowerCase()
        TERM = 'xterm-color'
    }

    options {
        timeout(20)
        // Show timestamps
        timestamps()

        // Convert color escape codes to pretty colors.
        ansiColor('xterm')
    }

    stages {

        stage('build') {
            agent {
                docker {
                    image javaBuildEnv
                    args '--init'
                    // Reuse the pickle node (from above) to run docker.
                    reuseNode true
                    registryUrl dockerRegistryUrl
                    registryCredentialsId artifactoryCreds
                }
            }
            steps {
                script {
                    sh """
                        ./gradlew clean build
                    """
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    def buildTimestamp = sh (
                        script: 'date +%Y%m%d-%H%M%S',
                        returnStdout: true
                    ).trim()

                    buildTag = "${env.BUILD_NUMBER}-${buildTimestamp}"
                    repoName = "${env.JOB_NAME}".toLowerCase().split('/')[1]
                    branchName = "${env.JOB_NAME}".toLowerCase().split('/')[2]
                    buildTag = "${VGW_VERSION}-${branchName}-${env.BUILD_NUMBER}-${buildTimestamp}"
                    dockerImageName = "${repoName}:${buildTag}"
                    
                    docker.withRegistry(dockerRegistryUrl, artifactoryCreds) {
                        docker.build(dockerImageName, '--pull --no-cache .')
                    }
                }
            }
        }

        stage('Docker Test') {
            agent {
                docker {
                    image dockerTestEnv
                    args '--init --volume /var/run/docker.sock:/var/run/docker.sock'
                    // Reuse the pickle node (from above) to run docker.
                    reuseNode true
                    registryUrl dockerRegistryUrl
                    registryCredentialsId artifactoryCreds
                }
            }
            steps {
                script {
                    docker.withRegistry(waDockerRegistry, waDockerRegistryCreds) {
                        withCredentials([file(credentialsId: 'agent-tester-required-credentials', variable: 'DOT_ENV_PATH')]){
                            sh """
                                export TESTER_IMAGE=$dockerImageName
                                export SO_IMAGE=us.icr.io/watson_assistant/development/voice-sip-orchestrator:develop
                                export MR_IMAGE=us.icr.io/watson_assistant/development/voice-media-relay:develop
                                export restAdmin=myRestAdminUser
                                export restPassword=myRestAdminPassword
                                export restCredentials=myRestAdminUser:myRestAdminPassword
                                export outboundCredentials=outboundCallsUser:outboundCallsPass
                                ./automated_tests/start-tests.sh
                            """
                        }
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'automated_tests/logs/**'
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry(waDockerRegistry, waDockerRegistryCreds) {
                       def image = docker.image(dockerImageName)
                       
                       if(env.BRANCH_NAME == "master" || env.BRANCH_NAME == "develop" || env.BRANCH_NAME.contains("release") || env.BRANCH_NAME == "develop-vgaas"){
                        pushToWaDevelopmentRedSonjaRegistries(dockerImageName)
                       }
                   }
                }
            }
            post {
                always {
                    sh """
                        docker rmi -f $dockerImageName
                    """
                }
            }
        }
    }

    post {
        always {
            // Clean up our workspace, delete all files.
            deleteDir()
            // Clean up the node's docker environment.
            cleanNode()
        }
    }
}

# Deploy Microservice on Kubernetes


This is a simple setup for deploying the Agent Tester Microservice to a kubernetes cluster on IBM Cloud. Right now the image used for the microservice is an artifactory image so you would need access to the artifactory for deploying on kubernetes. The `concurrent_scaling` directory contains deployment scripts for scaling and running multiple concurrent calls at the same time.

## Prerequisites

You must have an account for Watson Services Text to Speech and Speech to Text and also a kubernetes cluster service. Configure the following CLI tools so that you can access your Kubernetes cluster on IBM Cloud through the command line

1. [Install the IBM Cloud CLI](https://console.bluemix.net/docs/containers/cs_cli_install.html#cs_cli_install), which is required to interact with the IBM Cloud from the command line.
2. [Install the IBM Cloud Kubernetes plug-in (ibmcloud ks) and Kubernetes CLI](https://console.bluemix.net/docs/containers/cs_cli_install.html#cs_cli_install). The Kubernetes CLI enables you to use native Kubernetes commands to interact with your cluster.

3. [Configure the IBM Cloud CLI to run `kubectl` commands](https://console.bluemix.net/docs/containers/cs_cli_install.html#cs_cli_configure). After you configure the CLI, you can run the Kubernetes `kubectl` commands to work with your cluster.

4. [Install and configure the Calico CLI](https://console.bluemix.net/docs/containers/cs_network_policy.html#cli_install) so that you can use the `calicoctl` commands to change the default network policies. Network policies specify the network traffic that you want to allow or block to and from a pod in a cluster.

## Setup Steps

1. First apply the network configuration `network-policy.yml` which will allow traffic to the web applications in the cluster. Run `calicoctl apply -f network-policy.yml`. If you modify the `sample-deploy.json` to other ports make sure to change the network configuration also.

2. Open the `sample-creds.yml` and fill in all the credentials and variables needed for deploying. Once you fill the credentials run `kubectl apply -f sample-creds.yml`

3. Since the image is still in artifactory you would need to add your credentials for pulling the image so run:

```
kubectl create secret docker-registry artifactory-creds \
 --docker-server=https://watson-vg-docker-local.artifactory.swg-devops.com \
 --docker-username=<w3UsernameID> \
 --docker-password=<w3Password> \
 --docker-email=<w3EmailAddress>
```

4. Once all the network policies and credentials are in place you can deploy to kubernetes. Just run `kubectl create -f sample-deploy.json`
/* ***************************************************************** */
/*                                                                   */
/* Licensed Materials - Property of IBM                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2019. All Rights Reserved.                */
/*                                                                   */
/* US Government Users Restricted Rights - Use, duplication or       */
/* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. */
/*                                                                   */
/* ***************************************************************** */
package ibm.testing.microservice.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import ibm.testing.microservice.models.CreateWorker;
public abstract class WorkerApiService {
    public abstract Response workerDelete(String namespace,SecurityContext securityContext);
    public abstract Response workerGet(String namespace,SecurityContext securityContext);
    public abstract Response workerPost(CreateWorker body,SecurityContext securityContext);
    public abstract Response workerWorkerIdDelete(String workerId,SecurityContext securityContext);
    public abstract Response workerWorkerIdGet(String workerId,SecurityContext securityContext);
    public abstract Response workerWorkerIdPut(CreateWorker body,String workerId,SecurityContext securityContext);
    public abstract Response workerWorkerIdJobDelete(String workerId,SecurityContext securityContext);
    public abstract Response workerWorkerIdJobJobIdDelete(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdJobJobIdGet(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdJobJobIdPausePut(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdJobJobIdStartPut(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdJobJobIdStopPut(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdJobJobIdUnpausePut(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdJobPost(String workerId,SecurityContext securityContext);
    public abstract Response workerWorkerIdBatchJobDelete(String workerId,SecurityContext securityContext);
    public abstract Response workerWorkerIdBatchJobJobIdDelete(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdBatchJobJobIdGet(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdBatchJobJobIdPausePut(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdBatchJobJobIdStartPut(String workerId,String jobId,Integer jobsPerSecond,SecurityContext securityContext);
    public abstract Response workerWorkerIdBatchJobJobIdStopPut(String workerId,String jobId,SecurityContext securityContext);
    public abstract Response workerWorkerIdBatchJobJobIdUnpausePut(String workerId,String jobId,Integer jobsPerSecond,SecurityContext securityContext);
    public abstract Response workerWorkerIdBatchJobPost(String workerId,Integer concurrent,Integer jobsPerSecond,SecurityContext securityContext);
}

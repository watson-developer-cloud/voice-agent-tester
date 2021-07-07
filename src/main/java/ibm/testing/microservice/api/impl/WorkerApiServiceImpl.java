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
package ibm.testing.microservice.api.impl;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.JsonbBuilder;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.cloudant.client.org.lightcouch.NoDocumentException;

import ibm.testing.microservice.api.WorkerApiService;
import ibm.testing.microservice.api.utils.CloudantUtils;
import ibm.testing.microservice.api.utils.Messages;
import ibm.testing.microservice.api.utils.OutboundCallUtils;
import ibm.testing.microservice.models.BatchJob;
import ibm.testing.microservice.models.CreateWorker;
import ibm.testing.microservice.models.GetJob;
import ibm.testing.microservice.models.GetWorker;
import ibm.testing.microservice.models.GetWorkerJobs;
import ibm.testing.microservice.models.Model201;

public class WorkerApiServiceImpl extends WorkerApiService {

	private static Logger log = Logger.getLogger(WorkerApiServiceImpl.class.getName());

	@Override
	public Response workerDelete(String namespace, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0010I",namespace));
			String errors = CloudantUtils.removeAllWorkersInNamespace(namespace);
			if (errors.isEmpty())
				return Response.ok().build();
			return Response.status(Response.Status.CONFLICT).entity(errors).type(MediaType.TEXT_PLAIN).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerGet(String namespace, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0011I",namespace));
			List<GetWorker> l = CloudantUtils.getWorkersInNamespace(namespace);
			String JsonRepresentation = JsonbBuilder.create().toJson(l);
			return Response.ok(JsonRepresentation).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerPost(CreateWorker body, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0012I",body.getNamespace()));
			com.cloudant.client.api.model.Response creation = CloudantUtils.createWorker(body);
			return Response.status(Response.Status.CREATED).entity(new Model201().id(creation.getId())).build();
		} catch (IllegalArgumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0013E",body.getName()), e);
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
					.build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdDelete(String workerId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0014I",workerId));
			CloudantUtils.removeWorker(workerId);
			return Response.status(Response.Status.OK).build();
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdGet(String workerId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0015I",workerId));
			GetWorker worker = CloudantUtils.getWorker(workerId);
			String JsonRepresentation = JsonbBuilder.create().toJson(worker);
			return Response.ok(JsonRepresentation, MediaType.APPLICATION_JSON).build();
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdPut(CreateWorker body, String workerId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0016I",workerId));
			GetWorker worker = CloudantUtils.getWorker(workerId);
			worker.updateWorker(body);
			CloudantUtils.validateAndUpdateWorker(worker);
			return Response.ok().build();
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (IllegalArgumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0017W",workerId), e);
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
					.build();
		} catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdJobPost(String workerId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0019I",workerId));
			GetWorker worker = CloudantUtils.getWorker(workerId);
			if(worker.getCases().isEmpty())
				return Response.status(Response.Status.BAD_REQUEST).entity("Can not create job from worker with empty cases array").type(MediaType.TEXT_PLAIN).build();
			// Create a job from the specs of that worker. To map accordingly with the
			// worker we create the job id ourselves and its precedded by the worker id
			GetJob job = GetJob.newJobToStartCall(worker);
			// Add newly created job to worker
			worker.addJobsItem(new GetWorkerJobs().jobID(job.getId()));
			// Update worker in db
			CloudantUtils.updateWorker(worker);
			// Post job to db
			CloudantUtils.createJob(job);
			try {
				// Initialize call
				OutboundCallUtils.startOutboundCall(job, null);
				// Return id of job created and started
				return Response.status(Response.Status.CREATED).entity(new Model201().id(job.getId())).build();
			} catch (Exception e) {
				log.log(Level.SEVERE, Messages.getMessage("CWSAT0020E",job.getId()), e);
				try {
					job.setJobStopped();
					CloudantUtils.updateJob(job,false);
				} catch (Exception e2) {
					log.log(Level.SEVERE, Messages.getMessage("CWSAT0018E",job.getId()), e2);
				}
				return Response.status(Response.Status.CONFLICT).entity(new Model201().id(job.getId())).build();
			}
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdJobDelete(String workerId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0021I",workerId));
			CloudantUtils.removeAllJobsInWorker(workerId);
			return Response.ok().build();
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdJobJobIdGet(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0022I",new String[] {jobId,workerId}));
			GetJob job = CloudantUtils.getJob(workerId, jobId);
			String JsonRepresentation = JsonbBuilder.create().toJson(job);
			return Response.ok(JsonRepresentation, MediaType.APPLICATION_JSON).build();
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdJobJobIdDelete(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0023I",new String[] {jobId,workerId}));
			GetWorker worker=CloudantUtils.getWorker(workerId);
			GetJob job = CloudantUtils.getJob(workerId, jobId);
			if (job.isSubJob()) {
				log.log(Level.INFO,Messages.getMessage("CWSAT0024W",new String[] {jobId,job.getMasterJobId()}));
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("Job: " + job.getId() + " is a sub job for batch job: " + job.getMasterJobId())
						.type(MediaType.TEXT_PLAIN).build();
			}
			CloudantUtils.removeJob(worker, job);
			return Response.ok().build();
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdJobJobIdPausePut(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0025I",new String[] {jobId,workerId}));
			GetJob job = CloudantUtils.getJob(workerId, jobId);
			if (job.isSubJob()) {
				log.log(Level.INFO,Messages.getMessage("CWSAT0024W",new String[] {jobId,job.getMasterJobId()}));
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("Job: " + job.getId() + " is a sub job for batch job: " + job.getMasterJobId())
						.type(MediaType.TEXT_PLAIN).build();
			}
			// The job can only be put on pause if it is running
			if (job.isJobValidForRunning() && !job.isJobPausing()) {
				job.setJobPausing();
				CloudantUtils.updateJob(job);
				return Response.ok().build();
			} else {
				log.log(Level.INFO, Messages.getMessage("CWSAT0026I",new String[] {jobId,job.getStatus()}));
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Invalid status for pausing job. Job status: " + job.getStatus())
						.type(MediaType.TEXT_PLAIN).build();
			}
		}catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdJobJobIdStartPut(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0027I",new String[] {jobId,workerId}));

			GetWorker worker = CloudantUtils.getWorker(workerId);
			if(worker.getCases().isEmpty())
				return Response.status(Response.Status.BAD_REQUEST).entity("Can not start job from worker with empty cases array").type(MediaType.TEXT_PLAIN).build();
			GetJob job = CloudantUtils.getJob(workerId, jobId);
			if (job.isSubJob()) {
				log.log(Level.INFO,Messages.getMessage("CWSAT0024W",new String[] {jobId,job.getMasterJobId()}));
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("Job: " + job.getId() + " is a sub job for batch job: " + job.getMasterJobId())
						.type(MediaType.TEXT_PLAIN).build();
			}
			// The job can only be put on pause if it is running
			if (job.isJobValidForReStarting()) {
				job.setWorkerUsed(worker);
				if (job.isJobRunning() || job.isJobStarting()) {
					job.setJobReStarting();
					job.setStartTime(Instant.now().toEpochMilli());
					CloudantUtils.updateJob(job);
					return Response.ok().build();
				}
				job.reset();
				job.setJobStarting();
				// Post job to db
				CloudantUtils.updateJob(job,false);
				// Initialize call
				try {
					OutboundCallUtils.startOutboundCall(job, null);
				} catch (Exception e) {
					log.log(Level.SEVERE, Messages.getMessage("CWSAT0029E",jobId), e);
					job.setJobStopped();
					try {
						CloudantUtils.updateJob(job,false);
					} catch (Exception e2) {
						log.log(Level.SEVERE, Messages.getMessage("CWSAT0018E",jobId), e2);
					}
					return Response.status(Response.Status.CONFLICT).build();
				}
				return Response.ok().build();
			} else {
				log.log(Level.INFO, Messages.getMessage("CWSAT0028I",new String[] {jobId,job.getStatus()}));
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Invalid status for starting job. Job status: " + job.getStatus())
						.type(MediaType.TEXT_PLAIN).build();
			}
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdJobJobIdStopPut(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0030I",new String[] {jobId,workerId}));
			GetJob job = CloudantUtils.getJob(workerId, jobId);
			if (job.isSubJob()) {
				log.log(Level.INFO,Messages.getMessage("CWSAT0024W",new String[] {jobId,job.getMasterJobId()}));
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("Job: " + job.getId() + " is a sub job for batch job: " + job.getMasterJobId())
						.type(MediaType.TEXT_PLAIN).build();
			}
			// The job can only be put on pause if it is running
			if (job.isJobValidForRunning() && !job.isJobStopping()) {
				job.setJobStopping();
				CloudantUtils.updateJob(job);
				return Response.ok().build();
			} else {
				log.log(Level.INFO, Messages.getMessage("CWSAT0031I",new String[] {jobId,job.getStatus()}));
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Invalid status for stopping job. Job status: " + job.getStatus())
						.type(MediaType.TEXT_PLAIN).build();
			}
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdJobJobIdUnpausePut(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0032I",new String[] {jobId,workerId}));
			GetJob job = CloudantUtils.getJob(workerId, jobId);
			if (job.isSubJob()) {
				log.log(Level.INFO,Messages.getMessage("CWSAT0024W",new String[] {jobId,job.getMasterJobId()}));
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("Job: " + job.getId() + " is a sub job for batch job: " + job.getMasterJobId())
						.type(MediaType.TEXT_PLAIN).build();
			}
			// The job can only be put on pause if it is running
			if (job.isJobPaused() || job.isJobStopped()) {
				job.setJobStarting();
				CloudantUtils.updateJob(job,false);
				try {
					OutboundCallUtils.startOutboundCall(job, null);
				} catch (Exception e) {
					log.log(Level.SEVERE, Messages.getMessage("CWSAT0029E",jobId), e);
					job.setJobPaused();
					try {
						CloudantUtils.updateJob(job,false);
					} catch (Exception e2) {
						log.log(Level.SEVERE,Messages.getMessage("CWSAT0018E",jobId));
					}
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
				}
				return Response.ok().build();
			} else {
				log.log(Level.INFO, Messages.getMessage("CWSAT0033I",jobId));
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Invalid status for unpausing job. Job status: " + job.getStatus())
						.type(MediaType.TEXT_PLAIN).build();
			}
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdBatchJobDelete(String workerId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0034I",workerId));
			CloudantUtils.removeAllBatchJobsInWorker(workerId);
			return Response.ok().build();
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdBatchJobJobIdDelete(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0036I",new String[] {jobId,workerId}));
			BatchJob job = CloudantUtils.getBatchJob(workerId, jobId);
			CloudantUtils.removeBatchJob(workerId, job);
			return Response.ok().build();
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdBatchJobJobIdGet(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0035I",new String[] {jobId,workerId}));
			BatchJob job = CloudantUtils.getBatchJob(workerId, jobId);
			String JsonRepresentation = JsonbBuilder.create().toJson(job);
			return Response.ok(JsonRepresentation, MediaType.APPLICATION_JSON).build();
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdBatchJobJobIdPausePut(String workerId, String jobId,
			SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0037I",new String[] {jobId,workerId}));
			BatchJob job = CloudantUtils.getBatchJob(workerId, jobId);
			if (job.isJobValidForRunning() && !job.isJobPausing()) {
				CloudantUtils.pauseAllSubJobs(workerId, job);
				return Response.ok().build();
			} else {
				log.log(Level.INFO, Messages.getMessage("CWSAT0038I",new String[] {jobId,job.getStatus()}));
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Invalid status for pausing batch job. Batch job status: " + job.getStatus())
						.type(MediaType.TEXT_PLAIN).build();
			}
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdBatchJobJobIdStartPut(String workerId, String jobId, Integer jobsPerSecond,
			SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0039I",new String[] {jobId,workerId,String.valueOf(jobsPerSecond)}));

			GetWorker worker = CloudantUtils.getWorker(workerId);
			if(worker.getCases().isEmpty())
				return Response.status(Response.Status.BAD_REQUEST).entity("Can not start batch job from worker with empty cases array").type(MediaType.TEXT_PLAIN).build();
			BatchJob job = CloudantUtils.getBatchJob(workerId, jobId);
			if (job.isJobValidForReStarting()) {
				job.setStartTime(Instant.now().toEpochMilli());
				job.setStopTime(null);
				CloudantUtils.updateBatchJob(job,workerId);
				OutboundCallUtils.restartBatchCalls(job, worker, jobsPerSecond);
				return Response.ok().build();
			} else {
				log.log(Level.INFO, Messages.getMessage("CWSAT0040I",new String[] {jobId,job.getStatus()}));
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Invalid status for starting batch job. Batch job status: " + job.getStatus())
						.type(MediaType.TEXT_PLAIN).build();
			}
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdBatchJobJobIdStopPut(String workerId, String jobId, SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0041I",new String[] {jobId,workerId}));
			BatchJob job = CloudantUtils.getBatchJob(workerId, jobId);
			if (job.isJobValidForRunning() && !job.isJobStopping()) {
				CloudantUtils.stopAllSubJobs(workerId, job);
				return Response.ok().build();
			} else {
				log.log(Level.INFO, Messages.getMessage("CWSAT0042I",new String[] {jobId,job.getStatus()}));
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Invalid status for stopping batch job. Job status: " + job.getStatus())
						.type(MediaType.TEXT_PLAIN).build();
			}
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdBatchJobJobIdUnpausePut(String workerId, String jobId, Integer jobsPerSecond,
			SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0043I",new String[] {jobId,workerId,String.valueOf(jobsPerSecond)}));
			BatchJob job = CloudantUtils.getBatchJob(workerId, jobId);
			if (job.isJobPaused() || job.isJobStopped()) {
				OutboundCallUtils.unpauseBatchCalls(job, workerId, jobsPerSecond);
				return Response.ok().build();
			} else {
				log.log(Level.INFO, Messages.getMessage("CWSAT0044I",new String[] {jobId,job.getStatus()}));
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity("Invalid status for unpausing batch job. Batch job status: " + job.getStatus())
						.type(MediaType.TEXT_PLAIN).build();
			}
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId +"_"+jobId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public Response workerWorkerIdBatchJobPost(String workerId, Integer concurrent, Integer jobsPerSecond,
			SecurityContext securityContext) {
		try {
			log.log(Level.INFO, Messages.getMessage("CWSAT0045I",workerId));
			GetWorker worker = CloudantUtils.getWorker(workerId);
			if(worker.getCases().isEmpty())
				return Response.status(Response.Status.BAD_REQUEST).entity("Can not create batch job from worker with empty cases array").type(MediaType.TEXT_PLAIN).build();
			BatchJob job = BatchJob.newBatchJob(workerId, concurrent);
			// Add newly created job to worker
			worker.addBatchJobsItem(new GetWorkerJobs().jobID(job.getId()));
			// Update worker in db
			CloudantUtils.updateWorker(worker);
			// Post job to db
			CloudantUtils.createBatchJob(job);
			// Start batch jobs sub jobs
			OutboundCallUtils.startInitialBatchCall(job.getId(), worker, concurrent, jobsPerSecond);
			return Response.status(Response.Status.CREATED).entity(new Model201().id(job.getId())).build();
		} catch(NoDocumentException e) {
			log.log(Level.INFO, Messages.getMessage("CWSAT0001I",workerId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(ServerErrorException e) {
			log.log(Level.WARNING, Messages.getMessage("CWSAT0002W"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch (Exception e) {
			log.log(Level.SEVERE, Messages.getMessage("CWSAT0003E"), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}

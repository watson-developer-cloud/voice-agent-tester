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
package ibm.testing.microservice.api.rest.endpoints;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletConfig;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ibm.testing.microservice.api.WorkerApiService;
import ibm.testing.microservice.api.factories.WorkerApiServiceFactory;
import ibm.testing.microservice.models.BatchJob;
import ibm.testing.microservice.models.CreateWorker;
import ibm.testing.microservice.models.GetJob;
import ibm.testing.microservice.models.GetWorker;
import ibm.testing.microservice.models.Model201;

@Path("/v1/worker")
@Tag(ref = "Worker")
public class WorkerApi {
	private final WorkerApiService delegate;
	protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	public WorkerApi(@Context ServletConfig servletContext) {
		WorkerApiService delegate = null;

		if (servletContext != null) {
			String implClass = servletContext.getInitParameter("WorkerApi.implementation");
			if (implClass != null && !"".equals(implClass.trim())) {
				try {
					delegate = (WorkerApiService) Class.forName(implClass).newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (delegate == null) {
			delegate = WorkerApiServiceFactory.getWorkerApi();
		}

		this.delegate = delegate;
	}

	@POST
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Create a worker", description = "Creates a new worker which sets the outline to run jobs")
	@APIResponses(value = {
			@APIResponse(responseCode = "201", description = "Sucessfully created new worker", content = @Content(schema = @Schema(implementation = Model201.class))),
			@APIResponse(responseCode = "400", description = "Invalid worker"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server")})
	public Response workerPost(
			@RequestBody(description = "", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CreateWorker.class))) CreateWorker body,
			@Context SecurityContext securityContext){
		if(body==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Body is required. Must not be null.").type(MediaType.TEXT_PLAIN).build();
		StringBuilder sb=new StringBuilder("");
		for(ConstraintViolation<CreateWorker> err:validator.validate(body))
			sb.append(err.getMessage()+"\n");
		if(!sb.toString().isEmpty())
			return Response.status(Response.Status.BAD_REQUEST).entity(sb.toString()).type(MediaType.TEXT_PLAIN).build();
		return delegate.workerPost(body, securityContext);
	}

	@GET
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator", "testerViewer"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Get all workers", description = "Returns an array of all workers that belong to the namespace specified")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully got all workers", content = @Content(schema = @Schema(type=SchemaType.ARRAY,implementation = GetWorker.class))),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerGet(
			@Parameter(description = "Identifier for mapping user/role to resource", required = false) @DefaultValue("default")@QueryParam("namespace") String namespace,
			@Context SecurityContext securityContext){
		return delegate.workerGet(namespace,securityContext);
	}
	
	@DELETE
	@RolesAllowed({"testerAdministrator", "testerEditor"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Delete all workers", description = "Deletes all the workers that belong to the namespace specified")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully deleted all workers"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerDelete(
			@Parameter(description = "Identifier for mapping user/role to resource", required = false) @DefaultValue("default")@QueryParam("namespace") String namespace,
			@Context SecurityContext securityContext){
		return delegate.workerDelete(namespace,securityContext);
	}
	
	@GET
	@Path("/{worker-id}")
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator", "testerViewer"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Get a worker", description = "Returns the worker that corresponds to the ID specified")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Sucesfully got worker", content = @Content(schema = @Schema(implementation = GetWorker.class))),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "404", description = "Worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdGet(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdGet(workerId, securityContext);
	}

	@PUT
	@Path("/{worker-id}")
	@Consumes({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Modify a worker", description = "Modify the worker that corresponds to the ID specified")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully updated worker"),
			@APIResponse(responseCode = "400", description = "Invalid worker"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdPut(@RequestBody(description = "", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CreateWorker.class))) CreateWorker body,
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Context SecurityContext securityContext){
		if (body == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Body is required. Must not be null.")
					.type(MediaType.TEXT_PLAIN).build();
		StringBuilder sb = new StringBuilder("");
		if(body.getNamespace()!="default")
			return Response.status(Response.Status.BAD_REQUEST).entity("Namespace should not be provided when modifying worker").type(MediaType.TEXT_PLAIN).build();
		for (ConstraintViolation<CreateWorker> err : validator.validate(body))
			sb.append(err.getMessage() + "\n");
		if (!sb.toString().isEmpty())
			return Response.status(Response.Status.BAD_REQUEST).entity(sb.toString()).type(MediaType.TEXT_PLAIN)
					.build();
		return delegate.workerWorkerIdPut(body, workerId, securityContext);
	}

	@DELETE
	@Path("/{worker-id}")
	@RolesAllowed({"testerAdministrator", "testerEditor"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Delete a worker", description = "Deletes the worker that corresponds to the ID specified. IMPORTANT: All job data for the worker will be deleted as well")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully deleted worker"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdDelete(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdDelete(workerId, securityContext);
	}
	
	@POST
	@Path("/{worker-id}/job")
	@Tag(ref = "Job")
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Create a job", description = "Create and run a new Job which is the actual execution of the outline set by the worker that corresponds to the ID specified")
	@APIResponses(value = {
			@APIResponse(responseCode = "201", description = "Sucessfully created new Job", content = @Content(schema = @Schema(implementation = Model201.class))),
			@APIResponse(responseCode = "400", description = "Invalid worker"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Worker not found"),
			@APIResponse(responseCode = "409", description = "Job was created but not started"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdJobPost(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdJobPost(workerId, securityContext);
	}
	
	@DELETE
	@Path("/{worker-id}/job")
	@Tag(ref = "Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Delete all jobs from worker", description = "Delete all jobs that belong to the worker that corresponds to the ID specified. IMPORTANT: All job data for the worker will be lost")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully deleted all jobs"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdJobDelete(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdJobDelete(workerId, securityContext);
	}
	
	@GET
	@Path("/{worker-id}/job/{job-id}")
	@Tag(ref = "Job")
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator", "testerViewer"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Get a job", description = "Returns the job identified by job-id that belongs to the worker identified by worker-id")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Sucesfully got job", content = @Content(schema = @Schema(implementation = GetJob.class))),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "404", description = "Job or worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdJobJobIdGet(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdJobJobIdGet(workerId, jobId, securityContext);
	}

	@DELETE
	@Path("/{worker-id}/job/{job-id}")
	@Tag(ref = "Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Delete a job", description = "Deletes the job identified by job-id that belongs to the worker identified by worker-id")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully deleted job"),
			@APIResponse(responseCode = "400", description = "Invalid job"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Job or worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdJobJobIdDelete(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdJobJobIdDelete(workerId, jobId, securityContext);
	}

	@PUT
	@Path("/{worker-id}/job/{job-id}/pause")
	@Tag(ref = "Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Pause a job", description = "Will complete the currently running test the job is on and then pause the job at the next test case to run")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully paused job"),
			@APIResponse(responseCode = "400", description = "Invalid job"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Job or Worker not found"),
			@APIResponse(responseCode = "406", description = "Job in invalid state"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdJobJobIdPausePut(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdJobJobIdPausePut(workerId, jobId, securityContext);
	}

	@PUT
	@Path("/{worker-id}/job/{job-id}/unpause")
	@Tag(ref = "Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Unpause a job", description = "Will start the job at the test case and iteration where the job was paused.")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully unpaused job"),
			@APIResponse(responseCode = "400", description = "Invalid job"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "406", description = "Job in invalid state"),
			@APIResponse(responseCode = "404", description = "Job or Worker not found"),
			@APIResponse(responseCode = "409", description = "Job could not be unpaused"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdJobJobIdUnpausePut(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdJobJobIdUnpausePut(workerId, jobId, securityContext);
	}
	
	@PUT
	@Path("/{worker-id}/job/{job-id}/start")
	@Tag(ref = "Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Start a job", description = "If job is not running, this will clear the results and start it from the beginning. If running, this will clear the results and start the job from the beginning")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully started job"),
			@APIResponse(responseCode = "400", description = "Invalid job or worker"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Job or worker not found"),
			@APIResponse(responseCode = "406", description = "Job in invalid state"),
			@APIResponse(responseCode = "409", description = "Job could not be started"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdJobJobIdStartPut(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdJobJobIdStartPut(workerId, jobId, securityContext);
	}

	@PUT
	@Path("/{worker-id}/job/{job-id}/stop")
	@Tag(ref = "Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Stop a job", description = "Terminate the actively running test the job is on and reset job")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully stopped job"),
			@APIResponse(responseCode = "400", description = "Invalid job"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "406", description = "Job in invalid state"),
			@APIResponse(responseCode = "404", description = "Job or worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdJobJobIdStopPut(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdJobJobIdStopPut(workerId, jobId, securityContext);
	}
	
	
	
	
	
	
	@POST
	@Path("/{worker-id}/batchJob")
	@Tag(ref = "Batch Job")
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Create a batch job", description = "Create and run a new batch job which starts the amount of sub jobs you specify in the concurrent query")
	@APIResponses(value = {
			@APIResponse(responseCode = "201", description = "Sucessfully created Batch Job", content = @Content(schema = @Schema(implementation = Model201.class))),
			@APIResponse(responseCode = "400", description = "Invalid request or worker"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdBatchJobPost(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "Amount of jobs to run concurrently", required = false) @DefaultValue("1")@QueryParam("concurrent") Integer concurrent,
			@Parameter(description = "Amount of jobs to start per second. If not specified, it will start all sub  jobs at once", required = false) @QueryParam("jobsPerSecond") Integer jobsPerSecond,
			@Context SecurityContext securityContext){
		if(concurrent==null || concurrent<=0)
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Received invalid concurrent value: "+concurrent).type(MediaType.TEXT_PLAIN).build();
		if(jobsPerSecond!=null && jobsPerSecond<=0)
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Received invalid jobsPerSecond value: "+jobsPerSecond).type(MediaType.TEXT_PLAIN).build();
		return delegate.workerWorkerIdBatchJobPost(workerId,concurrent, jobsPerSecond,securityContext);
	}
	
	@DELETE
	@Path("/{worker-id}/batchJob")
	@Tag(ref = "Batch Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Delete all batch jobs from worker", description = "Delete all batch jobs that belong to the worker that corresponds to the ID specified. IMPORTANT: All batch job data for the worker will be lost")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully deleted all jobs"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdBatchJobDelete(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdBatchJobDelete(workerId, securityContext);
	}
	
	@GET
	@Path("/{worker-id}/batchJob/{job-id}")
	@Tag(ref = "Batch Job")
	@Produces({ "application/json" })
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator", "testerViewer"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Get a batch job", description = "Returns the batch job identified by job-id that belongs to the worker identified by worker-id")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "Sucesfully got batch job", content = @Content(schema = @Schema(implementation = BatchJob.class))),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "404", description = "Job or worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdBatchJobJobIdGet(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing batch job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdBatchJobJobIdGet(workerId, jobId, securityContext);
	}

	@DELETE
	@Path("/{worker-id}/batchJob/{job-id}")
	@Tag(ref = "Batch Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Delete a batch job", description = "Deletes all sub jobs in the batch job and itself from the worker identified by worker-id")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully deleted batch job"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Batch job or worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdBatchJobJobIdDelete(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing batch job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdBatchJobJobIdDelete(workerId, jobId, securityContext);
	}

	@PUT
	@Path("/{worker-id}/batchJob/{job-id}/pause")
	@Tag(ref = "Batch Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Pause a batch job", description = "Will complete the currently running test for sub jobs and then pause the job at the next test case to run")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Pausing batch job"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Batch job or Worker not found"),
			@APIResponse(responseCode = "406", description = "Batch job in invalid state"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdBatchJobJobIdPausePut(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing batch job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdBatchJobJobIdPausePut(workerId, jobId, securityContext);
	}

	@PUT
	@Path("/{worker-id}/batchJob/{job-id}/unpause")
	@Tag(ref = "Batch Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Unpause a batch job", description = "Will start sub jobs at the test case and iteration where the jobs were paused.")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Unpausing batch job"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "406", description = "Batch job in invalid state"),
			@APIResponse(responseCode = "404", description = "Batch job or Worker not found"),
			@APIResponse(responseCode = "409", description = "Batch job could not be unpaused"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdBatchJobJobIdUnpausePut(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing batch job", required = true) @PathParam("job-id") String jobId,
			@Parameter(description = "Amount of jobs to start per second. If not specified, it will start all sub  jobs at once", required = false) @QueryParam("jobsPerSecond") Integer jobsPerSecond,
			@Context SecurityContext securityContext){
		if(jobsPerSecond!=null && jobsPerSecond<=0)
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Received invalid jobsPerSecond value: "+jobsPerSecond).type(MediaType.TEXT_PLAIN).build();
		return delegate.workerWorkerIdBatchJobJobIdUnpausePut(workerId, jobId, jobsPerSecond, securityContext);
	}
	
	@PUT
	@Path("/{worker-id}/batchJob/{job-id}/start")
	@Tag(ref = "Batch Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Start a batch job", description = "If sub jobs are not running, this will clear their results and start it from the beginning. If running, this will clear the results and start sub jobs from the beginning")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Sucessfully started batch job"),
			@APIResponse(responseCode = "400", description = "Invalid request or worker"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "404", description = "Batch job or worker not found"),
			@APIResponse(responseCode = "406", description = "Batch job in invalid state"),
			@APIResponse(responseCode = "409", description = "Batch job could not be started"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdBatchJobJobIdStartPut(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing batch job", required = true) @PathParam("job-id") String jobId,
			@Parameter(description = "Amount of jobs to start per second. If not specified, it will start all sub  jobs at once", required = false) @QueryParam("jobsPerSecond") Integer jobsPerSecond,
			@Context SecurityContext securityContext){
		if(jobsPerSecond!=null && jobsPerSecond<=0)
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Received invalid jobsPerSecond value: "+jobsPerSecond).type(MediaType.TEXT_PLAIN).build();
		return delegate.workerWorkerIdBatchJobJobIdStartPut(workerId, jobId, jobsPerSecond, securityContext);
	}

	@PUT
	@Path("/{worker-id}/batchJob/{job-id}/stop")
	@Tag(ref = "Batch Job")
	@RolesAllowed({"testerAdministrator", "testerEditor", "testerOperator"})
	@SecurityRequirement(name = "AgentTesterBasicHttp")
	@Operation(summary = "Stop a batch job", description = "Terminate all actively running test on sub jobs and reset them")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Stopping batch job"),
			@APIResponse(responseCode = "401", description = "Unauthorized"),
			@APIResponse(responseCode = "403", description = "User doesn't have access to resource"),
			@APIResponse(responseCode = "406", description = "Batch job in invalid state"),
			@APIResponse(responseCode = "404", description = "Batch job or worker not found"),
			@APIResponse(responseCode = "500", description = "Something went wrong with the server") })
	public Response workerWorkerIdBatchJobJobIdStopPut(
			@Parameter(description = "The ID of an existing worker", required = true) @PathParam("worker-id") String workerId,
			@Parameter(description = "The ID of an existing batch job", required = true) @PathParam("job-id") String jobId,
			@Context SecurityContext securityContext){
		return delegate.workerWorkerIdBatchJobJobIdStopPut(workerId, jobId, securityContext);
	}
	
	
	

}

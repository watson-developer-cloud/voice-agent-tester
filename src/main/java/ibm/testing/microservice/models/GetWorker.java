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
package ibm.testing.microservice.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.google.gson.annotations.SerializedName;

/**
 * Model for what a worker could look like when running a GET
 */
@Schema(description = "Model for what a worker could look like")
@JsonbPropertyOrder(value = { "id","rev","name","description","namespace","iterations","failuresToIgnore","callDefinition","sipInviteHeaders","cases","jobs","batchJobs" })
public class GetWorker {
	@JsonbProperty("id")
	@SerializedName(value = "_id")
	private String id = null;
	
	@JsonbProperty("rev")
	@SerializedName(value = "_rev")
	private String rev = null;

	@JsonbProperty("name")
	private String name = null;

	@JsonbProperty("description")
	private String description = null;

	@JsonbProperty("namespace")
	private String namespace = null;

	@JsonbProperty("iterations")
	private Integer iterations = null;

	@JsonbProperty("failuresToIgnore")
	private Integer failuresToIgnore = null;
	
	@JsonbProperty("callDefinition")
	private CreateWorkerCallDefinition callDefinition=null;

	@JsonbProperty("cases")
	private List<CreateWorkerCases> cases = null;

	@JsonbProperty("jobs")
	private List<GetWorkerJobs> jobs = new ArrayList<GetWorkerJobs>();
	
	@JsonbProperty("batchJobs")
	private List<GetWorkerJobs> batchJobs = new ArrayList<GetWorkerJobs>();

	public GetWorker id(String _id) {
		this.id = _id;
		return this;
	}

	/**
	 * GUID which identifies the worker
	 * 
	 * @return id
	 **/
	@JsonbProperty("id")
	@SerializedName(value = "_id")
	@Schema(description = "GUID which identifies the worker")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GetWorker rev(String rev) {
		this.rev = rev;
		return this;
	}

	/**
	 * Version of the worker
	 * 
	 * @return rev
	 **/
	@JsonbProperty("rev")
	@SerializedName(value = "_rev")
	@Schema(description = "Version of the worker")
	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public GetWorker name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Name that describes worker
	 * 
	 * @return name
	 **/
	@JsonbProperty("name")
	@Schema(description = "Name that identifies worker")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GetWorker description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Brief description of the worker
	 * 
	 * @return description
	 **/
	@JsonbProperty("description")
	@Schema(description = "A brief description of what the worker does")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GetWorker iterations(Integer iterations) {
		this.iterations = iterations;
		return this;
	}

	/**
	 * Group to which the test case is bound to
	 * 
	 * @return namespace
	 **/
	@JsonbProperty("namespace")
	@Schema(description = "Identifies a group to which the test case is bound to")
	@NotEmpty
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public GetWorker namespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	/**
	 * Number of times that the worker will run each test case
	 * 
	 * @return iterations
	 **/
	@JsonbProperty("iterations")
	@Schema(description = "Number of times that the worker will run each test case")
	@NotNull
	public Integer getIterations() {
		return iterations;
	}

	public void setIterations(Integer iterations) {
		this.iterations = iterations;
	}

	public GetWorker failuresToIgnore(Integer failuresToIgnore) {
		this.failuresToIgnore = failuresToIgnore;
		return this;
	}

	/**
	 * Number of failures that it will allow before failing job
	 * 
	 * @return failuresToIgnore
	 **/
	@JsonbProperty("failuresToIgnore")
	@Schema(description = "Number of failures that allowed before failing job")
	public Integer getFailuresToIgnore() {
		return failuresToIgnore;
	}

	public void setFailuresToIgnore(Integer failuresToIgnore) {
		this.failuresToIgnore = failuresToIgnore;
	}

	
	/**
	 * Json object that defines the outbound call definition the jobs will run
	 * 
	 * @return callDefinition
	 **/
	@JsonbProperty("callDefinition")
	@Schema(required = false, description = "Json object that defines the outbound call definition the jobs will run")
	@NotNull
	@Valid
	public CreateWorkerCallDefinition getCallDefinition() {
		return callDefinition;
	}

	public void setCallDefinition(CreateWorkerCallDefinition callDefinition) {
		this.callDefinition = callDefinition;
	}

	public GetWorker callDefinition(CreateWorkerCallDefinition callDefinition) {
		this.callDefinition = callDefinition;
		return this;
	}


	public GetWorker cases(List<CreateWorkerCases> cases) {
		this.cases = cases;
		return this;
	}

	public GetWorker addCasesItem(CreateWorkerCases casesItem) {
		if (this.cases == null) {
			this.cases = new ArrayList<CreateWorkerCases>();
		}
		this.cases.add(casesItem);
		return this;
	}

	/**
	 * Array of test cases that the worker will test against when running jobs
	 * 
	 * @return cases
	 **/
	@JsonbProperty("cases")
	@Schema(description = "Array of test cases that the worker will test against when running jobs")
	@NotNull
	public List<CreateWorkerCases> getCases() {
		return cases;
	}

	public void setCases(List<CreateWorkerCases> cases) {
		this.cases = cases;
	}

	public GetWorker jobs(List<GetWorkerJobs> jobs) {
		this.jobs = jobs;
		return this;
	}

	public GetWorker addJobsItem(GetWorkerJobs jobsItem) {
		if (this.jobs == null) {
			this.jobs = new ArrayList<GetWorkerJobs>();
		}
		this.jobs.add(jobsItem);
		return this;
	}

	/**
	 * Array of jobs that are managed by the worker
	 * 
	 * @return jobs
	 **/
	@JsonbProperty("jobs")
	@Schema(description = "Array of jobs that are managed by the worker")
	public List<GetWorkerJobs> getJobs() {
		return jobs;
	}

	public void setJobs(List<GetWorkerJobs> jobs) {
		this.jobs = jobs;
	}
	
	
	
	public GetWorker batchJobs(List<GetWorkerJobs> batchJobs) {
		this.batchJobs = batchJobs;
		return this;
	}

	public GetWorker addBatchJobsItem(GetWorkerJobs batchJob) {
		if (this.batchJobs == null) {
			this.batchJobs = new ArrayList<GetWorkerJobs>();
		}
		this.batchJobs.add(batchJob);
		return this;
	}

	/**
	 * Array of batch jobs that are managed by the worker
	 * 
	 * @return batchJobs
	 **/
	@JsonbProperty("batchJobs")
	@Schema(description = "Array of batch jobs that are managed by the worker")
	public List<GetWorkerJobs> getBatchJobs() {
		return batchJobs;
	}

	public void setBatchJobs(List<GetWorkerJobs> batchJobs) {
		this.batchJobs = batchJobs;
	}
	
	
	
	
	
	public void updateWorker(CreateWorker worker) {
		this.name(worker.getName())
			.description(worker.getDescription())
			.iterations(worker.getIterations())
			.failuresToIgnore(worker.getFailuresToIgnore())
			.callDefinition(worker.getCallDefinition())
			.cases(worker.getCases());
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GetWorker getWorker = (GetWorker) o;
		return Objects.equals(this.id, getWorker.id) && Objects.equals(this.rev, getWorker.rev)
				&& Objects.equals(this.name, getWorker.name) && Objects.equals(this.description, getWorker.description)
				&& Objects.equals(this.namespace, getWorker.namespace)
				&& Objects.equals(this.iterations, getWorker.iterations)
				&& Objects.equals(this.callDefinition, getWorker.callDefinition)
				&& Objects.equals(this.failuresToIgnore, getWorker.failuresToIgnore)
				&& Objects.equals(this.cases, getWorker.cases) && Objects.equals(this.jobs, getWorker.jobs)
				&& Objects.equals(this.batchJobs, getWorker.batchJobs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, rev, name, description, namespace, iterations, callDefinition, failuresToIgnore, cases, jobs, batchJobs);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GetWorker {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    rev: ").append(toIndentedString(rev)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
		sb.append("    iterations: ").append(toIndentedString(iterations)).append("\n");
		sb.append("    callDefinition: ").append(toIndentedString(callDefinition)).append("\n");
		sb.append("    failuresToIgnore: ").append(toIndentedString(failuresToIgnore)).append("\n");
		sb.append("    cases: ").append(toIndentedString(cases)).append("\n");
		sb.append("    jobs: ").append(toIndentedString(jobs)).append("\n");
		sb.append("    batchJobs: ").append(toIndentedString(batchJobs)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}

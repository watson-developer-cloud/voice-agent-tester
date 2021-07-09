/*
 * (C) Copyright IBM Corp. 2019, 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ibm.testing.microservice.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.json.bind.annotation.JsonbNumberFormat;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.google.gson.annotations.SerializedName;

/**
 * Model for what a Job could look like when running a GET
 */
@Schema(description = "Model for what a Job could look like")
@JsonbPropertyOrder(value = { "id","status","startTime","stopTime","percentComplete","iterationNumber","caseNumber","turnNumber","casesExecuted","currentCase","workerUsed","results" })
public class GetJob {
	@JsonbProperty("id")
	@SerializedName(value = "_id")
	private String id = null;

	@JsonbTransient
	@SerializedName(value = "_rev")
	private String rev = null;
	
	@JsonbTransient
	private String callMetadata = null;
	
	@JsonbTransient
	private String masterJobId = null;
	
	@JsonbProperty("status")
	private String status = null;
	//Options of what status could be
	public static final String CREATED="created";
	public static final String STARTING="starting";
	public static final String RESTARTING="re-starting";
	public static final String RUNNING="running";
	public static final String COMPLETED="completed";
	public static final String FAILED="failed";
	public static final String PAUSED="paused";
	public static final String PAUSING="pausing";
	public static final String STOPPED="stopped";
	public static final String STOPPING="stopping";
	public static final String INVALID="invalid";
	public static final ArrayList<String> INVALID_RUNNING= new ArrayList<String>(Arrays.asList(new String[]{COMPLETED,FAILED,PAUSED,STOPPED,INVALID,CREATED}));

	@JsonbProperty("startTime")
	private Long startTime = null;
	
	@JsonbProperty("percentComplete")
	private Double percentComplete = null;

	@JsonbProperty("iterationNumber")
	private Integer iterationNumber = null;

	@JsonbProperty("caseNumber")
	private Integer caseNumber = null;
	
	@JsonbProperty("turnNumber")
	private Integer turnNumber = null;

	@JsonbProperty("stopTime")
	private Long stopTime = null;
	
	@JsonbProperty("casesExecuted")
	private List<GetJobCases> casesExecuted = new ArrayList<GetJobCases>();
	
	@JsonbProperty("currentCase")
	private GetTestCase currentCase = null;
	
	@JsonbProperty("workerUsed")
	private GetWorker workerUsed = null;
	
	@JsonbProperty("results")
	private GetJobResults results = new GetJobResults();
	

	public GetJob id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * GUID that identifies the job
	 * 
	 * @return id
	 **/
	@JsonbProperty("id")
	@Schema(description = "GUID which identifies the job")
	@SerializedName(value = "_id")
	public String getId() {
		return id.split("_")[1];
	}

	public void setId(String id) {
		this.id = id;
	}

	public GetJob rev(String rev) {
		this.rev = rev;
		return this;
	}

	/**
	 * Revision used by Cloudant for updating
	 * 
	 * @return rev
	 **/
	@JsonbTransient
	@SerializedName(value = "_rev")
	@Schema(description = "Revision used by Cloudant for updating", hidden = true)
	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	private GetJob status(String status) {
		this.status = status;
		return this;
	}
	
	/**
	 * Metadata of current call to map job
	 * 
	 * @return callMetadata
	 **/
	@JsonbTransient
	@Schema(hidden = true)
	public String getCallMetadata() {
		return callMetadata;
	}

	public void setCallMetadata(String callMetadata) {
		this.callMetadata = callMetadata;
	}

	private GetJob callMetadata(String callMetadata) {
		this.callMetadata = callMetadata;
		return this;
	}

	/**
	 * The current state of the job
	 * 
	 * @return status
	 **/
	@JsonbProperty("status")
	@Schema(description = "The current state of the job")
	@NotEmpty
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public GetJob startTime(Long startTime) {
		this.startTime = startTime;
		return this;
	}

	/**
	 * Time at which the job started running
	 * 
	 * @return startTime
	 **/
	@JsonbProperty("startTime")
	@Schema(description = "Time at which the job was started")
	@NotNull
	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public GetJob percentComplete(Double percentComplete) {
		this.percentComplete = percentComplete;
		return this;
	}

	/**
	 * Percent of job completed
	 * 
	 * @return percentComplete
	 **/
	@JsonbProperty("percentComplete")
	@JsonbNumberFormat(value="#0.00")
	@Schema(description = "Percent of job completed")
	public Double getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(Double percentComplete) {
		this.percentComplete = percentComplete;
	}

	public GetJob iterationNumber(Integer iterationNumber) {
		this.iterationNumber = iterationNumber;
		return this;
	}

	/**
	 * The iteration number the job is currently at
	 * 
	 * @return iterationNumber
	 **/
	@JsonbProperty("iterationNumber")
	@Schema(description = "The iteration number the job is currently at")
	public Integer getIterationNumber() {
		return iterationNumber;
	}

	public void setIterationNumber(Integer iterationNumber) {
		this.iterationNumber = iterationNumber;
	}

	public GetJob turnNumber(Integer turnNumber) {
		this.turnNumber = turnNumber;
		return this;
	}
	
	/**
	 * Index of test case inside the worker cases the job is currently at
	 * 
	 * @return caseNumber
	 **/
	@JsonbProperty("caseNumber")
	@Schema(description = "Index of test case inside the worker cases the job is currently at")
	public Integer getCaseNumber() {
		return caseNumber;
	}

	public void setCaseNumber(Integer caseNumber) {
		this.caseNumber = caseNumber;
	}

	public GetJob caseNumber(Integer caseNumber) {
		this.caseNumber = caseNumber;
		return this;
	}
	
	/**
	 * Test cases the job executes
	 * 
	 * @return casesExecuted
	 **/
	@JsonbProperty("casesExecuted")
	@Schema(description = "Test cases the job has reached")
	public List<GetJobCases> getCasesExecuted() {
		return casesExecuted;
	}

	public void setCasesExecuted(List<GetJobCases> cases) {
		this.casesExecuted = cases;
	}

	public GetJob addTestCase(GetTestCase testCase) {
		this.casesExecuted.add(new GetJobCases().caseId(testCase.getId()).latestRev(testCase.getRev()).statusDuringExecution(GetJobCases.UNCHANGED));
		return this;
	}
	
	public void manageMissingTestCase(String caseId) {
		int index=-1;
		for(int i=0;i<this.getCasesExecuted().size();i++) {
			if(this.getCasesExecuted().get(i).getCaseId().equals(caseId)) {
				index=i;
				break;
			}
		}
		// Add deleted test case
		if(index==-1) {
			this.getCasesExecuted().add(new GetJobCases().caseId(caseId).statusDuringExecution(GetJobCases.DELETED));
		}
		// Modify status of test case that was deleted
		else {
			this.getCasesExecuted().get(index).statusDuringExecution(GetJobCases.DELETED);
		}
	}
	
	public int indexOfCase(GetTestCase testCase) {
		for(int i=0;i<this.getCasesExecuted().size();i++) {
			if(this.getCasesExecuted().get(i).getCaseId().equals(testCase.getId()))
				return i;
		}
		return -1;
	}
	
	/**
	 * Test case the job is currently running
	 * 
	 * @return currentCase
	 **/
	@JsonbProperty("currentCase")
	@Schema(description = "Test case the job is currently running")
	public GetTestCase getCurrentCase() {
		return currentCase;
	}

	public void setCurrentCase(GetTestCase testCase) {
		this.currentCase = testCase;
	}

	public GetJob currentCase(GetTestCase testCase) {
		this.currentCase = testCase;
		return this;
	}

	/**
	 * Turn number in test case the job is currently validating
	 * 
	 * @return turnNumber
	 **/
	@JsonbProperty("turnNumber")
	@Schema(description = "Turn number in test case the job is currently validating")
	public Integer getTurnNumber() {
		return turnNumber;
	}

	public void setTurnNumber(Integer turnNumber) {
		this.turnNumber = turnNumber;
	}

	public GetJob stopTime(Long stopTime) {
		this.stopTime = stopTime;
		return this;
	}

	/**
	 * Time at which the job finished
	 * 
	 * @return stopTime
	 **/
	@JsonbProperty("stopTime")
	@Schema(description = "Time at which the job finished")
	public Long getStopTime() {
		return stopTime;
	}

	public void setStopTime(Long stopTime) {
		this.stopTime = stopTime;
	}

	public GetJob results(GetJobResults results) {
		this.results = results;
		return this;
	}

	/**
	 * Get results
	 * 
	 * @return results
	 **/
	@JsonbProperty("results")
	@Schema(description = "")
	@NotNull
	public GetJobResults getResults() {
		return results;
	}

	public void setResults(GetJobResults results) {
		this.results = results;
	}
	
	public void addResultFailure(GetJobResultsFailures failure) {
		this.results.addFailuresItem(failure);
		this.results.incrementFailures();
	}
	
	
	/**
	 * Worker outline used by the job for running the tests
	 * 
	 * @return workerUsed
	 **/
	@JsonbProperty("workerUsed")
	@Schema(description = "Worker outline used by the job for running the tests")
	@NotNull
	public GetWorker getWorkerUsed() {
		return workerUsed;
	}

	public void setWorkerUsed(GetWorker workerUsed) {
		this.workerUsed = workerUsed;
	}

	public GetJob workerUsed(GetWorker workerUsed) {
		this.workerUsed = workerUsed;
		return this;
	}
	
	public GetJob masterJobId(String masterJobId) {
		this.masterJobId = masterJobId;
		return this;
	}

	/**
	 * GUID which identifies the master job of which it is a sub job of
	 * 
	 * @return masterJobId
	 **/
	@JsonbTransient
	@Schema(description = "GUID which identifies the master job of which it is a sub job of",hidden=true)
	public String getMasterJobId() {
		return masterJobId;
	}

	public void setMasterJobId(String masterJobId) {
		this.masterJobId = masterJobId;
	}
	
	public static GetJob newJobToStartCall(GetWorker worker) {
		return new GetJob()
				.id(worker.getId()+"_"+(UUID.randomUUID().toString().replace("-", "")))
				.startTime(Instant.now().toEpochMilli())
				.caseNumber(0)
				.iterationNumber(1)
				.turnNumber(0)
				.status(CREATED)
				.percentComplete(worker.getIterations()==0?null:0.0)
				.workerUsed(worker);
	}
	
	public static GetJob newJobToStartCall(GetWorker worker,String masterJobId) {
		return new GetJob()
				.id(worker.getId()+"_"+(UUID.randomUUID().toString().replace("-", "")))
				.masterJobId(masterJobId)
				.startTime(Instant.now().toEpochMilli())
				.caseNumber(0)
				.iterationNumber(1)
				.turnNumber(0)
				.status(CREATED)
				.percentComplete(worker.getIterations()==0?null:0.0)
				.workerUsed(worker);
	}
	
	public void updateCurrentCase(GetTestCase testCase) {
		int index=this.indexOfCase(testCase);
		if(index==-1) {
			this.addTestCase(testCase);
		}else if(!this.getCasesExecuted().get(index).getLatestRev().equals(testCase.getRev())) {
			this.getCasesExecuted().get(index).setLatestRev(testCase.getRev());
			this.getCasesExecuted().get(index).setStatusDuringExecution(GetJobCases.CHANGED);;
		}
		this.currentCase=testCase;
	}
	
	public void reset() {
		this.startTime(Instant.now().toEpochMilli())
				.stopTime(null)
				.caseNumber(0)
				.turnNumber(0)
				.percentComplete(this.getWorkerUsed().getIterations()==0?null:0.0)
				.iterationNumber(1)
				.status(GetJob.STOPPED)
				.currentCase(null)
				.callMetadata(null);
		this.casesExecuted.clear();
		this.results.resetResults();		
	}
	
	public void setJobCompleted() {
		this.caseNumber(null)
		.currentCase(null)
		.callMetadata(null)
		.turnNumber(null)
		.percentComplete(null)
		.iterationNumber(null)
		.stopTime(Instant.now().toEpochMilli())
		.status(COMPLETED);
	}
	
	public void setJobFailed() {
		this.caseNumber(null)
		.currentCase(null)
		.turnNumber(null)
		.callMetadata(null)
		.percentComplete(null)
		.iterationNumber(null)
		.stopTime(Instant.now().toEpochMilli())
		.status(FAILED);
	}
	
	public void goToNewIteration() {
		this.iterationNumber(this.getIterationNumber() + 1)
		.caseNumber(0)
		.turnNumber(0)
		.callMetadata(null)
		.currentCase(null);
	}
	
	public void goToNewIteration(double percentIncrement) {
		this.iterationNumber(this.getIterationNumber() + 1)
		.caseNumber(0)
		.turnNumber(0)
		.callMetadata(null)
		.currentCase(null)
		.percentComplete(this.getPercentComplete()+percentIncrement);
	}
	
	public void goToNewTestCase(double percentIncrement) {
		this.caseNumber(this.getCaseNumber()+1)
			.percentComplete(this.getPercentComplete()+percentIncrement)
			.turnNumber(0)
			.callMetadata(null)
			.currentCase(null);
	}
	
	public void goToNewTestCase() {
		this.caseNumber(this.getCaseNumber()+1)
			.turnNumber(0)
			.callMetadata(null)
			.currentCase(null);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobRunning() {
		return this.getStatus().equals(RUNNING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobStopping() {
		return this.getStatus().equals(STOPPING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobPausing() {
		return this.getStatus().equals(PAUSING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobCreated() {
		return this.getStatus().equals(CREATED);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobReStarting() {
		return this.getStatus().equals(RESTARTING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobStarting() {
		return this.getStatus().equals(STARTING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobPaused() {
		return this.getStatus().equals(PAUSED);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobStopped() {
		return this.getStatus().equals(STOPPED);
	}

	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobValidForRunning() {
		return !INVALID_RUNNING.contains(this.getStatus());
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobValidForReStarting() {
		return !this.getStatus().equals(RESTARTING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isSubJob() {
		return this.getMasterJobId()!=null;
	}
	
	public void setJobRunning() {
		this.status(RUNNING);
	}
	
	public void setJobStopping() {
		this.status(STOPPING);
	}
	
	public void setJobPausing() {
		this.status(PAUSING);
	}
	
	public void setJobCreated() {
		this.status(STARTING);
	}
	
	public void setJobReStarting() {
		this.status(RESTARTING);
	}
	
	public void setJobStarting() {
		this.status(STARTING);
	}
	
	public void setJobInvalid() {
		this.status(INVALID)
			.currentCase(null)
			.callMetadata(null);
	}
	
	public void setJobStopped() {
		this.status(STOPPED)
			.currentCase(null)
			.callMetadata(null);
	}
	
	public void setJobPaused() {
		this.status(PAUSED)
			.currentCase(null)
			.callMetadata(null);
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GetJob getJob = (GetJob) o;
		return Objects.equals(this.id, getJob.id) && Objects.equals(this.rev, getJob.rev)
				&& Objects.equals(this.status, getJob.status) && Objects.equals(this.startTime, getJob.startTime)
				&& Objects.equals(this.percentComplete, getJob.percentComplete)&& Objects.equals(this.callMetadata, getJob.callMetadata)
				&& Objects.equals(this.iterationNumber, getJob.iterationNumber)&& Objects.equals(this.caseNumber, getJob.caseNumber)
				&& Objects.equals(this.turnNumber, getJob.turnNumber) && Objects.equals(this.stopTime, getJob.stopTime)
				&& Objects.equals(this.results, getJob.results)&& Objects.equals(this.currentCase, getJob.currentCase)
				&& Objects.equals(this.casesExecuted, getJob.casesExecuted)&& Objects.equals(this.workerUsed, getJob.workerUsed);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, rev, status, startTime, percentComplete, callMetadata, iterationNumber, caseNumber, turnNumber, stopTime,
				results, currentCase, casesExecuted, workerUsed);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GetJob {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    rev: ").append(toIndentedString(rev)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
		sb.append("    stopTime: ").append(toIndentedString(stopTime)).append("\n");
		sb.append("    callMetadata: ").append(toIndentedString(callMetadata)).append("\n");
		sb.append("    percentComplete: ").append(toIndentedString(percentComplete)).append("\n");
		sb.append("    iterationNumber: ").append(toIndentedString(iterationNumber)).append("\n");
		sb.append("    turnNumber: ").append(toIndentedString(turnNumber)).append("\n");
		sb.append("    caseNumber: ").append(toIndentedString(caseNumber)).append("\n");
		sb.append("    currentCase: ").append(toIndentedString(currentCase)).append("\n");
		sb.append("    workerUsed: ").append(toIndentedString(workerUsed)).append("\n");
		sb.append("    results: ").append(toIndentedString(results)).append("\n");
		sb.append("    casesExecuted: ").append(toIndentedString(casesExecuted)).append("\n");
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

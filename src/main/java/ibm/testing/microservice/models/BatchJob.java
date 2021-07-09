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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.json.bind.annotation.JsonbNumberFormat;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;
import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.google.gson.annotations.SerializedName;

/**
 * Model for what a Batch Job could look like when running a GET
 */
@Schema(description = "Model for what a Batch Job could look like")
@JsonbPropertyOrder(value = { "id","status","concurrentJobs","startTime","stopTime","percentComplete","subJobs" })
public class BatchJob {

	@JsonbProperty("id")
	@SerializedName(value = "_id")
	private String id = null;

	@JsonbTransient
	@SerializedName(value = "_rev")
	private String rev = null;
	
	@JsonbProperty("concurrentJobs")
	private Integer concurrentJobs = null;
	
	@JsonbProperty("status")
	private String status = null;
	
	@JsonbProperty("startTime")
	private Long startTime = null;
	
	@JsonbProperty("percentComplete")
	private Double percentComplete = null;
	
	@JsonbProperty("stopTime")
	private Long stopTime = null;
	
	@JsonbProperty("subJobs")
	private List<SubJobs> subJobs = new ArrayList<SubJobs>();

	
	public BatchJob id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * GUID that identifies the job
	 * 
	 * @return id
	 **/
	@JsonbProperty("id")
	@SerializedName(value = "_id")
	@Schema(description = "GUID which identifies the batch job")
	public String getId() {
		return id.split("_")[1];
	}

	public void setId(String id) {
		this.id = id;
	}

	public BatchJob rev(String rev) {
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

	public BatchJob status(String status) {
		this.status = status;
		return this;
	}
	
	/**
	 * The current state of the job
	 * 
	 * @return status
	 **/
	@JsonbProperty("status")
	@Schema(description = "The current state of the job")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public BatchJob startTime(Long startTime) {
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

	public BatchJob percentComplete(Double percentComplete) {
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
	
	public BatchJob stopTime(Long stopTime) {
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

	public BatchJob subJobs(List<SubJobs> subJobs) {
		this.subJobs = subJobs;
		return this;
	}

	/**
	 * Array of jobs managed by the batch job
	 * 
	 * @return subJobs
	 **/
	@JsonbProperty("subJobs")
	@Schema(description = "Array of jobs managed by the batch job")
	public List<SubJobs> getSubJobs() {
		return subJobs;
	}

	public void setSubJobs(List<SubJobs> subJobs) {
		this.subJobs = subJobs;
	}
	
	public void addSubJob(SubJobs job) {
		this.subJobs.add(job);
	}
	
	
	public static BatchJob newBatchJob(String workerId,Integer concurrentJobs) {
		return new BatchJob()
				.id(workerId+"_"+(UUID.randomUUID().toString().replace("-", "")))
				.startTime(Instant.now().toEpochMilli())
				.concurrentJobs(concurrentJobs);
	}
	
	/**
	 * Number of concurrent jobs that the batch job manages
	 * 
	 * @return concurrentJobs
	 **/
	@JsonbProperty("concurrentJobs")
	@Schema(description = "Number of concurrent jobs that the batch job manages")
	@NotNull
	public Integer getConcurrentJobs() {
		return concurrentJobs;
	}

	public void setConcurrentJobs(Integer concurrentJobs) {
		this.concurrentJobs = concurrentJobs;
	}
	
	public BatchJob concurrentJobs(Integer concurrentJobs) {
		this.concurrentJobs = concurrentJobs;
		return this;
	}
	
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobStopping() {
		return this.getStatus().equals(GetJob.STOPPING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobPausing() {
		return this.getStatus().equals(GetJob.PAUSING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobCreated() {
		return this.getStatus().equals(GetJob.CREATED);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobReStarting() {
		return this.getStatus().equals(GetJob.RESTARTING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobStarting() {
		return this.getStatus().equals(GetJob.STARTING);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobPaused() {
		return this.getStatus().equals(GetJob.PAUSED);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobStopped() {
		return this.getStatus().equals(GetJob.STOPPED);
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobValidForRunning() {
		return !GetJob.INVALID_RUNNING.contains(this.getStatus());
	}
	
	@JsonbTransient
	@Schema(hidden=true)
	public boolean isJobValidForReStarting() {
		return !this.getStatus().equals(GetJob.RESTARTING);
	}
	
	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BatchJob batchJob = (BatchJob) o;
		return Objects.equals(this.id, batchJob.id) && Objects.equals(this.rev, batchJob.rev)
				&& Objects.equals(this.status, batchJob.status) && Objects.equals(this.startTime, batchJob.startTime)
				&& Objects.equals(this.percentComplete, batchJob.percentComplete)
				&& Objects.equals(this.stopTime, batchJob.stopTime)
				&& Objects.equals(this.subJobs, batchJob.subJobs)&& Objects.equals(this.concurrentJobs, batchJob.concurrentJobs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, rev, concurrentJobs, status, startTime, percentComplete, stopTime, subJobs);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class BatchJob {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    rev: ").append(toIndentedString(rev)).append("\n");
		sb.append("    concurrentJobs: ").append(toIndentedString(concurrentJobs)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
		sb.append("    stopTime: ").append(toIndentedString(stopTime)).append("\n");
		sb.append("    percentComplete: ").append(toIndentedString(percentComplete)).append("\n");
		sb.append("    subJobs: ").append(toIndentedString(subJobs)).append("\n");
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

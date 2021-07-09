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

import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JsonbPropertyOrder(value = { "id","status","failures"})
public class SubJobs {

	@JsonbProperty("id")
	private String id = null;
	
	@JsonbProperty("status")
	private String status = null;
	
	@JsonbProperty("failures")
	private Integer failures = null;
	
	@JsonbTransient
	private Double percentComplete = null;
	
	@JsonbTransient
	private Long maxStopTime = null;
	
	public SubJobs id(String id) {
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
	public String getId() {
		return id.split("_")[1];
	}

	public void setId(String id) {
		this.id = id;
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
	
	public SubJobs status(String status) {
		this.status = status;
		return this;
	}
	
	/**
	 * The number of failures for the job
	 * 
	 * @return failures
	 **/
	@JsonbProperty("failures")
	@Schema(description = "The number of failures for the job")
	public Integer getFailures() {
		return failures;
	}

	public void setFailures(Integer failures) {
		this.failures = failures;
	}
	
	public SubJobs failures(Integer failures) {
		this.failures = failures;
		return this;
	}
	
	
	/**
	 * The percentage completed of the job
	 * 
	 * @return percentComplete
	 **/
	@JsonbTransient
	@Schema(description = "The percentage completed of the job",hidden=true)
	public Double getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(Double percentComplete) {
		this.percentComplete = percentComplete;
	}
	
	public SubJobs percentComplete(Double percentComplete) {
		this.percentComplete = percentComplete;
		return this;
	}
	
	/**
	 * The stop time of the job. Used for calculating the last stop time
	 * 
	 * @return maxStopTime
	 **/
	@JsonbTransient
	@Schema(description = "The stop time of the job. Used for calculating the last stop time",hidden=true)
	public Long getMaxStopTime() {
		return maxStopTime;
	}

	public void setMaxStopTime(Long maxStopTime) {
		this.maxStopTime = maxStopTime;
	}
	
	public SubJobs maxStopTime(Long maxStopTime) {
		this.maxStopTime = maxStopTime;
		return this;
	}
	
	
	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SubJobs subJob = (SubJobs) o;
		return Objects.equals(this.id, subJob.id)&& Objects.equals(this.status, subJob.status) && Objects.equals(this.failures, subJob.failures);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, status, failures);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SubJobs {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    failures: ").append(toIndentedString(failures)).append("\n");
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

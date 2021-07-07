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

import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class GetJobCases {
	
	// Possible status changes
	public static final String DELETED="deleted";
	public static final String CHANGED="modified";
	public static final String UNCHANGED="unchanged";
	
	
	@JsonbProperty("caseId")
	private String caseId=null;
	
	@JsonbProperty("latestRev")
	private String latestRev=null;
	
	@JsonbProperty("statusDuringExecution")
	private String statusDuringExecution=DELETED;
	
	public GetJobCases caseId(String id) {
		this.caseId = id;
		return this;
	}

	/**
	 * GUID of test case that job will execute each iteration
	 * 
	 * @return caseId
	 **/
	@JsonbProperty("caseId")
	@Schema(description = "GUID of test case that job will execute each iteration")
	@NotNull(message = "CaseId must not be null")
	@Size(min = 1, message = "CaseId must not be empty")
	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String id) {
		this.caseId = id;
	}
	
	public GetJobCases latestRev(String rev) {
		this.latestRev = rev;
		return this;
	}

	/**
	 * Latest version of the test case that job executed
	 * 
	 * @return latestRev
	 **/
	@JsonbProperty("latestRev")
	@Schema(description = "Latest version of the test case that job executed")
	@NotNull(message = "latestRev must not be null")
	@Size(min = 1, message = "latestRev must not be empty")
	public String getLatestRev() {
		return latestRev;
	}

	public void setLatestRev(String rev) {
		this.latestRev = rev;
	}
	
	public GetJobCases statusDuringExecution(String statusDuringExecution) {
		this.statusDuringExecution = statusDuringExecution;
		return this;
	}

	/**
	 * String that specifies if the test case was tampered with during job execution
	 * 
	 * @return statusDuringExecution
	 **/
	@JsonbProperty("statusDuringExecution")
	@Schema(description = "String that specifies if the test case was tampered with during job execution")
	public String getStatusDuringExecution() {
		return statusDuringExecution;
	}

	public void setStatusDuringExecution(String statusDuringExecution) {
		this.statusDuringExecution = statusDuringExecution;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GetJobCases getJobCases = (GetJobCases) o;
		return Objects.equals(this.caseId, getJobCases.caseId)&&
				Objects.equals(this.latestRev,  getJobCases.latestRev) &&
				Objects.equals(this.statusDuringExecution,  getJobCases.statusDuringExecution);
	}

	@Override
	public int hashCode() {
		return Objects.hash(caseId,latestRev,statusDuringExecution);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GetJobCases {\n");

		sb.append("    caseId: ").append(toIndentedString(caseId)).append("\n");
		sb.append("    latestRev: ").append(toIndentedString(latestRev)).append("\n");
		sb.append("    statusDuringExecution: ").append(toIndentedString(statusDuringExecution)).append("\n");
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

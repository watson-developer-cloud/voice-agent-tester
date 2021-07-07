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

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * GetWorkerJobs
 */
public class GetWorkerJobs   {
	@JsonbProperty("jobID")
  private String jobID = null;

  public GetWorkerJobs jobID(String jobID) {
    this.jobID = jobID;
    return this;
  }

  /**
   * GUID of job started by worker
   * @return jobID
   **/
  @JsonbProperty("jobID")
  @Schema(description = "GUID of job started by worker")
  public String getJobID() {
    return jobID;
  }

  public void setJobID(String jobID) {
    this.jobID = jobID;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetWorkerJobs getWorkerJobs = (GetWorkerJobs) o;
    return Objects.equals(this.jobID, getWorkerJobs.jobID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jobID);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetWorkerJobs {\n");
    
    sb.append("    jobID: ").append(toIndentedString(jobID)).append("\n");
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

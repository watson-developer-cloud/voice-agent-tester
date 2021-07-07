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
import javax.json.bind.annotation.JsonbPropertyOrder;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * GetJobResultsFailures
 */
@JsonbPropertyOrder(value = { "time","error","testCaseID","testCaseName","turnNumber","iteration","sipCallID" })
public class GetJobResultsFailures   {
	@JsonbProperty("time")
  private Long time = null;

	@JsonbProperty("error")
  private String error = null;

	@JsonbProperty("testCaseID")
  private String testCaseID = null;

	@JsonbProperty("testCaseName")
  private String testCaseName = null;

	@JsonbProperty("turnNumber")
  private Integer turnNumber = null;

	@JsonbProperty("iteration")
  private Integer iteration = null;

	@JsonbProperty("sipCallID")
  private String sipCallID = null;

  public GetJobResultsFailures time(Long time) {
    this.time = time;
    return this;
  }

  /**
   * Time at which the error occurred
   * @return time
   **/
  @JsonbProperty("time")
  @Schema(description = "Time at which the error occurred")
  public Long getTime() {
    return time;
  }

  public void setTime(Long time) {
    this.time = time;
  }

  public GetJobResultsFailures error(String error) {
    this.error = error;
    return this;
  }

  /**
   * A brief description of the error that occurred
   * @return error
   **/
  @JsonbProperty("error")
  @Schema(description = "A brief description of the error that occurred")
  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public GetJobResultsFailures testCaseID(String testCaseID) {
    this.testCaseID = testCaseID;
    return this;
  }

  /**
   * Id of the test case in which the job failed
   * @return testCaseID
   **/
  @JsonbProperty("testCaseID")
  @Schema(description = "Id of the test case in which the job failed")
  public String getTestCaseID() {
    return testCaseID;
  }

  public void setTestCaseID(String testCaseID) {
    this.testCaseID = testCaseID;
  }

  public GetJobResultsFailures testCaseName(String testCaseName) {
    this.testCaseName = testCaseName;
    return this;
  }

  /**
   * Name of the test case in which the job failed
   * @return testCaseName
   **/
  @JsonbProperty("testCaseName")
  @Schema(description = "Name of the test case in which the job failed")
  public String getTestCaseName() {
    return testCaseName;
  }

  public void setTestCaseName(String testCaseName) {
    this.testCaseName = testCaseName;
  }

  public GetJobResultsFailures turnNumber(Integer turnNumber) {
    this.turnNumber = turnNumber;
    return this;
  }

  /**
   * The turn number in the test case in which the job failed
   * @return turnNumber
   **/
  @JsonbProperty("turnNumber")
  @Schema(description = "The turn number in the test case in which the job failed")
  public Integer getTurnNumber() {
    return turnNumber;
  }

  public void setTurnNumber(Integer turnNumber) {
    this.turnNumber = turnNumber;
  }

  public GetJobResultsFailures iteration(Integer iteration) {
    this.iteration = iteration;
    return this;
  }

  /**
   * The iteration number in which the job failed
   * @return iteration
   **/
  @JsonbProperty("iteration")
  @Schema(description = "The iteration number in which the job failed")
  public Integer getIteration() {
    return iteration;
  }

  public void setIteration(Integer iteration) {
    this.iteration = iteration;
  }

  public GetJobResultsFailures sipCallID(String sipCallID) {
    this.sipCallID = sipCallID;
    return this;
  }

  /**
   * The GUID of the call to map back to the gateway it called
   * @return sipCallID
   **/
  @JsonbProperty("sipCallID")
  @Schema(description = "The GUID of the call to map back to the Caller Voice Gateway")
  public String getSipCallID() {
    return sipCallID;
  }

  public void setSipCallID(String sipCallID) {
    this.sipCallID = sipCallID;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetJobResultsFailures getJobResultsFailures = (GetJobResultsFailures) o;
    return Objects.equals(this.time, getJobResultsFailures.time) &&
        Objects.equals(this.error, getJobResultsFailures.error) &&
        Objects.equals(this.testCaseID, getJobResultsFailures.testCaseID) &&
        Objects.equals(this.testCaseName, getJobResultsFailures.testCaseName) &&
        Objects.equals(this.turnNumber, getJobResultsFailures.turnNumber) &&
        Objects.equals(this.iteration, getJobResultsFailures.iteration) &&
        Objects.equals(this.sipCallID, getJobResultsFailures.sipCallID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time, error, testCaseID, testCaseName, turnNumber, iteration, sipCallID);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetJobResultsFailures {\n");
    
    sb.append("    time: ").append(toIndentedString(time)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    testCaseID: ").append(toIndentedString(testCaseID)).append("\n");
    sb.append("    testCaseName: ").append(toIndentedString(testCaseName)).append("\n");
    sb.append("    turnNumber: ").append(toIndentedString(turnNumber)).append("\n");
    sb.append("    iteration: ").append(toIndentedString(iteration)).append("\n");
    sb.append("    sipCallID: ").append(toIndentedString(sipCallID)).append("\n");
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

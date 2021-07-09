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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * GetJobResults
 */
@JsonbPropertyOrder(value = { "numberOfFailures","failures" })
public class GetJobResults   {
	@JsonbProperty("numberOfFailures")
  private Integer numberOfFailures = 0;

	@JsonbProperty("failures")
  private List<GetJobResultsFailures> failures = null;

  public GetJobResults numberOfFailures(Integer numberOfFailures) {
    this.numberOfFailures = numberOfFailures;
    return this;
  }

  /**
   * Number of failures that have occurred
   * @return numberOfFailures
   **/
  @JsonbProperty("numberOfFailures")
  @Schema(description = "Number of failures that have occured")
  public Integer getNumberOfFailures() {
    return numberOfFailures;
  }

  public void setNumberOfFailures(Integer numberOfFailures) {
    this.numberOfFailures = numberOfFailures;
  }

  public GetJobResults failures(List<GetJobResultsFailures> failures) {
    this.failures = failures;
    return this;
  }

  public GetJobResults addFailuresItem(GetJobResultsFailures failuresItem) {
    if (this.failures == null) {
      this.failures = new ArrayList<GetJobResultsFailures>();
    }
    this.failures.add(failuresItem);
    return this;
  }

  /**
   * Array with details of each failure that occurred
   * @return failures
   **/
  @JsonbProperty("failures")
  @Schema(description = "Array with details of each failure that occured")
  public List<GetJobResultsFailures> getFailures() {
    return failures;
  }

  public void setFailures(List<GetJobResultsFailures> failures) {
    this.failures = failures;
  }
  
  public void incrementFailures() {
	  this.numberOfFailures++;
  }
  
  public void resetResults() {
	    this.numberOfFailures = 0;
	    if(this.failures!=null) {
	    	this.failures.clear();
	    	this.failures=null;
	    }
	  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetJobResults getJobResults = (GetJobResults) o;
    return Objects.equals(this.numberOfFailures, getJobResults.numberOfFailures) &&
        Objects.equals(this.failures, getJobResults.failures);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numberOfFailures, failures);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetJobResults {\n");
    
    sb.append("    numberOfFailures: ").append(toIndentedString(numberOfFailures)).append("\n");
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

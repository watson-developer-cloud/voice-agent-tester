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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * CreateWorkerCases
 */
public class CreateWorkerCases {
	@JsonbProperty("caseId")
	private String caseId = null;

	public CreateWorkerCases caseId(String id) {
		this.caseId = id;
		return this;
	}

	/**
	 * GUID of test case to include when starting a job
	 * 
	 * @return caseId
	 **/
	@JsonbProperty("caseId")
	@Schema(description = "GUID of test case to validate when running jobs")
	@NotNull(message = "caseId must not be null")
	@Size(min = 1, message = "caseId must not be empty")
	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String id) {
		this.caseId = id;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CreateWorkerCases createWorkerCases = (CreateWorkerCases) o;
		return Objects.equals(this.caseId, createWorkerCases.caseId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(caseId);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class WorkerCases {\n");

		sb.append("    caseId: ").append(toIndentedString(caseId)).append("\n");
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

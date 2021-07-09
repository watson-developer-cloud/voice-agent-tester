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
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Model for creating a worker
 */
@Schema(description = "Model for creating a worker")
public class CreateWorker {
	@JsonbProperty("name")
	private String name = null;

	@JsonbProperty("description")
	private String description = null;

	@JsonbProperty("namespace")
	private String namespace = "default";

	@JsonbProperty("iterations")
	private Integer iterations = null;

	@JsonbProperty("failuresToIgnore")
	private Integer failuresToIgnore = null;
	
	@JsonbProperty("callDefinition")
	private CreateWorkerCallDefinition callDefinition=null;


	@JsonbProperty("cases")
	private List<CreateWorkerCases> cases = new ArrayList<CreateWorkerCases>();

	public CreateWorker name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Name that describes worker
	 * 
	 * @return name
	 **/
	@JsonbProperty("name")
	@Schema(required = true, description = "Name that identifies worker",minLength=1,maxLength=50)
	@Size(min=1,max = 50, message = "name must be between 1 and 50 characters")
	@NotNull(message="name must not be null")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CreateWorker description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Brief description of what the worker does
	 * 
	 * @return description
	 **/
	@JsonbProperty("description")
	@Schema(required = false, description = "A brief description of what the worker does",minLength=1,maxLength=150)
	@Size(min=1,max = 150, message = "description must be between 1 and 150 characters")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CreateWorker iterations(Integer iterations) {
		this.iterations = iterations;
		return this;
	}

	/**
	 * Namespace to identify owner of worker
	 * 
	 * @return namespace
	 **/
	@JsonbProperty("namespace")
	@Schema(required = false, description = "Identifies a group to which the worker is bound to. Do not include when modifying worker", defaultValue = "default",minLength=1,maxLength=50)
	@Size(min = 1, max = 50, message = "namespace must be between 1 and 50 characters")
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public CreateWorker namespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	/**
	 * Number of times that the worker will run each test case
	 * 
	 * @return iterations
	 **/
	@JsonbProperty("iterations")
	@Schema(required = true, description = "Number of times that the worker will run the cases. If 0 it will run indefinitely",minimum="0")
	@NotNull(message = "iterations must not be null")
	@Min(value = 0,message="iterations must be a positive number or zero")
	public Integer getIterations() {
		return iterations;
	}

	public void setIterations(Integer iterations) {
		this.iterations = iterations;
	}


	public CreateWorker failuresToIgnore(Integer failuresToIgnore) {
		this.failuresToIgnore = failuresToIgnore;
		return this;
	}

	/**
	 * Number of failures that it will allow before ending the test
	 * 
	 * @return failuresToIgnore
	 **/
	@JsonbProperty("failuresToIgnore")
	@Schema(required = false, description = "Number of failures allowed before ending the test",minimum="0")
	@Min(value = 0,message="failuresToIgnore must be a positive number or zero")
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
	@Schema(required = true, description = "Json object that defines the outbound call definition the jobs will run")
	@NotNull(message="callDefinition must be valid and non null")
	@Valid
	public CreateWorkerCallDefinition getCallDefinition() {
		return callDefinition;
	}

	public void setCallDefinition(CreateWorkerCallDefinition callDefinition) {
		this.callDefinition = callDefinition;
	}

	public CreateWorker callDefinition(CreateWorkerCallDefinition callDefinition) {
		this.callDefinition = callDefinition;
		return this;
	}

	
	public CreateWorker cases(List<CreateWorkerCases> cases) {
		this.cases = cases;
		return this;
	}

	public CreateWorker addCasesItem(CreateWorkerCases casesItem) {
		this.cases.add(casesItem);
		return this;
	}

	/**
	 * Array of test cases that the worker will test against when running jobs
	 * 
	 * @return cases
	 **/
	@JsonbProperty("cases")
	@Schema(required = false, description = "Array of test cases that the worker will validate when running jobs")
	@Valid
	public List<CreateWorkerCases> getCases() {
		return cases;
	}

	public void setCases(List<CreateWorkerCases> cases) {
		this.cases = cases;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CreateWorker createWorker = (CreateWorker) o;
		return Objects.equals(this.name, createWorker.name)
				&& Objects.equals(this.description, createWorker.description)
				&& Objects.equals(this.namespace, createWorker.namespace)
				&& Objects.equals(this.iterations, createWorker.iterations)
				&& Objects.equals(this.callDefinition, createWorker.callDefinition)
				&& Objects.equals(this.failuresToIgnore, createWorker.failuresToIgnore)
				&& Objects.equals(this.cases, createWorker.cases);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, namespace, iterations, callDefinition, failuresToIgnore, cases);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CreateWorker {\n");

		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
		sb.append("    iterations: ").append(toIndentedString(iterations)).append("\n");
		sb.append("    failuresToIgnore: ").append(toIndentedString(failuresToIgnore)).append("\n");
		sb.append("    callDefinition: ").append(toIndentedString(callDefinition)).append("\n");
		sb.append("    cases: ").append(toIndentedString(cases)).append("\n");
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

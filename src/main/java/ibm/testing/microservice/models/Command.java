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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Value to use when using type commands. Its a Json object of vgwcommands")
public class Command {
	@JsonbProperty("parameters")
	private Map<String, Object> parameters;
	@JsonbProperty("command")
	@NotNull(message="Command name must not be null")
	@Size(min=1,message="Command name must not be empty")
	private String command;

	@JsonbCreator
	public Command(@JsonbProperty(value = "command") String command) {
		super();
		this.parameters = new HashMap<String, Object>();
		this.command = command;
	}
	
	public Command() {
	}

	/**
	 * The name of the command to run. Must be valid command
	 * 
	 * @return command
	 **/
	@JsonbProperty("command")
	@Schema(required = true, description = "The name of the command to run. Must be valid command")
	@NotNull(message="Command name must not be null")
	@Size(min=1,message="Command name must not be empty")
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * A json object of the parameters for the command. If the command doesn't
	 * have parameters don't specify
	 * 
	 * @return parameters
	 **/
	@JsonbProperty("parameters")
	@Schema(description = "A json object of the parameters for the command. If the command doesn't have parameters don't specify")
	public Map<String, Object> getParameters() {
		if(parameters.isEmpty())
			return null;
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Command command = (Command) o;
		return Objects.equals(this.command, command.command) && Objects.equals(this.parameters, command.parameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(command, parameters);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Command {\n");

		sb.append("    command: ").append(toIndentedString(command)).append("\n");
		if(!parameters.isEmpty())
			sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
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

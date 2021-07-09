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
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;


/**
 * CreateWorkerTenantConfig
 */
public class CreateWorkerTenantConfig {

	@JsonbProperty("config")
	private Object config = null;

	@JsonbProperty("updateMethod")
	private String updateMethod = "merge";

	public CreateWorkerTenantConfig config(Object config) {
		this.config = config;
		return this;
	}
	
	
	/**
	 * Json object that defines tenant configurations 
	 * 
	 * @return config
	 **/
	@JsonbProperty("config")
	@Schema(description = "Json object that defines tenant configurations. Additional info for model: https://www.ibm.com/support/knowledgecenter/en/SS4U29/json_config_props.html#json-configurationsample", externalDocs = @ExternalDocumentation(description = "Example of tenant config object", url = "https://www.ibm.com/support/knowledgecenter/en/SS4U29/json_config_props.html#json-configurationsample"))
	public Object getConfig() {
		return config;
	}

	public void setConfig(Object config) {
		this.config = config;
	}

	public CreateWorkerTenantConfig updateMethod(String updateMethod) {
		this.updateMethod = updateMethod;
		return this;
	}

	/**
	 * String that defines how the config object will be merged
	 * 
	 * @return updateMethod
	 **/
	@JsonbProperty("updateMethod")
	@Schema(description = "String that defines how the config object will be merged. Could be merge or replace",defaultValue="merge")
	public String getUpdateMethod() {
		return updateMethod;
	}

	public void setUpdateMethod(String updateMethod) {
		this.updateMethod = updateMethod;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CreateWorkerTenantConfig createWorkerTenantConfig = (CreateWorkerTenantConfig) o;
		return Objects.equals(this.config, createWorkerTenantConfig.config)
				&& Objects.equals(this.updateMethod, createWorkerTenantConfig.updateMethod);
	}

	@Override
	public int hashCode() {
		return Objects.hash(config, updateMethod);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TenantConfig {\n");

		sb.append("    config: ").append(toIndentedString(config)).append("\n");
		sb.append("    updateMethod: ").append(toIndentedString(updateMethod)).append("\n");
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

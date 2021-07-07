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

import java.util.List;
import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JsonbPropertyOrder({"tenantId","to","from","tenantConfig","route"})
public class CreateWorkerCallDefinition {
	
	@JsonbProperty("tenantId")
	private String tenantId="dummy";
	
	@JsonbProperty("to")
	private String to=null;
	
	@JsonbProperty("from")
	private String from=null;
	
	@JsonbProperty("tenantConfig")
	private CreateWorkerTenantConfig tenantConfig = null;
	
	@JsonbProperty("route")
	private List<String> route=null;
	
	public CreateWorkerCallDefinition to(String to) {
		this.to = to;
		return this;
	}
	
	
	/**
	 * ID of the tenant that will start the outbound call
	 * 
	 * @return tenantId
	 **/
	@JsonbProperty("tenantId")
	@Schema(required=false,description = "ID of the tenant that will start the outbound call",defaultValue="dummy",minLength=1)
	@Size(min=1, message = "tenantId in callDefinition must not be empty")
	@NotNull(message="tenantId in callDefinition must not be null")
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public CreateWorkerCallDefinition tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}
	
	
	
	/**
	 * SIP / Tel URI that corresponds to the agent that will be tested
	 * 
	 * @return to
	 **/
	@JsonbProperty("to")
	@Schema(required=true,description = "SIP / Tel URI that corresponds to the agent that will be tested",minLength=1)
	@NotNull(message = "to in callDefinition must not be null")
	@Size(min=1, message = "to in callDefinition must not be empty")
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public CreateWorkerCallDefinition from(String from) {
		this.from = from;
		return this;
	}

	/**
	 * SIP / Tel URI that corresponds to the agent that will drive the testing
	 * 
	 * @return from
	 **/
	@JsonbProperty("from")
	@Schema(required=true,description = "SIP / Tel URI that corresponds to the agent that will drive the testing",minLength=1)
	@NotNull(message = "from in callDefinition must not be null")
	@Size(min=1, message = "from in callDefinition must not be empty")
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public CreateWorkerCallDefinition tenantConfig(CreateWorkerTenantConfig tenantConfig) {
		this.tenantConfig = tenantConfig;
		return this;
	}

	/**
	 * Json object that defines tenant configurations
	 * 
	 * @return tenantConfig
	 **/
	@JsonbProperty("tenantConfig")
	@Schema(required=false,description = "Json object that defines tenant configurations.")
	public CreateWorkerTenantConfig getTenantConfig() {
		return tenantConfig;
	}

	public void setTenantConfig(CreateWorkerTenantConfig tenantConfig) {
		this.tenantConfig = tenantConfig;
	}
	
	public CreateWorkerCallDefinition route(List<String> route) {
		this.route = route;
		return this;
	}


	/**
	 * Brief description of what the worker does
	 * 
	 * @return description
	 **/
	@JsonbProperty("route")
	@Schema(required=false,description = "A comma separated list of SIP Route headers to be added to an outbound call")
	public List<String> getRoute() {
		return route;
	}

	public void setRoute(List<String> route) {
		this.route = route;
	}
	
	
	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CreateWorkerCallDefinition getWorkerCallDef = (CreateWorkerCallDefinition) o;
		return Objects.equals(this.to, getWorkerCallDef.to) && Objects.equals(this.from, getWorkerCallDef.from)
				&& Objects.equals(this.tenantConfig, getWorkerCallDef.tenantConfig)
				&& Objects.equals(this.route, getWorkerCallDef.route);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantId, to, from, tenantConfig, route);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CallDefinition {\n");

		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    to: ").append(toIndentedString(to)).append("\n");
		sb.append("    from: ").append(toIndentedString(from)).append("\n");
		sb.append("    tenantConfig: ").append(toIndentedString(tenantConfig)).append("\n");
		sb.append("    route: ").append(toIndentedString(route)).append("\n");
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

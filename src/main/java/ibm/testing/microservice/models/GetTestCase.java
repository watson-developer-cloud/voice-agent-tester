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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.validation.constraints.NotEmpty;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.google.gson.annotations.SerializedName;

/**
 * Model for what a test cases could look like when running a GET
 */
@Schema(description = "Model for what test cases could look like")
@JsonbPropertyOrder(value = { "id","rev","name","description","namespace","turns" })
public class GetTestCase {
	@JsonbProperty("id")
	@SerializedName(value = "_id")
	private String id = null;

	@JsonbProperty("rev")
	@SerializedName(value = "_rev")
	private String rev = null;

	@JsonbProperty("name")
	private String name = null;

	@JsonbProperty("description")
	private String description = null;

	@JsonbProperty("namespace")
	private String namespace = null;

	@JsonbProperty("turns")
	private List<CreateTestCaseTurns> turns = null;

	public GetTestCase id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * GUID that identifies the test case
	 * 
	 * @return id
	 **/
	@JsonbProperty("id")
	@SerializedName(value = "_id")
	@Schema(description = "This is the GUID that is associated with the test case")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GetTestCase rev(String rev) {
		this.rev = rev;
		return this;
	}

	/**
	 * Version of the test case
	 * 
	 * @return rev
	 **/
	@JsonbProperty("rev")
	@SerializedName(value = "_rev")
	@Schema(description = "Version of the test case")
	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public GetTestCase name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Name for identifying test case
	 * 
	 * @return name
	 **/
	@JsonbProperty("name")
	@Schema(description = "Name that identifies test case")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GetTestCase description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Description of test case
	 * 
	 * @return description
	 **/
	@JsonbProperty("description")
	@Schema(description = "A brief description of what the test case is about")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GetTestCase turns(List<CreateTestCaseTurns> turns) {
		this.turns = turns;
		return this;
	}

	public GetTestCase addTurnsItem(CreateTestCaseTurns turnsItem) {
		if (this.turns == null) {
			this.turns = new ArrayList<CreateTestCaseTurns>();
		}
		this.turns.add(turnsItem);
		return this;
	}

	/**
	 * Group to which the test case is bound to
	 * 
	 * @return namespace
	 **/
	@JsonbProperty("namespace")
	@Schema(description = "Identifies a group to which the test case is bound to")
	@NotEmpty
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public GetTestCase namespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	/**
	 * An ordered array of receive and send objects which symbolize the expected
	 * response of the agent under test and the response the agent tester will give
	 * accordingly.
	 * 
	 * @return turns
	 **/
	@JsonbProperty("turns")
	@Schema(description = "An ordered array of receive and send objects which symbolize the expected response of the agent under test and the response the agent tester will give accordingly.")
	@NotEmpty
	public List<CreateTestCaseTurns> getTurns() {
		return turns;
	}

	public void setTurns(List<CreateTestCaseTurns> turns) {
		this.turns = turns;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GetTestCase getTestCase = (GetTestCase) o;
		return Objects.equals(this.id, getTestCase.id) && Objects.equals(this.rev, getTestCase.rev)
				&& Objects.equals(this.name, getTestCase.name)
				&& Objects.equals(this.description, getTestCase.description)
				&& Objects.equals(this.namespace, getTestCase.namespace)
				&& Objects.equals(this.turns, getTestCase.turns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, rev, name, description, namespace, turns);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GetTestCase {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    rev: ").append(toIndentedString(rev)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
		sb.append("    turns: ").append(toIndentedString(turns)).append("\n");
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

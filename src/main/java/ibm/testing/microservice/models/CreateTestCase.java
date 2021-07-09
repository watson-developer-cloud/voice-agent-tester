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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Model for creating a test case
 */
@Schema(description = "Model for creating a test case")
public class CreateTestCase   {
  @JsonbProperty("name")
  private String name = null;

  @JsonbProperty("description")
  private String description = null;
  
  @JsonbProperty("namespace")
  private String namespace="default";

  @JsonbProperty("turns")
  private List<CreateTestCaseTurns> turns = new ArrayList<CreateTestCaseTurns>();

  public CreateTestCase name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name to identify your test case
   * @return name
   **/
  @JsonbProperty("name")
  @Schema(required = true, description = "Name that identifies test case",minLength=1,maxLength=50)
  @NotNull(message="name must not be null")
  @Size(min=1,max=50,message="name must be between 1 and 50 characters")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CreateTestCase description(String description) {
    this.description = description;
    return this;
  }

  /**
   * A brief description of what the test case is about
   * @return description
   **/
  @JsonbProperty("description")
  @Schema(required = false, description = "A brief description of what the test case is about",minLength=1,maxLength=150)
  @Size(min=1,max=150,message="description must be between 1 and 150 characters")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public CreateTestCase turns(List<CreateTestCaseTurns> turns) {
    this.turns = turns;
    return this;
  }

  public CreateTestCase addTurnsItem(CreateTestCaseTurns turnsItem) {
    this.turns.add(turnsItem);
    return this;
  }
  
  /**
   * Group to which the test case is bound to
   * @return namespace
   **/
  @JsonbProperty("namespace")
  @Schema(required = false, description = "Identifies a group to which the test case is bound to. Do not include when modifying test case",defaultValue="default",minLength=1,maxLength=50)
  @Size(min=1,max=50,message="namespace must be between 1 and 50 characters")
  public String getNamespace() {
    return namespace;
  }
  
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public CreateTestCase namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * An ordered array of receive and send objects that represent a conversation
   * @return turns
   **/
  @JsonbProperty("turns")
  @Schema(required = true, description = "An ordered array of receive and send objects that represent a conversation",minItems=1)
  @NotNull(message="turns must not be null")
  @Size(min=1,message="turns must have at least one object")
  @Valid
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
    CreateTestCase createTestCase = (CreateTestCase) o;
    return Objects.equals(this.name, createTestCase.name) &&
        Objects.equals(this.description, createTestCase.description) &&
        Objects.equals(this.namespace, createTestCase.namespace) &&
        Objects.equals(this.turns, createTestCase.turns);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, namespace, turns);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateTestCase {\n");
    
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
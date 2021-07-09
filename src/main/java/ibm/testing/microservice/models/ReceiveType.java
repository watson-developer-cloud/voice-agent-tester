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
import javax.json.bind.annotation.JsonbTypeDeserializer;
import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Model for making a valid receive object
 */
@Schema(description = "Model for making a valid receive object")
@ReceiveValueConstraint()
public class ReceiveType {
	/**
	 * Specifies the type of receive. Accepted are: string, substring, regex,
	 * context, andList, orList
	 */
	public enum TypeEnum {
		string("string"),

		substring("substring"),

		regex("regex"),

		context("context"),

		andList("andList"),

		orList("orList");

		private String value;

		TypeEnum(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static TypeEnum fromValue(String text) {
			for (TypeEnum b : TypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonbProperty("type")
	@JsonbTypeDeserializer(value = EnumDeserializer.class)
	private TypeEnum type = null;

	@JsonbProperty("value")
	private Object value = null;

	public ReceiveType type(TypeEnum type) {
		this.type = type;
		return this;
	}

	/**
	 * Specifies the type of receive. Accepted are: string, substring, regex,
	 * context, andList, orList
	 * 
	 * @return type
	 **/
	@JsonbProperty("type")
	@Schema(required = true, description = "Specifies the type of value expected")
	@NotNull(message = "Type of receive must be a valid string as specified.")
	public TypeEnum getType() {
		return type;
	}

	public void setType(TypeEnum type) {
		this.type = type;
	}

	public ReceiveType value(Object value) {
		this.value = value;
		return this;
	}

	/**
	 * Get value
	 * 
	 * @return value
	 **/
	@JsonbProperty("value")
	@Schema(required = true, description = "Content that depends on type. For string, regex, and substring must be a string; "
			+ "for andList and orList must be an array of receive objects; for context must be and object of key value pairs")
	@NotNull(message = "Value of receive must be a valid non null type")
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ReceiveType receiveType = (ReceiveType) o;
		return Objects.equals(this.type, receiveType.type) && Objects.equals(this.value, receiveType.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ReceiveType {\n");

		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

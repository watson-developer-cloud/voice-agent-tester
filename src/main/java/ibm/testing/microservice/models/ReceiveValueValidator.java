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
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReceiveValueValidator implements ConstraintValidator<ReceiveValueConstraint, ReceiveType> {

	@Override
	public void initialize(ReceiveValueConstraint value) {
	}

	@Override
	public boolean isValid(ReceiveType receive, ConstraintValidatorContext context) {
		if (receive.getType() == null || receive.getType().toString() == null)
			return false;
		String type = receive.getType().toString();
		context.disableDefaultConstraintViolation();
		if (type.equals("andList") || type.equals("orList")) {
			if (receive.getValue() instanceof List)
				try {
					Jsonb mapper=JsonbBuilder.create();
					String res=mapper.toJson(receive.getValue());
					List<ReceiveType> l=mapper.fromJson(res, new ArrayList<ReceiveType>() {}.getClass().getGenericSuperclass());
					boolean isValid = !l.isEmpty();
					for (ReceiveType r : l) {
						isValid = (isValid && this.isValid(r, context));
					}
					if(l.isEmpty()) {
						context.buildConstraintViolationWithTemplate("Receive: List "+type+" must not be empty.")
						.addConstraintViolation();
					}
					else if (!isValid) {
						context.buildConstraintViolationWithTemplate("Receive: Invalid content inside: " + type)
								.addConstraintViolation();
					}
					return isValid;
				} catch (Exception e) {
					context.buildConstraintViolationWithTemplate(
							"Receive: Invalid content in " + type + ". Something went wrong mapping items")
							.addConstraintViolation();
					e.printStackTrace();
					return false;
				}
			else {
				context.buildConstraintViolationWithTemplate("Receive: Invalid type of value for: " + type)
						.addConstraintViolation();
				return false;
			}
		} else if (type.equals("substring") || type.equals("regex") || type.equals("string")) {
			if (receive.getValue() instanceof String)
				return true;
			else {
				context.buildConstraintViolationWithTemplate("Receive: Invalid type of value for: " + type)
						.addConstraintViolation();
				return false;
			}
		} else if (type.equals("context")) {
			if (receive.getValue() instanceof Map) {
				try {
					Map<String, Object> map =(Map<String, Object>)receive.getValue();
					if (map.isEmpty())
						context.buildConstraintViolationWithTemplate("Receive: Context value must not be empty")
								.addConstraintViolation();
					return !map.isEmpty();
				} catch (Exception e) {
					e.printStackTrace();
					context.buildConstraintViolationWithTemplate("Receive: Invalid type of value for: " + type)
							.addConstraintViolation();
					return false;
				}
			} else {
				context.buildConstraintViolationWithTemplate("Receive: Invalid type of value for: " + type)
						.addConstraintViolation();
				return false;
			}
		} else {
			context.buildConstraintViolationWithTemplate("Receive: Invalid type:" + type).addConstraintViolation();
			return false;
		}
	}

}

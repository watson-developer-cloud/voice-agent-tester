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

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SendValueValidator implements ConstraintValidator<SendValueConstraint, SendType> {

	@Override
	public void initialize(SendValueConstraint value) {
	}

	@Override
	public boolean isValid(SendType send, ConstraintValidatorContext context) {
		if (send.getType() == null || send.getType().toString() == null)
			return false;
		String type = send.getType().toString();
		context.disableDefaultConstraintViolation();
		if (type.equals("string")) {
			if (send.getValue() instanceof String)
				return true;
			else {
				context.buildConstraintViolationWithTemplate("Send: Invalid type of value for: " + type)
						.addConstraintViolation();
				return false;
			}
		} else if (type.equals("commands")) {
			if (send.getValue() instanceof List)
				try {
					Jsonb mapper=JsonbBuilder.create();
					String res=mapper.toJson(send.getValue());
					List<Command> l=mapper.fromJson(res, new ArrayList<Command>() {}.getClass().getGenericSuperclass());
					if (l.isEmpty()) {
						context.buildConstraintViolationWithTemplate("Send: Command list must not be empty")
								.addConstraintViolation();
					}
					return !l.isEmpty();
				} catch (Exception e) {
					context.buildConstraintViolationWithTemplate(
							"Send: Invalid content in command list. Something went wrong mapping items")
							.addConstraintViolation();
					return false;
				}
			else {
				context.buildConstraintViolationWithTemplate("Send: Invalid type of value for: " + type)
						.addConstraintViolation();
				return false;
			}
		} else {
			context.buildConstraintViolationWithTemplate("Send: Invalid type: " + type).addConstraintViolation();
			return false;
		}
	}

}

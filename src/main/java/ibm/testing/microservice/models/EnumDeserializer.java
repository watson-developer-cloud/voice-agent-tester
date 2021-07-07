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

import java.lang.reflect.Type;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

public class EnumDeserializer implements JsonbDeserializer<Enum> {

	@Override
	public Enum deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
		try {
			return Enum.valueOf((Class<Enum>) rtType, parser.getString());
		}catch(Exception e) {
			return null;
		}
	}

}

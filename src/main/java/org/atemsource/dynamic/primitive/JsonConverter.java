package org.atemsource.dynamic.primitive;

import org.atemsource.atem.api.infrastructure.exception.ConversionException;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.api.TransformationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class JsonConverter implements JavaConverter<String, ObjectNode> {
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public ObjectNode convertAB(String a, TransformationContext ctx) {
		if (a == null) {
			return null;
		} else {
			try {
				return (ObjectNode) objectMapper.readTree(a);
			} catch (Exception e) {
				throw new TechnicalException("cannot convert string to node",
						e);
			}
		}
	}

	@Override
	public String convertBA(ObjectNode b, TransformationContext ctx) {
		if (b == null) {
			return null;
		} else {
			return b.toString();
		}

	}

}

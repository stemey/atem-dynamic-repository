package org.atemsource.dynamic.primitive;

import java.util.HashMap;
import java.util.Map;

import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class PrimitiveAttributeCreator extends AttributeCreator {
	private Map<String, Class> typeMapping;

	public PrimitiveAttributeCreator() {
		super();
		typeMapping= new HashMap<String, Class>();
		typeMapping.put("string", String.class);
		typeMapping.put("number", Number.class);
		typeMapping.put("boolean", Boolean.class);
	}

	@Override
	public boolean handles(ObjectNode node) {
		return true;
	}

	@Override
	public void addAttribute(ObjectNode node, EntityTypeBuilder entityTypeBuilder) {
		entityTypeBuilder.addSingleAttribute(node.get("code").getTextValue(),
				getType(node.get("type").getTextValue()));
	}
	private Class getType(String typeCode) {
		return typeMapping.get(typeCode);
	}
}

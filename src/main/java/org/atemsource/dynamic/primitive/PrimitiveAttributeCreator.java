package org.atemsource.dynamic.primitive;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.impl.dynamic.DynamicEntityTypeBuilder;
import org.atemsource.atem.utility.transform.api.AbstractTypeTransformationBuilder;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.impl.builder.SingleAttributeTransformationBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class PrimitiveAttributeCreator<T> extends AttributeCreator<T> {
	private Map<String, Class> typeMapping;

	public PrimitiveAttributeCreator() {
		super();
		typeMapping = new HashMap<String, Class>();
		typeMapping.put("string", String.class);
		typeMapping.put("double", Double.class);
		typeMapping.put("long", Long.class);
		typeMapping.put("boolean", Boolean.class);
	}

	@Override
	public boolean handles(ObjectNode node) {
		return node.has("type");
	}

	@Override
	public void addAttribute(ObjectNode node, String newCode,
			DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		String code = node.get("code").getTextValue();

		builder.getSourceTypeBuilder().addSingleAttribute(newCode,
				getType(node));

		SingleAttributeTransformationBuilder<T, ObjectNode> transformationBuilder = builder
				.transform().from(newCode).to(code);
		JavaConverter<?, ?> converter = factory.getConverter(code);
		if (converter != null) {
			transformationBuilder.convert(converter);
		}
	}

	protected Class getType(ObjectNode type) {
		String typeCode = type.get("type").getTextValue();
		if (typeCode.equals("number")) {
			if (type.has("numberFormat")) {
				if (type.get("numberFormat").getTextValue().contains(".")) {
					typeCode = "double";
				} else {
					typeCode = "long";
				}
			} else {
				typeCode = "double";
			}
		}
		return typeMapping.get(typeCode);
	}
}

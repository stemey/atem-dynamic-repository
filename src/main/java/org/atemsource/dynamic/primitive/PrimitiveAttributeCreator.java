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
		typeMapping.put("number", Number.class);
		typeMapping.put("boolean", Boolean.class);
	}

	@Override
	public boolean handles(ObjectNode node) {
		return true;
	}

	@Override
	public void addAttribute(ObjectNode node, String newCode,
			DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		String code = node.get("code").getTextValue();

		builder.getSourceTypeBuilder().addSingleAttribute(newCode,
				getType(node.get("type").getTextValue()));

		SingleAttributeTransformationBuilder<T, ObjectNode> transformationBuilder = builder
				.transform().from(newCode).to(code);
		JavaConverter<?, ?> converter = factory.getConverter(code);
		if (converter != null) {
			transformationBuilder.convert(converter);
		}
	}

	private Class getType(String typeCode) {
		return typeMapping.get(typeCode);
	}
}

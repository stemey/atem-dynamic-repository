package org.atemsource.dynamic.primitive;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.jcr.Node;

import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.impl.dynamic.DynamicEntityTypeBuilder;
import org.atemsource.atem.impl.json.schema.Format;
import org.atemsource.atem.utility.transform.api.AbstractTypeTransformationBuilder;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.impl.EntityTypeTransformation;
import org.atemsource.atem.utility.transform.impl.builder.SingleAttributeTransformationBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class BinaryAttributeCreator<T> extends AttributeCreator<T> {
	private Map<String, Class> typeMapping;

	@Override
	public boolean handles(ObjectNode node) {
		return node.has("type")
				&& node.get("type").getTextValue().equals("binary");
	}

	private EntityTypeTransformation<T, ObjectNode> binaryTransformation;

	@Override
	public void addAttribute(ObjectNode node, String newCode,
			DynamicTypeTransformationBuilder<T, ObjectNode> builder) {

		if (binaryTransformation == null) {
			DynamicTypeTransformationBuilder<T, ObjectNode> binaryBuilder = factory
					.createBuilder("embedded_binary");
			EntityTypeBuilder sourceTypeBuilder = binaryBuilder
					.getSourceTypeBuilder();
			sourceTypeBuilder.addSingleAttribute("url", String.class);
			sourceTypeBuilder.addSingleAttribute("type", String.class);
			sourceTypeBuilder.addSingleAttribute("name", String.class);
			sourceTypeBuilder.createEntityType();
			
			binaryBuilder.transform().from("url");
			binaryBuilder.transform().from("type");
			binaryBuilder.transform().from("name");
			binaryTransformation=binaryBuilder.buildTypeTransformation();
		}

		String code = node.get("code").getTextValue();

		builder.getSourceTypeBuilder()
				.addSingleAssociationAttribute(newCode,binaryTransformation.getEntityTypeA());

		SingleAttributeTransformationBuilder<T, ObjectNode> transformationBuilder = builder
				.transform().from(newCode).to(code)
				.convert(binaryTransformation);

	}
}

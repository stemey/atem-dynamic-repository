package org.atemsource.dynamic.primitive;

import org.atemsource.atem.api.attribute.annotation.Cardinality;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.api.type.SingleAssociationAttributeBuilder;
import org.atemsource.atem.utility.transform.api.AbstractTypeTransformationBuilder;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import transformation.RefConverter;

public class TemplateReferenceAttributeCreator<T> extends AttributeCreator<T> {

	@Override
	public boolean handles(ObjectNode node) {
		JsonNode typeNode = node.get("type");
		JsonNode templateNode = node.get("template");
		JsonNode editor = node.get("editor");
		return (editor != null && editor.getTextValue().equals("template-ref"))
				|| (typeNode != null && templateNode != null && typeNode
						.getTextValue().equals("ref"));
	}

	@Override
	public void addAttribute(ObjectNode node, String newCode,
			DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		String code = node.get("code").getTextValue();
		String schemaUrl = node.get("template").get("$ref").getTextValue();
		EntityType<T> targetType = factory.getEntityTypeBySchemaUri(schemaUrl);
		SingleAssociationAttributeBuilder<T> attributeBuilder = builder
				.getSourceTypeBuilder().addSingleAssociationAttribute(newCode);

		attributeBuilder.cardinality(Cardinality.ONE)//
				.type(targetType)//
				.composition(true)//
				.create();

		builder.transform().from(code).to(newCode)
				.convert(factory.getTransformation(schemaUrl));

	}

}

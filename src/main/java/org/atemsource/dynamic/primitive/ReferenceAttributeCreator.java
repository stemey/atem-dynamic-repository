package org.atemsource.dynamic.primitive;

import org.atemsource.atem.api.attribute.annotation.Cardinality;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.api.type.SingleAssociationAttributeBuilder;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class ReferenceAttributeCreator<T> extends AttributeCreator<T>{

	@Override
	public boolean handles(ObjectNode node) {
		return node.get("type").getTextValue().equals("ref");
	}

	@Override
	public void addAttribute(ObjectNode node,
			String newCode, DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		String schemaUrl = node.get("schemaUrl").getTextValue();
		EntityType<T> targetType = factory
				.getEntityTypeBySchemaUri(schemaUrl);
		SingleAssociationAttributeBuilder<T> attributeBuilder = builder.getSourceTypeBuilder().addSingleAssociationAttribute(newCode);
		attributeBuilder.cardinality(Cardinality.ONE)//
				.type(targetType)//
				.composition(false)//
				.create();
	}

}

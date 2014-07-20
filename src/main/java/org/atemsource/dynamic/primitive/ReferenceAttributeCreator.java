package org.atemsource.dynamic.primitive;

import org.atemsource.atem.api.attribute.annotation.Cardinality;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.api.type.SingleAttributeBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class ReferenceAttributeCreator extends AttributeCreator {

	@Override
	public boolean handles(ObjectNode node) {
		return node.get("type").getTextValue().equals("ref");
	}

	@Override
	public void addAttribute(ObjectNode node,
			EntityTypeBuilder entityTypeBuilder) {
		String schemaUrl = node.get("schemaUrl").getTextValue();
		EntityType<Object> targetType = factory
				.getEntityTypeBySchemaUri(schemaUrl);
		SingleAttributeBuilder<Object> builder = entityTypeBuilder
				.addSingleAssociationAttribute(node.get("code").getTextValue());
		builder.cardinality(Cardinality.ONE)//
				.type(targetType)//
				.composition(false)//
				.create();
	}

}

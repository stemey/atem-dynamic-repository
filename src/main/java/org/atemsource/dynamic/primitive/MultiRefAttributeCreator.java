package org.atemsource.dynamic.primitive;

import java.util.Collection;
import java.util.LinkedList;

import org.atemsource.atem.api.attribute.annotation.Cardinality;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.SingleAssociationAttributeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import transformation.RefConverter;

public class MultiRefAttributeCreator<T> extends AttributeCreator<T> {

	@Override
	public boolean handles(ObjectNode node) {
		JsonNode typeNode = node.get("type");
		return typeNode != null
				&& typeNode.getTextValue().equals("multi-ref");
	}

	@Override
	public void addAttribute(ObjectNode node, String newCode,
			DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		String code = node.get("code").getTextValue();
		ArrayNode schemas = (ArrayNode) node.get("schemas");
		Collection<EntityType<?>> targetTypes = new LinkedList<EntityType<?>>();
		for (int i = 0; i < schemas.size(); i++) {
			String url = schemas.get(i).getTextValue();
			EntityType<T> targetType = factory.getEntityTypeBySchemaUri(url);
			targetTypes.add(targetType);
		}
		SingleAssociationAttributeBuilder<T> attributeBuilder = builder
				.getSourceTypeBuilder().addSingleAssociationAttribute(newCode);
		
		builder.transform().from(code).to(newCode);
		attributeBuilder.cardinality(Cardinality.ONE)//
				.type((Type<T>) targetTypes.iterator().next())//
				.composition(false)//
				.create();

	}

}

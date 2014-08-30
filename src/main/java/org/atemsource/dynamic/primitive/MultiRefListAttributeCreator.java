package org.atemsource.dynamic.primitive;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.atemsource.atem.api.attribute.annotation.Cardinality;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.MultiAssociationAttributeBuilder;
import org.atemsource.atem.api.type.SingleAssociationAttributeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import transformation.RefConverter;

public class MultiRefListAttributeCreator<T> extends AttributeCreator<T> {

	@Override
	public boolean handles(ObjectNode node) {
		boolean list = node.has("element") && node.has("type") && node.get("type").getTextValue().equals("array");
		return list && node.get("element").get("type").getTextValue().equals("multi-ref");
	}

	@Override
	public void addAttribute(ObjectNode node, String newCode,
			DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		String code = node.get("code").getTextValue();
		ObjectNode element = (ObjectNode) node.get("element");
		
		ArrayNode schemas = (ArrayNode) element.get("schemas");
		Collection<EntityType<?>> targetTypes = new LinkedList<EntityType<?>>();
		for (int i = 0; i < schemas.size(); i++) {
			String url = schemas.get(i).getTextValue();
			EntityType<T> targetType = factory.getEntityTypeBySchemaUri(url);
			targetTypes.add(targetType);
		}
		
		MultiAssociationAttributeBuilder<?,T> attributeBuilder = builder
				.getSourceTypeBuilder().addMultiAssociationAttribute(newCode);
		Type<T> targetType=null;
		Iterator<EntityType<?>> iterator = targetTypes.iterator();
		if (iterator.hasNext()) {
			targetType=(Type<T>) iterator.next();
		}
		attributeBuilder.cardinality(Cardinality.ZERO_TO_MANY)//
				.type(targetType)//
				.composition(false)//
				.create();
		builder.transformCollection().from(code).to(newCode);

	}

}

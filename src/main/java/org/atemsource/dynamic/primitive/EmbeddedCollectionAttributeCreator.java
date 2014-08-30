package org.atemsource.dynamic.primitive;

import org.atemsource.atem.api.attribute.Attribute;
import org.atemsource.atem.api.attribute.CollectionSortType;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.api.type.MultiAssociationAttributeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class EmbeddedCollectionAttributeCreator<T> extends AttributeCreator<T> {

	@Override
	public boolean handles(ObjectNode node) {
		return node.get("group") != null
				&& node.get("type").getTextValue().contains("array");
	}

	@Override
	public void addAttribute(ObjectNode node,
			String newCode, DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		ObjectNode group = (ObjectNode) node.get("group");

		String code = node.get("code").getTextValue();
		String embeddedTypeCode = builder.getSourceTypeBuilder().getReference().getCode()+ "::" + code;


		DynamicTypeTransformationBuilder<T, ObjectNode> subBuilder = factory.createBuilder(embeddedTypeCode);

		MultiAssociationAttributeBuilder<Object, Object> attributeBuilder = builder.getSourceTypeBuilder().addMultiAssociationAttribute(newCode);
		
		attributeBuilder.composition(true);
		attributeBuilder.type((Type<Object>) subBuilder.getSourceTypeBuilder().getReference());
		Attribute<Object, Object> attribute = attributeBuilder.create();

		factory.addGroup(group, subBuilder);
		subBuilder.buildTypeTransformation();

		
		builder.transformCollection().from(code).to(newCode).convert(subBuilder.getReference()).sort(CollectionSortType.ORDERABLE);


	}

}

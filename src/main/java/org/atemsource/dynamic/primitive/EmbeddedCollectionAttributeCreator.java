package org.atemsource.dynamic.primitive;

import org.atemsource.atem.api.attribute.CollectionSortType;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class EmbeddedCollectionAttributeCreator extends AttributeCreator {

	
	@Override
	public boolean handles(ObjectNode node) {
		return node.get("group") != null
				&& node.get("type").getTextValue().contains("array");
	}

	@Override
	public void addAttribute(ObjectNode node,
			EntityTypeBuilder entityTypeBuilder) {
		ObjectNode group = (ObjectNode) node.get("group");
		String embeddedTypeCode = entityTypeBuilder.getReference().getCode()
				+ "::" + group.get("code").getTextValue();
		EntityType<?> targetType = factory.addGroup(group,
				embeddedTypeCode);

		entityTypeBuilder.addMultiAssociationAttribute(node.get("code")
				.getTextValue(), targetType, CollectionSortType.ORDERABLE);
	}

	

}

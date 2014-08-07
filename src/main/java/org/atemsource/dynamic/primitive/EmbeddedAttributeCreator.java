package org.atemsource.dynamic.primitive;

import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class EmbeddedAttributeCreator<T> extends AttributeCreator<T> {

	@Override
	public boolean handles(ObjectNode node) {
		return node.get("group") != null
				&& node.get("type").getTextValue().contains("object");
	}

	@Override
	public void addAttribute(ObjectNode node,
			String newCode, DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		ObjectNode group = (ObjectNode) node.get("group");

		String code = node.get("code").getTextValue();
		String embeddedTypeCode = builder.getSourceTypeBuilder().getReference().getCode()+ "::" + code;

		DynamicTypeTransformationBuilder<T, ObjectNode> subBuilder = factory.createBuilder(embeddedTypeCode);

		builder.getSourceTypeBuilder().addSingleAssociationAttribute(newCode,
				subBuilder.getSourceTypeBuilder().getReference());

		builder.transform().from(code).to(newCode).convert(subBuilder.getReference());
		factory.addGroup(group, subBuilder);
	}

}

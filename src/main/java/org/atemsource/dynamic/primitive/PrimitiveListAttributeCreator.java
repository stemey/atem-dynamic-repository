package org.atemsource.dynamic.primitive;

import org.atemsource.atem.api.attribute.CollectionSortType;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.impl.builder.CollectionAttributeTransformationBuilder;
import org.codehaus.jackson.node.ObjectNode;

public class PrimitiveListAttributeCreator<T> extends PrimitiveAttributeCreator<T> {

	@Override
	public boolean handles(ObjectNode node) {
		return node.has("type") && node.get("type").getTextValue().equals("array")
				&& node.has("element");
	}

	@Override
	public void addAttribute(ObjectNode node, String newCode,
			DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		ObjectNode element = (ObjectNode) node.get("element");

		String code = node.get("code").getTextValue();

		Class clazz = getType(element);
		
		
		builder.getSourceTypeBuilder().addMultiAssociationAttribute(newCode,
				factory.getType(clazz),CollectionSortType.ORDERABLE);

		CollectionAttributeTransformationBuilder<T, ObjectNode> transformationBuilder = builder
				.transformCollection().from(newCode).to(code);
		JavaConverter<?, ?> converter = factory.getConverter(code);
		if (converter != null) {
			transformationBuilder.convert(converter);
		}		
		
		
	}

}

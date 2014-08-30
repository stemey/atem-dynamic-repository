package org.atemsource.dynamic;

import javax.jcr.Node;

import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.impl.EntityTypeTransformation;
import org.atemsource.dynamic.primitive.GformEntityTypeFactory;
import org.codehaus.jackson.node.ObjectNode;

public class Cms4AppsGformEntityTypeFactory extends
		GformEntityTypeFactory<Node> {

	@Override
	public EntityTypeTransformation<Node, ObjectNode> createType(
			ObjectNode schema) {
		return super.createTypeByTypeCode((ObjectNode) schema.get("group"),
				schema.get("code").getTextValue());
	}

	@Override
	public EntityTypeTransformation<Node, ObjectNode> updateType(String id,
			ObjectNode schema) {
		return super.updateType(id, (ObjectNode) schema.get("group"));
	}

	public JavaConverter<?, ?> getConverter(String code) {
		 if (code.equals("template")) {
			return (JavaConverter<?, ?>) new JcrTypeCodeConverter();
		} else {
			return null;
		}
	}

}

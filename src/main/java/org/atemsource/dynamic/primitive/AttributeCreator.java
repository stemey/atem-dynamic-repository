package org.atemsource.dynamic.primitive;

import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.codehaus.jackson.node.ObjectNode;

public abstract class AttributeCreator<T> {

	protected GformEntityTypeFactory<T> factory;

	public abstract boolean handles(ObjectNode node);

	public void setFactory(GformEntityTypeFactory<T> factory) {
		this.factory = factory;
	}

	public abstract void addAttribute(ObjectNode node,
			String newCode, DynamicTypeTransformationBuilder<T,ObjectNode> builder);

}
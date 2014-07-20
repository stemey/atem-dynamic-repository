package org.atemsource.dynamic.primitive;

import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.codehaus.jackson.node.ObjectNode;

public abstract class  AttributeCreator {

	protected GformEntityTypeFactory factory;

	public abstract boolean handles(ObjectNode node);
	
	public void setFactory(GformEntityTypeFactory factory) {
		this.factory=factory;
	}

	public abstract void addAttribute(ObjectNode node,
			EntityTypeBuilder entityTypeBuilder);

}
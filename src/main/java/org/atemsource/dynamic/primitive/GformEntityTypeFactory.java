package org.atemsource.dynamic.primitive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.impl.dynamic.DynamicEntityTypeRepository;
import org.atemsource.atem.spi.DynamicEntityTypeSubrepository;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class GformEntityTypeFactory<T> {

	private List<AttributeCreator> attributeCreators;
	private DynamicEntityTypeSubrepository<T> dynamicEntityTypeRepository;
	private Map<String, EntityTypeBuilder> builders = new HashMap<String, EntityTypeBuilder>();

	public void setAttributeCreators(List<AttributeCreator> attributeCreators) {
		this.attributeCreators = attributeCreators;
		for (AttributeCreator creator:attributeCreators) {
			creator.setFactory(this);
		}
	}

	public EntityTypeBuilder getBuilder(String typeCode) {
		EntityTypeBuilder entityTypeBuilder = builders.get(typeCode);
		if (entityTypeBuilder == null) {
			entityTypeBuilder = dynamicEntityTypeRepository
					.createBuilder(typeCode);
			builders.put(typeCode, entityTypeBuilder);
		}
		return entityTypeBuilder;
	}

	public EntityType<T> addGroup(ObjectNode group, String typeCode) {
		EntityTypeBuilder builder = getBuilder(typeCode);
		addAttributesToGroup(builder, group);
		return (EntityType<T>) builder.createEntityType();
	}

	private void addAttributesToGroup(EntityTypeBuilder builder,
			ObjectNode group) {
		if (group.get("attributes") != null) {
			addAttributes(builder, (ArrayNode) group.get("attributes"));
		} else if (group.get("groups") != null) {
			ArrayNode groups = (ArrayNode) group.get("groups");
			for (int i = 0; i < groups.size(); i++) {
				addAttributesToGroup(builder, (ObjectNode) groups.get(i));
			}
		}
	}

	private void addAttributes(EntityTypeBuilder builder, ArrayNode attributes) {
		for (int i = 0; i < attributes.size(); i++) {
			addAttribute(builder, (ObjectNode) attributes.get(i));
		}
	}

	private void addAttribute(EntityTypeBuilder builder, ObjectNode attribute) {
		for (AttributeCreator attributeCreator : attributeCreators) {
			if (attributeCreator.handles(attribute)) {
				attributeCreator.addAttribute(attribute, builder);
				break;
			}
		}
	}

	private String schemaUriPrefix;

	public void setDynamicEntityTypeRepository(
			DynamicEntityTypeSubrepository<T> dynamicEntityTypeRepository) {
		this.dynamicEntityTypeRepository = dynamicEntityTypeRepository;
	}

	public void setSchemaUriPrefix(String schemaUriPrefix) {
		this.schemaUriPrefix = schemaUriPrefix;
	}

	public EntityType<?> getEntityTypeBySchemaUri(String schemaUrl) {
		if (schemaUrl.startsWith(schemaUriPrefix)) {
			String typeCode = getTypeCode(schemaUrl);
			EntityTypeBuilder builder = getBuilder(typeCode);
			// TODO maybe it is a type from a different repository
			return builder.getReference();
		} else {
			throw new IllegalArgumentException("invalid schemaUrl " + schemaUrl);
		}
	}

	private String getTypeCode(String schemaUrl) {
		return schemaUrl.substring(schemaUriPrefix.length());
	}

	public EntityType<T> createType(ObjectNode schema, String schemaUrl) {
		String typeCode = getTypeCode(schemaUrl);
		return addGroup(schema, typeCode);

	}

}

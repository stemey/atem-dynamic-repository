package org.atemsource.dynamic.primitive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.jcr.Node;

import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.infrastructure.exception.BusinessException;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.spi.DynamicEntityTypeSubrepository;
import org.atemsource.atem.utility.binding.AttributeConverter;
import org.atemsource.atem.utility.transform.api.DynamicTypeTransformationBuilder;
import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.api.JavaUniConverter;
import org.atemsource.atem.utility.transform.api.TransformationBuilderFactory;
import org.atemsource.atem.utility.transform.api.TypeTransformationBuilder;
import org.atemsource.atem.utility.transform.api.meta.DerivedType;
import org.atemsource.atem.utility.transform.impl.EntityTypeTransformation;
import org.atemsource.dynamic.JcrTypeCodeConverter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class GformEntityTypeFactory<T> {

	private List<AttributeCreator> attributeCreators;
	private DynamicEntityTypeSubrepository<T> dynamicEntityTypeRepository;
	private DynamicEntityTypeSubrepository<ObjectNode> jsonEntityTypeRepository;
	private Map<String, EntityTypeBuilder> builders = new HashMap<String, EntityTypeBuilder>();
	private Map<String, DynamicTypeTransformationBuilder> transformationBuilders = new HashMap<String, DynamicTypeTransformationBuilder>();
	private TransformationBuilderFactory transformationBuilderFactory;
	private JavaConverter<String, String> typeCodeConverter;
	private String schemaUriPrefix;
	private EntityType<?> sourceBaseType;
	private EntityType<?> targetBaseType;
	private String baseTypeCode=JcrTypeCodeConverter.JSON_PREFIX+"base";
	private EntityTypeTransformation<?, Object> baseTransformation;
	private EntityTypeRepository entityTypeRepository;
	
	public void setEntityTypeRepository(EntityTypeRepository entityTypeRepository) {
		this.entityTypeRepository = entityTypeRepository;
	}

	@PostConstruct
	public void initialize(){
		EntityTypeBuilder builder = dynamicEntityTypeRepository.createBuilder(typeCodeConverter.convertBA(baseTypeCode, null));
		//builder.addSingleAttribute("identifier", String.class);
		//builder.addSingleAttribute("template", String.class);
		//builder.addSingleAttribute("path", String.class);
		this.sourceBaseType=builder.createEntityType();
		
		EntityTypeBuilder typeBuilder = jsonEntityTypeRepository.createBuilder(baseTypeCode);
		TypeTransformationBuilder<?, Object> transformationBuilder = transformationBuilderFactory.create(sourceBaseType,typeBuilder );
		transformationBuilder.transform().from("identifier").to("identifier");
		transformationBuilder.transform().from("path").to("url");
		transformationBuilder.transform().from("template").to("template").convert(new JcrTypeCodeConverter());
		this.baseTransformation=transformationBuilder.buildTypeTransformation();
		targetBaseType=typeBuilder.getReference();
		
		
			
	}

	public void setAttributeCreators(List<AttributeCreator> attributeCreators) {
		this.attributeCreators = attributeCreators;
		for (AttributeCreator creator : attributeCreators) {
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


	public void setDynamicEntityTypeRepository(
			DynamicEntityTypeSubrepository<T> dynamicEntityTypeRepository) {
		this.dynamicEntityTypeRepository = dynamicEntityTypeRepository;
	}

	public void setSchemaUriPrefix(String schemaUriPrefix) {
		this.schemaUriPrefix = schemaUriPrefix;
	}

	public <T> EntityType<T> getEntityTypeBySchemaUri(String schemaUrl) {
		if (schemaUrl.startsWith(schemaUriPrefix)) {
			String typeCode = getTypeCode(schemaUrl);
			return getEntityType(typeCode);
		} else {
			throw new IllegalArgumentException("invalid schemaUrl " + schemaUrl);
		}
	}

	public <T> EntityType<T> getEntityType(String typeCode) {
		EntityTypeBuilder builder = getBuilder(typeCodeConverter.convertBA(
				typeCode, null));
		// TODO maybe it is a type from a different repository
		return (EntityType<T>) builder.getReference();
	}

	private String getTypeCode(String schemaUrl) {
		return schemaUrl.substring(schemaUriPrefix.length());
	}

	protected EntityTypeTransformation<T, ObjectNode> createTypeByTypeCode(
			ObjectNode schema, String typeCode) {
		if (builders.get(typeCode) == null) {
			DynamicTypeTransformationBuilder<T, ObjectNode> builder = createBuilder(typeCode);
			addGroup(schema, builder);
			builder.includeSuper(baseTransformation);
			return builder.buildTypeTransformation();
		} else {
//			DynamicTypeTransformationBuilder<T, ObjectNode> builder = replaceBuilder(typeCode);
//			builder.includeSuper(baseTransformation);
//			addGroup(schema, builder);
//			return builder.buildTypeTransformation();
			throw new IllegalStateException("type "+typeCode+" already exists");
		}
		
	}

	private EntityTypeBuilder createSourceTypeBuilder(String typeCode) {

		EntityTypeBuilder builder = builders.get(typeCode);
		if (builder == null) {
			builder = dynamicEntityTypeRepository.createBuilder(typeCode);
			builder.superType(sourceBaseType);
			builders.put(typeCode, builder);
		}
		return builder;
	}

	private EntityTypeBuilder createTargetTypeBuilder(String typeCode) {
		EntityTypeBuilder createBuilder = jsonEntityTypeRepository.createBuilder(typeCode);
		createBuilder.superType(targetBaseType);
		return createBuilder;
	}

	private EntityTypeBuilder replaceSourceTypeBuilder(String typeCode) {
		EntityTypeBuilder builder = dynamicEntityTypeRepository
				.replaceBuilder(typeCode);
		builder.superType(sourceBaseType);
		builders.put(typeCode, builder);
		return builder;
	}

	private EntityTypeBuilder replaceTargetTypeBuilder(String typeCode) {
		EntityTypeBuilder replaceBuilder = jsonEntityTypeRepository.replaceBuilder(typeCode);
		replaceBuilder.superType(targetBaseType);
		return replaceBuilder;
	}

	public void setJsonEntityTypeRepository(
			DynamicEntityTypeSubrepository<ObjectNode> jsonEntityTypeRepository) {
		this.jsonEntityTypeRepository = jsonEntityTypeRepository;
	}

	public void setTransformationBuilderFactory(
			TransformationBuilderFactory transformationBuilderFactory) {
		this.transformationBuilderFactory = transformationBuilderFactory;
	}

	public void addGroup(ObjectNode group,
			DynamicTypeTransformationBuilder<T, ObjectNode> builder) {
		addAttributesToGroup(builder, group);
	}

	private void addAttributesToGroup(
			DynamicTypeTransformationBuilder<T, ObjectNode> builder,
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

	private void addAttributes(
			DynamicTypeTransformationBuilder<T, ObjectNode> builder,
			ArrayNode attributes) {
		for (int i = 0; i < attributes.size(); i++) {
			addAttribute(builder, (ObjectNode) attributes.get(i));
		}
	}

	private JavaUniConverter<String, String> attributeCodeConverter;

	public void setAttributeCodeConverter(
			JavaUniConverter<String, String> attributeCodeConverter) {
		this.attributeCodeConverter = attributeCodeConverter;
	}

	private void addAttribute(
			DynamicTypeTransformationBuilder<T, ObjectNode> builder,
			ObjectNode attribute) {
		for (AttributeCreator attributeCreator : attributeCreators) {
			if (attributeCreator.handles(attribute)) {
				String newCode = attribute.get("code").getTextValue();
				if (attributeCodeConverter != null) {
					newCode = attributeCodeConverter.convert(newCode, null);
				}
				attributeCreator.addAttribute(attribute, newCode, builder);
				break;
			}
		}
	}

	public void setTypeCodeConverter(
			JavaConverter<String, String> typeCodeConverter) {
		this.typeCodeConverter = typeCodeConverter;
	}

	public DynamicTypeTransformationBuilder<T, ObjectNode> createBuilder(
			String typeCode) {
		
		String sourceTypeCode = typeCodeConverter.convertBA(typeCode, null);
		String transformationKey = sourceTypeCode+"::"+typeCode;
		DynamicTypeTransformationBuilder dynamicTypeTransformationBuilder = transformationBuilders.get(transformationKey);
		if (dynamicTypeTransformationBuilder==null) {
			dynamicTypeTransformationBuilder= transformationBuilderFactory.create(
				createSourceTypeBuilder(sourceTypeCode),
				createTargetTypeBuilder(typeCode));
			transformationBuilders.put(transformationKey, dynamicTypeTransformationBuilder);
		}
		return dynamicTypeTransformationBuilder;

	}

	public DynamicTypeTransformationBuilder<T, ObjectNode> replaceBuilder(
			String typeCode) {
		String sourceTypeCode = typeCodeConverter.convertBA(typeCode, null);
		return transformationBuilderFactory.create(
				replaceSourceTypeBuilder(sourceTypeCode),
				replaceTargetTypeBuilder(typeCode));

	}

	public void clear() {
		builders.clear();
		transformationBuilders.clear();
		jsonEntityTypeRepository.clear();
		dynamicEntityTypeRepository.clear();
	}

	public EntityTypeTransformation<Node, ObjectNode> updateType(String typeCode,
			ObjectNode schema) {
		DynamicTypeTransformationBuilder<T, ObjectNode> builder = replaceBuilder(typeCode);
		addGroup(schema, builder);
		builder.includeSuper(baseTransformation);
		return (EntityTypeTransformation<Node, ObjectNode>) builder
				.buildTypeTransformation();
	}

	public EntityTypeTransformation<T, ObjectNode> createType(ObjectNode schema) {

		JsonNode jsonNode = schema.get("code");
		if (jsonNode == null) {
			throw new IllegalArgumentException(
					"a schema must have a code proeprty");
		}
		return createTypeByTypeCode(schema, jsonNode.getTextValue());
	}

	public void deleteType(String jsonTypeCode) throws BusinessException {
		EntityType<ObjectNode> entityType = jsonEntityTypeRepository
				.getEntityType(jsonTypeCode);
		EntityTypeTransformation<T, ObjectNode> transformation = getTransformation(jsonTypeCode);
		EntityType<T> entityTypeA = transformation.getEntityTypeA();

		if (entityTypeA.getIncomingAssociations().size() == 0) {
			dynamicEntityTypeRepository.remove(entityTypeA.getCode());
		} else {
			throw new BusinessException("type is still being refernced");
		}
		jsonEntityTypeRepository.remove(jsonTypeCode);

	}

	

	public EntityTypeTransformation<T, ObjectNode> getTransformation(String jsonTypeCode) {
		DynamicTypeTransformationBuilder<T, ObjectNode> builder = createBuilder(jsonTypeCode);
		return (EntityTypeTransformation<T, ObjectNode>) builder.getReference();
		
		
//		EntityType<ObjectNode> jsonType = jsonEntityTypeRepository
//				.getEntityType(id);
//		DerivedType<T, ObjectNode> derivedType = (DerivedType<T, ObjectNode>) jsonType
//				.getMetaType()
//				.getMetaAttribute(DerivedType.META_ATTRIBUTE_CODE)
//				.getValue(jsonType);
//		return derivedType.getTransformation();
	}
	
	

	public JavaConverter<?, ?> getConverter(String code) {
		return null;
	}

	public <J> Type<J> getType(Class<J> clazz) {
		return entityTypeRepository.getType(clazz);
	}
}

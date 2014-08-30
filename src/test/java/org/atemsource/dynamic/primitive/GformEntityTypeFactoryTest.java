package org.atemsource.dynamic.primitive;

import java.io.IOException;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import junit.framework.Assert;

import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.attribute.Attribute;
import org.atemsource.atem.api.attribute.CollectionAttribute;
import org.atemsource.atem.api.infrastructure.exception.BusinessException;
import org.atemsource.atem.api.service.InsertionService;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.IncomingRelation;
import org.atemsource.atem.api.type.primitive.RefType;
import org.atemsource.atem.impl.json.JsonUtils;
import org.atemsource.atem.utility.transform.api.JacksonTransformationContext;
import org.atemsource.atem.utility.transform.impl.EntityTypeTransformation;
import org.atemsource.jcr.entitytype.PrimitiveListAttribute;
import org.atemsource.jcr.service.JcrCrudService;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "classpath:/test/atem/dynamictype/jcr.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class GformEntityTypeFactoryTest {

	@Inject
	private GformEntityTypeFactory<Node> gformEntityTypeFactory;

	@Inject
	private EntityTypeRepository entityTypeRepository;

	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Inject
	private Repository repository;
	
	@Before
	public void setup() {
		gformEntityTypeFactory.clear();
	}

	@Test
	public void testPrimitive() throws JsonProcessingException, IOException {
		ObjectNode schema = loadGform("primitive_string.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);
		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("text");
		Assert.assertEquals("text", attribute.getCode());
		Assert.assertEquals(String.class, attribute.getTargetType()
				.getJavaType());
		
		ObjectNode page=objectMapper.createObjectNode();
		page.put("url", "/index.html");
		page.put("template", "primitive_string");
		page.put("identifier", "/index.html");
		page.put("text", "xxx");
		
		String url = (String) entityTypeTransformation.getEntityTypeB().getAttribute("url").getValue(page);
		Assert.assertEquals(page.get("url").getTextValue(), url);
		String identifier = (String) entityTypeTransformation.getEntityTypeB().getAttribute("identifier").getValue(page);
		Assert.assertEquals(page.get("identifier").getTextValue(), identifier);
		String template = (String) entityTypeTransformation.getEntityTypeB().getAttribute("template").getValue(page);
		Assert.assertEquals(page.get("template").getTextValue(), template);
		EntityType<ObjectNode> entityType2 = entityTypeRepository.getEntityType(page);
		Assert.assertEquals(entityTypeTransformation.getEntityTypeB(), entityType2);
		
		Assert.assertEquals("xxx",entityTypeTransformation.getEntityTypeB().getAttribute("text").getValue(page));
		

	}
	
	@Test
	public void testPrimitiveList() throws JsonProcessingException, IOException, ValueFormatException, PathNotFoundException, RepositoryException {
		ObjectNode schema = loadGform("list_string.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);
		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("texts");
		Assert.assertEquals("texts", attribute.getCode());
		Assert.assertEquals(String.class, attribute.getTargetType()
				.getJavaType());
		
		ArrayNode texts=objectMapper.createArrayNode();
		texts.add(JsonUtils.convertToJson("hallo"));
		texts.add( JsonUtils.convertToJson("bye"));
		ObjectNode page=objectMapper.createObjectNode();
		page.put("url", "/index.html");
		page.put("template", "list_string");
		page.put("identifier", "/index.html");
		page.put("texts", texts);
		
		String url = (String) entityTypeTransformation.getEntityTypeB().getAttribute("url").getValue(page);
		Assert.assertEquals(page.get("url").getTextValue(), url);
		String identifier = (String) entityTypeTransformation.getEntityTypeB().getAttribute("identifier").getValue(page);
		Assert.assertEquals(page.get("identifier").getTextValue(), identifier);
		String template = (String) entityTypeTransformation.getEntityTypeB().getAttribute("template").getValue(page);
		Assert.assertEquals(page.get("template").getTextValue(), template);
		EntityType<ObjectNode> entityType2 = entityTypeRepository.getEntityType(page);
		Assert.assertEquals(entityTypeTransformation.getEntityTypeB(), entityType2);
		
		
		
		JcrCrudService service = (JcrCrudService) entityType.getService(InsertionService.class);
		service.createSession();
		try {
			Node convert = entityTypeTransformation.getBA().convert(page, new JacksonTransformationContext(entityTypeRepository));
			Assert.assertEquals(2,convert.getProperty("texts").getValues().length);
		} finally {
			service.closeSession();
		}		

	}
	
	
	
	@Test
	public void testRemovePrimitive() throws JsonProcessingException, IOException, BusinessException {
		ObjectNode schema = loadGform("primitive_string.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);
		EntityType<ObjectNode> entityType = entityTypeTransformation.getEntityTypeB();
		gformEntityTypeFactory.deleteType(entityType.getCode());

	}

	private ObjectNode loadGform(String file) throws IOException,
			JsonProcessingException {
		return (ObjectNode) objectMapper.readTree(getClass()
				.getResourceAsStream("/test/atem/dynamictype/samples/" + file));
	}

	@Test
	public void testEmbedded() throws JsonProcessingException, IOException {
		ObjectNode schema = loadGform("embedded.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);
		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("embedded");
		EntityType<?> embeddedType = (EntityType<?>) attribute.getTargetType();
		Assert.assertEquals(5, embeddedType.getAttributes().size());
		Assert.assertNotNull(entityType.getAttribute("path"));
		
		

	}

	@Test
	public void testEmbeddedList() throws JsonProcessingException, IOException {
		ObjectNode schema = loadGform("embedded-list.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);
		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("embeddeds");
		EntityType<?> embeddedType = (EntityType<?>) attribute.getTargetType();
		Assert.assertTrue(attribute instanceof CollectionAttribute);
		Assert.assertEquals(5, embeddedType.getAttributes().size());
		Assert.assertNotNull(entityType.getAttribute("path"));
		Assert.assertNotNull(entityTypeTransformation.getEntityTypeB());
		
		

	}

	@Test
	public void testTemplateRef() throws JsonProcessingException, IOException {
		ObjectNode primitive = loadGform("primitive_string.json");
		gformEntityTypeFactory.createType(primitive);
		ObjectNode schema = loadGform("template-ref.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);
		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("ref");
		EntityType<?> embeddedType = (EntityType<?>) attribute.getTargetType();
		Assert.assertEquals("jcr:primitive_string", embeddedType.getCode());

		
		ObjectNode e = objectMapper.createObjectNode();
		e.put("text", "hallo");
		e.put("template", "primitive_string");
		ObjectNode a = objectMapper.createObjectNode();
		a.put("template", "ref");
		a.put("url", "/index.html");
		a.put("ref", e);
		
		JcrCrudService service = (JcrCrudService) entityType.getService(InsertionService.class);
		service.createSession();
		try {
		Node convert = entityTypeTransformation.getBA().convert(a, new JacksonTransformationContext(entityTypeRepository));
		} finally {
			service.closeSession();
		}
	}

	@Test
	public void testTemplateRefWrongOrder() throws JsonProcessingException, IOException {
		ObjectNode schema = loadGform("template-ref.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);
		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("ref");
		EntityType<?> embeddedType = (EntityType<?>) attribute.getTargetType();
		Assert.assertEquals("jcr:primitive_string", embeddedType.getCode());

		
	}

	@Test
	public void testRef() throws JsonProcessingException, IOException {

		ObjectNode schema = loadGform("ref.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);

		ObjectNode schemaRef = loadGform("primitive_string.json");
		EntityTypeTransformation<Node, ObjectNode> refTransformation = gformEntityTypeFactory.createType(
				schemaRef);
		EntityType<Node> refEntityType=refTransformation.getEntityTypeA();

		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("ref");
		RefType associatedType = (RefType) attribute.getTargetType();
		Assert.assertEquals(refEntityType, associatedType.getTargetTypes()[0]);

		Assert.assertEquals(1, refEntityType.getIncomingAssociations().size());
		Attribute incomingAssociation = refEntityType.getIncomingAssociation(entityType.getCode());
		Assert.assertNotNull(incomingAssociation);
		Assert.assertEquals(entityType,incomingAssociation.getTargetType());
		Assert.assertEquals(refEntityType,incomingAssociation.getEntityType());

	}
	
	@Test
	public void testBinary() throws JsonProcessingException, IOException, ValueFormatException, PathNotFoundException, RepositoryException {

		ObjectNode schema = loadGform("binary.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);


		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		
		ObjectNode binary = objectMapper.createObjectNode();
		binary.put("url","url-to-image");
		ObjectNode node = objectMapper.createObjectNode();
		node.put("binary", binary);
		node.put("template", "binary");
		
		JcrCrudService service = (JcrCrudService) entityType.getService(InsertionService.class);
		service.createSession();
		try {
			Node convert = entityTypeTransformation.getBA().convert(node, new JacksonTransformationContext(entityTypeRepository));
			Assert.assertEquals("url-to-image", convert.getNode("binary").getProperty("url").getString());
		} finally {
			service.closeSession();
		}	
	}
	
	@Test
	public void testMultiRef() throws JsonProcessingException, IOException {

		ObjectNode schemaRef = loadGform("multi-ref.json");
		EntityTypeTransformation<Node, ObjectNode> refTransformation = gformEntityTypeFactory.createType(
				schemaRef);
		EntityType<Node> refEntityType=refTransformation.getEntityTypeA();


		Attribute attribute = refEntityType.getAttribute("ref");
		Assert.assertNotNull( attribute);
		// TODO support many types
		Assert.assertEquals("jcr:test",((RefType)attribute.getTargetType()).getTargetTypes()[0].getCode());

	}
	
	@Test
	public void testMultiRefList() throws JsonProcessingException, IOException {

		ObjectNode schemaRef = loadGform("multi-ref-list.json");
		
		EntityTypeTransformation<Node, ObjectNode> refTransformation = gformEntityTypeFactory.createType(
				schemaRef);
		EntityType<Node> refEntityType=refTransformation.getEntityTypeA();


		Attribute attribute = refEntityType.getAttribute("refs");
		Assert.assertNotNull( attribute);
		Assert.assertTrue(attribute instanceof PrimitiveListAttribute);
		Assert.assertEquals("jcr:test",((RefType)attribute.getTargetType()).getTargetTypes()[0].getCode());
		
		ArrayNode refs=objectMapper.createArrayNode();
		ObjectNode e = objectMapper.createObjectNode();
		e.put("refs", refs);
		e.put("template", "multi-ref-list");
		refs.add("hallo");
		
		JcrCrudService service = (JcrCrudService) refEntityType.getService(InsertionService.class);
		service.createSession();
		try {
		Node convert = refTransformation.getBA().convert(e, new JacksonTransformationContext(entityTypeRepository));
		} finally {
			service.closeSession();
		}		

	}
	
	@Test(expected=BusinessException.class)
	public void testRemoveRefFails() throws JsonProcessingException, IOException, BusinessException {

		ObjectNode schemaRef = loadGform("primitive_string.json");
		EntityTypeTransformation<Node, ObjectNode> refTransformation = gformEntityTypeFactory.createType(
				schemaRef);
		EntityType<Node> refEntityType=refTransformation.getEntityTypeA();

		ObjectNode schema = loadGform("ref.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);
		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		
		gformEntityTypeFactory.deleteType(schemaRef.get("code").getTextValue());

	}
	
	
	
	@Test
	public void testRefLazy() throws JsonProcessingException, IOException {


		ObjectNode schema = loadGform("ref.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation = gformEntityTypeFactory
				.createType(schema);

		ObjectNode schemaRef = loadGform("primitive_string.json");
		EntityTypeTransformation<Node, ObjectNode> refTransformation = gformEntityTypeFactory.createType(
				schemaRef);
		EntityType<Node> refEntityType=refTransformation.getEntityTypeA();

		EntityType<Node> entityType = entityTypeTransformation.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("ref");
		RefType associatedType = (RefType) attribute.getTargetType();
		Assert.assertEquals(refEntityType, associatedType.getTargetTypes()[0]);

		Assert.assertEquals(1, refEntityType.getIncomingAssociations().size());
		Attribute incomingAssociation = refEntityType.getIncomingAssociation(entityType.getCode());
		Assert.assertNotNull(incomingAssociation);
		Assert.assertEquals(entityType,incomingAssociation.getTargetType());
		Assert.assertEquals(refEntityType,incomingAssociation.getEntityType());

	}

	@After
	public void tearDown() {
		gformEntityTypeFactory.clear();
	}
	
	@Test
	public void testRefReplace() throws JsonProcessingException, IOException {

		ObjectNode schemaRef = loadGform("primitive_string.json");
		EntityTypeTransformation<Node, ObjectNode> refTransformation = gformEntityTypeFactory.createType(
				schemaRef);
		EntityType<Node> refEntityType=refTransformation.getEntityTypeA();

		ObjectNode schema = loadGform("ref.json");
		gformEntityTypeFactory
				.createType(schema);
		
		ObjectNode schema2 = loadGform("ref2.json");
		EntityTypeTransformation<Node, ObjectNode> entityTypeTransformation2 = gformEntityTypeFactory
				.updateType(schema2.get("code").getTextValue(),schema2);
		
		
		EntityType<Node> entityType = entityTypeTransformation2.getEntityTypeA();
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?, ?> attribute = entityType.getAttribute("ref2");
		RefType associatedType = (RefType) attribute.getTargetType();
		Assert.assertEquals(refEntityType, associatedType.getTargetTypes()[0]);

		Assert.assertEquals(1, refEntityType.getIncomingAssociations().size());
		IncomingRelation<?,?> incomingAssociation = refEntityType.getIncomingAssociation(entityType.getCode());
		Assert.assertNotNull(incomingAssociation);
		Assert.assertEquals("ref2", incomingAssociation.getAttribute().getCode());
		
		Assert.assertNotNull(entityTypeTransformation2.getSuperTransformation());
		Assert.assertNotNull(entityTypeTransformation2.getSuperTransformation().getTransformationByTypeB(entityType));
		

	}

}

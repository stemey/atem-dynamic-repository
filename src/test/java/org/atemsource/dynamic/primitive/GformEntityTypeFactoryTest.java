package org.atemsource.dynamic.primitive;

import java.io.IOException;

import javax.inject.Inject;
import javax.jcr.Node;

import junit.framework.Assert;

import org.atemsource.atem.api.attribute.Attribute;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.primitive.RefType;
import org.atemsource.jcr.entitytype.JcrEntityType;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;




@ContextConfiguration(locations = {"classpath:/test/atem/dynamictype/jcr.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class GformEntityTypeFactoryTest {

	
	@Inject
	private GformEntityTypeFactory<Node> gformEntityTypeFactory;
	
	private ObjectMapper objectMapper= new ObjectMapper();
	
	
	
	@Test
	public void testPrimitive() throws JsonProcessingException, IOException {
		ObjectNode schema= loadGform("primitive_string.json");
		EntityType<Node> entityType = gformEntityTypeFactory.createType(schema, "/meta/primitive_string");
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?,?> attribute=entityType.getAttribute("text");
		Assert.assertEquals("text",attribute.getCode());
		Assert.assertEquals(String.class,attribute.getTargetType().getJavaType());
		
	}

	private ObjectNode loadGform(String file) throws IOException, JsonProcessingException {
		return (ObjectNode) objectMapper.readTree(getClass().getResourceAsStream("/test/atem/dynamictype/samples/"+file));
	}
	
	@Test
	public void testEmbedded() throws JsonProcessingException, IOException {
		ObjectNode schema= loadGform("embedded.json");
		EntityType<Node> entityType = gformEntityTypeFactory.createType(schema, "/meta/embedded");
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?,?> attribute=entityType.getAttribute("embedded");
		JcrEntityType embeddedType=(JcrEntityType) attribute.getTargetType();
		Assert.assertEquals(5, embeddedType.getAttributes().size());
		
	}
	
	@Test
	public void testRef() throws JsonProcessingException, IOException {
		
		ObjectNode schemaRef= loadGform("primitive_string.json");
		EntityType<Node> refEntityType = gformEntityTypeFactory.createType(schemaRef, "/meta/ref_primitive_string");
		
		ObjectNode schema= loadGform("ref.json");
		EntityType<Node> entityType = gformEntityTypeFactory.createType(schema, "/meta/ref");
		Assert.assertEquals(5, entityType.getAttributes().size());
		Attribute<?,?> attribute=entityType.getAttribute("ref");
		RefType associatedType=(RefType) attribute.getTargetType();
		Assert.assertEquals(refEntityType,associatedType.getTargetType());
		
		Assert.assertEquals(1,refEntityType.getIncomingAssociations().size());
		Assert.assertNotNull(refEntityType.getIncomingAssociation("ref"));
		
	}
	
	
}

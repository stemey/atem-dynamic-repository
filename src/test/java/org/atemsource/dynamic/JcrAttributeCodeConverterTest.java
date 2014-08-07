package org.atemsource.dynamic;

import java.io.IOException;

import javax.inject.Inject;
import javax.jcr.Node;

import junit.framework.Assert;

import org.atemsource.atem.utility.transform.impl.EntityTypeTransformation;
import org.atemsource.dynamic.primitive.GformEntityTypeFactory;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "classpath:/test/atem/dynamictype/jcr.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class JcrAttributeCodeConverterTest {

	@Inject
	private GformEntityTypeFactory<Node> gformEntityTypeFactory;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Before
	public void setup() {
		gformEntityTypeFactory.clear();
	}

	private ObjectNode loadGform(String file) throws IOException,
			JsonProcessingException {
		return (ObjectNode) objectMapper.readTree(getClass()
				.getResourceAsStream("/test/atem/dynamictype/samples/" + file));
	}

	@Test
	public void testUrlToPath() throws JsonProcessingException, IOException {
		ObjectNode schema = loadGform("template.json");
		EntityTypeTransformation<Node, ObjectNode> transformation = gformEntityTypeFactory
				.createType(schema);
		Assert.assertNotNull(transformation.getEntityTypeB().getAttribute("url"));
		Assert.assertNotNull(transformation.getEntityTypeA().getAttribute("path"));
		Assert.assertEquals("url",transformation.getAttributeTransformationByA("path").getAttributeB().toString());
		
	}
}

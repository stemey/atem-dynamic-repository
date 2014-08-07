package org.atemsource.dynamic;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import javax.inject.Inject;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import junit.framework.Assert;

import org.apache.jackrabbit.commons.JcrUtils;
import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.impl.json.JsonEntityTypeImpl;
import org.atemsource.dynamic.RestService.Callback;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "classpath:/test/atem/dynamictype/jcr.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class RestServiceTest {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Inject
	private RestService restService;

	@Inject
	private Repository repository;

	private void assertSchemaNode(String id) throws LoginException,
			RepositoryException {
		Session session = repository.login();

		try {
			Node node = restService.getByTypeCode(session, id);
			Assert.assertEquals(id, node.getProperty("code").getString());

		} finally {
			session.logout();
		}
	}

	@Inject
	private EntityTypeRepository entityTypeRepository;

	@Test
	public void testCreate() throws JsonProcessingException, IOException {
		ObjectNode gform = loadGform("primitive_string.json");
		String id = (String) restService.create(gform);
		Assert.assertNotNull(id);
		EntityType<?> entityType = entityTypeRepository.getEntityType(id);
		Assert.assertTrue(entityType instanceof JsonEntityTypeImpl);

		// Assert.assertTrue(entityType instanceof JsonEntityTypeImpl);
	}

	@Test
	public void testGet() throws JsonProcessingException, IOException,
			LoginException, RepositoryException {
		ObjectNode gform = loadGform("primitive_string.json");
		String id = (String) restService.create(gform);

		ObjectNode schema = restService.getSchema(id);

		Assert.assertEquals("primitive_string", schema.get("code")
				.getTextValue());
		// Assert.assertEquals("string",schema.get("template").getTextValue()));
		EntityType<?> entityType = entityTypeRepository.getEntityType(id);
		Assert.assertTrue(entityType instanceof JsonEntityTypeImpl);

		// Assert.assertTrue(entityType instanceof JsonEntityTypeImpl);
		assertSchemaNode("jcr:" + id);

	}

	@Before
	public void setup() throws LoginException, NoSuchWorkspaceException,
			RepositoryException {
		// restService.initialize();
	}

	@Test
	public void testUpdate() throws JsonProcessingException, IOException {
		ObjectNode gform = loadGform("ref.json");
		String id = (String) restService.create(gform);
		restService.update(id, loadGform("ref2.json"));
		EntityType<?> entityType = entityTypeRepository.getEntityType(id);
		Assert.assertNotNull(entityType);
	}

	@Test
	public void testFindSchemas() throws JsonProcessingException, IOException {
		restService.create(loadGform("embedded.json"));
		restService.create(loadGform("ref.json"));
		restService.findSchemas("emb", new Callback<Iterator<ObjectNode>>() {

			@Override
			public void process(Iterator<ObjectNode> schemas) {
				if (schemas.hasNext()) {
					Assert.assertEquals("embedded", schemas.next().get("code")
							.getTextValue());
				} else {
					Assert.fail("cannot find element");
				}
			}
		});

	}

	@After
	public void tearDown() throws LoginException, NoSuchWorkspaceException,
			RepositoryException {
		restService.clear();
	}

	@Test
	public void testDelete() throws JsonProcessingException, IOException {
		ObjectNode gform = loadGform("ref.json");
		String id = (String) restService.create(gform);
		restService.delete(id);
		EntityType<?> entityType = entityTypeRepository.getEntityType(id);
		Assert.assertNull(entityType);
	}

	private ObjectNode loadGform(String file) throws IOException,
			JsonProcessingException {
		return (ObjectNode) objectMapper.readTree(getClass()
				.getResourceAsStream("/test/atem/dynamictype/samples/" + file));
	}
}

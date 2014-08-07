package org.atemsource.dynamic;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.atemsource.atem.service.entity.EntityRestService;
import org.atemsource.jcr.http.TreeServlet;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "classpath:/atem/jcr/application.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class EntityRestServiceTest {

	@Inject
	private EntityRestService entityRestService;
	
	private TreeServlet treeServlet= new TreeServlet();

	@Inject
	private RestService restService;

	private String schemaId="primitive_string";

	private ObjectMapper objectMapper = new ObjectMapper();

	@Before
	public void setup() throws JsonProcessingException, IOException {
		ObjectNode schema = restService.getSchema("primitive_string");
		if (schema == null) {
			 restService.create(loadGform("primitive_string.json"));
		}
	}
	
	
	private ObjectNode loadGform(String file) throws IOException,
			JsonProcessingException {
		return (ObjectNode) objectMapper.readTree(getClass()
				.getResourceAsStream("/test/atem/dynamictype/samples/" + file));
	}
	
	@Test
	public void testCrud() throws IOException, ServletException {
		String id = create("/index.html");
		get(id,"hallo");
		update("bye");
		get(id,"bye");
		//getCollection();
		delete(id);
	}
	
	@Test
	public void testTree() throws IOException, ServletException {
		String id1 = create("/xx/dudu.html");
		String id2 = create("/xx/dada.html");
		String id3 = create("/xx/yy/dada.html");
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		final String uri = "/";
		request.setRequestURI(uri);
		request.setMethod("GET");
		
		
		request.setParameter("parent", "/xx");
	
		treeServlet.service(request, response);
		String content = response.getContentAsString();
		ArrayNode result = (ArrayNode) objectMapper.readTree(content);
		Assert.assertEquals(3, result.size());
		Iterator<JsonNode> iterator = result.iterator();
		int folderCount=0;
		for (;iterator.hasNext();) {
			ObjectNode node = (ObjectNode) iterator.next();
			Assert.assertNotNull(node.get("id"));
			Assert.assertNotNull(node.get("name"));
			if (node.get("folder").getBooleanValue()) {
				folderCount++;
			}
		}
		Assert.assertEquals(1, folderCount);
		
		
	}
	
	@Test
	public void testRootTree() throws IOException, ServletException {
		String id1 = create("/hallo/dudu.html");
		String id2 = create("/hallo/dada.html");
		String id3 = create("/bye/dada.html");
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		final String uri = "/";
		request.setRequestURI(uri);
		request.setMethod("GET");
		
		
		
		treeServlet.service(request, response);
		String content = response.getContentAsString();
		ArrayNode result = (ArrayNode) objectMapper.readTree(content);
		Assert.assertEquals("/", result.get(0).get("id").getTextValue());
		
		
	}

	public void delete(String id) throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		final String uri = "/entity/" + schemaId + "/"+id;
		request.setRequestURI(uri);
		entityRestService.doDelete(request, response);

		Assert.assertEquals(200, response.getStatus());

	}

	public void update(String newtext) throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setContent(createPage("/index.html",newtext).toString().getBytes());

		final String uri = "/entity/" + schemaId + "//index.html";
		request.setRequestURI(uri);

		entityRestService.doPut(request, response);

		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals("", new String(response.getContentAsByteArray(),
				"UTF-8"));
	}

	public void get(String id, String text) throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		final String uri = "/entity/" + schemaId + "/"+id;
		request.setRequestURI(uri);

		entityRestService.doGet(request, response);

		Assert.assertEquals(200, response.getStatus());
		String result = new String(response.getContentAsByteArray(),
				"UTF-8");
		JsonNode node = objectMapper.readTree(result);
		Assert.assertEquals(schemaId, node.get("template").getTextValue());
		Assert.assertEquals(id, node.get("identifier").getTextValue());
		Assert.assertEquals(id, node.get("url").getTextValue());
		Assert.assertEquals(text, node.get("text").getTextValue());
	}

	public  void getCollection() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		final String uri = "/entity/"+schemaId;
		request.setRequestURI(uri);

		entityRestService.doGet(request, response);

		Assert.assertEquals(200, response.getStatus());
		String content = response.getContentAsString();
		ArrayNode result = (ArrayNode) objectMapper.readTree(content);
		Assert.assertEquals(1, result.size());
	}

	public  String create(String url) throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		ObjectNode node = createPage(url,"hallo");
		request.setContent(node.toString().getBytes());

		final String uri = "/entity/" + schemaId ;
		request.setRequestURI(uri);
		entityRestService.doPost(request, response);

		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals(url, new String(response.getContentAsByteArray(),
				"UTF-8"));
		return url;

	}

	private ObjectNode createPage(String url,String text) {
		ObjectNode node = objectMapper.createObjectNode();
		node.put("url", url);
		node.put("template", schemaId);
		node.put("text", text);
		return node;
	}

}

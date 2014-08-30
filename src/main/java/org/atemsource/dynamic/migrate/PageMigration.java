package org.atemsource.dynamic.migrate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.atemsource.atem.api.BeanLocator;
import org.atemsource.atem.api.infrastructure.bean.Bean;
import org.atemsource.atem.service.entity.EntityRestService;
import org.atemsource.dynamic.RestService;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class PageMigration {

	public static void main(String[] args) throws JsonProcessingException,
			IOException, LoginException, NoSuchWorkspaceException,
			RepositoryException {

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/atem/jcr/application.xml");
		try {

			EntityRestService restService = ctx
					.getBean(EntityRestService.class);

			ObjectMapper objectMapper = new ObjectMapper();
			MongoClient mongoClient = new MongoClient("localhost", 27017);

			DB db = mongoClient.getDB("test");
			Map<String, String> templateMapping = createMapping(db);

			deleteExistingPages();

			String schemaCollection = "page2";
			DBCollection collection = db.getCollection(schemaCollection);
			DBCursor cursor = collection.find();
			String code = null;
			try {
				System.out.println("##### START");
				while (cursor.hasNext()) {
					try {
						DBObject in = cursor.next();
						code = (String) in.get("url");
						ObjectNode page = (ObjectNode) objectMapper.readTree(in
								.toString());
						page.remove("_id");
						JsonNode templateNode = page.get("template");
						String template;
						if (templateNode instanceof ObjectNode) {
							template = templateMapping.get(templateNode.get(
									"$ref").getTextValue());
						} else {
							template = templateMapping.get(page.get("template")
									.getTextValue());
						}

						page.put("template", template);
						if (template == null) {
							System.out.println("FAILED");
						} else {

							MockHttpServletRequest req = new MockHttpServletRequest(
									"POST", "/entity/base");
							req.setContent(page.toString().getBytes("UTF-8"));
							MockHttpServletResponse resp = new MockHttpServletResponse();
							restService.doPost(req, resp);
							System.out.println("####### success " + code
									+ "  : " + resp.getStatus());
						}
					} catch (Exception e) {
						System.out.println("####### failed " + code + " "
								+ e.getMessage());
						e.printStackTrace();

					}
				}
			} finally {
				cursor.close();

			}
		} finally {
			ctx.close();
		}
	}

	private static void deleteExistingPages() throws LoginException,
			NoSuchWorkspaceException, RepositoryException {
		if (true) {
			return;
		}
		Repository repository = BeanLocator.getInstance().getInstance(
				Repository.class);
		Session session = repository.login("default");
		try {
			
			Node rootNode = session.getRootNode();
			NodeIterator nodes = rootNode.getNodes();
			for (; nodes.hasNext();) {
				Node nextNode = nodes.nextNode();
				if (nextNode.getName().contains(":")
						|| nextNode.getName().equals("schemas")) {

				} else {
					System.out.println("delete " + nextNode.getName());
					removeRecursively(session, nextNode);
					break;
				}
			}

			session.save();
		} finally {
			session.logout();
		}

		throw new IllegalStateException("stop");
	}

	private static void removeRecursively(Session session, Node node)
			throws RepositoryException {
		NodeIterator nodes = node.getNodes();
		for (; nodes.hasNext();) {
			removeRecursively(session, nodes.nextNode());
		}
		node.remove();
		session.save();

	}

	private static Map<String, String> createMapping(DB db) {
		Map<String, String> mapping = new HashMap<String, String>();
		String schemaCollection = "template2";

		DBCollection collection = db.getCollection(schemaCollection);
		DBCursor cursor = collection.find();
		String code = null;
		System.out.println("##### START");
		try {
			while (cursor.hasNext()) {
				try {
					DBObject in = cursor.next();
					code = (String) in.get("name");
					// restService.create(schema);
					System.out.println("map: " + "/template/" + in.get("_id")
							+ "->" + code);
					mapping.put("/template/" + in.get("_id"), code);
				} catch (Exception e) {
					System.out.println("####### failed " + code + " "
							+ e.getMessage());
					e.printStackTrace();

				}
			}
		} finally {
			cursor.close();
		}
		return mapping;
	}
}

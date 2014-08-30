package org.atemsource.dynamic;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.jcr.ItemExistsException;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.log4j.Logger;
import org.atemsource.atem.api.infrastructure.exception.BusinessException;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.utility.transform.impl.EntityTypeTransformation;
import org.atemsource.dynamic.primitive.GformEntityTypeFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.eclipse.jetty.util.log.Log;

public class RestService {
	private static final String SCHEMA_PROPERTY = "schema";

	private static Logger logger = Logger.getLogger(RestService.class);
	public interface Callback<T> {
		public void process(T t);
	}

	public void setFactory(GformEntityTypeFactory<?> factory) {
		this.factory = factory;
	}

	public void setSchemaPath(String schemaPath) {
		this.schemaPath = schemaPath;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	private GformEntityTypeFactory<?> factory;

	private String schemaPath = "/schemas";

	private Repository repository;

	private ObjectMapper objectMapper;

	private Session createSession() throws LoginException,
			NoSuchWorkspaceException, RepositoryException {
		return repository.login("default");//new SimpleCredentials("admin","admin".toCharArray()),"default");
	}

	@PostConstruct
	public void initialize() throws LoginException, NoSuchWorkspaceException,
			RepositoryException {
		Session session = createSession();
		try {
			JcrUtils.getOrCreateByPath(schemaPath, NodeType.NT_FOLDER,
					NodeType.NT_UNSTRUCTURED, session, true);
			Iterator<ObjectNode> iterator = loadSchemas(session);
			for (; iterator.hasNext();) {
				ObjectNode next = iterator.next();
				factory.createType(next);
			}
		} finally {
			session.logout();
		}
	}

	public void clear() {
		factory.clear();
	}

	private class SchemaIterator implements Iterator<ObjectNode> {
		private NodeIterator nodeIterator;

		public SchemaIterator(NodeIterator nodeIterator) {
			super();
			this.nodeIterator = nodeIterator;
		}

		@Override
		public boolean hasNext() {
			return nodeIterator.hasNext();
		}

		@Override
		public ObjectNode next() {
			Node next = nodeIterator.nextNode();
			try {
				return convert(next);
			} catch (Exception e) {
				throw new TechnicalException("cannot read schema", e);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("not implmented");
		}

	}

	private Iterator<ObjectNode> loadSchemas(Session session)
			throws PathNotFoundException, RepositoryException {
		NodeIterator nodes = session.getNode(schemaPath).getNodes();
		return new SchemaIterator(nodes);

	}

	public ObjectNode convert(Node next) throws JsonProcessingException,
			IOException, ValueFormatException, PathNotFoundException,
			RepositoryException {
		if (next == null) {
			return null;
		} else {
			String schema = next.getProperty(SCHEMA_PROPERTY).getString();
			ObjectNode schemaNode = (ObjectNode) objectMapper.readTree(schema);
			// schemaNode.put("id", next.getProperty("code").getString());
			return schemaNode;
		}
	}

	public String create(ObjectNode schema) {
		try {
			EntityTypeTransformation<?, ObjectNode> transformation = factory
					.createType(schema);
			createSchema(schema);
			return transformation.getEntityTypeB().getCode();
		} catch (Exception e) {
			throw new TechnicalException("cannot create schema ", e);
		}
	}

	private Serializable createSchema(ObjectNode schema)
			throws ItemExistsException, PathNotFoundException,
			VersionException, ConstraintViolationException, LockException,
			RepositoryException {
		Session session = createSession();
		try {
			String typeCode = schema.get("code").getTextValue();
			if (StringUtils.isEmpty(typeCode)) {
				throw new TechnicalException(
						"a schema needs to have a code property");
			}
			Node schemaNode = session.getNode(schemaPath);
			Node node = schemaNode.addNode(UUID.randomUUID().toString(),
					NodeType.NT_UNSTRUCTURED);
			node.setProperty(SCHEMA_PROPERTY, schema.toString());
			JsonNode nameNode = schema.get("name");
			if (nameNode != null) {
				node.setProperty("name", nameNode.getTextValue());
			}
			node.setProperty("code",
					new JcrTypeCodeConverter().convertBA(typeCode, null));
			session.save();
			return node.getIdentifier();
		} finally {
			session.logout();
		}
	}

	Node getByTypeCode(Session session, String id)
			throws InvalidQueryException, RepositoryException {
		Query query = session
				.getWorkspace()
				.getQueryManager()
				.createQuery(
						"select * from [nt:unstructured] where [code] = $id",
						Query.JCR_SQL2);
		Value idValue = session.getValueFactory().createValue(id);
		query.bindValue("id", idValue);
		QueryResult queryResult = query.execute();
		NodeIterator nodes = queryResult.getNodes();
		if (nodes.hasNext()) {
			Node result= nodes.nextNode();
			if (nodes.hasNext()) {
				logger.warn("more than one schema for "+id);
			}
			return result;
		} else {
			return null;
		}
	}

	public void update(String id, ObjectNode schema) {
		try {
			String jcrTypeCode = new JcrTypeCodeConverter().convertBA(id, null);
			factory.updateType(id, schema);

			updateSchema(jcrTypeCode, schema);
		} catch (Exception e) {
			throw new TechnicalException("cannot update schema " + id, e);
		}
	}

	public String getTypeCode(String jsonTypeCode) {
		return factory.getTransformation(jsonTypeCode).getEntityTypeA()
				.getCode();
	}

	private void updateSchema(String id, ObjectNode schema)
			throws LoginException, NoSuchWorkspaceException,
			RepositoryException {
		Session session = createSession();
		try {
			Node node = getByTypeCode(session, id);
			node.setProperty(SCHEMA_PROPERTY, schema.toString());
			JsonNode nameNode = schema.get("name");
			if (nameNode != null) {
				node.setProperty("name", nameNode.getTextValue());
			}
			logger.debug("updated schema "+id);
			 //factory.updateType(id, schema);
			session.save();
		} finally {
			session.logout();
		}
	}

	public void delete(String id) {
		try {
			deleteSchema(id);
		} catch (Exception e) {
			throw new TechnicalException("cannot delete schema " + id, e);
		}
	}

	public void getSchemas(Callback<Iterator<ObjectNode>> callback) {
		Session session = null;
		try {
			session = createSession();

			callback.process(new SchemaIterator(session.getNode(schemaPath)
					.getNodes()));

		} catch (Exception e) {
			throw new TechnicalException("cannot find schemas", e);
		} finally {
			session.logout();
		}
	}

	public void findSchemas(String name, Callback<Iterator<ObjectNode>> callback) {
		Session session = null;
		try {
			session = createSession();
			Query query = session
					.getWorkspace()
					.getQueryManager()
					.createQuery(
							"select * from [nt:unstructured] where [jcr:path] like '"
									+ schemaPath
									+ "%' and (name like $name or code like $name)",
							Query.JCR_SQL2);
			Value nameValue = session.getValueFactory().createValue(
					"jcr:" + name.replace('*', '%'));
			query.bindValue("name", nameValue);
			QueryResult queryResult = query.execute();
			callback.process(new SchemaIterator(queryResult.getNodes()));

		} catch (Exception e) {
			throw new TechnicalException("cannot find schemas", e);
		} finally {
			session.logout();
		}
	}

	private void deleteSchema(String id) throws LoginException,
			NoSuchWorkspaceException, RepositoryException,
			JsonProcessingException, BusinessException, IOException {
		Session session = createSession();
		try {
			String jcrId = new JcrTypeCodeConverter().convertBA(id, null);
			factory.deleteType(id);
			Node node = getByTypeCode(session, jcrId);
			session.removeItem(node.getPath());
		} finally {
			session.logout();
		}
	}

	public ObjectNode getSchema(String id) {
		try {
			Session session = createSession();
			try {
				String jcrId = new JcrTypeCodeConverter().convertBA(id, null);
				Node node = getByTypeCode(session, jcrId);
				return convert(node);
			} finally {
				session.logout();
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot get schema " + id, e);
		}
	}

	public static class Schema {
		public ObjectNode schema;
		String id;
	}
}

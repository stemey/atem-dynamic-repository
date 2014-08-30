package org.atemsource.dynamic.migrate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.atemsource.dynamic.RestService;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class UniqueCodeMigration {
	
	public static void main(String[] args) throws JsonProcessingException, IOException, InvalidQueryException, RepositoryException {
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/atem/jcr/application.xml");
		
		Repository repository = ctx.getBean(Repository.class);
		
		Map<String,Integer> counts = new HashMap<String, Integer>();
		Session session = repository.login("default");
		try {
			Query query = session.getWorkspace().getQueryManager().createQuery("select * from [nt:base] where [jcr:path] like \"/schemas/%\" ", javax.jcr.query.Query.JCR_SQL2);
			NodeIterator nodes = query.execute().getNodes();
			for (;nodes.hasNext();) {
				Node node = nodes.nextNode();
				String code = node.getProperty("name").getString();
				System.out.println("node "+code);
				String schema = node.getProperty("schema").getString();
				
				
				
				if (schema.contains("localhost:")) {
					System.out.println("error in "+code);
				}
				if (counts.containsKey(code)) {
					counts.put(code,counts.get(code)+1);
				}else{
					counts.put(code,new Integer(1));
				}
			}
			for (Map.Entry<String,Integer> entry:counts.entrySet()) {
				System.out.println("###  "+entry.getKey()+"  "+entry.getValue());
			}
		}finally {
			session.logout();
			ctx.close();
		}
	}
}

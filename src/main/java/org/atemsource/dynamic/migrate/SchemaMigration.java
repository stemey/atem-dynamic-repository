package org.atemsource.dynamic.migrate;

import java.io.IOException;

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

public class SchemaMigration {
	
	public static void main(String[] args) throws JsonProcessingException, IOException {
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/atem/jcr/application.xml");
		
		RestService restService = ctx.getBean(RestService.class);
		
		
		ObjectMapper objectMapper= new ObjectMapper();
		String schemaCollection="template2";
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		
		DB db = mongoClient.getDB("test");
		DBCollection collection = db.getCollection(schemaCollection);
		DBCursor cursor = collection.find();
		String code=null;
		System.out.println("##### START");
		try {
			while (cursor.hasNext()) {
				try{
				DBObject in = cursor.next();
				code=(String) in.get("name");
				ObjectNode schema = (ObjectNode)objectMapper.readTree(in.toString());
				schema.remove("_id");
				schema.put("sourceCode",(String) in.get("code"));
				schema.put("code", (String) in.get("name"));
				restService.create(schema);
				System.out.println("####### success "+code);
				} catch (Exception e) {
					System.out.println("####### failed "+code+" "+e.getMessage());
					e.printStackTrace();
					
				}
			}
		} finally {
			cursor.close();
			ctx.close();
		}
	}
}

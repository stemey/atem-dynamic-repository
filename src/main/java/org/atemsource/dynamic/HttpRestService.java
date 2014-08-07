package org.atemsource.dynamic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.atemsource.atem.service.meta.service.Cors;
import org.atemsource.dynamic.RestService.Callback;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class HttpRestService {

	private static Logger logger = Logger.getLogger(HttpRestService.class);
	private ObjectMapper objectMapper;
	private Cors cors;
	private String basePath = "/schema";

	public String parseId(String uri) {
		return uri.substring(basePath.length() + 1);
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	private RestService restService;

	/**
	 * insert a new entity
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public <O> void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		cors.appendCors(resp);
		try {
			String uri = req.getRequestURI();
			String id = parseId(uri);
			restService.update(id,
					(ObjectNode) objectMapper.readTree(req.getInputStream()));
		} catch (Exception e) {
			handle500Error(resp, e);
		}

	}

	private void handle500Error(HttpServletResponse resp, Exception e)
			throws IOException {
		logger.error("error when serving request", e);
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		// e.printStackTrace(resp.getWriter());
		resp.flushBuffer();
	}

	/**
	 * update an existing entity
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		cors.appendCors(resp);
		try {
			String uri = req.getRequestURI();

			BufferedReader reader = req.getReader();
			ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(reader);
			Serializable id = restService.create(jsonNode);
			objectMapper.writeValue(resp.getWriter(), id);

		} catch (Exception e) {
			handle500Error(resp, e);
		}

	}

	public void doOptions(HttpServletResponse resp) {
		cors.appendCors(resp);
	}

	/**
	 * get a sngle entity or a collection
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		cors.appendCors(resp);
		String uri = req.getRequestURI();
		try {
			try {
				if (uri.equals(basePath) || uri.equals(basePath+"/")) {
					String query = req.getParameter("query");
					final PrintWriter writer = resp.getWriter();
					writer.write("[");
					Callback callback=new Callback<Iterator<ObjectNode>>() {

						@Override
						public void process(Iterator<ObjectNode> t) {
							while (t.hasNext()) {
								writer.write(t.next().toString());
								if (t.hasNext()) {
									writer.write(",");
								}
							}
						}
					};
					
					if (query!=null) {
						restService.findSchemas(query, callback);
					}else {
						restService.getSchemas( callback);
					}
					
					writer.write("]");

				} else {
					String id = parseId(uri);
					//String jcrId=new JcrTypeCodeConverter().convertBA(id, null);
					ObjectNode schema = restService.getSchema(id);
					resp.getWriter().write(schema.toString());
				}

			} catch (Exception e) {
				handle500Error(resp, e);
			}
		} catch (Exception e) {
			logger.error("cannot find resource", e);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}

	}

	/**
	 * delete an entity
	 * 
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		cors.appendCors(resp);
		try {
			String uri = req.getRequestURI();
			String id = parseId(uri);
			cors.appendCors(resp);
			restService.delete(id);
		} catch (Exception e) {
			handle500Error(resp, e);
		}
	}

	public void setCors(Cors cors) {
		this.cors = cors;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setRestService(RestService restService) {
		this.restService = restService;
	}
}

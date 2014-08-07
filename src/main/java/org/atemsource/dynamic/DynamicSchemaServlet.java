package org.atemsource.dynamic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atemsource.atem.api.BeanLocator;

public class DynamicSchemaServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BeanLocator.getInstance().getInstance(HttpRestService.class).doDelete(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BeanLocator.getInstance().getInstance(HttpRestService.class).doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BeanLocator.getInstance().getInstance(HttpRestService.class).doPost(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BeanLocator.getInstance().getInstance(HttpRestService.class)
				.doPut(req, resp);
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BeanLocator.getInstance().getInstance(HttpRestService.class).doOptions(resp);
	}

}

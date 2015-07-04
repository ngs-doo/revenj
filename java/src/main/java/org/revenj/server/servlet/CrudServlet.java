package org.revenj.server.servlet;

import org.revenj.patterns.ServiceLocator;
import org.revenj.server.ProcessingEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CrudServlet extends HttpServlet {

	private final ServiceLocator locator;
	private final ProcessingEngine engine;

	public CrudServlet(ServiceLocator locator, ProcessingEngine engine) {
		this.locator = locator;
		this.engine = engine;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("application/json");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("application/json");
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
	}
}

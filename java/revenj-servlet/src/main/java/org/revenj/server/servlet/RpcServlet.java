package org.revenj.server.servlet;

import org.revenj.patterns.ServiceLocator;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Optional;

public class RpcServlet extends HttpServlet {

	private final ProcessingEngine engine;

	public RpcServlet(ProcessingEngine engine) {
		this.engine = engine;
	}

	RpcServlet(ServiceLocator locator) {
		this(locator.resolve(ProcessingEngine.class));
	}

	private void executeRequest(HttpServletRequest req, HttpServletResponse res, ServletInputStream stream) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path == null || path.length() < 1) {
			res.sendError(400, "Command not specified");
			return;
		}
		String name = path.substring(1);
		Optional<Class<?>> command = engine.findCommand(name);
		if (!command.isPresent()) {
			res.sendError(404, "Unknown command: " + name);
			return;
		}
		String argument;
		if (stream == null) {
			argument = null;
		} else {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			int len;
			while ((len = stream.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			argument = new String(baos.toByteArray(), req.getCharacterEncoding() != null ? req.getCharacterEncoding() : "UTF-8");
		}
		ServerCommandDescription[] scd = new ServerCommandDescription[]{
				new ServerCommandDescription<>(null, command.get(), argument)
		};
		ProcessingResult<String> result = engine.execute(String.class, String.class, scd, Utility.toPrincipal(req));
		Utility.returnJSON(res, result);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		executeRequest(req, res, null);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		executeRequest(req, res, req.getInputStream());
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		executeRequest(req, res, req.getInputStream());
	}
}

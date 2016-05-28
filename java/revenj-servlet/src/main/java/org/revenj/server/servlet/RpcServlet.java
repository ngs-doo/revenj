package org.revenj.server.servlet;

import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;

public class RpcServlet extends HttpServlet {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final ProcessingEngine engine;
	private final WireSerialization serialization;

	public RpcServlet(ProcessingEngine engine, WireSerialization serialization) {
		this.engine = engine;
		this.serialization = serialization;
	}

	RpcServlet(ServiceLocator locator) {
		this(locator.resolve(ProcessingEngine.class), locator.resolve(WireSerialization.class));
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
		String argument = Utility.readString(stream, req.getCharacterEncoding());
		ServerCommandDescription[] scd = new ServerCommandDescription[]{
				new ServerCommandDescription<>(null, command.get(), argument)
		};
		ProcessingResult<Object> result = engine.execute(String.class, Object.class, scd, Utility.toPrincipal(req));
		Utility.returnResponse(req, res, serialization, result);
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

package org.revenj.server.servlet;

import org.revenj.server.ProcessingEngine;
import org.revenj.server.commands.crud.Read;
import org.revenj.server.commands.crud.Update;
import org.revenj.server.commands.crud.Create;
import org.revenj.server.commands.crud.Delete;
import org.revenj.patterns.DomainModel;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.WireSerialization;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiFunction;

public class CrudServlet extends HttpServlet {

	private final DomainModel model;
	private final ProcessingEngine engine;
	private final WireSerialization serialization;

	public CrudServlet(DomainModel model, ProcessingEngine engine, WireSerialization serialization) {
		this.model = model;
		this.engine = engine;
		this.serialization = serialization;
	}

	CrudServlet(ServiceLocator locator) {
		this(locator.resolve(DomainModel.class), locator.resolve(ProcessingEngine.class), locator.resolve(WireSerialization.class));
	}

	private <T> Optional<T> check(HttpServletRequest req, HttpServletResponse res, BiFunction<String, String, T> call) throws IOException {
		String path = req.getPathInfo();
		if (path.length() == 0 || path.charAt(0) != '/') {
			res.sendError(400, "Invalid url path. Expecting /module.name?uri=value");
			return Optional.empty();
		}
		String name = path.substring(1, path.length());
		Optional<Class<?>> manifest = model.find(name);
		if (!manifest.isPresent()) {
			res.sendError(400, "Unknown domain object: " + name);
			return Optional.empty();
		}
		String uri = req.getParameter("uri");
		if (uri == null) {
			res.sendError(400, "Uri parameter not set. Expecting /module.name?uri=value");
			return Optional.empty();
		}
		return Optional.of(call.apply(name, uri));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		check(req, res, Read.Argument::new).ifPresent(arg -> Utility.executeJson(engine, res, Read.class, arg));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.length() == 0 || path.charAt(0) != '/') {
			res.sendError(400, "Invalid url path. Expecting /module.name");
			return;
		}
		String name = path.substring(1, path.length());
		Optional<Class<?>> manifest = model.find(name);
		if (!manifest.isPresent()) {
			res.sendError(400, "Unknown domain object: " + name);
			return;
		}
		Object instance = serialization.deserialize(manifest.get(), req.getInputStream(), req.getContentType());
		Utility.executeJson(engine, res, Create.class, new Create.Argument<>(name, instance));
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.length() == 0 || path.charAt(0) != '/') {
			res.sendError(400, "Invalid url path. Expecting /module.name?uri=value");
			return;
		}
		String name = path.substring(1, path.length());
		Optional<Class<?>> manifest = model.find(name);
		if (!manifest.isPresent()) {
			res.sendError(400, "Unknown domain object: " + name);
			return;
		}
		String uri = req.getParameter("uri");
		if (uri == null) {
			res.sendError(400, "Uri parameter not set. Expecting /module.name?uri=value");
			return;
		}
		Object instance = serialization.deserialize(manifest.get(), req.getInputStream(), req.getContentType());
		Utility.executeJson(engine, res, Update.class, new Update.Argument<>(name, uri, instance));
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		check(req, res, Delete.Argument::new).ifPresent(arg -> Utility.executeJson(engine, res, Delete.class, arg));
	}
}

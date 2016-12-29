package org.revenj.server.servlet;

import org.revenj.patterns.*;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.commands.*;
import org.revenj.server.commands.search.*;
import org.revenj.serialization.WireSerialization;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DomainServlet extends HttpServlet {

	private final DomainModel model;
	private final ProcessingEngine engine;
	private final WireSerialization serialization;

	public DomainServlet(DomainModel model, ProcessingEngine engine, WireSerialization serialization) {
		this.model = model;
		this.engine = engine;
		this.serialization = serialization;
	}

	DomainServlet(ServiceLocator locator) {
		this(locator.resolve(DomainModel.class), locator.resolve(ProcessingEngine.class), locator.resolve(WireSerialization.class));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/search/")) {
			Optional<String> name = Utility.findName(model, path, "/search/", res);
			if (name.isPresent()) {
				String spec = req.getParameter("specification");
				Optional<Object> specification = Utility.specificationFromQuery(name.get(), spec, model, req, res);
				if (spec != null && !specification.isPresent()) {
					return;
				}
				List<Map.Entry<String, Boolean>> order = Utility.parseOrder(req.getParameter("order"));
				Integer limit;
				Integer offset;
				try {
					limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : null;
					offset = req.getParameter("offset") != null ? Integer.parseInt(req.getParameter("offset")) : null;
				} catch (NumberFormatException nfe) {
					res.sendError(400, "Invalid limit/offset provided: " + nfe.getMessage());
					return;
				}
				SearchDomainObject.Argument arg = new SearchDomainObject.Argument<>(name.get(), spec, specification.orElse(null), offset, limit, order);
				Utility.execute(engine, req, res, serialization, SearchDomainObject.class, arg);
			}
		} else if (path.startsWith("/count/")) {
			Optional<String> name = Utility.findName(model, path, "/count/", res);
			if (name.isPresent()) {
				String spec = req.getParameter("specification");
				Optional<Object> specification = Utility.specificationFromQuery(name.get(), spec, model, req, res);
				if (spec != null && !specification.isPresent()) {
					return;
				}
				CountDomainObject.Argument arg = new CountDomainObject.Argument<>(name.get(), spec, specification.orElse(null));
				Utility.execute(engine, req, res, serialization, CountDomainObject.class, arg);
			}
		} else if (path.startsWith("/exists/")) {
			Optional<String> name = Utility.findName(model, path, "/exists/", res);
			if (name.isPresent()) {
				String spec = req.getParameter("specification");
				Optional<Object> specification = Utility.specificationFromQuery(name.get(), spec, model, req, res);
				if (spec != null && !specification.isPresent()) {
					return;
				}
				DomainObjectExists.Argument arg = new DomainObjectExists.Argument<>(name.get(), spec, specification.orElse(null));
				Utility.execute(engine, req, res, serialization, DomainObjectExists.class, arg);
			}
		} else if (path.startsWith("/check/")) {
			Optional<String> name = Utility.findName(model, path, "/check/", res);
			if (name.isPresent()) {
				String uri = req.getParameter("uri");
				if (uri == null) {
					res.sendError(400, "Uri parameter not set. Expecting /module.name?uri=value");
					return;
				}
				CheckDomainObject.Argument arg = new CheckDomainObject.Argument(name.get(), uri);
				Utility.execute(engine, req, res, serialization, CheckDomainObject.class, arg);
			}
		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/submit/")) {
			String name = path.substring("/submit/".length(), path.length());
			Optional<Class<?>> manifest = model.find(name);
			if (!manifest.isPresent()) {
				res.sendError(400, "Unknown domain object: " + name);
				return;
			}
			Optional<?> domainEvent = Utility.deserializeOrBadRequest(serialization, manifest.get(), req, res);
			if (!domainEvent.isPresent()) return;
			SubmitEvent.Argument arg = new SubmitEvent.Argument<>(name, domainEvent.get(), Utility.returnInstance(req));
			Utility.execute(engine, req, res, serialization, SubmitEvent.class, arg);
		} else if (path.startsWith("/queue/")) {
			String name = path.substring("/queue/".length(), path.length());
			Optional<Class<?>> manifest = model.find(name);
			if (!manifest.isPresent()) {
				res.sendError(400, "Unknown domain object: " + name);
				return;
			}
			Optional<?> domainEvent = Utility.deserializeOrBadRequest(serialization, manifest.get(), req, res);
			if (!domainEvent.isPresent()) return;
			QueueEvent.Argument arg = new QueueEvent.Argument<>(name, domainEvent.get());
			Utility.execute(engine, req, res, serialization, QueueEvent.class, arg);
		} else if (path.startsWith("/find/")) {
			findWithArguments(req, res, path);
		} else if (path.startsWith("/search/")) {
			searchWithArguments(req, res, path);
		} else if (path.startsWith("/count/")) {
			countWithArguments(req, res, path);
		} else if (path.startsWith("/exists/")) {
			existsWithArguments(req, res, path);

		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/find/")) {
			findWithArguments(req, res, path);
		} else if (path.startsWith("/search/")) {
			searchWithArguments(req, res, path);
		} else if (path.startsWith("/count/")) {
			countWithArguments(req, res, path);
		} else if (path.startsWith("/exists/")) {
			existsWithArguments(req, res, path);
		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}

	private void existsWithArguments(HttpServletRequest req, HttpServletResponse res, String path) throws IOException {
		Optional<String> name = Utility.findName(model, path, "/exists/", res);
		if (name.isPresent()) {
			executeWithSpecification(
					DomainObjectExists.class,
					req,
					res,
					name.get(),
					spec -> new DomainObjectExists.Argument<>(name.get(), null, spec));
		}
	}

	private void countWithArguments(HttpServletRequest req, HttpServletResponse res, String path) throws IOException {
		Optional<String> name = Utility.findName(model, path, "/count/", res);
		if (name.isPresent()) {
			executeWithSpecification(
					CountDomainObject.class,
					req,
					res,
					name.get(),
					spec -> new CountDomainObject.Argument<>(name.get(), null, spec));
		}
	}

	private void searchWithArguments(HttpServletRequest req, HttpServletResponse res, String path) throws IOException {
		final Optional<String> name = Utility.findName(model, path, "/search/", res);
		if (name.isPresent()) {
			List<Map.Entry<String, Boolean>> order = Utility.parseOrder(req.getParameter("order"));
			Integer limit;
			Integer offset;
			try {
				limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : null;
				offset = req.getParameter("offset") != null ? Integer.parseInt(req.getParameter("offset")) : null;
			} catch (NumberFormatException nfe) {
				res.sendError(400, "Invalid limit/offset provided: " + nfe.getMessage());
				return;
			}
			executeWithSpecification(
					SearchDomainObject.class,
					req,
					res,
					name.get(),
					spec -> new SearchDomainObject.Argument<>(name.get(), null, spec, offset, limit, order));
		}
	}

	private void findWithArguments(HttpServletRequest req, HttpServletResponse res, String path) throws IOException {
		Optional<String> name = Utility.findName(model, path, "/find/", res);
		if (name.isPresent()) {
			Optional<String[]> uris = Utility.deserializeOrBadRequest(serialization, String[].class, req, res);
			if (!uris.isPresent()) return;
			GetDomainObject.Argument arg = new GetDomainObject.Argument(name.get(), uris.get(), "match".equals(req.getParameter("order")));
			Utility.execute(engine, req, res, serialization, GetDomainObject.class, arg);
		}
	}

	private void executeWithSpecification(
			Class<?> target,
			HttpServletRequest req,
			HttpServletResponse res,
			String name,
			Function<Specification, Object> buildArgument) throws IOException {
		String spec = req.getParameter("specification");
		Object arg;
		if (spec != null) {
			Optional<Class<?>> specType = model.find(name + '+' + spec);
			if (!specType.isPresent()) {
				specType = model.find(spec);
			}
			if (!specType.isPresent()) {
				res.sendError(400, "Couldn't find specification: " + spec);
				return;
			}
			try {
				Specification specification = (Specification) serialization.deserialize(specType.get(), req.getInputStream(), req.getContentType());
				arg = buildArgument.apply(specification);
			} catch (IOException e) {
				res.sendError(400, "Error deserializing specification. " + e.getMessage());
				return;
			}
		} else {
			arg = buildArgument.apply(null);
		}
		Utility.execute(engine, req, res, serialization, target, arg);
	}
}

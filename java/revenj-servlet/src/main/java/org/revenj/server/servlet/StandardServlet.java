package org.revenj.server.servlet;

import org.revenj.patterns.DomainModel;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;
import org.revenj.server.commands.*;
import org.revenj.server.commands.reporting.AnalyzeOlapCube;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class StandardServlet extends HttpServlet {

	private final DomainModel model;
	private final ProcessingEngine engine;
	private final WireSerialization serialization;

	public StandardServlet(
			DomainModel model,
			ProcessingEngine engine,
			WireSerialization serialization) {
		this.model = model;
		this.engine = engine;
		this.serialization = serialization;
	}

	StandardServlet(ServiceLocator locator) {
		this(locator.resolve(DomainModel.class), locator.resolve(ProcessingEngine.class), locator.resolve(WireSerialization.class));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/olap/")) {
			final Optional<Class<?>> manifest = Utility.findType(model, path, "/olap/", res);
			if (manifest.isPresent()) {
				String name = path.substring("/olap/".length(), path.length());
				String spec = req.getParameter("specification");
				Optional<Object> specification = Utility.specificationFromQuery(name, spec, model, req, res);
				if (spec != null && !specification.isPresent()) {
					return;
				}
				Utility.OlapInfo olapInfo = new Utility.OlapInfo(req);
				AnalyzeOlapCube.Argument<Object> arg =
						new AnalyzeOlapCube.Argument<>(
								name,
								spec,
								specification.orElse(null),
								olapInfo.dimensions,
								olapInfo.facts,
								olapInfo.order,
								olapInfo.limit,
								olapInfo.offset);
				Utility.execute(engine, req, res, serialization, AnalyzeOlapCube.class, arg);
			}
		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/persist/")) {
			final Optional<Class<?>> manifest = Utility.findType(model, path, "/persist/", res);
			if (manifest.isPresent()) {
				String name = path.substring("/persist/".length(), path.length());
				Optional<ArrayList> insert = Utility.deserializeOrBadRequest(serialization, req, res, ArrayList.class, manifest.get());
				if (insert.isPresent()) return;
				PersistAggregateRoot.Argument<Object> arg = new PersistAggregateRoot.Argument<>(name, insert.get(), null, null);
				Utility.execute(engine, req, res, serialization, PersistAggregateRoot.class, arg);
			}
		} else if (path.startsWith("/execute/")) {
			String name = path.substring("/execute/".length(), path.length()).replace('+', '$');
			String argument = Utility.readString(req.getInputStream(), req.getCharacterEncoding());
			ExecuteService.Argument<String> arg = new ExecuteService.Argument<>(name, argument);
			ByteArrayOutputStream os = serialization.serialize(arg, "application/json");
			ServerCommandDescription[] scd = new ServerCommandDescription[]{
					new ServerCommandDescription<>(null, ExecuteService.class, os.toString("UTF-8"))
			};
			ProcessingResult<Object> result = engine.execute(String.class, Object.class, scd, Utility.toPrincipal(req));
			Utility.returnResponse(req, res, serialization, result);
		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/persist/")) {
			final Optional<Class<?>> manifest = Utility.findType(model, path, "/persist/", res);
			if (manifest.isPresent()) {
				String name = path.substring("/persist/".length(), path.length());
				Optional<ArrayList> input = Utility.deserializeOrBadRequest(serialization, req, res, ArrayList.class, manifest.get());
				if (input.isPresent()) return;
				ArrayList<PersistAggregateRoot.Pair> toUpdate = new ArrayList<>(input.get().size());
				for (Object it : input.get()) {
					toUpdate.add(new PersistAggregateRoot.Pair<>(null, it));
				}
				PersistAggregateRoot.Argument<Object> arg = new PersistAggregateRoot.Argument<>(name, null, toUpdate, null);
				Utility.execute(engine, req, res, serialization, PersistAggregateRoot.class, arg);
			}
		} else if (path.startsWith("/olap/")) {
			final Optional<Class<?>> manifest = Utility.findType(model, path, "/olap/", res);
			if (manifest.isPresent()) {
				String name = path.substring("/olap/".length(), path.length());
				String spec = req.getParameter("specification");
				Optional<Object> specification = Utility.specificationFromQuery(name, spec, model, req, res);
				if (spec != null && !specification.isPresent()) {
					return;
				}
				Utility.OlapInfo olapInfo = new Utility.OlapInfo(req);
				AnalyzeOlapCube.Argument<Object> arg =
						new AnalyzeOlapCube.Argument<>(
								name,
								spec,
								specification.orElse(null),
								olapInfo.dimensions,
								olapInfo.facts,
								olapInfo.order,
								olapInfo.limit,
								olapInfo.offset);
				Utility.execute(engine, req, res, serialization, AnalyzeOlapCube.class, arg);
			}
		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}
}

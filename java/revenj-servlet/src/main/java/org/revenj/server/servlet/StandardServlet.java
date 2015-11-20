package org.revenj.server.servlet;

import org.revenj.patterns.DomainModel;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;
import org.revenj.server.commands.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

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
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/persist/")) {
			final Optional<Class<?>> manifest = Utility.findType(model, path, "/persist/", res);
			if (manifest.isPresent()) {
				String name = path.substring("/persist/".length(), path.length());
				ArrayList insert = serialization.deserialize(req.getInputStream(), "application/json", ArrayList.class, manifest.get());
				PersistAggregateRoot.Argument<Object> arg = new PersistAggregateRoot.Argument<>(name, insert, null, null);
				Utility.executeJson(engine, req, res, PersistAggregateRoot.class, arg);
			}
		} else if (path.startsWith("/execute/")) {
			String name = path.substring("/execute/".length(), path.length());
			String argument = Utility.readString(req.getInputStream(), req.getCharacterEncoding());
			ExecuteService.Argument<String> arg = new ExecuteService.Argument<>(name, argument);
			ByteArrayOutputStream os = serialization.serialize(arg, "application/json");
			ServerCommandDescription[] scd = new ServerCommandDescription[]{
					new ServerCommandDescription<>(null, ExecuteService.class, os.toString("UTF-8"))
			};
			ProcessingResult<String> result = engine.execute(String.class, String.class, scd, Utility.toPrincipal(req));
			Utility.returnJSON(res, result);
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
				ArrayList input = serialization.deserialize(req.getInputStream(), "application/json", ArrayList.class, manifest.get());
				ArrayList<PersistAggregateRoot.Pair> toUpdate = new ArrayList<>(input.size());
				for (Object it : input) {
					toUpdate.add(new PersistAggregateRoot.Pair<>(null, it));
				}
				PersistAggregateRoot.Argument<Object> arg = new PersistAggregateRoot.Argument<>(name, null, toUpdate, null);
				Utility.executeJson(engine, req, res, PersistAggregateRoot.class, arg);
			}
		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}
}

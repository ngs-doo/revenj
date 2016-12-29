package org.revenj.server.servlet;

import org.revenj.patterns.DomainModel;
import org.revenj.patterns.Report;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.commands.reporting.PopulateReport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class ReportingServlet extends HttpServlet {

	private final DomainModel model;
	private final ProcessingEngine engine;
	private final WireSerialization serialization;

	public ReportingServlet(
			DomainModel model,
			ProcessingEngine engine,
			WireSerialization serialization) {
		this.model = model;
		this.engine = engine;
		this.serialization = serialization;
	}

	ReportingServlet(ServiceLocator locator) {
		this(locator.resolve(DomainModel.class), locator.resolve(ProcessingEngine.class), locator.resolve(WireSerialization.class));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/report/")) {
			String name = path.substring("/report/".length(), path.length());
			Optional<Class<?>> manifest = model.find(name);
			if (!manifest.isPresent()) {
				res.sendError(400, "Unknown report object: " + name);
				return;
			}
			final Optional<Object> report = Utility.objectFromQuery(manifest.get(), req, res);
			if (report.isPresent()) {
				PopulateReport.Argument<Object> arg = new PopulateReport.Argument<>(report.get(), name);
				Utility.execute(engine, req, res, serialization, PopulateReport.class, arg);
			}
		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.startsWith("/report/")) {
			final Optional<Class<?>> manifest = Utility.findType(model, path, "/report/", res);
			if (manifest.isPresent()) {
				String name = path.substring("/report/".length(), path.length());
				Optional<?> report = Utility.deserializeOrBadRequest(serialization, manifest.get(), req, res);
				if (!report.isPresent()) return;
				PopulateReport.Argument<Object> arg = new PopulateReport.Argument<>(report.get(), name);
				Utility.execute(engine, req, res, serialization, PopulateReport.class, arg);
			}
		} else {
			res.sendError(405, "Unknown URL path: " + path);
		}
	}
}

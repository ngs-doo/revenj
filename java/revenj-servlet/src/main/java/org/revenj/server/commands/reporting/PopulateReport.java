package org.revenj.server.commands.reporting;

import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.Serialization;
import org.revenj.server.CommandResult;
import org.revenj.server.ReadOnlyServerCommand;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

public class PopulateReport implements ReadOnlyServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;

	public PopulateReport(
			DomainModel domainModel,
			PermissionManager permissions) {
		this.domainModel = domainModel;
		this.permissions = permissions;
	}

	public static final class Argument<TFormat> {
		public TFormat Data;
		public String ReportName;

		public Argument(TFormat data, String reportName) {
			this.Data = data;
			this.ReportName = reportName;
		}

		@SuppressWarnings("unused")
		private Argument() {
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <TInput, TOutput> CommandResult<TOutput> execute(
			ServiceLocator locator,
			Serialization<TInput> input,
			Serialization<TOutput> output,
			TInput data,
			Principal principal) {
		Argument<TInput> arg;
		try {
			arg = input.deserialize(data, Argument.class, data.getClass());
		} catch (IOException e) {
			return CommandResult.badRequest(e.getMessage());
		}
		Optional<Class<?>> manifest = domainModel.find(arg.ReportName);
		if (!manifest.isPresent()) {
			return CommandResult.badRequest("Couldn't find report type: " + arg.ReportName);
		}
		if (!permissions.canAccess(manifest.get(), principal)) {
			return CommandResult.forbidden(arg.ReportName);
		}
		if (!Report.class.isAssignableFrom(manifest.get())) {
			return CommandResult.badRequest("Specified type is not a report: " + arg.ReportName);
		}
		final Report report;
		try {
			report = (Report)input.deserialize(arg.Data, manifest.get());
		} catch (IOException e) {
			return CommandResult.badRequest("Error deserializing report: " + arg.ReportName + ". Reason: " + e.getMessage());
		}
		final Object result = report.populate(locator);
		try {
			return CommandResult.success("Report populated", output.serialize(result));
		} catch (IOException e) {
			return new CommandResult<>(null, "Error serializing result.", 500);
		}
	}
}

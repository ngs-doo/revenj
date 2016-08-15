package org.revenj.server.commands.reporting;

import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.Serialization;
import org.revenj.server.CommandResult;
import org.revenj.server.ReadOnlyServerCommand;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnalyzeOlapCube implements ReadOnlyServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;

	public AnalyzeOlapCube(
			DomainModel domainModel,
			PermissionManager permissions) {
		this.domainModel = domainModel;
		this.permissions = permissions;
	}

	public static final class Argument<TFormat> {
		public String CubeName;
		public String SpecificationName;
		public TFormat Specification;
		public String[] Dimensions;
		public String[] Facts;
		public List<Map.Entry<String, Boolean>> Order;
		public Integer Limit;
		public Integer Offset;

		public Argument(
				String cubeName,
				String specificationName,
				TFormat specification,
				String[] dimensions,
				String[] facts,
				List<Map.Entry<String, Boolean>> order,
				Integer limit,
				Integer offset) {
			this.CubeName = cubeName;
			this.SpecificationName = specificationName;
			this.Specification = specification;
			this.Dimensions = dimensions;
			this.Facts = facts;
			this.Order = order;
			this.Limit = limit;
			this.Offset = offset;
		}

		@SuppressWarnings("unused")
		private Argument() {
		}
	}

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
		Optional<Class<?>> manifest = domainModel.find(arg.CubeName);
		if (!manifest.isPresent()) {
			return CommandResult.badRequest("Couldn't find cube type: " + arg.CubeName);
		}
		if (!OlapCubeQuery.class.isAssignableFrom(manifest.get())) {
			return CommandResult.badRequest("Specified type is not an olap cube: " + arg.CubeName);
		}
		if (!permissions.canAccess(manifest.get(), principal)) {
			return CommandResult.forbidden(arg.CubeName);
		}
		final Specification specification;
		if (arg.SpecificationName != null && arg.SpecificationName.length() > 0) {
			Optional<Class<?>> specType = domainModel.find(arg.CubeName + '+' + arg.SpecificationName);
			if (!specType.isPresent()) {
				specType = domainModel.find(arg.SpecificationName);
			}
			if (!specType.isPresent()) {
				return CommandResult.badRequest("Couldn't find specification: " + arg.SpecificationName);
			}
			try {
				specification = (Specification) input.deserialize((Type) specType.get(), arg.Specification);
			} catch (IOException e) {
				return CommandResult.badRequest("Error deserializing specification: " + arg.SpecificationName);
			}
		} else if (arg.Specification instanceof Specification) {
			specification = (Specification) arg.Specification;
		} else {
			specification = null;
		}
		OlapCubeQuery cube;
		try {
			cube = (OlapCubeQuery) locator.resolve((Type) manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving cube: " + arg.CubeName + ". Reason: " + e.getMessage());
		}
		List<String> dimensions = arg.Dimensions == null ? null : Arrays.asList(arg.Dimensions);
		List<String> facts = arg.Facts == null ? null : Arrays.asList(arg.Facts);
		List found = cube.analyze(dimensions, facts, arg.Order, specification, arg.Limit, arg.Offset);
		try {
			return CommandResult.success("Found " + found.size() + " items", output.serialize(found));
		} catch (IOException e) {
			return new CommandResult<>(null, "Error serializing result.", 500);
		}
	}
}

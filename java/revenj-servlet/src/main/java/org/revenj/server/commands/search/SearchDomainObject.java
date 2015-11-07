package org.revenj.server.commands.search;

import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;
import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SearchDomainObject implements ServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;

	public SearchDomainObject(
			DomainModel domainModel,
			PermissionManager permissions) {
		this.domainModel = domainModel;
		this.permissions = permissions;
	}

	public static final class Argument<TFormat> {
		public String Name;
		public String SpecificationName;
		public TFormat Specification;
		public Integer Offset;
		public Integer Limit;
		public Map<String, String> Order;

		public Argument(String name, String specificationName, TFormat specification, Integer offset, Integer limit, Map<String, String> order) {
			this.Name = name;
			this.SpecificationName = specificationName;
			this.Specification = specification;
			this.Offset = offset;
			this.Limit = limit;
			this.Order = order;
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
		Optional<Class<?>> manifest = domainModel.find(arg.Name);
		if (!manifest.isPresent()) {
			return CommandResult.badRequest("Unable to find specified domain object: " + arg.Name);
		}
		if (!permissions.canAccess(manifest.get(), principal)) {
			return CommandResult.forbidden(arg.Name);
		}
		final Specification specification;
		if (arg.SpecificationName != null && arg.SpecificationName.length() > 0) {
			Optional<Class<?>> specType = domainModel.find(arg.Name + "$" + arg.SpecificationName);
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
		SearchableRepository repository;
		try {
			repository = locator.resolve(SearchableRepository.class, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving repository for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		List<AggregateRoot> found = repository.search(specification, arg.Limit, arg.Offset);
		try {
			return CommandResult.success("Found " + found.size() + " items", output.serialize(found));
		} catch (IOException e) {
			return new CommandResult<>(null, "Error serializing result.", 500);
		}
	}
}

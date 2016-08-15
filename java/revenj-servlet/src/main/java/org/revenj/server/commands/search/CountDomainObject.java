package org.revenj.server.commands.search;

import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.server.CommandResult;
import org.revenj.server.ReadOnlyServerCommand;
import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Optional;

public class CountDomainObject implements ReadOnlyServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;

	public CountDomainObject(
			DomainModel domainModel,
			PermissionManager permissions) {
		this.domainModel = domainModel;
		this.permissions = permissions;
	}

	public static final class Argument<TFormat> {
		public String Name;
		public String SpecificationName;
		public TFormat Specification;

		public Argument(String name, String specificationName, TFormat specification) {
			this.Name = name;
			this.SpecificationName = specificationName;
			this.Specification = specification;
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
			Optional<Class<?>> specType = domainModel.find(arg.Name + '+' + arg.SpecificationName);
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
		long found = repository.count(specification);
		try {
			return CommandResult.success(Long.toString(found), output.serialize(found));
		} catch (IOException e) {
			return new CommandResult<>(null, "Error serializing result.", 500);
		}
	}
}

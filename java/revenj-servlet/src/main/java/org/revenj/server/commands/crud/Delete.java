package org.revenj.server.commands.crud;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import org.revenj.patterns.*;
import org.revenj.Utils;
import org.revenj.security.PermissionManager;
import org.revenj.server.ServerCommand;
import org.revenj.server.CommandResult;
import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Optional;

public final class Delete implements ServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;

	public Delete(
			DomainModel domainModel,
			PermissionManager permissions) {
		this.domainModel = domainModel;
		this.permissions = permissions;
	}

	@CompiledJson
	public static final class Argument {
		@JsonAttribute(name = "Name", alternativeNames = {"name"})
		public final String name;
		@JsonAttribute(name = "Uri", alternativeNames = {"uri"})
		public final String uri;

		public Argument(String name, String uri) {
			this.name = name;
			this.uri = uri;
		}
	}

	@Override
	public <TInput, TOutput> CommandResult<TOutput> execute(
			ServiceLocator locator,
			Serialization<TInput> input,
			Serialization<TOutput> output,
			TInput data,
			Principal principal) {
		Argument arg;
		try {
			Type genericType = Utils.makeGenericType(Argument.class, data.getClass());
			arg = (Argument) input.deserialize(genericType, data);
		} catch (IOException e) {
			return CommandResult.badRequest(e.getMessage());
		}
		Optional<Class<?>> manifest = domainModel.find(arg.name);
		if (!manifest.isPresent()) {
			return CommandResult.badRequest("Unable to find specified domain object: " + arg.name);
		}
		if (!AggregateRoot.class.isAssignableFrom(manifest.get())) {
			return CommandResult.badRequest("Specified type is not an aggregate root: " + arg.name);
		}
		if (!permissions.canAccess(manifest.get(), principal)) {
			return CommandResult.forbidden(arg.name);
		}
		PersistableRepository repository;
		try {
			repository = locator.resolve(PersistableRepository.class, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving repository for: " + arg.name + ". Reason: " + e.getMessage());
		}
		try {
			Optional<AggregateRoot> found = repository.find(arg.uri);
			if (!found.isPresent()) {
				return CommandResult.badRequest("Can't find " + arg.name + " with uri: " + arg.uri);
			}
			repository.delete(found.get());
			return CommandResult.success("Object deleted", output.serialize(found.get()));
		} catch (IOException e) {
			return CommandResult.badRequest(e.getMessage());
		}
	}
}

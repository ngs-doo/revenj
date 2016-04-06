package org.revenj.server.commands.crud;

import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;
import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

public final class Update implements ServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;

	public Update(
			DomainModel domainModel,
			PermissionManager permissions) {
		this.domainModel = domainModel;
		this.permissions = permissions;
	}

	public static final class Argument<TFormat> {
		public String Name;
		public String Uri;
		public TFormat Data;
		public Boolean ReturnInstance;

		public Argument(String name, String uri, TFormat data, Boolean returnInstance) {
			this.Name = name;
			this.Uri = uri;
			this.Data = data;
			this.ReturnInstance = returnInstance;
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
		if (arg.Uri == null) {
			return CommandResult.badRequest("Uri to update not specified.");
		} else if (arg.Data == null) {
			return CommandResult.badRequest("Data to update not specified.");
		}
		if (!AggregateRoot.class.isAssignableFrom(manifest.get())) {
			return CommandResult.badRequest("Specified type is not an aggregate root: " + arg.Name);
		}
		if (!permissions.canAccess(manifest.get(), principal)) {
			return CommandResult.forbidden(arg.Name);
		}
		AggregateRoot instance;
		try {
			instance = (AggregateRoot) input.deserialize(manifest.get(), arg.Data);
		} catch (IOException e) {
			return CommandResult.badRequest("Error deserializing provided input for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		PersistableRepository repository;
		try {
			repository = locator.resolve(PersistableRepository.class, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving repository for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		try {
			Optional<AggregateRoot> found = repository.find(arg.Uri);
			if (!found.isPresent()) {
				return CommandResult.badRequest("Can't find " + arg.Name + " with uri: " + arg.Uri);
			}
			repository.update(found.get(), instance);
			boolean returnInstance = arg.ReturnInstance == null || arg.ReturnInstance;
			return new CommandResult<>(output.serialize(returnInstance ? instance : instance.getURI()), "Object changed", 200);
		} catch (IOException e) {
			return CommandResult.badRequest(e.getMessage());
		}
	}
}

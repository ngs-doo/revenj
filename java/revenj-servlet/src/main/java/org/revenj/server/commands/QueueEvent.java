package org.revenj.server.commands;

import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.Serialization;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

public class QueueEvent implements ServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;
	private final DataContext dataContext;

	public QueueEvent(
			DomainModel domainModel,
			PermissionManager permissions,
			DataContext dataContext) {
		this.domainModel = domainModel;
		this.permissions = permissions;
		this.dataContext = dataContext;
	}

	public static final class Argument<TFormat> {
		public String Name;
		public TFormat Data;

		public Argument(String name, TFormat data) {
			this.Name = name;
			this.Data = data;
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
		if (arg.Data == null) {
			return CommandResult.badRequest("Data to queue not specified.");
		}
		if (!DomainEvent.class.isAssignableFrom(manifest.get())) {
			return CommandResult.badRequest("Specified type is not an domain event: " + arg.Name);
		}
		if (!permissions.canAccess(manifest.get(), principal)) {
			return CommandResult.forbidden(arg.Name);
		}
		DomainEvent instance;
		try {
			instance = (DomainEvent) input.deserialize(manifest.get(), arg.Data);
		} catch (IOException e) {
			return CommandResult.badRequest("Error deserializing provided input for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		dataContext.queue(instance);
		return new CommandResult<>(null, "Event queued", 202);
	}
}

package org.revenj.server.commands;

import org.revenj.patterns.*;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;

import java.io.IOException;
import java.util.Optional;

public class SubmitEvent implements ServerCommand {

	private final DomainModel domainModel;

	public SubmitEvent(DomainModel domainModel) {
		this.domainModel = domainModel;
	}

	public static final class Argument<TFormat> {
		public String Name;
		public TFormat Data;
		public Boolean ReturnInstance;

		public Argument(String name, TFormat data, Boolean returnInstance) {
			this.Name = name;
			this.Data = data;
			this.ReturnInstance = returnInstance;
		}

		@SuppressWarnings("unused")
		private Argument() {
		}
	}

	@Override
	public <TInput, TOutput> CommandResult<TOutput> execute(ServiceLocator locator, Serialization<TInput> input, Serialization<TOutput> output, TInput data) {
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
			return CommandResult.badRequest("Data to submit not specified.");
		}
		DomainEvent instance;
		try {
			instance = (DomainEvent) input.deserialize(manifest.get(), arg.Data);
		} catch (IOException e) {
			return CommandResult.badRequest("Error deserializing provided input for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		DomainEventStore store;
		try {
			store = locator.resolve(DomainEventStore.class, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving event store for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		String uri = store.submit(instance);
		return new CommandResult<>(Boolean.TRUE.equals(arg.ReturnInstance) ? output.serialize(store) : output.serialize(uri), "Event stored", 201);
	}
}

package org.revenj.server.commands.CRUD;

import org.revenj.patterns.DomainModel;
import org.revenj.patterns.PersistableRepository;
import org.revenj.patterns.Serialization;
import org.revenj.patterns.ServiceLocator;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;
import org.revenj.server.commands.Utility;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Optional;

public final class Update implements ServerCommand {

	private final DomainModel domainModel;

	public Update(DomainModel domainModel) {
		this.domainModel = domainModel;
	}

	public static final class Argument<TFormat> {
		public String Name;
		public String Uri;
		public TFormat Data;

		public Argument(String name, String uri, TFormat data) {
			this.Name = name;
			this.Uri = uri;
			this.Data = data;
		}

		private Argument() {
		}
	}

	@Override
	public <TInput, TOutput> CommandResult<TOutput> execute(ServiceLocator locator, Serialization<TInput> input, Serialization<TOutput> output, TInput data) {
		Argument arg;
		try {
			Type genericType = Utility.makeGenericType(Argument.class, data.getClass());
			arg = (Argument) input.deserialize(genericType, data);
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
		Object instance;
		try {
			instance = input.deserialize(manifest.get(), (TInput) arg.Data);
		} catch (IOException e) {
			return CommandResult.badRequest("Error deserializing provided input for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		PersistableRepository repository;
		try {
			repository = Utility.resolvePersistableRepository(locator, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving repository for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		try {
			Optional<Object> found = repository.find(arg.Uri);
			if (!found.isPresent()) {
				return CommandResult.badRequest("Can't find " + arg.Name + " with uri: " + arg.Uri);
			}
			repository.update(found.get(), instance);
			return new CommandResult<>(output.serialize(instance), "Object changed", 200);
		} catch (SQLException e) {
			return CommandResult.badRequest(e.getMessage());
		}
	}
}

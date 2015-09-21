package org.revenj.server.commands.crud;

import org.revenj.patterns.DomainModel;
import org.revenj.patterns.Repository;
import org.revenj.serialization.Serialization;
import org.revenj.patterns.ServiceLocator;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;

import java.io.IOException;
import java.util.Optional;

public final class Read implements ServerCommand {

	private final DomainModel domainModel;

	public Read(DomainModel domainModel) {
		this.domainModel = domainModel;
	}

	public static final class Argument {
		public String Name;
		public String Uri;

		public Argument(String name, String uri) {
			this.Name = name;
			this.Uri = uri;
		}

		@SuppressWarnings("unused")
		private Argument() {
		}
	}

	@Override
	public <TInput, TOutput> CommandResult<TOutput> execute(ServiceLocator locator, Serialization<TInput> input, Serialization<TOutput> output, TInput data) {
		Argument arg;
		try {
			arg = input.deserialize(data, Argument.class);
		} catch (IOException e) {
			return CommandResult.badRequest(e.getMessage());
		}
		Optional<Class<?>> manifest = domainModel.find(arg.Name);
		if (!manifest.isPresent()) {
			return CommandResult.badRequest("Unable to find specified domain object: " + arg.Name);
		}
		Repository repository;
		try {
			repository = locator.resolve(Repository.class, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving repository for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		Optional<Object> found = repository.find(arg.Uri);
		if (!found.isPresent()) {
			return CommandResult.success("Object not found", null);
		}
		return CommandResult.success("Object found", output.serialize(found.get()));
	}
}

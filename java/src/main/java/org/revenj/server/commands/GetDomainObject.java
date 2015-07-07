package org.revenj.server.commands;

import org.revenj.patterns.*;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class GetDomainObject implements ServerCommand {

	private final DomainModel domainModel;

	public GetDomainObject(DomainModel domainModel) {
		this.domainModel = domainModel;
	}

	public static final class Argument {
		public String Name;
		public String[] Uri;
		public boolean MatchOrder;

		public Argument(String name, String[] uri, boolean matchOrder) {
			this.Name = name;
			this.Uri = uri;
			this.MatchOrder = matchOrder;
		}

		@SuppressWarnings("unused")
		private Argument() {
		}
	}

	@Override
	public <TInput, TOutput> CommandResult<TOutput> execute(ServiceLocator locator, Serialization<TInput> input, Serialization<TOutput> output, TInput data) {
		Argument arg;
		try {
			arg = input.deserialize(Argument.class, data);
		} catch (IOException e) {
			return CommandResult.badRequest(e.getMessage());
		}
		Optional<Class<?>> manifest = domainModel.find(arg.Name);
		if (!manifest.isPresent()) {
			return CommandResult.badRequest("Unable to find specified domain object: " + arg.Name);
		}
		if (arg.Uri == null || arg.Uri.length == 0) {
			return CommandResult.badRequest("Uri not specified.");
		}
		Repository repository;
		try {
			repository = Utility.resolveRepository(locator, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving repository for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		List<AggregateRoot> found = repository.find(arg.Uri);
		return new CommandResult<>(output.serialize(found), "Found " + found.size() + " items", 200);
	}
}

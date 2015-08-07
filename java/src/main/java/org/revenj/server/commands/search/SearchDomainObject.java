package org.revenj.server.commands.search;

import org.revenj.Utils;
import org.revenj.patterns.*;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SearchDomainObject implements ServerCommand {

	private final DomainModel domainModel;

	public SearchDomainObject(DomainModel domainModel) {
		this.domainModel = domainModel;
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
		Optional<Specification> filter;
		if (arg.SpecificationName != null && arg.SpecificationName.length() > 0) {
			Optional<Class<?>> specType = domainModel.find(arg.Name + "$" + arg.SpecificationName);
			if (!specType.isPresent()) {
				specType = domainModel.find(arg.SpecificationName);
			}
			if (!specType.isPresent()) {
				return CommandResult.badRequest("Couldn't find specification: " + arg.SpecificationName);
			}
			try {
				Specification specification = (Specification) input.deserialize((Type) specType.get(), arg.Specification);
				filter = Optional.ofNullable(specification);
			} catch (IOException e) {
				return CommandResult.badRequest("Error deserializing specification: " + arg.SpecificationName);
			}
		} else if (arg.Specification instanceof Specification) {
			filter = Optional.ofNullable((Specification) arg.Specification);
		} else {
			filter = Optional.empty();
		}
		SearchableRepository repository;
		try {
			repository = locator.resolve(SearchableRepository.class, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving repository for: " + arg.Name + ". Reason: " + e.getMessage());
		}
		List<AggregateRoot> found = repository.search(filter, Optional.ofNullable(arg.Limit), Optional.ofNullable(arg.Offset));
		return CommandResult.success("Found " + found.size() + " items", output.serialize(found));
	}
}

package org.revenj.server.commands.search;

import org.revenj.patterns.*;
import org.revenj.database.postgres.jinq.JinqMetaModel;
import org.revenj.security.PermissionManager;
import org.revenj.server.CommandResult;
import org.revenj.server.ReadOnlyServerCommand;
import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SearchDomainObject implements ReadOnlyServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;
	private final JinqMetaModel jinqModel;

	public SearchDomainObject(
			DomainModel domainModel,
			PermissionManager permissions,
			JinqMetaModel jinqModel) {
		this.domainModel = domainModel;
		this.permissions = permissions;
		this.jinqModel = jinqModel;
	}

	public static final class Argument<TFormat> {
		public String Name;
		public String SpecificationName;
		public TFormat Specification;
		public Integer Offset;
		public Integer Limit;
		public List<Map.Entry<String, Boolean>> Order;

		public Argument(String name, String specificationName, TFormat specification, Integer offset, Integer limit, List<Map.Entry<String, Boolean>> order) {
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
		if (!DataSource.class.isAssignableFrom(manifest.get())) {
			return CommandResult.badRequest("Specified type is not a data source: " + arg.Name);
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
		List<DataSource> found;
		if (arg.Order != null && !arg.Order.isEmpty()) {
			Query<DataSource> query = repository.query(specification);
			for (Map.Entry<String, Boolean> o : arg.Order) {
				Method method;
				try {
					method = manifest.get().getMethod("get" + o.getKey().substring(0, 1).toUpperCase() + o.getKey().substring(1));
				} catch (NoSuchMethodException e) {
					return CommandResult.badRequest("Unable to find getter method for: " + o.getKey());
				}
				if (o.getValue()) {
					query = query.sortedBy(jinqModel.findGetter(method));
				} else {
					query = query.sortedDescendingBy(jinqModel.findGetter(method));
				}
			}
			if (arg.Offset != null) {
				query = query.skip(arg.Offset);
			}
			if (arg.Limit != null) {
				query = query.limit(arg.Limit);
			}
			try {
				found = query.list();
			} catch (IOException ex) {
				return CommandResult.badRequest(ex.getMessage());
			}
		} else {
			found = repository.search(specification, arg.Limit, arg.Offset);
		}
		try {
			return CommandResult.success("Found " + found.size() + " items", output.serialize(found));
		} catch (IOException e) {
			return new CommandResult<>(null, "Error serializing result.", 500);
		}
	}
}

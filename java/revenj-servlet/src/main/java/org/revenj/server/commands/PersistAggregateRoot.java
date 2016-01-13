package org.revenj.server.commands;

import org.revenj.Utils;
import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.Serialization;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

public class PersistAggregateRoot implements ServerCommand {

	private final DomainModel domainModel;
	private final PermissionManager permissions;

	public PersistAggregateRoot(
			DomainModel domainModel,
			PermissionManager permissions) {
		this.domainModel = domainModel;
		this.permissions = permissions;
	}

	public static final class Argument<TFormat> {
		public String RootName;
		public TFormat ToInsert;
		public TFormat ToUpdate;
		public TFormat ToDelete;

		public Argument(String rootName, TFormat toInsert, TFormat toUpdate, TFormat toDelete) {
			this.RootName = rootName;
			this.ToInsert = toInsert;
			this.ToUpdate = toUpdate;
			this.ToDelete = toDelete;
		}

		@SuppressWarnings("unused")
		private Argument() {
		}
	}

	public static class Pair<T> {
		public T Key;
		public T Value;

		public Pair(T key, T value) {
			this.Key = key;
			this.Value = value;
		}

		@SuppressWarnings("unused")
		private Pair() {
		}
	}

	@Override
	public <TInput, TOutput> CommandResult<TOutput> execute(
			ServiceLocator locator,
			Serialization<TInput> input,
			Serialization<TOutput> output,
			TInput data,
			Principal principal) {
		if (data == null) {
			return CommandResult.badRequest("Data to persist not specified.");
		}
		Argument<TInput> arg;
		try {
			arg = input.deserialize(data, Argument.class, data.getClass());
		} catch (IOException e) {
			return CommandResult.badRequest(e.getMessage());
		}
		Optional<Class<?>> manifest = domainModel.find(arg.RootName);
		if (!manifest.isPresent()) {
			return CommandResult.badRequest("Couldn't find root type: " + arg.RootName);
		}
		if (!AggregateRoot.class.isAssignableFrom(manifest.get())) {
			return CommandResult.badRequest("Specified type is not an aggregate root: " + arg.RootName);
		}
		if (!permissions.canAccess(manifest.get(), principal)) {
			return CommandResult.forbidden(arg.RootName);
		}
		if (arg.ToInsert == null && arg.ToUpdate == null && arg.ToDelete == null) {
			return CommandResult.badRequest("Data to persist not specified.");
		}
		List<AggregateRoot> insertData = null;
		List<Map.Entry<AggregateRoot, AggregateRoot>> updateData = null;
		List<AggregateRoot> deleteData = null;
		try {
			if (arg.ToInsert != null) {
				insertData = input.deserialize(arg.ToInsert, ArrayList.class, manifest.get());
			}
			if (arg.ToUpdate != null) {
				ArrayList<Pair> pairs = input.deserialize(arg.ToUpdate, ArrayList.class, Utils.makeGenericType(Pair.class, manifest.get()));
				if (pairs != null) {
					updateData = new ArrayList<>(pairs.size());
					for (Pair item : pairs) {
						if (item.Value != null) {
							updateData.add(new AbstractMap.SimpleEntry(item.Key, item.Value));
						}
					}
					if (updateData.size() != pairs.size()) {
						ArrayList<AggregateRoot> values = input.deserialize(arg.ToUpdate, ArrayList.class, manifest.get());
						if (values != null) {
							updateData = new ArrayList<>(values.size());
							for (AggregateRoot item : values) {
								updateData.add(new AbstractMap.SimpleEntry(null, item));
							}
						}
					}
				}
			}
			if (arg.ToDelete != null) {
				deleteData = input.deserialize(arg.ToDelete, ArrayList.class, manifest.get());
			}
		} catch (IOException e) {
			return CommandResult.badRequest("Error deserializing provided input for: " + arg.RootName + ". Reason: " + e.getMessage());
		}

		if ((insertData == null || insertData.size() == 0)
				&& (updateData == null || updateData.size() == 0)
				&& (deleteData == null || deleteData.size() == 0)) {
			return CommandResult.badRequest("Data not sent or deserialized unsuccessfully.");
		}

		PersistableRepository repository;
		try {
			repository = locator.resolve(PersistableRepository.class, manifest.get());
		} catch (ReflectiveOperationException e) {
			return CommandResult.badRequest("Error resolving repository for: " + arg.RootName + ". Reason: " + e.getMessage());
		}
		try {
			String[] uris = repository.persist(insertData, updateData, deleteData);
			return new CommandResult<>(output.serialize(uris), "Data persisted", 200);
		} catch (IOException ex) {
			return new CommandResult<>(null, ex.getMessage(), 409);
		}
	}
}

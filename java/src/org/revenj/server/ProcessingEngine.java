package org.revenj.server;

import org.revenj.extensibility.PluginLoader;
import org.revenj.patterns.Container;
import org.revenj.patterns.DomainModel;
import org.revenj.patterns.Generic;
import org.revenj.patterns.Serialization;
import org.revenj.serialization.JsonSerialization;
import org.revenj.serialization.PassThroughSerialization;
import org.revenj.server.commands.CRUD.Create;
import org.revenj.server.commands.CRUD.Delete;
import org.revenj.server.commands.CRUD.Read;
import org.revenj.server.commands.CRUD.Update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class ProcessingEngine {

	private final Container container;
	private final Map<Class<?>, ServerCommand> serverCommands = new HashMap<>();
	private final Map<Class<?>, Serialization> serializers = new HashMap<>();

	public ProcessingEngine(Container container) throws Exception {
		this(container,
				new Generic<Optional<PluginLoader>>() {
				}.resolve(container),
				new Generic<Optional<ClassLoader>>() {
				}.resolve(container));
	}

	public ProcessingEngine(
			Container container,
			Optional<PluginLoader> extensibility,
			Optional<ClassLoader> classLoader) throws Exception {
		this.container = container;
		if (extensibility.isPresent()) {
			for (ServerCommand com : extensibility.get().resolve(container, ServerCommand.class)) {
				serverCommands.put(com.getClass(), com);
			}
		} else {
			ServiceLoader<ServerCommand> plugins = classLoader.isPresent()
					? ServiceLoader.load(ServerCommand.class, classLoader.get())
					: ServiceLoader.load(ServerCommand.class);
			for (ServerCommand com : plugins) {
				serverCommands.put(com.getClass(), com);
			}
		}
		serializers.put(String.class, new JsonSerialization());
		serializers.put(Object.class, new PassThroughSerialization());
	}

	public <TInput, TOutput> ProcessingResult<TOutput> execute(
			Class<TInput> input,
			Class<TOutput> output,
			ServerCommandDescription<TInput>[] commandDescriptions) throws SQLException {
		long startProcessing = System.nanoTime();

		if (commandDescriptions == null || commandDescriptions.length == 0) {
			return ProcessingResult.badRequest("There are no commands to execute.", startProcessing);
		}

		Serialization<TInput> inputSerializer = serializers.get(input);
		Serialization<TOutput> outputSerializer = serializers.get(output);

		ArrayList<CommandResultDescription<TOutput>> executedCommands = new ArrayList<>(commandDescriptions.length);
		Connection connection = container.resolve(Connection.class);
		try {
			try (Container scope = container.createScope()) {
				scope.registerInstance(Connection.class, connection, false);
				connection.setAutoCommit(false);
				for (ServerCommandDescription<TInput> cd : commandDescriptions) {
					long startCommand = System.nanoTime();
					ServerCommand command = serverCommands.get(cd.commandClass);
					if (command == null) {
						throw new RuntimeException("Command not registered: " + cd.commandClass);
					}
					CommandResult<TOutput> result = command.execute(scope, inputSerializer, outputSerializer, cd.data);
					if (result == null) {
						throw new RuntimeException("Result returned null for: " + cd.commandClass);
					}
					executedCommands.add(CommandResultDescription.create(cd.requestID, result, startCommand));
					if (result.status >= 400) {
						connection.rollback();
						return new ProcessingResult<>(result.message, result.status, executedCommands, startProcessing);
					}
				}
				connection.commit();
				connection.setAutoCommit(true);
				return ProcessingResult.success(executedCommands, startProcessing);
			}
		} catch (Exception e) {
			connection.rollback();
			connection.setAutoCommit(true);
			return new ProcessingResult<>(e.getMessage(), 500, executedCommands, startProcessing);
		}
	}
}

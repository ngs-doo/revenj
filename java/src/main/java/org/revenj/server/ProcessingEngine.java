package org.revenj.server;

import org.revenj.extensibility.PluginLoader;
import org.revenj.extensibility.Container;
import org.revenj.patterns.Serialization;
import org.revenj.patterns.WireSerialization;
import org.revenj.server.commands.CRUD.Create;
import org.revenj.server.commands.CRUD.Delete;
import org.revenj.server.commands.CRUD.Read;
import org.revenj.server.commands.CRUD.Update;
import org.revenj.server.commands.GetDomainObject;
import org.revenj.server.commands.SubmitEvent;
import org.revenj.server.commands.search.SearchDomainObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class ProcessingEngine {

	private final Container container;
	private final Map<Class<?>, ServerCommand> serverCommands = new HashMap<>();
	private final WireSerialization serialization;

	public ProcessingEngine(
			Container container,
			WireSerialization serialization,
			Optional<PluginLoader> extensibility,
			Optional<ClassLoader> classLoader) throws Exception {
		this.container = container;
		this.serialization = serialization;
		//TODO:...fix service loader in containers
		if (extensibility.isPresent()) {
			for (ServerCommand com : extensibility.get().resolve(container, ServerCommand.class)) {
				serverCommands.put(com.getClass(), com);
			}
			if(serverCommands.size() == 0) {
				//TODO: forced add just for now
				Container scope = container.createScope();
				scope.register(Create.class, Read.class, Update.class, Delete.class);
				scope.register(SearchDomainObject.class, GetDomainObject.class, SubmitEvent.class);
				serverCommands.put(Create.class, scope.resolve(Create.class));
				serverCommands.put(Read.class, scope.resolve(Read.class));
				serverCommands.put(Update.class, scope.resolve(Update.class));
				serverCommands.put(Delete.class, scope.resolve(Delete.class));
				serverCommands.put(SearchDomainObject.class, scope.resolve(SearchDomainObject.class));
				serverCommands.put(GetDomainObject.class, scope.resolve(GetDomainObject.class));
				serverCommands.put(SubmitEvent.class, scope.resolve(SubmitEvent.class));
				scope.close();
			}
		} else {
			ServiceLoader<ServerCommand> plugins = classLoader.isPresent()
					? ServiceLoader.load(ServerCommand.class, classLoader.get())
					: ServiceLoader.load(ServerCommand.class);
			for (ServerCommand com : plugins) {
				serverCommands.put(com.getClass(), com);
			}
		}
	}

	public CommandResult<String> executeJson(Class<?> command, Object argument) {
		try {
			ServerCommandDescription[] scd = new ServerCommandDescription[]{
					new ServerCommandDescription<>(null, command, argument)
			};
			ProcessingResult<String> result = execute(Object.class,String.class, scd);
			return result.executedCommandResults[0].result;
		} catch (SQLException e) {
			return new CommandResult(null, e.getMessage(), 409);
		} catch (Exception e) {
			return new CommandResult(null, e.getMessage(), 500);
		}
	}

	public CommandResult<Object> passThrough(Class<?> command, Object argument) {
		try {
			ServerCommandDescription[] scd = new ServerCommandDescription[]{
					new ServerCommandDescription<>(null, command, argument)
			};
			ProcessingResult<Object> result = execute(Object.class, Object.class, scd);
			return result.executedCommandResults[0].result;
		} catch (SQLException e) {
			return new CommandResult(null, e.getMessage(), 409);
		} catch (Exception e) {
			return new CommandResult(null, e.getMessage(), 500);
		}
	}

	public <TInput, TOutput> ProcessingResult<TOutput> execute(
			Class<TInput> input,
			Class<TOutput> output,
			ServerCommandDescription<TInput>[] commandDescriptions) throws SQLException {
		long startProcessing = System.nanoTime();

		if (commandDescriptions == null || commandDescriptions.length == 0) {
			return ProcessingResult.badRequest("There are no commands to execute.", startProcessing);
		}

		Serialization<TInput> inputSerializer = serialization.find(input).orElseGet(() -> {
			throw new RuntimeException("Invalid serialization format: " + input);
		});
		Serialization<TOutput> outputSerializer = serialization.find(output).orElseGet(() -> {
			throw new RuntimeException("Invalid serialization format: " + output);
		});

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

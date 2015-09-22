package org.revenj.server;

import org.revenj.extensibility.Container;
import org.revenj.extensibility.PluginLoader;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.Serialization;
import org.revenj.serialization.WireSerialization;

import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public final class ProcessingEngine {

	private final Container container;
	private final Map<Class<?>, ServerCommand> serverCommands = new HashMap<>();
	private final WireSerialization serialization;
	private final PermissionManager permissions;

	public ProcessingEngine(
			Container container,
			WireSerialization serialization,
			PermissionManager permissions,
			Optional<PluginLoader> extensibility) throws Exception {
		this(container,
				serialization,
				permissions,
				extensibility.isPresent() ? extensibility.get().resolve(container, ServerCommand.class) : new ServerCommand[0]);
	}

	public ProcessingEngine(
			Container container,
			WireSerialization serialization,
			PermissionManager permissions,
			ServerCommand[] commands) {
		this.container = container;
		this.serialization = serialization;
		this.permissions = permissions;
		for (ServerCommand com : commands) {
			serverCommands.put(com.getClass(), com);
		}
	}

	public CommandResult<String> executeJson(Class<?> command, Object argument, Principal principal) {
		try {
			ServerCommandDescription[] scd = new ServerCommandDescription[]{
					new ServerCommandDescription<>(null, command, argument)
			};
			ProcessingResult<String> result = execute(Object.class, String.class, scd, principal);
			return result.executedCommandResults[0].result;
		} catch (SQLException e) {
			return new CommandResult(null, e.getMessage(), 409);
		} catch (Exception e) {
			return new CommandResult(null, e.getMessage(), 500);
		}
	}

	public CommandResult<Object> passThrough(Class<?> command, Object argument, Principal principal) {
		try {
			ServerCommandDescription[] scd = new ServerCommandDescription[]{
					new ServerCommandDescription<>(null, command, argument)
			};
			ProcessingResult<Object> result = execute(Object.class, Object.class, scd, principal);
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
			ServerCommandDescription<TInput>[] commandDescriptions,
			Principal principal) throws SQLException {
		long startProcessing = System.nanoTime();

		PermissionManager.boundPrincipal.set(principal);

		if (commandDescriptions == null || commandDescriptions.length == 0) {
			return ProcessingResult.badRequest("There are no commands to execute.", startProcessing);
		}

		Serialization<TInput> inputSerializer = serialization.find(input).orElseGet(() -> {
			throw new RuntimeException("Invalid serialization format: " + input);
		});
		Serialization<TOutput> outputSerializer = serialization.find(output).orElseGet(() -> {
			throw new RuntimeException("Invalid serialization format: " + output);
		});

		for (ServerCommandDescription<TInput> cd : commandDescriptions) {
			if (!permissions.canAccess(cd.commandClass, principal)) {
				return new ProcessingResult<>(
						"You don't have permission to execute command: " + cd.commandClass,
						403,
						Collections.EMPTY_LIST,
						startProcessing);
			}
		}
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
					CommandResult<TOutput> result = command.execute(scope, inputSerializer, outputSerializer, cd.data, principal);
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
		} catch (IOException e) {
			connection.rollback();
			connection.setAutoCommit(true);
			if (e.getCause() instanceof SQLException) {
				return new ProcessingResult<>(e.getCause().getMessage(), 409, executedCommands, startProcessing);
			}
			return new ProcessingResult<>(e.getMessage(), 403, executedCommands, startProcessing);
		} catch (SecurityException e) {
			connection.rollback();
			connection.setAutoCommit(true);
			return new ProcessingResult<>(e.getMessage(), 403, executedCommands, startProcessing);
		} catch (Exception e) {
			connection.rollback();
			connection.setAutoCommit(true);
			return new ProcessingResult<>(e.getMessage(), 500, executedCommands, startProcessing);
		}
	}
}

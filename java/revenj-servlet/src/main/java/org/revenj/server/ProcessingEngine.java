package org.revenj.server;

import org.revenj.extensibility.Container;
import org.revenj.extensibility.PluginLoader;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.Serialization;
import org.revenj.serialization.WireSerialization;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public final class ProcessingEngine {

	private final Container container;
	private final DataSource dataSource;
	private final Map<Class<?>, ServerCommand> serverCommands = new HashMap<>();
	private final WireSerialization serialization;
	private final PermissionManager permissions;

	public ProcessingEngine(
			Container container,
			DataSource dataSource,
			WireSerialization serialization,
			PermissionManager permissions,
			Optional<PluginLoader> extensibility) throws Exception {
		this(container,
				dataSource,
				serialization,
				permissions,
				extensibility.isPresent() ? extensibility.get().resolve(container, ServerCommand.class) : new ServerCommand[0]);
	}

	ProcessingEngine(
			Container container,
			DataSource dataSource,
			WireSerialization serialization,
			PermissionManager permissions,
			ServerCommand[] commands) {
		this.container = container;
		this.dataSource = dataSource;
		this.serialization = serialization;
		this.permissions = permissions;
		for (ServerCommand com : commands) {
			serverCommands.put(com.getClass(), com);
		}
	}

	public Optional<Class<?>> findCommand(String name) {
		for (Class<?> command : serverCommands.keySet()) {
			if (command.getName().equals(name) || command.getSimpleName().equals(name)) {
				return Optional.of(command);
			}
		}
		return Optional.empty();
	}

	public <TInput, TOutput> ProcessingResult<TOutput> execute(
			Class<TInput> input,
			Class<TOutput> output,
			ServerCommandDescription<TInput>[] commandDescriptions,
			Principal principal) {
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

		boolean withTransaction = false;
		try {
			for (ServerCommandDescription<TInput> cd : commandDescriptions) {
				if (!permissions.canAccess(cd.commandClass, principal)) {
					return new ProcessingResult<>(
							"You don't have permission to execute command: " + cd.commandClass,
							403,
							Collections.EMPTY_LIST,
							startProcessing);
				}
				if (!ReadOnlyServerCommand.class.isAssignableFrom(cd.commandClass)) {
					withTransaction = true;
				}
			}
		} catch (SecurityException se) {
			return new ProcessingResult<>(
					se.getMessage(),
					403,
					Collections.EMPTY_LIST,
					startProcessing);
		}
		ArrayList<CommandResultDescription<TOutput>> executedCommands = new ArrayList<>(commandDescriptions.length);
		Connection connection;
		try {
			connection = dataSource.getConnection();
		} catch (Exception e) {
			return new ProcessingResult<>("Unable to create database connection", 503, null, startProcessing);
		}
		try {
			try {
				try (Container scope = container.createScope()) {
					scope.registerInstance(Connection.class, connection, false);
					connection.setAutoCommit(!withTransaction);
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
							if (withTransaction) {
								connection.rollback();
							}
							return new ProcessingResult<>(result.message, result.status, null, startProcessing);
						}
					}
					if (withTransaction) {
						connection.commit();
					}
					return ProcessingResult.success(executedCommands, startProcessing);
				}
			} catch (IOException e) {
				if (withTransaction) {
					connection.rollback();
				}
				if (e.getCause() instanceof SQLException) {
					return new ProcessingResult<>(e.getCause().getMessage(), 409, null, startProcessing);
				}
				return new ProcessingResult<>(e.getMessage(), 500, null, startProcessing);
			} catch (SecurityException e) {
				if (withTransaction) {
					connection.rollback();
				}
				return new ProcessingResult<>(e.getMessage(), 403, null, startProcessing);
			} catch (Exception e) {
				if (withTransaction) {
					connection.rollback();
				}
				return ProcessingResult.error(e, startProcessing);
			} finally {
				if (withTransaction) {
					connection.setAutoCommit(true);
				}
				connection.close();
			}
		} catch (SQLException ex) {
			return ProcessingResult.error(ex, startProcessing);
		}
	}
}

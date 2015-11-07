package org.revenj.server.commands;

import org.revenj.patterns.DomainModel;
import org.revenj.patterns.SearchableRepository;
import org.revenj.patterns.ServiceLocator;
import org.revenj.patterns.Specification;
import org.revenj.security.PermissionManager;
import org.revenj.serialization.Serialization;
import org.revenj.server.CommandResult;
import org.revenj.server.ServerCommand;
import org.revenj.server.ServerService;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;

public class ExecuteService implements ServerCommand {

	private final ClassLoader loader;
	private final PermissionManager permissions;

	public ExecuteService(
			ClassLoader loader,
			PermissionManager permissions) {
		this.loader = loader;
		this.permissions = permissions;
	}

	public static final class Argument<TFormat> {
		public String Name;
		public TFormat Data;

		public Argument(String name, TFormat data) {
			this.Name = name;
			this.Data = data;
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
		Class<?> manifest;
		try {
			manifest = Class.forName(arg.Name, true, loader);
		} catch (ClassNotFoundException e) {
			return CommandResult.badRequest("Unable to find specified service: " + arg.Name);
		}
		if (!ServerService.class.isAssignableFrom(manifest)) {
			return CommandResult.badRequest("Object: " + arg.Name + " is not a valid service.");
		}
		Optional<Method> method =
				Arrays.stream(manifest.getDeclaredMethods())
						.filter(it -> "execute".equals(it.getName()) && it.getGenericParameterTypes().length == 1)
						.findFirst();
		if (!method.isPresent()) {
			return CommandResult.badRequest("Object: " + arg.Name + " is not a valid service.");
		}
		if (!permissions.canAccess(manifest, principal)) {
			return CommandResult.forbidden(arg.Name);
		}
		Class<?> argumentType = method.get().getParameterTypes()[0];
		Object argument;
		try {
			argument = input.deserialize(arg.Data, argumentType);
		} catch (IOException e) {
			return CommandResult.badRequest(e.getMessage());
		}
		ServerService service = null;
		try {
			service = (ServerService) locator.resolve((Type) manifest);
		} catch (ReflectiveOperationException e) {
			for (Constructor ctor : manifest.getConstructors()) {
				try {
					service = (ServerService) locator.create(ctor);
				} catch (ReflectiveOperationException ignore) {
				}
			}
			if (service == null) {
				return new CommandResult<>(null, "Unable to create an instance of: " + manifest, 500);
			}
		}
		Object result = service.execute(argument);
		try {
			return CommandResult.success("Service executed", output.serialize(result));
		} catch (IOException e) {
			return new CommandResult<>(null, "Error serializing result.", 500);
		}
	}
}

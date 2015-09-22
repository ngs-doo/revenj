package org.revenj.server;

import org.revenj.serialization.Serialization;
import org.revenj.patterns.ServiceLocator;

import java.security.Principal;

public interface ServerCommand {
	<TInput, TOutput> CommandResult<TOutput> execute(
			ServiceLocator locator,
			Serialization<TInput> input,
			Serialization<TOutput> output,
			TInput data,
			Principal principal);
}
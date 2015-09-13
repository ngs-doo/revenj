package org.revenj.server;

import org.revenj.patterns.Serialization;
import org.revenj.patterns.ServiceLocator;

public interface ServerCommand {
	<TInput, TOutput> CommandResult<TOutput> execute(
			ServiceLocator locator,
			Serialization<TInput> input,
			Serialization<TOutput> output,
			TInput data);
}
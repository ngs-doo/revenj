package org.revenj.server;

public interface ServerService<TInput, TOutput> {
	TOutput execute(TInput argument);
}

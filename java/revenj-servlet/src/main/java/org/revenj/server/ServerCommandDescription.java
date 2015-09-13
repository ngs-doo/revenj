package org.revenj.server;

public final class ServerCommandDescription<TFormat> {
	public final String requestID;
	public final Class<?> commandClass;
	public final TFormat data;

	public ServerCommandDescription(String requestID, Class<?> commandClass, TFormat data) {
		this.requestID = requestID;
		this.commandClass = commandClass;
		this.data = data;
	}
}


package org.revenj.server;

public final class CommandResult<TFormat> {
	public final TFormat data;
	public final String message;
	public final int status;

	public CommandResult(TFormat data, String message, int status) {
		this.data = data;
		this.message = message;
		this.status = status;
	}

	public static <TFormat> CommandResult<TFormat> badRequest(String message) {
		return new CommandResult<>(null, message, 400);
	}

	public static <TFormat> CommandResult<TFormat> forbidden(String name) {
		return new CommandResult<>(null, "You don't have permissions to access:" + name, 403);
	}

	public static <TFormat> CommandResult<TFormat> success(String message, TFormat value) {
		return new CommandResult<>(value, message, 200);
	}
}
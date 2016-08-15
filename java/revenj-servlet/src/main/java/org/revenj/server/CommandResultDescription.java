package org.revenj.server;

public final class CommandResultDescription<TFormat> {
	public final String requestID;
	public final CommandResult<TFormat> result;
	public final long duration;

	CommandResultDescription(String requestID, CommandResult<TFormat> result, long start) {
		this.requestID = requestID;
		this.result = result;
		duration = (System.nanoTime() - start) / 1_000;
	}

	public static <TFormat> CommandResultDescription<TFormat> create(String requestID, CommandResult<TFormat> result, long start) {
		return new CommandResultDescription<>(requestID, result, start);
	}
}

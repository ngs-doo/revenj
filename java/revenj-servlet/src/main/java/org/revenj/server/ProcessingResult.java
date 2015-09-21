package org.revenj.server;

import java.util.List;

public final class ProcessingResult<TFormat> {
	public final String message;
	public final int status;
	public final CommandResultDescription<TFormat>[] executedCommandResults;
	public final long duration;

	ProcessingResult(String message, int status, List<CommandResultDescription<TFormat>> executedCommandResults, long start) {
		this.message = message;
		this.status = status;
		this.executedCommandResults = executedCommandResults.toArray(new CommandResultDescription[executedCommandResults.size()]);
		this.duration = (start - System.nanoTime()) / 1000;
	}

	public static <T> ProcessingResult<T> badRequest(String message, long start) {
		return new ProcessingResult<>(message, 400, null, start);
	}

	public static <TOutput> ProcessingResult<TOutput> success(List<CommandResultDescription<TOutput>> executedCommands, long start) {
		return new ProcessingResult<>("Commands successfully executed", 200, executedCommands, start);
	}
}

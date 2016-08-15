package org.revenj.server;

import java.util.List;

public final class ProcessingResult<TFormat> {
	public final String message;
	public final int status;
	public final CommandResultDescription<TFormat>[] executedCommandResults;
	public final long duration;

	private static final CommandResultDescription[] EMPTY = new CommandResultDescription[0];

	ProcessingResult(String message, int status, List<CommandResultDescription<TFormat>> executedCommandResults, long start) {
		this.message = message;
		this.status = status;
		this.executedCommandResults = executedCommandResults != null
				? executedCommandResults.toArray(new CommandResultDescription[executedCommandResults.size()])
				: EMPTY;
		this.duration = (System.nanoTime() - start) / 1_000;
	}

	public static <T> ProcessingResult<T> badRequest(String message, long start) {
		return new ProcessingResult<>(message, 400, null, start);
	}

	public static <T> ProcessingResult<T> error(Exception ex, long start) {
		return new ProcessingResult<>(
				ex.getMessage() == null || ex.getMessage().length() == 0 ? ex.toString() : ex.getMessage(),
				500,
				null,
				start);
	}

	public static <TOutput> ProcessingResult<TOutput> success(List<CommandResultDescription<TOutput>> executedCommands, long start) {
		return new ProcessingResult<>("Commands successfully executed", 200, executedCommands, start);
	}
}

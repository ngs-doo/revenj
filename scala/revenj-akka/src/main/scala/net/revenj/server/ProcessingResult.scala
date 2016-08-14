package net.revenj.server

final class ProcessingResult[TFormat] (
  val message: String,
  val status: Int,
  val executedCommandResults: Seq[CommandResultDescription[TFormat]],
  val start: Long) {
  val duration = (start - System.nanoTime) / 1000
}

object ProcessingResult {
  def badRequest[T](message: String, start: Long) = new ProcessingResult[T](message, 400, Nil, start)

  def error[T](ex: Exception, start: Long) =
    new ProcessingResult[T](
      if (ex.getMessage == null || ex.getMessage.length == 0) ex.toString else ex.getMessage,
      500,
      Nil,
      start)

  def success[TOutput](executedCommands: Seq[CommandResultDescription[TOutput]], start: Long) =
    new ProcessingResult[TOutput]("Commands successfully executed", 200, executedCommands, start)
}
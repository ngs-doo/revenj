package net.revenj.server

case class ProcessingResult[TFormat] (
  message: String,
  status: Int,
  executedCommandResults: Seq[CommandResultDescription[TFormat]],
  start: Long) {
  val duration = (System.nanoTime - start) / 1000
}

object ProcessingResult {
  def badRequest[T](message: String, start: Long) = ProcessingResult[T](message, 400, Nil, start)

  def error[T](ex: Throwable, start: Long) =
    ProcessingResult[T](
      if (ex.getMessage == null || ex.getMessage.isEmpty) ex.toString else ex.getMessage,
      500,
      Nil,
      start)

  def error[T](message: String, start: Long, errorCode: Int = 500) = ProcessingResult[T](message, errorCode, Nil, start)

  def success[TOutput](executedCommands: Seq[CommandResultDescription[TOutput]], start: Long) =
    ProcessingResult[TOutput]("Commands successfully executed", 200, executedCommands, start)
}
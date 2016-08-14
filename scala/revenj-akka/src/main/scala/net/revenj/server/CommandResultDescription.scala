package net.revenj.server

final class CommandResultDescription[TFormat](
  val requestID: String,
  val result: CommandResult[TFormat],
  val start: Long) {
  val duration = (start - System.nanoTime) / 1000
}

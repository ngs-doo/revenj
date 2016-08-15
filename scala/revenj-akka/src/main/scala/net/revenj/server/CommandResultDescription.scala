package net.revenj.server

case class CommandResultDescription[TFormat](
  requestID: String,
  result: CommandResult[TFormat],
  start: Long) {
  val duration = (System.nanoTime - start) / 1000
}

package net.revenj.server

final class ServerCommandDescription[TFormat](
  val requestID: String,
  val commandClass: Class[_ <: ServerCommand],
  val data: TFormat)

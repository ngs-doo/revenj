package net.revenj.server

case class ServerCommandDescription[TFormat](
  requestID: String,
  commandClass: Class[_ <: ServerCommand],
  data: TFormat)

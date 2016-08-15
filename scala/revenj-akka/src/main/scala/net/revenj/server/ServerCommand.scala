package net.revenj.server

import net.revenj.patterns.ServiceLocator
import net.revenj.serialization.Serialization

import scala.concurrent.Future

trait ServerCommand {
  def execute[TInput, TOutput](
    locator: ServiceLocator,
    input: Serialization[TInput],
    output: Serialization[TOutput],
    data: TInput): Future[CommandResult[TOutput]]
}

trait ReadOnlyServerCommand extends ServerCommand

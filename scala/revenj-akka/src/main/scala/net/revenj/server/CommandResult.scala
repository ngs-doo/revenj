package net.revenj.server

import scala.concurrent.Future
import scala.util.Try

final class CommandResult[TFormat](val data: Option[TFormat], val message: String, val status: Int)

object CommandResult {
  def badRequest[TFormat](message: String): Future[CommandResult[TFormat]] = {
    Future.successful(new CommandResult[TFormat](None, message, 400))
  }

  def badRequest[TFormat](error: Try[Throwable]): Future[CommandResult[TFormat]] = {
    Future.successful(new CommandResult[TFormat](None, error.get.getMessage, 400))
  }

  def forbidden[TFormat](name: String): Future[CommandResult[TFormat]] = {
    Future.successful(new CommandResult[TFormat](None, "You don't have permissions to access:" + name, 403))
  }

  def success[TFormat](message: String, value: TFormat): Future[CommandResult[TFormat]] = {
    Future.successful(new CommandResult[TFormat](Some(value), message, 200))
  }
}

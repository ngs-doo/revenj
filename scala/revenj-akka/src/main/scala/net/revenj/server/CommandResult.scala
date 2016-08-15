package net.revenj.server

import scala.concurrent.Future
import scala.util.Try

case class CommandResult[TFormat](data: Option[TFormat], message: String, status: Int)

object CommandResult {
  def badRequest[TFormat](message: String): Future[CommandResult[TFormat]] = {
    Future.successful(CommandResult[TFormat](None, message, 400))
  }

  def error[TFormat](message: String): Future[CommandResult[TFormat]] = {
    Future.successful(CommandResult[TFormat](None, message, 500))
  }

  def badRequest[TFormat](error: Try[Throwable]): Future[CommandResult[TFormat]] = {
    Future.successful(CommandResult[TFormat](None, error.get.getMessage, 400))
  }

  def forbidden[TFormat](name: String): Future[CommandResult[TFormat]] = {
    Future.successful(CommandResult[TFormat](None, s"You don't have permissions to access: $name", 403))
  }

  def success[TFormat](message: String, value: TFormat): Future[CommandResult[TFormat]] = {
    Future.successful(CommandResult(Some(value), message, 200))
  }
}

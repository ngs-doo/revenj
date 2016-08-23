package net.revenj.server.commands

import net.revenj.patterns._
import net.revenj.serialization.Serialization
import net.revenj.server.commands.SubmitEvent.Argument
import net.revenj.server.{CommandResult, ServerCommand}

import scala.concurrent.Future

class SubmitEvent(domainModel: DomainModel) extends ServerCommand {

  override def execute[TInput, TOutput](
    locator: ServiceLocator,
    input: Serialization[TInput],
    output: Serialization[TOutput],
    data: TInput): Future[CommandResult[TOutput]] = {

    val arg = input.deserializeRuntime(data, classOf[Argument[TInput]], data.getClass)
    lazy val manifest = domainModel.find(arg.get.Name)
    if (!arg.isSuccess) {
      CommandResult.badRequest(arg.failed)
    } else if (manifest.isEmpty) {
      CommandResult.badRequest(s"Unable to find specified domain object: ${arg.get.Name}")
    } else if (arg.get.Data == null) {
      CommandResult.badRequest("Data to submit not specified")
    } else if (!classOf[DomainEvent].isAssignableFrom(manifest.get)) {
      CommandResult.badRequest(s"Specified type is not an domain event: ${arg.get.Name}")
    } else {
      val instance = input.deserializeRuntime[DomainEvent](arg.get.Data, manifest.get)
      if (!instance.isSuccess) {
        CommandResult.badRequest(s"Error deserializing provided input for: ${arg.get.Name}. Reason: ${instance.failed.get.getMessage}")
      } else {
        val tryStore = locator.resolve(classOf[DomainEventStore[DomainEvent]], manifest.get)
        if (!tryStore.isSuccess) {
          CommandResult.badRequest(s"Error resolving event store for: ${arg.get.Name}. Reason: ${tryStore.failed.get.getMessage}")
        } else {
          import scala.concurrent.ExecutionContext.Implicits.global
          tryStore.get.submit(instance.get).map { uri =>
            val returnInstance = arg.get.ReturnInstance.getOrElse(false)
            val response = output.serializeRuntime(if (returnInstance) instance.get else uri)
            if (response.isSuccess) {
              CommandResult[TOutput](Some(response.get), "Event stored", 201)
            } else {
              CommandResult[TOutput](None, response.failed.get.getMessage, 500)
            }
          }
        }
      }
    }
  }
}

object SubmitEvent {
  case class Argument[TFormat](
    Name: String,
    Data: TFormat,
    ReturnInstance: Option[Boolean]
  )
}
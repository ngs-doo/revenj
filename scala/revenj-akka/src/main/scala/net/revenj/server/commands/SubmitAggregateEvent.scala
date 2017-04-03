package net.revenj.server.commands

import net.revenj.patterns._
import net.revenj.serialization.Serialization
import net.revenj.server.commands.SubmitAggregateEvent.Argument
import net.revenj.server.{CommandResult, ServerCommand}

import scala.concurrent.Future

class SubmitAggregateEvent(domainModel: DomainModel) extends ServerCommand {

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
    } else if (!classOf[AggregateDomainEvent[_]].isAssignableFrom(manifest.get)) {
      CommandResult.badRequest(s"Specified type is not an aggregate domain event: ${arg.get.Name}")
    } else {
      val instance = input.deserializeRuntime[AggregateDomainEvent[AggregateRoot]](arg.get.Data, manifest.get)
      if (!instance.isSuccess) {
        CommandResult.badRequest(s"Error deserializing provided input for: ${arg.get.Name}. Reason: ${instance.failed.get.getMessage}")
      } else {
        val aggType = manifest.get.getDeclaringClass
        if (!classOf[AggregateRoot].isAssignableFrom(aggType)) {
          CommandResult.badRequest(s"Specified type is not an aggregate domain event: ${arg.get.Name}")
        } else {
          val tryRepo = locator.resolve(classOf[Repository[AggregateRoot]], aggType)
          if (!tryRepo.isSuccess) {
            CommandResult.badRequest(s"Error resolving repository for: ${aggType.getSimpleName}. Reason: ${tryRepo.failed.get.getMessage}")
          } else {
            val tryStore = locator.resolve(classOf[DomainEventStore[DomainEvent]], manifest.get)
            if (!tryStore.isSuccess) {
              CommandResult.badRequest(s"Error resolving event store for: ${arg.get.Name}. Reason: ${tryStore.failed.get.getMessage}")
            } else {
              import scala.concurrent.ExecutionContext.Implicits.global
              tryRepo.get.find(arg.get.Uri).flatMap {
                case Some(agg) =>
                  val event = instance.get
                  try {
                    event.apply(agg)
                    tryStore.get.submit(event).map { uri =>
                      val returnInstance = arg.get.ReturnInstance.getOrElse(true)
                      val response = output.serializeRuntime(if (returnInstance) agg else uri)
                      if (response.isSuccess) {
                        CommandResult[TOutput](Some(response.get), "Event stored", 201)
                      } else {
                        CommandResult[TOutput](None, response.failed.get.getMessage, 500)
                      }
                    }
                  } catch {
                    case e: Throwable =>
                      CommandResult.badRequest(e.getMessage)
                  }
                case _ =>
                  CommandResult.badRequest(s"Specified aggregate root not found: ${arg.get.Uri}")
              }
            }
          }
        }
      }
    }
  }
}

object SubmitAggregateEvent {

  case class Argument[TFormat](
    Name: String,
    Data: TFormat,
    Uri: String,
    ReturnInstance: Option[Boolean]
  )

}
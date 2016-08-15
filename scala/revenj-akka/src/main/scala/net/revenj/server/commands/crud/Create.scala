package net.revenj.server.commands.crud

import net.revenj.patterns.{AggregateRoot, DomainModel, PersistableRepository, ServiceLocator}
import net.revenj.serialization.Serialization
import net.revenj.server.commands.crud.Create.Argument
import net.revenj.server.{CommandResult, ServerCommand}

import scala.concurrent.Future

class Create(domainModel: DomainModel) extends ServerCommand {

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
      CommandResult.badRequest("Data to create not specified.")
    } else if (!classOf[AggregateRoot].isAssignableFrom(manifest.get)) {
      CommandResult.badRequest(s"Specified type is not an aggregate root: ${arg.get.Name}")
    } else {
      val instance = input.deserializeRuntime[AggregateRoot](arg.get.Data, manifest.get)
      if (!instance.isSuccess) {
        CommandResult.badRequest(s"Error deserializing provided input for: ${arg.get.Name}. Reason: ${instance.failed.get.getMessage}")
      } else {
        val tryRepository = locator.resolve(classOf[PersistableRepository[AggregateRoot]], manifest.get)
        if (!tryRepository.isSuccess) {
          CommandResult.badRequest(s"Error resolving repository for: ${arg.get.Name}. Reason: ${tryRepository.failed.get.getMessage}")
        } else {
          import scala.concurrent.ExecutionContext.Implicits.global
          tryRepository.get.insert(instance.get).map { uri =>
            val returnInstance = arg.get.ReturnInstance.getOrElse(true)
            val response = output.serializeRuntime(if (returnInstance) instance.get else uri)
            if (response.isSuccess) {
              CommandResult[TOutput](Some(response.get), "Object created", 201)
            } else {
              CommandResult[TOutput](None, response.failed.get.getMessage, 500)
            }
          }
        }
      }
    }
  }
}

object Create {

  case class Argument[TFormat](Name: String, Data: TFormat, ReturnInstance: Option[Boolean])

}
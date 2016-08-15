package net.revenj.server.commands.crud

import net.revenj.patterns._
import net.revenj.serialization.Serialization
import net.revenj.server.commands.crud.Delete.Argument
import net.revenj.server.{CommandResult, ServerCommand}

import scala.concurrent.Future

class Delete(domainModel: DomainModel) extends ServerCommand {

  override def execute[TInput, TOutput](
    locator: ServiceLocator,
    input: Serialization[TInput],
    output: Serialization[TOutput],
    data: TInput): Future[CommandResult[TOutput]] = {

    val arg = input.deserialize[Argument](data)
    lazy val manifest = domainModel.find(arg.get.Name)
    if (!arg.isSuccess) {
      CommandResult.badRequest(arg.failed)
    } else if (manifest.isEmpty) {
      CommandResult.badRequest(s"Unable to find specified domain object: ${arg.get.Name}")
    } else if (arg.get.Uri == null) {
      CommandResult.badRequest("Uri to find not specified.")
    } else if (!classOf[AggregateRoot].isAssignableFrom(manifest.get)) {
      CommandResult.badRequest(s"Specified type is not an aggregate root: ${arg.get.Name}")
    } else {
      val tryRepository = locator.resolve(classOf[PersistableRepository[AggregateRoot]], manifest.get)
      if (!tryRepository.isSuccess) {
        CommandResult.badRequest(s"Error resolving repository for: ${arg.get.Name}. Reason: ${tryRepository.failed.get.getMessage}")
      } else {
        import scala.concurrent.ExecutionContext.Implicits.global
        tryRepository.get.find(arg.get.Uri).flatMap {
          case Some(found) =>
            tryRepository.get.delete(found).map { _ =>
              val response = output.serializeRuntime(found)
              if (response.isSuccess) {
                CommandResult[TOutput](Some(response.get), "Object deleted", 200)
              } else {
                CommandResult[TOutput](None, response.failed.get.getMessage, 500)
              }
            }
          case _ =>
            CommandResult.badRequest(s"Can't find ${arg.get.Name} with uri: ${arg.get.Uri}")
        }
      }
    }
  }
}

object Delete {

  case class Argument(Name: String, Uri: String)

}
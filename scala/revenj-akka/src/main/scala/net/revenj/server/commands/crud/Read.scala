package net.revenj.server.commands.crud

import net.revenj.patterns._
import net.revenj.serialization.Serialization
import net.revenj.server.commands.crud.Read.Argument
import net.revenj.server.{CommandResult, ReadOnlyServerCommand}

import scala.concurrent.Future

class Read(domainModel: DomainModel) extends ReadOnlyServerCommand {

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
    } else if (!classOf[Identifiable].isAssignableFrom(manifest.get)) {
      CommandResult.badRequest(s"Specified type is not an identifiable: ${arg.get.Name}")
    } else {
      val tryRepository = locator.resolve(classOf[Repository[Identifiable]], manifest.get)
      if (!tryRepository.isSuccess) {
        CommandResult.badRequest(s"Error resolving repository for: ${arg.get.Name}. Reason: ${tryRepository.failed.get.getMessage}")
      } else {
        import scala.concurrent.ExecutionContext.Implicits.global
        tryRepository.get.find(arg.get.Uri).map {
          case Some(found) =>
            val response = output.serializeRuntime(found)
            if (response.isSuccess) {
              CommandResult[TOutput](Some(response.get), "Object found", 200)
            } else {
              CommandResult[TOutput](None, response.failed.get.getMessage, 500)
            }
          case _ =>
            CommandResult[TOutput](None, "Object not found", 404)
        }
      }
    }
  }
}
object Read {
  case class Argument(Name: String, Uri: String)
}
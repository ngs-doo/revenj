package net.revenj.server.commands.search

import net.revenj.patterns._
import net.revenj.serialization.Serialization
import net.revenj.server.commands.search.CountDomainObject.Argument
import net.revenj.server.{CommandResult, ReadOnlyServerCommand}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class CountDomainObject(domainModel: DomainModel) extends ReadOnlyServerCommand {

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
    } else if (!classOf[DataSource].isAssignableFrom(manifest.get)) {
      CommandResult.badRequest(s"Specified type is not a data source: ${arg.get.Name}")
    } else {
      val tryRepository = locator.resolve(classOf[SearchableRepository[DataSource]], manifest.get)
      if (!tryRepository.isSuccess) {
        CommandResult.badRequest(s"Error resolving repository for: ${arg.get.Name}. Reason: ${tryRepository.failed.get.getMessage}")
      } else {
        val trySpec: Try[Specification[DataSource]] =
          if (arg.get.Specification.isDefined && arg.get.SpecificationName.getOrElse("").nonEmpty) {
            val shortSpec = domainModel.find(arg.get.Name + "+" + arg.get.SpecificationName.get)
            lazy val fullSpec = domainModel.find(arg.get.SpecificationName.get)
            if (shortSpec.isDefined) input.deserializeRuntime[Specification[DataSource]](arg.get.Specification.get, shortSpec.get)
            else if (fullSpec.isDefined) input.deserializeRuntime[Specification[DataSource]](arg.get.Specification.get, fullSpec.get)
            else Failure(new IllegalArgumentException(s"Couldn't find specification: ${arg.get.SpecificationName.get}"))
          } else {
            Success(
              arg.get.Specification match {
                case Some(spec: Specification[DataSource] @unchecked) => spec
                case _ => null
              }
            )
          }
        if (trySpec.isFailure) {
          CommandResult.badRequest(trySpec.failed.get.getMessage)
        } else {
          import scala.concurrent.ExecutionContext.Implicits.global
          tryRepository.get.count(Option(trySpec.get)).map { found =>
            val response = output.serializeRuntime(found)
            if (response.isSuccess) {
              CommandResult[TOutput](Some(response.get), found.toString, 200)
            } else {
              CommandResult[TOutput](None, response.failed.get.getMessage, 500)
            }
          }
        }
      }
    }
  }
}

object CountDomainObject {

  case class Argument[TFormat](
    Name: String,
    SpecificationName: Option[String],
    Specification: Option[TFormat])

}
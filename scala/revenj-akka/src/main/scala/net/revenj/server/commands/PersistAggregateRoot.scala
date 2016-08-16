package net.revenj.server.commands

import net.revenj.patterns.{AggregateRoot, DomainModel, PersistableRepository, ServiceLocator}
import net.revenj.serialization.Serialization
import net.revenj.server.commands.PersistAggregateRoot.{Argument, RootPair}
import net.revenj.server.{CommandResult, ServerCommand}

import scala.concurrent.Future
import scala.util.Success

class PersistAggregateRoot(domainModel: DomainModel) extends ServerCommand {

  override def execute[TInput, TOutput](
    locator: ServiceLocator,
    input: Serialization[TInput],
    output: Serialization[TOutput],
    data: TInput): Future[CommandResult[TOutput]] = {

    val arg = input.deserializeRuntime(data, classOf[Argument[TInput]], data.getClass)
    lazy val manifest = domainModel.find(arg.get.RootName)
    if (!arg.isSuccess) {
      CommandResult.badRequest(arg.failed)
    } else if (manifest.isEmpty) {
      CommandResult.badRequest(s"Couldn't find root type: ${arg.get.RootName}")
    } else if (!classOf[AggregateRoot].isAssignableFrom(manifest.get)) {
      CommandResult.badRequest(s"Specified type is not an aggregate root: ${arg.get.RootName}")
    } else {
      val insertData =
        if (arg.get.ToInsert.isEmpty) Success(Nil)
        else input.deserializeRuntime(arg.get.ToInsert.get, classOf[Seq[AggregateRoot]], manifest.get)
      val updateData =
        if (arg.get.ToUpdate.isEmpty) Success(Nil)
        else input.deserializeRuntime(arg.get.ToUpdate.get, classOf[Seq[RootPair[AggregateRoot]]], classOf[RootPair[AggregateRoot]], manifest.get)
      val deleteData =
        if (arg.get.ToDelete.isEmpty) Success(Nil)
        else input.deserializeRuntime(arg.get.ToDelete.get, classOf[Seq[AggregateRoot]], manifest.get)
      if (insertData.isFailure || updateData.isFailure || deleteData.isFailure) {
        //TODO: reason
        CommandResult.badRequest(s"Error deserializing provided input for: ${arg.get.RootName}.")
      } else if (insertData.get.isEmpty && updateData.get.isEmpty && deleteData.get.isEmpty) {
        CommandResult.badRequest("Data not sent or deserialized unsuccessfully.")
      } else {
        val tryRepository = locator.resolve(classOf[PersistableRepository[AggregateRoot]], manifest.get)
        if (!tryRepository.isSuccess) {
          CommandResult.badRequest(s"Error resolving repository for: ${arg.get.RootName}. Reason: ${tryRepository.failed.get.getMessage}")
        } else {
          import scala.concurrent.ExecutionContext.Implicits.global
          tryRepository.get.persist(insertData.get, updateData.get.map(kv => (kv.Key, kv.Value)), deleteData.get).map { uris =>
            val response = output.serializeRuntime(uris)
            if (response.isSuccess) {
              CommandResult[TOutput](Some(response.get), "Data persisted", 201)
            } else {
              CommandResult[TOutput](None, response.failed.get.getMessage, 500)
            }
          }
        }
      }
    }
  }
}

object PersistAggregateRoot {

  case class Argument[TFormat](
    RootName: String,
    ToInsert: Option[TFormat],
    ToUpdate: Option[TFormat],
    ToDelete: Option[TFormat]
  )

  case class RootPair[T](Key: T, Value: T)

}
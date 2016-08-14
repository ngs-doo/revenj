package net.revenj.server.handlers

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.stream.Materializer
import net.revenj.patterns.DomainModel
import net.revenj.server._
import net.revenj.server.commands.Utils
import net.revenj.server.commands.crud.{Create, Read}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CrudHandler(
  ec: ExecutionContext,
  model: DomainModel,
  engine: ProcessingEngine,
  serialization: WireSerialization,
  implicit private val materializer: Materializer) extends FlowBinding {

  override def bind(requests: mutable.Map[Path#Head, HttpRequest => Future[HttpResponse]]): Unit = {

    requests.put(Uri.Path("Crud.svc").head, matchRequest)
  }

  private def getURI(uri: Uri): Either[String, HttpResponse] = {
    uri.query().get("uri") match {
      case Some(id) => Left(id)
      case _ => Right(Utils.badResponse("URI query param not provided"))
    }
  }

  private def matchRequest(req: HttpRequest): Future[HttpResponse] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    req match {
      case HttpRequest(GET, uri, _, _, _) =>
        Utils.findClass(uri, model) match {
          case Left(info) =>
            getURI(uri) match {
              case Left(id) =>
                execute(classOf[Read], Read.Argument(info.name, id))
              case Right(response) =>
                Future.successful(response)
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(POST, uri, _, entity, _) =>
        Utils.findClass(uri, model) match {
          case Left(info) =>
            Utils.getInstance(serialization, info.manifest, entity).flatMap { inst =>
              if (inst.isSuccess) {
                execute(classOf[Create], Create.Argument[Any](info.name, inst.get, None))
              } else {
                Future.successful(Utils.badResponse(inst.failed.get.getMessage))
              }
            }.recover {
              case ex: Throwable => Utils.badResponse(ex.getMessage)
            }
          case Right(response) =>
            Future.successful(response)
        }
      case _ =>
        Future.successful(Utils.badResponse("Invalid URL"))
    }
  }

  private def execute(commandType: Class[_ <: ServerCommand], argument: Any): Future[HttpResponse] = {
    val scd = Array[ServerCommandDescription[Any]](new ServerCommandDescription[Any]("", commandType, argument))
    import scala.concurrent.ExecutionContext.Implicits.global
    engine.execute[Any, Any](scd).map { result =>
      if (result.executedCommandResults.length == 1) {
        val command = result.executedCommandResults.head.result
        if (command.data.isDefined) {
          serialization.serialize(command.data.get, "application/json") match {
            case Success(os) =>
              HttpResponse(status = command.status, entity = HttpEntity(ContentTypes.`application/json`, os.toByteArray))
            case Failure(err) =>
              HttpResponse(status = 500, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, err.getMessage))
          }
        } else {
          HttpResponse(status = command.status, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, command.message))
        }
      } else {
        HttpResponse(status = result.status, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, result.message))
      }
    }.recover {
      case ex: Throwable =>
        HttpResponse(status = 500, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, ex.getMessage))
    }
  }
}

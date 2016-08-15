package net.revenj.server.handlers

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.Materializer
import net.revenj.patterns.DomainModel
import net.revenj.server._
import net.revenj.server.commands.Utils
import net.revenj.server.commands.crud._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class CrudHandler(
  ec: ExecutionContext,
  model: DomainModel,
  engine: ProcessingEngine,
  serialization: WireSerialization,
  implicit private val materializer: Materializer) extends RequestBinding {

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
        Utils.findClass(uri, model, 3) match {
          case Left(info) =>
            getURI(uri) match {
              case Left(id) =>
                Utils.executeJson(req, engine, serialization, classOf[Read], Read.Argument(info.name, id))
              case Right(response) =>
                Future.successful(response)
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(POST, uri, _, entity, _) =>
        Utils.findClass(uri, model, 3) match {
          case Left(info) =>
            Utils.getInstance(serialization, info.manifest, entity).flatMap { inst =>
              if (inst.isSuccess) {
                Utils.executeJson(req, engine, serialization, classOf[Create], Create.Argument[Any](info.name, inst.get, None))
              } else {
                Future.successful(Utils.badResponse(inst.failed.get.getMessage))
              }
            }.recover {
              case ex: Throwable => Utils.badResponse(ex.getMessage)
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(PUT, uri, _, entity, _) =>
        Utils.findClass(uri, model, 3) match {
          case Left(info) =>
            getURI(uri) match {
              case Left(id) =>
                Utils.getInstance(serialization, info.manifest, entity).flatMap { inst =>
                  if (inst.isSuccess) {
                    Utils.executeJson(req, engine, serialization, classOf[Update], Update.Argument[Any](info.name, id, inst.get, None))
                  } else {
                    Future.successful(Utils.badResponse(inst.failed.get.getMessage))
                  }
                }.recover {
                  case ex: Throwable => Utils.badResponse(ex.getMessage)
                }
              case Right(response) =>
                Future.successful(response)
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(DELETE, uri, _, _, _) =>
        Utils.findClass(uri, model, 3) match {
          case Left(info) =>
            getURI(uri) match {
              case Left(id) =>
                Utils.executeJson(req, engine, serialization, classOf[Delete], Delete.Argument(info.name, id))
              case Right(response) =>
                Future.successful(response)
            }
          case Right(response) =>
            Future.successful(response)
        }
      case _ =>
        Future.successful(Utils.badResponse("Invalid URL"))
    }
  }
}

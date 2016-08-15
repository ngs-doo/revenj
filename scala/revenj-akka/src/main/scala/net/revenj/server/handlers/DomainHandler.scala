package net.revenj.server.handlers

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.Materializer
import net.revenj.patterns.DomainModel
import net.revenj.server._
import net.revenj.server.commands.Utils
import net.revenj.server.commands.search._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class DomainHandler(
  ec: ExecutionContext,
  model: DomainModel,
  engine: ProcessingEngine,
  serialization: WireSerialization,
  implicit private val materializer: Materializer) extends RequestBinding {

  override def bind(requests: mutable.Map[Path#Head, HttpRequest => Future[HttpResponse]]): Unit = {

    requests.put(Uri.Path("Domain.svc").head, matchRequest)
  }

  private def getURI(uri: Uri): Either[String, HttpResponse] = {
    uri.query().get("uri") match {
      case Some(id) => Left(id)
      case _ => Right(Utils.badResponse("URI query param not provided"))
    }
  }

  private val checkPath = Path("/Domain.svc/check/")
  private val findPath = Path("/Domain.svc/find/")

  private def matchRequest(req: HttpRequest): Future[HttpResponse] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    req match {
      case HttpRequest(GET, uri, _, _, _) if uri.path.startsWith(checkPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            getURI(uri) match {
              case Left(id) =>
                Utils.executeJson(req, engine, serialization, classOf[CheckDomainObject], CheckDomainObject.Argument(info.name, id))
              case Right(response) =>
                Future.successful(response)
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(PUT, uri, _, entity, _) if uri.path.startsWith(findPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            Utils.getInstance[Array[String]](serialization, entity).flatMap { inst =>
              if (inst.isSuccess) {
                Utils.executeJson(req, engine, serialization, classOf[GetDomainObject], GetDomainObject.Argument(info.name, inst.get, false))
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
}

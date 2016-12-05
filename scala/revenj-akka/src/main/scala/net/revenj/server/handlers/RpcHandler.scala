package net.revenj.server.handlers

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.Uri.Path
import akka.stream.Materializer
import akka.util.ByteString
import net.revenj.server.{ProcessingEngine, ServerCommandDescription, WireSerialization}
import net.revenj.server.commands.Utils

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class RpcHandler(
  ec: ExecutionContext,
  engine: ProcessingEngine,
  serialization: WireSerialization,
  implicit private val materializer: Materializer) extends RequestBinding {

  override def bind(requests: mutable.Map[Path#Head, HttpRequest => Future[HttpResponse]]): Unit = {

    requests.put(Uri.Path("RestApplication.svc").head, matchRequest)
  }

  private def executeRequest(req: HttpRequest, uri: Uri, withBody: Boolean): Future[HttpResponse] = {
    if (uri.path.length < 3) {
      Future.successful(Utils.badResponse("Command not specified"))
    } else {
      import scala.concurrent.ExecutionContext.Implicits.global
      val name = uri.path.tail.tail.tail.toString
      engine.findCommand(name) match {
        case Some(command) =>
          val argument =
            if (withBody) req.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map { bs =>
              new String(bs.toArray[Byte], "UTF-8")
            }
            else Future.successful(null)
          argument.flatMap { arg =>
            val scd = Array(ServerCommandDescription[String]("", command, arg))
            val result = engine.execute[String, Any](scd)
            Utils.returnResponse(serialization, result)
          }
        case _ =>
          Future.successful(Utils.badResponse(s"Unknown command: $name"))
      }
    }
  }

  private def matchRequest(req: HttpRequest): Future[HttpResponse] = {
    req match {
      case HttpRequest(GET, uri, _, _, _) => executeRequest(req, uri, false)
      case HttpRequest(POST, uri, _, entity, _) => executeRequest(req, uri, true)
      case HttpRequest(PUT, uri, _, entity, _) => executeRequest(req, uri, true)
      case _ =>
        Future.successful(Utils.badResponse("Invalid URL"))
    }
  }
}

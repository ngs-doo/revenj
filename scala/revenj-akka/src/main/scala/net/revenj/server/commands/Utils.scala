package net.revenj.server.commands

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CustomHeader
import akka.stream.Materializer
import akka.util.ByteString
import net.revenj.patterns.DomainModel
import net.revenj.server.{ProcessingEngine, ServerCommand, ServerCommandDescription, WireSerialization}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.reflect.runtime.universe.TypeTag

private[revenj] object Utils {
  def badResponse(message: String): HttpResponse = {
    HttpResponse(status = StatusCodes.BadRequest, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, message))
  }

  case class NameInfo(manifest: Class[_], name: String)

  def findClass(uri: Uri, model: DomainModel, skip: Int): Either[NameInfo, HttpResponse] = {
    var i = 0
    var path = uri.path
    while (i < skip) {
      path = path.tail
      i += 1
    }
    val name = path.toString()
    model.find(name) match {
      case Some(manifest) => Left(NameInfo(manifest, name))
      case _ => Right(badResponse(s"Unknown domain object: $name"))
    }
  }

  def getInstance(
    serialization: WireSerialization,
    manifest: Class[_],
    entity: RequestEntity)
    (implicit materializer: Materializer, ec: ExecutionContext): Future[Try[Any]] = {

    entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map { b =>
      serialization.deserialize(manifest, b.toArray[Byte], b.length, "application/json")
    }
  }

  def getInstance[T: TypeTag](
    serialization: WireSerialization,
    entity: RequestEntity)
    (implicit materializer: Materializer, ec: ExecutionContext): Future[Try[T]] = {

    entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map { b =>
      serialization.deserialize[T](b.toArray[Byte], b.length, "application/json")
    }
  }

  private case class DurationHeader(total: Long) extends CustomHeader {
    val name = "X-Duration"
    override def lowercaseName = "x-duration"
    val value = BigDecimal(total, 3).toString
    val renderInRequests = false
    val renderInResponses = true
  }

  def executeJson(
    request: HttpRequest,
    engine: ProcessingEngine,
    serialization: WireSerialization,
    commandType: Class[_ <: ServerCommand],
    argument: Any): Future[HttpResponse] = {

    val scd = Array[ServerCommandDescription[Any]](new ServerCommandDescription[Any]("", commandType, argument))
    import scala.concurrent.ExecutionContext.Implicits.global
    engine.execute[Any, Any](scd).map { result =>
      if (result.executedCommandResults.length == 1) {
        val command = result.executedCommandResults.head.result
        if (command.data.isDefined) {
          serialization.serialize(command.data.get, "application/json") match {
            case Success(os) =>
              HttpResponse(
                status = command.status,
                headers = immutable.Seq(DurationHeader(result.duration)),
                entity = HttpEntity(ContentTypes.`application/json`, os.toByteArray))
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
        HttpResponse(
          status = 500,
          entity = HttpEntity(
            ContentTypes.`text/plain(UTF-8)`,
            if (ex.getMessage == null) ex.toString else ex.getMessage))
    }
  }
}

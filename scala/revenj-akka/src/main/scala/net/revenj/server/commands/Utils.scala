package net.revenj.server.commands

import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.ByteString
import net.revenj.patterns.DomainModel
import net.revenj.server.WireSerialization

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

private[revenj] object Utils {
  def badResponse(message: String): HttpResponse = {
    HttpResponse(status = StatusCodes.BadRequest, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, message))
  }

  case class NameInfo(manifest: Class[_], name: String)

  def findClass(uri: Uri, model: DomainModel): Either[NameInfo, HttpResponse] = {
    val name = uri.path.tail.tail.tail.toString()
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
}

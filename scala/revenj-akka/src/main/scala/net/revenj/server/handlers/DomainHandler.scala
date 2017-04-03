package net.revenj.server.handlers

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.Materializer
import net.revenj.patterns.{AggregateRoot, DomainModel}
import net.revenj.server._
import net.revenj.server.commands.reporting.PopulateReport
import net.revenj.server.commands.{SubmitAggregateEvent, SubmitEvent, Utils}
import net.revenj.server.commands.search._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

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
  private val searchPath = Path("/Domain.svc/search/")
  private val countPath = Path("/Domain.svc/count/")
  private val existsPath = Path("/Domain.svc/exists/")
  private val submitPath = Path("/Domain.svc/submit/")
  private val reportPath = Path("/Domain.svc/report/")

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
      case HttpRequest(GET, uri, _, _, _) if uri.path.startsWith(searchPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            val limit = uri.query().get("limit").map(_.toInt)
            val offset = uri.query().get("offset").map(_.toInt)
            Utils.executeJson(req, engine, serialization, classOf[SearchDomainObject], SearchDomainObject.Argument[Any](info.name, None, None, offset, limit))
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(GET, uri, _, _, _) if uri.path.startsWith(countPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            Utils.executeJson(req, engine, serialization, classOf[CountDomainObject], CountDomainObject.Argument[Any](info.name, None, None))
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(GET, uri, _, _, _) if uri.path.startsWith(existsPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            Utils.executeJson(req, engine, serialization, classOf[DomainObjectExists], DomainObjectExists.Argument[Any](info.name, None, None))
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
      case HttpRequest(PUT, uri, _, entity, _) if uri.path.startsWith(searchPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            val limit = uri.query().get("limit").map(_.toInt)
            val offset = uri.query().get("offset").map(_.toInt)
            uri.query().get("specification") match {
              case Some(spec) =>
                val shortSpec = model.find(info.name + "+" + spec)
                lazy val fullSpec = model.find(spec)
                val specification =
                  if (shortSpec.isDefined) Utils.getInstance(serialization, shortSpec.get, entity)
                  else if (fullSpec.isDefined) Utils.getInstance(serialization, fullSpec.get, entity)
                  else Future.successful(Failure(new IllegalArgumentException(s"Unable to find specification type: $spec")))
                specification.flatMap { s =>
                  if (s.isSuccess) {
                    Utils.executeJson(req, engine, serialization, classOf[SearchDomainObject], SearchDomainObject.Argument[Any](info.name, Some(spec), Some(s.get), offset, limit))
                  } else {
                    Future.successful(Utils.badResponse(s.failed.get.getMessage))
                  }
                }
              case _ =>
                Future.successful(Utils.badResponse("specification query param not specified"))
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(PUT, uri, _, entity, _) if uri.path.startsWith(countPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            uri.query().get("specification") match {
              case Some(spec) =>
                val shortSpec = model.find(info.name + "+" + spec)
                lazy val fullSpec = model.find(spec)
                val specification =
                  if (shortSpec.isDefined) Utils.getInstance(serialization, shortSpec.get, entity)
                  else if (fullSpec.isDefined) Utils.getInstance(serialization, fullSpec.get, entity)
                  else Future.successful(Failure(new IllegalArgumentException(s"Unable to find specification type: $spec")))
                specification.flatMap { s =>
                  if (s.isSuccess) {
                    Utils.executeJson(req, engine, serialization, classOf[CountDomainObject], CountDomainObject.Argument[Any](info.name, Some(spec), Some(s.get)))
                  } else {
                    Future.successful(Utils.badResponse(s.failed.get.getMessage))
                  }
                }
              case _ =>
                Future.successful(Utils.badResponse("specification query param not specified"))
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(PUT, uri, _, entity, _) if uri.path.startsWith(existsPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            uri.query().get("specification") match {
              case Some(spec) =>
                val shortSpec = model.find(info.name + "+" + spec)
                lazy val fullSpec = model.find(spec)
                val specification =
                  if (shortSpec.isDefined) Utils.getInstance(serialization, shortSpec.get, entity)
                  else if (fullSpec.isDefined) Utils.getInstance(serialization, fullSpec.get, entity)
                  else Future.successful(Failure(new IllegalArgumentException(s"Unable to find specification type: $spec")))
                specification.flatMap { s =>
                  if (s.isSuccess) {
                    Utils.executeJson(req, engine, serialization, classOf[DomainObjectExists], DomainObjectExists.Argument[Any](info.name, Some(spec), Some(s.get)))
                  } else {
                    Future.successful(Utils.badResponse(s.failed.get.getMessage))
                  }
                }
              case _ =>
                Future.successful(Utils.badResponse("specification query param not specified"))
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(PUT, uri, _, entity, _) if uri.path.startsWith(reportPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            val report = Utils.getInstance(serialization, info.manifest, entity)
            report.flatMap { r =>
              if (r.isSuccess) {
                Utils.executeJson(req, engine, serialization, classOf[PopulateReport], PopulateReport.Argument[Any](info.name, r.get))
              } else {
                Future.successful(Utils.badResponse(r.failed.get.getMessage))
              }
            }
          case Right(response) =>
            Future.successful(response)
        }
      case HttpRequest(POST, uri, _, entity, _) if uri.path.startsWith(submitPath) =>
        Utils.findClass(uri, model, 5) match {
          case Left(info) =>
            if (classOf[AggregateRoot].isAssignableFrom(info.manifest)) {
              Utils.findName(uri, 7) match {
                case Left(evName) =>
                  getURI(uri) match {
                    case Left(id) =>
                      model.find(info.name + "$" + evName) match {
                        case Some(evClass) =>
                          Utils.getInstance(serialization, evClass, entity).flatMap { inst =>
                            if (inst.isSuccess) {
                              Utils.executeJson(req, engine, serialization, classOf[SubmitAggregateEvent], SubmitAggregateEvent.Argument(info.name + "+" + evName , inst.get, id, None))
                            } else {
                              Future.successful(Utils.badResponse(inst.failed.get.getMessage))
                            }
                          }.recover {
                            case ex: Throwable => Utils.badResponse(ex.getMessage)
                          }
                        case _ =>
                          Future.successful(Utils.badResponse(s"Unable to find aggregate domain event: $evName for aggregate root: ${info.name}"))
                      }
                    case Right(response) =>
                      Future.successful(response)
                  }
                case Right(response) =>
                  Future.successful(response)
              }
            } else {
              Utils.getInstance(serialization, info.manifest, entity).flatMap { inst =>
                if (inst.isSuccess) {
                  Utils.executeJson(req, engine, serialization, classOf[SubmitEvent], SubmitEvent.Argument(info.name, inst.get, None))
                } else {
                  Future.successful(Utils.badResponse(inst.failed.get.getMessage))
                }
              }.recover {
                case ex: Throwable => Utils.badResponse(ex.getMessage)
              }
            }
          case Right(response) =>
            Future.successful(response)
        }
      case _ =>
        Future.successful(Utils.badResponse("Invalid URL"))
    }
  }
}

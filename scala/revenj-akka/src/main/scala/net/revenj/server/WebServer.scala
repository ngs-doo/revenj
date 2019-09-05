package net.revenj.server

import java.util.Properties
import javax.sql.DataSource

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Materializer}
import akka.http.scaladsl.model.Uri.Path
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import net.revenj.Revenj
import net.revenj.extensibility.{Container, PluginLoader}
import net.revenj.server.handlers.RequestBinding

import scala.collection.mutable
import scala.concurrent.Future

object WebServer {
  private[this] def parseArgs(args: Array[String]): (String, Int) = {
    if (args.isEmpty) {
      ("localhost", 8080)
    } else {
      (args(0), args(1).toInt)
    }
  }

  def main(args: Array[String]): Unit = {
    val (address, port) = parseArgs(args)
    start(address, port)
  }

  def start(address: String, port: Int, dataSource: Option[DataSource] = None): Container = {
    val url = s"http://${address}:$port"

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val config = ConfigFactory.load()
    val props = new Properties()
    val iter = config.entrySet().iterator
    while (iter.hasNext) {
      val kv = iter.next()
      props.put(kv.getKey, kv.getValue.unwrapped())
    }
    val revenj =
      Revenj.setup(
        dataSource.getOrElse(Revenj.dataSource(props)),
        props,
        None,
        None,
        Some(executionContext)
      )
    revenj.registerInstance(config)
    revenj.registerInstance(system)
    revenj.registerInstance(materializer)
    revenj.registerInstance[Materializer](materializer)

    val plugins = revenj.resolve[PluginLoader]
    val bindings = plugins.resolve[RequestBinding](revenj)
    val flow = Flow[HttpRequest]
    val cpuCount = Runtime.getRuntime.availableProcessors
    val asyncBuilder = new mutable.HashMap[Path#Head, HttpRequest => Future[HttpResponse]]()
    bindings.foreach(_.bind(asyncBuilder))
    val asyncUrls = asyncBuilder.toMap
    val routes = flow.mapAsync(cpuCount / 2 + 1) {
      case req@HttpRequest(_, uri, _, _, _) if !uri.path.tail.isEmpty =>
        asyncUrls.get(uri.path.tail.head) match {
          case Some(handler) => handler(req)
          case _ =>
            Future.successful(
              HttpResponse(status = StatusCodes.BadRequest, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Unrecognized path"))
            )
        }
      case _ =>
        Future {
          HttpResponse(status = StatusCodes.BadRequest, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Invalid request"))
        }
    }

    val binding = Http().bindAndHandle(routes, address, port)
    println(s"Starting server at $url ...")
    binding foreach { bind =>
      println(s"Started server at $url")
    }
    revenj.registerInstance(new Shutdown(binding, system, url), handleClose = true)

    revenj
  }

  private class Shutdown(binding: Future[ServerBinding], system: ActorSystem, url: String) extends AutoCloseable {
    override def close(): Unit = {
      import scala.concurrent.ExecutionContext.Implicits.global
      binding foreach { bind =>
        println(s"Shutting down server at $url ...")
        bind.unbind() map { _ =>
          system.terminate()
          println(s"Shut down server at $url")
        }
      }
    }
  }
}

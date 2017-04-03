package net.revenj.server

import java.util.Properties

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Materializer}
import akka.http.scaladsl.model.Uri.Path
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import net.revenj.Revenj
import net.revenj.extensibility.PluginLoader
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
    val server = new WebServer(address, port)
    server.start()
    // server.shutdown()
  }
}

class WebServer(address: String, port: Int) {
  val url = s"http://${address}:$port"

  def start(): Unit = {
    require(shutdownPosibility.isEmpty, "Server has already been started!")
    shutdownPosibility = Some(() => ())

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
        Revenj.dataSource(props),
        props,
        None,
        None,
        Some(executionContext)
      )
    revenj.registerInstance(config)
    revenj.registerInstance(system)
    revenj.registerInstance(materializer)
    revenj.registerInstance[Materializer](materializer)
    revenj.registerAs[WireSerialization, RevenjSerialization](singleton = true)
    revenj.registerAs[ProcessingEngine, ProcessingEngine](singleton = true)

    val plugins = revenj.resolve[PluginLoader]
    val bindings = plugins.resolve[RequestBinding](revenj)
    val flow = Flow[HttpRequest]
    val cputCount = Runtime.getRuntime.availableProcessors
    val asyncBuilder = new mutable.HashMap[Path#Head, HttpRequest => Future[HttpResponse]]()
    bindings.foreach(_.bind(asyncBuilder))
    val asyncUrls = asyncBuilder.toMap
    val routes = flow.mapAsync(cputCount / 2 + 1) {
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

    val bindingFuture = Http().bindAndHandle(routes, address, port)
    println(s"Starting server at $url ...")
    bindingFuture foreach { bind =>
      println(s"Started server at $url")
      shutdownPosibility = Some { () =>
        println(s"Shutting down server at $url ...")
        bind.unbind() map { _ =>
          system.terminate()
          println(s"Shut down server at $url")
        }
      }
    }
  }

  private[this] var shutdownPosibility = Option.empty[() => Unit]
  def shutdown(): Unit = {
    require(shutdownPosibility.isDefined, "Server has not yet been started!")
    shutdownPosibility.get.apply()
    shutdownPosibility = None
  }
}

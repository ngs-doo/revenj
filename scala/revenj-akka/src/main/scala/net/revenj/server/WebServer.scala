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
import net.revenj.server.handlers.FlowBinding

import scala.collection.mutable
import scala.concurrent.Future
import scala.io.StdIn

object WebServer {
  def main(args: Array[String]): Unit = {
    setup("localhost", 8080)
  }

  def setup(address: String, port: Int): Unit = {

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
    val bindings = plugins.resolve(revenj, classOf[FlowBinding])
    val flow = Flow[HttpRequest]
    val cputCount = Runtime.getRuntime.availableProcessors
    val asyncBuilder = new mutable.HashMap[Path#Head, HttpRequest => Future[HttpResponse]]()
    bindings.foreach(_.bind(asyncBuilder))
    val asyncUrls = asyncBuilder.toMap
    val routes = flow.mapAsync(cputCount / 2 + 1) {
      case req@HttpRequest(_, uri, _, _, _) =>
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
    println(s"Server online at http://$address:$port/\nPress RETURN to stop...")
    StdIn.readLine()

    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
package net.revenj.server.handlers

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.collection.mutable
import scala.concurrent.Future

trait RequestBinding {
  def bind(requests: mutable.Map[Path#Head, HttpRequest => Future[HttpResponse]]): Unit
}

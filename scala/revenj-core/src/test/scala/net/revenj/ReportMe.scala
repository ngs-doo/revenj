package net.revenj

import net.revenj.ReportMe.Result
import net.revenj.patterns.{Report, ServiceLocator}

import scala.concurrent.Future

class ReportMe extends Report[ReportMe.Result] {
  override def populate(locator: ServiceLocator): Future[ReportMe.Result] = Future.successful(Result())
}
object ReportMe {
  case class Result()
}

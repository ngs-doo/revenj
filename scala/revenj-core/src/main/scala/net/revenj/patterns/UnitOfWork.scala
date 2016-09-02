package net.revenj.patterns

import java.io.Closeable

import scala.concurrent.Future

trait UnitOfWork extends DataContext with Closeable {

  def commit(): Future[Unit]

  def rollback(): Future[Unit]

}

package net.revenj.patterns

import java.io.Closeable

import scala.concurrent.duration.Duration
import scala.util.Try

trait UnitOfWork extends DataContext with Closeable {

  def commit(implicit duration: Duration): Try[Unit]

  def rollback(implicit duration: Duration): Try[Unit]

}

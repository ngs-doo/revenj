package net.revenj.serialization

import scala.collection.mutable
import scala.util.Try

object Queries {

  trait CommandQuery[T <: net.revenj.patterns.Command] {
    def from(input: Array[Byte], len: Int, contentType: String, arguments: Map[String, String]): Try[T]

    def from(input: java.io.InputStream, contentType: String, arguments: Map[String, String]): Try[T]

    def to(command: T, contentType: String, headers: mutable.Map[String, String], output: java.io.OutputStream): Try[_]
  }

  case class QueryInfo(commandName: String, query: CommandQuery[_ <: net.revenj.patterns.Command])
}

trait Queries {
  def find(name: String): Option[Queries.QueryInfo]
}
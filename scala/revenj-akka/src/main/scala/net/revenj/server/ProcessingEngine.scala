package net.revenj.server

import java.io.IOException
import java.sql.{Connection, SQLException}
import javax.sql.DataSource

import net.revenj.extensibility.{Container, PluginLoader}
import net.revenj.serialization.Serialization

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.reflect.runtime.universe._

class ProcessingEngine(
  container: Container,
  dataSource: DataSource,
  serialization: WireSerialization,
  commands: Array[ServerCommand]) {

  private val serverCommands: Map[Class[_], ServerCommand] = commands.map(c => c.getClass -> c).toMap

  def this(container: Container, dataSource: DataSource, serialization: WireSerialization, extensibility: Option[PluginLoader]) {
    this(container,
      dataSource,
      serialization,
      extensibility.map(_.resolve(container, classOf[ServerCommand])).getOrElse(Array.empty[ServerCommand]))
  }

  def findCommand(name: String): Option[Class[_]] = {
    var found: Option[Class[_]] = None
    val iter = serverCommands.keys.iterator
    while (found.isEmpty && iter.hasNext) {
      val command = iter.next()
      if (command.getName == name || command.getSimpleName == name) {
        found = Some(command)
      }
    }
    found
  }

  def execute[TInput: TypeTag, TOutput: TypeTag](
    commandDescriptions: Array[ServerCommandDescription[TInput]]): Future[ProcessingResult[TOutput]] = {

    val startProcessing = System.nanoTime
    if (commandDescriptions == null || commandDescriptions.length == 0) {
      Future.successful(ProcessingResult.badRequest("There are no commands to execute.", startProcessing))
    } else {
      (serialization.find[TInput](), serialization.find[TOutput]()) match {
        case (Some(input), Some(output)) =>
          val executed = new ArrayBuffer[CommandResultDescription[TOutput]](commandDescriptions.length)
          try {
            val connection = dataSource.getConnection
            val scope = container.createScope()
            scope.registerInstance(connection)
            connection.setAutoCommit(false)
            runCommands(scope, connection, startProcessing, commandDescriptions, input, output, executed, 0)
          } catch {
            case sql: SQLException =>
              Future.successful(ProcessingResult[TOutput]("Unable to create database connection", 503, null, startProcessing))
            case ex: Throwable =>
              Future.successful(ProcessingResult.error(ex, startProcessing))
          }
        case (None, _) =>
          Future.failed(new RuntimeException("Invalid serialization format: " + typeOf[TInput]))
        case _ =>
          Future.failed(new RuntimeException("Invalid serialization format: " + typeOf[TOutput]))
      }
    }
  }

  private def runCommands[TInput: TypeTag, TOutput: TypeTag](
    scope: Container,
    connection: java.sql.Connection,
    startProcessing: Long,
    commandDescriptions: Array[ServerCommandDescription[TInput]],
    input: Serialization[TInput],
    output: Serialization[TOutput],
    executed: ArrayBuffer[CommandResultDescription[TOutput]],
    index: Int): Future[ProcessingResult[TOutput]] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val startCommand = System.nanoTime()
    val cd = commandDescriptions(index)
    serverCommands.get(cd.commandClass) match {
      case Some(com) =>
        com.execute(scope, input, output, cd.data) flatMap { r =>
          executed += CommandResultDescription[TOutput](cd.requestID, r, startCommand)
          if (r.status >= 400) {
            connection.rollback()
            cleanup(scope, connection)
            Future.successful(ProcessingResult[TOutput](r.message, r.status, Nil, startProcessing))
          } else if (index + 1 < commandDescriptions.length) {
            runCommands(scope, connection, startProcessing, commandDescriptions, input, output, executed, index + 1)
          } else {
            connection.commit()
            cleanup(scope, connection)
            Future.successful(ProcessingResult.success(executed, startProcessing))
          }
        } recover {
          case e: IOException =>
            connection.rollback()
            cleanup(scope, connection)
            if (e.getCause.isInstanceOf[SQLException]) ProcessingResult.error(e.getCause.getMessage, startProcessing, 409)
            else ProcessingResult.error(e, startProcessing)
          case e: SecurityException =>
            connection.rollback()
            cleanup(scope, connection)
            ProcessingResult.error(e.getMessage, startProcessing, 403)
          case e: Exception =>
            connection.rollback()
            cleanup(scope, connection)
            ProcessingResult.error(e, startProcessing)
        }
      case _ =>
        connection.rollback()
        cleanup(scope, connection)
        Future.successful(ProcessingResult.error(s"Command not registered: ${cd.commandClass}", startProcessing))
    }
  }

  private def cleanup(scope: Container, connection: Connection): Unit = {
    connection.setAutoCommit(true)
    connection.close()
    scope.close()
  }
}

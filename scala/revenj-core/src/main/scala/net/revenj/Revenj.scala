package net.revenj

import java.io._
import java.lang.reflect.ParameterizedType
import java.net.{URL, URLClassLoader}
import java.sql.Connection
import java.util.{Properties, ServiceLoader}
import javax.sql.DataSource

import net.revenj.database.postgres.converters.JsonConverter
import net.revenj.extensibility.{Container, PluginLoader, SystemAspect, SystemState}
import net.revenj.patterns._
import net.revenj.serialization.{JacksonSerialization, Serialization}
import org.postgresql.ds.PGPoolingDataSource

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext
import scala.reflect.runtime.universe._

object Revenj {

  def setup(): Container = {
    val properties = new Properties
    var revProps = new File("revenj.properties")
    if (revProps.exists && revProps.isFile) {
      properties.load(new FileReader(revProps))
    } else {
      val location = System.getProperty("revenj.properties")
      if (location != null) {
        revProps = new File(location)
        if (revProps.exists && revProps.isFile) {
          properties.load(new FileReader(revProps))
        } else {
          throw new IOException(s"Unable to find revenj.properties in alternative location. Searching in: ${revProps.getAbsolutePath}")
        }
      } else {
        throw new IOException(s"Unable to find revenj.properties. Searching in: ${revProps.getAbsolutePath}")
      }
    }
    setup(properties)
  }

  def setup(properties: Properties): Container = {
    val plugins = properties.getProperty("revenj.pluginsPath")
    val pluginsPath = {
      if (plugins != null) {
        val pp = new File(plugins)
        if (pp.isDirectory) Some(pp) else None
      } else None
    }
    setup(dataSource(properties), properties, pluginsPath, Option(Thread.currentThread.getContextClassLoader))
  }

  def dataSource(properties: Properties): DataSource = {
    val jdbcUrl = properties.getProperty("revenj.jdbcUrl")
    if (jdbcUrl == null) {
      throw new IOException("revenj.jdbcUrl is missing from Properties")
    }
    if (!jdbcUrl.startsWith("jdbc:postgresql:")) {
      throw new IOException(s"""Invalid revenj.jdbcUrl provided. Expecting: 'jdbc:postgresql:...'. Found: '$jdbcUrl'.
If you wish to use custom jdbc driver provide custom data source instead of using Postgres builtin data source.""")
    }
    val dataSource = new PGPoolingDataSource
    dataSource.setUrl(jdbcUrl)
    val user = properties.getProperty("user")
    val revUser = properties.getProperty("revenj.user")
    if (revUser != null && revUser.length > 0) {
      dataSource.setUser(revUser)
    } else if (user != null && user.length > 0) {
      dataSource.setUser(user)
    }
    val password = properties.getProperty("password")
    val revPassword = properties.getProperty("revenj.password")
    if (revPassword != null && revPassword.length > 0) {
      dataSource.setPassword(revPassword)
    } else if (password != null && password.length > 0) {
      dataSource.setPassword(password)
    }
    dataSource
  }

  def setup(dataSource: DataSource, properties: Properties, pluginsPath: Option[File] = None, classLoader: Option[ClassLoader] = None, context: Option[ExecutionContext] = None): Container = {
    val loader = {
      if (pluginsPath.isDefined) {
        val jars = pluginsPath.get.listFiles(new FileFilter {
          override def accept(pathname: File): Boolean = {
            pathname.getPath.toLowerCase.endsWith(".jar")
          }
        })
        val urls = if (jars == null) Array.empty[URL] else jars.map(_.toURI.toURL)
        if (classLoader.isDefined) Some(new URLClassLoader(urls, classLoader.get))
        else Some(new URLClassLoader(urls))
      } else if (classLoader.isDefined) {
        classLoader
      } else {
        Option(Thread.currentThread.getContextClassLoader)
      }
    }
    val aspects = ServiceLoader.load(classOf[SystemAspect], loader.orNull).iterator()
    val buf = ArrayBuffer[SystemAspect]()
    while (aspects.hasNext) {
      buf += aspects.next()
    }
    setup(dataSource, properties, loader, context, buf)
  }

  private class SimpleDomainModel(loader: ClassLoader) extends DomainModel {
    private var namespaces = Array.empty[String]
    private val cache = new TrieMap[String, Class[_]]

    def setNamespace(value: String): Unit = {
      val parts = if (value == null) Array.empty[String] else value.split(",").distinct
      namespaces = parts map { ns =>
        if (ns.isEmpty) "" else ns + "."
      }
    }

    def find(name: String): Option[Class[_]] = {
      if (name == null) None
      else cache.get(name) match {
        case res@Some(_) => res
        case _ =>
          val className = if (name.indexOf('+') != -1) name.replace('+', '$') else name
          var found: Option[Class[_]] = None
          namespaces foreach { ns =>
            if (found.isEmpty) {
              try {
                val manifest = Class.forName(ns + className, true, loader)
                cache.put(name, manifest)
                found = Option(manifest)
              } catch {
                case _: Throwable =>
              }
            }
          }
          found
      }
    }
  }

  def container(resolveUnknown: Boolean, loader: ClassLoader): Container = {
    new SimpleContainer(resolveUnknown, loader)
  }

  def setup(
    dataSource: DataSource,
    properties: Properties,
    classLoader: Option[ClassLoader],
    context: Option[ExecutionContext],
    aspects: Seq[SystemAspect]): Container = {

    val state = new RevenjSystemState
    val loader = classLoader.getOrElse(Thread.currentThread.getContextClassLoader)
    val container = new SimpleContainer("true" == properties.getProperty("revenj.resolveUnknown"), loader)
    container.registerInstance(properties)
    container.registerInstance[SystemState](state, handleClose = false)
    container.registerInstance(context.getOrElse(ExecutionContext.global.prepare()))
    container.registerInstance[ServiceLocator](container, handleClose = false)
    container.registerInstance(dataSource, handleClose = false)
    container.registerInstance(loader, handleClose = false)
    container.register[JsonConverter](singleton = true)
    val plugins = new ServicesPluginLoader(loader)
    container.registerInstance[PluginLoader](plugins)
    val domainModel = new SimpleDomainModel(loader)
    container.registerInstance[DomainModel](domainModel, handleClose = false)
    val databaseNotification = new PostgresDatabaseNotification(dataSource, Some(domainModel), properties, state, container)
    container.registerInstance[EagerNotification](databaseNotification, handleClose = false)
    container.registerInstance[DataChangeNotification](databaseNotification, handleClose = true)
    ChangeNotification.registerContainer(container, databaseNotification)
    container.registerAs[JacksonSerialization, JacksonSerialization](singleton = true)
    container.registerAs[Serialization[String], JacksonSerialization](singleton = true)
    container.registerFactory[DataContext](c => LocatorDataContext.asDataContext(c, loader), singleton = false)
    container.registerFactory[UnitOfWork](c => LocatorDataContext.asUnitOfWork(c, loader), singleton = false)
    container.registerFactory[Function1[Connection, DataContext]](c => conn => LocatorDataContext.asDataContext(conn, c, loader), singleton = false)
    aspects foreach { a => a.configure(container) }
    domainModel.setNamespace(properties.getProperty("revenj.namespace"))
    properties.setProperty("revenj.aspectsCount", Integer.toString(aspects.size))
    state.started(container)
    container
  }

  def registerEvents[T <: DomainEvent : TypeTag](container: Container, plugins: PluginLoader, loader: ClassLoader): Unit = {
    val javaClass = Utils.findType(typeOf[T], runtimeMirror(loader)) match {
      case Some(p) =>
        p match {
          case cl: Class[_] => cl
          case _ => throw new IllegalArgumentException(s"Only non-generic types supported. Found: ${typeOf[T]}")
        }
      case p => throw new IllegalArgumentException(s"Unable to detect type: ${typeOf[T]}")
    }
    def processHandlers[X: TypeTag](gt: ParameterizedType, eventHandlers: Seq[Class[DomainEventHandler[X]]]): Unit = {
      for (h <- eventHandlers) {
        container.registerType(h, h, singleton = false)
        container.registerType(gt, h, singleton = false)
      }
    }
    val arrInst = java.lang.reflect.Array.newInstance(javaClass, 0)
    val gt = Utils.makeGenericType(classOf[DomainEventHandler[_]], javaClass)
    val agt = Utils.makeGenericType(classOf[DomainEventHandler[_]], arrInst.getClass)
    val ft = Utils.makeGenericType(classOf[Function0[_]], javaClass)
    val fgt = Utils.makeGenericType(classOf[DomainEventHandler[_]], ft)
    val aft = Utils.makeGenericType(classOf[Function0[_]], arrInst.getClass)
    val afgt = Utils.makeGenericType(classOf[DomainEventHandler[_]], aft)
    val simpleHandlers = plugins.find[DomainEventHandler[T]]
    processHandlers(gt, simpleHandlers)
    val simpleArrayHandlers = plugins.find[DomainEventHandler[Array[T]]]
    processHandlers(agt, simpleArrayHandlers)
    val funcHandlers = plugins.find[DomainEventHandler[Function0[T]]]
    processHandlers(fgt, funcHandlers)
    val arrayFuncHandlers = plugins.find[DomainEventHandler[Function0[Array[T]]]]
    processHandlers(afgt, arrayFuncHandlers)
  }
}

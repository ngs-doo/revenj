package net.revenj

import java.io._
import java.lang.reflect.ParameterizedType
import java.net.{URL, URLClassLoader}
import java.sql.Connection
import java.util.{Properties, ServiceLoader}
import javax.sql.DataSource

import net.revenj.extensibility.{Container, PluginLoader, SystemAspect}
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
          throw new IOException("Unable to find revenj.properties in alternative location. Searching in: " + revProps.getAbsolutePath)
        }
      } else {
        throw new IOException("Unable to find revenj.properties. Searching in: " + revProps.getAbsolutePath)
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
        val urls = new ArrayBuffer[URL]
        jars foreach { j => urls += j.toURI.toURL  }
        if (classLoader.isDefined) Some(new URLClassLoader(urls.toArray, classLoader.get))
        else Some(new URLClassLoader(urls.toArray))
      } else if (classLoader.isDefined) {
        classLoader
      } else {
        Option(Thread.currentThread.getContextClassLoader)
      }
    }
    val aspects = ServiceLoader.load(classOf[SystemAspect], loader.orNull)
    setup(dataSource, properties, loader, context, aspects.iterator)
  }

  private class SimpleDomainModel(var namespace: String, loader: ClassLoader) extends DomainModel {
    namespace = if (namespace != null && namespace.length > 0) namespace + "." else ""
    private val cache = new TrieMap[String, Class[_]]

    def updateNamespace(value: String): Unit = {
      namespace = if (value != null && value.length > 0) value + "." else ""
    }

    def find(name: String): Option[Class[_]] = {
      if (name == null) None
      else cache.get(name) match {
        case res@Some(_) => res
        case _ =>
          try {
            val className = if (name.indexOf('+') != -1) name.replace('+', '$') else name
            val manifest = Class.forName(namespace + className, true, loader)
            cache.put(name, manifest)
            Option(manifest)
          } catch {
            case _: Throwable => None
          }
      }
    }
  }

  def setup(dataSource: DataSource, properties: Properties, classLoader: Option[ClassLoader], context: Option[ExecutionContext], aspects: java.util.Iterator[SystemAspect]): Container = {
    val loader = classLoader.getOrElse(Thread.currentThread.getContextClassLoader)
    val container = new SimpleContainer("true" == properties.getProperty("revenj.resolveUnknown"), loader)
    container.registerInstance(properties)
    container.registerInstance(context.getOrElse(ExecutionContext.global.prepare()))
    container.registerInstance[ServiceLocator](container, handleClose = false)
    container.registerInstance(dataSource, handleClose = false)
    container.registerInstance(loader, handleClose = false)
    val plugins = new ServicesPluginLoader(loader)
    container.registerInstance[PluginLoader](plugins)
    val ns = properties.getProperty("revenj.namespace")
    val domainModel = new SimpleDomainModel(ns, loader)
    container.registerInstance[DomainModel](domainModel, handleClose = false)
    container.registerAs[JacksonSerialization, JacksonSerialization](singleton = true)
    container.registerAs[Serialization[String], JacksonSerialization](singleton = true)
    container.registerFactory[DataContext](c => LocatorDataContext.asDataContext(c, loader), singleton = false)
    container.registerFactory[UnitOfWork](c => LocatorDataContext.asUnitOfWork(c, loader), singleton = false)
    container.registerFactory[Function1[Connection, DataContext]](c => conn => LocatorDataContext.asDataContext(conn, c, loader), singleton = false)
    var total = 0
    if (aspects != null) {
      while (aspects.hasNext) {
        aspects.next().configure(container)
        total += 1
      }
    }
    val nsAfter = properties.getProperty("revenj.namespace")
    if (ns != nsAfter) {
      domainModel.updateNamespace(nsAfter)
    }
    properties.setProperty("revenj.aspectsCount", Integer.toString(total))
    container
  }

  def registerEvents[T <: DomainEvent : TypeTag](container: Container, plugins: PluginLoader, loader: ClassLoader): Unit = {
    val javaClass = Utils.findType(typeOf[T], runtimeMirror(loader)) match {
      case Some(p) =>
        p match {
          case cl: Class[_] => cl
          case _ => throw new IllegalArgumentException("Only non-generic types supported. Found: " + typeOf[T])
        }
      case p => throw new IllegalArgumentException("Unable to detect type: " + typeOf[T])
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

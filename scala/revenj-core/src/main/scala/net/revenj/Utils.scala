package net.revenj

import java.lang.reflect.{GenericArrayType, ParameterizedType, Type => JavaType}
import java.net.InetAddress
import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue
import javax.xml.parsers.{DocumentBuilderFactory, SAXParser, SAXParserFactory}

import org.w3c.dom.Document
import org.xml.sax.InputSource

import scala.collection.concurrent.TrieMap
import scala.reflect.runtime.universe._
import scala.xml.TopScope
import scala.xml.parsing.NoBindingFactoryAdapter

object Utils {
  val MinLocalDate: LocalDate = LocalDate.of(1, 1, 1)
  val MinLocalDateTime: LocalDateTime = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0)
  val MinDateTime: OffsetDateTime = OffsetDateTime.of(MinLocalDateTime, ZoneOffset.UTC)
  val MinUuid: UUID = new UUID(0L, 0L)
  val Zero0: BigDecimal = BigDecimal(0).setScale(0)
  val Zero1: BigDecimal = BigDecimal(0).setScale(1)
  val Zero2: BigDecimal = BigDecimal(0).setScale(2)
  val Zero3: BigDecimal = BigDecimal(0).setScale(3)
  val Zero4: BigDecimal = BigDecimal(0).setScale(4)
  val Loopback: InetAddress = InetAddress.getLoopbackAddress

  private val typeCache = new TrieMap[Type, JavaType]
  private val genericsCache = new TrieMap[String, GenericType]

  private val documentBuilder = {
    val dbf = DocumentBuilderFactory.newInstance
    dbf.setValidating(false)
    dbf.setFeature("http://xml.org/sax/features/namespaces", false)
    dbf.setFeature("http://xml.org/sax/features/validation", false)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    dbf.setNamespaceAware(false)
    dbf.newDocumentBuilder
  }
  private val documentParsers = {
    val cpu = Runtime.getRuntime.availableProcessors
    val res = new ArrayBlockingQueue[SAXParser](cpu)
    0 until cpu foreach { _ => res.offer(initializeParser()) }
    res
  }

  private def initializeParser() = {
    val f = SAXParserFactory.newInstance()
    f.setValidating(false)
    f.setFeature("http://xml.org/sax/features/namespaces", false)
    f.setFeature("http://xml.org/sax/features/validation", false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    f.setNamespaceAware(false)
    f.newSAXParser()
  }

  def newDocument: Document = documentBuilder.newDocument

  def parse[T](source: InputSource): T = {
    val adapter = new NoBindingFactoryAdapter()
    val parser = Option(documentParsers.poll()) match {
      case Some(p) => p
      case _ => initializeParser()
    }

    try {
      adapter.scopeStack push TopScope
      parser.parse(source, adapter)
      adapter.scopeStack.pop()
    } finally {
      documentParsers.offer(parser)
    }

    adapter.rootElem.asInstanceOf[T]
  }

  private class GenericType(val name: String, val raw: JavaType, val arguments: Array[JavaType]) extends ParameterizedType {
    private val argObjects = arguments.map(_.asInstanceOf[AnyRef])

    override def hashCode: Int = {
      java.util.Arrays.hashCode(argObjects) ^ raw.hashCode
    }

    override def equals(other: Any): Boolean = {
      other match {
        case pt: ParameterizedType =>
          raw == pt.getRawType && java.util.Arrays.equals(argObjects, pt.getActualTypeArguments.map(_.asInstanceOf[AnyRef]))
        case _ =>
          false
      }
    }

    def getActualTypeArguments: Array[JavaType] = arguments

    def getRawType: JavaType = raw

    def getOwnerType: JavaType = null

    override def toString: String = name
  }

  private class GenArrType(genType: JavaType) extends GenericArrayType {
    lazy private val typeName = genType.getTypeName + "[]"
    override def getGenericComponentType: JavaType = genType
    override def getTypeName: String = typeName
    override def toString: String = typeName
  }

  private[revenj] def findType(tpe: Type, mirror: Mirror): Option[JavaType] = {
    typeCache.get(tpe) match {
      case found@Some(_) => found
      case _ =>
        tpe.dealias match {
          case TypeRef(_, sym, args) if args.isEmpty =>
            Some(mirror.runtimeClass(sym.asClass))
          case TypeRef(_, sym, args) if sym.fullName == "scala.Array" && args.size == 1 =>
            findType(args.head, mirror) match {
              case Some(typeArg) => Some(new GenArrType(typeArg))
              case _ => None
            }
          case TypeRef(_, sym, args) =>
            val symClass = mirror.runtimeClass(sym.asClass)
            val typeArgs = args.flatMap(it => findType(it, mirror))
            if (typeArgs.size == args.size) Some(Utils.makeGenericType(symClass, typeArgs))
            else None
          case _ =>
            None
        }
    }
  }

  private[revenj] def makeGenericType(container: Class[_], arguments: List[JavaType]): ParameterizedType = {
    val sb = new StringBuilder
    sb.append(container.getTypeName)
    sb.append("<")
    sb.append(arguments.head.getTypeName)
    for (arg <- arguments.tail) {
      sb.append(", ")
      sb.append(arg.getTypeName)
    }
    sb.append(">")
    val name = sb.toString
    genericsCache.getOrElseUpdate(name, {
      new GenericType(name, container, arguments.toArray)
    })
  }

  def makeGenericType(container: Class[_], argument: JavaType, arguments: JavaType*): ParameterizedType = {
    val sb = new StringBuilder
    sb.append(container.getTypeName)
    sb.append("<")
    sb.append(argument.getTypeName)
    for (arg <- arguments) {
      sb.append(", ")
      sb.append(arg.getTypeName)
    }
    sb.append(">")
    val name = sb.toString
    genericsCache.getOrElseUpdate(name, {
      val genericArgs = new Array[JavaType](arguments.length + 1)
      genericArgs(0) = argument
      var i = 0
      while (i < arguments.length) {
        genericArgs(i + 1) = arguments(i)
        i += 1
      }
      new GenericType(name, container, genericArgs)
    })
  }
}

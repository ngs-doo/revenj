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

  case class TypeCache(actual: JavaType, erased: JavaType)
  private val typeCache = new TrieMap[Type, TypeCache]
  private val genericsCache = new TrieMap[String, GenericType]

  private val sharedBuilder = ThreadLocal.withInitial[java.lang.StringBuilder](() => new java.lang.StringBuilder())

  Seq(
    (typeOf[Byte], classOf[Byte]),
    (typeOf[Boolean], classOf[Boolean]),
    (typeOf[Int], classOf[Int]),
    (typeOf[Long], classOf[Long]),
    (typeOf[Short], classOf[Short]),
    (typeOf[Float], classOf[Float]),
    (typeOf[Double], classOf[Double]),
    (typeOf[Char], classOf[Char])).foreach { case (t, c) =>
    typeCache.put(t, TypeCache(c, classOf[AnyRef]))
  }
  Seq(
    (typeOf[Nothing], classOf[AnyRef]),
    (typeOf[Any], classOf[AnyRef]),
    (typeOf[Option[Nothing]], classOf[Option[AnyRef]]),
    (typeOf[None.type], classOf[Option[AnyRef]])).foreach { case (t, c) =>
    typeCache.put(t, TypeCache(c, c))
  }

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

  def javaType[T : TypeTag](mirror: Mirror): JavaType = {
    val tpe = mirror.typeOf[T]
    findType(tpe, mirror).getOrElse(sys.error(s"Unable to find java version of type for $tpe"))
  }

  private[revenj] def findType(tpe: Type, mirror: Mirror): Option[JavaType] = {
    val ft = typeCache.get(tpe)
    if (ft.isDefined) Some(ft.get.actual)
    else {
      val actual = buildType(tpe, mirror, false, false)
      val erased = buildType(tpe, mirror, false, true)
      if (actual.isDefined && erased.isDefined) {
        val tc = TypeCache(actual.get, erased.get)
        typeCache.put(tpe, tc)
        Some(tc.actual)
      } else actual
    }
  }

  private[revenj] def findTypeInfo(tpe: Type, mirror: Mirror): TypeCache = {
    val ft = typeCache.get(tpe)
    if (ft.isDefined) ft.get
    else {
      val actual = buildType(tpe, mirror, false, false)
      val erased = buildType(tpe, mirror, false, true)
      if (actual.isEmpty || erased.isEmpty) throw new IllegalArgumentException(s"Unable to analyze $tpe")
      val tc = TypeCache(actual.get, erased.get)
      typeCache.put(tpe, tc)
      tc
    }
  }

  private[revenj] def buildType(tpe: Type, mirror: Mirror, inContainer: Boolean, erasedVersion: Boolean): Option[JavaType] = {
    val ft = typeCache.get(tpe)
    if (ft.isDefined) {
      Some(if (inContainer && erasedVersion) ft.get.erased else ft.get.actual)
    } else tpe.dealias match {
      case TypeRef(_, sym, args) if args.isEmpty && sym.isClass =>
        Some(mirror.runtimeClass(sym.asClass))
      case TypeRef(_, sym, args) if sym.fullName == "scala.Array" && args.size == 1 =>
        buildType(args.head, mirror, inContainer, erasedVersion) match {
          case Some(typeArg) =>
            typeArg match {
              case cl: Class[_] => Some(java.lang.reflect.Array.newInstance(cl, 0).getClass)
              case _ => Some(new GenArrType(typeArg))
            }
          case _ => None
        }
      case TypeRef(_, sym, args) if args.nonEmpty && sym.isClass =>
        val symClass = mirror.runtimeClass(sym.asClass)
        val typeArgs = args.flatMap(it => buildType(it, mirror, true, erasedVersion))
        if (typeArgs.size == args.size) Some(Utils.makeGenericType(symClass, typeArgs))
        //TODO: temporary hacky resolution to raw type
        else if (typeArgs.isEmpty) Some(symClass)
        else None
      case ExistentialType(_, t) =>
        t match {
          case TypeRef(_, sym, _) if sym.isClass =>
            Some(mirror.runtimeClass(sym.asClass))
          case _ =>
            None
        }
      case _ =>
        None
    }
  }


  private[revenj] def makeGenericType(container: Class[_], arguments: List[JavaType]): ParameterizedType = {
    val sb = sharedBuilder.get()
    sb.setLength(0)
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
    val sb = sharedBuilder.get()
    sb.setLength(0)
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

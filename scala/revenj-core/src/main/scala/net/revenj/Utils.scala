package net.revenj

import java.lang.reflect.{GenericArrayType, ParameterizedType, Type => JavaType}
import java.net.InetAddress
import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.UUID

import scala.collection.concurrent.TrieMap
import scala.reflect.runtime.universe._

object Utils {
  val MIN_LOCAL_DATE = LocalDate.of(1, 1, 1)
  val MIN_LOCAL_DATE_TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0)
  val MIN_DATE_TIME = OffsetDateTime.of(MIN_LOCAL_DATE_TIME, ZoneOffset.UTC)
  val MIN_UUID = new UUID(0L, 0L)
  val EMPTY_BINARY = new Array[Byte](0)
  val ZERO_0 = BigDecimal(0).setScale(0)
  val ZERO_1 = BigDecimal(0).setScale(1)
  val ZERO_2 = BigDecimal(0).setScale(2)
  val ZERO_3 = BigDecimal(0).setScale(3)
  val ZERO_4 = BigDecimal(0).setScale(4)
  val LOOPBACK = InetAddress.getLoopbackAddress
  private val typeCache = new TrieMap[String, GenericType]

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
    typeCache.getOrElseUpdate(name, {
      new GenericType(name, container, arguments.toArray)
    })
  }

  private class GenArrType(genType: JavaType) extends GenericArrayType {
    def getGenericComponentType = genType
  }

  private[revenj] def findType(tpe: Type, mirror: Mirror): Option[JavaType] = {
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
    typeCache.getOrElseUpdate(name, {
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

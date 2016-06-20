package net.revenj

import java.lang.reflect.{ParameterizedType, Type}
import java.net.InetAddress
import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.UUID

import scala.collection.concurrent.TrieMap

object Utils {
  val MIN_LOCAL_DATE: LocalDate = LocalDate.of(1, 1, 1)
  val MIN_LOCAL_DATE_TIME: LocalDateTime = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0)
  val MIN_DATE_TIME: OffsetDateTime = OffsetDateTime.of(MIN_LOCAL_DATE_TIME, ZoneOffset.UTC)
  val MIN_UUID: UUID = new UUID(0L, 0L)
  val EMPTY_BINARY: Array[Byte] = new Array[Byte](0)
  val ZERO_0: BigDecimal = BigDecimal(0).setScale(0)
  val ZERO_1: BigDecimal = BigDecimal(0).setScale(1)
  val ZERO_2: BigDecimal = BigDecimal(0).setScale(2)
  val ZERO_3: BigDecimal = BigDecimal(0).setScale(3)
  val ZERO_4: BigDecimal = BigDecimal(0).setScale(4)
  val LOOPBACK: InetAddress = InetAddress.getLoopbackAddress
  private val typeCache = new TrieMap[String, GenericType]

  private class GenericType(val name: String, val raw: Type, val arguments: Array[Type]) extends ParameterizedType {
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

    def getActualTypeArguments: Array[Type] = arguments

    def getRawType: Type = raw

    def getOwnerType: Type = null

    override def toString: String = name
  }

  private[revenj] def makeGenericType(container: Class[_], arguments: List[Type]): ParameterizedType = {
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

  def makeGenericType(container: Class[_], argument: Type, arguments: Type*): ParameterizedType = {
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
      val genericArgs = new Array[Type](arguments.length + 1)
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

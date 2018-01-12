package net.revenj.database.postgres.converters

import java.io.ByteArrayInputStream
import java.nio.charset.Charset

import net.revenj.Utils
import net.revenj.database.postgres.PostgresReader
import org.xml.sax.InputSource

import scala.xml.Elem

object XmlConverter extends Converter[Elem] {

  private val utf8 = Charset.forName("UTF-8")

  override val dbName = "xml"

  override def default(): Elem = null

  private def toElem(xml: String) = Utils.parse[Elem](new InputSource(new ByteArrayInputStream(xml.getBytes(utf8))))

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Elem = {
    toElem(StringConverter.parseRaw(reader, start, context))
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Elem = {
    toElem(StringConverter.parseCollectionItem(reader, context))
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Elem] = {
    StringConverter.parseNullableCollectionItem(reader, context).map(toElem)
  }

  override def toTuple(value: Elem): PostgresTuple = ValueTuple.from(value.toString)
}

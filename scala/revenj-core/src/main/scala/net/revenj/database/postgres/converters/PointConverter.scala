package net.revenj.database.postgres.converters

import java.awt.Point
import java.sql.PreparedStatement

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}
import org.postgresql.util.PGobject

object PointConverter extends Converter[Point] {

  override val dbName = "point"

  override def default() = new Point

  def setParameter(sw: PostgresBuffer, ps: PreparedStatement, index: Int, value: Point): Unit = {
    val pg = new PGobject
    pg.setType("point")
    pg.setValue("(" + value.x + "," + value.y + ")")
    ps.setObject(index, pg)
  }

  def setParameter(sw: PostgresBuffer, ps: PreparedStatement, index: Int, value: Option[Point]): Unit = {
    val pg = new PGobject
    pg.setType("point")
    ps.setObject(index, pg)
  }

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Point = {
    reader.read(context)
    val x = IntConverter.parse(reader, context)
    val y = IntConverter.parse(reader, context)
    reader.read(context + 1)
    new Point(x, y)
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Point = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      new Point
    } else {
      reader.read(context)
      val x = IntConverter.parse(reader, context)
      val y = IntConverter.parse(reader, context)
      reader.read(context + 1)
      new Point(x, y)
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Point] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      reader.read(context)
      val x = IntConverter.parse(reader, context)
      val y = IntConverter.parse(reader, context)
      reader.read(context + 1)
      Some(new Point(x, y))
    }
  }

  override def toTuple(value: Point): PostgresTuple = new PointTuple(value)

  private class PointTuple(val value: Point) extends PostgresTuple {
    val mustEscapeRecord = true

    val mustEscapeArray = true

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write('(')
      sw.write(value.x.toString)
      sw.write(',')
      sw.write(value.y.toString)
      sw.write(')')
    }

    override def buildTuple(quote: Boolean): String = {
      if (quote) {
        "'(" + value.x + "," + value.y + ")'"
      } else {
        "(" + value.x + "," + value.y + ")"
      }
    }
  }
}

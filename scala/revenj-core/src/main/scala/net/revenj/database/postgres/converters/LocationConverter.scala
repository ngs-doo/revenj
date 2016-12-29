package net.revenj.database.postgres.converters

import java.awt.geom.Point2D
import java.sql.PreparedStatement

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader, PostgresWriter}
import org.postgresql.util.PGobject

object LocationConverter extends Converter[Point2D] {

  override val dbName = "point"

  override def default() = new Point2D.Double

  def setParameter(sw: PostgresBuffer, ps: PreparedStatement, index: Int, value: Point2D): Unit = {
    val pg = new PGobject
    pg.setType("point")
    pg.setValue("(" + value.getX + "," + value.getY + ")")
    ps.setObject(index, pg)
  }

  def setParameter(sw: PostgresBuffer, ps: PreparedStatement, index: Int, value: Option[Point2D]): Unit = {
    val pg = new PGobject
    pg.setType("point")
    ps.setObject(index, pg)
  }

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): Point2D = {
    reader.read(context)
    val x = DoubleConverter.parse(reader, context)
    val y = DoubleConverter.parse(reader, context)
    reader.read(context + 1)
    new Point2D.Double(x, y)
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): Point2D = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      new Point2D.Double
    } else {
      reader.read(context)
      val x = DoubleConverter.parse(reader, context)
      val y = DoubleConverter.parse(reader, context)
      reader.read(context + 1)
      new Point2D.Double(x, y)
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[Point2D] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      reader.read(context)
      val x = DoubleConverter.parse(reader, context)
      val y = DoubleConverter.parse(reader, context)
      reader.read(context + 1)
      Some(new Point2D.Double(x, y))
    }
  }

  override def toTuple(value: Point2D): PostgresTuple = new PointTuple(value)

  private class PointTuple(val value: Point2D) extends PostgresTuple {
    val mustEscapeRecord = true

    val mustEscapeArray = true

    def insertRecord(sw: PostgresWriter, escaping: String, mappings: Option[(PostgresWriter, Char) => Unit]): Unit = {
      sw.write('(')
      sw.write(value.getX.toString)
      sw.write(',')
      sw.write(value.getY.toString)
      sw.write(')')
    }

    override def buildTuple(quote: Boolean): String = {
      if (quote) {
        "'(" + value.getX + "," + value.getY + ")'"
      } else {
        "(" + value.getX + "," + value.getY + ")"
      }
    }
  }
}

package net.revenj.database.postgres.converters

import net.revenj.TreePath
import net.revenj.database.postgres.{PostgresBuffer, PostgresReader}

object TreePathConverter extends Converter[TreePath] {
  override def serializeURI(sw: PostgresBuffer, value: TreePath): Unit = {
    if (value != null) sw.addToBuffer(value.toString)
  }

  val dbName = "ltree"

  def default() = TreePath.Empty

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): TreePath = {
    reader.initBuffer(start.toChar)
    reader.fillUntil(',', ')')
    reader.read()
    TreePath.create(reader.bufferToString())
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): TreePath = {
    val cur = reader.read()
    reader.initBuffer(cur.toChar)
    reader.fillUntil(',', '}')
    reader.read()
    if (reader.bufferMatches("NULL")) {
      TreePath.Empty
    } else {
      TreePath.create(reader.bufferToString())
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[TreePath] = {
    val cur = reader.read()
    reader.initBuffer(cur.toChar)
    reader.fillUntil(',', '}')
    reader.read()
    if (reader.bufferMatches("NULL")) {
      None
    } else {
      Some(TreePath.create(reader.bufferToString()))
    }
  }

  override def toTuple(value: TreePath): PostgresTuple = ValueTuple.from(value.toString)
}

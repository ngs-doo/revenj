package net.revenj.database.postgres.converters

import net.revenj.database.postgres.{PostgresBuffer, PostgresReader}
import net.revenj.storage.S3

object S3Converter extends Converter[S3] {
  private def toDatabase(value: S3): String = {
    val map = HstoreConverter.toTuple(value.metadata).buildTuple(false)
    def escape(input: String) = {
      input.replace("\\", "\\\\").replace("\"", "\\\"")
    }
    val name = if (value.name.isEmpty) "" else escape(value.name.get)
    val mimeType = if (value.mimeType.isEmpty) "" else escape(value.mimeType.get)
    s"""(${value.bucket},${value.key},${value.length},"$name","$mimeType","${escape(map)}")"""
  }

  override def serializeURI(sw: PostgresBuffer, value: S3): Unit = {
    val str = toDatabase(value)
    sw.addToBuffer(str)
  }

  def serializeCompositeURI(sw: PostgresBuffer, value: S3): Unit = {
    val str = toDatabase(value)
    StringConverter.serializeCompositeURI(sw, str)
  }

  override val dbName = "s3"

  override def default(): S3 = null

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): S3 = {
    parseS3(reader, context, if (context > 0) context << 1 else 1)
  }

  private def parseS3(reader: PostgresReader, context: Int, innerContext: Int): S3 = {
    reader.read(context)
    val bucket = StringConverter.parse(reader, innerContext)
    val key = StringConverter.parse(reader, innerContext)
    val length = LongConverter.parse(reader, innerContext)
    val name = StringConverter.parseOption(reader, innerContext)
    val mimeType = StringConverter.parseOption(reader, innerContext)
    val metadata = HstoreConverter.parse(reader, innerContext)
    reader.read(context + 1)
    S3(bucket, key, length, name, mimeType, metadata)
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): S3 = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      null
    } else {
      parseS3(reader, context, if (context == 0) 1 else context << 1)
    }
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[S3] = {
    val cur = reader.read()
    if (cur == 'N') {
      reader.read(4)
      None
    } else {
      Some(parseS3(reader, context, if (context == 0) 1 else context << 1))
    }
  }

  def toTuple(value: S3): PostgresTuple = {
    new RecordTuple(
      Array(
        StringConverter.toTuple(value.bucket),
        StringConverter.toTuple(value.key),
        LongConverter.toTuple(value.length),
        StringConverter.toTuple(value.name),
        StringConverter.toTuple(value.mimeType),
        HstoreConverter.toTuple(value.metadata)
      )
    )
  }
}

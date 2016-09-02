package net.revenj.database.postgres.converters

import java.net.InetAddress

import net.revenj.Utils
import net.revenj.database.postgres.PostgresReader

object InetConverter extends Converter[InetAddress] {

  override val dbName = "inet"

  override def default() = Utils.Loopback

  override def parseRaw(reader: PostgresReader, start: Int, context: Int): InetAddress = {
    InetAddress.getByName(StringConverter.parseRaw(reader, start, context))
  }

  override def parseCollectionItem(reader: PostgresReader, context: Int): InetAddress = {
    InetAddress.getByName(StringConverter.parseCollectionItem(reader, context))
  }

  override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[InetAddress] = {
    StringConverter.parseNullableCollectionItem(reader, context).map(InetAddress.getByName)
  }

  override def toTuple(value: InetAddress): PostgresTuple = ValueTuple.from(value.getHostAddress)
}

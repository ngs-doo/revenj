package net.revenj.database.postgres

trait PostgresBuffer {
  def tempBuffer: Array[Char]

  def initBuffer(): Unit

  def initBuffer(c: Char): Unit

  def addToBuffer(c: Char): Unit

  def addToBuffer(c: Array[Char]): Unit

  def addToBuffer(c: Array[Char], len: Int): Unit

  def addToBuffer(c: Array[Char], start: Int, emd: Int): Unit

  def addToBuffer(input: String): Unit

  def bufferToString(): String
}

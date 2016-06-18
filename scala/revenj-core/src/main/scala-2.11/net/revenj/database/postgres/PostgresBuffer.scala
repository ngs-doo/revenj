package net.revenj.database.postgres

trait PostgresBuffer {
  def tempBuffer: Array[Char]

  def initBuffer()

  def initBuffer(c: Char)

  def addToBuffer(c: Char)

  def addToBuffer(c: Array[Char])

  def addToBuffer(c: Array[Char], len: Int)

  def addToBuffer(c: Array[Char], start: Int, emd: Int)

  def addToBuffer(input: String)

  def bufferToString(): String
}

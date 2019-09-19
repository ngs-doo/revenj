package net.revenj.patterns

trait ValidationErrors {
  def registerError(path: String, message: String): Unit

  def getValidationErrors: scala.collection.Map[String, scala.collection.Seq[String]]
}
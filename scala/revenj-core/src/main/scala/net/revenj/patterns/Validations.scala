package net.revenj.patterns

trait ValidationErrors {
  def registerError(path: String, message: String): Unit

  def getValidationErrors: Map[String, Seq[String]]
}
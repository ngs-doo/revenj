package org.revenj.patterns

case class Properties(path: String) {
  private val props = new java.util.Properties

  locally {
    val pIS = new java.io.FileInputStream(path)
    try {
      props.load(pIS)
    } finally {
      pIS.close()
    }
  }

  private def read(key: String) =
    Option(props.getProperty(key)) map (_.replaceFirst("#.*", "").trim)

  def apply(key: String) =
    read(key) getOrElse sys.error("""Could not read property "%s"!""" format key)

  def apply(key: String, default: String) =
    read(key) getOrElse default

  def apply(key: String, default: Boolean) =
    read(key) map (_.toBoolean) getOrElse default

  def apply(key: String, default: Int) =
    read(key) map (_.toInt) getOrElse default
}

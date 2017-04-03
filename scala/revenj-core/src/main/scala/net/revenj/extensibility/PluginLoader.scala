package net.revenj.extensibility

import net.revenj.Utils

import scala.reflect.runtime.universe._

trait PluginLoader {
  def find[T : TypeTag]: Seq[Class[T]]

  @deprecated("Use resolve without class argument", "0.5.0")
  def resolve[T : TypeTag](container: Container, manifest: Class[T]): Array[T] = {
    resolve[T](container)
  }
  def resolve[T : TypeTag](container: Container): Array[T] = {
    val loader = container.resolve[ClassLoader]
    Utils.findType(typeOf[T], runtimeMirror(loader)) match {
      case Some(tpe) =>
        val scope = container.createScope()
        try {
          find[T] foreach { sc =>
            scope.registerType(tpe, sc, singleton = true)
          }
          scope.resolve[Array[T]]
        } finally {
          scope.close()
        }
      case _ => throw new IllegalArgumentException(s"Unable to resolve plugins for provided type: ${typeOf[T]}")
    }
  }
}

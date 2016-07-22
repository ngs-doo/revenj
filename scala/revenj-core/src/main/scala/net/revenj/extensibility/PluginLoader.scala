package net.revenj.extensibility

import scala.reflect.runtime.universe.TypeTag

trait PluginLoader {
  def find[T : TypeTag]: Seq[Class[T]]

  def resolve[T : TypeTag](container: Container, manifest: Class[T]): Array[T] = {
    val scope = container.createScope()
    try {
      val manifests = find[T]
      for (sc <- manifests) {
        scope.registerType(manifest, sc, singleton = false)
      }
      scope.resolve[Array[T]]
    } finally {
      scope.close()
    }
  }
}

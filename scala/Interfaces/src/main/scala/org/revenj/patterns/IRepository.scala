package org.revenj.patterns

trait IRepository[T <: IIdentifiable] {
  def find(uris: Traversable[String]): IndexedSeq[T]

  def find(uri: String): Option[T] =
    find(Seq(uri)).headOption
}

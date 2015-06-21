package org.revenj.patterns

trait IPersistableRepository[T <: IIdentifiable] extends IRepository[T] {
  def persist(insert: Traversable[T],
              update: Traversable[(T, T)],
              delete: Traversable[T]): IndexedSeq[String]

  def insert(insert: Traversable[T]): IndexedSeq[String] =
    persist(insert, Nil, Nil)

  def insert(insert: T): String =
    persist(Seq(insert), Nil, Nil).head

  def update(update: Traversable[T]) {
    val newValues = update.toIndexedSeq
    val oldValues = find(newValues.map(_.URI))
    if (newValues.length != oldValues.length) {
      throw new IllegalArgumentException("Can't find all values to update!")
    }
    persist(Nil, oldValues.zip(newValues), Nil)
  }

  def update(update: T) {
    find(update.URI) match {
      case Some(oldValue) =>
        persist(Nil, Seq(oldValue -> update), Nil)

      case _ =>
        throw new IllegalArgumentException("Can't find value to update: " + update.URI)
    }
  }

  def delete(delete: Traversable[T]) {
    persist(Nil, Nil, delete)
  }

  def delete(delete: T) {
    persist(Nil, Nil, Seq(delete))
  }
}

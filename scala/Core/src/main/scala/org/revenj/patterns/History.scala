package org.revenj.patterns

case class History[T <: IIdentifiable] (
    val snapshots: IndexedSeq[Snapshot[T]]
  ) extends IHistory[T] {

  val URI = snapshots.head.URI
}

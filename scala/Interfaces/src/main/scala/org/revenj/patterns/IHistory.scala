package org.revenj.patterns

trait IHistory[T <: IIdentifiable] extends IIdentifiable {
  val snapshots: IndexedSeq[ISnapshot[T]]
}

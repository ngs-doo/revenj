package org.revenj.patterns

import org.joda.time.DateTime

trait ISnapshot[T <: IIdentifiable] extends IIdentifiable {
  val at: DateTime
  val value: T
  val action: String
}

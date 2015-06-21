package org.revenj.patterns

import org.joda.time.DateTime

case class Snapshot[T <: IIdentifiable] (
    at: DateTime
  , value: T
  , action: String
  ) extends ISnapshot[T] {

  val URI = value.URI + '/' + at.getMillis

  override val hashCode = URI.hashCode

  override def equals(o: Any) =
    o match {
      case c: ISnapshot[_] => c.URI == URI
      case _ => false
    }
}

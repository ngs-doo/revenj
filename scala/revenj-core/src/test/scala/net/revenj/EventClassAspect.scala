package net.revenj

import net.revenj.patterns.EventStoreAspect

class EventClassAspect extends EventStoreAspect[TestMe] {
  override def before(events: scala.collection.Seq[TestMe]): scala.collection.Seq[TestMe] = {
    EventClassAspect.calledBefore += 1
    events
  }
  override def after(events: scala.collection.Seq[TestMe]): Unit = {
    EventClassAspect.calledAfter += 1
  }
}
object EventClassAspect {
  var calledBefore = 0
  var calledAfter = 0
}
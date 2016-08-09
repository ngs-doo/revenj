package net.revenj

import net.revenj.patterns.DomainEventHandler

class TestMeArrayHandler extends DomainEventHandler[Array[TestMe]] {
  var called = 0
  override def handle(events: Array[TestMe]): Unit = {
    called += 1
  }
}

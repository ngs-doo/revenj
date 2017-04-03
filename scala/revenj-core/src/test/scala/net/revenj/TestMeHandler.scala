package net.revenj

import net.revenj.patterns.DomainEventHandler

class TestMeHandler extends DomainEventHandler[TestMe] {
  var called = 0
  override def handle(event: TestMe): Unit = {
    called += 1
  }
}

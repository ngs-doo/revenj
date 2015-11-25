package org.revenj.processor.models;

import org.revenj.patterns.DomainEventHandler;
import org.revenj.patterns.EventHandler;

@EventHandler
public class TestValidEventHandler implements DomainEventHandler<TestEvent> {
	public static int SINGLE_COUNT;

	@Override
	public void handle(TestEvent domainEvent) {
		SINGLE_COUNT++;
	}
}

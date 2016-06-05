package org.revenj.processor.models;

import org.revenj.patterns.DomainEventHandler;
import org.revenj.patterns.EventHandler;

@EventHandler
public class ValidEventHandler implements DomainEventHandler<SimpleEvent> {
	public static int SINGLE_COUNT;

	@Override
	public void handle(SimpleEvent domainEvent) {
		SINGLE_COUNT++;
	}
}

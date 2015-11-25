package org.revenj.processor.models;

import org.revenj.patterns.EventHandler;

@EventHandler
public class TestInvalidEventHandler {
	public void handle(TestEvent domainEvent) {
	}
}

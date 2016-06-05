package org.revenj.processor.models;

import org.revenj.patterns.DomainEvent;

import java.time.OffsetDateTime;

public class SimpleEvent implements DomainEvent {
	@Override
	public OffsetDateTime getQueuedAt() {
		return null;
	}

	@Override
	public OffsetDateTime getProcessedAt() {
		return null;
	}

	@Override
	public String getURI() {
		return null;
	}
}

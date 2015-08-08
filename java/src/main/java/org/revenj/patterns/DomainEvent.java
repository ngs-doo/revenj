package org.revenj.patterns;

import java.time.OffsetDateTime;

public interface DomainEvent extends Identifiable {
	OffsetDateTime getQueuedAt();
	OffsetDateTime getProcessedAt();
}

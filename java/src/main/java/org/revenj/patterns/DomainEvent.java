package org.revenj.patterns;

import java.time.LocalDateTime;

public interface DomainEvent extends Identifiable {
	LocalDateTime getQueuedAt();
	LocalDateTime getProcessedAt();
}

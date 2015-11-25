package org.revenj.patterns;

public interface DomainEventHandler<T> {
	void handle(T domainEvent);
}

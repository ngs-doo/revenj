package org.revenj.patterns;

import java.util.Collection;
import java.util.Collections;

public interface DomainEventStore<TEvent extends DomainEvent> extends Repository<TEvent>, SearchableRepository<TEvent> {
	String[] submit(Collection<TEvent> domainEvents);

	default String submit(TEvent domainEvent) {
		return submit(Collections.singletonList(domainEvent))[0];
	}

	void mark(String[] uris);

	default void mark(String uri) {
		mark(new String[]{uri});
	}
}

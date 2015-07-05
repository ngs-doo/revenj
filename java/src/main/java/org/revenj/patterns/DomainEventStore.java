package org.revenj.patterns;

import java.util.Collections;
import java.util.List;

public interface DomainEventStore<TEvent /*extends DomainEvent*/> extends Repository<TEvent> {
	String[] submit(List<TEvent> domainEvents);

	default String submit(TEvent domainEvent) {
		return submit(Collections.singletonList(domainEvent))[0];
	}

	void mark(String[] uris);

	default void mark(String uri) {
		mark(new String[]{uri});
	}
}

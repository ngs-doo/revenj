package org.revenj.patterns;

import java.time.OffsetDateTime;

public interface Snapshot<T extends ObjectHistory> extends Identifiable {
	OffsetDateTime getAt();

	String getAction();

	T getValue();
}

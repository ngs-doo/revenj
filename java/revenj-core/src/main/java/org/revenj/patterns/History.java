package org.revenj.patterns;

import java.util.List;

public interface History<T extends ObjectHistory> extends Identifiable {
	<S extends Snapshot<T>> List<S> getSnapshots();
}

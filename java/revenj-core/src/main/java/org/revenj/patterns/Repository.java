package org.revenj.patterns;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Repository<T extends Identifiable> {
	List<T> find(String[] uris);

	default List<T> find(Collection<String> uris) {
		return find(uris.toArray(new String[uris.size()]));
	}

	default Optional<T> find(String uri) {
		List<T> result = find(new String[] { uri });
		return result.isEmpty() ? Optional.<T>empty() : Optional.of(result.get(0));
	}
}

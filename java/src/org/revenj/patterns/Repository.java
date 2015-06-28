package org.revenj.patterns;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface Repository<T/* extends Identifiable*/> {
	List<T> find(String[] uris);

	default Optional<T> find(String uri) {
		List<T> result = find(new String[] { uri });
		return result.isEmpty() ? Optional.<T>empty() : Optional.of(result.get(0));
	}
}

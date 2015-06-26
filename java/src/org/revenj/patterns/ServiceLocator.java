package org.revenj.patterns;

import java.util.NoSuchElementException;
import java.util.Optional;

public interface ServiceLocator {
	Optional<Object> resolve(String name);

	default <T> T resolve(Class<T> manifest) {
		Optional<Object> found = resolve(manifest.getName());
		if (!found.isPresent()) {
			throw new NoSuchElementException(manifest.getName());
		}
		return (T) found.get();
	}
}

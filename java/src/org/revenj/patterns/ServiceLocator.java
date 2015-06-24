package org.revenj.patterns;

import java.util.Optional;

public interface ServiceLocator {
	Optional<Object> lookup(String name);

	default <T> Optional<T> lookup(Class<T> manifest) {
		Optional<Object> found = lookup(manifest.getName());
		return found.isPresent() ? Optional.of((T)found.get()) : Optional.<T>empty();
	}
}

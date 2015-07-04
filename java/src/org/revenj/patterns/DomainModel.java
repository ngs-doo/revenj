package org.revenj.patterns;

import java.util.Optional;

public interface DomainModel {
	Optional<Class<?>> find(String name);
}

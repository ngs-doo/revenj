package org.revenj.postgres;

import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.converters.PostgresTuple;

import java.io.IOException;

public interface ObjectConverter<T> {
	T from(PostgresReader reader, ServiceLocator locator) throws IOException;
	PostgresTuple to(T instance);
}

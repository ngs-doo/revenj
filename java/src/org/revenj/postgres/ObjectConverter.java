package org.revenj.postgres;

import org.revenj.postgres.converters.PostgresTuple;

import java.io.IOException;

public interface ObjectConverter<T> {
	T from(PostgresReader reader) throws IOException;
	PostgresTuple to(T instance);

	interface Reader<T> {
		void read(T instance, PostgresReader reader, int context) throws IOException;
	}
}

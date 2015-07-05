package org.revenj.patterns;

import java.io.IOException;
import java.lang.reflect.Type;

public interface Serialization<TFormat> {
	TFormat serialize(Object value);

	Object deserialize(Type type, TFormat data) throws IOException;

	default <T> T deserialize(Class<T> manifest, TFormat data) throws IOException {
		return (T) deserialize((Type) manifest, data);
	}
}

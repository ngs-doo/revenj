package org.revenj.extensibility;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;

public interface PluginLoader {
	<T> List<Class<T>> find(Class<T> manifest, Type... types) throws Exception;

	@SuppressWarnings("unchecked")
	default <T> T[] resolve(Container container, Class<T> manifest) throws Exception {
		try (Container scope = container.createScope()) {
			List<Class<T>> manifests = find(manifest);
			for (Class<T> sc : manifests) {
				scope.registerType(manifest, sc, InstanceScope.CONTEXT);
			}
			return (T[]) scope.resolve((GenericArrayType) () -> manifest);
		}
	}
}

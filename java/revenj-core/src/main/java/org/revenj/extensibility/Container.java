package org.revenj.extensibility;

import org.revenj.patterns.ServiceLocator;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Container extends ServiceLocator, AutoCloseable {

	void registerClass(Type type, Class<?> manifest, boolean singleton);

	void registerInstance(Type type, Object service, boolean handleClose);

	void registerFactory(Type type, Function<Container, ?> factory, boolean singleton);

	<T> void registerGenerics(Class<T> manifest, BiFunction<Container, Type[], T> factory);

	default <T> void register(Class<T> manifest, boolean singleton) {
		registerClass(manifest, manifest, singleton);
	}

	default <T> void register(Class<T> manifest, Class<?>... manifests) {
		registerClass(manifest, manifest, false);
		for (Class<?> it : manifests) {
			registerClass(it, it, false);
		}
	}

	default <TInterface, TService extends TInterface> void registerAs(
			Class<TService> manifest,
			Class<TInterface> as,
			boolean singleton) {
		registerClass(as, manifest, singleton);
	}

	default <T> void registerInstance(T service) {
		registerInstance(service.getClass(), service, service instanceof AutoCloseable);
	}

	default <TInterface, TService extends TInterface> void registerAs(TService service, Class<TInterface> as) {
		registerInstance(as, service, service instanceof AutoCloseable);
	}

	default <T> void register(Class<T> manifest, Function<Container, T> service) {
		registerFactory(manifest, service, false);
	}

	Container createScope();
}

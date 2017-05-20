package org.revenj.extensibility;

import org.revenj.patterns.ServiceLocator;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Container extends ServiceLocator, AutoCloseable {

	@Deprecated
	default void registerClass(Type type, Class<?> manifest, boolean singleton) {
		registerType(type, manifest, singleton ? InstanceScope.SINGLETON : InstanceScope.TRANSIENT);
	}

	void registerType(Type type, Class<?> manifest, InstanceScope scope);

	void registerInstance(Type type, Object service, boolean handleClose);

	@Deprecated
	default void registerFactory(Type type, Function<Container, ?> factory, boolean singleton) {
		registerFactory(type, factory, singleton ? InstanceScope.SINGLETON : InstanceScope.CONTEXT);
	}

	@Deprecated
	default void registerFactory(Type type, Function<Container, ?> factory) {
		registerFactory(type, factory, InstanceScope.TRANSIENT);
	}

	void registerFactory(Type type, Function<Container, ?> factory, InstanceScope scope);

	default	<T> void registerFactory(Class<T> manifest, Function<Container, T> factory, InstanceScope scope) {
		registerFactory((Type)manifest, factory, scope);
	}

	<T> void registerGenerics(Class<T> manifest, BiFunction<Container, Type[], T> factory, InstanceScope scope);

	@Deprecated
	default <T> void register(Class<T> manifest, boolean singleton) {
		registerType(manifest, manifest, singleton ? InstanceScope.SINGLETON : InstanceScope.TRANSIENT);
	}

	default <T> void register(Class<T> manifest, InstanceScope scope) {
		registerType(manifest, manifest, scope);
	}

	default <T> void register(Class<T> manifest, Class<?>... manifests) {
		registerType(manifest, manifest, InstanceScope.TRANSIENT);
		for (Class<?> it : manifests) {
			registerType(it, it, InstanceScope.TRANSIENT);
		}
	}

	@Deprecated
	default <TInterface, TService extends TInterface> void registerAs(
			Class<TService> manifest,
			Class<TInterface> as,
			boolean singleton) {
		registerAs(manifest, as, singleton ? InstanceScope.SINGLETON : InstanceScope.TRANSIENT);
	}

	default <TInterface, TService extends TInterface> void registerAs(
			Class<TService> manifest,
			Class<TInterface> as,
			InstanceScope scope) {
		registerType(as, manifest, scope);
	}

	default <T> void registerInstance(T service) {
		registerInstance(service.getClass(), service, service instanceof AutoCloseable);
	}

	default <TInterface, TService extends TInterface> void registerAs(TService service, Class<TInterface> as) {
		registerInstance(as, service, service instanceof AutoCloseable);
	}

	default <T> void register(Class<T> manifest, Function<Container, T> service) {
		registerFactory(manifest, service, InstanceScope.TRANSIENT);
	}

	Container createScope();
}

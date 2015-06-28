package org.revenj.patterns;

public interface Container extends ServiceLocator, AutoCloseable {
	interface Factory<T> {
		T create(Container scope);
	}

	<T> void register(Class<T> manifest, boolean singleton);

	<TInterface, TService extends TInterface> void register(Class<TService> manifest, Class<TInterface> as, boolean singleton);

	<T> void register(boolean closable, T service);

	default <T> void register(T service) {
		register(service instanceof AutoCloseable, service);
	}

	<TInterface, TService extends TInterface> void register(TService service, Class<TInterface> as);

	<T> void register(Class<T> manifest, boolean singleton, Factory<T> service);

	Container createScope();
}

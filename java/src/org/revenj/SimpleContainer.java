package org.revenj;

import org.revenj.patterns.Container;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

class SimpleContainer implements Container {

	private static class Registration<T> {
		public final Container owner;
		public final Class<T> as;
		public final Class<T> type;
		public final T instance;
		public final Container.Factory<T> factory;
		public final boolean singleton;

		public Registration(Container owner, Class<T> as, Class<T> type, T instance, Container.Factory<T> factory, boolean singleton) {
			this.owner = owner;
			this.as = as;
			this.type = type;
			this.instance = instance;
			this.factory = factory;
			this.singleton = singleton;
		}
	}

	private final Map<String, Registration<?>> container;

	private final List<AutoCloseable> closeables = new ArrayList<>();

	SimpleContainer() {
		container = new HashMap<>();
	}

	private SimpleContainer(Map<String, Registration<?>> parent) {
		this.container = new HashMap<>(parent);
	}

	private Optional<Object> tryResolve(Class<?> target) {
		for (Constructor<?> ctor : target.getConstructors()) {
			List<Object> args = new ArrayList<>();
			boolean success = true;
			for (Class<?> p : ctor.getParameterTypes()) {
				final Optional<Object> a = resolve(p.getName());
				if (!a.isPresent()) {
					success = false;
					break;
				}
				args.add(a.get());
			}

			if (success) {
				try {
					final Object instance = ctor.newInstance(args.toArray());
					return Optional.of(instance);
				} catch (final Exception ignored) {
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Object> resolve(String name) {
		Registration<?> registration = container.get(name);
		if (registration == null) {
			Class<?> target;
			try {
				target = Class.forName(name);
			} catch (ClassNotFoundException e) {
				return Optional.empty();
			}
			if (target.isArray()) {
				Class<?> element = target.getComponentType();
			}
			return Optional.empty();
		}
		if (registration.instance != null) {
			return Optional.of(registration.instance);
		} else if (registration.factory != null) {
			Object instance = registration.factory.create(this);
			if (registration.singleton && instance != null) {
				if (instance instanceof AutoCloseable) {
					closeables.add((AutoCloseable) instance);
				}
				container.put(name, new Registration(this, instance.getClass(), registration.type, instance, null, true));
			}
			return Optional.ofNullable(instance);
		}
		Optional<Object> tryInstance = tryResolve(registration.type);
		if (registration.singleton && tryInstance.isPresent()) {
			if (tryInstance.get() instanceof AutoCloseable) {
				closeables.add((AutoCloseable) tryInstance.get());
			}
			container.put(name, new Registration(this, tryInstance.getClass(), null, tryInstance.get(), null, true));
		}
		return tryInstance;
	}

	@Override
	public <T> void register(Class<T> manifest, boolean singleton) {
		container.put(manifest.getName(), new Registration(this, manifest, manifest, null, null, singleton));
	}

	@Override
	public <TInterface, TService extends TInterface> void register(Class<TService> manifest, Class<TInterface> as, boolean singleton) {
		container.put(as.getName(), new Registration(this, as, manifest, null, null, singleton));
	}

	@Override
	public <T> void register(boolean closable, T service) {
		if (closable && service instanceof AutoCloseable) {
			closeables.add((AutoCloseable) service);
		}
		container.put(service.getClass().getName(), new Registration(this, service.getClass(), null, service, null, closable));
	}

	@Override
	public <TInterface, TService extends TInterface> void register(TService service, Class<TInterface> as) {
		container.put(as.getName(), new Registration(this, as, null, service, null, true));
	}

	@Override
	public <T> void register(Class<T> manifest, boolean singleton, Factory<T> factory) {
		container.put(manifest.getName(), new Registration(this, manifest, null, null, factory, singleton));
	}

	@Override
	public Container createScope() {
		return new SimpleContainer(this.container);
	}

	@Override
	public void close() throws Exception {
		for (AutoCloseable closable : closeables) {
			closable.close();
		}
		container.clear();
	}
}

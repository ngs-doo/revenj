package org.revenj;

import org.revenj.patterns.Container;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

class SimpleContainer implements Container {

	private static class Registration<T> {
		public final Container owner;
		public final Class<T> manifest;
		public T instance;
		public final Container.Factory<T> factory;
		public final boolean singleton;

		public Registration(Container owner, Class<T> manifest, T instance, Container.Factory<T> factory, boolean singleton) {
			this.owner = owner;
			this.manifest = manifest;
			this.instance = instance;
			this.factory = factory;
			this.singleton = singleton;
		}

		void promoteToSingleton(Object instance) {
			this.instance = (T) instance;
		}
	}

	private static final ConcurrentMap<Class<?>, CtorInfo[]> classCache = new ConcurrentHashMap<>();
	private static final ConcurrentMap<ParameterizedType, TypeInfo> typeCache = new ConcurrentHashMap<>();

	private static class TypeInfo {
		final CtorInfo[] constructors;
		final Class<?> rawClass;
		final Map<Type, Type> mappings = new HashMap<>();
		final Type[] genericArguments;

		public TypeInfo(ParameterizedType type) {
			Type rawType = type.getRawType();
			if (rawType instanceof Class<?>) {
				rawClass = (Class<?>) rawType;
				genericArguments = type.getActualTypeArguments();
				TypeVariable[] variables = rawClass.getTypeParameters();
				for (int i = 0; i < genericArguments.length; i++) {
					mappings.put(variables[i], genericArguments[i]);
				}
				Constructor<?>[] ctors = rawClass.getConstructors();
				constructors = new CtorInfo[ctors.length];
				for (int i = 0; i < ctors.length; i++) {
					constructors[i] = new CtorInfo(ctors[i]);
				}
			} else {
				constructors = null;
				rawClass = null;
				genericArguments = null;
			}
		}
	}

	static class CtorInfo {

		final Constructor<?> ctor;
		final Type[] rawTypes;
		final Type[] genTypes;

		public CtorInfo(Constructor<?> ctor) {
			this.ctor = ctor;
			rawTypes = ctor.getParameterTypes();
			genTypes = ctor.getGenericParameterTypes();
		}
	}

	private final Map<Type, List<Registration<?>>> container;

	private final CopyOnWriteArrayList<AutoCloseable> closeables = new CopyOnWriteArrayList<>();

	SimpleContainer() {
		container = new HashMap<>();
	}

	private SimpleContainer(Map<Type, List<Registration<?>>> parent) {
		this.container = new HashMap<>(parent);
	}

	private Optional<Object> tryResolveClass(Class<?> manifest) {
		CtorInfo[] constructors = classCache.get(manifest);
		if (constructors == null) {
			Constructor<?>[] ctors = manifest.getConstructors();
			constructors = new CtorInfo[ctors.length];
			for (int i = 0; i < ctors.length; i++) {
				constructors[i] = new CtorInfo(ctors[i]);
			}
			classCache.putIfAbsent(manifest, constructors);
		}
		for (CtorInfo info : constructors) {
			Type[] genTypes = info.genTypes;
			Object[] args = new Object[genTypes.length];
			boolean success = true;
			for (int i = 0; i < genTypes.length; i++) {
				Type p = genTypes[i];
				final Optional<Object> arg = tryResolve(p);
				if (!arg.isPresent()) {
					success = false;
					break;
				}
				args[i] = arg.get();
			}

			if (success) {
				try {
					final Object instance = info.ctor.newInstance(args);
					return Optional.of(instance);
				} catch (final Exception ignored) {
				}
			}
		}
		return Optional.empty();
	}

	private Optional<Object> tryResolveType(ParameterizedType type) {
		TypeInfo typeInfo = typeCache.get(type);
		if (typeInfo == null) {
			typeInfo = new TypeInfo(type);
			typeCache.putIfAbsent(type, typeInfo);
		}
		if (typeInfo.rawClass == null) {
			return Optional.empty();
		} else if (typeInfo.rawClass == Optional.class) {
			return Optional.of(tryResolve(typeInfo.genericArguments[0]));
		}
		Map<Type, Type> mappings = typeInfo.mappings;
		return tryResolveTypeFrom(typeInfo, mappings);
	}

	private Optional<Object> tryResolveTypeFrom(TypeInfo typeInfo, Map<Type, Type> mappings) {
		for (CtorInfo info : typeInfo.constructors) {
			Type[] genTypes = info.genTypes;
			Object[] args = new Object[genTypes.length];
			boolean success = true;
			for (int i = 0; i < genTypes.length; i++) {
				Type p = genTypes[i];
				if (p instanceof ParameterizedType) {
					ParameterizedType nestedType = (ParameterizedType) p;
					TypeInfo nestedInfo = typeCache.get(nestedType);
					if (nestedInfo == null) {
						nestedInfo = new TypeInfo(nestedType);
						typeCache.putIfAbsent(nestedType, nestedInfo);
					}
					if (nestedInfo.rawClass == null) {
						success = false;
						break;
					} else if (nestedInfo.rawClass == Optional.class) {
						args[i] = tryResolve(nestedInfo.genericArguments[0]);
					} else {
						Map<Type, Type> nestedMappings = new HashMap<>(typeInfo.mappings);
						for (Map.Entry<Type, Type> entry : nestedInfo.mappings.entrySet()) {
							Type parentValue = nestedMappings.get(entry.getValue());
							nestedMappings.put(entry.getKey(), parentValue != null ? parentValue : entry.getValue());
						}
						final Optional<Object> arg = tryResolveTypeFrom(nestedInfo, nestedMappings);
						if (!arg.isPresent()) {
							success = false;
							break;
						}
						args[i] = arg.get();
					}

				} else {
					while (p instanceof TypeVariable) {
						p = mappings.get(p);
						if (p == null) {
							success = false;
							break;
						}
					}
					final Optional<Object> arg = tryResolve(p);
					if (!arg.isPresent()) {
						success = false;
						break;
					}
					args[i] = arg.get();
				}
			}

			if (success) {
				try {
					final Object instance = info.ctor.newInstance(args);
					return Optional.of(instance);
				} catch (final Exception ignored) {
				}
			}
		}
		return Optional.empty();
	}

	private Registration<?> getRegistration(Type type) {
		List<Registration<?>> registrations = container.get(type);
		if (registrations == null) {
			return null;
		}
		return registrations.get(registrations.size() - 1);
	}

	@Override
	public Optional<Object> tryResolve(Type type) {
		Registration<?> registration = getRegistration(type);
		if (registration == null) {
			if (type instanceof ParameterizedType) {
				return tryResolveType((ParameterizedType) type);
			}
			if (type instanceof Class<?> == false) {
				return Optional.empty();
			}
			Class<?> target = (Class<?>) type;
			if (target.isArray()) {
				Class<?> element = target.getComponentType();
				List<Registration<?>> registrations = container.get(element);
				if (registrations == null || registrations.isEmpty()) {
					return Optional.of(Array.newInstance(element, 0));
				}
				List<Object> result = new ArrayList<>(registrations.size());
				for (int i = 0; i < registrations.size(); i++) {
					Optional<Object> item = resolveRegistration(registrations.get(i));
					if (item.isPresent()) {
						result.add(item.get());
					}
				}
				Object[] instance = (Object[]) Array.newInstance(element, result.size());
				for (int i = 0; i < instance.length; i++) {
					instance[i] = result.get(i);
				}
				return Optional.of(instance);
			}
			return Optional.empty();
		}
		return resolveRegistration(registration);
	}

	private Optional<Object> resolveRegistration(Registration<?> registration) {
		if (registration.instance != null) {
			return Optional.of(registration.instance);
		} else if (registration.factory != null) {
			Object instance = registration.factory.create(this);
			if (registration.singleton && instance != null) {
				if (instance instanceof AutoCloseable) {
					closeables.add((AutoCloseable) instance);
				}
				registration.promoteToSingleton(instance);
			}
			return Optional.ofNullable(instance);
		}
		Optional<Object> tryInstance = tryResolveClass(registration.manifest);
		if (registration.singleton && tryInstance.isPresent()) {
			if (tryInstance.get() instanceof AutoCloseable) {
				closeables.add((AutoCloseable) tryInstance.get());
			}
			registration.promoteToSingleton(tryInstance.get());
		}
		return tryInstance;
	}

	private synchronized void addToRegistry(Type type, Registration registration) {
		List<Registration<?>> registrations = container.get(type);
		if (registrations == null) {
			registrations = new CopyOnWriteArrayList<>();
			registrations.add(registration);
			container.put(type, registrations);
		} else {
			registrations.add(registration);
		}
	}

	@Override
	public void registerClass(Type type, Class<?> manifest, boolean singleton) {
		addToRegistry(type, new Registration(this, manifest, null, null, singleton));
	}

	@Override
	public void registerInstance(Type type, Object service, boolean handleClose) {
		if (handleClose && service instanceof AutoCloseable) {
			closeables.add((AutoCloseable) service);
		}
		addToRegistry(type, new Registration(this, null, service, null, handleClose));
	}

	@Override
	public void registerFactory(Type type, Factory<?> factory, boolean singleton) {
		addToRegistry(type, new Registration(this, null, null, factory, singleton));
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

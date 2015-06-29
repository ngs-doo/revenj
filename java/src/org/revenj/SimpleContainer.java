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

		boolean promoted;

		void promoteToSingleton(Object instance) {
			promoted = true;
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

	private static final class Either<T> {
		final T value;
		final Throwable error;

		private Either(T value, Throwable error) {
			this.value = value;
			this.error = error;
		}

		boolean hasError() {
			return error != null;
		}

		boolean isPresent() {
			return error == null;
		}

		static <T> Either<T> success(final T value) {
			return new Either<T>(value, null);
		}

		static <T> Either<T> fail(final Throwable error) {
			return new Either<T>(null, error);
		}

		static <T> Either<T> fail(final String error) {
			return new Either<T>(null, new ReflectiveOperationException(error));
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

	private Either<Object> tryResolveClass(Class<?> manifest) {
		Throwable error = null;
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
				Either<Object> arg = tryResolve(p);
				if (arg.hasError()) {
					success = false;
					if (error == null) {
						error = arg.error;
					} else {
						error.addSuppressed(arg.error);
					}
					break;
				}
				args[i] = arg.value;
			}
			if (success) {
				try {
					Object instance = info.ctor.newInstance(args);
					return Either.success(instance);
				} catch (Exception e) {
					if (error == null) {
						error = e;
					} else {
						error.addSuppressed(e);
					}
				}
			}
		}
		return error == null
				? Either.fail("Unable to find constructors for: " + manifest)
				: Either.fail(error);
	}

	private Either<Object> tryResolveType(ParameterizedType type) {
		TypeInfo typeInfo = typeCache.get(type);
		if (typeInfo == null) {
			typeInfo = new TypeInfo(type);
			typeCache.putIfAbsent(type, typeInfo);
		}
		if (typeInfo.rawClass == null) {
			return Either.fail(type + " is not an instance of Class<?> and cannot be resolved");
		} else if (typeInfo.rawClass == Optional.class) {
			Either<Object> content = tryResolve(typeInfo.genericArguments[0]);
			return Either.success(Optional.ofNullable(content.value));
		}
		Map<Type, Type> mappings = typeInfo.mappings;
		return tryResolveTypeFrom(typeInfo, mappings);
	}

	private Either<Object> tryResolveTypeFrom(TypeInfo typeInfo, Map<Type, Type> mappings) {
		Throwable error = null;
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
						ReflectiveOperationException roe = new ReflectiveOperationException("Nested parametrized type: " + nestedType + " is not an instance of Class<?>. Error while resolving constructor: " + info.ctor);
						if (error == null) {
							error = roe;
						} else {
							error.addSuppressed(roe);
						}
						break;
					} else if (nestedInfo.rawClass == Optional.class) {
						args[i] = tryResolve(nestedInfo.genericArguments[0]);
					} else {
						Map<Type, Type> nestedMappings = new HashMap<>(typeInfo.mappings);
						for (Map.Entry<Type, Type> entry : nestedInfo.mappings.entrySet()) {
							Type parentValue = nestedMappings.get(entry.getValue());
							nestedMappings.put(entry.getKey(), parentValue != null ? parentValue : entry.getValue());
						}
						Either<Object> arg = tryResolveTypeFrom(nestedInfo, nestedMappings);
						if (arg.hasError()) {
							success = false;
							if (error == null) {
								error = arg.error;
							} else {
								error.addSuppressed(arg.error);
							}
							break;
						}
						args[i] = arg.value;
					}

				} else {
					while (p instanceof TypeVariable) {
						p = mappings.get(p);
						if (p == null) {
							success = false;
							break;
						}
					}
					Either<Object> arg = tryResolve(p);
					if (arg.hasError()) {
						success = false;
						if (error == null) {
							error = arg.error;
						} else {
							error.addSuppressed(arg.error);
						}
						break;
					}
					args[i] = arg.value;
				}
			}

			if (success) {
				try {
					Object instance = info.ctor.newInstance(args);
					return Either.success(instance);
				} catch (final Exception e) {
					if (error == null) {
						error = e;
					} else {
						error.addSuppressed(e);
					}
				}
			}
		}
		return error == null
				? Either.fail("Unable to find constructors for: " + typeInfo.rawClass)
				: Either.fail(error);
	}

	private Registration<?> getRegistration(Type type) {
		List<Registration<?>> registrations = container.get(type);
		if (registrations == null) {
			return null;
		}
		return registrations.get(registrations.size() - 1);
	}

	@Override
	public Object resolve(Type type) throws ReflectiveOperationException {
		Either<Object> found = tryResolve(type);
		if (found.hasError()) {
			if (found.error instanceof ReflectiveOperationException) {
				throw (ReflectiveOperationException) found.error;
			}
			throw new ReflectiveOperationException("Unable to resolve: " + type + ". Reason: " + found.error.getMessage(), found.error);
		}
		return found.value;
	}

	public Either<Object> tryResolve(Type type) {
		Registration<?> registration = getRegistration(type);
		if (registration == null) {
			if (type instanceof ParameterizedType) {
				return tryResolveType((ParameterizedType) type);
			}
			if (type instanceof Class<?> == false) {
				return Either.fail(type + " is not an instance of Class<?> and cannot be resolved since it's not registered in the container.");
			}
			Class<?> target = (Class<?>) type;
			if (target.isArray()) {
				Class<?> element = target.getComponentType();
				List<Registration<?>> registrations = container.get(element);
				if (registrations == null || registrations.isEmpty()) {
					return Either.success(Array.newInstance(element, 0));
				}
				List<Object> result = new ArrayList<>(registrations.size());
				for (int i = 0; i < registrations.size(); i++) {
					Either<Object> item = resolveRegistration(registrations.get(i));
					if (item.isPresent()) {
						result.add(item.value);
					}
				}
				Object[] instance = (Object[]) Array.newInstance(element, result.size());
				for (int i = 0; i < instance.length; i++) {
					instance[i] = result.get(i);
				}
				return Either.success(instance);
			}
			return Either.fail(type + " is not registered in the container");
		}
		return resolveRegistration(registration);
	}

	private Either<Object> resolveRegistration(Registration<?> registration) {
		if (registration.instance != null) {
			return Either.success(registration.instance);
		} else if (registration.factory != null) {
			try {
				Object instance;
				if (registration.singleton) {
					synchronized (this) {
						if (registration.promoted) {
							return Either.success(registration.instance);
						}
						instance = registration.factory.create(this);
						if (instance instanceof AutoCloseable) {
							closeables.add((AutoCloseable) instance);
						}
						registration.promoteToSingleton(instance);
					}
				} else {
					instance = registration.factory.create(this);
				}
				return Either.success(instance);
			} catch (Throwable ex) {
				return Either.fail(ex);
			}
		}
		if (registration.singleton) {
			synchronized (this) {
				if (registration.promoted) {
					return Either.success(registration.instance);
				}
				Either<Object> tryInstance = tryResolveClass(registration.manifest);
				if (tryInstance.isPresent()) {
					if (tryInstance.value instanceof AutoCloseable) {
						closeables.add((AutoCloseable) tryInstance.value);
					}
					registration.promoteToSingleton(tryInstance.value);
				}
				return tryInstance;
			}
		}
		return tryResolveClass(registration.manifest);
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
		container.clear();
		for (AutoCloseable closable : closeables) {
			closable.close();
		}
	}
}

package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.extensibility.InstanceScope;
import org.revenj.patterns.ServiceLocator;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

final class SimpleContainer implements Container {

	private static class Registration<T> {
		public final Registration<T> parent;
		public final Type signature;
		public final SimpleContainer owner;
		public final Class<T> manifest;
		public T instance;
		public final Function<Container, T> singleFactory;
		public final BiFunction<Container, Type[], T> biFactory;
		public final InstanceScope lifetime;

		public final String name;

		private Registration(
				Registration<T> parent,
				Type signature,
				SimpleContainer owner,
				Class<T> manifest,
				T instance,
				Function<Container, T> singleFactory,
				BiFunction<Container, Type[], T> biFactory,
				InstanceScope lifetime) {
			this.parent = parent;
			this.signature = signature;
			this.owner = owner;
			this.manifest = manifest;
			this.instance = instance;
			this.singleFactory = singleFactory;
			this.biFactory = biFactory;
			this.lifetime = lifetime;

			this.name = signature.getTypeName();
		}

		static <T> Registration<T> register(Type signature, SimpleContainer owner, Class<T> manifest, InstanceScope lifetime) {
			return new Registration<>(null, signature, owner, manifest, null, null, null, lifetime);
		}

		static <T> Registration<T> register(Type signature, SimpleContainer owner, T instance, boolean singleton) {
			return new Registration<>(null, signature, owner, null, instance, null, null, singleton ? InstanceScope.SINGLETON : InstanceScope.CONTEXT);
		}

		static <T> Registration<T> register(Type signature, SimpleContainer owner, Function<Container, T> factory, InstanceScope lifetime) {
			return new Registration<>(null, signature, owner, null, null, factory, null, lifetime);
		}

		static <T> Registration<T> register(Type signature, SimpleContainer owner, BiFunction<Container, Type[], T> factory, InstanceScope lifetime) {
			return new Registration<>(null, signature, owner, null, null, null, factory, lifetime);
		}

		boolean promoted;
		boolean promoting;

		void promoteToSingleton(Object instance) {
			promoted = true;
			promoting = false;
			this.instance = (T) instance;
		}

		Registration<T> prepareSingleton(SimpleContainer caller) {
			Registration<T> registration = new Registration<>(this, signature, caller, manifest, null, singleFactory, biFactory, InstanceScope.CONTEXT);
			caller.addToRegistry(registration);
			return registration;
		}

		@Override
		public int hashCode() {
			return parent == null ? super.hashCode() : parent.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Registration) {
				Registration reg = (Registration)obj;
				Registration thisParent = parent != null ? parent : this;
				Registration regParent = reg.parent != null ? reg.parent : reg;
				return thisParent == regParent;
			}
			return false;
		}
	}

	private static final ConcurrentMap<Class<?>, CtorInfo[]> classCache = new ConcurrentHashMap<>();
	private static final ConcurrentMap<Type, TypeInfo> typeCache = new ConcurrentHashMap<>();
	private static final ConcurrentMap<String, Type> typeNameMappings = new ConcurrentHashMap<>();

	private static class TypeInfo {
		final CtorInfo[] constructors;
		final Class<?> rawClass;
		final Map<Type, Type> mappings = new HashMap<>();
		final Type[] genericArguments;
		final Type mappedType;
		final String name;

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
			name = type.toString();
			mappedType = typeNameMappings.get(name);
		}
	}

	private static class CtorInfo {

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
			return new Either<>(value, null);
		}

		static <T> Either<T> fail(final Throwable error) {
			return new Either<>(null, error);
		}

		static <T> Either<T> fail(final String error) {
			return new Either<>(null, new ReflectiveOperationException(error));
		}
	}

	private final Map<Type, List<Registration<?>>> container = new HashMap<>();
	private final SimpleContainer parent;
	private final boolean resolveUnknown;
	private boolean closed = false;

	private final CopyOnWriteArrayList<AutoCloseable> closeables = new CopyOnWriteArrayList<>();

	SimpleContainer(boolean resolveUnknown) {
		parent = null;
		this.resolveUnknown = resolveUnknown;
		registerGenerics(
				Optional.class,
				(locator, args) -> {
					try {
						return Optional.ofNullable(locator.resolve(args[0]));
					} catch (ReflectiveOperationException ignore) {
						return Optional.empty();
					}
				},
				InstanceScope.TRANSIENT
		);
		registerGenerics(Callable.class, (locator, args) -> () -> locator.resolve(args[0]), InstanceScope.TRANSIENT);
		registerInstance(Container.class, this, false);
		registerInstance(ServiceLocator.class, this, false);
	}

	private SimpleContainer(SimpleContainer parent) {
		this.parent = parent;
		this.resolveUnknown = parent.resolveUnknown;
		registerInstance(Container.class, this, false);
		registerInstance(ServiceLocator.class, this, false);
	}

	private Either<Object> tryResolveClass(Class<?> manifest, SimpleContainer caller) {
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
				Either<Object> arg = tryResolve(p, caller);
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
		if (constructors.length == 0) {
			try {
				Object instance = manifest.newInstance();
				return Either.success(instance);
			} catch (Exception ignore) {
			}
		}
		return error == null
				? Either.fail("Unable to find constructors for: " + manifest)
				: Either.fail(error);
	}

	private Either<Object> tryResolveType(ParameterizedType type, SimpleContainer caller) {
		TypeInfo typeInfo = typeCache.get(type);
		if (typeInfo == null) {
			typeInfo = new TypeInfo(type);
			typeCache.putIfAbsent(type, typeInfo);
		}
		if (typeInfo.rawClass == null) {
			return Either.fail(type + " is not an instance of Class<?> and cannot be resolved");
		}
		Registration<?> registration = getRegistration(typeInfo.rawClass);
		if (registration != null && registration.biFactory != null && typeInfo.genericArguments != null) {
			try {
				Object result = registration.biFactory.apply(caller, typeInfo.genericArguments);
				return Either.success(result);
			} catch (Exception ex) {
				return Either.fail(ex);
			}
		} else if (typeInfo.constructors.length == 0 && typeInfo.mappedType != null) {
			return tryResolve(typeInfo.mappedType, caller);
		}
		Map<Type, Type> mappings = typeInfo.mappings;
		return tryResolveTypeFrom(typeInfo, mappings, caller);
	}

	private Either<Object> tryResolveTypeFrom(TypeInfo typeInfo, Map<Type, Type> mappings, SimpleContainer caller) {
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
						args[i] = tryResolve(nestedInfo.genericArguments[0], caller);
					} else {
						Map<Type, Type> nestedMappings = new HashMap<>(typeInfo.mappings);
						for (Map.Entry<Type, Type> entry : nestedInfo.mappings.entrySet()) {
							Type parentValue = nestedMappings.get(entry.getValue());
							nestedMappings.put(entry.getKey(), parentValue != null ? parentValue : entry.getValue());
						}
						Either<Object> arg = tryResolveTypeFrom(nestedInfo, nestedMappings, caller);
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
					Either<Object> arg = tryResolve(p, caller);
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
			return parent != null ? parent.getRegistration(type) : null;
		}
		return registrations.get(registrations.size() - 1);
	}

	@Override
	public Object resolve(Type type) throws ReflectiveOperationException {
		if (closed) {
			if (parent != null) {
				return parent.resolve(type);
			}
			throw new ReflectiveOperationException("Container has been closed");
		}
		Either<Object> found = tryResolve(type, this);
		if (found.hasError()) {
			if (found.error instanceof ReflectiveOperationException) {
				throw (ReflectiveOperationException) found.error;
			}
			throw new ReflectiveOperationException("Unable to resolve: " + type + ". Reason: " + found.error.getMessage(), found.error);
		}
		return found.value;
	}

	public Either<Object> tryResolve(Type type, SimpleContainer caller) {
		Registration<?> registration = getRegistration(type);
		if (registration == null) {
			Type basicType = typeNameMappings.get(type.toString());
			if (basicType != null) {
				registration = getRegistration(basicType);
				if (registration != null) {
					return resolveRegistration(registration, caller);
				}
			}
			if (type instanceof ParameterizedType) {
				return tryResolveType((ParameterizedType) type, caller);
			} else if (type instanceof GenericArrayType) {
				GenericArrayType gat = (GenericArrayType) type;
				if (gat.getGenericComponentType() instanceof Class<?>) {
					return tryResolveCollection((Class<?>) gat.getGenericComponentType(), gat.getGenericComponentType(), caller);
				} else if (gat.getGenericComponentType() instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) gat.getGenericComponentType();
					if (pt.getRawType() instanceof Class<?>) {
						return tryResolveCollection((Class<?>) pt.getRawType(), pt, caller);
					}
				}
			}
			if (type instanceof Class<?> == false) {
				return Either.fail(type + " is not an instance of Class<?> and cannot be resolved since it's not registered in the container.");
			}
			Class<?> target = (Class<?>) type;
			if (target.isArray()) {
				return tryResolveCollection(target.getComponentType(), target.getComponentType(), caller);
			}
			if (resolveUnknown) {
				if (target.isInterface()) {
					return Either.fail(type + " is not an class and cannot be resolved since it's not registered in the container.\n" +
							"Try resolving implementation instead.");
				}
				return tryResolveClass(target, caller);
			}
			return target.isInterface()
					? Either.fail(type + " is not registered in the container.\n" +
					"Since " + type + " is an interface, it must be registered into the container.")
					: Either.fail(type + " is not registered in the container.\n" +
					"If you wish to resolve types not registered in the container, specify revenj.resolveUnknown=true in Properties configuration.");
		}
		if (registration.biFactory != null && type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			TypeInfo typeInfo = typeCache.get(type);
			if (typeInfo == null) {
				typeInfo = new TypeInfo(pt);
				typeCache.putIfAbsent(type, typeInfo);
			}
			if (typeInfo.genericArguments != null) {
				try {
					Object result = registration.biFactory.apply(caller, typeInfo.genericArguments);
					return Either.success(result);
				} catch (Exception ex) {
					return Either.fail(ex);
				}
			}
		}
		return resolveRegistration(registration, caller);
	}

	private Either<Object> tryResolveCollection(Class<?> container, Type element, SimpleContainer caller) {
		LinkedHashSet<Registration<?>> registrations = new LinkedHashSet<>();
		SimpleContainer current = caller;
		do {
			List<Registration<?>> found = current.container.get(element);
			if (found != null) {
				registrations.addAll(found);
			}
			current = current.parent;
		} while (current != null);
		if (registrations.isEmpty()) {
			return Either.success(Array.newInstance(container, 0));
		}
		Object[] result = (Object[]) Array.newInstance(container, registrations.size());
		Iterator<Registration<?>> iter = registrations.iterator();
		int i = 0;
		while (iter.hasNext()) {
			Registration<?> it = iter.next();
			Either<Object> item = resolveRegistration(it, caller);
			if (item.isPresent()) {
				result[i++] = item.value;
			} else {
				String message = item.error.getMessage();
				if (message == null && item.error.getCause() != null) {
					message = item.error.getCause().getMessage();
				}
				return Either.fail(new ReflectiveOperationException("Unable to resolve " + it.signature + ". Error: " + message, item.error));
			}
		}
		return Either.success(result);
	}

	private Either<Object> resolveRegistration(Registration<?> registration, SimpleContainer caller) {
		if (registration.instance != null) {
			return Either.success(registration.instance);
		} else if (registration.singleFactory != null) {
			try {
				//TODO match registration owner and caller
				Object instance;
				if (registration.lifetime != InstanceScope.TRANSIENT) {
					final SimpleContainer self = registration.lifetime == InstanceScope.SINGLETON
							? registration.owner
							: registration.owner == caller
							? this
							: caller;
					final Registration<?> reg = registration.lifetime == InstanceScope.SINGLETON || registration.owner == caller
							? registration
							: registration.prepareSingleton(caller);
					synchronized (self) {
						if (reg.promoted) {
							return Either.success(reg.instance);
						} else if (reg.promoting) {
							return Either.fail("Unable to resolve: " + registration.signature + ". Circular dependencies in signature detected");
						}
						reg.promoting = true;
						instance = reg.singleFactory.apply(self);
						if (instance instanceof AutoCloseable) {
							self.closeables.add((AutoCloseable) instance);
						}
						reg.promoteToSingleton(instance);
					}
				} else {
					instance = registration.singleFactory.apply(this);
				}
				return Either.success(instance);
			} catch (Throwable ex) {
				return Either.fail(ex);
			}
		}
		if (registration.lifetime != InstanceScope.TRANSIENT) {
			final SimpleContainer self = registration.lifetime == InstanceScope.SINGLETON
					? registration.owner
					: registration.owner == caller
					? this
					: caller;
			final Registration<?> reg = registration.lifetime == InstanceScope.SINGLETON || registration.owner == caller
					? registration
					: registration.prepareSingleton(caller);
			synchronized (self) {
				if (reg.promoted) {
					return Either.success(reg.instance);
				} else if (reg.promoting) {
					return Either.fail("Unable to resolve: " + registration.signature + ". Circular dependencies in signature detected");
				} else if (reg.manifest == null) {
					return Either.fail("Unable to resolve: " + registration.signature);
				}
				reg.promoting = true;
				Either<Object> tryInstance = self.tryResolveClass(reg.manifest, self);
				if (tryInstance.isPresent()) {
					if (tryInstance.value instanceof AutoCloseable) {
						self.closeables.add((AutoCloseable) tryInstance.value);
					}
					reg.promoteToSingleton(tryInstance.value);
				}
				return tryInstance;
			}
		}
		return tryResolveClass(registration.manifest, caller);
	}

	private synchronized void addToRegistry(Registration registration) {
		List<Registration<?>> registrations = container.get(registration.signature);
		typeNameMappings.put(registration.name, registration.signature);
		if (registrations == null) {
			registrations = new CopyOnWriteArrayList<>();
			registrations.add(registration);
			container.put(registration.signature, registrations);
		} else {
			registrations.add(registration);
		}
	}

	@Override
	public void registerType(Type type, Class<?> manifest, InstanceScope lifetime) {
		addToRegistry(Registration.register(type, this, manifest, lifetime));
	}

	@Override
	public void registerInstance(Type type, Object service, boolean handleClose) {
		if (handleClose && service instanceof AutoCloseable) {
			closeables.add((AutoCloseable) service);
		}
		addToRegistry(Registration.register(type, this, service, false));
	}

	@Override
	public void registerFactory(Type type, Function<Container, ?> factory, InstanceScope lifetime) {
		addToRegistry(Registration.register(type, this, factory, lifetime));
	}

	@Override
	public <T> void registerGenerics(Class<T> container, BiFunction<Container, Type[], T> factory, InstanceScope lifetime) {
		addToRegistry(Registration.register(container, this, factory, lifetime));
	}

	@Override
	public Container createScope() {
		return new SimpleContainer(this);
	}

	@Override
	public void close() throws Exception {
		closed = true;
		container.clear();
		for (AutoCloseable closable : closeables) {
			closable.close();
		}
		closeables.clear();
	}
}

package org.revenj.server.commands;

import org.revenj.patterns.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class Utility {

	private static class GenericType implements ParameterizedType {

		private final Type raw;
		private final Type[] arguments;

		public GenericType(Type raw, Class<?> argument) {
			this.raw = raw;
			this.arguments = new Type[]{argument};
		}

		@Override
		public Type[] getActualTypeArguments() {
			return arguments;
		}

		@Override
		public Type getRawType() {
			return raw;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

		@Override
		public String toString() {
			return raw.getTypeName() + "<" + arguments[0].getTypeName() + ">";
		}
	}

	public static ParameterizedType makeGenericType(Class<?> container, Class<?> argument) {
		return new GenericType(container, argument);
	}

	public static Repository resolveRepository(ServiceLocator locator, Class<?> manifest) throws ReflectiveOperationException {
		return (Repository) locator.resolve(new GenericType(Repository.class, manifest));
	}

	public static SearchableRepository resolveSearchRepository(ServiceLocator locator, Class<?> manifest) throws ReflectiveOperationException {
		return (SearchableRepository) locator.resolve(new GenericType(SearchableRepository.class, manifest));
	}

	public static PersistableRepository resolvePersistableRepository(ServiceLocator locator, Class<?> manifest) throws ReflectiveOperationException {
		return (PersistableRepository) locator.resolve(new GenericType(PersistableRepository.class, manifest));
	}

	public static DomainEventStore resolveEventStore(ServiceLocator locator, Class<?> manifest) throws ReflectiveOperationException {
		return (DomainEventStore) locator.resolve(new GenericType(DomainEventStore.class, manifest));
	}
}

package org.revenj.server.commands;

import org.revenj.Utils;
import org.revenj.patterns.*;

public abstract class Utility {

	public static Repository resolveRepository(ServiceLocator locator, Class<?> manifest) throws ReflectiveOperationException {
		return (Repository) locator.resolve(Utils.makeGenericType(Repository.class, manifest));
	}

	public static SearchableRepository resolveSearchRepository(ServiceLocator locator, Class<?> manifest) throws ReflectiveOperationException {
		return (SearchableRepository) locator.resolve(Utils.makeGenericType(SearchableRepository.class, manifest));
	}

	public static PersistableRepository resolvePersistableRepository(ServiceLocator locator, Class<?> manifest) throws ReflectiveOperationException {
		return (PersistableRepository) locator.resolve(Utils.makeGenericType(PersistableRepository.class, manifest));
	}

	public static DomainEventStore resolveEventStore(ServiceLocator locator, Class<?> manifest) throws ReflectiveOperationException {
		return (DomainEventStore) locator.resolve(Utils.makeGenericType(DomainEventStore.class, manifest));
	}
}

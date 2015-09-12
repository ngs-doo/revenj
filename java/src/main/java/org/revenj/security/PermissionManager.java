package org.revenj.security;

import org.revenj.patterns.DataSource;
import org.revenj.patterns.Query;
import org.revenj.patterns.Specification;

import java.io.Closeable;
import java.lang.reflect.Array;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

public interface PermissionManager {
	boolean canAccess(String identifier, Principal user);

	default boolean canAccess(Class<?> manifest, Principal user) {
		return canAccess(manifest.getTypeName(), user);
	}

	<T extends DataSource> Query<T> applyFilters(Class<T> manifest, Principal user, Query<T> data);

	<T extends DataSource> Collection<T> applyFilters(Class<T> manifest, Principal user, Collection<T> data);

	<T extends DataSource> Closeable registerFilter(Class<T> manifest, Specification<T> filter, String role, boolean inverse);
}

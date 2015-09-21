package org.revenj.security;

import org.revenj.patterns.Query;
import org.revenj.patterns.Specification;
import org.revenj.patterns.DataSource;

import java.io.Closeable;
import java.util.List;

public interface PermissionManager {
	boolean canAccess(String identifier, Principal user);

	default boolean canAccess(Class<?> manifest, Principal user) {
		return canAccess(manifest.getTypeName(), user);
	}

	<T extends DataSource> Query<T> applyFilters(Class<T> manifest, Principal user, Query<T> data);

	<T extends DataSource> List<T> applyFilters(Class<T> manifest, Principal user, List<T> data);

	<T extends DataSource> Closeable registerFilter(Class<T> manifest, Specification<T> filter, String role, boolean inverse);

	default <T extends DataSource> Closeable registerForRole(Class<T> manifest, Specification<T> filter, String role) {
		return registerFilter(manifest, filter, role, false);
	}

	default <T extends DataSource> Closeable registerWhenNotInRole(Class<T> manifest, Specification<T> filter, String role) {
		return registerFilter(manifest, filter, role, true);
	}
}

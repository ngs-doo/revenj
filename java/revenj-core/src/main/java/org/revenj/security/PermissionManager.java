package org.revenj.security;

import org.revenj.patterns.Query;
import org.revenj.patterns.Specification;
import org.revenj.patterns.DataSource;

import java.io.Closeable;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

public interface PermissionManager {
	boolean canAccess(String identifier, Principal user);

	default boolean canAccess(Class<?> manifest, Principal user) {
		return canAccess(manifest.getTypeName(), user);
	}

	<T extends DataSource, S extends T> Query<S> applyFilters(Class<T> manifest, Principal user, Query<S> data);

	default <T extends DataSource, S extends T> Query<S> applyFilters(Class<T> manifest, Query<S> data) {
		return applyFilters(manifest, boundPrincipal.get(), data);
	}

	<T extends DataSource, S extends T> List<S> applyFilters(Class<T> manifest, Principal user, List<S> data);

	default <T extends DataSource, S extends T> List<S> applyFilters(Class<T> manifest, List<S> data) {
		return applyFilters(manifest, boundPrincipal.get(), data);
	}

	<T extends DataSource> Closeable registerFilter(Class<T> manifest, Specification<T> filter, String role, boolean inverse);

	default <T extends DataSource> Closeable registerForRole(Class<T> manifest, Specification<T> filter, String role) {
		return registerFilter(manifest, filter, role, false);
	}

	default <T extends DataSource> Closeable registerWhenNotInRole(Class<T> manifest, Specification<T> filter, String role) {
		return registerFilter(manifest, filter, role, true);
	}

	ThreadLocal<Principal> boundPrincipal = new ThreadLocal<>();

	static boolean implies(String role) {
		Principal principal = boundPrincipal.get();
		if (principal instanceof UserPrincipal) {
			return ((UserPrincipal) principal).implies(role);
		}
		return principal != null && Objects.equals(role, principal.getName());
	}
}

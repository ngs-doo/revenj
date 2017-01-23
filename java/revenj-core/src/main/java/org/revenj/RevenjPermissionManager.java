package org.revenj;

import org.revenj.patterns.*;
import org.revenj.security.PermissionManager;
import org.revenj.security.GlobalPermission;
import org.revenj.security.RolePermission;
import org.revenj.security.UserPrincipal;
import rx.Observable;
import rx.Subscription;

import java.io.Closeable;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RevenjPermissionManager implements PermissionManager, Closeable {

	private final Callable<Optional<SearchableRepository<GlobalPermission>>> globalRepository;
	private final Callable<Optional<SearchableRepository<RolePermission>>> rolesRepository;
	private final boolean defaultPermissions;

	private final Subscription globalSubscription;
	private final Subscription roleSubscription;

	private Map<String, Boolean> globalPermissions = new HashMap<>();
	private Map<String, List<Pair>> rolePermissions = new HashMap<>();

	private Map<String, Boolean> cache = new HashMap<>();
	private final Map<Class<?>, List<Filter>> registeredFilters = new HashMap<>();

	private final class Pair {
		public final String name;
		public final boolean isAllowed;

		public Pair(String name, boolean isAllowed) {
			this.name = name;
			this.isAllowed = isAllowed;
		}
	}

	private final class Filter<T> {
		public final Specification<T> specification;
		public final String role;
		public final boolean inverse;

		public Filter(Specification<T> specification, String role, boolean inverse) {
			this.specification = specification;
			this.role = role;
			this.inverse = inverse;
		}
	}

	private boolean permissionsChanged = true;

	public RevenjPermissionManager(ServiceLocator locator) {
		this(locator.resolve(Properties.class),
				new Generic<Observable<Callable<GlobalPermission>>>() {
				}.resolve(locator),
				new Generic<Observable<Callable<RolePermission>>>() {
				}.resolve(locator),
				new Generic<Callable<Optional<SearchableRepository<GlobalPermission>>>>() {
				}.resolve(locator),
				new Generic<Callable<Optional<SearchableRepository<RolePermission>>>>() {
				}.resolve(locator));
	}

	public RevenjPermissionManager(
			Properties properties,
			Observable<Callable<GlobalPermission>> globalChanges,
			Observable<Callable<RolePermission>> roleChanges,
			Callable<Optional<SearchableRepository<GlobalPermission>>> globalRepository,
			Callable<Optional<SearchableRepository<RolePermission>>> rolesRepository) {
		String permissions = properties.getProperty("revenj.permissions");
		if (permissions != null && permissions.length() > 0) {
			if (!permissions.equalsIgnoreCase("open") && !permissions.equalsIgnoreCase("closed")) {
				throw new RuntimeException("Invalid revenj.permission settings found: '" + permissions + "'.\n"
						+ "Allowed values are open and closed");
			}
		}
		defaultPermissions = permissions == null || "open".equals(permissions);
		globalSubscription = globalChanges.subscribe(c -> permissionsChanged = true);
		roleSubscription = roleChanges.subscribe(c -> permissionsChanged = true);
		this.globalRepository = globalRepository;
		this.rolesRepository = rolesRepository;
	}

	private void checkPermissions() {
		if (!permissionsChanged) {
			return;
		}
		Optional<SearchableRepository<GlobalPermission>> global;
		Optional<SearchableRepository<RolePermission>> roles;
		try {
			global = globalRepository.call();
			roles = rolesRepository.call();
		} catch (Exception ignore) {
			global = Optional.empty();
			roles = Optional.empty();
		}
		if (global.isPresent()) {
			globalPermissions =
					global.get().search().stream().collect(
							Collectors.toMap(GlobalPermission::getName, GlobalPermission::getIsAllowed));
		}
		if (roles.isPresent()) {
			rolePermissions =
					roles.get().search().stream().collect(
							Collectors.groupingBy(
									RolePermission::getName,
									Collectors.mapping(it -> new Pair(it.getRoleID(), it.getIsAllowed()), Collectors.toList())));
		}
		cache = new HashMap<>();
		permissionsChanged = false;
	}

	private boolean checkOpen(String[] parts, int len) {
		if (len < 0) {
			return defaultPermissions;
		}
		String name = String.join(".", Arrays.copyOf(parts, len));
		Boolean found = globalPermissions.get(name);
		return found != null ? found : checkOpen(parts, len - 1);
	}

	private boolean implies(Principal principal, String role) {
		return principal instanceof UserPrincipal
				? ((UserPrincipal) principal).implies(role)
				: role.equals(principal.getName());
	}

	@Override
	public boolean canAccess(String identifier, Principal user) {
		checkPermissions();
		String target = identifier != null ? identifier : "";
		String id = user != null ? user.getName() + ":" + target : target;
		Boolean exists = cache.get(id);
		if (exists != null) {
			return exists;
		}
		String[] parts = target.split("\\.");
		boolean isAllowed = checkOpen(parts, parts.length);
		if (user != null) {
			List<Pair> permissions;
			for (int i = parts.length; i >= 0; i--) {
				String subName = String.join(".", Arrays.copyOf(parts, i));
				permissions = rolePermissions.get(subName);
				if (permissions != null) {
					Optional<Pair> found = permissions.stream().filter(it -> implies(user, it.name)).findFirst();
					if (found.isPresent()) {
						isAllowed = found.get().isAllowed;
						break;
					}
				}
			}
		}
		Map<String, Boolean> newCache = new HashMap<>(cache);
		newCache.put(id, isAllowed);
		cache = newCache;
		return isAllowed;
	}

	@Override
	public <T, S extends T> Query<S> applyFilters(Class<T> manifest, Principal user, Query<S> data) {
		if (user == null) return data.filter(it -> defaultPermissions);
		List<Filter> registered = registeredFilters.get(manifest);
		if (registered != null) {
			Query<S> result = data;
			for (Filter r : registered) {
				if ((implies(user, r.role)) != r.inverse) {
					result = result.filter(r.specification);
				}
			}
			return result;
		}
		return data;
	}

	@Override
	public <T, S extends T> List<S> applyFilters(Class<T> manifest, Principal user, List<S> data) {
		if (user == null) return defaultPermissions ? data : Collections.EMPTY_LIST;
		List<Filter> registered = registeredFilters.get(manifest);
		if (registered != null) {
			Stream<S> result = data.stream();
			boolean filtered = false;
			for (Filter r : registered) {
				if (implies(user, r.role) != r.inverse) {
					result = result.filter(r.specification);
					filtered = true;
				}
			}
			return filtered ? result.collect(Collectors.toList()) : data;
		}
		return data;
	}

	@Override
	public <T> Closeable registerFilter(Class<T> manifest, Specification<T> filter, String role, boolean inverse) {
		List<Filter> registered = registeredFilters.get(manifest);
		if (registered == null) {
			registered = new ArrayList<>();
			registeredFilters.put(manifest, registered);
		}
		Filter item = new Filter(filter, role, inverse);
		List<Filter> reg = registered;
		reg.add(item);
		return () -> reg.remove(item);
	}

	public void close() {
		globalSubscription.unsubscribe();
		roleSubscription.unsubscribe();
	}
}

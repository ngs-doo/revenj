package org.revenj.security;

import java.security.Principal;
import java.util.Set;
import java.util.function.Function;

public final class UserPrincipal implements Principal {

	private final String name;
	private final Function<String, Boolean> isInRole;

	public UserPrincipal(String name, Function<String, Boolean> isInRole) {
		this.name = name;
		this.isInRole = isInRole;
	}

	public UserPrincipal(String name, Set<String> roles) {
		this.name = name;
		this.isInRole = roles::contains;
	}

	@Override
	public String getName() {
		return name;
	}

	public boolean implies(String role) {
		return name.equals(role) || isInRole.apply(role);
	}
}

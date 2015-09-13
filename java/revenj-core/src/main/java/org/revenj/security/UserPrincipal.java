package org.revenj.security;

import java.security.Principal;
import java.util.Set;

public final class UserPrincipal implements Principal {

	private final String name;
	private final Set<String> roles;

	public UserPrincipal(String name) {
		this.name = name;
		this.roles = null;
	}

	public UserPrincipal(String name, Set<String> roles) {
		this.name = name;
		this.roles = roles;
	}

	@Override
	public String getName() {
		return name;
	}
}

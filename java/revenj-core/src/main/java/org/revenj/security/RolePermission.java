package org.revenj.security;

import org.revenj.patterns.DataSource;

//TODO: remove DataSource signature
public interface RolePermission extends DataSource {
	String getName();

	String getRoleID();

	boolean getIsAllowed();
}

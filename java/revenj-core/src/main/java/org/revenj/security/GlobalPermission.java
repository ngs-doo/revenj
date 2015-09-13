package org.revenj.security;

import org.revenj.patterns.DataSource;

//TODO: remove DataSource signature
public interface GlobalPermission extends DataSource {
	String getName();

	boolean getIsAllowed();
}

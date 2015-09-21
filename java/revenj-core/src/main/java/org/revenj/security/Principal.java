package org.revenj.security;

public interface Principal extends java.security.Principal {
	boolean implies(String role);
}

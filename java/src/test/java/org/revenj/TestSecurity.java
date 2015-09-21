package org.revenj;

import gen.model.Boot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.ServiceLocator;
import org.revenj.security.PermissionManager;
import org.revenj.security.Principal;
import org.revenj.security.UserPrincipal;

import java.io.IOException;
import java.util.HashSet;

public class TestSecurity {

	private Container container;

	@Before
	public void initContainer() throws IOException {
		container = (Container) Boot.configure("jdbc:postgresql://localhost/revenj");
	}

	@After
	public void closeContainer() throws Exception {
		container.close();
	}

	@Test
	public void simpleAccessCheck() throws Exception {
		ServiceLocator locator = container;
		PermissionManager security = locator.resolve(PermissionManager.class);
		Principal user = new UserPrincipal("user", new HashSet<>());
		Assert.assertTrue(security.canAccess("test", user));
	}
}

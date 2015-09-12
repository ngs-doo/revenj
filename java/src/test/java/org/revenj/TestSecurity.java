package org.revenj;

import gen.model.Boot;
import gen.model.test.Composite;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.DataChangeNotification;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.ServiceLocator;
import org.revenj.security.PermissionManager;
import org.revenj.security.UserPrincipal;

import java.io.Closeable;
import java.io.IOException;
import java.security.Principal;

public class TestSecurity {

	private Container container;

	@Before
	public void initContainer() throws IOException {
		container = (Container) Boot.configure("jdbc:postgresql://localhost:5432/revenj");
	}

	@After
	public void closeContainer() throws Exception {
		container.close();
	}

	@Test
	public void simpleAccessCheck() throws Exception {
		ServiceLocator locator = container;
		PermissionManager security = locator.resolve(PermissionManager.class);
		Principal user = new UserPrincipal("user");
		Assert.assertTrue(security.canAccess("test", user));
	}
}

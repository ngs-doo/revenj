package org.revenj;

import gen.model.mixinReference.UserFilter;
import gen.model.mixinReference.repositories.UserFilterRepository;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.ServiceLocator;
import org.revenj.security.PermissionManager;
import org.revenj.security.UserPrincipal;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class TestSecurity extends Setup {

	@Test
	public void simpleDefaultAccessCheck() throws Exception {
		ServiceLocator locator = container;
		PermissionManager security = locator.resolve(PermissionManager.class);
		Principal user = new UserPrincipal("user", new HashSet<>());
		Assert.assertFalse(security.canAccess("test", user));
	}

	@Test
	public void simpleOpenAccessCheck() throws Exception {
		Properties props = new Properties();
		props.setProperty("revenj.permissions", "open");
		Container locator = container(props);
		PermissionManager security = locator.resolve(PermissionManager.class);
		Principal user = new UserPrincipal("user", new HashSet<>());
		Assert.assertTrue(security.canAccess("test", user));
		container.close();
	}

	@Test
	public void repositoryFilterCheck() throws Exception {
		ServiceLocator locator = container;
		Principal current = PermissionManager.boundPrincipal.get();
		UserFilterRepository repository = locator.resolve(UserFilterRepository.class);
		repository.delete(repository.search());
		List<UserFilter> found = repository.search();
		Assert.assertEquals(0, found.size());
		repository.insert(new UserFilter().setName("test"));
		found = repository.search();
		Assert.assertEquals(1, found.size());
		PermissionManager.boundPrincipal.set(new UserPrincipal("test", Collections.singleton("RegularUser")));
		found = repository.search(it -> true);
		Assert.assertEquals(1, found.size());
		PermissionManager.boundPrincipal.set(new UserPrincipal("test1", Collections.singleton("RegularUser")));
		found = repository.search(it -> true);
		Assert.assertEquals(0, found.size());

		PermissionManager.boundPrincipal.set(current);
	}
}

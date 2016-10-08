package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.DataSource;
import org.revenj.security.PermissionManager;
import org.revenj.security.UserPrincipal;

import java.io.Closeable;
import java.security.Principal;
import java.util.*;

public class TestSecurity {

	@Test
	public void simpleRoleCheck() throws Exception {
		Set<String> roles = Collections.singleton("role");
		UserPrincipal user = new UserPrincipal("user", roles::contains);
		Assert.assertTrue(user.implies("user"));
		Assert.assertTrue(user.implies("role"));
		Assert.assertFalse(user.implies("role1"));
	}

	class Model implements DataSource {
		public final int i;

		public Model(int i) {
			this.i = i;
		}
	}

	@Test
	public void filterCheck() throws Exception {
		Principal user1 = new UserPrincipal("user1", Collections.singleton("role"));
		Principal user2 = new UserPrincipal("user2", Collections.singleton("not-in-role"));
		Properties properties = new Properties();
		properties.setProperty("revenj.notifications.status", "disabled");
		Container container = Revenj.setup(null, properties, Optional.<ClassLoader>empty(), null);
		PermissionManager permissions = container.resolve(PermissionManager.class);
		List<Model> values = Arrays.asList(new Model(1), new Model(5), new Model(7));

		List<Model> filtered1 = permissions.applyFilters(Model.class, user1, values);
		List<Model> filtered2 = permissions.applyFilters(Model.class, user2, values);

		Assert.assertEquals(3, filtered1.size());
		Assert.assertEquals(3, filtered2.size());

		Closeable reg1 = permissions.registerWhenNotInRole(Model.class, it -> it.i > 5, "role");
		Closeable reg2 = permissions.registerForRole(Model.class, it -> it.i == 5, "role");

		filtered1 = permissions.applyFilters(Model.class, user1, values);
		filtered2 = permissions.applyFilters(Model.class, user2, values);

		Assert.assertEquals(1, filtered1.size());
		Assert.assertEquals(values.get(1), filtered1.get(0));
		Assert.assertEquals(1, filtered2.size());
		Assert.assertEquals(values.get(2), filtered2.get(0));

		reg1.close();
		reg2.close();

		filtered1 = permissions.applyFilters(Model.class, user1, values);
		filtered2 = permissions.applyFilters(Model.class, user2, values);

		Assert.assertEquals(3, filtered1.size());
		Assert.assertEquals(3, filtered2.size());
	}
}

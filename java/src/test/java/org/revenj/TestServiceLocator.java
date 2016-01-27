package org.revenj;

import gen.model.Boot;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.revenj.extensibility.Container;
import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.QueryProvider;

import java.util.Properties;

@RunWith(Parameterized.class)
public class TestServiceLocator {
	@Parameters
	public static Object[][] data() {
		return new Object[][]{{"foo", "bar" }, {"revenj.resolveUnknown", "true" }};
	}

	private final String key;
	private final String value;

	public TestServiceLocator(final String key, final String value) {
		this.key = key;
		this.value = value;
	}

	@Test
	public void canResolveQueryProvider() throws Exception {
		final Properties props = new Properties();
		props.put(key, value);
		java.io.File revProps = new java.io.File("revenj.properties");
		if (revProps.exists() && revProps.isFile()) {
			props.load(new java.io.FileReader(revProps));
		}

		final ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost/revenj", props);
		try {
			Assert.assertNotNull(locator.resolve(QueryProvider.class));
		} finally {
			((Container) locator).close();
		}
	}
}

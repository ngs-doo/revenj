package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.revenj.database.postgres.QueryProvider;

import java.util.Properties;

@RunWith(Parameterized.class)
public class TestServiceLocator extends Setup {
	@Parameters
	public static Object[][] data() {
		return new Object[][]{{"foo", "bar"}, {"revenj.resolveUnknown", "true"}};
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

		Assert.assertNotNull(container.resolve(QueryProvider.class));
	}
}

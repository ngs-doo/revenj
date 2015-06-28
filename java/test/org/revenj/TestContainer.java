package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.ServiceLocator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class TestContainer {

	@Test
	public void initialSetup() throws IOException, SQLException {
		ServiceLocator locator = Revenj.setup("jdbc:postgresql://localhost:5432/revenj");
		java.util.List<org.revenj.postgres.ObjectConverter.ColumnInfo> columns = new java.util.ArrayList<>();
		try (Connection connection = locator.resolve(Connection.class)) {
			Assert.assertNotNull(connection);
		}
	}
}

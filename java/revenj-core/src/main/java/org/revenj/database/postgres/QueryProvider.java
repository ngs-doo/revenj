package org.revenj.database.postgres;

import org.revenj.patterns.DataSource;
import org.revenj.patterns.Query;
import org.revenj.patterns.ServiceLocator;

import java.sql.Connection;

public interface QueryProvider {
    <T extends DataSource> Query<T> query(Connection connection, ServiceLocator locator, Class<T> manifest);
}

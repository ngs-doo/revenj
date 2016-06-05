package org.revenj.spring;

import org.revenj.patterns.DataSource;
import org.revenj.patterns.Query;
import org.revenj.patterns.ServiceLocator;
import org.revenj.database.postgres.QueryProvider;
import org.revenj.database.postgres.jinq.RevenjQueryComposer;
import org.revenj.database.postgres.jinq.RevenjQueryComposerCache;
import org.revenj.database.postgres.jinq.transform.MetamodelUtil;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;
import java.sql.SQLException;

final class JinqQueryProvider implements QueryProvider {
	private final MetamodelUtil metamodel;
	private final ClassLoader loader;
	private final javax.sql.DataSource dataSource;
	private final RevenjQueryComposerCache cachedQueries = new RevenjQueryComposerCache();

	public JinqQueryProvider(MetamodelUtil metamodel, ClassLoader loader, javax.sql.DataSource dataSource) {
		this.metamodel = metamodel;
		this.loader = loader;
		this.dataSource = dataSource;
	}

	public <T extends DataSource> Query<T> query(Connection connection, ServiceLocator locator, Class<T> manifest) {
		return RevenjQueryComposer.findAll(
				metamodel,
				loader,
				manifest,
				cachedQueries,
				connection,
				locator,
				this::getConnection,
				this::releaseConnection);
	}

	private Connection getConnection() throws SQLException {
		return DataSourceUtils.getConnection(dataSource);
	}

	private void releaseConnection(Connection connection) throws SQLException {
		DataSourceUtils.releaseConnection(connection, dataSource);
	}
}

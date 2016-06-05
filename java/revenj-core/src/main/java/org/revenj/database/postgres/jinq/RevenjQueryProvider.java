package org.revenj.database.postgres.jinq;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.revenj.patterns.Query;
import org.revenj.database.postgres.QueryProvider;
import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;

import org.revenj.patterns.DataSource;
import org.revenj.patterns.ServiceLocator;
import org.revenj.database.postgres.jinq.transform.MetamodelUtil;

final class RevenjQueryProvider implements QueryProvider {
	private final MetamodelUtil metamodel;
	private final ClassLoader loader;
	private final javax.sql.DataSource dataSource;
	private final RevenjQueryComposerCache cachedQueries = new RevenjQueryComposerCache();

	public RevenjQueryProvider(MetamodelUtil metamodel, ClassLoader loader, javax.sql.DataSource dataSource) {
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
				this::getFromDataSource,
				Connection::close);
	}

	private Connection getFromDataSource() throws SQLException {
		return dataSource.getConnection();
	}
}

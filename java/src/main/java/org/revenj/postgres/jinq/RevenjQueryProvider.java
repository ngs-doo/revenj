package org.revenj.postgres.jinq;

import java.sql.Connection;
import java.util.Optional;

import org.revenj.postgres.QueryProvider;
import org.revenj.postgres.jinq.jpqlquery.JPQLQuery;

import org.revenj.patterns.DataSource;
import org.revenj.patterns.Query;
import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.jinq.transform.MetamodelUtil;

public class RevenjQueryProvider implements QueryProvider {
    private final MetamodelUtil metamodel;
    private final RevenjQueryComposerCache cachedQueries = new RevenjQueryComposerCache();

    public RevenjQueryProvider(MetamodelUtil metamodel) {
        this.metamodel = metamodel;
    }

    public <T extends DataSource> Query<T> query(Connection connection, ServiceLocator locator, Class<T> dataSource) {
        String sqlSource = metamodel.dataSourceNameFromClass(dataSource);
        Optional<JPQLQuery<?>> cachedQuery = cachedQueries.findCachedFindAll(sqlSource);
        if (cachedQuery == null) {
            JPQLQuery<T> query = JPQLQuery.findAll(sqlSource);
            cachedQuery = Optional.of(query);
            cachedQuery = cachedQueries.cacheFindAll(sqlSource, cachedQuery);
        }
        JPQLQuery<T> query = (JPQLQuery<T>) cachedQuery.get();
        RevenjQueryComposer<T> queryComposer = RevenjQueryComposer.findAll(
                metamodel,
                dataSource,
                cachedQueries,
                connection,
                locator,
                query);
        return new RevenjQuery<T>(queryComposer);
    }
}

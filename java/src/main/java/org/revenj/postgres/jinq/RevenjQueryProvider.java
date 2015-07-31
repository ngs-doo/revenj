package org.revenj.postgres.jinq;

import java.sql.Connection;
import java.util.Optional;

import org.revenj.postgres.jinq.jpqlquery.JPQLQuery;
import org.revenj.postgres.jinq.transform.JPQLQueryTransformConfigurationFactory;
import org.revenj.postgres.jinq.transform.LambdaAnalysisFactory;

import org.revenj.patterns.DataSource;
import org.revenj.patterns.Query;
import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.jinq.transform.MetamodelUtil;

public class RevenjQueryProvider {
    final MetamodelUtil metamodel;
    final RevenjQueryComposerCache cachedQueries = new RevenjQueryComposerCache();
    final LambdaAnalysisFactory lambdaAnalyzer = new LambdaAnalysisFactory();
    final JPQLQueryTransformConfigurationFactory jpqlQueryTransformConfigurationFactory = new JPQLQueryTransformConfigurationFactory();

    public RevenjQueryProvider(MetamodelUtil metamodel) {
        this.metamodel = metamodel;
    }

    /**
     * Returns a stream of all the entities of a particular type in a
     * database.
     *
     * @param dataSource type of the entity
     * @return a stream of the results of querying the database for all
     * entities of the given type.
     */
    public <T extends DataSource> Query<T> query(final Connection connection, ServiceLocator locator, Class<T> dataSource) {
        String sqlSource = metamodel.dataSourceNameFromClass(dataSource);
        Optional<JPQLQuery<?>> cachedQuery = cachedQueries.findCachedFindAll(sqlSource);
        if (cachedQuery == null) {
            JPQLQuery<T> query = JPQLQuery.findAll(sqlSource);
            cachedQuery = Optional.of(query);
            cachedQuery = cachedQueries.cacheFindAll(sqlSource, cachedQuery);
        }
        JPQLQuery<T> query = (JPQLQuery<T>) cachedQuery.get();
        RevenjQueryComposer<T> queryComposer = RevenjQueryComposer.findAll(
                metamodel, dataSource, cachedQueries, lambdaAnalyzer, jpqlQueryTransformConfigurationFactory,
                connection, locator, query);
        return new RevenjQuery<T>(queryComposer);
    }
}

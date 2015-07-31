package org.revenj.postgres.jinq;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.revenj.postgres.jinq.jpqlquery.RowReader;
import org.revenj.postgres.jinq.jpqlquery.SelectFromWhere;
import org.revenj.postgres.jinq.transform.CountTransform;
import org.revenj.postgres.jinq.transform.DistinctTransform;
import org.revenj.postgres.jinq.transform.JPQLMultiLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.JPQLNoLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.JPQLOneLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.JPQLQueryTransformConfiguration;
import org.revenj.postgres.jinq.transform.JPQLQueryTransformConfigurationFactory;
import org.revenj.postgres.jinq.transform.LambdaAnalysis;
import org.revenj.postgres.jinq.transform.LambdaAnalysisFactory;
import org.revenj.postgres.jinq.transform.LambdaInfo;
import org.revenj.postgres.jinq.transform.LimitSkipTransform;
import org.revenj.postgres.jinq.transform.MetamodelUtil;
import org.revenj.postgres.jinq.transform.QueryTransformException;
import org.revenj.postgres.jinq.transform.SortingTransform;
import org.revenj.postgres.jinq.transform.WhereTransform;
import org.jinq.orm.internal.QueryComposer;
import org.jinq.orm.stream.JinqStream.AggregateGroup;
import org.jinq.orm.stream.JinqStream.JoinToIterable;
import org.jinq.orm.stream.JinqStream.Select;
import org.jinq.orm.stream.NextOnlyIterator;
import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple;
import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.jinq.jpqlquery.GeneratedQueryParameter;
import org.revenj.postgres.jinq.jpqlquery.JPQLQuery;

/**
 * Holds a query and can apply the logic for composing JPQL queries.
 * It mostly delegates the work to other objects, but this object
 * does manage caching of queries and substituting of parameters
 * into queries.
 *
 * @param <T>
 */
class RevenjQueryComposer<T> implements QueryComposer<T> {
    final MetamodelUtil metamodel;
    final RevenjQueryComposerCache cachedQueries;
    final JPQLQueryTransformConfigurationFactory jpqlQueryTransformConfigurationFactory;
    final Connection connection;
    final ServiceLocator locator;
    final JPQLQuery<T> query;
    final LambdaAnalysisFactory lambdaAnalyzer;
    final Class<T> manifest;

    /**
     * Holds the chain of lambdas that were used to create this query. This is needed
     * because query parameters (which are stored in the lambda objects) are only
     * substituted into the query during query execution, which occurs much later
     * than query generation.
     */
    List<LambdaInfo> lambdas = new ArrayList<>();

    private RevenjQueryComposer(RevenjQueryComposer<?> base, Class<T> manifest, JPQLQuery<T> query, List<LambdaInfo> chainedLambdas, LambdaInfo... additionalLambdas) {
        this(base.metamodel, manifest, base.cachedQueries, base.lambdaAnalyzer, base.jpqlQueryTransformConfigurationFactory, base.connection, base.locator, query, chainedLambdas, additionalLambdas);
    }

    private RevenjQueryComposer(MetamodelUtil metamodel, Class<T> manifest, RevenjQueryComposerCache cachedQueries, LambdaAnalysisFactory lambdaAnalyzer, JPQLQueryTransformConfigurationFactory jpqlQueryTransformConfigurationFactory, Connection connection, ServiceLocator locator, JPQLQuery<T> query, List<LambdaInfo> chainedLambdas, LambdaInfo... additionalLambdas) {
        this.metamodel = metamodel;
        this.manifest = manifest;
        this.cachedQueries = cachedQueries;
        this.lambdaAnalyzer = lambdaAnalyzer;
        this.jpqlQueryTransformConfigurationFactory = jpqlQueryTransformConfigurationFactory;
        this.connection = connection;
        this.locator = locator;
        this.query = query;
        lambdas.addAll(chainedLambdas);
        for (LambdaInfo newLambda : additionalLambdas)
            lambdas.add(newLambda);
    }

    public static <T> RevenjQueryComposer<T> findAll(
            MetamodelUtil metamodel,
            Class<T> manifest,
            RevenjQueryComposerCache cachedQueries,
            LambdaAnalysisFactory lambdaAnalyzer,
            JPQLQueryTransformConfigurationFactory jpqlQueryTransformConfigurationFactory,
            Connection conn,
            ServiceLocator locator,
            JPQLQuery<T> findAllQuery) {
        return new RevenjQueryComposer<>(metamodel, manifest, cachedQueries, lambdaAnalyzer, jpqlQueryTransformConfigurationFactory, conn, locator, findAllQuery, new ArrayList<>());
    }

    @Override
    public String getDebugQueryString() {
        return query.getQueryString();
    }

    private void fillQueryParameters(PreparedStatement ps, List<GeneratedQueryParameter> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            GeneratedQueryParameter param = parameters.get(i);
            if (param.fieldName == null) {
                ps.setObject(i + 1, lambdas.get(param.lambdaIndex).getCapturedArg(param.argIndex));
            } else {
                ps.setObject(i + 1, lambdas.get(param.lambdaIndex).getField(param.fieldName));
            }
        }
    }

    private ObjectConverter<T> getConverter() throws ReflectiveOperationException {
        return (ObjectConverter) locator.resolve(new GenericType(ObjectConverter.class, manifest));
    }

    private static class GenericType implements ParameterizedType {

        private final Type raw;
        private final Type[] arguments;

        public GenericType(Type raw, Class<?> argument) {
            this.raw = raw;
            this.arguments = new Type[]{argument};
        }

        @Override
        public Type[] getActualTypeArguments() {
            return arguments;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public String toString() {
            return raw.getTypeName() + "<" + arguments[0].getTypeName() + ">";
        }
    }

    public T executeAndGetSingleResult() {
        try {
            final String queryString = query.getQueryString();
            PreparedStatement ps = connection.prepareStatement(queryString);
            fillQueryParameters(ps, query.getQueryParameters());
            final RowReader<T> reader = query.getRowReader();
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ObjectConverter converter = getConverter();
                    PostgresReader pr = new PostgresReader(locator);
                    pr.process(rs.getString(1));
                    return reader.readResult(converter.from(pr));
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<T> executeAndReturnResultIterator(Consumer<Throwable> exceptionReporter) {
        try {
            final String queryString = query.getQueryString();
            PreparedStatement ps = connection.prepareStatement(queryString);
            fillQueryParameters(ps, query.getQueryParameters());
            final RowReader<T> reader = query.getRowReader();
            long skip = 0;
            long limit = Long.MAX_VALUE;
            if (query instanceof SelectFromWhere) {
                SelectFromWhere<?> sfw = (SelectFromWhere<?>) query;
                if (sfw.limit >= 0)
                    limit = sfw.limit;
                if (sfw.skip >= 0)
                    skip = sfw.skip;
            }
            final long initialOffset = skip;
            final ResultSet rs = ps.executeQuery();
            final ObjectConverter converter = getConverter();
            final PostgresReader pr = new PostgresReader(locator);

            // To handle the streaming of giant result sets, we will break
            // them down into pages. Technically, this is not really correct
            // because a database can return the results in different orders
            // and this is potentially slow depending on the underlying
            // database, but it helps us avoid running out of memory.
            return new NextOnlyIterator<T>() {
                boolean hasNextPage = false;
                Iterator<Object> resultIterator;
                int offset = (int) initialOffset;
                long totalRead = 0;

                @Override
                protected void generateNext() {
                    if (resultIterator == null) {
                        //if (offset > 0) ps.setFirstResult(offset);
                    }
                    try {
                        if (rs.next()) {
                            pr.process(rs.getString(1));
                            nextElement(reader.readResult(converter.from(pr)));
                        } else {
                            if (hasNextPage) {
                                hasNextPage = false;
                                resultIterator = null;
                                generateNext();
                            } else {
                                noMoreElements();
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long runCount() throws SQLException {
        final String queryString = query.getQueryString();
        PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM (" + queryString + ") sq");
        fillQueryParameters(ps, query.getQueryParameters());
        try (final ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    public boolean any() throws SQLException {
        final String queryString = query.getQueryString();
        PreparedStatement ps = connection.prepareStatement("SELECT EXISTS(" + queryString + ")");
        fillQueryParameters(ps, query.getQueryParameters());
        try (final ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        }
        return false;
    }

    //TODO: optimize
    public boolean all(Object lambda) throws SQLException {
        long filter = this.where(lambda).runCount();
        long all = this.runCount();
        return filter == all && all > 0;
    }

    public boolean none() throws SQLException {
        final String queryString = query.getQueryString();
        PreparedStatement ps = connection.prepareStatement("SELECT NOT EXISTS(" + queryString + ")");
        fillQueryParameters(ps, query.getQueryParameters());
        try (final ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        }
        return true;
    }

    public Optional<T> first() throws SQLException {
        final String queryString = query.getQueryString();
        PreparedStatement ps = connection.prepareStatement(queryString);
        fillQueryParameters(ps, query.getQueryParameters());
        final PostgresReader pr = new PostgresReader(locator);
        try {
            final ObjectConverter<T> converter = getConverter();
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pr.process(rs.getString(1));
                    return Optional.of(converter.from(pr));
                }
            }
        } catch (IOException | ReflectiveOperationException e) {
            throw new SQLException(e);
        }
        return Optional.empty();
    }

    public List<T> toList() throws SQLException {
        final String queryString = query.getQueryString();
        PreparedStatement ps = connection.prepareStatement(queryString);
        fillQueryParameters(ps, query.getQueryParameters());
        final PostgresReader pr = new PostgresReader(locator);
        final ArrayList<T> result = new ArrayList<>();
        try {
            final ObjectConverter<T> converter = getConverter();
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pr.process(rs.getString(1));
                    result.add(converter.from(pr));
                }
            }
        } catch (IOException | ReflectiveOperationException e) {
            throw new SQLException(e);
        }
        return result;
    }

    private <U> RevenjQueryComposer<U> applyTransformWithLambda(Class<U> newManifest, JPQLNoLambdaQueryTransform transform) {
        Optional<JPQLQuery<?>> cachedQuery = cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), null);
        if (cachedQuery == null) {
            cachedQuery = Optional.empty();
            JPQLQuery<U> newQuery = null;
            try {
                newQuery = transform.apply(query, null);
            } catch (QueryTransformException e) {
                throw new RuntimeException(e);
            } finally {
                // Always cache the resulting query, even if it is an error
                cachedQuery = Optional.ofNullable(newQuery);
                cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), null, cachedQuery);
            }
        }
        if (!cachedQuery.isPresent()) {
            return null;
        }
        return new RevenjQueryComposer<>(this, newManifest, (JPQLQuery<U>) cachedQuery.get(), lambdas);
    }

    public <U> RevenjQueryComposer<U> applyTransformWithLambda(Class<U> newManifest, JPQLOneLambdaQueryTransform transform, Object lambda) {
        LambdaInfo lambdaInfo = LambdaInfo.analyze(lambda, lambdas.size(), true);
        if (lambdaInfo == null) {
            return null;
        }
        Optional<JPQLQuery<?>> cachedQuery =
                cachedQueries.findInCache(
                        query,
                        transform.getTransformationTypeCachingTag(),
                        new String[]{lambdaInfo.getLambdaSourceString()});
        if (cachedQuery == null) {
            cachedQuery = Optional.empty();
            JPQLQuery<U> newQuery = null;
            try {
                LambdaAnalysis lambdaAnalysis = lambdaInfo.fullyAnalyze(metamodel, null, true, true, true);
                if (lambdaAnalysis == null) {
                    return null;
                }
                getConfig().checkLambdaSideEffects(lambdaAnalysis);
                newQuery = transform.apply(query, lambdaAnalysis, null);
            } catch (QueryTransformException e) {
                throw new RuntimeException(e);
            } finally {
                // Always cache the resulting query, even if it is an error
                cachedQuery = Optional.ofNullable(newQuery);
                cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), new String[]{lambdaInfo.getLambdaSourceString()}, cachedQuery);
            }
        }
        if (!cachedQuery.isPresent()) {
            return null;
        }
        return new RevenjQueryComposer<>(this, newManifest, (JPQLQuery<U>) cachedQuery.get(), lambdas, lambdaInfo);
    }

    public <U> RevenjQueryComposer<U> applyTransformWithLambdas(Class<U> newManifest, JPQLMultiLambdaQueryTransform transform, Object[] groupingLambdas) {
        LambdaInfo[] lambdaInfos = new LambdaInfo[groupingLambdas.length];
        String[] lambdaSources = new String[lambdaInfos.length];
        for (int n = 0; n < groupingLambdas.length; n++) {
            lambdaInfos[n] = lambdaAnalyzer.extractSurfaceInfo(groupingLambdas[n], lambdas.size() + n, true);
            if (lambdaInfos[n] == null) {
                return null;
            }
            lambdaSources[n] = lambdaInfos[n].getLambdaSourceString();
        }

        Optional<JPQLQuery<?>> cachedQuery =
                cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), lambdaSources);
        if (cachedQuery == null) {
            cachedQuery = Optional.empty();
            JPQLQuery<U> newQuery = null;
            try {
                LambdaAnalysis[] lambdaAnalyses = new LambdaAnalysis[lambdaInfos.length];
                for (int n = 0; n < lambdaInfos.length; n++) {
                    lambdaAnalyses[n] = lambdaInfos[n].fullyAnalyze(metamodel, null, true, true, true);
                    if (lambdaAnalyses[n] == null) {
                        return null;
                    }
                    getConfig().checkLambdaSideEffects(lambdaAnalyses[n]);
                }
                newQuery = transform.apply(query, lambdaAnalyses, null);
            } catch (QueryTransformException e) {
                throw new RuntimeException(e);
            } finally {
                // Always cache the resulting query, even if it is an error
                cachedQuery = Optional.ofNullable(newQuery);
                cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), lambdaSources, cachedQuery);
            }
        }
        if (!cachedQuery.isPresent()) {
            return null;
        }
        return new RevenjQueryComposer<>(this, newManifest, (JPQLQuery<U>) cachedQuery.get(), lambdas, lambdaInfos);
    }

    /**
     * Holds configuration information used when transforming this composer to a new composer.
     * Since a JPAQueryComposer can only be transformed once, we only need one transformationConfig
     * (and it is instantiated lazily).
     */
    private JPQLQueryTransformConfiguration transformationConfig = null;

    public JPQLQueryTransformConfiguration getConfig() {
        if (transformationConfig == null) {
            transformationConfig = jpqlQueryTransformConfigurationFactory.createConfig();
            transformationConfig.metamodel = metamodel;
            transformationConfig.alternateClassLoader = null;
            transformationConfig.isObjectEqualsSafe = true;
            transformationConfig.isCollectionContainsSafe = true;
        }
        return transformationConfig;
    }

    @Override
    public <E extends Exception> RevenjQueryComposer<T> where(Object testLambda) {
        return applyTransformWithLambda(manifest, new WhereTransform(getConfig(), false), testLambda);
    }

    @Override
    public <E extends Exception> RevenjQueryComposer<T> whereWithSource(Object test) {
        return applyTransformWithLambda(manifest, new WhereTransform(getConfig(), true), test);
    }

    @Override
    public <V extends Comparable<V>> RevenjQueryComposer<T> sortedBy(
            Object sorter, boolean isAscending) {
        return applyTransformWithLambda(manifest, new SortingTransform(getConfig(), isAscending), sorter);
    }

    @Override
    public RevenjQueryComposer<T> limit(long n) {
        return applyTransformWithLambda(manifest, new LimitSkipTransform(getConfig(), true, n));
    }

    @Override
    public RevenjQueryComposer<T> skip(long n) {
        return applyTransformWithLambda(manifest, new LimitSkipTransform(getConfig(), false, n));
    }

    @Override
    public RevenjQueryComposer<T> distinct() {
        return applyTransformWithLambda(manifest, new DistinctTransform(getConfig()));
    }

    @Override
    public <U> RevenjQueryComposer<U> select(Object selectLambda) {
        return null;
    }

    @Override
    public <U> RevenjQueryComposer<U> selectWithSource(Object selectLambda) {
        return null;
    }

    @Override
    public <U> QueryComposer<U> selectAll(Object selectLambda) {
        return null;
    }

    @Override
    public <U> QueryComposer<U> selectAllWithSource(Object selectLambda) {
        return null;
    }

    @Override
    public <U> QueryComposer<U> selectAllIterable(Object selectLambda) {
        return null;
    }

    @Override
    public <U> RevenjQueryComposer<Pair<T, U>> join(org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda) {
        return null;
    }

    @Override
    public <U> RevenjQueryComposer<Pair<T, U>> joinWithSource(org.jinq.orm.stream.JinqStream.JoinWithSource<T, U> joinLambda) {
        return null;
    }

    @Override
    public <U> QueryComposer<Pair<T, U>> joinIterable(JoinToIterable<T, U> joinLambda) {
        return null;
    }

    public <U> RevenjQueryComposer<T> joinFetch(org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda) {
        return null;
    }

    public <U> QueryComposer<T> joinFetchIterable(JoinToIterable<T, U> joinLambda) {
        return null;
    }

    @Override
    public <U> RevenjQueryComposer<Pair<T, U>> leftOuterJoin(org.jinq.orm.stream.JinqStream.Join<T, U> joinLambda) {
        return null;
    }

    @Override
    public <U> QueryComposer<Pair<T, U>> leftOuterJoinIterable(JoinToIterable<T, U> joinLambda) {
        return null;
    }

    @Override
    public Long count() {
        try {
            return runCount();
        } catch (SQLException ignore) {
            return null;
        }
    }

    @Override
    public <V extends Number & Comparable<V>> Number sum(Object aggregate, Class<V> collectClass) {
        return null;
    }

    @Override
    public <V extends Comparable<V>> V max(Object aggregate) {
        return null;
    }

    @Override
    public <V extends Comparable<V>> V min(Object aggregate) {
        return null;
    }

    @Override
    public <V extends Number & Comparable<V>> Double avg(Object aggregate) {
        return null;
    }

    @Override
    public <U extends Tuple> U multiaggregate(org.jinq.orm.stream.JinqStream.AggregateSelect<T, ?>[] aggregates) {
        return null;
    }

    @Override
    public <U, W extends Tuple> RevenjQueryComposer<W> groupToTuple(Select<T, U> select, AggregateGroup<U, T, ?>[] aggregates) {
        return null;
    }

    @Override
    public boolean setHint(String name, Object val) {
        return false;
    }
}

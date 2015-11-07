package org.revenj.postgres.jinq;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.postgresql.util.PGobject;
import org.revenj.patterns.DataSource;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresWriter;
import org.revenj.postgres.jinq.jpqlquery.GeneratedQueryParameter;
import org.revenj.postgres.jinq.jpqlquery.JinqPostgresQuery;
import org.revenj.Utils;
import org.revenj.postgres.converters.PostgresTuple;
import org.revenj.postgres.converters.TimestampConverter;
import org.revenj.postgres.jinq.transform.RevenjMultiLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.RevenjNoLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.RevenjOneLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.RevenjQueryTransformConfiguration;
import org.revenj.postgres.jinq.transform.LambdaAnalysis;
import org.revenj.postgres.jinq.transform.LambdaInfo;
import org.revenj.postgres.jinq.transform.LimitSkipTransform;
import org.revenj.postgres.jinq.transform.MetamodelUtil;
import org.revenj.postgres.jinq.transform.QueryTransformException;
import org.revenj.postgres.jinq.transform.SortingTransform;
import org.revenj.postgres.jinq.transform.WhereTransform;
import org.revenj.postgres.PostgresReader;
import org.revenj.patterns.ServiceLocator;

public final class RevenjQueryComposer<T> {

	private static final Map<Class<?>, String> typeMapping = new HashMap<>();

	static {
		typeMapping.put(int.class, "int");
		typeMapping.put(Integer.class, "int");
		typeMapping.put(String.class, "varchar");
		typeMapping.put(long.class, "bigint");
		typeMapping.put(Long.class, "bigint");
		typeMapping.put(BigDecimal.class, "numeric");
		typeMapping.put(float.class, "real");
		typeMapping.put(Float.class, "real");
		typeMapping.put(double.class, "float");
		typeMapping.put(Double.class, "float");
		typeMapping.put(UUID.class, "uuid");
		typeMapping.put(Map.class, "hstore");
		typeMapping.put(byte[].class, "bytea");
	}

	@FunctionalInterface
	public interface GetConnection {
		Connection get() throws SQLException;
	}

	@FunctionalInterface
	public interface ReleaseConnection {
		void release(Connection connection) throws SQLException;
	}

	private final MetamodelUtil metamodel;
	private final RevenjQueryComposerCache cachedQueries;
	private final Connection connection;
	private final ServiceLocator locator;
	private final GetConnection getConnection;
	private final ReleaseConnection releaseConnection;
	private final JinqPostgresQuery<T> query;
	private final Class<T> manifest;

	/**
	 * Holds the chain of lambdas that were used to create this query. This is needed
	 * because query parameters (which are stored in the lambda objects) are only
	 * substituted into the query during query execution, which occurs much later
	 * than query generation.
	 */
	private final List<LambdaInfo> lambdas = new ArrayList<>();

	private RevenjQueryComposer(
			RevenjQueryComposer<?> base,
			Class<T> manifest,
			JinqPostgresQuery<T> query,
			List<LambdaInfo> chainedLambdas,
			LambdaInfo... additionalLambdas) {
		this(base.metamodel, manifest, base.cachedQueries, base.connection, base.locator, base.getConnection, base.releaseConnection, query, chainedLambdas, additionalLambdas);
	}

	private RevenjQueryComposer(
			MetamodelUtil metamodel,
			Class<T> manifest,
			RevenjQueryComposerCache cachedQueries,
			Connection connection,
			ServiceLocator locator,
			GetConnection getConnection,
			ReleaseConnection releaseConnection,
			JinqPostgresQuery<T> query,
			List<LambdaInfo> chainedLambdas,
			LambdaInfo... additionalLambdas) {
		this.metamodel = metamodel;
		this.manifest = manifest;
		this.cachedQueries = cachedQueries;
		this.connection = connection;
		this.locator = locator;
		this.getConnection = getConnection;
		this.releaseConnection = releaseConnection;
		this.query = query;
		lambdas.addAll(chainedLambdas);
		for (LambdaInfo newLambda : additionalLambdas) {
			lambdas.add(newLambda);
		}
	}

	public static <T extends DataSource> RevenjQuery<T> findAll(
			MetamodelUtil metamodel,
			Class<T> manifest,
			RevenjQueryComposerCache cachedQueries,
			Connection conn,
			ServiceLocator locator,
			GetConnection getConnection,
			ReleaseConnection releaseConnection) {
		String sqlSource = metamodel.dataSourceNameFromClass(manifest);
		Optional<JinqPostgresQuery<?>> cachedQuery = cachedQueries.findCachedFindAll(sqlSource);
		if (cachedQuery == null) {
			JinqPostgresQuery<T> query = JinqPostgresQuery.findAll(sqlSource);
			cachedQuery = Optional.of(query);
			cachedQuery = cachedQueries.cacheFindAll(sqlSource, cachedQuery);
		}
		JinqPostgresQuery<T> findAllQuery = (JinqPostgresQuery<T>) cachedQuery.get();
		RevenjQueryComposer<T> queryComposer =
				new RevenjQueryComposer(
						metamodel,
						manifest,
						cachedQueries,
						conn,
						locator,
						getConnection,
						releaseConnection,
						findAllQuery,
						new ArrayList<>());
		return new RevenjQuery<>(queryComposer);
	}

	private static String getTypeFor(Class<?> manifest) {
		return typeMapping.get(manifest);
	}

	private static String getElementTypeFor(Object[] elements) {
		for (Object item : elements) {
			if (item != null) {
				String type = getTypeFor(item.getClass());
				if (type != null) {
					return type;
				}
			}
		}
		return "unknown";
	}

	private static final PGobject EMPTY_ARRAY;

	static {
		EMPTY_ARRAY = new PGobject();
		EMPTY_ARRAY.setType("record[]");
		try {
			EMPTY_ARRAY.setValue("{}");
		} catch (SQLException ignore) {
		}
	}

	public static void fillQueryParameters(
			Connection connection,
			ServiceLocator locator,
			PreparedStatement ps,
			List<GeneratedQueryParameter> parameters,
			List<LambdaInfo> lambdas) throws SQLException {
		PostgresWriter writer = null;
		for (int i = 0; i < parameters.size(); i++) {
			GeneratedQueryParameter param = parameters.get(i);
			Object value;
			if (param.fieldName == null) {
				value = lambdas.get(param.lambdaIndex).getCapturedArg(param.argIndex);
			} else {
				value = lambdas.get(param.lambdaIndex).getField(param.fieldName);
			}
			if (value == null) {
				ps.setObject(i + 1, null);
				continue;
			}
			Object[] elements = null;
			if (value instanceof Collection) {
				Collection collection = (Collection) value;
				elements = new Object[collection.size()];
				int x = 0;
				for (Object item : collection) {
					elements[x++] = item;
				}
			} else if (value instanceof Object[]) {
				elements = (Object[]) value;
			}
			if (elements == null) {
				Class<?> manifest = value.getClass();
				Optional<ObjectConverter> converter = getConverterFor(locator, manifest);
				if (converter.isPresent()) {
					PGobject pgo = new PGobject();
					if (writer == null) writer = PostgresWriter.create();
					writer.reset();
					PostgresTuple tuple = converter.get().to(value);
					tuple.buildTuple(writer, false);
					pgo.setValue(writer.toString());
					pgo.setType(converter.get().getDbName());
					ps.setObject(i + 1, pgo);
				} else if (value instanceof LocalDate) {
					ps.setDate(i + 1, java.sql.Date.valueOf((LocalDate) value));
					//if (writer == null) writer = PostgresWriter.create();
					//DateConverter.setParameter(writer, ps, i + 1, (LocalDate) value);
				} else if (value instanceof LocalDateTime) {
					if (writer == null) writer = PostgresWriter.create();
					TimestampConverter.setParameter(writer, ps, i + 1, (LocalDateTime) value);
				} else if (value instanceof OffsetDateTime) {
					if (writer == null) writer = PostgresWriter.create();
					TimestampConverter.setParameter(writer, ps, i + 1, (OffsetDateTime) value);
				} else {
					ps.setObject(i + 1, value);
				}
			} else {
				Class<?> manifest = null;
				for (Object item : elements) {
					if (item != null) {
						manifest = item.getClass();
						break;
					}
				}
				Optional<ObjectConverter> converter = manifest != null ? getConverterFor(locator, manifest) : Optional.<ObjectConverter>empty();
				if (converter.isPresent()) {
					ObjectConverter oc = converter.get();
					Object[] pgos = new Object[elements.length];
					if (writer == null) writer = PostgresWriter.create();
					for (int x = 0; x < pgos.length; x++) {
						Object item = elements[x];
						if (item != null) {
							PGobject pgo = new PGobject();
							writer.reset();
							oc.to(item).buildTuple(writer, false);
							pgo.setValue(writer.toString());
							pgo.setType(oc.getDbName());
							ps.setObject(i + 1, pgo);
						}
					}
					java.sql.Array array = connection.createArrayOf(oc.getDbName(), pgos);
					ps.setArray(i + 1, array);
				} else {
					if (elements.length == 0) {
						ps.setObject(i + 1, EMPTY_ARRAY);
					} else {
						java.sql.Array array = connection.createArrayOf(getElementTypeFor(elements), elements);
						ps.setArray(i + 1, array);
					}
				}
			}
		}
		if (writer != null) writer.close();
	}

	private static final ConcurrentMap<Class<?>, Optional<ObjectConverter>> objectConverters = new ConcurrentHashMap<>();

	private static Optional<ObjectConverter> getConverterFor(ServiceLocator locator, Class<?> manifest) {
		return objectConverters.computeIfAbsent(manifest, clazz ->
		{
			try {
				ObjectConverter result = (ObjectConverter) locator.resolve(Utils.makeGenericType(ObjectConverter.class, clazz));
				return Optional.of(result);
			} catch (Exception ignore) {
				return Optional.empty();
			}
		});
	}

	private Connection getConnection() throws SQLException {
		if (connection != null) return connection;
		return getConnection.get();
	}

	private void releaseConnection(Connection connection) throws SQLException {
		if (this.connection == null) releaseConnection.release(connection);
	}

	public long count() throws SQLException {
		final String queryString = query.getQueryString();
		Connection connection = getConnection();
		try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM (" + queryString + ") sq")) {
			fillQueryParameters(connection, locator, ps, query.getQueryParameters(), lambdas);
			try (final ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
			}
		} finally {
			releaseConnection(connection);
		}
		return 0;
	}

	public boolean any() throws SQLException {
		final String queryString = query.getQueryString();
		Connection connection = getConnection();
		try (PreparedStatement ps = connection.prepareStatement("SELECT EXISTS(" + queryString + ")")) {
			fillQueryParameters(connection, locator, ps, query.getQueryParameters(), lambdas);
			try (final ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getBoolean(1);
				}
			}
		} finally {
			releaseConnection(connection);
		}
		return false;
	}

	//TODO: optimize

	public boolean all(Object lambda) throws SQLException {
		long filter = this.where(lambda).count();
		long all = this.count();
		return filter == all && all > 0;
	}

	public boolean none() throws SQLException {
		final String queryString = query.getQueryString();
		Connection connection = getConnection();
		try (PreparedStatement ps = connection.prepareStatement("SELECT NOT EXISTS(" + queryString + ")")) {
			fillQueryParameters(connection, locator, ps, query.getQueryParameters(), lambdas);
			try (final ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getBoolean(1);
				}
			}
		} finally {
			releaseConnection(connection);
		}
		return true;
	}

	public Optional<T> first() throws SQLException {
		final String queryString = query.getQueryString();
		Connection connection = getConnection();
		try (PreparedStatement ps = connection.prepareStatement(queryString)) {
			fillQueryParameters(connection, locator, ps, query.getQueryParameters(), lambdas);
			final PostgresReader pr = new PostgresReader(locator);
			try {
				final ObjectConverter<T> converter = getConverterFor(locator, manifest).get();
				try (final ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						pr.process(rs.getString(1));
						return Optional.of(converter.from(pr));
					}
				}
			} catch (IOException e) {
				throw new SQLException(e);
			} finally {
				releaseConnection(connection);
			}
			return Optional.empty();
		}
	}

	public List<T> toList() throws SQLException {
		final String queryString = query.getQueryString();
		Connection connection = getConnection();
		try (PreparedStatement ps = connection.prepareStatement(queryString)) {
			fillQueryParameters(connection, locator, ps, query.getQueryParameters(), lambdas);
			final PostgresReader pr = new PostgresReader(locator);
			final ArrayList<T> result = new ArrayList<>();
			try {
				final ObjectConverter<T> converter = getConverterFor(locator, manifest).get();
				try (final ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						pr.process(rs.getString(1));
						result.add(converter.from(pr));
					}
				}
			} catch (IOException e) {
				throw new SQLException(e);
			} finally {
				releaseConnection(connection);
			}
			return result;
		}
	}

	private <U> RevenjQueryComposer<U> applyTransformWithLambda(
			Class<U> newManifest,
			RevenjNoLambdaQueryTransform transform) {
		Optional<JinqPostgresQuery<?>> cachedQuery = cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), null);
		if (cachedQuery == null) {
			cachedQuery = Optional.empty();
			JinqPostgresQuery<U> newQuery = null;
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
		return new RevenjQueryComposer<>(this, newManifest, (JinqPostgresQuery<U>) cachedQuery.get(), lambdas);
	}

	public <U> RevenjQueryComposer<U> applyTransformWithLambda(
			Class<U> newManifest,
			RevenjOneLambdaQueryTransform transform,
			Object lambda) {
		LambdaInfo lambdaInfo = LambdaInfo.analyze(lambda, lambdas.size(), true);
		if (lambdaInfo == null) {
			return null;
		}
		Optional<JinqPostgresQuery<?>> cachedQuery =
				cachedQueries.findInCache(
						query,
						transform.getTransformationTypeCachingTag(),
						new String[]{lambdaInfo.getLambdaSourceString()});
		if (cachedQuery == null) {
			JinqPostgresQuery<U> newQuery = null;
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
		return new RevenjQueryComposer<>(this, newManifest, (JinqPostgresQuery<U>) cachedQuery.get(), lambdas, lambdaInfo);
	}

	public <U> RevenjQueryComposer<U> applyTransformWithLambdas(
			Class<U> newManifest,
			RevenjMultiLambdaQueryTransform transform,
			Object[] groupingLambdas) {
		LambdaInfo[] lambdaInfos = new LambdaInfo[groupingLambdas.length];
		String[] lambdaSources = new String[lambdaInfos.length];
		for (int n = 0; n < groupingLambdas.length; n++) {
			lambdaInfos[n] = LambdaInfo.analyze(groupingLambdas[n], lambdas.size() + n, true);
			if (lambdaInfos[n] == null) {
				return null;
			}
			lambdaSources[n] = lambdaInfos[n].getLambdaSourceString();
		}

		Optional<JinqPostgresQuery<?>> cachedQuery =
				cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), lambdaSources);
		if (cachedQuery == null) {
			JinqPostgresQuery<U> newQuery = null;
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
		return new RevenjQueryComposer<>(this, newManifest, (JinqPostgresQuery<U>) cachedQuery.get(), lambdas, lambdaInfos);
	}

	/**
	 * Holds configuration information used when transforming this composer to a new composer.
	 * Since a JPAQueryComposer can only be transformed once, we only need one transformationConfig
	 * (and it is instantiated lazily).
	 */
	private RevenjQueryTransformConfiguration transformationConfig = null;

	public RevenjQueryTransformConfiguration getConfig() {
		if (transformationConfig == null) {
			transformationConfig = new RevenjQueryTransformConfiguration();
			transformationConfig.metamodel = metamodel;
			transformationConfig.alternateClassLoader = null;
			transformationConfig.isObjectEqualsSafe = true;
			transformationConfig.isCollectionContainsSafe = true;
		}
		return transformationConfig;
	}

	public <E extends Exception> RevenjQueryComposer<T> where(Object testLambda) {
		return applyTransformWithLambda(manifest, new WhereTransform(getConfig(), false), testLambda);
	}

	public <V extends Comparable<V>> RevenjQueryComposer<T> sortedBy(
			Object sorter, boolean isAscending) {
		return applyTransformWithLambda(manifest, new SortingTransform(getConfig(), isAscending), sorter);
	}

	public RevenjQueryComposer<T> limit(long n) {
		return applyTransformWithLambda(manifest, new LimitSkipTransform(getConfig(), true, n));
	}

	public RevenjQueryComposer<T> skip(long n) {
		return applyTransformWithLambda(manifest, new LimitSkipTransform(getConfig(), false, n));
	}
}

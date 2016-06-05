package org.revenj.database.postgres.jinq;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.postgresql.core.Oid;
import org.postgresql.util.PGobject;
import org.revenj.patterns.DataSource;
import org.revenj.patterns.Specification;
import org.revenj.database.postgres.ObjectConverter;
import org.revenj.database.postgres.PostgresWriter;
import org.revenj.database.postgres.converters.ArrayTuple;
import org.revenj.database.postgres.jinq.jpqlquery.GeneratedQueryParameter;
import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;
import org.revenj.Utils;
import org.revenj.database.postgres.converters.PostgresTuple;
import org.revenj.database.postgres.converters.TimestampConverter;
import org.revenj.database.postgres.jinq.transform.RevenjMultiLambdaQueryTransform;
import org.revenj.database.postgres.jinq.transform.RevenjNoLambdaQueryTransform;
import org.revenj.database.postgres.jinq.transform.RevenjOneLambdaQueryTransform;
import org.revenj.database.postgres.jinq.transform.RevenjQueryTransformConfiguration;
import org.revenj.database.postgres.jinq.transform.LambdaAnalysis;
import org.revenj.database.postgres.jinq.transform.LambdaInfo;
import org.revenj.database.postgres.jinq.transform.LimitSkipTransform;
import org.revenj.database.postgres.jinq.transform.MetamodelUtil;
import org.revenj.database.postgres.jinq.transform.QueryTransformException;
import org.revenj.database.postgres.jinq.transform.SortingTransform;
import org.revenj.database.postgres.jinq.transform.WhereTransform;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.patterns.ServiceLocator;

public final class RevenjQueryComposer<T> {

	private static final Map<Class<?>, String> typeMapping = new HashMap<>();
	private static final Map<String, Integer> sqlIdMapping = new HashMap<>();

	private static void addMapping(Class<?> manifest, String dbType, int oid, int sqlId) {
		typeMapping.put(manifest, dbType);
		sqlIdMapping.put(manifest.getName(), sqlId);
	}

	static {
		addMapping(int.class, "int", Oid.INT4, Types.INTEGER);
		addMapping(Integer.class, "int", Oid.INT4, Types.INTEGER);
		addMapping(String.class, "varchar", Oid.VARCHAR, Types.VARCHAR);
		addMapping(long.class, "bigint", Oid.INT8, Types.BIGINT);
		addMapping(Long.class, "bigint", Oid.INT8, Types.BIGINT);
		addMapping(BigDecimal.class, "numeric", Oid.NUMERIC, Types.NUMERIC);
		addMapping(float.class, "real", Oid.FLOAT4, Types.FLOAT);
		addMapping(Float.class, "real", Oid.FLOAT4, Types.FLOAT);
		addMapping(double.class, "float", Oid.FLOAT8, Types.DOUBLE);
		addMapping(Double.class, "float", Oid.FLOAT8, Types.DOUBLE);
		addMapping(boolean.class, "bool", Oid.BOOL, Types.BOOLEAN);
		addMapping(Boolean.class, "bool", Oid.BOOL, Types.BOOLEAN);
		addMapping(LocalDate.class, "date", Oid.DATE, Types.DATE);
		addMapping(OffsetDateTime.class, "timestamptz", Oid.TIMESTAMPTZ, Types.TIMESTAMP_WITH_TIMEZONE);
		addMapping(UUID.class, "uuid", Oid.UUID, Types.OTHER);
		addMapping(Map.class, "hstore", -1, Types.OTHER);
		addMapping(byte[].class, "bytea", Oid.BYTEA, Types.BLOB);
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
	private final ClassLoader loader;
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

	public int getLambdaCount() {
		return lambdas.size();
	}

	private RevenjQueryComposer(
			RevenjQueryComposer<?> base,
			Class<T> manifest,
			JinqPostgresQuery<T> query,
			List<LambdaInfo> chainedLambdas,
			LambdaInfo... additionalLambdas) {
		this(base.metamodel, base.loader, manifest, base.cachedQueries, base.connection, base.locator, base.getConnection, base.releaseConnection, query, chainedLambdas, additionalLambdas);
	}

	private RevenjQueryComposer(
			MetamodelUtil metamodel,
			ClassLoader loader,
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
		this.loader = loader;
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
			ClassLoader loader,
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
						loader,
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
			int parameterOffset,
			List<GeneratedQueryParameter> parameters,
			List<LambdaInfo> lambdas) throws SQLException {
		PostgresWriter writer = null;
		for (int i = 0; i < parameters.size(); i++) {
			GeneratedQueryParameter param = parameters.get(i);
			Object value = param.getValue.apply(lambdas.get(param.lambdaIndex));
			if (value == null) {
				Integer sqlId = sqlIdMapping.get(param.javaType);
				if (sqlId == null || sqlId == -1) {
					if (param.sqlType != null) {
						PGobject pgo = new PGobject();
						pgo.setType(param.sqlType);
						pgo.setValue("null");
						ps.setObject(i + 1 + parameterOffset, pgo);
					} else {
						ps.setObject(i + 1 + parameterOffset, null);
					}
				} else {
					ps.setNull(i + 1 + parameterOffset, sqlId);
				}
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
					ps.setObject(i + 1 + parameterOffset, pgo);
				} else if (value instanceof LocalDate) {
					ps.setDate(i + 1 + parameterOffset, java.sql.Date.valueOf((LocalDate) value));
					//if (writer == null) writer = PostgresWriter.create();
					//DateConverter.setParameter(writer, ps, i + 1, (LocalDate) value);
				} else if (value instanceof LocalDateTime) {
					if (writer == null) writer = PostgresWriter.create();
					TimestampConverter.setParameter(writer, ps, i + 1, (LocalDateTime) value);
				} else if (value instanceof OffsetDateTime) {
					if (writer == null) writer = PostgresWriter.create();
					TimestampConverter.setParameter(writer, ps, i + 1, (OffsetDateTime) value);
				} else {
					ps.setObject(i + 1 + parameterOffset, value);
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
					ObjectConverter<Object> oc = converter.get();
					if (writer == null) writer = PostgresWriter.create();
					writer.reset();
					PostgresTuple tuple = ArrayTuple.create(elements, oc::to);
					PGobject pgo = new PGobject();
					pgo.setType(oc.getDbName() + "[]");
					tuple.buildTuple(writer, false);
					pgo.setValue(writer.toString());
					ps.setObject(i + 1 + parameterOffset, pgo);
				} else {
					String type = getElementTypeFor(elements);
					if ("unknown".equals(type)) {
						if (elements.length == 0) {
							//TODO: provide null instead !?
							if (param.sqlType != null) {
								PGobject pgo = new PGobject();
								pgo.setType(param.sqlType);
								pgo.setValue("{}");
								ps.setObject(i + 1 + parameterOffset, pgo);
							} else {
								ps.setObject(i + 1 + parameterOffset, EMPTY_ARRAY);
							}
							continue;
						} else {
							// throw meaningfull error!?
						}
					}
					java.sql.Array array = connection.createArrayOf(type, elements);
					ps.setArray(i + 1 + parameterOffset, array);
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
			fillQueryParameters(connection, locator, ps, 0, query.getQueryParameters(), lambdas);
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
			fillQueryParameters(connection, locator, ps, 0, query.getQueryParameters(), lambdas);
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

	public boolean all(LambdaInfo lambda) throws SQLException {
		long filter = this.where(lambda).count();
		long all = this.count();
		return filter == all && all > 0;
	}

	public boolean none() throws SQLException {
		final String queryString = query.getQueryString();
		Connection connection = getConnection();
		try (PreparedStatement ps = connection.prepareStatement("SELECT NOT EXISTS(" + queryString + ")")) {
			fillQueryParameters(connection, locator, ps, 0, query.getQueryParameters(), lambdas);
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
			fillQueryParameters(connection, locator, ps, 0, query.getQueryParameters(), lambdas);
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
			fillQueryParameters(connection, locator, ps, 0, query.getQueryParameters(), lambdas);
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
			LambdaInfo lambdaInfo) {
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
				LambdaAnalysis lambdaAnalysis = lambdaInfo.fullyAnalyze(metamodel, loader, true, true, true, true);
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
					lambdaAnalyses[n] = lambdaInfos[n].fullyAnalyze(metamodel, null, true, true, true, true);
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
	 * Since a RevenjQueryComposer can only be transformed once, we only need one transformationConfig
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

	public Specification rewrite(Specification filter) {
		Function<Specification, Specification> conversion = metamodel.lookupRewrite(filter);
		return conversion != null ? conversion.apply(filter) : filter;
	}

	public <E extends Exception> RevenjQueryComposer<T> where(LambdaInfo lambdaInfo) {
		return applyTransformWithLambda(manifest, new WhereTransform(getConfig(), false), lambdaInfo);
	}

	public <V extends Comparable<V>> RevenjQueryComposer<T> sortedBy(LambdaInfo lambdaInfo, boolean isAscending) {
		return applyTransformWithLambda(manifest, new SortingTransform(getConfig(), isAscending), lambdaInfo);
	}

	public RevenjQueryComposer<T> limit(long n) {
		return applyTransformWithLambda(manifest, new LimitSkipTransform(getConfig(), true, n));
	}

	public RevenjQueryComposer<T> skip(long n) {
		return applyTransformWithLambda(manifest, new LimitSkipTransform(getConfig(), false, n));
	}
}
